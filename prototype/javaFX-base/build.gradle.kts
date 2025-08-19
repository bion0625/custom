plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
    version = "17.0.13"
    modules = listOf("javafx.controls", "javafx.fxml")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val javafxVersion = "17.0.13"
val osName = System.getProperty("os.name").lowercase()
val platform = when {
    osName.contains("win") -> "win"
    osName.contains("mac") -> "mac"
    osName.contains("linux") -> "linux"
    else -> throw GradleException("Unknown OS: $osName")
}

dependencies {
    // 컴파일
    implementation("org.openjfx:javafx-controls:$javafxVersion")
    implementation("org.openjfx:javafx-fxml:$javafxVersion")

    // 런타임(네이티브 포함)
    runtimeOnly("org.openjfx:javafx-graphics:$javafxVersion:$platform")
    runtimeOnly("org.openjfx:javafx-controls:$javafxVersion:$platform")
    runtimeOnly("org.openjfx:javafx-fxml:$javafxVersion:$platform")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("org.example.MainApp") // JavaFX Application 클래스 경로
}