# Project Structure

## Module boundaries

### Backend

`backend` là Spring Boot REST API và dùng cấu trúc phân tầng:

- `controller`: HTTP endpoints.
- `service`: hợp đồng nghiệp vụ.
- `service.impl`: hiện thực nghiệp vụ.
- `repository`: truy cập PostgreSQL qua Spring Data JPA.
- `entity`: mô hình persistence.
- `dto`: request/response của API.
- `security`, `config`, `exception`: hạ tầng dùng chung.

### Frontend

`frontend` là JavaFX desktop client:

- `desktop.application`: entry point và điều hướng hệ thống chính.
- `desktop.shared`: API client, model và exception dùng chung.
- `desktop.warehouse.application`: điều phối use case kiểm hàng.
- `desktop.warehouse.domain`: quy tắc đối chiếu và sai lệch.
- `desktop.warehouse.ui.page`: từng màn hình WMS.
- `desktop.warehouse.ui.component`: thành phần giao diện dùng lại.

### Database

`db/migration` là nguồn SQL duy nhất. Docker mount thư mục này vào PostgreSQL để khởi tạo schema và dữ liệu test.

## Dependency direction

```text
warehouse.ui -> warehouse.application -> shared.api
warehouse.ui -> warehouse.domain
application  -> shared.api + shared.model
backend controller -> service -> repository/entity
```

UI không truy cập database trực tiếp. JavaFX gọi backend qua `DatabaseClient`; backend chịu trách nhiệm nghiệp vụ và persistence.
