import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "org.sovereignos"
    compileSdk = 37
    defaultConfig {
        applicationId = "org.sovereignos"
        minSdk = 26
        // Keep the stable target API until Android 17 (API 37) is finalized.
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests.isReturnDefaultValues = true }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget("17"))
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.8.0.202609011348-r")
    implementation("com.google.ai.edge.litertlm:litertlm-android:0.19.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.eclipse.jgit:org.eclipse.jgit:7.8.0.202609011348-r")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
