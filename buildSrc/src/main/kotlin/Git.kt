import Git.Commands.run
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Git interactions used to derive versionName/codes. Shells support mocking with a little setup.
 * See ConfigTests.kt for sample responses and testing implementation.
 *
 * This implementation uses remote refs for inspecting tags and branches, but could use a local
 * methodology instead. Currently, if we can't reach origin because of account issues or because
 * we're offline, fail if we're on a build server but proceed for local builds. Logic could be
 * adjusted as needed. Remote verification is guaranteed to be accurate and up to date, and sets
 * us up for a future enhancement of supporting headless checkouts.
 */
object Git {

    // example method of determining whether we're in a CI/CD system (Azure)
    val isBuildServer = System.getenv("SYSTEM_SERVERTYPE") == "hosted"

    val isMocking get() = Commands.mocks.size > 0

    // local branch name
    val revParseHead get() = Branch(Commands.branchName().run().trim())

    // local count of commits to this branch since inception
    val revListCount get() = Commands.commitsSinceInception().run().trim().toInt()

    // retrieves remote branches, filters by releases, then orders oldest first
    // includes simple filter to keep the list small
    // survives error locally; fails in pipeline
    val releaseBranches
        get() = Commands.releaseBranches().run(returnBlankStringIfError = !isBuildServer)
            .split("\n", ",")
            .map { Branch(it) }
            .filter { it.isRelease }
            .sortedBy { it.modif }
            .sortedBy { it.minor }
            .sortedBy { it.major }

    // list of remote release tags, oldest first. survives error locally
    val releaseTags
        get() = Commands.remoteTags().run(returnBlankStringIfError = !isBuildServer)
            .split("\n", ",")
            .map { Tag(it) }
            .filter { it.isRelease }
            .sortedBy { it.modif }
            .sortedBy { it.minor }
            .sortedBy { it.major }

    // compares to remote
    fun getCommitsAhead(fromBranch: Branch, toBranch: Branch) = Commands.commitsAhead(fromBranch, toBranch).run().toInt()

    fun getCommitsBehind(fromBranch: Branch, toBranch: Branch) = Commands.commitsBehind(fromBranch, toBranch).run().toInt()

    fun mergeFrom(base: Branch, head: Branch): Boolean {
        if (getCommitsBehind(base, head) == 0) return false
        Commands.merge(head).run()
        return true
    }

    fun push(currentBranch: Branch) = Commands.push(currentBranch).run()

    fun retireReleaseBranch(currentBranch: Branch) {
        val master = Branch("master")
        with(Commands) {
            run(
                addReleaseTag(currentBranch),
                pushReleaseTag(currentBranch),
                checkout(master),
                pull(),
                merge(currentBranch),
                push(master),
                checkout(currentBranch),
                deleteReleaseBranch(currentBranch)
            )
        }
    }

    fun getParentBranchCandidates() = releaseBranches + Branch("origin/develop")

    fun getMigrationBranchCandidates() =
        listOf(
            Branch("origin/master"),
            *releaseBranches.toTypedArray(),
            Branch("origin/develop"))

    fun getLatestReleaseTag(): String {
        if (releaseTags.isEmpty()) return "0.0.0"
        return releaseTags.last().semanticVersion!!
    }

    fun getNextSemanticVersion(): String {
        if (releaseBranches.isNotEmpty()) return releaseBranches.last().nextSemanticVersion!!
        if (releaseTags.isEmpty()) return "0.0.0"
        return releaseTags.last().nextSemanticVersion!!
    }

    fun pruneLocalUntracked() {
        Commands.fetch().run()
        Commands.pruneLocalUntrackedBranches().run()
    }

    object Commands {

        val mocks = HashMap<String, String>()

        fun branchName() = "git rev-parse --abbrev-ref HEAD"
        fun commitsSinceInception() = "git rev-list HEAD --count"
        fun releaseBranches() = "git branch -r --list ${Branch.gitFilter}"
        fun remoteTags() = "git ls-remote --tags origin"
        fun commitsAhead(fromBranch: Branch, toBranch: Branch) = "git rev-list --left-only --count ${fromBranch}...${toBranch}"
        fun commitsBehind(fromBranch: Branch, toBranch: Branch) = "git rev-list --right-only --count ${fromBranch}...${toBranch}"
        fun checkout(branch: Branch) = "git checkout $branch"
        fun pull() = "git pull"
        fun merge(head: Branch) = "git merge $head"
        fun push(currentBranch: Branch) = "git push -u origin $currentBranch"
        fun addReleaseTag(currentBranch: Branch) = "git tag -a v${currentBranch.semanticVersion} -m 'Master release tag'"
        fun pushReleaseTag(currentBranch: Branch) = "git push origin v${currentBranch.semanticVersion}"
        fun deleteReleaseBranch(currentBranch: Branch) = "git push --delete origin $currentBranch"
        fun fetch() = "git fetch"
        fun pruneLocalUntrackedBranches() = "git branch -vv | grep ': gone]' | awk '{print \$1}' | xargs git branch -D"

        fun run(vararg command: String) {
            for (c in command) c.run()
        }

        fun String.run(returnBlankStringIfError: Boolean = false): String {
            if (mocks.containsKey(this)) return mocks[this]!!

            if (isMocking && (this.contains("git push") || this.contains("git merge") || this.contains("git tag"))) {
                throw GradleException("Attempting dangerous operation with mocked data!: $this")
            }

            val standardByteOut = java.io.ByteArrayOutputStream()
            val errorByteOut = java.io.ByteArrayOutputStream()
            val command = this
            val result = project.exec {
                commandLine = listOf("bash", "-c", command)
                standardOutput = standardByteOut
                errorOutput = errorByteOut
                isIgnoreExitValue = true  // we'll throw the error more elegantly below
            }
            if (result.exitValue != 0) {
                val err = """
                    Error executing shell command: 
                    $this
                    Terminated with non-zero exit value ${result.exitValue}
                    ${String(errorByteOut.toByteArray())}
                    """.trimIndent()
                if (returnBlankStringIfError) {
                    println("Ignoring error.")
                    println(err)
                    return ""
                }
                throw GradleException(err)
            }
            return String(standardByteOut.toByteArray())
        }

    }

    private lateinit var project: Project
    fun initialize(project: Project) {
        this.project = project
    }

}