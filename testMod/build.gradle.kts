plugins {
	id("java")
	id("dev.pandasystems.bamboo-loom")
}

group = "dev.pandasystems"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

bambooLoom {
	minecraftVersion = "1.21.5"
}