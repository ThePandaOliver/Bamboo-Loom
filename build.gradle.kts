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
	implementation("com.google.code.gson:gson:2.10.1")

	implementation("org.ow2.asm:asm:9.5")
	implementation("org.ow2.asm:asm-commons:9.5")
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
