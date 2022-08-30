import org.gradle.api.GradleException

// Property getters are required because gradle's caching is unpredictable between invocations.
object Config {

    // Current git branch
    private val currentBranch get() = Git.revParseHead

    // Number of commits since inception
    val versionCode get() = Git.revListCount

    val versionName: String get() {

        // determine the easy ones
        if (currentBranch.isRelease) return currentBranch.semanticVersion!!
        if (currentBranch.isMaster) return Git.getLatestReleaseTag()
        if (currentBranch.isDevelop) return Git.getNextSemanticVersion()

        // see which develop/release branch we're closest to, infer version
        val closestParent = Git.getParentBranchCandidates().sortedWith(
            compareBy(
                { Git.getCommitsAhead(currentBranch, it) },
                { Git.getCommitsBehind(currentBranch, it) }
            )).first()
        if (closestParent.isDevelop) return Git.getNextSemanticVersion()
        return closestParent.semanticVersion!!
    }

    /*
        Merges commits from any upstream release branches, in order.
        WARNING: Will modify your local and remote repo if run, so be careful.
        Intended to be run from a dedicated CI/CD system in a controlled environment.
        Returns the branches pulled from to support unit testing.
     */
    fun pullCommitsFromUpstreamReleaseBranches(): List<Branch> {
        if (!currentBranch.isRelease && !currentBranch.isDevelop)
            throw GradleException("Commit migration expected on release or develop branches only.")
        if (!Git.isBuildServer && !Git.isMocking)
            throw GradleException("Commit migration expected to be run on build server.")

        // iterate ordered release branches, exit when matching, merge.
        val pulledFrom = mutableListOf<Branch>()
        Git.getMigrationBranchCandidates().forEach {
            if (it.name == "origin/$currentBranch") {
                if (pulledFrom.isNotEmpty()) Git.push(currentBranch)
                return pulledFrom
            }
            if (Git.mergeFrom(currentBranch, it)) pulledFrom.add(it)
        }
        return pulledFrom
    }

    fun retireReleaseBranch() {
        if (!currentBranch.isRelease) throw GradleException("Attempting to retire non-release branch!")
        Git.retireReleaseBranch(currentBranch)
    }

}