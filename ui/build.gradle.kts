
import edu.wpi.first.wpilib.opencv.installer.Installer
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.junit.JUnitOptions
import org.gradle.script.lang.kotlin.*
import org.gradle.util.GFileUtils

buildscript {
    repositories {
        maven {
            setUrl("https://github.com/WPIRoboticsProjects/opencv-maven/raw/mvn-repo")
        }
    }
    dependencies {
        classpath(dependencyNotation = "edu.wpi.first.wpilib.opencv:opencv-installer:2.0.0")
    }
}

plugins {
    java
    idea
    jacoco
    application
}
apply {
    plugin("com.google.osdetector")
}

val ideProviderConfiguration = configurations.maybeCreate("ideProvider")

if (!(project.hasProperty("generation") || project.hasProperty("genonly"))) {
    java {
        sourceSets {
            "test" {
                java {
                    exclude("**/ui/codegeneration")
                    exclude("**/ui/codegeneration/**")
                }
            }
        }
    }
}
val coreProject = project(":core")
dependencies {
    compile(project(path = ":core", configuration = "shadow"))
    compile(project(path = ":ui:preloader"))
    (ideProviderConfiguration.name)(project(path = ":core", configuration = "compile"))
    compile(group = "org.controlsfx", name = "controlsfx", version = "8.40.11")
    compile(group = "com.hierynomus", name = "sshj", version = "0.16.0")
    compile(group = "org.apache.velocity", name = "velocity", version = "1.7")
    val coreOutput = coreProject.`java`.sourceSets["test"].output
    testCompile(files(coreOutput.classesDirs))
    testCompile(files(coreOutput.resourcesDir))
    testCompile(group = "org.testfx", name = "testfx-core", version = "4.0.5-alpha")
    testCompile(group = "org.testfx", name = "testfx-junit", version = "4.0.5-alpha")
    testRuntime(group = "org.testfx", name = "openjfx-monocle", version = "1.8.0_20")
    testCompile(group = "org.opencv", name = "opencv-java", version = "3.1.0")
}

evaluationDependsOn(":core")
evaluationDependsOn(":ui:preloader")

tasks {
    if (System.getProperty("os.name").toLowerCase().contains("linux")) {
        "jfxNative"{
            dependsOn(tasks.getByPath(":ui:linuxLauncher:linuxLauncherExecutable"))
        }
    }
    "compileTestJava" {
        dependsOn(tasks.getByPath(":core:testClasses"))
    }

    /*
     * Allows you to run the UI tests in headless mode by calling gradle with the -Pheadless=true argument
     */
    if (project.hasProperty("headless") && properties["headless"].toString().toBoolean()) {
        println("Running UI Tests Headless")
        "test"(Test::class) {
            jvmArgs = listOf(
                    "-Djava.awt.headless=true",
                    "-Dtestfx.robot=glass",
                    "-Dtestfx.headless=true",
                    "-Dprism.order=sw",
                    "-Dprism.text=t2k"
            )
        }
    }
}

idea {
    module {
        scopes["PROVIDED"]?.get("plus")?.add(ideProviderConfiguration)
    }
}

fun setupTestSharedLib() {
    val syst = osdetector.os
    val testClassesDir = file("$buildDir/classes/test")
    val sharedLibWorkingDir = file("$testClassesDir/pipelib")
    val testSharedLibStep1 = task<Exec>("testSharedLibStep1") {
        doFirst {
            GFileUtils.mkdirs(sharedLibWorkingDir)
        }
        workingDir = sharedLibWorkingDir
        if (syst == "windows") {
            commandLine(
                    "\"C:\\Program Files (x86)\\Microsoft Visual Studio 14.0\\VC\\vcvarsall.bat\"",
                    "amd64",
                    "&",
                    "cmake",
                    "-G",
                    "Visual Studio 14 2015 Win64",
                    "..\\..\\..\\..\\src\\test\\resources\\edu\\wpi\\grip\\ui\\codegeneration\\tools\\"
            )
        } else {
            commandLine(
                    "cmake",
                    "../../../../src/test/resources/edu/wpi/grip/ui/codegeneration/tools/"
            )
        }
    }

    val testSharedLibStep2 = task<Exec>("testSharedLibStep2") {
        dependsOn(testSharedLibStep1)
        workingDir = sharedLibWorkingDir
        if (syst == "windows") {
            commandLine(
                    "\"C:\\\\Program Files (x86)\\\\Microsoft Visual Studio 14.0\\\\VC\\\\vcvarsall.bat\"",
                    "amd64",
                    "&",
                    "cmake",
                    "--build",
                    ".",
                    "--target",
                    "ALL_BUILD",
                    "--config",
                    "Release"
            )
        } else {
            commandLine("make")
        }
    }
    val testSharedLib = task<Copy>("testSharedLib") {
        description = "Compiles the shared library used by c++ generation testing."
        group = "build"
        dependsOn(testSharedLibStep2)
        into(testClassesDir) {
            if (syst == "osx") {
                from("$testClassesDir/pipelib") {
                    include("libgenJNI.dylib")
                }
            } else if (syst == "linux") {
                from("$testClassesDir/pipelib") {
                    include("libgenJNI.so")
                }
            } else if (syst == "windows") {
                from("pipelib/Release") {
                    include("**")
                }
                from("pipelib/pipe/Release") {
                    include("**")
                }
            }
        }
        into(file("$sharedLibWorkingDir/pipe")) {
            from("src/test/resources/edu/wpi/grip/ui/codegeneration/tools/pipe") {
                include("AbsPipeline.h")
            }
        }
        into(testClassesDir) {
            from("src/test/resources/edu/wpi/grip/ui/codegeneration/tools/realpipe") {
                include("CMakeLists.txt")
            }
        }
    }
}
setupTestSharedLib()

fun setupGenerationTests() {
    fun getJniLocation() =
            properties["jniLocation"]?.toString() ?:  Installer.getPlatform().defaultJniLocation()
    val installOpenCVTask = task("installOpenCV") {
        doLast {
            val platform = Installer.getPlatform()
            Installer.setOpenCvVersion("3.1.0")
            val jniLocation = getJniLocation()
            Installer.installJni(jniLocation)
        }
    }
    tasks {
        "test"(Test::class) {
            dependsOn(installOpenCVTask)
            val defaultLibPath = System.getProperty("java.library.path")
            jvmArgs("-Djava.library.path=$defaultLibPath${System.getProperty("path.separator")}" +
                            getJniLocation())
            if(project.hasProperty("genonly")){
                useJUnit {
                    this as JUnitOptions
                    includeCategories("edu.wpi.grip.ui.codegeneration.GenerationTesting")
                }
            }
        }
    }
}
if (project.hasProperty("generation") || project.hasProperty("genonly")) {
    setupGenerationTests()
}





