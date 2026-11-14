param()

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
if ($env:OS -ne 'Windows_NT') { throw 'Build this package on Windows.' }
if (-not $env:JAVA_HOME) { throw 'Set JAVA_HOME to a JDK 17 or newer installation.' }
$jpackageTool = Join-Path $env:JAVA_HOME 'bin\jpackage.exe'
if (-not (Test-Path -LiteralPath $jpackageTool)) { throw 'jpackage.exe was not found in JAVA_HOME.' }

Push-Location $projectRoot
try {
    & .\mvnw.cmd --batch-mode --no-transfer-progress clean verify '-Djava.awt.headless=true' '-Ddesktop.tests=false'
    if ($LASTEXITCODE -ne 0) { throw 'Maven verification failed.' }
    $inputDirectory = Join-Path $projectRoot 'target\package-input'
    $outputDirectory = Join-Path $projectRoot 'target\windows'
    New-Item -ItemType Directory -Path $inputDirectory, $outputDirectory -Force | Out-Null
    Copy-Item -LiteralPath (Join-Path $projectRoot 'target\minesweeper-1.0-SNAPSHOT.jar') -Destination $inputDirectory
    & $jpackageTool --type app-image --name Minesweeper --app-version 1.0.0 `
        --input $inputDirectory --main-jar minesweeper-1.0-SNAPSHOT.jar `
        --main-class com.coderwin.minesweeper.MinesweeperApp --dest $outputDirectory `
        --add-modules java.desktop --jlink-options '--strip-debug --no-man-pages --no-header-files' `
        --java-options '-Dfile.encoding=UTF-8' --vendor coderwin
    if ($LASTEXITCODE -ne 0) { throw 'jpackage failed.' }
    $applicationDirectory = Join-Path $outputDirectory 'Minesweeper'
    Copy-Item -LiteralPath (Join-Path $projectRoot 'README.md') -Destination $applicationDirectory
    $architecture = if ([System.Runtime.InteropServices.RuntimeInformation]::OSArchitecture -eq 'X64') { 'x64' } else { 'arm64' }
    $archive = Join-Path $projectRoot "target\Minesweeper-windows-$architecture.zip"
    Compress-Archive -LiteralPath $applicationDirectory -DestinationPath $archive -Force
    Write-Output "Application: $applicationDirectory\Minesweeper.exe"
    Write-Output "ZIP: $archive"
} finally {
    Pop-Location
}
