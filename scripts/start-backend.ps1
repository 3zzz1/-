$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$workDir = Join-Path $repoRoot 'backend\ruoyi-admin'
$mvn = 'D:\Program Files\apache-maven-3.9.9\bin\mvn.cmd'
$logDir = Join-Path $repoRoot 'logs'
$tempDir = Join-Path $repoRoot '.tmp\backend'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null
Get-ChildItem -LiteralPath $tempDir -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
Set-Location $workDir
& $mvn "-Dspring-boot.run.jvmArguments=-Djava.io.tmpdir=$tempDir" spring-boot:run *> (Join-Path $logDir 'backend-script.log')
