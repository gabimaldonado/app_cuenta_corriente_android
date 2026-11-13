plugins {
    alias(libs.plugins.android.application)

    // Add the Google services Gradle plugin (FIREBASE)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.appcuentacorriente"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.appcuentacorriente"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.messaging)
    implementation(libs.material)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
    // Dependencia de firebase messaging
    implementation(platform("com.google.firebase:firebase-messaging"))

}