package dev.pandasystems.bambooloom.models

/**
 * Data class representing the version manifest data.
 * The class is based on the JSON structure of the minecraft version manifest version 2
 */
class VersionManifestData {
    lateinit var latest: Latest
    lateinit var versions: List<Version>

    class Latest {
        lateinit var release: String
        lateinit var snapshot: String
    }

	class Version {
        lateinit var id: String
        lateinit var type: String
        lateinit var url: String
        lateinit var time: String
        lateinit var releaseTime: String
    }
}