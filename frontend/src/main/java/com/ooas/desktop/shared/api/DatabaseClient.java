package com.ooas.desktop.shared.api;

import com.ooas.desktop.shared.exception.DatabaseException;
import com.ooas.desktop.shared.model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseClient {
    private String jdbcUrl;
    private String dbUsername;
    private String dbPassword;
    private String currentUserId;

    public DatabaseClient(String jdbcUrl) {
        Map<String, String> env = loadDotEnv();
        this.jdbcUrl = firstNonBlank(env.get("DB_URL"), System.getenv("DB_URL"), jdbcUrl, "jdbc:postgresql://localhost:5432/ooas");
        this.dbUsername = firstNonBlank(env.get("DB_USERNAME"), System.getenv("DB_USERNAME"), env.get("POSTGRES_USER"), System.getenv("POSTGRES_USER"), "postgres");
        this.dbPassword = firstNonBlank(env.get("DB_PASSWORD"), System.getenv("DB_PASSWORD"), env.get("POSTGRES_PASSWORD"), System.getenv("POSTGRES_PASSWORD"), "admin");
    }

    public void setBaseUrl(String jdbcUrl) {
        this.jdbcUrl = firstNonBlank(jdbcUrl, this.jdbcUrl);
    }

    public String baseUrl() {
        return jdbcUrl;
    }

    public void setDatabaseCredentials(String username, String password) {
        this.dbUsername = firstNonBlank(username, this.dbUsername);
        this.dbPassword = firstNonBlank(password, this.dbPassword);
    }

    public String dbUsername() {
        return dbUsername;
    }

    public String dbPassword() {
        return dbPassword;
    }

    public void setToken(String token) {
        this.currentUserId = token;
    }

    public CompletableFuture<AuthResponse> login(String email, String password) {
        return async(conn -> {
            UserWithPassword user = findUserByEmail(conn, email);
            if (user == null || !BCrypt.checkpw(password, user.password())) {
                throw unauthorized("Email hoac mat khau khong dung");
            }
            if (user.response().status() == AccountStatus.PENDING) {
                throw badRequest("Tai khoan dang cho phe duyet");
            }
            if (user.response().status() == AccountStatus.BLOCKED) {
                throw badRequest("Tai khoan da bi khoa");
            }
            currentUserId = user.response().id();
            return new AuthResponse(currentUserId, user.response());
        });
    }

    public CompletableFuture<Map<String, Object>> register(RegisterRequest request) {
        return async(conn -> {
            requireText(request.fullName(), "Ho ten la bat buoc");
            requireText(request.email(), "Email la bat buoc");
            requireText(request.password(), "Mat khau la bat buoc");
            requireText(request.employeeId(), "Ma nhan vien la bat buoc");
            if (exists(conn, "SELECT 1 FROM users WHERE lower(email)=lower(?)", request.email())) {
                throw badRequest("Email da ton tai");
            }
            if (exists(conn, "SELECT 1 FROM users WHERE employee_id=?", request.employeeId())) {
                throw badRequest("Ma nhan vien da ton tai");
            }
            String id = uuid();
            try (PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO users (id, email, password, full_name, employee_id, role, status, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, 'PENDING', NOW(), NOW())
                    """)) {
                ps.setString(1, id);
                ps.setString(2, request.email().trim());
                ps.setString(3, BCrypt.hashpw(request.password(), BCrypt.gensalt(10)));
                ps.setString(4, request.fullName().trim());
                ps.setString(5, request.employeeId().trim());
                ps.setString(6, request.role().name());
                ps.executeUpdate();
            }
            return Map.of("id", id, "message", "Dang ky thanh cong. Vui long cho phe duyet.");
        });
    }

    public CompletableFuture<UserResponse> profile() {
        return async(conn -> requireCurrentUser(conn));
    }

    public CompletableFuture<Map<String, Object>> updateProfile(String fullName, String employeeId) {
        return async(conn -> {
            UserResponse user = requireCurrentUser(conn);
            try (PreparedStatement ps = conn.prepareStatement("""
                    UPDATE users SET full_name=?, employee_id=?, updated_at=NOW()
                    WHERE id=?
                    """)) {
                ps.setString(1, fullName.trim());
                ps.setString(2, employeeId.trim());
                ps.setString(3, user.id());
                ps.executeUpdate();
            }
            return Map.of("message", "Da cap nhat ho so");
        });
    }

    public CompletableFuture<Map<String, Object>> changePassword(String oldPassword, String newPassword) {
        return async(conn -> {
            UserWithPassword user = requireCurrentUserWithPassword(conn);
            if (!BCrypt.checkpw(oldPassword, user.password())) {
                throw badRequest("Mat khau hien tai khong dung");
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET password=?, updated_at=NOW() WHERE id=?")) {
                ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt(10)));
                ps.setString(2, user.response().id());
                ps.executeUpdate();
            }
            return Map.of("message", "Da doi mat khau");
        });
    }

    public CompletableFuture<List<UserResponse>> listUsers(AccountStatus status, String search) {
        return async(conn -> {
            List<Object> params = new ArrayList<>();
            StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
            if (status != null) {
                sql.append(" AND status=?");
                params.add(status.name());
            }
            if (hasText(search)) {
                sql.append(" AND (lower(email) LIKE ? OR lower(full_name) LIKE ? OR lower(employee_id) LIKE ?)");
                String like = like(search);
                params.add(like);
                params.add(like);
                params.add(like);
            }
            sql.append(" ORDER BY created_at DESC");
            return query(conn, sql.toString(), params, this::mapUser);
        });
    }

    public CompletableFuture<Map<String, Object>> approveUser(String id) {
        return updateUserStatus(id, AccountStatus.APPROVED);
    }

    public CompletableFuture<Map<String, Object>> blockUser(String id) {
        return updateUserStatus(id, AccountStatus.BLOCKED);
    }

    public CompletableFuture<Map<String, Object>> updateRole(String id, Role role) {
        return async(conn -> {
            execute(conn, "UPDATE users SET role=?, updated_at=NOW() WHERE id=?", role.name(), id);
            return Map.of("message", "Da cap nhat vai tro");
        });
    }

    public CompletableFuture<Map<String, Long>> dashboardSummary() {
        return async(conn -> Map.of(
                "pendingRequests", count(conn, "SELECT COUNT(*) FROM order_requests WHERE status='PENDING'"),
                "processingRequests", count(conn, "SELECT COUNT(*) FROM order_requests WHERE status='PROCESSING'"),
                "shippingOrders", count(conn, "SELECT COUNT(*) FROM purchase_orders WHERE status='SHIPPING'"),
                "warehouseInboundOrders", count(conn, "SELECT COUNT(*) FROM purchase_orders WHERE status IN ('SHIPPING','ARRIVED')")
        ));
    }

    public CompletableFuture<List<SkuResponse>> listSkus(String search) {
        return async(conn -> {
            List<Object> params = new ArrayList<>();
            String sql = "SELECT * FROM skus";
            if (hasText(search)) {
                sql += " WHERE lower(code) LIKE ? OR lower(name) LIKE ?";
                params.add(like(search));
                params.add(like(search));
            }
            return query(conn, sql + " ORDER BY code", params, this::mapSku);
        });
    }

    public CompletableFuture<SkuResponse> createSku(SkuRequest request) {
        return async(conn -> {
            String id = uuid();
            execute(conn, """
                    INSERT INTO skus (id, code, name, unit, description, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, NOW(), NOW())
                    """, id, request.code().trim(), request.name().trim(), request.unit().trim(), blankToNull(request.description()));
            return requireSku(conn, id);
        });
    }

    public CompletableFuture<SkuResponse> updateSku(String id, SkuRequest request) {
        return async(conn -> {
            execute(conn, """
                    UPDATE skus SET code=?, name=?, unit=?, description=?, updated_at=NOW()
                    WHERE id=?
                    """, request.code().trim(), request.name().trim(), request.unit().trim(), blankToNull(request.description()), id);
            return requireSku(conn, id);
        });
    }

    public CompletableFuture<List<SiteResponse>> listSites(Boolean active, String search) {
        return async(conn -> {
            List<Object> params = new ArrayList<>();
            StringBuilder sql = new StringBuilder("""
                    SELECT s.*, COUNT(si.id) AS sku_count
                    FROM sites s LEFT JOIN site_inventories si ON si.site_id=s.id
                    WHERE 1=1
                    """);
            if (active != null) {
                sql.append(" AND s.active=?");
                params.add(active);
            }
            if (hasText(search)) {
                sql.append(" AND (lower(s.code) LIKE ? OR lower(s.name) LIKE ? OR lower(s.country) LIKE ?)");
                String like = like(search);
                params.add(like);
                params.add(like);
                params.add(like);
            }
            sql.append(" GROUP BY s.id ORDER BY s.code");
            return query(conn, sql.toString(), params, this::mapSite);
        });
    }

    public CompletableFuture<SiteResponse> createSite(SiteRequest request) {
        return async(conn -> {
            String id = uuid();
            execute(conn, """
                    INSERT INTO sites (id, code, name, country, sea_lead_time, air_lead_time, active, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                    """, id, request.code().trim(), request.name().trim(), request.country().trim(), request.seaLeadTime(), request.airLeadTime(), request.active() == null || request.active());
            return requireSite(conn, id);
        });
    }

    public CompletableFuture<SiteResponse> updateSite(String id, SiteRequest request) {
        return async(conn -> {
            execute(conn, """
                    UPDATE sites SET code=?, name=?, country=?, sea_lead_time=?, air_lead_time=?, active=?, updated_at=NOW()
                    WHERE id=?
                    """, request.code().trim(), request.name().trim(), request.country().trim(), request.seaLeadTime(), request.airLeadTime(), request.active() == null || request.active(), id);
            return requireSite(conn, id);
        });
    }

    public CompletableFuture<List<InventoryResponse>> siteInventory(String siteId) {
        return async(conn -> query(conn, """
                SELECT si.*, s.code site_code, s.name site_name, sku.code sku_code, sku.name sku_name, sku.unit sku_unit
                FROM site_inventories si
                JOIN sites s ON s.id=si.site_id
                JOIN skus sku ON sku.id=si.sku_id
                WHERE si.site_id=?
                ORDER BY sku.code
                """, List.of(siteId), this::mapInventory));
    }

    public CompletableFuture<InventoryResponse> upsertInventory(String siteId, String skuId, int quantity) {
        return async(conn -> {
            String id = uuid();
            execute(conn, """
                    INSERT INTO site_inventories (id, site_id, sku_id, quantity, created_at, updated_at)
                    VALUES (?, ?, ?, ?, NOW(), NOW())
                    ON CONFLICT (site_id, sku_id)
                    DO UPDATE SET quantity=EXCLUDED.quantity, updated_at=NOW()
                    """, id, siteId, skuId, quantity);
            return queryOne(conn, """
                    SELECT si.*, s.code site_code, s.name site_name, sku.code sku_code, sku.name sku_name, sku.unit sku_unit
                    FROM site_inventories si
                    JOIN sites s ON s.id=si.site_id
                    JOIN skus sku ON sku.id=si.sku_id
                    WHERE si.site_id=? AND si.sku_id=?
                    """, List.of(siteId, skuId), this::mapInventory);
        });
    }

    public CompletableFuture<List<OrderRequestResponse>> listOrderRequests(RequestStatus status, String search) {
        return async(conn -> orderRequests(conn, status, search, null));
    }

    public CompletableFuture<OrderRequestResponse> orderRequestDetail(String id) {
        return async(conn -> requireOrderRequest(conn, id));
    }

    public CompletableFuture<OrderRequestResponse> createOrderRequest(OrderRequestUpsertRequest request) {
        return asyncTx(conn -> {
            UserResponse user = requireCurrentUser(conn);
            String id = uuid();
            execute(conn, """
                    INSERT INTO order_requests (id, code, expected_date, notes, status, created_by_id, created_at, updated_at)
                    VALUES (?, ?, ?, ?, 'DRAFT', ?, NOW(), NOW())
                    """, id, generateCode(conn, "YC", "order_requests"), request.expectedDate(), blankToNull(request.notes()), user.id());
            replaceOrderRequestItems(conn, id, request);
            return requireOrderRequest(conn, id);
        });
    }

    public CompletableFuture<OrderRequestResponse> updateOrderRequest(String id, OrderRequestUpsertRequest request) {
        return asyncTx(conn -> {
            execute(conn, "UPDATE order_requests SET expected_date=?, notes=?, updated_at=NOW() WHERE id=?", request.expectedDate(), blankToNull(request.notes()), id);
            replaceOrderRequestItems(conn, id, request);
            return requireOrderRequest(conn, id);
        });
    }

    public CompletableFuture<OrderRequestResponse> submitOrderRequest(String id) {
        return async(conn -> {
            execute(conn, "UPDATE order_requests SET status='PENDING', updated_at=NOW() WHERE id=?", id);
            return requireOrderRequest(conn, id);
        });
    }

    public CompletableFuture<OrderRequestResponse> cancelOrderRequest(String id, String reason) {
        return async(conn -> {
            execute(conn, "UPDATE order_requests SET status='CANCELLED', cancel_reason=?, updated_at=NOW() WHERE id=?", reason.trim(), id);
            return requireOrderRequest(conn, id);
        });
    }

    public CompletableFuture<InventoryCheckResponse> inventoryCheck(String requestId) {
        return async(conn -> checkInventory(conn, requireOrderRequest(conn, requestId)));
    }

    public CompletableFuture<OptimizationResponse> optimize(String requestId) {
        return async(conn -> optimizeOrder(conn, requireOrderRequest(conn, requestId)));
    }

    public CompletableFuture<List<PurchaseOrderResponse>> listPurchaseOrders(POStatus status, String siteId, String search) {
        return async(conn -> purchaseOrders(conn, status, siteId, search, null));
    }

    public CompletableFuture<PurchaseOrderResponse> purchaseOrderDetail(String id) {
        return async(conn -> requirePurchaseOrder(conn, id));
    }

    public CompletableFuture<List<PurchaseOrderResponse>> generatePurchaseOrders(String requestId) {
        return asyncTx(conn -> {
            UserResponse user = requireCurrentUser(conn);
            execute(conn, "UPDATE order_requests SET status='PROCESSING', processed_by_id=?, updated_at=NOW() WHERE id=?", user.id(), requestId);
            OrderRequestResponse request = requireOrderRequest(conn, requestId);
            OptimizationResponse optimization = optimizeOrder(conn, request);
            if (!optimization.warnings().isEmpty()) {
                throw badRequest("Khong the tao PO vi con canh bao thieu hang: " + String.join("; ", optimization.warnings()));
            }
            if (optimization.allocations().isEmpty()) {
                throw badRequest("Khong co phuong an phan bo de tao PO");
            }
            Map<String, String> poIds = new LinkedHashMap<>();
            for (AllocationResponse allocation : optimization.allocations()) {
                String key = allocation.siteId() + "|" + allocation.transportMethod();
                String poId = poIds.computeIfAbsent(key, ignored -> insertPurchaseOrder(conn, requestId, user.id(), allocation));
                execute(conn, """
                        INSERT INTO purchase_order_items (id, purchase_order_id, sku_id, quantity_ordered, quantity_received, difference, created_at, updated_at)
                        VALUES (?, ?, ?, ?, 0, 0, NOW(), NOW())
                        """, uuid(), poId, allocation.skuId(), allocation.quantity());
                execute(conn, """
                        UPDATE site_inventories SET quantity=quantity-?, updated_at=NOW()
                        WHERE site_id=? AND sku_id=? AND quantity>=?
                        """, allocation.quantity(), allocation.siteId(), allocation.skuId(), allocation.quantity());
            }
            execute(conn, "UPDATE order_requests SET status='ORDERED', updated_at=NOW() WHERE id=?", requestId);
            return purchaseOrders(conn, null, null, null, new ArrayList<>(poIds.values()));
        });
    }

    public CompletableFuture<PurchaseOrderResponse> updatePurchaseOrderStatus(String id, POStatus status, LocalDate actualArrivalDate) {
        return async(conn -> {
            LocalDate arrival = actualArrivalDate != null ? actualArrivalDate : (status == POStatus.ARRIVED ? LocalDate.now() : null);
            execute(conn, "UPDATE purchase_orders SET status=?, actual_arrival_date=COALESCE(?, actual_arrival_date), updated_at=NOW() WHERE id=?",
                    status.name(), arrival, id);
            return requirePurchaseOrder(conn, id);
        });
    }

    public CompletableFuture<PurchaseOrderResponse> cancelPurchaseOrder(String id, String reason) {
        return async(conn -> {
            PurchaseOrderResponse po = requirePurchaseOrder(conn, id);
            if (po.status() == POStatus.COMPLETED) {
                throw badRequest("Khong the huy PO da hoan tat");
            }
            execute(conn, "UPDATE purchase_orders SET status='CANCELLED', cancel_reason=?, updated_at=NOW() WHERE id=?", reason.trim(), id);
            return requirePurchaseOrder(conn, id);
        });
    }

    public CompletableFuture<List<PurchaseOrderResponse>> inboundPurchaseOrders() {
        return async(conn -> purchaseOrders(conn, null, null, null, null).stream()
                .filter(po -> po.status() == POStatus.SHIPPING || po.status() == POStatus.ARRIVED)
                .toList());
    }

    public CompletableFuture<PurchaseOrderResponse> receivePurchaseOrder(String id, ReceivePurchaseOrderRequest request) {
        return asyncTx(conn -> {
            boolean hasDifference = false;
            for (ReceiveItemRequest item : request.items()) {
                int ordered = intValue(conn, "SELECT quantity_ordered FROM purchase_order_items WHERE id=?", item.purchaseOrderItemId());
                int difference = item.quantityReceived() - ordered;
                hasDifference = hasDifference || difference != 0 || hasQualityIssue(item.notes());
                execute(conn, """
                        UPDATE purchase_order_items
                        SET quantity_received=?, difference=?, notes=?, updated_at=NOW()
                        WHERE id=?
                        """, item.quantityReceived(), difference, blankToNull(item.notes()), item.purchaseOrderItemId());
            }
            POStatus status = hasDifference ? POStatus.NEEDS_ACTION : POStatus.COMPLETED;
            execute(conn, """
                    UPDATE purchase_orders
                    SET status=?, actual_arrival_date=?, updated_at=NOW()
                    WHERE id=?
                    """, status.name(), request.actualArrivalDate() == null ? LocalDate.now() : request.actualArrivalDate(), id);
            addTrackingRow(conn, id, status, "Kho dich",
                    hasDifference ? "Da ghi nhan ket qua nhap kho co chenhlech/can xu ly" : "Da kiem dem va nhap kho", null);
            return requirePurchaseOrder(conn, id);
        });
    }

    public CompletableFuture<List<PurchaseOrderResponse>> inTransitShipments() {
        return async(conn -> purchaseOrders(conn, null, null, null, null).stream()
                .filter(po -> po.status() == POStatus.PREPARING || po.status() == POStatus.SHIPPING || po.status() == POStatus.ARRIVED)
                .toList());
    }

    public CompletableFuture<List<TrackingResponse>> trackingHistory(String purchaseOrderId) {
        return async(conn -> query(conn, """
                SELECT st.*, u.full_name updated_by_name
                FROM shipment_trackings st JOIN users u ON u.id=st.updated_by_id
                WHERE st.purchase_order_id=?
                ORDER BY st.timestamp DESC
                """, List.of(purchaseOrderId), this::mapTracking));
    }

    public CompletableFuture<TrackingResponse> addTracking(String purchaseOrderId, TrackingRequest request) {
        return async(conn -> {
            String id = addTrackingRow(conn, purchaseOrderId, request.status(), request.location(), request.notes(), request.evidenceFileUrl());
            execute(conn, "UPDATE purchase_orders SET status=?, updated_at=NOW() WHERE id=?", request.status().name(), purchaseOrderId);
            return queryOne(conn, """
                    SELECT st.*, u.full_name updated_by_name
                    FROM shipment_trackings st JOIN users u ON u.id=st.updated_by_id
                    WHERE st.id=?
                    """, List.of(id), this::mapTracking);
        });
    }

    public String toPrettyJson(Object value) {
        return pretty(value, 0);
    }

    private InventoryCheckResponse checkInventory(Connection conn, OrderRequestResponse request) throws SQLException {
        List<CandidateResponse> candidates = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (OrderRequestItemResponse item : request.items()) {
            LocalDate needDate = item.expectedDate() == null ? request.expectedDate() : item.expectedDate();
            List<CandidateResponse> feasible = candidatesForItem(conn, item, needDate, today);
            int total = feasible.stream().mapToInt(CandidateResponse::availableQuantity).sum();
            if (total < item.quantity()) {
                warnings.add("SKU " + item.skuCode() + " thieu " + (item.quantity() - total) + " " + item.unit());
            }
            candidates.addAll(feasible);
        }
        return new InventoryCheckResponse(request.id(), request.code(), candidates, warnings);
    }

    private OptimizationResponse optimizeOrder(Connection conn, OrderRequestResponse request) throws SQLException {
        List<AllocationResponse> allocations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (OrderRequestItemResponse item : request.items()) {
            LocalDate needDate = item.expectedDate() == null ? request.expectedDate() : item.expectedDate();
            List<CandidateResponse> candidates = candidatesForItem(conn, item, needDate, today).stream()
                    .sorted(Comparator.comparing(CandidateResponse::transportMethod).thenComparing(CandidateResponse::availableQuantity, Comparator.reverseOrder()))
                    .toList();
            int total = candidates.stream().mapToInt(CandidateResponse::availableQuantity).sum();
            if (total < item.quantity()) {
                warnings.add("SKU " + item.skuCode() + " khong du nguon cung kha thi. Thieu " + (item.quantity() - total));
            }
            int remaining = item.quantity();
            for (CandidateResponse candidate : candidates) {
                if (remaining <= 0) {
                    break;
                }
                int quantity = Math.min(remaining, candidate.availableQuantity());
                remaining -= quantity;
                allocations.add(new AllocationResponse(item.skuId(), item.skuCode(), item.skuName(),
                        candidate.siteId(), candidate.siteCode(), candidate.siteName(), candidate.transportMethod(), quantity, candidate.expectedArrivalDate()));
            }
        }
        return new OptimizationResponse(request.id(), request.code(), allocations, warnings);
    }

    private List<CandidateResponse> candidatesForItem(Connection conn, OrderRequestItemResponse item, LocalDate needDate, LocalDate today) throws SQLException {
        return query(conn, """
                SELECT si.quantity, s.id site_id, s.code site_code, s.name site_name, s.sea_lead_time, s.air_lead_time
                FROM site_inventories si JOIN sites s ON s.id=si.site_id
                WHERE si.sku_id=? AND si.quantity>0 AND s.active=true
                ORDER BY s.code
                """, List.of(item.skuId()), rs -> {
            LocalDate sea = today.plusDays(rs.getInt("sea_lead_time"));
            if (!sea.isAfter(needDate)) {
                return new CandidateResponse(item.skuId(), item.skuCode(), item.skuName(), item.quantity(), rs.getString("site_id"),
                        rs.getString("site_code"), rs.getString("site_name"), rs.getInt("quantity"), TransportMethod.SEA, rs.getInt("sea_lead_time"), sea, true);
            }
            LocalDate air = today.plusDays(rs.getInt("air_lead_time"));
            if (!air.isAfter(needDate)) {
                return new CandidateResponse(item.skuId(), item.skuCode(), item.skuName(), item.quantity(), rs.getString("site_id"),
                        rs.getString("site_code"), rs.getString("site_name"), rs.getInt("quantity"), TransportMethod.AIR, rs.getInt("air_lead_time"), air, true);
            }
            return null;
        }).stream().filter(Objects::nonNull).toList();
    }

    private String insertPurchaseOrder(Connection conn, String requestId, String userId, AllocationResponse allocation) {
        try {
            String id = uuid();
            execute(conn, """
                    INSERT INTO purchase_orders (id, code, request_id, site_id, created_by_id, transport_method, status, expected_arrival_date, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, 'PENDING_CONFIRM', ?, NOW(), NOW())
                    """, id, generateCode(conn, "PO", "purchase_orders"), requestId, allocation.siteId(), userId, allocation.transportMethod().name(), allocation.expectedArrivalDate());
            return id;
        } catch (SQLException e) {
            throw db(e);
        }
    }

    private String addTrackingRow(Connection conn, String purchaseOrderId, POStatus status, String location, String notes, String evidence) throws SQLException {
        UserResponse user = requireCurrentUser(conn);
        String id = uuid();
        execute(conn, """
                INSERT INTO shipment_trackings (id, purchase_order_id, status, location, notes, evidence_file_url, updated_by_id, timestamp, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), NOW())
                """, id, purchaseOrderId, status.name(), blankToNull(location), blankToNull(notes), blankToNull(evidence), user.id());
        return id;
    }

    private List<OrderRequestResponse> orderRequests(Connection conn, RequestStatus status, String search, String id) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT r.*, cu.full_name created_by_name, pu.full_name processed_by_name, COUNT(i.id) item_count
                FROM order_requests r
                JOIN users cu ON cu.id=r.created_by_id
                LEFT JOIN users pu ON pu.id=r.processed_by_id
                LEFT JOIN order_request_items i ON i.request_id=r.id
                WHERE 1=1
                """);
        if (id != null) {
            sql.append(" AND r.id=?");
            params.add(id);
        }
        if (status != null) {
            sql.append(" AND r.status=?");
            params.add(status.name());
        }
        if (hasText(search)) {
            sql.append(" AND (lower(r.code) LIKE ? OR lower(cu.full_name) LIKE ?)");
            params.add(like(search));
            params.add(like(search));
        }
        sql.append(" GROUP BY r.id, cu.full_name, pu.full_name ORDER BY r.updated_at DESC");
        List<OrderRequestResponse> rows = query(conn, sql.toString(), params, rs -> mapOrderRequest(conn, rs, id != null));
        return rows;
    }

    private OrderRequestResponse requireOrderRequest(Connection conn, String id) throws SQLException {
        OrderRequestResponse request = queryOne(conn, "SELECT * FROM (" + """
                SELECT r.*, cu.full_name created_by_name, pu.full_name processed_by_name, COUNT(i.id) item_count
                FROM order_requests r
                JOIN users cu ON cu.id=r.created_by_id
                LEFT JOIN users pu ON pu.id=r.processed_by_id
                LEFT JOIN order_request_items i ON i.request_id=r.id
                GROUP BY r.id, cu.full_name, pu.full_name
                """ + ") x WHERE id=?", List.of(id), rs -> mapOrderRequest(conn, rs, true));
        if (request == null) {
            throw notFound("Khong tim thay yeu cau");
        }
        return request;
    }

    private void replaceOrderRequestItems(Connection conn, String requestId, OrderRequestUpsertRequest request) throws SQLException {
        execute(conn, "DELETE FROM order_request_items WHERE request_id=?", requestId);
        for (OrderRequestItemRequest item : request.items()) {
            execute(conn, """
                    INSERT INTO order_request_items (id, request_id, sku_id, quantity, expected_date, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, NOW(), NOW())
                    """, uuid(), requestId, item.skuId(), item.quantity(), item.expectedDate());
        }
    }

    private List<PurchaseOrderResponse> purchaseOrders(Connection conn, POStatus status, String siteId, String search, List<String> ids) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT po.*, r.code request_code, s.code site_code, s.name site_name, s.country site_country,
                       u.full_name created_by_name, COUNT(i.id) item_count
                FROM purchase_orders po
                JOIN order_requests r ON r.id=po.request_id
                JOIN sites s ON s.id=po.site_id
                JOIN users u ON u.id=po.created_by_id
                LEFT JOIN purchase_order_items i ON i.purchase_order_id=po.id
                WHERE 1=1
                """);
        if (status != null) {
            sql.append(" AND po.status=?");
            params.add(status.name());
        }
        if (hasText(siteId)) {
            sql.append(" AND po.site_id=?");
            params.add(siteId);
        }
        if (hasText(search)) {
            sql.append(" AND (lower(po.code) LIKE ? OR lower(s.code) LIKE ? OR lower(s.name) LIKE ?)");
            String like = like(search);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (ids != null && !ids.isEmpty()) {
            sql.append(" AND po.id IN (");
            sql.append("?,".repeat(ids.size()));
            sql.setLength(sql.length() - 1);
            sql.append(")");
            params.addAll(ids);
        }
        sql.append(" GROUP BY po.id, r.code, s.code, s.name, s.country, u.full_name ORDER BY po.updated_at DESC");
        return query(conn, sql.toString(), params, rs -> mapPurchaseOrder(conn, rs, true));
    }

    private PurchaseOrderResponse requirePurchaseOrder(Connection conn, String id) throws SQLException {
        List<PurchaseOrderResponse> rows = purchaseOrders(conn, null, null, null, List.of(id));
        if (rows.isEmpty()) {
            throw notFound("Khong tim thay don hang");
        }
        return rows.get(0);
    }

    private OrderRequestResponse mapOrderRequest(Connection conn, ResultSet rs, boolean withItems) throws SQLException {
        String id = rs.getString("id");
        List<OrderRequestItemResponse> items = withItems ? query(conn, """
                SELECT i.*, s.code sku_code, s.name sku_name, s.unit sku_unit
                FROM order_request_items i JOIN skus s ON s.id=i.sku_id
                WHERE i.request_id=?
                ORDER BY s.code
                """, List.of(id), this::mapOrderRequestItem) : List.of();
        return new OrderRequestResponse(id, rs.getString("code"), localDate(rs, "expected_date"), rs.getString("notes"),
                RequestStatus.valueOf(rs.getString("status")), rs.getString("cancel_reason"), rs.getString("created_by_id"),
                rs.getString("created_by_name"), rs.getString("processed_by_id"), rs.getString("processed_by_name"),
                rs.getInt("item_count"), items, instant(rs, "created_at"), instant(rs, "updated_at"));
    }

    private PurchaseOrderResponse mapPurchaseOrder(Connection conn, ResultSet rs, boolean withItems) throws SQLException {
        String id = rs.getString("id");
        List<PurchaseOrderItemResponse> items = withItems ? query(conn, """
                SELECT i.*, s.code sku_code, s.name sku_name, s.unit sku_unit
                FROM purchase_order_items i JOIN skus s ON s.id=i.sku_id
                WHERE i.purchase_order_id=?
                ORDER BY s.code
                """, List.of(id), this::mapPurchaseOrderItem) : List.of();
        return new PurchaseOrderResponse(id, rs.getString("code"), rs.getString("request_id"), rs.getString("request_code"),
                rs.getString("site_id"), rs.getString("site_code"), rs.getString("site_name"), rs.getString("site_country"),
                rs.getString("created_by_id"), rs.getString("created_by_name"), TransportMethod.valueOf(rs.getString("transport_method")),
                POStatus.valueOf(rs.getString("status")), localDate(rs, "expected_arrival_date"), localDate(rs, "actual_arrival_date"),
                rs.getString("cancel_reason"), rs.getInt("item_count"), items, instant(rs, "created_at"), instant(rs, "updated_at"));
    }

    private UserResponse mapUser(ResultSet rs) throws SQLException {
        return new UserResponse(rs.getString("id"), rs.getString("email"), rs.getString("full_name"), rs.getString("employee_id"),
                Role.valueOf(rs.getString("role")), AccountStatus.valueOf(rs.getString("status")), instant(rs, "created_at"));
    }

    private SkuResponse mapSku(ResultSet rs) throws SQLException {
        return new SkuResponse(rs.getString("id"), rs.getString("code"), rs.getString("name"), rs.getString("unit"),
                rs.getString("description"), instant(rs, "created_at"), instant(rs, "updated_at"));
    }

    private SiteResponse mapSite(ResultSet rs) throws SQLException {
        return new SiteResponse(rs.getString("id"), rs.getString("code"), rs.getString("name"), rs.getString("country"),
                rs.getInt("sea_lead_time"), rs.getInt("air_lead_time"), rs.getBoolean("active"), rs.getLong("sku_count"),
                instant(rs, "created_at"), instant(rs, "updated_at"));
    }

    private InventoryResponse mapInventory(ResultSet rs) throws SQLException {
        return new InventoryResponse(rs.getString("id"), rs.getString("site_id"), rs.getString("site_code"), rs.getString("site_name"),
                rs.getString("sku_id"), rs.getString("sku_code"), rs.getString("sku_name"), rs.getString("sku_unit"),
                rs.getInt("quantity"), instant(rs, "updated_at"));
    }

    private OrderRequestItemResponse mapOrderRequestItem(ResultSet rs) throws SQLException {
        return new OrderRequestItemResponse(rs.getString("id"), rs.getString("sku_id"), rs.getString("sku_code"),
                rs.getString("sku_name"), rs.getString("sku_unit"), rs.getInt("quantity"), localDate(rs, "expected_date"));
    }

    private PurchaseOrderItemResponse mapPurchaseOrderItem(ResultSet rs) throws SQLException {
        return new PurchaseOrderItemResponse(rs.getString("id"), rs.getString("sku_id"), rs.getString("sku_code"),
                rs.getString("sku_name"), rs.getString("sku_unit"), rs.getInt("quantity_ordered"), rs.getInt("quantity_received"),
                rs.getInt("difference"), rs.getString("notes"));
    }

    private TrackingResponse mapTracking(ResultSet rs) throws SQLException {
        return new TrackingResponse(rs.getString("id"), rs.getString("purchase_order_id"), POStatus.valueOf(rs.getString("status")),
                rs.getString("location"), rs.getString("notes"), rs.getString("evidence_file_url"), rs.getString("updated_by_id"),
                rs.getString("updated_by_name"), instant(rs, "timestamp"));
    }

    private UserWithPassword findUserByEmail(Connection conn, String email) throws SQLException {
        return queryOne(conn, "SELECT * FROM users WHERE lower(email)=lower(?)", List.of(email.trim()), rs -> new UserWithPassword(mapUser(rs), rs.getString("password")));
    }

    private UserResponse requireCurrentUser(Connection conn) throws SQLException {
        return requireCurrentUserWithPassword(conn).response();
    }

    private UserWithPassword requireCurrentUserWithPassword(Connection conn) throws SQLException {
        if (!hasText(currentUserId)) {
            throw unauthorized("Vui long dang nhap");
        }
        UserWithPassword user = queryOne(conn, "SELECT * FROM users WHERE id=?", List.of(currentUserId), rs -> new UserWithPassword(mapUser(rs), rs.getString("password")));
        if (user == null) {
            throw unauthorized("Phien dang nhap khong hop le");
        }
        return user;
    }

    private CompletableFuture<Map<String, Object>> updateUserStatus(String id, AccountStatus status) {
        return async(conn -> {
            execute(conn, "UPDATE users SET status=?, updated_at=NOW() WHERE id=?", status.name(), id);
            return Map.of("message", "Da cap nhat trang thai");
        });
    }

    private SkuResponse requireSku(Connection conn, String id) throws SQLException {
        return queryOne(conn, "SELECT * FROM skus WHERE id=?", List.of(id), this::mapSku);
    }

    private SiteResponse requireSite(Connection conn, String id) throws SQLException {
        return queryOne(conn, """
                SELECT s.*, COUNT(si.id) AS sku_count
                FROM sites s LEFT JOIN site_inventories si ON si.site_id=s.id
                WHERE s.id=?
                GROUP BY s.id
                """, List.of(id), this::mapSite);
    }

    private <T> CompletableFuture<T> async(SqlWork<T> work) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = connection()) {
                return work.run(conn);
            } catch (DatabaseException e) {
                throw e;
            } catch (SQLException e) {
                throw db(e);
            }
        });
    }

    private <T> CompletableFuture<T> asyncTx(SqlWork<T> work) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = connection()) {
                conn.setAutoCommit(false);
                try {
                    T value = work.run(conn);
                    conn.commit();
                    return value;
                } catch (Exception e) {
                    conn.rollback();
                    if (e instanceof SQLException sqlException) {
                        throw db(sqlException);
                    }
                    if (e instanceof RuntimeException runtimeException) {
                        throw runtimeException;
                    }
                    throw new RuntimeException(e);
                }
            } catch (DatabaseException e) {
                throw e;
            } catch (SQLException e) {
                throw db(e);
            }
        });
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
    }

    private <T> List<T> query(Connection conn, String sql, List<?> params, RowMapper<T> mapper) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapper.map(rs));
                }
                return rows;
            }
        }
    }

    private <T> T queryOne(Connection conn, String sql, List<?> params, RowMapper<T> mapper) throws SQLException {
        List<T> rows = query(conn, sql, params, mapper);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private void execute(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, Arrays.asList(params));
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, List<?> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof LocalDate date) {
                ps.setDate(i + 1, java.sql.Date.valueOf(date));
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    private boolean exists(Connection conn, String sql, Object... params) throws SQLException {
        return queryOne(conn, sql, Arrays.asList(params), rs -> 1) != null;
    }

    private long count(Connection conn, String sql) throws SQLException {
        return queryOne(conn, sql, List.of(), rs -> rs.getLong(1));
    }

    private int intValue(Connection conn, String sql, Object... params) throws SQLException {
        Integer value = queryOne(conn, sql, Arrays.asList(params), rs -> rs.getInt(1));
        if (value == null) {
            throw notFound("Khong tim thay du lieu");
        }
        return value;
    }

    private String generateCode(Connection conn, String prefix, String table) throws SQLException {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String code;
        do {
            code = prefix + "-" + date + "-" + Long.toString(System.nanoTime()).substring(8);
        } while (exists(conn, "SELECT 1 FROM " + table + " WHERE code=?", code));
        return code;
    }

    private Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    private LocalDate localDate(ResultSet rs, String column) throws SQLException {
        java.sql.Date value = rs.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    private Map<String, String> loadDotEnv() {
        Map<String, String> values = new HashMap<>();
        Path path = Path.of(".env");
        if (!Files.exists(path)) {
            return values;
        }
        try {
            for (String line : Files.readAllLines(path)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int split = trimmed.indexOf('=');
                values.put(trimmed.substring(0, split).trim(), trimmed.substring(split + 1).trim());
            }
        } catch (IOException ignored) {
            // Environment variables and defaults still allow the app to start.
        }
        return values;
    }

    private String pretty(Object value, int indent) {
        if (value == null || value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
            return String.valueOf(value);
        }
        if (value instanceof String text) {
            return "\"" + text.replace("\"", "\\\"") + "\"";
        }
        if (value instanceof Record record) {
            Map<String, Object> fields = new LinkedHashMap<>();
            for (var component : record.getClass().getRecordComponents()) {
                try {
                    fields.put(component.getName(), component.getAccessor().invoke(record));
                } catch (ReflectiveOperationException ignored) {
                    fields.put(component.getName(), null);
                }
            }
            return pretty(fields, indent);
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder out = new StringBuilder("{\n");
            String pad = " ".repeat(indent + 2);
            Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<?, ?> entry = iterator.next();
                out.append(pad).append('"').append(entry.getKey()).append("\": ").append(pretty(entry.getValue(), indent + 2));
                out.append(iterator.hasNext() ? ",\n" : "\n");
            }
            return out.append(" ".repeat(indent)).append("}").toString();
        }
        if (value instanceof Iterable<?> iterable) {
            StringBuilder out = new StringBuilder("[\n");
            String pad = " ".repeat(indent + 2);
            Iterator<?> iterator = iterable.iterator();
            while (iterator.hasNext()) {
                out.append(pad).append(pretty(iterator.next(), indent + 2));
                out.append(iterator.hasNext() ? ",\n" : "\n");
            }
            return out.append(" ".repeat(indent)).append("]").toString();
        }
        return "\"" + value + "\"";
    }

    private String uuid() {
        return UUID.randomUUID().toString();
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String blankToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasQualityIssue(String notes) {
        String value = notes == null ? "" : notes;
        boolean badCondition = value.contains("Tình trạng=") && !value.contains("Tình trạng=Đạt");
        boolean badPackaging = value.contains("Bao bì=") && !value.contains("Bao bì=Nguyên vẹn");
        return badCondition || badPackaging;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private void requireText(String value, String message) {
        if (!hasText(value)) {
            throw badRequest(message);
        }
    }

    private DatabaseException db(SQLException e) {
        return new DatabaseException(500, "Loi PostgreSQL: " + e.getMessage());
    }

    private DatabaseException badRequest(String message) {
        return new DatabaseException(400, message);
    }

    private DatabaseException unauthorized(String message) {
        return new DatabaseException(401, message);
    }

    private DatabaseException notFound(String message) {
        return new DatabaseException(404, message);
    }

    private record UserWithPassword(UserResponse response, String password) {
    }

    @FunctionalInterface
    private interface SqlWork<T> {
        T run(Connection conn) throws SQLException;
    }

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
