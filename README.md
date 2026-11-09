# 지뢰찾기 (Minesweeper)

Java Swing으로 만드는 PC용 지뢰찾기 게임 프로젝트입니다.

## 프로젝트 상태

프로젝트 폴더와 README를 생성한 초기 단계입니다. 게임 구현과 실행 환경 설정은 아직 진행하지 않았습니다.

## 작업 목록

개발 단계별 체크리스트는 [TASKS.md](TASKS.md)에서 관리합니다.

## 기본 게임 규칙

1. 칸을 열어 지뢰의 위치를 추리합니다.
2. 숫자는 해당 칸 주변 8칸에 있는 지뢰의 개수를 뜻합니다.
3. 지뢰가 있다고 생각하는 칸에는 깃발을 표시합니다.
4. 지뢰가 없는 모든 칸을 열면 승리하며, 지뢰를 열면 패배합니다.

## 실행 방법

JDK 17 이상이 필요합니다. 프로젝트 폴더의 PowerShell에서 실행합니다.

```powershell
.\mvnw.cmd clean verify
java -jar target/minesweeper-1.0-SNAPSHOT.jar
```

macOS/Linux에서는 `./mvnw clean verify`로 빌드합니다.
Maven은 Wrapper가 자동으로 내려받으므로 따로 설치하지 않아도 됩니다.

## 개발 환경

- 프로젝트 기준 버전: Java 17
- 확인된 로컬 JDK: Microsoft OpenJDK 17.0.16 (`C:\Users\dltnq\.jdks\ms-17.0.16`)
- 현재 셸에는 JAVA_HOME과 Java 실행 경로가 설정되어 있지 않습니다.
- Maven 명령은 현재 PATH에서 찾을 수 없습니다. Maven 프로젝트 구성 단계에서 실행 환경을 준비합니다.

현재 PowerShell 세션에서 JDK를 사용하려면 다음과 같이 설정합니다.

```powershell
$env:JAVA_HOME = 'C:\Users\dltnq\.jdks\ms-17.0.16'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
javac -version
```

작업 절차는 [CONTRIBUTING.md](CONTRIBUTING.md)를 따릅니다.
