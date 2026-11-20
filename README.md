# 지뢰찾기 (Minesweeper)

Java Swing으로 만드는 PC용 지뢰찾기 게임 프로젝트입니다.

## 프로젝트 상태

초급·중급·고급 난이도를 지원하는 Java Swing 게임을 구현했습니다.
첫 클릭과 주변 8칸은 안전하며, 빈 칸 자동 열기, 깃발, 타이머, 새 게임 기능을 제공합니다.

## 작업 목록

개발 단계별 체크리스트는 [TASKS.md](TASKS.md)에서 관리합니다.

## 기본 게임 규칙

1. 칸을 열어 지뢰의 위치를 추리합니다.
2. 숫자는 해당 칸 주변 8칸에 있는 지뢰의 개수를 뜻합니다.
3. 지뢰가 있다고 생각하는 칸에는 깃발을 표시합니다.
4. 지뢰가 없는 모든 칸을 열면 승리하며, 지뢰를 열면 패배합니다.

## 실행 방법

JDK 17 이상과 그래픽 데스크톱 환경이 필요합니다. 아래 개발 환경 설정 후 프로젝트 폴더의 PowerShell에서 실행합니다.
첫 빌드에는 Maven 및 의존성을 내려받기 위한 인터넷 연결이 필요합니다.

```powershell
.\mvnw.cmd clean verify
java -jar target/minesweeper-1.0-SNAPSHOT.jar
```

macOS/Linux에서는 `./mvnw clean verify`로 빌드합니다.
Maven은 Wrapper가 자동으로 내려받으므로 따로 설치하지 않아도 됩니다.

## 조작법

- 왼쪽 클릭 또는 키보드로 칸 선택 후 Space: 칸 열기
- 오른쪽 클릭: 깃발(`F`) 표시 및 해제
- 열린 숫자 칸 클릭: 주변 깃발 수가 숫자와 같으면 깃발 없는 주변 칸을 한 번에 열기
- `새 게임`: 현재 난이도로 다시 시작
- 난이도 선택: 새 보드로 전환하며 타이머와 깃발 수 초기화
- 지뢰는 게임 종료 시 `*`로 표시됩니다.

| 난이도 | 가로 × 세로 | 지뢰 수 |
| --- | --- | --- |
| 초급 | 9 × 9 | 10 |
| 중급 | 16 × 16 | 40 |
| 고급 | 30 × 16 | 99 |

첫 칸을 열면 시간이 흐르고 승리 또는 패배 시 멈춥니다.
깃발은 칸 열기를 막으며, 깃발이 있는 칸을 열려면 먼저 해제해야 합니다.
남은 지뢰 수는 전체 지뢰 수에서 깃발 수를 뺀 값이므로 깃발을 많이 표시하면 음수가 될 수 있습니다.
주변 칸 한 번에 열기는 깃발 수만 확인합니다. 깃발 위치가 틀리면 지뢰가 열려 패배할 수 있습니다.

## 최고 기록

승리하면 난이도별 최단 시간을 밀리초 단위로 저장하고, 화면에는 소수점 셋째 자리 초로 표시합니다.
패배하거나 기존 기록보다 느리면 갱신하지 않습니다. 난이도를 바꾸면 해당 난이도의 기록을 표시합니다.
기록 파일은 사용자 홈의 `.minesweeper/best-times.properties`에 저장됩니다.
Windows에서는 일반적으로 `C:\Users\<사용자명>\.minesweeper\best-times.properties`입니다.
파일이 없으면 새로 만들고, 손상된 값은 제외합니다. 읽기·쓰기 실패 시 안내를 표시하고
게임은 계속되며 이번 실행 중 기록은 메모리에 유지됩니다.
개발 및 검증 시 `-Dminesweeper.dataDir=<경로>` JVM 옵션으로 저장 폴더를 변경할 수 있습니다.

## 개발 환경

- 프로젝트 기준 버전: Java 17
- 확인된 로컬 JDK: Microsoft OpenJDK 17.0.16 (`C:\Users\dltnq\.jdks\ms-17.0.16`)
- 빌드 도구: Maven Wrapper 3.3.2 / Maven 3.9.9
- UI: JDK에 포함된 Swing
- 테스트: JUnit 5

현재 PowerShell 세션에서 JDK를 사용하려면 다음과 같이 설정합니다.
다른 PC에서는 설치된 JDK 경로로 변경합니다.

```powershell
$env:JAVA_HOME = 'C:\Users\dltnq\.jdks\ms-17.0.16'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
javac -version
```

## Windows 배포본

GitHub Actions의 **Windows package** 워크플로는 main 대상 PR과 main 푸시에서
테스트·패키징·ZIP 압축 해제 후 포함 런타임 검증을 실행합니다. Actions 탭에서
성공한 실행의 `windows-x64-package` artifact를 내려받으면 배포 ZIP이 들어 있습니다.
`windows-verification-reports`에는 테스트 및 패키지 검증 결과가 있습니다.
수동 실행도 가능하며 실제 마우스를 움직이는 데스크톱 테스트는 실행하지 않습니다.

Windows x64에서 JDK 17의 `jpackage`로 런타임을 포함한 앱 폴더와 ZIP을 만듭니다.
개발 PC에는 `JAVA_HOME` 설정이 필요하지만 배포본 사용자는 별도 Java 설치가 필요 없습니다.
Maven 빌드·테스트에 성공한 뒤 앱을 패키징합니다. MSI 설치 파일을 만들지 않으므로 WiX는 필요하지 않습니다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Package-Windows.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Verify-WindowsPackage.ps1
```

- 앱: `target/windows/Minesweeper/Minesweeper.exe`
- 배포 ZIP: `target/Minesweeper-windows-x64.zip`
- 검증 환경: Windows 빌드 26200, x64, Microsoft OpenJDK 17.0.16

ZIP을 원하는 폴더에 전부 풀고 `Minesweeper.exe`를 실행합니다.
`runtime`과 `app` 폴더도 함께 필요하므로 EXE만 따로 옮기지 마세요.
최고 기록은 앱 폴더가 아닌 사용자 홈에 저장합니다. 배포본에는 개발 소스와 테스트를 넣지 않습니다.
검증 스크립트는 `JAVA_HOME`과 PATH에서 외부 Java를 제외하고 포함 런타임으로 기능과 기록 복원을 확인합니다.
생성 파일은 `target` 아래에 있으며 Git에는 포함하지 않습니다. 공개 릴리스 게시는 별도 작업입니다.

## 검증

GitHub Actions는 `main` 대상 PR과 `main` 푸시마다 Java 17에서 빌드 및
일반 테스트를 화면 없이 실행합니다. 실제 마우스를 사용하는 테스트는 CI에서 제외합니다.
실행 결과와 테스트 보고서는 저장소의 Actions 탭에서 확인할 수 있습니다.
로컬에서도 같은 조건으로 실행할 수 있습니다.

```powershell
.\mvnw.cmd clean verify '-Djava.awt.headless=true' '-Ddesktop.tests=false'
```

일반 빌드에는 게임 규칙 및 Swing 컴포넌트 테스트가 포함됩니다.
실제 창과 마우스를 사용하는 테스트는 다음과 같이 별도로 활성화합니다.

```powershell
.\mvnw.cmd clean verify '-Ddesktop.tests=true'
```

데스크톱 테스트는 잠시 창을 맨 위에 표시하고
마우스를 움직이므로 해당 실행 중에는 마우스를 사용하지 마세요.
테스트 후 창은 닫히며 화면 캡처는 `target/desktop-smoke.png`에 저장됩니다.
검증 항목은 첫 클릭 안전, 난이도별 지뢰 수, 주변 숫자, 자동 열기, 깃발,
승패와 종료 후 입력 제한, 재시작, 난이도 전환, 타이머 시작 및 주변 칸 한 번에 열기입니다.

작업 절차는 [CONTRIBUTING.md](CONTRIBUTING.md)를 따릅니다.
