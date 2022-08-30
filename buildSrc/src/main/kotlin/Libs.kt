/*
    This approach allows ensures the same libraries are used. Be warned though: Android Studio doesn't
    unpack the constants in buildSrc, so you'll get weird messages in the Project Structure dialog.
    You also don't get the linting for dependency upgrades. Future releases of AS and Gradle should
    resolve this.
 */

object Libs {
    const val coreKtx = "androidx.core:core-ktx:1.8.0"
    const val appCompat = "androidx.appcompat:appcompat:1.5.0"
    const val material = "com.google.android.material:material:1.6.1"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.4"
    const val navFragmentKtx = "androidx.navigation:navigation-fragment-ktx:${Versions.nav}"
    const val navUiKtx = "androidx.navigation:navigation-ui-ktx:${Versions.nav}"
    const val jUnit = "junit:junit:4.13.2"
    const val extJUnit = "androidx.test.ext:junit:1.1.3"
    const val espressoCore = "androidx.test.espresso:espresso-core:3.4.0"
}