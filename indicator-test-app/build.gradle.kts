plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // id("com.jini.indicator")  // Task 8 구현 후 활성화
}
android {
    namespace = "com.jini.testapp"; compileSdk = 34
    defaultConfig { applicationId = "com.jini.testapp"; minSdk = 24; targetSdk = 34; versionCode = 1; versionName = "1.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(project(":indicator-core"))
    implementation(libs.lottie)
    implementation(libs.okhttp)
}
