import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    alias(libs.plugins.google.gms.google.services)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.collegegrad.suggestme"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.collegegrad.suggestme"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load the values from .properties file
        val keystoreFile = project.rootProject.file("local.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())

        // Return empty key in case something goes wrong
        val apiKey = properties.getProperty("apiKey") ?: ""

        buildConfigField(
            type = "String",
            name = "apiKey",
            value = "\"$apiKey\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.coil.compose.v222)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui:1.7.3")
    implementation("androidx.compose.ui:ui-graphics:1.7.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.3")
    implementation(libs.androidx.material3)
    implementation(libs.androidx.webkit)
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.ui:ui-test-junit4:1.7.3")
    implementation("androidx.compose.ui:ui-test-manifest:1.7.3")
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.firebase.firestore)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.3")

    // Added DataStore and Gson dependencies
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // ViewModel utilities for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    // Lifecycles only (without ViewModel or LiveData)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Coil
    implementation(libs.coil.compose)

    implementation(libs.kotlinx.coroutines.android.v139)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.jsoup)
    implementation(libs.converter.scalars)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Firebase authentication
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)

    //For authentication with Google using Credential Manager
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    //Image Cropper
    implementation ("com.github.yalantis:ucrop:2.2.8")

    //Json Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    //Gemini Api
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

}