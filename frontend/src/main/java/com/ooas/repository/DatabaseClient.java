package com.ooas.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ooas.exception.DatabaseException;
import com.ooas.model.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DatabaseClient {
    private String apiBaseUrl;
    private String token;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DatabaseClient(String apiBaseUrl) {
        setBaseUrl(apiBaseUrl);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public void setBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl != null && !apiBaseUrl.isBlank() ? apiBaseUrl.trim() : "http://localhost:8080/api";
        if (this.apiBaseUrl.endsWith("/")) {
            this.apiBaseUrl = this.apiBaseUrl.substring(0, this.apiBaseUrl.length() - 1);
        }
    }

    public String baseUrl() {
        return apiBaseUrl;
    }

    public void setDatabaseCredentials(String username, String password) {
        // Ignored in REST API mode
    }

    public String dbUsername() {
        return "";
    }

    public String dbPassword() {
        return "";
    }

    public void setToken(String token) {
        this.token = token;
    }

    // Auth & Users
    public CompletableFuture<AuthResponse> login(String email, String password) {
        Map<String, String> body = Map.of("email", email, "password", password);
        return post("/auth/login", body, new TypeReference<AuthResponse>() {}).thenApply(res -> {
            this.token = res.accessToken() != null ? res.accessToken() : (res.user() != null ? res.user().id() : null);
            return res;
        });
    }

    public CompletableFuture<Map<String, Object>> register(RegisterRequest request) {
        return post("/auth/register", request, new TypeReference<Map<String, Object>>() {});
    }

    public CompletableFuture<UserResponse> profile() {
        return get("/auth/profile", new TypeReference<UserResponse>() {});
    }

    public CompletableFuture<Map<String, Object>> updateProfile(String fullName, String employeeId) {
        return put("/auth/profile", Map.of("fullName", fullName, "employeeId", employeeId), new TypeReference<Map<String, Object>>() {});
    }

    public CompletableFuture<Map<String, Object>> changePassword(String oldPassword, String newPassword) {
        return put("/auth/password", Map.of("oldPassword", oldPassword, "newPassword", newPassword), new TypeReference<Map<String, Object>>() {});
    }

    public CompletableFuture<List<UserResponse>> listUsers(AccountStatus status, String search) {
        String query = buildQuery("status", status, "search", search);
        return get("/admin/users" + query, new TypeReference<List<UserResponse>>() {});
    }

    public CompletableFuture<Map<String, Object>> approveUser(String id) {
        return patch("/admin/users/" + id + "/approve", null, new TypeReference<Map<String, Object>>() {});
    }

    public CompletableFuture<Map<String, Object>> blockUser(String id) {
        return patch("/admin/users/" + id + "/block", null, new TypeReference<Map<String, Object>>() {});
    }

    public CompletableFuture<Map<String, Object>> updateRole(String id, Role role) {
        return patch("/admin/users/" + id + "/role", Map.of("role", role), new TypeReference<Map<String, Object>>() {});
    }

    public CompletableFuture<Map<String, Long>> dashboardSummary() {
        return get("/dashboard/summary", new TypeReference<Map<String, Long>>() {});
    }

    // SKUs
    public CompletableFuture<List<SkuResponse>> listSkus(String search) {
        String query = buildQuery("search", search);
        return get("/skus" + query, new TypeReference<List<SkuResponse>>() {});
    }

    public CompletableFuture<SkuResponse> createSku(SkuRequest request) {
        return post("/skus", request, new TypeReference<SkuResponse>() {});
    }

    public CompletableFuture<SkuResponse> updateSku(String id, SkuRequest request) {
        return put("/skus/" + id, request, new TypeReference<SkuResponse>() {});
    }

    // Sites & Inventory
    public CompletableFuture<List<SiteResponse>> listSites(Boolean active, String search) {
        String query = buildQuery("active", active, "search", search);
        return get("/sites" + query, new TypeReference<List<SiteResponse>>() {});
    }

    public CompletableFuture<SiteResponse> createSite(SiteRequest request) {
        return post("/sites", request, new TypeReference<SiteResponse>() {});
    }

    public CompletableFuture<SiteResponse> updateSite(String id, SiteRequest request) {
        return put("/sites/" + id, request, new TypeReference<SiteResponse>() {});
    }

    public CompletableFuture<List<InventoryResponse>> siteInventory(String siteId) {
        return get("/sites/" + siteId + "/inventory", new TypeReference<List<InventoryResponse>>() {});
    }

    public CompletableFuture<InventoryResponse> upsertInventory(String siteId, String skuId, int quantity) {
        return put("/sites/" + siteId + "/inventory/" + skuId, Map.of("quantity", quantity), new TypeReference<InventoryResponse>() {});
    }

    // Order Requests
    public CompletableFuture<List<OrderRequestResponse>> listOrderRequests(RequestStatus status, String search) {
        String query = buildQuery("status", status, "search", search);
        return get("/order-requests" + query, new TypeReference<List<OrderRequestResponse>>() {});
    }

    public CompletableFuture<OrderRequestResponse> orderRequestDetail(String id) {
        return get("/order-requests/" + id, new TypeReference<OrderRequestResponse>() {});
    }

    public CompletableFuture<OrderRequestResponse> createOrderRequest(OrderRequestUpsertRequest request) {
        return post("/order-requests", request, new TypeReference<OrderRequestResponse>() {});
    }

    public CompletableFuture<OrderRequestResponse> updateOrderRequest(String id, OrderRequestUpsertRequest request) {
        return put("/order-requests/" + id, request, new TypeReference<OrderRequestResponse>() {});
    }

    public CompletableFuture<OrderRequestResponse> submitOrderRequest(String id) {
        return patch("/order-requests/" + id + "/submit", null, new TypeReference<OrderRequestResponse>() {});
    }

    public CompletableFuture<OrderRequestResponse> cancelOrderRequest(String id, String reason) {
        return patch("/order-requests/" + id + "/cancel", Map.of("reason", reason), new TypeReference<OrderRequestResponse>() {});
    }

    public CompletableFuture<InventoryCheckResponse> inventoryCheck(String requestId) {
        return get("/order-requests/" + requestId + "/inventory-check", new TypeReference<InventoryCheckResponse>() {});
    }

    public CompletableFuture<OptimizationResponse> optimize(String requestId) {
        return post("/order-requests/" + requestId + "/optimize", null, new TypeReference<OptimizationResponse>() {});
    }

    // Purchase Orders
    public CompletableFuture<List<PurchaseOrderResponse>> listPurchaseOrders(POStatus status, String siteId, String search) {
        String query = buildQuery("status", status, "siteId", siteId, "search", search);
        return get("/purchase-orders" + query, new TypeReference<List<PurchaseOrderResponse>>() {});
    }

    public CompletableFuture<PurchaseOrderResponse> purchaseOrderDetail(String id) {
        return get("/purchase-orders/" + id, new TypeReference<PurchaseOrderResponse>() {});
    }

    public CompletableFuture<List<PurchaseOrderResponse>> generatePurchaseOrders(String requestId) {
        return post("/order-requests/" + requestId + "/purchase-orders", null, new TypeReference<List<PurchaseOrderResponse>>() {});
    }

    public CompletableFuture<PurchaseOrderResponse> updatePurchaseOrderStatus(String id, POStatus status, LocalDate actualArrivalDate) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("status", status);
        if (actualArrivalDate != null) {
            body.put("actualArrivalDate", actualArrivalDate);
        }
        return patch("/purchase-orders/" + id + "/status", body, new TypeReference<PurchaseOrderResponse>() {});
    }

    public CompletableFuture<PurchaseOrderResponse> cancelPurchaseOrder(String id, String reason) {
        return patch("/purchase-orders/" + id + "/cancel", Map.of("reason", reason), new TypeReference<PurchaseOrderResponse>() {});
    }

    public CompletableFuture<List<PurchaseOrderResponse>> inboundPurchaseOrders() {
        return get("/warehouse/inbound", new TypeReference<List<PurchaseOrderResponse>>() {});
    }

    public CompletableFuture<PurchaseOrderResponse> receivePurchaseOrder(String id, ReceivePurchaseOrderRequest request) {
        return post("/warehouse/purchase-orders/" + id + "/receive", request, new TypeReference<PurchaseOrderResponse>() {});
    }

    public CompletableFuture<List<PurchaseOrderResponse>> inTransitShipments() {
        return get("/shipments/in-transit", new TypeReference<List<PurchaseOrderResponse>>() {});
    }

    public CompletableFuture<List<TrackingResponse>> trackingHistory(String purchaseOrderId) {
        return get("/shipments/purchase-orders/" + purchaseOrderId + "/tracking", new TypeReference<List<TrackingResponse>>() {});
    }

    public CompletableFuture<TrackingResponse> addTracking(String purchaseOrderId, TrackingRequest request) {
        return post("/shipments/purchase-orders/" + purchaseOrderId + "/tracking", request, new TypeReference<TrackingResponse>() {});
    }

    // Utility methods
    public String toPrettyJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    // HTTP Helpers
    private <T> CompletableFuture<T> get(String path, TypeReference<T> responseType) {
        HttpRequest request = requestBuilder(path).GET().build();
        return sendAsync(request, responseType);
    }

    private <T> CompletableFuture<T> post(String path, Object body, TypeReference<T> responseType) {
        HttpRequest request = requestBuilder(path)
                .header("Content-Type", "application/json")
                .POST(bodyPublisher(body))
                .build();
        return sendAsync(request, responseType);
    }

    private <T> CompletableFuture<T> put(String path, Object body, TypeReference<T> responseType) {
        HttpRequest request = requestBuilder(path)
                .header("Content-Type", "application/json")
                .PUT(bodyPublisher(body))
                .build();
        return sendAsync(request, responseType);
    }

    private <T> CompletableFuture<T> patch(String path, Object body, TypeReference<T> responseType) {
        HttpRequest request = requestBuilder(path)
                .header("Content-Type", "application/json")
                .method("PATCH", bodyPublisher(body))
                .build();
        return sendAsync(request, responseType);
    }

    private HttpRequest.Builder requestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(apiBaseUrl + path));
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private HttpRequest.BodyPublisher bodyPublisher(Object body) {
        if (body == null) {
            return HttpRequest.BodyPublishers.noBody();
        }
        try {
            String json = objectMapper.writeValueAsString(body);
            return HttpRequest.BodyPublishers.ofString(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
    }

    private <T> CompletableFuture<T> sendAsync(HttpRequest request, TypeReference<T> responseType) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        try {
                            Map<String, Object> error = objectMapper.readValue(response.body(), new TypeReference<>() {});
                            throw new DatabaseException(response.statusCode(), String.valueOf(error.getOrDefault("message", "Loi tu server: " + response.statusCode())));
                        } catch (Exception e) {
                            throw new DatabaseException(response.statusCode(), "Loi tu server: " + response.statusCode());
                        }
                    }
                    try {
                        if (response.body() == null || response.body().isBlank()) {
                            return null;
                        }
                        return objectMapper.readValue(response.body(), responseType);
                    } catch (Exception e) {
                        throw new DatabaseException(500, "Loi parse du lieu: " + e.getMessage());
                    }
                });
    }

    private String buildQuery(Object... params) {
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < params.length; i += 2) {
            String key = (String) params[i];
            Object value = params[i + 1];
            if (value != null && !String.valueOf(value).isBlank()) {
                if (query.isEmpty()) {
                    query.append("?");
                } else {
                    query.append("&");
                }
                query.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8));
            }
        }
        return query.toString();
    }
}
