$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$backendPath = Join-Path $projectRoot "backend"
$frontendPath = Join-Path $projectRoot "frontend"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " PG Davidov ERP - Development startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

function Test-CommandExists {
    param (
        [Parameter(Mandatory = $true)]
        [string]$Command
    )

    return [bool](Get-Command $Command -ErrorAction SilentlyContinue)
}

function Test-PortListening {
    param (
        [Parameter(Mandatory = $true)]
        [int]$Port
    )

    return [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

if (-not (Test-CommandExists "docker")) {
    Write-Host "Docker nije dostupan u PATH-u." -ForegroundColor Red
    Write-Host "Pokreni Docker Desktop i ponovo izvrsi skriptu." -ForegroundColor Yellow
    exit 1
}

try {
    docker info *> $null
}
catch {
    Write-Host "Docker Desktop nije pokrenut." -ForegroundColor Red
    Write-Host "Pokreni Docker Desktop, sacekaj da se ucita i ponovo izvrsi skriptu." -ForegroundColor Yellow
    exit 1
}

Write-Host "[1/3] Pokretanje PostgreSQL baze..." -ForegroundColor Yellow

Push-Location $projectRoot

try {
    docker compose up -d postgres

    if ($LASTEXITCODE -ne 0) {
        throw "Docker nije uspeo da pokrene PostgreSQL."
    }
}
finally {
    Pop-Location
}

Write-Host "PostgreSQL je pokrenut na portu 5432." -ForegroundColor Green
Write-Host ""

if (-not (Test-PortListening -Port 8090)) {
    Write-Host "[2/3] Backend nije pokrenut na portu 8090." -ForegroundColor Yellow

    $mavenWrapper = Join-Path $backendPath "mvnw.cmd"

    if (Test-Path $mavenWrapper) {
        Write-Host "Pokrecem backend preko Maven Wrapper-a..." -ForegroundColor Cyan

        Start-Process powershell.exe -ArgumentList @(
            "-NoExit",
            "-Command",
            "Set-Location '$backendPath'; .\mvnw.cmd spring-boot:run"
        )
    }
    elseif (Test-CommandExists "mvn") {
        Write-Host "Pokrecem backend preko sistemskog Maven-a..." -ForegroundColor Cyan

        Start-Process powershell.exe -ArgumentList @(
            "-NoExit",
            "-Command",
            "Set-Location '$backendPath'; mvn spring-boot:run"
        )
    }
    else {
        Write-Host "Maven nije dostupan iz terminala." -ForegroundColor Yellow
        Write-Host "Pokreni PgDavidovErpApplication iz IntelliJ-a." -ForegroundColor Yellow
    }
}
else {
    Write-Host "[2/3] Backend vec radi na portu 8090." -ForegroundColor Green
}

Write-Host ""

if (-not (Test-CommandExists "npm")) {
    Write-Host "npm nije dostupan u PATH-u." -ForegroundColor Red
    Write-Host "Proveri da li je C:\Program Files\nodejs dodat u PATH." -ForegroundColor Yellow
    exit 1
}

if (-not (Test-PortListening -Port 5173)) {
    Write-Host "[3/3] Pokretanje frontenda..." -ForegroundColor Yellow

    Start-Process powershell.exe -ArgumentList @(
        "-NoExit",
        "-Command",
        "Set-Location '$frontendPath'; npm install; if (`$LASTEXITCODE -eq 0) { npm run dev }"
    )
}
else {
    Write-Host "[3/3] Frontend vec radi na portu 5173." -ForegroundColor Green
}

Write-Host ""
Write-Host "Development servisi:" -ForegroundColor Cyan
Write-Host "PostgreSQL: localhost:5432"
Write-Host "Backend:    http://localhost:8090"
Write-Host "Frontend:   http://localhost:5173"
Write-Host "Swagger:    http://localhost:8090/swagger-ui.html"
Write-Host ""
Write-Host "Development okruzenje je pokrenuto." -ForegroundColor Green