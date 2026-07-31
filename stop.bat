@echo off
setlocal

cd /d "%~dp0"

echo.
echo ========================================
echo   PG Davidov ERP - zaustavljanje
echo ========================================
echo.

docker compose down

if errorlevel 1 (
    echo.
    echo GRESKA: Kontejneri nisu uspesno zaustavljeni.
    pause
    exit /b 1
)

echo.
echo Aplikacija je zaustavljena.
echo Podaci u bazi i dokumenti su sacuvani.
echo.

pause