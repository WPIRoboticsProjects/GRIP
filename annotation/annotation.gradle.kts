plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(group = "com.google.auto.service", name = "auto-service", version = "1.0-rc6")
    annotationProcessor(group = "com.google.auto.service", name = "auto-service", version = "1.0-rc6")
}
