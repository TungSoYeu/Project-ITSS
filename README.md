# Overseas Order Automation System (OOAS)

Đây là hệ thống quản lý và tự động hóa đơn hàng quốc tế. Dự án bao gồm:
- **Backend:** Java Spring Boot 3, Spring Data JPA, Spring Security (JWT), PostgreSQL, Flyway.
- **Frontend:** React, Vite, TypeScript, TailwindCSS (hoặc các thư viện styling tương ứng).

Dưới đây là hướng dẫn để chạy dự án bằng **Docker** (dành cho người muốn chạy thử nhanh) hoặc chạy **Local** (dành cho môi trường phát triển).

---

## 🐳 1. Chạy dự án bằng Docker (Khuyên dùng)

Cách này phù hợp để khởi chạy toàn bộ hệ thống (Database, Backend, Frontend) chỉ với 1 lệnh, không cần phải cài đặt Node.js hay Java trên máy.

### Yêu cầu:
- Đã cài đặt [Docker](https://www.docker.com/products/docker-desktop/) và Docker Compose.

### Các bước thực hiện:

1. Copy file `.env.example` thành `.env` ở thư mục gốc (nếu bạn muốn đổi thông số, mặc định có thể để nguyên):
   ```bash
   cp .env.example .env
   ```

2. Mở terminal tại thư mục gốc của dự án và chạy lệnh:
   ```bash
   docker-compose up -d --build
   ```

3. Đợi vài phút để Docker tải image và build dự án. Sau khi hoàn tất, truy cập:
   - **Frontend:** [http://localhost:5173](http://localhost:5173)
   - **Backend API:** [http://localhost:3000](http://localhost:3000)

4. Để dừng dự án:
   ```bash
   docker-compose down
   ```

---

## 💻 2. Chạy dự án Local (Môi trường Development)

Cách này phù hợp khi bạn muốn code và debug trực tiếp.

### Yêu cầu:
- **Java 21+**
- **Node.js 20+**
- **PostgreSQL 15+** (hoặc dùng Docker chỉ để chạy Database)

### Bước 1: Khởi động Database

Bạn có thể tạo database `ooas_spring` trên PostgreSQL local của bạn, hoặc chạy nhanh bằng Docker:
```bash
# Lệnh này sẽ dùng file compose.yaml trong thư mục backend để chạy riêng DB
docker-compose -f backend/compose.yaml up -d
```

### Bước 2: Cấu hình biến môi trường

1. Copy file `.env.example` ra `.env` ở thư mục gốc (hoặc thư mục `backend/` tuỳ cấu hình của bạn).
2. Đảm bảo thông tin kết nối DB (URL, username, password) trong file `.env` hoặc `application.yml` khớp với database của bạn.

### Bước 3: Chạy Backend (Spring Boot)

Mở terminal, di chuyển vào thư mục `backend`:
```bash
cd backend
# Chạy dự án (Windows)
.\mvnw spring-boot:run
# Hoặc trên macOS/Linux: ./mvnw spring-boot:run
```
Backend sẽ khởi chạy tại: **http://localhost:3000**

### Bước 4: Chạy Frontend (React Vite)

Mở một terminal khác, di chuyển vào thư mục `frontend`:
```bash
cd frontend
# Cài đặt thư viện
npm install
# Khởi chạy server development
npm run dev
```
Frontend sẽ khởi chạy tại: **http://localhost:5173** (tuỳ theo hiển thị của Vite).

---

## 🔑 Tài khoản Test

Vui lòng khởi tạo (seed) database bằng Flyway để có dữ liệu mẫu hoặc đăng ký tài khoản mới qua màn hình Register.

## 🔗 Các Module API chính

- **Auth:** `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/profile`
- **Users:** `GET /api/admin/users`
- **Catalog/SKUs:** `GET/POST/PUT /api/skus`
- **Sites/Inventory:** `GET/POST/PUT /api/sites`, `GET/PUT /api/sites/{id}/inventory`
- **Order Requests:** `GET/POST/PUT /api/order-requests`, `GET /api/order-requests/{id}/inventory-check`
- **Optimization:** `POST /api/order-requests/{id}/optimize`
- **Purchase Orders:** `POST /api/order-requests/{id}/purchase-orders`, `GET /api/purchase-orders`
- **Warehouse/Shipments:** `GET /api/warehouse/inbound`, `GET /api/shipments/in-transit`
