# Overseas Order Automation System

OOAS hiện chạy bằng JavaFX desktop app nối trực tiếp PostgreSQL qua JDBC.

- UI: JavaFX desktop, không phải web.
- Database: PostgreSQL.
- Data access: JDBC trực tiếp, không gọi backend REST, không dùng ORM.
- Dependency ngoài tối thiểu: PostgreSQL JDBC driver và BCrypt verifier.

`http://localhost:3000` không còn là luồng chạy chính. Khi chạy đúng, bạn sẽ thấy cửa sổ OOAS JavaFX, không phải trang web trong trình duyệt.

## 1. Yêu cầu

- Java 21+
- Docker và Docker Compose

## 2. Chạy nhanh

### Bước 1: Tạo file `.env`

Tại thư mục gốc dự án:

```powershell
Copy-Item .env.example .env
```

Trên macOS/Linux:

```bash
cp .env.example .env
```

Mặc định database là:

- Database: `ooas`
- Database port: `5432`
- User: `postgres`
- Password: `admin`

### Bước 2: Khởi động PostgreSQL

```powershell
docker compose up -d
```

Compose chỉ chạy PostgreSQL. Các file SQL trong `db/migration` được mount vào `/docker-entrypoint-initdb.d` để tạo schema và seed dữ liệu khi volume database được tạo lần đầu.

Nếu trước đó bạn đã có volume cũ và muốn tạo lại database từ đầu:

```powershell
docker compose down -v
docker compose up -d
```

### Bước 3: Chạy app desktop JavaFX

Mở terminal mới tại thư mục gốc dự án:

```powershell
.\mvnw.cmd "-Dmaven.repo.local=.m2\repository" javafx:run
```

Trên macOS/Linux:

```bash
./mvnw "-Dmaven.repo.local=.m2/repository" javafx:run
```

Trên màn hình đăng nhập chỉ cần nhập tài khoản OOAS:

- Email
- Password

### Bước 4: Đăng nhập hệ thống

Bạn có thể dùng tài khoản seed mặc định, tất cả dùng mật khẩu `warehouse123`:

- `admin.test@ooas.local`
- `sales.test@ooas.local`
- `overseas.test@ooas.local`
- `warehouse.test@ooas.local`
- `supplier.test@ooas.local`

Hoặc dùng tính năng "Đăng ký tài khoản" trên màn hình đăng nhập.

## 3. Dừng app

Đóng cửa sổ JavaFX, sau đó dừng PostgreSQL:

```powershell
docker compose down
```

Nếu muốn xóa luôn dữ liệu database:

```powershell
docker compose down -v
```

## 4. Cấu trúc dự án

```text
frontend/src/main/java/com/ooas/desktop
  application/   JavaFX entry point và màn hình chính
  shared/        JDBC client, exception, request/response record
  warehouse/     Màn hình và logic quản lý kho WMS

frontend/src/main/resources/com/ooas/desktop
  ooas/          CSS hệ thống đặt hàng
  warehouse/     CSS hệ thống kho

db/migration     Schema và seed PostgreSQL
docs/            Tài liệu phân tích môn học
```

## 5. Build kiểm tra

```powershell
.\mvnw.cmd "-Dmaven.repo.local=.m2\repository" -DskipTests package
```

Luồng chạy chính chỉ cần JavaFX desktop app và PostgreSQL.
