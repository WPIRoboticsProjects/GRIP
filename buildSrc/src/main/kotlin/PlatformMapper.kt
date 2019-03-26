import NativePlatforms.*

/**
 * The platform performing the build. This is in the format `"<os.name><os.arch>"`, e.g. `win32`, `mac64`, `linux64`.
 */
val currentPlatform: NativePlatforms by lazy {
    val osName = System.getProperty("os.name")
    val os: String = when {
        osName.contains("windows", true) -> "win"
        osName.contains("mac", true) -> "mac"
        osName.contains("linux", true) -> "linux"
        else -> throw UnsupportedOperationException("Unknown OS: $osName")
    }
    val osArch = System.getProperty("os.arch")
    val arch: String =
            if (osArch.contains("x86_64") || osArch.contains("amd64")) {
                "64"
            } else if (osArch.contains("x86")) {
                "32"
            } else {
                throw UnsupportedOperationException(osArch)
            }
    NativePlatforms.forName(os + arch)
}

/**
 * Generates a classifier string for a platform-specific WPILib native library.
 */
fun wpilibClassifier(platform: NativePlatforms) = when (platform) {
    WIN32 -> "windowsx86"
    WIN64 -> "windowsx86-64"
    MAC -> "osxx86-64"
    LINUX -> "linuxx86-64"
}

/**
 * Generates a classifier string for a platform-specific JavaCPP native library.
 */
fun javaCppClassifier(platform: NativePlatforms) = when (platform) {
    WIN32 -> "windows-x86"
    WIN64 -> "windows-x86_64"
    MAC -> "macosx-x86_64"
    LINUX -> "linux-x86_64"
}

/**
 * Generates a classifier string for a platform-specific JavaFX artifact.
*/
fun javaFxClassifier(platform: NativePlatforms) = when (platform) {
    WIN32 -> "win32"
    WIN64 -> "win"
    MAC -> "mac"
    LINUX -> "linux"
}
