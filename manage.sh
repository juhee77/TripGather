#!/bin/bash

# TripGather 통합 관리 스크립트
# 사용법: ./manage.sh [up|stop|restart|logs|clean]

COLOR_GREEN='\033[0;32m'
COLOR_BLUE='\033[0;34m'
COLOR_RED='\033[0;31m'
NC='\033[0m' # No Color

function print_msg() {
    echo -e "${COLOR_BLUE}[TripGather]${NC} $1"
}

function build_backend() {
    print_msg "백엔드 빌드 중... (Gradle)"
    cd backend && ./gradlew bootJar && cd ..
}

function build_frontend() {
    print_msg "프론트엔드 종속성 설치 및 빌드 중... (npm)"
    cd frontend && npm install && cd ..
}

case "$1" in
    up)
        print_msg "서비스 구동 시작..."
        build_backend
        docker-compose down
        docker-compose up -d --build
        print_msg "${COLOR_GREEN}모든 서비스가 백그라운드에서 실행 중입니다.${NC}"
        ;;
    stop)
        print_msg "서비스 중지 중..."
        docker-compose down
        print_msg "${COLOR_RED}모든 서비스가 중지되었습니다.${NC}"
        ;;
    restart)
        print_msg "서비스 재시작 중..."
        build_backend
        docker-compose down
        docker-compose up -d --build
        print_msg "${COLOR_GREEN}서비스가 재시작되었습니다.${NC}"
        ;;
    logs)
        docker-compose logs -f
        ;;
    clean)
        print_msg "시스템 정리 중 (컨테이너/네트워크/미사용 이미지)..."
        docker-compose down --rmi all --volumes --remove-orphans
        print_msg "${COLOR_RED}정리가 완료되었습니다.${NC}"
        ;;
    *)
        echo "사용법: $0 {up|stop|restart|logs|clean}"
        exit 1
        ;;
esac
