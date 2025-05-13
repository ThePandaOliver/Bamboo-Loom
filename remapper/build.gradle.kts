plugins {
	kotlin("jvm")
}

group = "dev.pandasystems"
version = "1.0-SNAPSHOT"

repositories {
	maven("https://maven.fabricmc.net/")
	mavenCentral()
}

dependencies {
	testImplementation(kotlin("test"))

	implementation("org.ow2.asm:asm:9.8")
	implementation("org.ow2.asm:asm-commons:9.8")
	implementation("org.ow2.asm:asm-tree:9.8")
	implementation("org.ow2.asm:asm-analysis:9.8")
	implementation("org.ow2.asm:asm-util:9.8")

	implementation("org.apache.logging.log4j:log4j-api:2.23.1")
	implementation("org.apache.logging.log4j:log4j-core:2.23.1")
	implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")

	implementation("com.google.code.gson:gson:2.13.1")
	
	// https://mvnrepository.com/artifact/net.fabricmc/tiny-remapper
	implementation("net.fabricmc:tiny-remapper:0.11.1")
	// https://mvnrepository.com/artifact/net.fabricmc/mapping-io
	implementation("net.fabricmc:mapping-io:0.7.1")
}

tasks.test {
	useJUnitPlatform()
}
kotlin {
	jvmToolchain(21)
}