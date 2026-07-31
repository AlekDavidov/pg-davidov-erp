@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

echo.
echo ========================================
echo   PG Davidov ERP - pokretanje
echo ========================================
echo.

where docker >nul 2>nul
if errorlevel 1 (
    echo GRESKA: Docker nije pronadjen.
    echo Instalirajte Docker Desktop i pokrenite ga.
    pause
    exit /b 1
)

docker info >nul 2>nul
if errorlevel 1 (
    echo GRESKA: Docker Desktop nije pokrenut.
    echo Pokrenite Docker Desktop pa probajte ponovo.
    pause
    exit /b 1
)

if not exist ".env" (
    if exist ".env.example" (
        echo Kreiram .env iz .env.example...
        copy ".env.example" ".env" >nul
        echo.
        echo Proverite vrednosti u .env fajlu pre produkcione upotrebe.
        echo.
    ) else (
        echo GRESKA: Ne postoje .env ni .env.example.
        pause
        exit /b 1
    )
)

echo Pokrecem i gradim kontejnere...
echo.

docker compose up -d --build

if errorlevel 1 (
    echo.
    echo GRESKA: Docker Compose nije uspesno pokrenuo aplikaciju.
    echo.
    docker compose ps
    pause
    exit /b 1
)

echo.
echo Cekam PostgreSQL i backend healthcheck...
echo.

set MAX_ATTEMPTS=60
set ATTEMPT=0

:WAIT_BACKEND
set /a ATTEMPT+=1

for /f "delims=" %%i in ('docker inspect --format="{{.State.Health.Status}}" pg-davidov-erp-backend-1 2^>nul') do (
    set BACKEND_STATUS=%%i
)

if "!BACKEND_STATUS!"=="healthy" goto BACKEND_READY

if !ATTEMPT! GEQ !MAX_ATTEMPTS! (
    echo.
    echo GRESKA: Backend nije postao healthy u predvidjenom roku.
    echo.
    docker compose ps
    echo.
    docker compose logs backend --tail=100
    pause
    exit /b 1
)

<nul set /p "=."
timeout /t 2 /nobreak >nul
goto WAIT_BACKEND

:BACKEND_READY
echo.
echo Backend je spreman.
echo.

echo Proveravam frontend...

set FRONTEND_ATTEMPTS=0

:WAIT_FRONTEND
set /a FRONTEND_ATTEMPTS+=1

curl --silent --fail http://localhost >nul 2>nul
if not errorlevel 1 goto FRONTEND_READY

if !FRONTEND_ATTEMPTS! GEQ 30 (
    echo.
    echo GRESKA: Frontend nije dostupan na http://localhost.
    echo.
    docker compose ps
    echo.
    docker compose logs frontend --tail=100
    pause
    exit /b 1
)

<nul set /p "=."
timeout /t 2 /nobreak >nul
goto WAIT_FRONTEND

:FRONTEND_READY
echo.
echo.
echo ========================================
echo   PG Davidov ERP je spreman
echo ========================================
echo.
echo Aplikacija: http://localhost
echo Backend:    http://localhost:8090
echo Swagger:    http://localhost:8090/swagger-ui/index.html
echo Health:     http://localhost:8090/actuator/health
echo.
echo PgAdmin se opciono pokrece komandom:
echo docker compose --profile tools up -d pgadmin
echo.

docker compose ps

echo.
echo Otvaram aplikaciju u browseru...
start "" "http://localhost"

echo.
pause