import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}
fun getApiKey(propertyKey: String): String {
    return gradleLocalProperties(rootDir).getProperty(propertyKey)
}
android {
    namespace = "com.brandon.campingmate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.brandon.campingmate"
        minSdk = 29
        targetSdk = 34
        versionCode = 2
        versionName = "1.0"
        buildConfigField("String", "camp_data_key", getApiKey("camp_data_key"))
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
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Firebase
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation("com.google.firebase:firebase-firestore:24.10.2")

    // Retrofit & OkHttp
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ViewModels
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.activity:activity-ktx:1.8.2")

    // Timber for logging
    implementation ("com.jakewharton.timber:timber:5.0.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // Lottie
    implementation ("com.airbnb.android:lottie:6.3.0")

    // Android splash
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Bottom navigation theme
    implementation ("nl.joery.animatedbottombar:library:1.1.0")

    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // coil
    implementation("io.coil-kt:coil:1.4.0")

    //kakao
    implementation("com.kakao.sdk:v2-user:2.0.1")

    //네이버 맵
    implementation("com.naver.maps:map-sdk:3.17.0")
    //네이버맵 클러스터링
    implementation("io.github.ParkSangGwon:tedclustering-naver:1.0.2")
    //인디케이터
    implementation("com.tbuonomo:dotsindicator:5.0")

}