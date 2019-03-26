plugins {
    `application`
}

createNativeConfigurations()

dependencies {
    javafx("base")
    javafx("controls")
    javafx("fxml")
    javafx("graphics")
}

application {
    mainClassName = "edu.wpi.grip.preloader.Launch"
}

tasks.named<JavaExec>("run") {
    classpath = sourceSets["main"].runtimeClasspath
    main = application.mainClassName
    args = listOf("windowed")
}
