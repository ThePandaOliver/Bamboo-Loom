plugins {
	`kotlin-dsl`
	`maven-publish`
	kotlin("jvm") version "2.0.21"
}

group = "dev.pandasystems"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
	implementation(kotlin("stdlib"))
	// https://mvnrepository.com/artifact/com.google.code.gson/gson
	implementation("com.google.code.gson:gson:2.12.1")
}

gradlePlugin {
	plugins {
		create("bambooLoom") {
			id = "dev.pandasystems.bamboo-loom"
			implementationClass = "dev.pandasystems.bambooloom.BambooLoomPlugin"
		}
	}
}

kotlin {
	jvmToolchain(21)
}