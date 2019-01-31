plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(group = "com.google.guava", name = "guava", version = "20.0")
    api(group = "com.google.auto.service", name = "auto-service", version = "+")
    annotationProcessor(group = "com.google.auto.service", name = "auto-service", version = "+")
}
