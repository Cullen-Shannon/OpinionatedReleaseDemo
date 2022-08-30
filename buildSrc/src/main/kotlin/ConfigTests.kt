import org.gradle.api.GradleException

// Executing from a gradle task app:testAutoVersioning. Haven't found a better way to unit test yet.
// Review scenario for values. Mocks are cleared after each scenario method call.
object ConfigTests {

    fun run() {

        clearMocks()

        // release branches
        scenario("releases/2.0.2", "2.0.2")
        scenario("releases/2.1", "2.1.0")

        // master, works with or without release branches
        scenario("master", "1.9.1", releaseBranchesExist = true)
        scenario("master", "1.9.1", releaseBranchesExist = false)

        // develop -- use next version. Depends whether release branches exist
        scenario("develop", "2.2.0", releaseBranchesExist = true)
        scenario("develop", "1.10.0", releaseBranchesExist = false)

        // feature branched off develop
        mockCommitsAheadAndBehind("testFeatureBranch", "origin/releases/2.0.1", 500, 0)
        mockCommitsAheadAndBehind("testFeatureBranch", "origin/releases/2.0.2", 400, 0)
        mockCommitsAheadAndBehind("testFeatureBranch", "origin/releases/2.1", 300, 0)
        mockCommitsAheadAndBehind("testFeatureBranch", "origin/releases/2.1.1", 200, 0)
        mockCommitsAheadAndBehind("testFeatureBranch", "origin/develop", 50, 100)
        scenario("testFeatureBranch", "2.2.0")

        // hotfix branch off of 2.1
        mockCommitsAheadAndBehind("testFeatureBranch", "origin/releases/2.0.1", 200, 0)
        mockCommitsAheadAndBehind("testFeatureBranch", "origin/releases/2.0.2", 100, 0)
        mockCommitsAheadAndBehind("testFeatureBranch", "origin/releases/2.1", 4, 1)
        mockCommitsAheadAndBehind("testFeatureBranch", "origin/releases/2.1.1", 4, 100)
        mockCommitsAheadAndBehind("testFeatureBranch", "origin/develop", 4, 200)
        scenario("testFeatureBranch", "2.1.0")

        // weird cases, like no tags and no release branches
        scenario("testFeatureBranch", "0.0.0",
            releaseBranchesExist = false,
            releaseTagsExist = false
        )
        scenario("develop", "0.0.0",
            releaseBranchesExist = false,
            releaseTagsExist = false
        )
        scenario("master", "0.0.0",
            releaseBranchesExist = false,
            releaseTagsExist = false
        )

        // branch migration -- develop, up to date
        mockCommitsAheadAndBehind("develop", "origin/master", 500, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.0.1", 400, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.0.2", 300, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.1", 200, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.1.1", 100, 0)
        scenario("develop", "2.2.0", pulledFromSemanticVersion = emptyList())

        // develop, behind latest
        mockCommitsAheadAndBehind("develop", "origin/master", 500, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.0.1", 400, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.0.2", 300, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.1", 200, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.1.1", 100, 10)
        Git.Commands.merge(Branch("origin/releases/2.1.1")).mock("SUCCESS")
        scenario("develop", "2.2.0", pulledFromSemanticVersion = listOf("2.1.1"))

        // develop, behind last two
        mockCommitsAheadAndBehind("develop", "origin/master", 500, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.0.1", 400, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.0.2", 300, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.1", 200, 5)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.1.1", 100, 10)
        Git.Commands.merge(Branch("origin/releases/2.1")).mock("SUCCESS")
        Git.Commands.merge(Branch("origin/releases/2.1.1")).mock("SUCCESS")
        scenario("develop", "2.2.0", pulledFromSemanticVersion = listOf("2.1.0", "2.1.1"))

        // develop, behind last two reverse order
        mockCommitsAheadAndBehind("develop", "origin/master", 500, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.0.1", 400, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.0.2", 300, 0)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.1", 200, 10)
        mockCommitsAheadAndBehind("develop", "origin/releases/2.1.1", 100, 5)
        Git.Commands.merge(Branch("origin/releases/2.1")).mock("SUCCESS")
        Git.Commands.merge(Branch("origin/releases/2.1.1")).mock("SUCCESS")
        scenario("develop", "2.2.0", pulledFromSemanticVersion = listOf("2.1.0", "2.1.1"))

        // latest release, up to date
        mockCommitsAheadAndBehind("releases/2.1.1", "origin/master", 400, 0)
        mockCommitsAheadAndBehind("releases/2.1.1", "origin/releases/2.0.1", 300, 0)
        mockCommitsAheadAndBehind("releases/2.1.1", "origin/releases/2.0.2", 200, 0)
        mockCommitsAheadAndBehind("releases/2.1.1", "origin/releases/2.1", 100, 0)
        scenario("releases/2.1.1", "2.1.1", pulledFromSemanticVersion = emptyList())

        // latest release, behind hotfix
        mockCommitsAheadAndBehind("releases/2.1.1", "origin/master", 400, 0)
        mockCommitsAheadAndBehind("releases/2.1.1", "origin/releases/2.0.1", 300, 0)
        mockCommitsAheadAndBehind("releases/2.1.1", "origin/releases/2.0.2", 200, 0)
        mockCommitsAheadAndBehind("releases/2.1.1", "origin/releases/2.1", 100, 5)
        Git.Commands.merge(Branch("origin/releases/2.1")).mock("SUCCESS")
        scenario("releases/2.1.1", "2.1.1", pulledFromSemanticVersion = listOf("2.1.0"))

        // latest release, behind two hotfixes
        mockCommitsAheadAndBehind("releases/2.1.1", "origin/master", 400, 0)
        mockCommitsAheadAndBehind("releases/2.1.1", "origin/releases/2.0.1", 300, 0)
        mockCommitsAheadAndBehind("releases/2.1.1", "origin/releases/2.0.2", 200, 10)
        mockCommitsAheadAndBehind("releases/2.1.1", "origin/releases/2.1", 100, 5)
        Git.Commands.merge(Branch("origin/releases/2.0.2")).mock("SUCCESS")
        Git.Commands.merge(Branch("origin/releases/2.1")).mock("SUCCESS")
        scenario("releases/2.1.1", "2.1.1", pulledFromSemanticVersion = listOf("2.0.2", "2.1.0"))

    }

    private fun scenario(
        branchName: String,
        assertVersion: String,
        releaseBranchesExist: Boolean = true,
        releaseTagsExist: Boolean = true,
        pulledFromSemanticVersion: List<String>? = null
    ) {

        Git.Commands.branchName().mock(branchName)

        if (releaseTagsExist) {
            Git.Commands.remoteTags().mock("""
                    dkjfgegiuhrgirtern9rertnrtrtgerg67ergedf        refs/tags/v1.8.1
                    dkjfgegiuhrgirtern9rertnrtrtgerg67ergedf        refs/tags/v1.9.1
                    dkjfgegiuhrgirtern9rertnrtrtgerg67ergedf        refs/tags/v1.8
                    dkjfgegiuhrgirtern9rertnrtrtgerg67ergedf        refs/tags/v1.8.badExtraStuff
                    dkjfgegiuhrgirtern9rertnrtrtgerg67ergedf        refs/tags/nonReleaseTagDummy
                    dkjfgegiuhrgirtern9rertnrtrtgerg67ergedf        refs/tags/v1.9.0
                    dkjfgegiuhrgirtern9rertnrtrtgerg67ergedf        refs/tags/v1.8.2
                """.trimIndent())
            assert(Git.releaseTags.size == 5)
            assert(Git.releaseTags[0].name.endsWith("1.8"))
            assert(Git.releaseTags[1].name.endsWith("1.8.1"))
            assert(Git.releaseTags[2].name.endsWith("1.8.2"))
            assert(Git.releaseTags[3].name.endsWith("1.9.0"))
            assert(Git.releaseTags[4].name.endsWith("1.9.1"))
        } else {
            Git.Commands.remoteTags().mock("")
            assert(Git.releaseTags.isEmpty())
        }

        if (releaseBranchesExist) {

            Git.Commands.releaseBranches().mock("""
                    origin/releases/2.1.1
                    origin/releases/2.0.1
                    origin/releases/badNameToExclude
                    origin/releases/2.1
                    origin/releases/2.0.2
                """.trimIndent()
            )
            assert(Git.releaseBranches.size == 4)
            assert(Git.releaseBranches[0].name == "origin/releases/2.0.1")
            assert(Git.releaseBranches[1].name == "origin/releases/2.0.2")
            assert(Git.releaseBranches[2].name == "origin/releases/2.1")
            assert(Git.releaseBranches[3].name == "origin/releases/2.1.1")
        } else {
            Git.Commands.releaseBranches().mock("")
            assert(Git.releaseBranches.isEmpty())
        }

        if (Config.versionName != assertVersion) {
            throw GradleException(
                "Fail! Branch $branchName version ${Config.versionName} (observed) != $assertVersion (expected)"
            )
        } else {
            println("Pass! Branch $branchName version ${Config.versionName} (observed) == $assertVersion (expected)")
        }

        if (pulledFromSemanticVersion != null) {
            Git.Commands.push(Branch(branchName)).mock("SUCCESS")
            val pulledFrom = Config.pullCommitsFromUpstreamReleaseBranches().map { it.semanticVersion }
            if (pulledFrom != pulledFromSemanticVersion) {
                throw GradleException(
                    "Fail! $branchName pulled from ${pulledFrom.joinToString()} incorrectly"
                )
            } else {
                println("Pass! $branchName (v${Config.versionName}) pulled from ${pulledFrom.joinToString()} as expected")
            }
        }
        clearMocks()

    }

    private fun assert(test: Boolean) {
        if (!test) throw GradleException("Assertion failed.")
    }

    private fun String.mock(mock: String) {
        Git.Commands.mocks[this] = mock
    }

    private fun mockCommitsAheadAndBehind(fromBranch: String, toBranch: String, ahead: Int, behind: Int) {
        Git.Commands.commitsAhead(Branch(fromBranch), Branch(toBranch)).mock(ahead.toString())
        Git.Commands.commitsBehind(Branch(fromBranch), Branch(toBranch)).mock(behind.toString())
    }

    private fun clearMocks() = Git.Commands.mocks.clear()

}