@echo off
setlocal

set ROOT=%~dp0
set BACKEND=%ROOT%backend
set FRONTEND=%ROOT%frontend

echo Starting backend in a new terminal...
start "CTU Backend" cmd /k "cd /d "%BACKEND%" && mvn spring-boot:run"

echo Starting frontend in a new terminal...
start "CTU Frontend" cmd /k "cd /d "%FRONTEND%" && npm run dev"

echo Both services are starting.
echo Frontend: http://localhost:5173 (or 5174 if busy)
echo Backend:  http://localhost:8080

endlocal
