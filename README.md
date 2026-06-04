# Overseas Order Automation System

OOAS gồm ba thành phần:

- `backend`: Spring Boot REST API.
- `frontend`: JavaFX desktop, gồm hệ thống đặt hàng OOAS và hệ thống quản lý kho WMS.
- `db`: schema và dữ liệu kiểm thử PostgreSQL.

## Chạy dự án

Yêu cầu: Java 21+, Docker Desktop.

```powershell
Copy-Item .env.example .env
docker compose up -d postgres
.\mvnw.cmd "-Dmaven.repo.local=.m2\repository" -pl backend spring-boot:run
```

Mở terminal thứ hai:

```powershell
.\mvnw.cmd "-Dmaven.repo.local=.m2\repository" -pl frontend javafx:run
```

Tài khoản WMS kiểm thử:

```text
Email: warehouse.test@ooas.local
Mật khẩu: warehouse123
```

## Cấu trúc

```text
backend/
  src/main/java/com/ooas/
    config/ controller/ dto/ entity/
    exception/ repository/ security/ service/

frontend/
  src/main/java/com/ooas/desktop/
    application/                 JavaFX entry point
    shared/
      api/                       REST API client
      exception/                 Lỗi dùng chung
      model/                     Request/response model dùng chung
    warehouse/
      application/               Điều phối use case WMS
      domain/                    Logic kiểm hàng
      ui/
        component/               Thành phần JavaFX dùng lại
        page/                    Các màn hình WMS
  src/main/resources/com/ooas/desktop/
    ooas/                        CSS hệ thống đặt hàng
    warehouse/                   CSS hệ thống kho

db/migration/                    Nguồn schema và seed duy nhất
docs/                            Tài liệu phân tích
```

Xem mô tả chi tiết tại `docs/STRUCTURE.md`.

## Build kiểm tra

```powershell
.\mvnw.cmd "-Dmaven.repo.local=.m2\repository" clean package -DskipTests
```

## Database

PostgreSQL chạy tại `localhost:5432`. Các SQL trong `db/migration` chỉ được chạy tự động khi Docker volume được tạo lần đầu.

Để tạo lại toàn bộ database:

```powershell
docker compose down -v
docker compose up -d postgres
```
