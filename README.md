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

## 검증

일반 빌드에는 게임 규칙 및 Swing 컴포넌트 테스트 12개가 포함됩니다.
실제 창과 마우스를 사용하는 테스트는 다음과 같이 별도로 활성화합니다.

```powershell
.\mvnw.cmd clean verify '-Ddesktop.tests=true'
```

전체 13개 테스트가 통과했습니다. 데스크톱 테스트는 잠시 창을 맨 위에 표시하고
마우스를 움직이므로 해당 실행 중에는 마우스를 사용하지 마세요.
테스트 후 창은 닫히며 화면 캡처는 `target/desktop-smoke.png`에 저장됩니다.
검증 항목은 첫 클릭 안전, 난이도별 지뢰 수, 주변 숫자, 자동 열기, 깃발,
승패와 종료 후 입력 제한, 재시작, 난이도 전환 및 타이머 시작입니다.

작업 절차는 [CONTRIBUTING.md](CONTRIBUTING.md)를 따릅니다.
