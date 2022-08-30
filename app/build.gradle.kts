plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = Versions.compileSdk

    defaultConfig {
        applicationId = "com.cullenshannon.opinionatedreleasedemo"
        minSdk = Versions.minSdk
        targetSdk = Versions.compileSdk
        versionCode = Config.versionCode
        versionName = Config.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = Versions.jvmTarget
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(Libs.coreKtx)
    implementation(Libs.appCompat)
    implementation(Libs.material)
    implementation(Libs.constraintLayout)
    implementation(Libs.navFragmentKtx)
    implementation(Libs.navUiKtx)
    testImplementation(Libs.jUnit)
    androidTestImplementation(Libs.extJUnit)
    androidTestImplementation(Libs.espressoCore)
}

/*
    Task to execute autoVersion tests. DoLast ensures it's only run on-demand.
    AS now supports debugging custom gradle tasks! Find your task in the Gradle Tool window, right
    click it, and debug. Breakpoints are supported. Probably don't need to run this unless
    you want to test out something specific in your pipeline.
 */
tasks.create("testAutoVersioning") {
    this.outputs.cacheIf { false } // ensure our test classes don't get cached
    doLast {
        ConfigTests.run()
    }
}

// See method comments
tasks.create("pullCommitsFromUpstreamBranches") {
    this.outputs.cacheIf { false }
    doLast {
        Config.pullCommitsFromUpstreamReleaseBranches()
    }
}

// See method comments
tasks.create("retireReleaseBranch") {
    this.outputs.cacheIf { false }
    doLast {
        Config.retireReleaseBranch()
    }
}

// Convenience task to pune local untracked branches from local repo
tasks.create("pruneLocalUntrackedBranches") {
    this.outputs.cacheIf { false }
    doLast {
        Git.pruneLocalUntracked()
    }
}

// See method comments
tasks.create("exampleCommandLineArgs") {
    this.outputs.cacheIf { false }
    doLast {
        println(Args.simpleDefault)
        println(Args.testEnv)
        println(Args.appHardeningEnabled)
        println(Args.adminEmail)
        println(Args.userEmails!!.joinToString())
    }
}