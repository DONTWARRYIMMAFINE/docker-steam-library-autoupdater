plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.7"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.github.dontworryimmafine"
version = "0.0.1-SNAPSHOT"
description = "A Dockerized application to automate the download and update of Steam games from your library. Built on the official `steamcmd` image with secure non-root execution and flexible scheduling."

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
	maven(uri("https://jitpack.io"))
}

dependencies {
	// Spring
	implementation("org.springframework.boot:spring-boot-starter")

	// Other
	implementation("org.jetbrains.kotlin:kotlin-reflect")

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

tasks.withType<Test> {
	useJUnitPlatform()
}
