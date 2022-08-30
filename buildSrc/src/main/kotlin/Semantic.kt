// Define the regex pattern for your release branches below. Currently assumes releases/<semanticVersion>
class Branch(val name: String) :
    SemanticBase(name, Regex("origin/releases/(\\d\\d?).(\\d\\d?)(.(\\d\\d?))?$")) {
    val isDevelop = name == "develop" || name == "origin/develop"
    val isMaster = name == "master" || name == "origin/master"
    companion object {
        // used in Git call to help keep list of remote branches retrieved trimmed down.
        const val gitFilter = "*origin/releases/*"
    }
    override fun toString() = name
}

// Define the regex pattern for your release tags below. Currently assumes v<semanticVersion>
class Tag(val name: String) :
    SemanticBase(name, Regex("refs/tags/v(\\d\\d?).(\\d\\d?)(.(\\d\\d?))?\\Z"))

open class SemanticBase(
    branchOrTagName: String,
    regex: Regex
) {

    var major: Int? = null
    var minor: Int? = null
    var modif: Int? = null
    var semanticVersion: String? = null
    var nextSemanticVersion: String? = null
    var isRelease: Boolean = false

    init {
        val prefix = if (branchOrTagName.startsWith("origin/")) "" else "origin/"
        val match = regex.find(prefix + branchOrTagName)
        if (match != null) {
            isRelease = true
            major = match.groupValues[1].toInt()
            minor = match.groupValues[2].toInt()
            modif = if (match.groupValues[4].isNotEmpty()) match.groupValues[4].toInt() else 0
            semanticVersion = "$major.$minor.$modif"
            nextSemanticVersion = "$major.${minor!! + 1}.0"
        }
    }

}