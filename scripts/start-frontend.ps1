$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$workDir = Join-Path $repoRoot 'frontend'
$logDir = Join-Path $repoRoot 'logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
Set-Location $workDir
& npm.cmd run dev *> (Join-Path $logDir 'frontend-script.log')
