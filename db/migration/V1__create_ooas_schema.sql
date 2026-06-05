CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    employee_id VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    site_id VARCHAR(36),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE skus (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE sites (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    sea_lead_time INTEGER NOT NULL,
    air_lead_time INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE site_inventories (
    id VARCHAR(36) PRIMARY KEY,
    site_id VARCHAR(36) NOT NULL REFERENCES sites(id) ON DELETE CASCADE,
    sku_id VARCHAR(36) NOT NULL REFERENCES skus(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_site_inventory_site_sku UNIQUE (site_id, sku_id)
);

CREATE TABLE order_requests (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    expected_date DATE NOT NULL,
    notes TEXT,
    status VARCHAR(32) NOT NULL,
    cancel_reason TEXT,
    created_by_id VARCHAR(36) NOT NULL REFERENCES users(id),
    processed_by_id VARCHAR(36) REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE order_request_items (
    id VARCHAR(36) PRIMARY KEY,
    request_id VARCHAR(36) NOT NULL REFERENCES order_requests(id) ON DELETE CASCADE,
    sku_id VARCHAR(36) NOT NULL REFERENCES skus(id),
    quantity INTEGER NOT NULL,
    expected_date DATE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_order_request_item_request_sku UNIQUE (request_id, sku_id)
);

CREATE TABLE site_inquiries (
    id VARCHAR(36) PRIMARY KEY,
    request_id VARCHAR(36) NOT NULL REFERENCES order_requests(id) ON DELETE CASCADE,
    site_id VARCHAR(36) NOT NULL REFERENCES sites(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE site_inquiry_items (
    id VARCHAR(36) PRIMARY KEY,
    inquiry_id VARCHAR(36) NOT NULL REFERENCES site_inquiries(id) ON DELETE CASCADE,
    sku_id VARCHAR(36) NOT NULL REFERENCES skus(id) ON DELETE CASCADE,
    quantity_requested INTEGER NOT NULL,
    quantity_available INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_site_inquiry_item_inquiry_sku UNIQUE (inquiry_id, sku_id)
);

CREATE TABLE purchase_orders (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    request_id VARCHAR(36) NOT NULL REFERENCES order_requests(id),
    site_id VARCHAR(36) NOT NULL REFERENCES sites(id),
    created_by_id VARCHAR(36) NOT NULL REFERENCES users(id),
    transport_method VARCHAR(16) NOT NULL,
    status VARCHAR(32) NOT NULL,
    expected_arrival_date DATE NOT NULL,
    actual_arrival_date DATE,
    cancel_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE purchase_order_items (
    id VARCHAR(36) PRIMARY KEY,
    purchase_order_id VARCHAR(36) NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    sku_id VARCHAR(36) NOT NULL REFERENCES skus(id),
    quantity_ordered INTEGER NOT NULL,
    quantity_received INTEGER NOT NULL DEFAULT 0,
    difference INTEGER NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_purchase_order_item_po_sku UNIQUE (purchase_order_id, sku_id)
);

CREATE TABLE shipment_trackings (
    id VARCHAR(36) PRIMARY KEY,
    purchase_order_id VARCHAR(36) NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    location VARCHAR(255),
    notes TEXT,
    evidence_file_url VARCHAR(500),
    updated_by_id VARCHAR(36) NOT NULL REFERENCES users(id),
    timestamp TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_site_inventories_sku ON site_inventories(sku_id);
CREATE INDEX idx_order_requests_status ON order_requests(status);
CREATE INDEX idx_purchase_orders_status ON purchase_orders(status);
CREATE INDEX idx_purchase_orders_site ON purchase_orders(site_id);
CREATE INDEX idx_shipment_trackings_po ON shipment_trackings(purchase_order_id);
CREATE INDEX idx_site_inquiries_request ON site_inquiries(request_id);

ALTER TABLE users ADD CONSTRAINT fk_users_site FOREIGN KEY (site_id) REFERENCES sites(id) ON DELETE SET NULL;

