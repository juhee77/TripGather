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

## 3. 포트 충돌 해결 (프로세스 종료)

서버가 이미 실행 중이거나 포트가 점유되어 실행되지 않을 때, 아래 명령어로 해당 프로세스를 찾아 종료할 수 있습니다.

### 🍎 macOS 및 🐧 Linux
```bash
# 8080 포트를 사용 중인 프로세스 ID(PID) 확인
lsof -i :8080

# 프로세스 종료 (PID는 위 명령어로 확인 가능)
kill -9 <PID>
```

### 🪟 Windows (CMD/PowerShell)
```powershell
# 8080 포트를 사용 중인 프로세스 정보 확인
netstat -ano | findstr :8080

# 프로세스 종료 (가장 오른쪽의 PID 입력)
taskkill /F /PID <PID>
```

> [!TIP]
> 프론트엔드 포트(5173)가 충돌할 경우 Vite에서 자동으로 다른 포트(5174 등)를 추천해주기도 하지만, 위 명령어로 직접 정리하는 것이 가장 깔끔합니다.
