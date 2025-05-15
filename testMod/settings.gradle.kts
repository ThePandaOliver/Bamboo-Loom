pluginManagement {
	includeBuild("../")
	repositories {
		gradlePluginPortal()
		mavenCentral()
		maven("https://maven.fabricmc.net/")
	}
}

rootProject.name = "testMod"