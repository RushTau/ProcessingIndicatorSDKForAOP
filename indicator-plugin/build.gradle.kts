plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}
dependencies {
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-commons:9.7")
    implementation("org.yaml:snakeyaml:2.3")
    compileOnly("com.android.tools.build:gradle:8.6.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
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
