# TripGather 실행 가이드 🚀

TripGather 프로젝트의 백엔드와 프론트엔드 서버를 실행하는 방법입니다.

## 1. 백엔드 (Spring Boot) 실행

백엔드 폴더로 이동하여 Gradle 명령어를 사용합니다.

```bash
cd backend
./gradlew bootRun
```

- **API 주소**: [http://localhost:8080](http://localhost:8080)
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (API 테스트용)

## 2. 프론트엔드 (React + Vite) 실행

프론트엔드 폴더로 이동하여 npm 명령어를 사용합니다. (첫 실행 시 `npm install` 필요)

```bash
cd frontend
npm run dev
```

- **접속 주소**: [http://localhost:5173](http://localhost:5173)

---

> [!TIP]
> 백엔드 서버가 이미 실행 중이라면 `kill` 명령어로 기존 프로세스를 종료한 뒤 다시 실행하세요.
> 프론트엔드 포트가 충돌할 경우 자동으로 다른 포트(5174 등)가 할당될 수 있습니다.
