import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Example of ingesting command-line parameters for gradle tasks.
 * Generally preferable to dependence on env variables for build pipelines.
 *
 * Running this:
 *      ./gradlew <whatever task> -PtestEnv=B -PappHardening=TRUE -PadminEmail="supfoos@aol"
 *
 * Will result in these:
 *      simpleDefault = myDefaultValue
 *      testEnv = B
 *      appHardening = true
 *      adminEmail = supfoos@aol.com
 *      userEmails = ["person1@yahoo", "person2@hotmail"]
 */
object Args {

    val simpleDefault get() = getCmdLineProp("simpleDefault", "myDefaultValue")
    val testEnv get() = getCmdLineProp("testEnv", TestEnv.A)
    val appHardeningEnabled get()  = getCmdLineProp<Boolean>("appHardening")
    val adminEmail get() = getCmdLineProp<String>("adminEmail")
    val userEmails get() = getCmdLineProp("userEmails", arrayOf("person1@yahoo", "person2@hotmail"))

    private inline fun <reified T>getCmdLineProp(key: String, default: T? = null): T? {
        if (!project.hasProperty(key)) return default
        val prop = project.property(key) as String
        return when (T::class) {
            String::class -> prop as T
            Array<String>::class -> prop.split(",").toTypedArray() as T
            Int::class -> prop.toInt() as T
            Boolean::class -> prop.toBoolean() as T

            // include your enums here
            TestEnv::class -> TestEnv.valueOf(prop) as T
            else -> throw GradleException("Unexpected command line type.")
        }
    }

    private lateinit var project: Project
    fun initialize(project: Project) {
        this.project = project
    }

    enum class TestEnv {
        A, B, C, D
    }

}