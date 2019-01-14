plugins {
    `application`
}

application {
    mainClassName = "edu.wpi.grip.preloader.GripPreloader"
}

tasks.named<JavaExec>("run") {
    classpath = sourceSets["main"].runtimeClasspath
    main = application.mainClassName
    args = listOf("windowed")
}
