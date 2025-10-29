plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.foodfinder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.foodfinder"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-firestore")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("com.squareup.picasso:picasso:2.8")

    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation("com.github.PayHereDevs:payhere-android-sdk:v3.0.17")
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("com.google.code.gson:gson:2.8.0")

    implementation("com.google.android.gms:play-services-maps:19.0.0")

    implementation("com.google.android.gms:play-services-location:21.3.0")

}