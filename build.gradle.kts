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
    `java`
    `jacoco`
    `checkstyle`
    `pmd`
    id("com.github.johnrengelman.shadow") version "4.0.3"
    id("com.google.osdetector") version "1.4.0"
    id("org.ajoberstar.grgit") version "2.0.0" apply false
}

repositories {
    mavenCentral()
    jcenter()
}

tasks.withType<Wrapper>().configureEach {
    gradleVersion = "5.0"
    distributionType = Wrapper.DistributionType.ALL
}

fun javaSubprojects(action: Project.() -> Unit) {
    subprojects.minus(project(":ui:linuxLauncher")).forEach { project ->
        project.action()
    }
}

javaSubprojects {
    apply {
        plugin("java")
        plugin("org.gradle.jacoco")
        plugin("org.gradle.pmd")
        plugin("org.gradle.checkstyle")
    }
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

    dependencies {
        "compile"(group = "com.google.code.findbugs", name = "annotations", version = "3.0.1")
        "testCompile"(group = "net.jodah", name = "concurrentunit", version = "0.4.2")
        "testCompile"(group = "org.hamcrest", name = "hamcrest-all", version = "1.3")
        "testCompile"(group = "junit", name = "junit", version = "4.12")
        "testCompile"(group = "com.google.truth", name = "truth", version = "0.34")
        "testCompile"(group = "com.google.guava", name = "guava-testlib", version = "22.0")
    }

    checkstyle {
        configFile = rootDir.resolve("checkstyle.xml")
        toolVersion = "6.19"
        if (project.hasProperty("ignoreCheckstyle")) {
            isIgnoreFailures = true
        }
    }

    pmd {
        toolVersion = "5.6.0"
        isConsoleOutput = true
        val projectSourcesSets = this@javaSubprojects.sourceSets
        sourceSets = listOf(projectSourcesSets["main"], projectSourcesSets["test"])
        reportsDir = buildDir.resolve("reports/pmd")
        ruleSetFiles = files(rootDir.resolve("pmd-ruleset.xml"))
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        reports {
            html.isEnabled = true
            xml.isEnabled = true
        }
    }

    tasks.withType<Javadoc> {
        source(tasks.named<JavaCompile>("compileJava").map { it.source })
    }

}

tasks.register<JacocoReport>("jacocoRootReport") {
    group = "Coverage reports"
    description = "Generates an aggregate report from all subprojects"

    reports {
        html.isEnabled = true
        xml.isEnabled = true
    }

    javaSubprojects {
        val sourceSets = (this as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer
        dependsOn(tasks["test"])
        val srcFiles = files(sourceSets["main"].allSource.srcDirs)
        additionalSourceDirs(srcFiles)
        sourceDirectories.from(srcFiles)
        classDirectories.from(files(sourceSets["main"].output))
        executionData.from(tasks.named<JacocoReport>("jacocoTestReport").map { it.executionData })
    }
    doFirst {
        executionData.setFrom(files(executionData.files.filter { it.exists() }))
    }
}

var grgit: Grgit? = null
if (rootProject.file(".git").exists()) {
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
