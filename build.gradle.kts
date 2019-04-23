import com.github.spotbugs.SpotBugsTask

buildscript {
    repositories {
        jcenter()
        maven {
            setUrl("https://github.com/WPIRoboticsProjects/opencv-maven/raw/mvn-repo")
        }
    }
    dependencies {
        classpath(group = "edu.wpi.first.wpilib.opencv", name = "opencv-installer", version = "2.0.0")
        classpath("com.netflix.nebula:gradle-aggregate-javadocs-plugin:2.2.+")

    }
}

plugins {
    `java`
    `jacoco`
    `checkstyle`
    `pmd`
    id("com.github.johnrengelman.shadow") version "4.0.1"
    id("com.google.osdetector") version "1.4.0"
    id("org.ajoberstar.grgit") version "2.0.0" apply false
    //id("net.ltgt.errorprone") version "0.0.16"
    id("com.github.spotbugs") version "1.7.1"
    id("com.gradle.build-scan") version "2.1"
}

apply {
    plugin("nebula-aggregate-javadocs")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
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
        //plugin("net.ltgt.errorprone")
        plugin("com.github.spotbugs")
    }
    repositories {
        mavenCentral()
        jcenter()
        maven {
            name = "WPILib Maven Release"
            setUrl("https://first.wpi.edu/FRC/roborio/maven/release")
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

    version = getGitVersion()

    dependencies {
        "compile"(group = "javax.annotation", name = "javax.annotation-api", version = "1.3.2")
        "annotationProcessor"(group = "javax.annotation", name = "javax.annotation-api", version = "1.3.2")
        "compile"(group = "com.google.code.findbugs", name = "annotations", version = "3.0.1")
        "testCompile"(group = "net.jodah", name = "concurrentunit", version = "0.4.2")
        "testCompile"(group = "org.hamcrest", name = "hamcrest-all", version = "1.3")
        "testCompile"(group = "junit", name = "junit", version = "4.12")
        "testCompile"(group = "com.google.truth", name = "truth", version = "0.34")
        "testCompile"(group = "com.google.guava", name = "guava-testlib", version = "22.0")
    }

    checkstyle {
        toolVersion = "6.19"
        if (project.hasProperty("ignoreCheckstyle")) {
            isIgnoreFailures = true
        }
    }

    pmd {
        toolVersion = "6.7.0"
        isConsoleOutput = true
        val projectSourcesSets = this@javaSubprojects.sourceSets
        sourceSets = listOf(projectSourcesSets["main"], projectSourcesSets["test"])
        reportsDir = buildDir.resolve("reports/pmd")
        ruleSetFiles = files(rootDir.resolve("pmd-ruleset.xml"))
        ruleSets = emptyList()
    }

//    configurations["errorprone"].apply {
//        resolutionStrategy.force("com.google.errorprone:error_prone_core:2.3.2")
//    }

    spotbugs {
        toolVersion = "3.1.12"
        val javaSources = this@javaSubprojects.sourceSets
        sourceSets = setOf(javaSources["main"], javaSources["test"])
        excludeFilter = file("$rootDir/findBugsSuppressions.xml")
        effort = "max"
    }

    tasks.withType<SpotBugsTask> {
        reports {
            xml.isEnabled = false
            emacs.isEnabled = true
        }
        finalizedBy(task("${name}Report") {
            mustRunAfter(this@withType)
            doLast {
                this@withType
                        .reports
                        .emacs
                        .destination
                        .takeIf { it.exists() }
                        ?.readText()
                        .takeIf { !it.isNullOrBlank() }
                        ?.also { logger.warn(it) }
            }
        })
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

    tasks.named("check").configure {
        dependsOn("javadoc")
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "8"
    }

}

tasks.register<JacocoReport>("jacocoRootReport") {
    group = "Coverage reports"
    description = "Generates an aggregate report from all subprojects"

    // Based on the codecov Gradle example: https://github.com/codecov/example-gradle

    javaSubprojects {
        this@register.sourceSets(sourceSets["main"])
        this@register.dependsOn(tasks.named<JacocoReport>("jacocoTestReport"))
    }

    executionData(fileTree(rootDir.absolutePath).include("**/build/jacoco/*.exec"))

    reports {
        xml.isEnabled = true
        xml.destination = buildDir.resolve("reports/jacoco/report.xml")
        html.isEnabled = false
        csv.isEnabled = false
    }
}

allprojects {
    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
        isFailOnError = true
    }
}
