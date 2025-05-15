import dev.pandasystems.bambooloom.extensions.minecraft

plugins {
	id("java")
	id("dev.pandasystems.bamboo-loom")
}

group = "dev.pandasystems"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	mappedImplementation(minecraft("1.21.5"))
	mapping("net.fabricmc:yarn:1.21.5+build.1:v2")
}