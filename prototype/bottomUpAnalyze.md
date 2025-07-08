# 프로젝트 구조

```
project-root/
├── bottomUpAnalyze/
│   ├── Main.java
│   ├── ... 기타 .java 소스 파일
├── out/             # 컴파일된 .class 파일이 저장될 디렉터리
└── MANIFEST.MF      # JAR 매니페스트 파일
```

* `bottomUpAnalyze/` : Java 패키지 구조에 맞춘 `.java` 소스
* `out/` : `javac -d` 옵션으로 컴파일된 `.class` 파일 출력
* `MANIFEST.MF` : JAR 실행 시 메인 클래스를 지정하는 매니페스트

---

## 1. 컴파일

모든 `.java` 파일을 한 번에 컴파일하고, 결과를 `out` 디렉터리에 패키지 구조대로 모읍니다.

```
javac -encoding UTF-8 -d out .\bottomUpAnalyze\Main.java
```

* `-encoding UTF-8` : 소스 파일 문자셋을 UTF-8로 지정
* `-d out` : 컴파일된 클래스 파일을 `out` 폴더에 패키지 구조로 생성

---

## 2. JAR 패키징

### 1) 매니페스트 파일(`MANIFEST.MF`) 생성

```text
Manifest-Version: 1.0
Main-Class: bottomUpAnalyze.Main
```

### 2) jar 명령으로 실행용 JAR 생성

```bash
jar cfm bottomUpAnalyze.jar MANIFEST.MF -C out .
```

* `c` : JAR 생성(create)
* `f` : 결과 파일 이름 지정(file)
* `m` : 지정된 매니페스트(manifest)를 포함
* `-C out .` : `out` 디렉터리 내부 모든 파일을 JAR에 포함

이렇게 하면 모든 클래스가 포함된 `app.jar`가 생성됩니다.

---

## 3. 실행

```bash
java -jar bottomUpAnalyze.jar
```

---

## 4. 팁

* `out/` 디렉터리는 빌드 산출물이므로 `.gitignore`에 추가하세요.
* 소스 수정 후 `out/`을 지우고(`rm -rf out/*` 또는 `Remove-Item -Recurse out\*`),
  다시 컴파일하면 깔끔하게 빌드할 수 있습니다.
