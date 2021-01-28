plugins {
    application
    kotlin("jvm") version "1.3.72"
}

val koinVersion = "2.1.5"
val junitVersion = "5.6.2"
val jacksonVersion = "2.11.0"

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "dusk-rs"
    version = "0.0.1"

    java.sourceCompatibility = JavaVersion.VERSION_15

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
        maven(url = "https://repo.maven.apache.org/maven2")
        maven(url = "https://jitpack.io")
        maven(url = "https://dl.bintray.com/michaelbull/maven")
    }
}

subprojects {
    dependencies {
        // Jvm
        implementation(kotlin("stdlib"))

        // Kotlin
        implementation(kotlin("reflect"))
        implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.7")

        // Dependency Injection
        implementation("org.koin", "koin-core", koinVersion)
        implementation("org.koin", "koin-logger-slf4j", koinVersion)

        // Network
        implementation("io.netty:netty-all:4.1.44.Final")

        // Logging
        implementation("org.slf4j:slf4j-api:1.7.30")
        implementation("ch.qos.logback:logback-classic:1.2.3")
        implementation("com.michael-bull.kotlin-inline-logger", "kotlin-inline-logger-jvm", "1.0.2")

        // Utilities
        implementation("com.google.guava:guava:29.0-jre")
        implementation("io.github.classgraph", "classgraph", "4.8.78")

        // Testing
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testImplementation("org.koin", "koin-test", koinVersion)
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

}