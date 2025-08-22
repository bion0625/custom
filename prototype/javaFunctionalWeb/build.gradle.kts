plugins {
    id("java")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

val mainCls = "org.example.App"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.undertow:undertow-core:2.3.18.Final")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    // App.java 의 FQCN (아래 예제와 동일하게)
    mainClass.set(mainCls)
}


// 일반 jar에도 매니페스트 넣어두면 편함
tasks.jar {
    manifest { attributes["Main-Class"] = mainCls }
}

// ★ Shadow(fat-jar) 설정: 서비스 파일 머지 + 파일명/버전 깔끔화
tasks.shadowJar {
    archiveBaseName.set("app")     // app.jar
    archiveClassifier.set("")      // -all 같은 접미사 제거
    archiveVersion.set("")         // 버전 제거
    manifest { attributes["Main-Class"] = mainCls }

    // ServiceLoader 리소스(META-INF/services/*) 합치기 — Undertow/XNIO에 중요
    mergeServiceFiles()
}