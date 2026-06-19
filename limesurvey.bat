@echo off
REM LimeSurvey Docker Automation Script (Windows)
REM Manejo sencillo de Docker para desarrollo y testing

setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

REM Command handling
if "%1%"=="" (
    call :show_help
    exit /b 0
)

if /i "%1%"=="start" (
    call :check_docker
    call :start_services
) else if /i "%1%"=="stop" (
    call :check_docker
    call :stop_services
) else if /i "%1%"=="restart" (
    call :check_docker
    call :restart_services
) else if /i "%1%"=="clean" (
    call :check_docker
    call :clean_services
) else if /i "%1%"=="status" (
    call :check_docker
    call :show_status
) else if /i "%1%"=="logs" (
    call :check_docker
    if "%2%"=="" (
        docker-compose logs -f
    ) else (
        docker-compose logs -f %2%
    )
) else if /i "%1%"=="test" (
    call :run_tests
) else if /i "%1%"=="test:integration" (
    call :run_integration_tests
) else if /i "%1%"=="test:all" (
    call :run_tests
    echo.
    call :run_integration_tests
) else if /i "%1%"=="version" (
    call :change_version %2%
) else if /i "%1%"=="help" (
    call :show_help
) else (
    echo [ERROR] Unknown command: %1%
    call :show_help
    exit /b 1
)
exit /b 0

:check_docker
    docker --version >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Docker is not installed or not in PATH
        exit /b 1
    )
    docker-compose --version >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] docker-compose is not installed or not in PATH
        exit /b 1
    )
    echo [OK] Docker and docker-compose are installed
    exit /b 0

:start_services
    echo [INFO] Starting LimeSurvey stack...
    docker-compose up -d
    
    echo [INFO] Waiting for services to be healthy...
    timeout /t 10 /nobreak
    
    echo [OK] LimeSurvey is accessible at http://localhost
    echo   Admin User: admin
    echo   Admin Password: admin123
    exit /b 0

:stop_services
    echo [INFO] Stopping LimeSurvey stack...
    docker-compose down
    echo [OK] Stack stopped
    exit /b 0

:restart_services
    echo [INFO] Restarting LimeSurvey stack...
    docker-compose restart
    timeout /t 5 /nobreak
    echo [OK] Stack restarted
    exit /b 0

:clean_services
    echo [WARNING] This will remove all containers and volumes!
    set /p confirm="Are you sure? (y/N): "
    if /i "%confirm%"=="y" (
        echo [INFO] Cleaning up...
        docker-compose down -v
        echo [OK] Clean complete
    ) else (
        echo [INFO] Cleanup cancelled
    )
    exit /b 0

:show_status
    echo [INFO] Docker Services Status:
    docker-compose ps
    echo.
    docker ps | find "limesurvey" >nul
    if errorlevel 1 (
        echo [WARNING] LimeSurvey is not running
    ) else (
        echo [OK] LimeSurvey is running
    )
    exit /b 0

:run_tests
    echo [INFO] Running unit tests...
    mvn -DskipTests=false -Dtest=*Test test
    echo [OK] Unit tests completed
    exit /b 0

:run_integration_tests
    docker ps | find "limesurvey-app" >nul
    if errorlevel 1 (
        echo [ERROR] LimeSurvey is not running. Please run: limesurvey.bat start
        exit /b 1
    )
    
    echo [INFO] Running integration tests...
    set LIMESURVEY_TEST_URL=http://localhost
    set LIMESURVEY_TEST_USER=admin
    set LIMESURVEY_TEST_PASSWORD=admin123
    mvn verify
    echo [OK] Integration tests completed
    exit /b 0

:change_version
    if "%1%"=="" (
        echo [ERROR] Please specify version: 6 or 7
        exit /b 1
    )
    
    if "%1%"=="6" (
        echo [INFO] Changing LimeSurvey version to 6...
        powershell -Command "(gc docker-compose.yml) -replace 'martialblog/limesurvey:[0-9]*-apache', 'martialblog/limesurvey:6-apache' | Out-File -encoding UTF8 docker-compose.yml"
        echo [OK] Version updated to 6
    ) else if "%1%"=="7" (
        echo [INFO] Changing LimeSurvey version to 7...
        powershell -Command "(gc docker-compose.yml) -replace 'martialblog/limesurvey:[0-9]*-apache', 'martialblog/limesurvey:7-apache' | Out-File -encoding UTF8 docker-compose.yml"
        echo [OK] Version updated to 7
    ) else (
        echo [ERROR] Invalid version. Use 6 or 7
        exit /b 1
    )
    echo [NOTE] Stop and start the stack to apply changes:
    echo   limesurvey.bat stop
    echo   limesurvey.bat start
    exit /b 0

:show_help
    echo.
    echo LimeSurvey Docker Management
    echo.
    echo Usage: limesurvey.bat ^<command^>
    echo.
    echo Commands:
    echo   start              Start LimeSurvey stack
    echo   stop               Stop LimeSurvey stack
    echo   restart            Restart services
    echo   clean              Remove all containers and volumes
    echo   status             Show services status
    echo   logs [service]     Show logs
    echo.
    echo   test               Run unit tests
    echo   test:integration   Run integration tests (requires running LimeSurvey^)
    echo   test:all           Run all tests
    echo.
    echo   version ^<6^|7^>     Change LimeSurvey version
    echo   help               Show this message
    echo.
    echo Examples:
    echo   limesurvey.bat start
    echo   limesurvey.bat logs limesurvey-app
    echo   limesurvey.bat test
    echo   limesurvey.bat version 7
    echo.
    exit /b 0
