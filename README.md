# Overseas Order Automation System (OOAS)

OOAS hiện chạy bằng **JavaFX desktop app nối trực tiếp PostgreSQL qua JDBC**.

- **UI:** JavaFX desktop.
- **Database:** PostgreSQL.
- **Data access:** JDBC trực tiếp, không gọi backend REST, không dùng ORM.

App desktop không dùng Spring/JPA/Jackson/HTTP client. Dependency ngoài tối thiểu còn lại là PostgreSQL JDBC driver để Java kết nối được PostgreSQL và BCrypt verifier để đăng nhập đúng với password seed hiện có.

`http://localhost:3000` không còn là luồng chạy chính của app desktop. Khi chạy đúng, bạn sẽ thấy cửa sổ `OOAS JavaFX`, không phải trang web trong trình duyệt.

---

## 1. Yêu cầu

- Java 21+
- Docker và Docker Compose

---

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

```text
Database: ooas
Database port: 5433 on host, 5432 inside Docker
```

### Bước 2: Khởi động PostgreSQL

```powershell
docker-compose up -d
```

Compose chỉ chạy PostgreSQL. Các file SQL trong `db/migration` được mount vào `/docker-entrypoint-initdb.d` để tạo schema và seed dữ liệu khi volume database được tạo lần đầu.

Nếu trước đó bạn đã có volume cũ và muốn tạo lại database từ đầu:

```powershell
docker-compose down -v
docker-compose up -d
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

```text
Email
Password
```

### Bước 4: Đăng nhập hệ thống

Sử dụng tài khoản mà bạn đã thiết lập trong cơ sở dữ liệu để đăng nhập. Hoặc bạn có thể dùng tính năng "Đăng ký tài khoản" trên màn hình đăng nhập.

---

## 3. Dừng app

Đóng cửa sổ JavaFX, sau đó dừng PostgreSQL:

```powershell
docker-compose down
```

Nếu muốn xóa luôn dữ liệu database:

```powershell
docker-compose down -v
```

---

## 4. Cấu trúc dự án

```text
src/main/java/com/ooas
  app/          JavaFX entry point và màn hình chính
  exception/    Lỗi nghiệp vụ/database
  model/        Enum, request/response record tách theo từng file
  repository/   JDBC trực tiếp PostgreSQL
src/main/resources/com/ooas/ui
  app.css       Giao diện JavaFX
db/migration    Schema và seed PostgreSQL
docs/           Tài liệu phân tích môn học
```

## 5. Build kiểm tra

Build JavaFX client:

```powershell
.\mvnw.cmd "-Dmaven.repo.local=.m2\repository" -DskipTests package
```

Luồng chạy chính chỉ cần JavaFX desktop app và PostgreSQL.
