import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.trafficscotland"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.trafficscotland"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        // Google Maps key for AndroidManifest.xml
        manifestPlaceholders["MAPS_API_KEY"] =
            localProperties.getProperty("MAPS_API_KEY", "")


        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "TRAFFIC_CLIENT_ID",
            "\"${localProperties.getProperty("TRAFFIC_CLIENT_ID", "")}\""
        )

        buildConfigField(
            "String",
            "TRAFFIC_CLIENT_KEY",
            "\"${localProperties.getProperty("TRAFFIC_CLIENT_KEY", "")}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_11

        targetCompatibility =
            JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    implementation(
        "androidx.recyclerview:recyclerview:1.3.2"
    )

    testImplementation(libs.junit)

    androidTestImplementation(
        libs.ext.junit
    )

    androidTestImplementation(
        libs.espresso.core
    )

    implementation("com.google.android.gms:play-services-maps:20.1.0")
}