import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("java-library")
    id("application")
    id("com.github.johnrengelman.shadow")
    id("com.google.osdetector")
}

application {
    mainClassName = "edu.wpi.grip.core.Main"
}

val os = osdetector.classifier.replace("osx", "macosx").replace("x86_32", "x86")
val arch = osdetector.arch.replace("x86_64", "x64")

dependencies {
    api(group = "com.google.code.findbugs", name = "jsr305", version = "3.0.1")
    api(group = "org.bytedeco", name = "javacv", version = "1.1")
    api(group = "org.bytedeco.javacpp-presets", name = "opencv", version = "3.0.0-1.1")
    api(group = "org.bytedeco.javacpp-presets", name = "opencv", version = "3.0.0-1.1", classifier = os)
    api(group = "org.bytedeco.javacpp-presets", name = "videoinput", version = "0.200-1.1", classifier = os)
    api(group = "org.bytedeco.javacpp-presets", name = "ffmpeg", version = "0.200-1.1", classifier = os)
    api(group = "org.python", name = "jython", version = "2.7.0")
    api(group = "com.thoughtworks.xstream", name = "xstream", version = "1.4.10")
    api(group = "org.apache.commons", name = "commons-lang3", version = "3.5")
    api(group = "com.google.guava", name = "guava", version = "20.0")
    api(group = "com.google.auto.value", name = "auto-value", version = "1.3")
    api(group = "com.google.code.gson", name = "gson", version = "2.8.0")
    api(group = "org.eclipse.jetty", name = "jetty-server", version = "9.3.14.v20161028")
    testCompile(group = "org.apache.httpcomponents", name = "httpclient", version = "4.5.2")
    testCompile(group = "org.apache.httpcomponents", name = "httpcore", version = "4.4.5")
    testCompile(group = "org.apache.httpcomponents", name = "httpmime", version = "4.5.2")
    api(group = "commons-cli", name = "commons-cli", version = "1.3.1")
    api(group = "commons-io", name = "commons-io", version = "2.4")
    // We use the no_aop version of Guice because the aop isn"t avaiable in arm java
    // http://stackoverflow.com/a/15235190/3708426
    // https://github.com/google/guice/wiki/OptionalAOP
    api(group = "com.google.inject", name = "guice", version = "4.1.0", classifier = "no_aop")
    api(group = "com.google.inject.extensions", name = "guice-assistedinject", version = "4.1.0")

    // Lombok (replaces AutoValue)
    compileOnly(group = "org.projectlombok", name = "lombok", version = "1.18.4")
    annotationProcessor("org.projectlombok:lombok:1.18.4")

    // Network publishing dependencies
    api(group = "org.ros.rosjava_core", name = "rosjava", version = "+")
    api(group = "org.ros.rosjava_bootstrap", name = "message_generation", version = "+")
    api(group = "org.ros.rosjava_messages", name = "std_msgs", version = "+")
    api(group = "org.ros.rosjava_messages", name = "grip_msgs", version = "0.0.1")
    api(group = "edu.wpi.first.wpilib.networktables.java", name = "NetworkTables", version = "3.1.2", classifier = "desktop")
    api(group = "edu.wpi.first.wpilib.networktables.java", name = "NetworkTables", version = "3.1.2", classifier = "arm")
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = application.mainClassName
    }
}

tasks.withType<ShadowJar>().configureEach {
    /* The icudt54b directory in Jython takes up 9 megabytes and doesn"t seem to do anything useful. */
    exclude("org/python/icu/impl/data/icudt54b/")

    /* We don"t use all of the OpenCV libraries, and they seem to take up a lot of space.  If we ever decide to
    use any more of these (or perhaps just include them for people to use from Python scripts), the following lines
    should be changed, but for now this saves us a lot of space. */
    exclude("org/bytedeco/javacpp/*/*calib3d*")
    exclude("org/bytedeco/javacpp/*/*optflow*")
    exclude("org/bytedeco/javacpp/*/*photo*")
    exclude("org/bytedeco/javacpp/*/*shape*")
    exclude("org/bytedeco/javacpp/*/*stitching*")
    exclude("org/bytedeco/javacpp/*/*superres*")
    exclude("org/bytedeco/javacpp/*/*videostab*")
    exclude("org/bytedeco/javacpp/*/*xfeatures2d*")
}
