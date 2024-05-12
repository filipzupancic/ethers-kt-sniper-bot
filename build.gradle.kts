import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.graalvm.buildtools.native") version "0.9.28"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    id("io.kriptal.ethers.abigen-plugin") version "0.5.0"
}

ethersAbigen {
    directorySource("src/main/abi")
    outputDir = "generated/source/ethers/main/kotlin"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_19
}

repositories {
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("io.github.cdimascio:dotenv-java:2.2.0")

    // ethers-kt
    implementation(platform("io.kriptal.ethers:ethers-bom:0.5.0-SNAPSHOT"))
    implementation("io.kriptal.ethers:ethers-abi")
    implementation("io.kriptal.ethers:ethers-core")
    implementation("io.kriptal.ethers:ethers-providers")
    implementation("io.kriptal.ethers:ethers-signers")
}

configurations {
    all {
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j2-impl")
    }
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "19"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
