plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.triskelapps.updateappviewsample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.triskelapps.updateappviewsample"
        minSdk = 21
        targetSdk = 34
        versionCode = 128
        versionName = "1.2.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures.buildConfig = true
    viewBinding.isEnabled = true
}

dependencies {

    implementation(project(":simpleappupdate"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)


    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.core.ktx)
}