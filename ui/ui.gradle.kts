import edu.wpi.first.wpilib.opencv.installer.Installer
import org.gradle.internal.jvm.Jvm
import org.gradle.internal.os.OperatingSystem

plugins {
    `application`
    id("com.google.osdetector")
}

if (!(project.hasProperty("generation") || project.hasProperty("genonly"))) {
    sourceSets["test"].java {
        exclude("**/ui/codegeneration")
        exclude("**/ui/codegeneration/**")
    }
}

createNativeConfigurations()

dependencies {
    compile(project(":core"))
    compile(project(":ui:preloader"))
    //ideProvider project(path= ":core", configuration= "compile")
    compile(group = "org.controlsfx", name = "controlsfx", version = "11.0.0-RC2")
    compile(group = "com.hierynomus", name = "sshj", version = "0.16.0")
    compile(group = "org.apache.velocity", name = "velocity", version = "1.7")

    javafx("base")
    javafx("controls")
    javafx("fxml")
    javafx("graphics")

    val coreTestOutput = project(":core").dependencyProject.sourceSets["test"].output
    testCompile(files(coreTestOutput))
    testCompile(files(coreTestOutput.resourcesDir))
    testCompile(group = "org.testfx", name = "testfx-core", version = "4.0.15-alpha")
    testCompile(group = "org.testfx", name = "testfx-junit", version = "4.0.15-alpha")
    testRuntime(group = "org.testfx", name = "openjfx-monocle", version = "jdk-11+26")
    testCompile(group = "org.opencv", name = "opencv-java", version = "3.1.0")
}

tasks.named<JavaCompile>("compileTestJava") {
    dependsOn(":core:testClasses")
}

/*
 * Allows you to run the UI tests in headless mode by calling gradle with the -Pheadless argument
 */
if (project.hasProperty("headless")) {
    //println("Running UI Tests Headless")
    println("UI tests do not work properly when headless, and are disabled until fixed in JavaFX and TestFX")
    tasks.withType<Test> {
        jvmArgs = listOf(
                "-Djava.awt.headless=true",
                "-Dtestfx.robot=glass",
                "-Dtestfx.headless=true",
                "-Dprism.order=sw",
                "-Dprism.text=t2k"
        )
        useJUnit {
            excludeCategories("edu.wpi.grip.ui.UiTests")
        }
    }
}

/*
 * TestFX is flaky on Java >= 10, and is completely broken in headless mode on Java 10+. JavaFX 13
 * should fix the issue, but won't be publicly available for a while.
 */
tasks.withType<Test> {
    useJUnit {
        excludeCategories("edu.wpi.grip.ui.UiTests")
    }
}

tasks.register("testSharedLib") {
    description = "Compiles the shared library used by c++ generation testing."
    doLast {
        val syst = osdetector.os
        val testClassesDir = buildDir.resolve("classes/test")
        val pipelibDir = testClassesDir.resolve("pipelib")
        if (syst == "windows") {
            exec {
                isIgnoreExitValue = true // if clean hasn"t been called, directory already exists and mkdir fails.
                workingDir = testClassesDir
                commandLine = listOf("cmd", "/c", "mkdir", "pipelib")
            }
            exec {
                workingDir = pipelibDir
                commandLine = listOf("C:\\Program Files (x86)\\Microsoft Visual Studio 14.0\\VC\\vcvarsall.bat", "amd64", "&", "cmake", "-G", "Visual Studio 14 2015 Win64", "..\\..\\..\\..\\src\\test\\resources\\edu\\wpi\\grip\\ui\\codegeneration\\tools\\")
            }
            exec {
                workingDir = pipelibDir
                commandLine = listOf("C:\\Program Files (x86)\\Microsoft Visual Studio 14.0\\VC\\vcvarsall.bat", "amd64", "&", "cmake", "--build", ".", "--target", "ALL_BUILD", "--config", "Release")
            }
            exec {
                workingDir = testClassesDir
                commandLine = listOf("cmd", "/C", "copy /Y pipelib\\Release\\* .")
            }
            exec {
                workingDir = testClassesDir
                commandLine = listOf("cmd", "/C", "copy /Y pipelib\\pipe\\Release\\* .")
            }
            exec {
                workingDir = pipelibDir
                commandLine = listOf("cmd", "/C", "copy /Y ..\\..\\..\\..\\src\\test\\resources\\edu\\wpi\\grip\\ui\\codegeneration\\tools\\pipe\\AbsPipeline.h pipe\\")
            }
            exec {
                workingDir = testClassesDir
                commandLine = listOf("cmd", "/C", "copy /Y ..\\..\\..\\src\\test\\resources\\edu\\wpi\\grip\\ui\\codegeneration\\tools\\realpipe\\CMakeLists.txt .")
            }
        } else {
            exec {
                isIgnoreExitValue = true // if clean hasn"t been called, directory already exists and mkdir fails.
                workingDir = testClassesDir
                commandLine = listOf("mkdir", "pipelib")
            }
            exec {
                workingDir = pipelibDir
                commandLine = listOf("cmake", "../../../../src/test/resources/edu/wpi/grip/ui/codegeneration/tools/")
            }
            exec {
                workingDir = pipelibDir
                commandLine = listOf("make")
            }
            exec {
                workingDir = testClassesDir
                if (syst == "osx") {
                    commandLine = listOf("cp", "pipelib/libgenJNI.dylib", ".")
                }
                if (syst == "linux") {
                    commandLine = listOf("cp", "pipelib/libgenJNI.so", ".")
                }
            }
            exec {
                workingDir = pipelibDir
                commandLine = listOf("cp", "../../../../src/test/resources/edu/wpi/grip/ui/codegeneration/tools/pipe/AbsPipeline.h", "pipe/")
            }
            exec {
                workingDir = testClassesDir
                commandLine = listOf("cp", "../../../src/test/resources/edu/wpi/grip/ui/codegeneration/tools/realpipe/CMakeLists.txt", ".")
            }
        }
    }
}

if (project.hasProperty("generation") || project.hasProperty("genonly")) {
    val platform = Installer.getPlatform()
    val jniLocation: String = project.properties.getOrDefault("jniLocation", platform.defaultJniLocation()).toString()
    val jniPath = File(jniLocation).absolutePath

    val installOpenCV = tasks.register("installOpenCV") {
        doFirst {
            Installer.setOpenCvVersion("3.1.0")
            Installer.installJni(jniPath)
        }
    }

    tasks.withType<Test> {
        dependsOn(installOpenCV)
        val defaultLibPath = System.getProperty("java.library.path");
        jvmArgs = listOf("-Djava.library.path=$defaultLibPath${System.getProperty("path.separator")}$jniPath")
        if (project.hasProperty("genonly")) {
            useJUnit {
                includeCategories = setOf("edu.wpi.grip.ui.codegeneration.GenerationTesting")
            }
        }
    }
}

tasks.register<Delete>("cleanInstaller") {
    group = "Installer generation"
    description = "Deletes old installer files for the GRIP application installer."
    delete(buildDir.resolve("installer"))
}

tasks.register<Copy>("collectDependencies") {
    group = "Installer generation"
    description = "Collects dependencies into a single directory for jpackage."
    from(
            configurations["compile"],
            configurations["runtime"],
            configurations["runtimeClasspath"],
            project.projectDir.resolve("installer-files"),
            tasks.named("jar"),
            project("preloader").tasks.named("jar")
    )
    into(buildDir.resolve("installerInput"))
}

/**
 * Build a Java 11 runtime image with jlink. Otherwise a Java 13 image would be generated by
 * jpackage, which is not ideal given that Java 13 is not yet stable.
 *
 * This task regenerates the jlink image every time it is invoked.
 */
tasks.register<Exec>("jlink") {
    group = "Installer generation"
    description = "Generates a runtime image for a native installer."

    val outputDir = project.buildDir.resolve("jlink").absolutePath
    outputs.file(outputDir)
    delete(outputDir)

    setCommandLine(
            Jvm.current().javaHome.resolve("bin/jlink").absolutePath,
            "--add-modules",
            "java.base,java.desktop,java.datatransfer,java.logging,java.management,java.naming,java.prefs,java.scripting,java.sql,java.xml,jdk.dynalink,jdk.scripting.nashorn,jdk.unsupported",
            "--output", outputDir,
            "--no-header-files",
            "--compress=2",
            "--no-man-pages",
            "--strip-debug"
    )
}

tasks.register<JpackageExec>("jpackage") {
    group = "Installer generation"
    description = "Generates a native installer for the GRIP application."

    // TODO: Since Gradle does not run on JDK 13, we need to pass in the JDK home as part of the build process
    // See https://github.com/gradle/gradle/issues/8681
    if (!project.properties.containsKey("jdk13")) {
        logger.error("The path to a valid JDK 13 installation with jpackage must be provided with -Pjdk13=/path/to/jdk-13")
        return@register
    }

    val cleanInstaller: Delete by tasks
    val collectDependencies: Copy by tasks
    val jlink by tasks
    dependsOn(cleanInstaller, collectDependencies, jlink)

    val jdk13: String = project.property("jdk13").toString()

    jdkHome.set(File(jdk13))
    runtimeImage.set(jlink.outputs.files.singleFile)
    verbose.set(true)
    outputDir.set(buildDir.resolve("installer"))
    inputDir.set(collectDependencies.destinationDir)

    jvmArgs.setAll("-Xmx200M")

    mainJar.set(collectDependencies.destinationDir.resolve("ui-${project.version}.jar"))
    mainClassName.set("edu.wpi.grip.ui.Launch")

    applicationName.set("GRIP")
    applicationDescription.set("GRIP Computer Vision Engine")

    val projectVersion = "${project.version}"
    applicationVersion.set(projectVersion.drop(1).takeWhile { it != '-' }) // 'v1.5.2-abfa51a' -> '1.5.2'
    fullApplicationVersion.set(projectVersion)
    copyright.set("Copyright (c) 2015-2019 WPI")
    licenseFile.set(rootDir.resolve("LICENSE.txt"))
    applicationVendor.set("Worcester Polytechnic Institute")
    identifier.set("edu.wpi.grip")

    configureForCurrentOs()

    winUpgradeUuid.set("d74b4d69-a88a-47ef-b972-9a7911cf7af1")
    winRegistryName.set("edu.wpi.grip")
    addToWindowsMenu.set(true)
    addWindowsDesktopShortcut.set(true)

    macBundleIdentifier.set("edu.wpi.grip")
}

application {
    mainClassName = "edu.wpi.grip.ui.Launch"
}

/**
 * Gets the installer type to use for the current operating system. Windows uses `.exe`, mac `.dmg`,
 * and linux `.deb`.
 */
fun installerTypeForCurrentOs() = when (OperatingSystem.current()) {
    OperatingSystem.WINDOWS -> "exe"
    OperatingSystem.MAC_OS -> "dmg"
    OperatingSystem.LINUX -> "deb"
    else -> throw UnsupportedOperationException("Unsupported OS")
}

/**
 * The base directory for installer files.
 */
val installerFilesBaseDir by lazy {
    projectDir.resolve("installer-files")
}

/**
 * Configures a jpackage task for the current operating system.
 */
fun JpackageExec.configureForCurrentOs() {
    when (OperatingSystem.current()) {
        OperatingSystem.WINDOWS -> {
            val installerFileDir = installerFilesBaseDir.resolve("win")
            resourceDir.set(installerFileDir)
            icon.set(installerFileDir.resolve("grip_TP6_icon.ico"))
            fileAssociations.set(installerFileDir.resolve("file-associations.properties"))
            installerType.set("exe")
        }
        OperatingSystem.MAC_OS -> {
            val installerFileDir = installerFilesBaseDir.resolve("mac")
            resourceDir.set(installerFileDir)
            icon.set(installerFileDir.resolve("GRIP.icns"))
            installerType.set("dmg")
        }
        OperatingSystem.LINUX -> {
            val installerFileDir = installerFilesBaseDir.resolve("linux")
            resourceDir.set(installerFileDir)
            icon.set(installerFileDir.resolve("GRIP.png"))
            installerType.set("deb")
        }
    }
}
