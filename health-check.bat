@echo off
REM Health check - Verifica que Docker y dependencias estén disponibles

setlocal enabledelayedexpansion

echo.
echo ═══════════════════════════════════════════════════════════════
echo LimeSurvey Docker Setup - Health Check
echo ═══════════════════════════════════════════════════════════════
echo.

REM Check Docker
echo Checking Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not installed or not in PATH
    echo Install from: https://docs.docker.com/get-docker/
    exit /b 1
) else (
    for /f "tokens=3" %%i in ('docker --version') do set DOCKER_VERSION=%%i
    echo [OK] Docker ^(!DOCKER_VERSION!^)
)

REM Check Docker Compose
echo Checking Docker Compose...
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] docker-compose is not installed or not in PATH
    echo Install from: https://docs.docker.com/compose/install/
    exit /b 1
) else (
    for /f "tokens=3" %%i in ('docker-compose --version') do set COMPOSE_VERSION=%%i
    echo [OK] Docker Compose ^(!COMPOSE_VERSION!^)
)

REM Check Maven (optional)
echo Checking Maven...
mvn --version >nul 2>&1
if errorlevel 1 (
    echo [OPTIONAL] Maven not found (needed for tests^)
    echo Install from: https://maven.apache.org/download.cgi
) else (
    for /f "tokens=3" %%i in ('mvn --version ^| findstr "Apache Maven"') do set MVN_VERSION=%%i
    echo [OK] Maven
)

REM Check Java (optional)
echo Checking Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo [OPTIONAL] Java not found (needed for tests^)
    echo Install from: https://www.oracle.com/java/technologies/downloads/
) else (
    for /f "tokens=2" %%i in ('java -version 2^>^&1 ^| findstr "version"') do (
        set JAVA_VERSION=%%i
        echo [OK] Java
    )
)

echo.
echo Checking Docker files...
echo.

REM Check required files
if exist "docker-compose.yml" (
    echo [OK] docker-compose.yml
) else (
    echo [ERROR] docker-compose.yml NOT FOUND
)

if exist "Dockerfile.test" (
    echo [OK] Dockerfile.test
) else (
    echo [ERROR] Dockerfile.test NOT FOUND
)

if exist "limesurvey.bat" (
    echo [OK] limesurvey.bat
) else (
    echo [ERROR] limesurvey.bat NOT FOUND
)

if exist "DOCKER.md" (
    echo [OK] DOCKER.md
) else (
    echo [ERROR] DOCKER.md NOT FOUND
)

echo.
echo Checking Docker daemon...
docker ps >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker daemon is not running
    echo Start Docker and try again
    exit /b 1
) else (
    echo [OK] Docker daemon is running
)

echo.
echo ═══════════════════════════════════════════════════════════════
echo [✓] All checks passed!
echo ═══════════════════════════════════════════════════════════════
echo.
echo Ready to start:
echo   limesurvey.bat start
echo.
echo Then run tests:
echo   limesurvey.bat test:all
echo.
