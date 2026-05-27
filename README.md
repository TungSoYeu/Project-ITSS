# OOAS - Spring Boot Backend

Backend cho he thong Overseas Order Automation System, duoc dung lai bang Java Spring Boot, PostgreSQL va Spring Data JPA.

## Stack

- Java 21+
- Spring Boot 3.5.14
- Spring Data JPA
- Spring Security + JWT
- PostgreSQL
- Flyway migration
- Maven

## Chay local

1. Tao database PostgreSQL `ooas_spring`, hoac chay PostgreSQL bang Docker:

```bash
docker compose up -d postgres
```

2. Cau hinh `.env` tu file mau:

```bash
copy .env.example .env
```

3. Chay backend:

```bash
mvn spring-boot:run
```

API mac dinh chay tai `http://localhost:3000`.

## Tai khoan test

Vui long khoi tao (seed) database bang Flyway de co du lieu mau hoac lien he Admin de duoc cung cap tai khoan test.

## Module API chinh

- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/profile`
- `GET /api/admin/users`
- `GET/POST/PUT /api/skus`
- `GET/POST/PUT /api/sites`, `GET/PUT /api/sites/{id}/inventory`
- `GET/POST/PUT /api/order-requests`
- `GET /api/order-requests/{id}/inventory-check`
- `POST /api/order-requests/{id}/optimize`
- `POST /api/order-requests/{id}/purchase-orders`
- `GET /api/purchase-orders`
- `GET /api/warehouse/inbound`
- `GET /api/shipments/in-transit`
