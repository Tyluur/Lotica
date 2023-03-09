plugins {
    application
    kotlin("jvm") version "1.8.0"
}

group = "dusk-rs"
version = "0.0.1"

java.sourceCompatibility = JavaVersion.toVersion('8')
java.targetCompatibility = JavaVersion.toVersion('8')

val koinVersion = "2.1.5"
val junitVersion = "5.6.2"
val jacksonVersion = "2.11.0"

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    maven(url = "https://repo.maven.apache.org/maven2")
    maven(url = "https://jitpack.io")
    maven(url = "https://dl.bintray.com/michaelbull/maven")
}

application {
    mainClass.set("com.runescape.Main")
}

dependencies {
    // Java
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    // Kotlin
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.4.2")

    // Dependency Injection
    implementation("org.koin", "koin-core", koinVersion)
    implementation("org.koin", "koin-logger-slf4j", koinVersion)

    // Reflection
    implementation("io.github.classgraph", "classgraph", "4.8.78")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.michael-bull.kotlin-inline-logger", "kotlin-inline-logger-jvm", "1.0.2")

    // RuneScape API
    implementation("com.displee", "rs-cache-library", "6.7")
    implementation("com.github.michaelbull", "rs-api", "1.1.1")

    implementation("com.google.guava:guava:29.0-jre")
    implementation("org.apache.commons:commons-lang3:3.10")
    implementation("commons-cli", "commons-cli", "1.4")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("org.postgresql:postgresql:42.2.12")
    implementation("com.zaxxer:HikariCP:3.4.5")
    implementation("it.unimi.dsi:fastutil:8.3.1")
    implementation("com.zaxxer", "HikariCP", "2.3.2")
    implementation("org.yaml", "snakeyaml", "1.26")
    implementation("com.sun.activation", "javax.activation", "1.2.0")
    // compile 'com.sun.activation:javax.activation:1.2.0'

    // Network
    implementation("io.netty:netty:3.10.6.Final")
    testImplementation(group = "org.koin", name = "koin-test", version = koinVersion)
    testImplementation(group = "io.mockk", name = "mockk", version = "1.10.0")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
