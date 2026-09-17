plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}
android {
    namespace = "com.jini.indicator"
    compileSdk = 34
    defaultConfig { minSdk = 24; consumerProguardFiles("consumer-rules.pro") }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.snakeyaml)
    implementation(libs.coroutines.android)
    compileOnly(libs.lottie)
    compileOnly(libs.okhttp)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.okhttp)
    testImplementation(libs.coroutines.test)
}
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.jini"; artifactId = "processing-indicator-aop"; version = "1.0.0"
            afterEvaluate { from(components["release"]) }
        }
    }
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/jini/ProcessingIndicatorSDKForAOP")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
