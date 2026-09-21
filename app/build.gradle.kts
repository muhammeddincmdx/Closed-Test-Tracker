import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// Signing credentials are kept out of version control. They are read from
// keystore.properties (git-ignored) or, as a fallback, environment variables.
// See keystore.properties.example for the expected keys.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

fun signingSecret(propertyKey: String, envKey: String): String? =
    keystoreProperties.getProperty(propertyKey) ?: System.getenv(envKey)

android {
    namespace = "com.mdstudio.closedtesttracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mdstudio.closedtesttracker"
        minSdk = 26
        targetSdk = 36
        versionCode = 21
        versionName = "0.0.26.07"
        buildConfigField("boolean", "PRO_PREVIEW", providers.gradleProperty("proPreview").orNull ?: "false")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystoreFile = rootProject.file("release-keystore.jks")
            val storePw = signingSecret("storePassword", "CTT_STORE_PASSWORD")
            val keyAliasValue = signingSecret("keyAlias", "CTT_KEY_ALIAS")
            val keyPw = signingSecret("keyPassword", "CTT_KEY_PASSWORD")
            if (keystoreFile.exists() && storePw != null && keyAliasValue != null && keyPw != null) {
                storeFile = keystoreFile
                storePassword = storePw
                keyAlias = keyAliasValue
                keyPassword = keyPw
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            // Launcher label for debug builds (matches the release app name).
            resValue("string", "app_name", "TesterApp")
        }
        create("preview") {
            initWith(getByName("release"))
            applicationIdSuffix = ".preview"
            versionNameSuffix = "-preview"
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("boolean", "PRO_PREVIEW", "true")
            resValue("string", "app_name", "Closed Test Tracker Pro Preview")
            matchingFallbacks += listOf("release")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Fall back to debug signing only when no release credentials are
            // configured, so the release variant always builds locally.
            val releaseSigning = signingConfigs.getByName("release")
            signingConfig = if (releaseSigning.storeFile != null) {
                releaseSigning
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.01.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.room:room-runtime:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")
    ksp("androidx.room:room-compiler:2.7.1")

    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:review:2.0.2")
    implementation("com.google.android.play:review-ktx:2.0.2")
    implementation("com.android.billingclient:billing-ktx:8.0.0")
    implementation("com.google.android.gms:play-services-ads:23.5.0")

    testImplementation("junit:junit:4.13.2")
}

