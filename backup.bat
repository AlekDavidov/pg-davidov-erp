@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

if not exist "backups" mkdir "backups"

for /f "tokens=1-4 delims=/.- " %%a in ("%date%") do (
    set DATE_PART=%%d-%%c-%%b
)

for /f "tokens=1-3 delims=:, " %%a in ("%time%") do (
    set TIME_PART=%%a-%%b-%%c
)

set TIME_PART=%TIME_PART: =0%
set BACKUP_FILE=backups\pg_davidov_erp_%DATE_PART%_%TIME_PART%.sql

echo.
echo Kreiram backup baze...
echo %BACKUP_FILE%
echo.

docker compose exec -T postgres sh -c "pg_dump -U $POSTGRES_USER -d $POSTGRES_DB" > "%BACKUP_FILE%"

if errorlevel 1 (
    echo.
    echo GRESKA: Backup nije uspesno napravljen.
    if exist "%BACKUP_FILE%" del "%BACKUP_FILE%"
    pause
    exit /b 1
)

echo.
echo Backup je uspesno napravljen:
echo %BACKUP_FILE%
echo.

pause