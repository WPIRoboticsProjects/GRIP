import de.dynamicfiles.projects.gradle.plugins.javafx.JavaFXGradlePluginExtension
import de.dynamicfiles.projects.gradle.plugins.javafx.tasks.JfxNativeTask
import edu.wpi.first.wpilib.opencv.installer.Installer
import java.io.FileFilter

plugins {
    `application`
    id("javafx-gradle-plugin")
    id("com.google.osdetector")
}

if (!(project.hasProperty("generation") || project.hasProperty("genonly"))) {
    sourceSets {
        test {
            java {
                exclude("**/ui/codegeneration")
                exclude("**/ui/codegeneration/**")
            }
        }
    }
}

dependencies {
    compile(project(":core"))
    compile(project(":ui:preloader"))
    //ideProvider project(path= ":core", configuration= "compile")
    compile(group = "org.controlsfx", name = "controlsfx", version = "8.40.11")
    compile(group = "com.hierynomus", name = "sshj", version = "0.16.0")
    compile(group = "org.apache.velocity", name = "velocity", version = "1.7")

    val coreTestOutput = project(":core").dependencyProject.sourceSets["test"].output
    testCompile(files(coreTestOutput))
    testCompile(files(coreTestOutput.resourcesDir))
    testCompile(group = "org.testfx", name = "testfx-core", version = "4.0.5-alpha")
    testCompile(group = "org.testfx", name = "testfx-junit", version = "4.0.5-alpha")
    testRuntime(group = "org.testfx", name = "openjfx-monocle", version = "1.8.0_20")
    testCompile(group = "org.opencv", name = "opencv-java", version = "3.1.0")
}

evaluationDependsOn(":core")
evaluationDependsOn(":ui:preloader")
if (System.getProperty("os.name").toLowerCase().contains("linux")) {
    tasks.named("jfxNative") {
        dependsOn(":ui:linuxLauncher:linuxLauncherExecutable")
    }
}

tasks.named<JavaCompile>("compileTestJava") {
    dependsOn(":core:testClasses")
}

/*
 * Allows you to run the UI tests in headless mode by calling gradle with the -Pheadless=true argument
 */
if (project.hasProperty("headless")) {
    println("Running UI Tests Headless")
    tasks.withType<Test>() {
        jvmArgs = listOf(
                "-Djava.awt.headless=true",
                "-Dtestfx.robot=glass",
                "-Dtestfx.headless=true",
                "-Dprism.order=sw",
                "-Dprism.text=t2k"
        )
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
    val syst = osdetector.os
    tasks.withType<Test>() {
        val platform = Installer.getPlatform()
        Installer.setOpenCvVersion("3.1.0")
        val jniLocation = project.properties.getOrDefault("jniLocation", platform.defaultJniLocation())
        Installer.installJni("$jniLocation")
        val defaultLibPath = System.getProperty("java.library.path");
        jvmArgs = listOf("-Djava.library.path=$defaultLibPath${System.getProperty("path.separator")}$jniLocation")
        if (project.hasProperty("genonly")) {
            useJUnit {
                includeCategories = setOf("edu.wpi.grip.ui.codegeneration.GenerationTesting")
            }
        }
    }
}

val arch = osdetector.arch.replace("x86_64", "x64")

jfx {
    mainClass = "edu.wpi.grip.ui.Main"
    preLoader = "edu.wpi.grip.preloader.GripPreloader"

    identifier = "GRIP"
    appName = "GRIP"
    vendor = "Worcester Polytechnic Institute"
    nativeReleaseVersion = "$version-$arch"

    jfxMainAppJarName = "${jfx.appName}-${jfx.nativeReleaseVersion}.jar"

    // -XX:-OmitStackTraceInFastThrow prevents the JIT from eating stack traces that get thrown a lot
    // This is slower but means we actually get the stack traces instead of
    // having them become one line like `java.lang.ArrayIndexOutOfBoundsException`
    // and as such, would be useless.
    // See= https://plumbr.eu/blog/java/on-a-quest-for-missing-stacktraces
    // -Xmx limits the heap size. This prevents memory use from ballooning with a lot
    // of JavaCV native objects being allocated hanging around waiting to get GC"d.
    // -XX:MaxNewSize limits the size of the eden space to force minor GCs to run more often.
    // This causes old mats (which take up little space on the heap but a lot of native memory) to get deallocated
    // and free up native memory quicker, limiting the memory the app takes up.
    jvmArgs = listOf("-XX:-OmitStackTraceInFastThrow", "-Xmx200m", "-XX:MaxNewSize=32m")

    bundleArguments = mapOf(
            "linux.launcher.url" to file("linuxLauncher/build/exe/linuxLauncher/linuxLauncher").toURI().toURL().toExternalForm()
    )
}

tasks.named<JfxNativeTask>("jfxNative") {
    dependsOn(":core:jar")

    // The JavaFX plugin removes all but numeric characters after the app name in the file
    // name, so we restore the full name of the file here
    doLast {
        logger.log(LogLevel.INFO, "Renaming installer packages")
        val packageFileFilter = FileFilter { file ->
            listOf("exe", "dmg", "pkg", "deb", "rpm").any {
                file.extension == it
            }
        }
        val packageFiles = buildDir.resolve("jfx/native").listFiles(packageFileFilter)
        packageFiles.forEach { packageFile ->
            val newName: String = jfx.jfxMainAppJarName.replace(Regex("""\.jar$"""), ".${packageFile.extension}")
            logger.log(LogLevel.DEBUG, "Renaming ${packageFile.name} to $newName")
            packageFile.renameTo(packageFile.resolveSibling(newName))
        }
    }
}

application {
    mainClassName = jfx.mainClass
}

val jfx: JavaFXGradlePluginExtension
    get() = extensions.getByType(JavaFXGradlePluginExtension::class.java)

fun jfx(configuration: JavaFXGradlePluginExtension.() -> Unit) = jfx.apply(configuration)
