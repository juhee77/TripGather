#!/bin/bash

# TripGather 통합 관리 스크립트 (로컬 백그라운드 프로세스 제어 방식)
# 사용법: ./manage.sh [up|stop|restart|logs|clean]

COLOR_GREEN='\033[0;32m'
COLOR_BLUE='\033[0;34m'
COLOR_RED='\033[0;31m'
NC='\033[0m' # No Color

function print_msg() {
    echo -e "${COLOR_BLUE}[TripGather]${NC} $1"
}

function stop_service() {
    local pid_file=$1
    local service_name=$2
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null; then
            print_msg "$service_name (PID: $pid) 중지 중..."
            kill $pid 2>/dev/null
            sleep 1
            # 강제 종료 필요시
            if ps -p $pid > /dev/null; then
                kill -9 $pid 2>/dev/null
            fi
        fi
        rm -f "$pid_file"
    else
        # 포트 점유 등으로 프로세스 찾아서 정리
        if [ "$service_name" == "백엔드" ]; then
            local port_pid=$(lsof -t -i:8080)
            if [ -n "$port_pid" ]; then
                print_msg "포트 8080 점유 중인 프로세스 ($port_pid) 종료 중..."
                kill -9 $port_pid 2>/dev/null
            fi
        fi
    fi
}

case "$1" in
    up)
        print_msg "서비스 구동 시작..."
        
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
        ;;
    stop)
        print_msg "서비스 중지 중..."
        stop_service "backend.pid" "백엔드"
        stop_service "frontend.pid" "프론트엔드"
        stop_service "storybook.pid" "스토리북"
        print_msg "${COLOR_RED}모든 서비스가 중지되었습니다.${NC}"
        ;;
    restart)
        $0 stop
        sleep 2
        $0 up
        ;;
    logs)
        print_msg "로그 스트리밍 시작 (종료하려면 Ctrl+C)..."
        tail -f backend_startup.log frontend_startup.log storybook_startup.log 2>/dev/null
        ;;
    clean)
        print_msg "임시 파일 및 빌드 산출물 정리 중..."
        $0 stop
        rm -f *.pid *.log
        cd backend && ./gradlew clean && cd ..
        print_msg "${COLOR_GREEN}정리가 완료되었습니다.${NC}"
        ;;
    *)
        echo "사용법: $0 {up|stop|restart|logs|clean}"
        exit 1
        ;;
esac
