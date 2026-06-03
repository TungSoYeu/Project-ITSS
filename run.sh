#!/bin/bash
echo "==================================================="
echo "Khởi động OOAS System..."
echo "==================================================="
echo ""

# 1. Kiểm tra Java
if ! command -v java &> /dev/null
then
    echo "[LỖI] Không tìm thấy Java trên máy tính này. Vui lòng cài đặt JDK 21!"
    exit 1
fi

# 2. Khởi động Docker
echo "[1/3] Đang khởi động Backend & Database (Docker)..."
docker-compose up -d
if [ $? -ne 0 ]; then
    echo "[LỖI] Không thể chạy Docker. Vui lòng đảm bảo Docker đang bật!"
    exit 1
fi

# 3. Chờ Backend sẵn sàng
echo ""
echo "[2/3] Đang đợi Backend khởi động hoàn tất (thường mất khoảng 10-20 giây)..."
until curl -s http://localhost:3000/api/health | grep -q "UP"; do
    sleep 2
done
echo "=> Backend đã sẵn sàng!"

# 4. Khởi động Frontend
echo ""
echo "[3/3] Đang bật giao diện Frontend (JavaFX Desktop)..."
cd frontend
../mvnw javafx:run
cd ..

echo ""
echo "Ứng dụng đã đóng."
