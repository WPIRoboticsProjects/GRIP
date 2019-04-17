import org.gradle.api.Project
import java.io.OutputStream

/**
 * Gets the project version from git by running `git describe --tags`. If no .git directory is
 * present (like when the project is downloaded in a ZIP file), the version falls back to "v0.0.0".
 */
fun Project.getGitVersion(): String {
    val DEFAULT_VERSION_STRING = "v0.0.0"

    if (rootDir.resolve(".git").exists()) {
        StringOutputStream().use {
            project.exec {
                setWorkingDir(project.rootDir)
                setCommandLine("git", "describe", "--tags")
                setStandardOutput(it)
            }
            return it.toString().takeWhile { !it.isWhitespace() }
        }
    } else {
        System.err.println("No .git directory found! Falling back to $DEFAULT_VERSION_STRING")
        return DEFAULT_VERSION_STRING
    }
}

private class StringOutputStream : OutputStream() {
    private val buf = StringBuffer()

    override fun write(b: ByteArray) {
        this.buf.append(String(b))
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        this.buf.append(String(b, off, len))
    }

    override fun write(b: Int) {
        val bytes = byteArrayOf(b.toByte())
        this.buf.append(String(bytes))
    }

    override fun toString(): String {
        return this.buf.toString()
    }
}
