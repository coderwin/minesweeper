param()

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$application = Join-Path $projectRoot 'target\windows\Minesweeper'
$runtimeJava = Join-Path $application 'runtime\bin\java.exe'
$applicationJar = Join-Path $application 'app\minesweeper-1.0-SNAPSHOT.jar'
$classes = Join-Path $projectRoot 'target\package-smoke-classes'
$recordsDirectory = Join-Path $projectRoot ('target\package-smoke-' + [guid]::NewGuid().ToString('N'))
$jdkCompiler = Join-Path $env:JAVA_HOME 'bin\javac.exe'
if (-not (Test-Path -LiteralPath $runtimeJava)) { throw 'Build the Windows package first.' }
New-Item -ItemType Directory -Path $classes -Force | Out-Null
& $jdkCompiler -encoding UTF-8 --release 17 -cp $applicationJar -d $classes (Join-Path $PSScriptRoot 'PackageSmoke.java')
if ($LASTEXITCODE -ne 0) { throw 'Smoke test compilation failed.' }

$savedJavaHome = $env:JAVA_HOME
$savedPath = $env:Path
$appProcess = $null
$gameProcess = $null
try {
    $env:JAVA_HOME = ''
    $env:Path = "$env:SystemRoot\System32;$env:SystemRoot"
    # Only the runtime shipped with the app is available to these commands.
    & $runtimeJava "-Dminesweeper.dataDir=$recordsDirectory" -cp "$classes;$applicationJar" com.coderwin.minesweeper.PackageSmoke
    if ($LASTEXITCODE -ne 0) { throw 'Packaged runtime functional checks failed.' }
    & $runtimeJava "-Dminesweeper.dataDir=$recordsDirectory" -cp "$classes;$applicationJar" com.coderwin.minesweeper.PackageSmoke restore
    if ($LASTEXITCODE -ne 0) { throw 'Cross-process record restoration failed.' }
    $appProcess = Start-Process -FilePath (Join-Path $application 'Minesweeper.exe') -WindowStyle Hidden -PassThru
    $deadline = [DateTime]::UtcNow.AddSeconds(15)
    do {
        Start-Sleep -Milliseconds 250
        $appProcess.Refresh()
        # Windows jpackage starts the JVM in a child launcher process.
        $child = Get-CimInstance Win32_Process -Filter "ParentProcessId=$($appProcess.Id) AND Name='Minesweeper.exe'" | Select-Object -First 1
        if ($child) { $gameProcess = Get-Process -Id $child.ProcessId -ErrorAction SilentlyContinue }
        if (-not $gameProcess -and $appProcess.MainWindowHandle -ne 0) { $gameProcess = $appProcess }
        if ($gameProcess) { $gameProcess.Refresh() }
    } while (-not $appProcess.HasExited -and (-not $gameProcess -or $gameProcess.MainWindowHandle -eq 0) -and [DateTime]::UtcNow -lt $deadline)
    if (-not $gameProcess -or $gameProcess.HasExited -or $gameProcess.MainWindowHandle -eq 0) { throw 'Packaged launcher did not create a game window.' }
    if ($gameProcess.Path -ne (Join-Path $application 'Minesweeper.exe')) { throw 'Unexpected child process path.' }
    $jvm = $gameProcess.Modules | Where-Object { $_.ModuleName -eq 'jvm.dll' } | Select-Object -First 1
    if (-not $jvm -or -not $jvm.FileName.StartsWith($application + '\', [StringComparison]::OrdinalIgnoreCase)) {
        throw 'Launcher must load the JVM shipped in the app image.'
    }
    Write-Output "Launcher uses bundled JVM: $($jvm.FileName)"
} finally {
    if ($gameProcess -and -not $gameProcess.HasExited) { Stop-Process -Id $gameProcess.Id }
    if ($appProcess) { $appProcess.Refresh() }
    if ($appProcess -and -not $appProcess.HasExited) { Stop-Process -Id $appProcess.Id }
    $env:JAVA_HOME = $savedJavaHome
    $env:Path = $savedPath
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = Get-ChildItem -LiteralPath (Join-Path $projectRoot 'target') -Filter 'Minesweeper-windows-*.zip' | Select-Object -First 1
$zip = [IO.Compression.ZipFile]::OpenRead($archive.FullName)
try {
    $unexpected = $zip.Entries | Where-Object { $_.FullName.Replace('\','/') -match '(^|/)(src|\.git|\.tools|target|scripts)/|\.(java|iml)$' }
    if ($unexpected) { throw 'Archive contains development files.' }
    if (-not ($zip.Entries | Where-Object { $_.FullName.Replace('\','/') -eq 'Minesweeper/Minesweeper.exe' })) { throw 'ZIP is missing the launcher.' }
} finally { $zip.Dispose() }
Write-Output 'Windows package verification passed.'
