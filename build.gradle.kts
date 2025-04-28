plugins {
	`kotlin-dsl`
	`maven-publish`
	kotlin("jvm") version "1.9.24"
}

group = "dev.pandasystems"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
	implementation("com.google.code.gson:gson:2.10.1")

	implementation("org.ow2.asm:asm:9.8")
	implementation("org.ow2.asm:asm-commons:9.8")
	implementation("org.ow2.asm:asm-tree:9.8")
	implementation("org.ow2.asm:asm-analysis:9.8")
	implementation("org.ow2.asm:asm-util:9.8")

	implementation("org.vineflower:vineflower:1.11.1")

	testImplementation(gradleTestKit())
	testImplementation(kotlin("test"))
	testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

tasks.test {
	useJUnitPlatform()
}