@echo off
chcp 65001 >nul
rem TripGather Windows CMD/PowerShell 관리 배치 파일

set CMD=%1
set TARGET=%2

if "%CMD%"=="up" (
    if "%TARGET%"=="docker" (
        echo [TripGather] Docker 구동을 위해 backend 빌드 중...
        cd backend
        call gradlew.bat bootJar
        cd ..
        docker-compose down
        docker-compose up -d --build
        echo [TripGather] 모든 서비스가 Docker 상에서 구동 중입니다.
        goto end
    ) else if "%TARGET%"=="--docker" (
        echo [TripGather] Docker 구동을 위해 backend 빌드 중...
        cd backend
        call gradlew.bat bootJar
        cd ..
        docker-compose down
        docker-compose up -d --build
        echo [TripGather] 모든 서비스가 Docker 상에서 구동 중입니다.
        goto end
    ) else (
        echo [TripGather] 로컬 백그라운드 프로세스로 구동 중...
        
        rem 포트 8080, 5173, 6006 정리
        for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
            if not "%%a"=="0" (
                taskkill /F /PID %%a 2>nul
            )
        )
        for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5173') do (
            if not "%%a"=="0" (
                taskkill /F /PID %%a 2>nul
            )
        )
        for /f "tokens=5" %%a in ('netstat -aon ^| findstr :6006') do (
            if not "%%a"=="0" (
                taskkill /F /PID %%a 2>nul
            )
        )

        echo [TripGather] 백엔드 구동 시작...
        cd backend
        start /B gradlew.bat bootRun > ..\backend_startup.log 2>&1
        cd ..

        echo [TripGather] 프론트엔드 구동 시작...
        cd frontend
        start /B npm run dev > ..\frontend_startup.log 2>&1
        cd ..

        echo [TripGather] 스토리북 구동 시작...
        cd frontend
        start /B npm run storybook -- --ci > ..\storybook_startup.log 2>&1
        cd ..

        echo [TripGather] 모든 서비스가 구동되었습니다. (백엔드: 8080, 프론트엔드: 5173, 스토리북: 6006)
        goto end
    )
)

if "%CMD%"=="stop" (
    echo [TripGather] 서비스 종료 중...
    
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
        if not "%%a"=="0" (
            taskkill /F /PID %%a 2>nul
        )
    )
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5173') do (
        if not "%%a"=="0" (
            taskkill /F /PID %%a 2>nul
        )
    )
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :6006') do (
        if not "%%a"=="0" (
            taskkill /F /PID %%a 2>nul
        )
    )
    
    docker-compose down 2>nul
    echo [TripGather] 모든 서비스가 종료되었습니다.
    goto end
)

if "%CMD%"=="clean" (
    echo [TripGather] 임시 파일 및 빌드 산출물 정리 중...
    
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
        if not "%%a"=="0" (
            taskkill /F /PID %%a 2>nul
        )
    )
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5173') do (
        if not "%%a"=="0" (
            taskkill /F /PID %%a 2>nul
        )
    )
    
    del /Q /F *.pid *.log 2>nul
    cd backend
    call gradlew.bat clean
    cd ..
    docker-compose down --rmi all --volumes --remove-orphans 2>nul
    echo [TripGather] 정리가 완료되었습니다.
    goto end
)

echo 사용법:
echo   로컬 구동:   manage.bat {up^|stop^|clean}
echo   Docker 구동: manage.bat {up^|stop} docker

:end
