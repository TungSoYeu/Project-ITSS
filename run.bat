@echo off
chcp 65001 > nul
echo ===================================================
echo Khởi động OOAS System...
echo ===================================================
echo.

:: 1. Kiểm tra Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [LỖI] Không tìm thấy Java trên máy tính này. Vui lòng cài đặt JDK 21 để tiếp tục!
    pause
    exit /b
)

:: 2. Khởi động Docker
echo [1/3] Đang khởi động Backend & Database (Docker)...
docker-compose up -d
if %ERRORLEVEL% NEQ 0 (
    echo [LỖI] Không thể chạy Docker. Vui lòng kiểm tra xem Docker Desktop đã được bật chưa!
    pause
    exit /b
)

:: 3. Chờ Backend sẵn sàng
echo.
echo [2/3] Đang đợi Backend khởi động hoàn tất (thường mất khoảng 10-20 giây)...
:wait_for_backend
curl -s http://localhost:3000/api/health | find "UP" > nul
if %ERRORLEVEL% NEQ 0 (
    timeout /t 2 > nul
    goto wait_for_backend
)
echo =^> Backend đã sẵn sàng!

:: 4. Khởi động Frontend
echo.
echo [3/3] Đang bật giao diện Frontend (JavaFX Desktop)...
cd frontend
call ..\mvnw.cmd javafx:run
cd ..

echo.
echo Ứng dụng đã đóng.
pause
