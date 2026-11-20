# Minesweeper v1.0.0

Java Swing 지뢰찾기의 첫 공개 버전입니다.

## 포함 기능

- 초급(9×9/10개), 중급(16×16/40개), 고급(30×16/99개)
- 첫 클릭과 주변 8칸 안전 보장, 빈 칸 자동 열기
- 좌클릭으로 칸 열기, 우클릭으로 깃발 표시·해제
- 열린 숫자 칸 클릭으로 주변 칸 한 번에 열기
- 타이머, 재시작, 승패 판정 및 난이도별 최고 기록 저장
- 별도 Java 설치가 필요 없는 Windows x64 ZIP

## 실행

`Minesweeper-windows-x64.zip`을 전부 압축 해제하고 `Minesweeper/Minesweeper.exe`를 실행합니다.
`app`과 `runtime` 폴더를 EXE와 함께 유지해야 합니다.
기록은 사용자 홈의 `.minesweeper/best-times.properties`에 저장됩니다.

## 지원 환경과 검증

- Windows x64, 그래픽 데스크톱 환경
- Java 17 런타임 포함; 빌드 및 포함 런타임 검증은 GitHub Actions에서 수행
- 게임 규칙·Swing 테스트, 패키지 기능·기록 복원·EXE 실행 검증
- 첨부한 `SHA256SUMS.txt`와 ZIP의 SHA-256을 비교할 수 있습니다.

## 알려진 제한

- Windows x64 전용 배포본이며 macOS/Linux 설치 패키지는 제공하지 않습니다.
- 앱 코드 서명이 적용되어 있지 않습니다.
- 사용자 지정 난이도와 키보드 깃발 조작은 후속 버전 작업입니다.
