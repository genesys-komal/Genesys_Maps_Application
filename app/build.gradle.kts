plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.mapapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mapapplication"
        minSdk = 22
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Access API key and Base URL
// Access API key and Base URL
        buildConfigField("String", "API_KEY", "\"${project.findProperty("apiKey") ?: "4FBC5BFE7E34C58F414519B43972C"}\"")
        buildConfigField("String", "BASE_URL", "\"${project.findProperty("apiUrl") ?: "https://api.genesysmap.com/api/v1"}\"")
    }
    buildFeatures {
        buildConfig = true  // Enable the BuildConfig feature
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
    repositories {

    }

}

dependencies {

//    implementation("androidx.core:core-ktx:1.13.1")
//    implementation("androidx.appcompat:appcompat:1.7.0")
//    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.maplibre.sdk)
    implementation(libs.maplibre.navigation.android)

//    implementation(files("libs/Maps_Sdk.aar"))
    implementation (libs.okhttp3.logging.interceptor)
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation ("com.intuit.sdp:sdp-android:1.1.1")

}