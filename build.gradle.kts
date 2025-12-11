plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.github.dontworryimmafine"
version = "0.2.1"
description =
    "A Dockerized application to automate the download and update of Steam games from your library. Built on the official `steamcmd` image with secure non-root execution and flexible scheduling."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

ktlint {
    debug = true
    verbose = true
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Other
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.register("printVersion") {
    doLast {
        println(version)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
