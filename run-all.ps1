$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendPath = Join-Path $root 'backend'
$frontendPath = Join-Path $root 'frontend'

Write-Host 'Starting backend in a new terminal...' -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    '-NoExit',
    '-Command',
    "Set-Location '$backendPath'; mvn spring-boot:run"
)

Write-Host 'Starting frontend in a new terminal...' -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    '-NoExit',
    '-Command',
    "Set-Location '$frontendPath'; npm run dev"
)

Write-Host 'Both services are starting.' -ForegroundColor Green
Write-Host 'Frontend: http://localhost:5173 (or 5174 if 5173 is busy)'
Write-Host 'Backend:  http://localhost:8080'
