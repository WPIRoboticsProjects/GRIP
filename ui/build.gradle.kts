plugins {
    java
    idea
    jacoco
    application
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
    compile (group= "org.controlsfx", name= "controlsfx", version= "8.40.11")
    compile( group= "com.hierynomus", name= "sshj", version= "0.16.0")
    compile( group= "org.apache.velocity", name= "velocity", version= "1.7")
    val coreOutput = coreProject.`java`.sourceSets["test"].output
    testCompile( files(coreOutput.classesDirs))
    testCompile( files(coreOutput.resourcesDir))
    testCompile( group= "org.testfx", name= "testfx-core", version= "4.0.5-alpha")
    testCompile( group= "org.testfx", name= "testfx-junit", version= "4.0.5-alpha")
    testRuntime( group= "org.testfx", name= "openjfx-monocle", version= "1.8.0_20")
    testCompile( group= "org.opencv", name= "opencv-java", version= "3.1.0")
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
        scopes["PROVIDED"]?.get("")?.add(ideProviderConfiguration)
    }
}


