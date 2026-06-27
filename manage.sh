#!/bin/bash

# TripGather 통합 관리 스크립트 (로컬 프로세스 및 Docker 양방향 지원)
# 사용법: 
#   - 로컬 구동: ./manage.sh [up|stop|restart|logs|clean]
#   - Docker 구동: ./manage.sh [up|stop|restart] docker

COLOR_GREEN='\033[0;32m'
COLOR_BLUE='\033[0;34m'
COLOR_RED='\033[0;31m'
NC='\033[0m' # No Color

function print_msg() {
    echo -e "${COLOR_BLUE}[TripGather]${NC} $1"
}

# Docker 모드 인자 체크
USE_DOCKER=false
if [ "$2" == "docker" ] || [ "$2" == "--docker" ] || [ "$1" == "logs" ] && [ "$2" == "docker" ]; then
    USE_DOCKER=true
fi

function stop_service() {
    local pid_file=$1
    local service_name=$2
    
    # 1. PID 파일 기준 중지
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null; then
            print_msg "$service_name (PID: $pid) 중지 중..."
            kill $pid 2>/dev/null
            sleep 1
            if ps -p $pid > /dev/null; then
                kill -9 $pid 2>/dev/null
            fi
        fi
        rm -f "$pid_file"
    fi

    # 2. 포트 점유 프로세스 강제 정리 (OS 판별 적용)
    if [ "$service_name" == "백엔드" ]; then
        if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
            # Windows Git Bash
            local win_pid=$(netstat -ano | grep :8080 | awk '{print $5}' | head -n 1)
            if [ -n "$win_pid" ] && [ "$win_pid" != "0" ]; then
                print_msg "Windows 포트 8080 점유 중인 프로세스 ($win_pid) 종료 중..."
                taskkill //F //PID "$win_pid" 2>/dev/null
            fi
        else
            # macOS / Linux
            local port_pid=$(lsof -t -i:8080)
            if [ -n "$port_pid" ]; then
                print_msg "포트 8080 점유 중인 프로세스 ($port_pid) 종료 중..."
                kill -9 $port_pid 2>/dev/null
            fi
        fi
    elif [ "$service_name" == "프론트엔드" ]; then
        if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
            # Windows Git Bash
            local win_pid=$(netstat -ano | grep :5173 | awk '{print $5}' | head -n 1)
            if [ -n "$win_pid" ] && [ "$win_pid" != "0" ]; then
                print_msg "Windows 포트 5173 점유 중인 프로세스 ($win_pid) 종료 중..."
                taskkill //F //PID "$win_pid" 2>/dev/null
            fi
        else
            # macOS / Linux
            local port_pid=$(lsof -t -i:5173)
            if [ -n "$port_pid" ]; then
                print_msg "포트 5173 점유 중인 프로세스 ($port_pid) 종료 중..."
                kill -9 $port_pid 2>/dev/null
            fi
        fi
    fi
}

case "$1" in
    up)
        if [ "$USE_DOCKER" = true ]; then
            print_msg "Docker를 통해 서비스 구동 시작..."
            print_msg "Docker 이미지 빌드용 백엔드 JAR 생성 중..."
            cd backend && ./gradlew bootJar && cd ..
            docker-compose down
            docker-compose up -d --build
            print_msg "${COLOR_GREEN}모든 서비스가 Docker 컨테이너에서 백그라운드로 실행 중입니다.${NC}"
            print_msg "로그 스트리밍: docker-compose logs -f"
        else
            print_msg "로컬 프로세스를 통해 서비스 구동 시작..."
            
            # 1. 백엔드 구동 (Port: 8080)
            stop_service "backend.pid" "백엔드"
            print_msg "백엔드 구동 중... (./gradlew bootRun)"
            cd backend
            ./gradlew bootRun > ../backend_startup.log 2>&1 &
            echo $! > ../backend.pid
            cd ..
            
            # 2. 프론트엔드 구동 (Port: 5173)
            stop_service "frontend.pid" "프론트엔드"
            print_msg "프론트엔드 구동 중... (npm run dev)"
            cd frontend
            npm run dev > ../frontend_startup.log 2>&1 &
            echo $! > ../frontend.pid
            cd ..

            # 3. 스토리북 구동 (Port: 6006)
            stop_service "storybook.pid" "스토리북"
            print_msg "스토리북 구동 중... (npm run storybook)"
            cd frontend
            npm run storybook -- --ci > ../storybook_startup.log 2>&1 &
            echo $! > ../storybook.pid
            cd ..

            print_msg "${COLOR_GREEN}모든 서비스(백엔드:8080, 프론트엔드:5173, 스토리북:6006)가 백그라운드에서 구동되었습니다.${NC}"
            print_msg "로그 확인: tail -f backend_startup.log 또는 frontend_startup.log"
        fi
        ;;
    stop)
        print_msg "서비스 중지 중..."
        stop_service "backend.pid" "백엔드"
        stop_service "frontend.pid" "프론트엔드"
        stop_service "storybook.pid" "스토리북"
        
        if command -v docker-compose &> /dev/null && [ -f "docker-compose.yml" ]; then
            docker-compose down 2>/dev/null
        fi
        print_msg "${COLOR_RED}모든 서비스가 중지되었습니다.${NC}"
        ;;
    restart)
        if [ "$USE_DOCKER" = true ]; then
            $0 stop
            sleep 2
            $0 up docker
        else
            $0 stop
            sleep 2
            $0 up
        fi
        ;;
    logs)
        if [ "$USE_DOCKER" = true ]; then
            docker-compose logs -f
        else
            print_msg "로그 스트리밍 시작 (종료하려면 Ctrl+C)..."
            tail -f backend_startup.log frontend_startup.log storybook_startup.log 2>/dev/null
        fi
        ;;
    clean)
        print_msg "임시 파일 및 빌드 산출물 정리 중..."
        $0 stop
        rm -f *.pid *.log
        cd backend && ./gradlew clean && cd ..
        if command -v docker-compose &> /dev/null && [ -f "docker-compose.yml" ]; then
            docker-compose down --rmi all --volumes --remove-orphans 2>/dev/null
        fi
        print_msg "${COLOR_GREEN}정리가 완료되었습니다.${NC}"
        ;;
    *)
        echo "사용법:"
        echo "  로컬 구동:   $0 {up|stop|restart|logs|clean}"
        echo "  Docker 구동: $0 {up|stop|restart|logs} docker"
        exit 1
        ;;
esac
