import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.HasMultipleValues
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import java.io.FileFilter

/**
 * Runs the `jpackage` tool (tentatively to be introduced in Java 13) to generate an installer
 * for the current operating system. `jpackage` cannot generate an installer for a different OS than
 * the one running the build.
 *
 * Because JDK 13 has not yet been released, and because `jpackage` is not yet included in the
 * early-access builds, a compatible JDK must be downloaded from
 * [https://jdk.java.net/jpackage/](https://jdk.java.net/jpackage/). The location of the JDK must be
 * specified with [jdkHome] (eg `/opt/java/jdk-13/`)
 *
 */
open class JpackageExec : DefaultTask() {

    private val objectFactory = project.objects

    private fun stringProperty() = objectFactory.property<String>()

    /**
     * The JDK_HOME directory containing a JDK with the jpackage tool.
     */
    @get:InputDirectory
    val jdkHome = objectFactory.directoryProperty()

    /**
     * The location of a prebuilt runtime image generated from `jlink`. If not specified, the task
     * will generate an image with all JDK modules included.
     */
    @get:InputDirectory
    @get:Optional
    val runtimeImage = objectFactory.directoryProperty()

    /**
     * Java arguments to pass to the virtual machine.
     */
    @get:Input
    @get:Optional
    val jvmArgs = objectFactory.listProperty<String>()

    /**
     * Whether or not the jpackage execution should be verbose (ie print to stdout). Defaults to
     * `false` if not specified.
     */
    @get:Input
    @get:Optional
    val verbose = objectFactory.property<Boolean>()

    /**
     * The output directory into which the installer should be generated.
     */
    @get:OutputDirectory
    val outputDir = objectFactory.directoryProperty()

    /**
     * The input directory of files to bundle into the installer. Typically, this should contain
     * the dependency JAR files of the application.
     */
    @get:InputDirectory
    val inputDir = objectFactory.directoryProperty()

    /**
     * Path to override jpackage resources. Icons, template files, and other resources of jpackage
     * can be over-ridden by adding replacement resources to this directory.
     */
    @get:InputDirectory
    @get:Optional
    val resourceDir = objectFactory.directoryProperty()

    /**
     * An icon file to use for the application and installer. This can be a file or a file name.
     */
    @get:InputFile
    @get:Optional
    val icon = objectFactory.property<Any>()

    /**
     * The main JAR file. This file must be present in the [inputDir].
     */
    @get:InputFile
    val mainJar = objectFactory.fileProperty()

    /**
     * The name of the main class in the [mainJar].
     */
    @get:Input
    val mainClassName = stringProperty()

    /**
     * The name of the application (e.g. "GRIP").
     */
    @get:Input
    val applicationName = stringProperty()

    /**
     * A description of the application (e.g. "GRIP Computer Vision Engine").
     */
    @get:Input
    @get:Optional
    val applicationDescription = stringProperty()

    /**
     * The version of the application. This must be a semver-compliant version string, e.g.
     * `"1.5.2"`. Extra build information such as `"SNAPSHOT"`, `"RC1"`, etc. must be ommited
     * from the string. A full version string may be specified with [fullApplicationVersion]
     * to use to rename the installer packages with the full version information.
     */
    @get:Input
    val applicationVersion = stringProperty()

    /**
     * A full version string to use to name the installer packages. This is _only_ used for naming
     * the installer executable - the version of the application as seen by the OS will be the one
     * specified with [applicationVersion].
     */
    @get:Input
    @get:Optional
    val fullApplicationVersion = stringProperty()

    /**
     * Copyright for the application.
     */
    @get:Input
    @get:Optional
    val copyright = stringProperty()

    /**
     * Path to the license file.
     */
    @get:InputFile
    @get:Optional
    val licenseFile = objectFactory.fileProperty()

    /**
     * The name of the application vendor (e.g. "Worcester Polytechnic Institute").
     */
    @get:Input
    @get:Optional
    val applicationVendor = stringProperty()

    /**
     * A properties file containing key-value pairs for file association integration.
     * Currently broken on Linux and Mac.
     */
    @get:InputFile
    @get:Optional
    val fileAssociations = objectFactory.fileProperty()

    /**
     * The type of installer to generate. Windows supports "exe" (requires InnoSetup) and "msi".
     * Mac supports "dmg" and "pkg". Linux supports "deb" and "rpm".
     */
    @get:Input
    val installerType = stringProperty()

    /**
     * Windows-specific. A constant UUID to use for Windows to identify version upgrades.
     */
    @get:Input
    @get:Optional
    val winUpgradeUuid = stringProperty()

    /**
     * Windows-specific. Allows the application to be installed in the Windows start menu.
     */
    @get:Input
    @get:Optional
    val addToWindowsMenu = objectFactory.property<Boolean>()

    /**
     * Windows-specific. Adds a shortcut to the user's desktop.
     */
    @get:Input
    @get:Optional
    val addWindowsDesktopShortcut = objectFactory.property<Boolean>()

    /**
     * Mac-specific. An identifier to use for the App Store.
     */
    @get:Input
    @get:Optional
    val macBundleIdentifier = stringProperty()

    @TaskAction
    fun exec() {
        project.exec {
            val args = mutableListOf<String>()
            args.add(jdkHome.file("bin/jpackage").get().asFile.absolutePath)
            //args.add("create-installer")

            runtimeImage.ifPresent { dir ->
                args.addAll("--runtime-image", dir.asFile.absolutePath)
            }
            if (verbose.getOrElse(false)) {
                args.add("--verbose")
            }
            jvmArgs.ifPresent { jvmArgs ->
                args.add("--java-options")
                args.add(jvmArgs.joinToString(separator = " ", prefix = "\"", postfix = "\""))
            }
            args.addAll("--input", inputDir.get().asFile.absolutePath)
            resourceDir.ifPresent { dir ->
                args.addAll("--resource-dir", dir.asFile.absolutePath)
            }
            args.addAll("--dest", outputDir.get().asFile.absolutePath)
            icon.ifPresent { iconFile ->
                args.addAll("--icon", iconFile.toString())
            }
            args.addAll("--main-jar", mainJar.get().asFile.name)
            args.addAll("--main-class", mainClassName.get())

            args.addAll("--name", applicationName.get())
            applicationDescription.ifPresent { description ->
                args.addAll("--description", description)
            }
            args.addAll("--app-version", applicationVersion.get())
            copyright.ifPresent {
                args.addAll("--copyright", it)
            }
            licenseFile.ifPresent { file ->
                args.addAll("--license-file", file.asFile.absolutePath)
            }
            applicationVendor.ifPresent { vendor ->
                args.addAll("--vendor", vendor)
            }
            fileAssociations.ifPresent { propsFile ->
                args.addAll("--file-associations", propsFile.asFile.absolutePath)
            }
            args.addAll("--package-type", installerType.get())

            when (OperatingSystem.current()) {
                OperatingSystem.WINDOWS -> {
                    winUpgradeUuid.ifPresent { uuid ->
                        args.addAll("--win-upgrade-uuid", uuid)
                    }
                    if (addToWindowsMenu.getOrElse(false)) {
                        args.add("--win-menu")
                    }
                    if (addWindowsDesktopShortcut.getOrElse(false)) {
                        args.add("--win-shortcut")
                    }
                }
                OperatingSystem.MAC_OS -> {
                    macBundleIdentifier.ifPresent { id ->
                        args.addAll("--mac-package-identifier", id)
                    }
                }
                OperatingSystem.LINUX -> {
                    // No linux-specific properties yet
                }
            }

            setCommandLine(args)
        }

        fullApplicationVersion.ifPresent { version ->
            logger.log(LogLevel.INFO, "Renaming installer packages")
            val packageFileFilter = FileFilter { file ->
                listOf("exe", "dmg", "pkg", "deb", "rpm").any {
                    file.extension == it
                }
            }
            val packageFiles = outputDir.get().asFile.listFiles(packageFileFilter)
            packageFiles.forEach { packageFile ->
                val newName = "GRIP-${version}.${packageFile.extension}"
                logger.log(LogLevel.DEBUG, "Renaming ${packageFile.name} to $newName")
                packageFile.renameTo(packageFile.resolveSibling(newName))
            }
        }
    }
}

inline fun <reified T> MutableList<T>.addAll(vararg items: T) {
    items.forEach(this::add)
}

inline fun <reified T> Provider<T>.ifPresent(action: (T) -> Unit) {
    if (isPresent) {
        action(get())
    }
}

inline fun <reified T> HasMultipleValues<T>.setAll(vararg items: T) {
    set(items.asList())
}
