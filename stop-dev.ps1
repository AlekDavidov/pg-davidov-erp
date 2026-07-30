$ErrorActionPreference = "Continue"

$projectRoot = $PSScriptRoot

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " PG Davidov ERP - Development shutdown" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

function Stop-ProcessListeningOnPort {
    param (
        [Parameter(Mandatory = $true)]
        [int]$Port,

        [Parameter(Mandatory = $true)]
        [string]$ServiceName
    )

    $connections = Get-NetTCPConnection `
        -LocalPort $Port `
        -State Listen `
        -ErrorAction SilentlyContinue

    if (-not $connections) {
        Write-Host "$ServiceName nije pokrenut na portu $Port." -ForegroundColor DarkGray
        return
    }

    $processIds = $connections |
        Select-Object -ExpandProperty OwningProcess |
        Sort-Object -Unique

    foreach ($processId in $processIds) {
        try {
            $process = Get-Process -Id $processId -ErrorAction Stop

            Write-Host "Zaustavljam $ServiceName - PID $processId ($($process.ProcessName))..." -ForegroundColor Yellow
            Stop-Process -Id $processId -Force -ErrorAction Stop

            Write-Host "$ServiceName je zaustavljen." -ForegroundColor Green
        }
        catch {
            Write-Host "Nije moguce zaustaviti PID $processId za $ServiceName." -ForegroundColor Red
        }
    }
}

Stop-ProcessListeningOnPort -Port 5173 -ServiceName "Frontend"
Stop-ProcessListeningOnPort -Port 8090 -ServiceName "Backend"

Write-Host ""
Write-Host "Zaustavljam PostgreSQL kontejner..." -ForegroundColor Yellow

Push-Location $projectRoot

try {
    docker compose stop postgres

    if ($LASTEXITCODE -eq 0) {
        Write-Host "PostgreSQL kontejner je zaustavljen." -ForegroundColor Green
    }
    else {
        Write-Host "PostgreSQL kontejner nije mogao da bude zaustavljen." -ForegroundColor Red
    }
}
finally {
    Pop-Location
}

Write-Host ""
Write-Host "Development okruzenje je zaustavljeno." -ForegroundColor Green
Write-Host "Docker Desktop nije ugasen." -ForegroundColor DarkGray