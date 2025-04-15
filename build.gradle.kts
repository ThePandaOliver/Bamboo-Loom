plugins {
	`kotlin-dsl`
	`maven-publish`
	kotlin("jvm") version "2.0.21"
	kotlin("plugin.serialization") version "2.0.21"
}

group = "dev.pandasystems"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
	implementation(kotlin("stdlib"))
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
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