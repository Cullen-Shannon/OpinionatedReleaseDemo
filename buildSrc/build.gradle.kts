plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

/*
    If your buildSrc directory needs libraries (like retrofit for making network calls to a bug
    tracking system, for example), declare them here in a dependencies block. Android Studio currently
    fails to unpack variables here, so the references must be hardcoded.
 */