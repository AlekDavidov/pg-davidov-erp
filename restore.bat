@echo off
setlocal

cd /d "%~dp0"

echo.
echo ========================================
echo   PG Davidov ERP - restore baze
echo ========================================
echo.

if not exist "backups" (
    echo GRESKA: Folder backups ne postoji.
    pause
    exit /b 1
)

echo Dostupni backup fajlovi:
echo.

dir /b /o-d backups\*.sql

echo.
set /p BACKUP_FILE=Unesite naziv backup fajla:

if "%BACKUP_FILE%"=="" (
    echo GRESKA: Niste uneli naziv fajla.
    pause
    exit /b 1
)

if not exist "backups\%BACKUP_FILE%" (
    echo GRESKA: Fajl backups\%BACKUP_FILE% ne postoji.
    pause
    exit /b 1
)

echo.
echo UPOZORENJE:
echo Postojeci podaci u bazi bice zamenjeni.
echo.

set /p CONFIRM=Upisite YES za nastavak:

if /I not "%CONFIRM%"=="YES" (
    echo Restore je otkazan.
    pause
    exit /b 0
)

echo.
echo Proveravam PostgreSQL kontejner...

docker compose up -d postgres

if errorlevel 1 (
    echo GRESKA: PostgreSQL nije mogao da se pokrene.
    pause
    exit /b 1
)

echo.
echo Brisem i ponovo kreiram bazu...

docker compose exec -T postgres sh -c "dropdb -U $POSTGRES_USER --if-exists $POSTGRES_DB"
docker compose exec -T postgres sh -c "createdb -U $POSTGRES_USER $POSTGRES_DB"

if errorlevel 1 (
    echo GRESKA: Baza nije mogla da se kreira.
    pause
    exit /b 1
)

echo.
echo Vracam backup...

type "backups\%BACKUP_FILE%" | docker compose exec -T postgres sh -c "psql -U $POSTGRES_USER -d $POSTGRES_DB"

if errorlevel 1 (
    echo.
    echo GRESKA: Restore nije uspesno zavrsen.
    pause
    exit /b 1
)

echo.
echo Restore je uspesno zavrsen.
echo.

pause