$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$jarPath = Join-Path $repoRoot 'backend\ruoyi-admin\target\ruoyi-admin.jar'
$logDir = Join-Path $repoRoot 'logs'
$tempDir = Join-Path $repoRoot '.tmp\backend-jar'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null
Get-ChildItem -LiteralPath $tempDir -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
& java "-Djava.io.tmpdir=$tempDir" -jar $jarPath *> (Join-Path $logDir 'backend-jar.log')
