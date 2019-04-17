enum class BuildType {

    /**
     * Building with JDK 8. Useful for running UI tests, since it's the only version on which
     * testfx will work properly, but nothing else.
     */
    JDK_8,

    /**
     * Building on JDK 11, and allows the possibility of building a native installer package
     */
    JDK_11;

    companion object {
        val current by lazy {
            if (org.gradle.internal.jvm.Jvm.current().javaVersion?.isJava11Compatible ?: true) {
                JDK_11
            } else {
                JDK_8
            }
        }

        /**
         * Checks if the current JDK is Java 11 (or newer).
         */
        val isJdk11 by lazy {
            println("Build JDK is $current")
            current == JDK_11
        }
    }

}
