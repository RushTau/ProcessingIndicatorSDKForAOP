plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}
dependencies {
    implementation(libs.asm)
    implementation(libs.asm.commons)
    implementation(libs.snakeyaml)
    compileOnly("com.android.tools.build:gradle:8.6.0")
    testImplementation(libs.junit)
    testImplementation(libs.okhttp)
}
gradlePlugin {
    plugins {
        create("indicatorPlugin") {
            id = "com.jini.indicator"
            implementationClass = "com.jini.indicator.plugin.IndicatorPlugin"
        }
    }
}
publishing {
    publications {
        create<MavenPublication>("plugin") {
            groupId = "com.jini"; artifactId = "processing-indicator-aop-plugin"; version = "1.0.0"
            from(components["java"])
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
