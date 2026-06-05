# Project Structure

## Runtime boundary

Luồng chạy chính của OOAS là JavaFX desktop app kết nối trực tiếp PostgreSQL qua JDBC. Không có backend REST, HTTP client, Spring Data JPA hay ORM trong luồng desktop.

## Desktop App

Source chạy chính nằm trong `frontend/src/main/java/com/ooas/desktop`:

- `application`: JavaFX entry point và điều hướng hệ thống chính.
- `shared.api`: `DatabaseClient`, JDBC client gọi PostgreSQL bằng SQL trực tiếp.
- `shared.exception`: lỗi nghiệp vụ/database dùng chung.
- `shared.model`: enum và request/response record.
- `warehouse.application`: điều phối use case kiểm hàng.
- `warehouse.domain`: quy tắc đối chiếu và sai lệch.
- `warehouse.ui.page`: từng màn hình WMS.
- `warehouse.ui.component`: thành phần giao diện dùng lại.

Resource giao diện nằm trong `frontend/src/main/resources/com/ooas/desktop`.

## Database

`db/migration` là nguồn SQL duy nhất. Docker Compose mount thư mục này vào PostgreSQL tại `/docker-entrypoint-initdb.d` để khởi tạo schema và seed dữ liệu khi volume database được tạo lần đầu.

## Dependency Direction

```text
application  -> shared.api + shared.model
warehouse.ui -> warehouse.application -> shared.api
warehouse.ui -> warehouse.domain
shared.api   -> PostgreSQL JDBC
```

Thư mục `backend` chỉ còn là mã cũ tham khảo và không nằm trong Maven/build/chạy chính của app desktop.
