import java.util.Properties

plugins {
    alias(libs.plugins.android.application)

    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.osnordev.abaco"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.osnordev.abaco"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Supabase config from local.properties
        val localProps = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) localProps.load(localPropsFile.inputStream())
        buildConfigField("String", "SUPABASE_URL", "\"${localProps.getProperty("SUPABASE_URL", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProps.getProperty("SUPABASE_ANON_KEY", "")}\"")
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

// Nueva forma recomendada para Kotlin 2.0+
kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Vico Charts
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)

    // Supabase
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.auth)
    implementation(libs.ktor.android)

    // SQLCipher
    implementation(libs.sqlcipher)

    // ZXing (QR)
    implementation(libs.zxing.core)

    // WorkManager
    implementation(libs.workmanager.ktx)

    // Coil (imágenes)
    implementation(libs.coil.compose)

    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    //the Kore library
    implementation(libs.kore)
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.assertions)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
