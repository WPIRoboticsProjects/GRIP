import org.ajoberstar.grgit.Grgit

buildscript {
    repositories {
        jcenter()
        maven {
            setUrl("https://github.com/WPIRoboticsProjects/opencv-maven/raw/mvn-repo")
        }
    }
    dependencies {
        classpath(group = "de.dynamicfiles.projects.gradle.plugins", name = "javafx-gradle-plugin", version = "8.8.2")
        classpath(group = "edu.wpi.first.wpilib.opencv", name = "opencv-installer", version = "2.0.0")
    }
}

plugins {
    id("com.github.johnrengelman.shadow") version "4.0.3"
    id("com.google.osdetector") version "1.4.0"
    id("org.ajoberstar.grgit") version "2.0.0" apply false
}

tasks.withType<Wrapper>().configureEach {
    gradleVersion = "5.0"
    distributionType = Wrapper.DistributionType.ALL
}

subprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven {
            name = "WPILib Maven Release"
            setUrl("http://first.wpi.edu/FRC/roborio/maven/release")
        }
        maven {
            name = "rosjava Maven"
            setUrl("https://github.com/rosjava/rosjava_mvn_repo/raw/master")
        }
        maven {
            name = "GRIP ROS Maven"
            setUrl("https://github.com/WPIRoboticsProjects/rosjava_mvn_repo/raw/master")
        }
    }

    version = getVersionName()

    afterEvaluate {
        if (pluginManager.hasPlugin("java")) {
            dependencies {
                "compileOnly"(group = "com.google.code.findbugs", name = "annotations", version = "3.0.1")
                "testCompile"(group = "net.jodah", name = "concurrentunit", version = "0.4.2")
                "testCompile"(group = "org.hamcrest", name = "hamcrest-all", version = "1.3")
                "testCompile"(group = "junit", name = "junit", version = "4.12")
                "testCompile"(group = "com.google.truth", name = "truth", version = "0.34")
                "testCompile"(group = "com.google.guava", name = "guava-testlib", version = "22.0")
            }
        }
    }
}

var git_provided = false
var grgit: Grgit? = null
if (rootProject.file(".git").exists()) {
    git_provided = true
    project.apply {
        plugin("org.ajoberstar.grgit")
    }
    grgit = Grgit.open()
}

fun Project.getGitCommit(): String {
    return grgit?.head()?.abbreviatedId ?: "<no commit>"
}

fun Project.getGitDescribe(): String {
    return grgit?.describe() ?: "v0.0.0"
}

fun Project.getGitDescribeAbbrev(): String {
    return grgit?.tag?.list()?.last()?.name ?: "v0.0.0"
}

fun Project.getVersionName(): String {
    if (project.hasProperty("vers")) {
        val vers: String by properties
        return vers
    }
    return getGitDescribe()
}

fun Project.getVersionSimple(): String {
    if (project.hasProperty("vers")) {
        val vers: String by properties
        return vers
    }
    return getGitDescribeAbbrev()
}
