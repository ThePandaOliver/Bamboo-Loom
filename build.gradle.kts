plugins {
	`java-gradle-plugin`
	`maven-publish`
	kotlin("jvm")
}

group = "dev.pandasystems"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
	implementation("com.google.code.gson:gson:2.12.1")
	implementation(kotlin("stdlib-jdk8"))
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