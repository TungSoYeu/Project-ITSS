-- Comprehensive, repeatable test data for the OOAS demo.
-- Run manually with:
-- docker exec -i ooas-postgres psql -U postgres -d ooas < db/migration/V2__seed_test_data.sql

DO $$
DECLARE
    admin_id VARCHAR(36);
    test_index INTEGER;
    test_status VARCHAR(32);
    request_id VARCHAR(36);
    purchase_order_id VARCHAR(36);
    purchase_order_item_id VARCHAR(36);
    test_site_id VARCHAR(36);
    first_sku_id VARCHAR(36);
    second_sku_id VARCHAR(36);
    ordered_quantity INTEGER;
    received_quantity INTEGER;
    inspection_notes TEXT;
    -- Password for all seeded test accounts: warehouse123
    test_password VARCHAR(255) := '$2a$10$jVd5r043RZUEBzPs1l0uHuMu5IEmxfL7k4jUDQxvaQK3cePbPXvEq';
BEGIN
    SELECT id
    INTO admin_id
    FROM users
    WHERE role = 'ADMIN'
    ORDER BY created_at
    LIMIT 1;

    IF admin_id IS NULL THEN
        RAISE EXCEPTION 'Create an ADMIN account before loading test data';
    END IF;

    INSERT INTO users (id, email, password, full_name, employee_id, role, status, created_at, updated_at)
    VALUES
        ('10000000-0000-0000-0000-000000000001', 'sales.test@ooas.local', test_password, 'Nguyen Minh Sales', 'TEST-SALES', 'SALES', 'APPROVED', NOW(), NOW()),
        ('10000000-0000-0000-0000-000000000002', 'overseas.test@ooas.local', test_password, 'Tran Lan Overseas', 'TEST-OVERSEAS', 'OVERSEAS_ORDER', 'APPROVED', NOW(), NOW()),
        ('10000000-0000-0000-0000-000000000003', 'warehouse.test@ooas.local', test_password, 'Le Nam Warehouse', 'TEST-WAREHOUSE', 'WAREHOUSE', 'APPROVED', NOW(), NOW()),
        ('10000000-0000-0000-0000-000000000004', 'supplier.test@ooas.local', test_password, 'Pham Mai Supplier', 'TEST-SUPPLIER', 'SUPPLIER', 'APPROVED', NOW(), NOW()),
        ('10000000-0000-0000-0000-000000000005', 'pending.test@ooas.local', test_password, 'Tai Khoan Cho Duyet', 'TEST-PENDING', 'SALES', 'PENDING', NOW(), NOW()),
        ('10000000-0000-0000-0000-000000000006', 'blocked.test@ooas.local', test_password, 'Tai Khoan Bi Khoa', 'TEST-BLOCKED', 'SALES', 'BLOCKED', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

    UPDATE users
    SET password = test_password,
        updated_at = NOW()
    WHERE employee_id LIKE 'TEST-%';

    INSERT INTO skus (id, code, name, unit, description, created_at, updated_at)
    VALUES
        ('20000000-0000-0000-0000-000000000001', 'LAPTOP-PRO-14', 'Laptop Pro 14 inch', 'cai', 'Laptop van phong cao cap', NOW(), NOW()),
        ('20000000-0000-0000-0000-000000000002', 'MONITOR-27', 'Man hinh 27 inch', 'cai', 'Man hinh IPS QHD', NOW(), NOW()),
        ('20000000-0000-0000-0000-000000000003', 'KEYBOARD-MECH', 'Ban phim co', 'cai', 'Ban phim co ket noi USB', NOW(), NOW()),
        ('20000000-0000-0000-0000-000000000004', 'MOUSE-WL', 'Chuot khong day', 'cai', 'Chuot khong day van phong', NOW(), NOW()),
        ('20000000-0000-0000-0000-000000000005', 'HEADSET-USB', 'Tai nghe USB', 'cai', 'Tai nghe hop truc tuyen', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO sites (id, code, name, country, sea_lead_time, air_lead_time, active, created_at, updated_at)
    VALUES
        ('30000000-0000-0000-0000-000000000001', 'JP-TOKYO', 'Tokyo Distribution Center', 'Japan', 18, 4, TRUE, NOW(), NOW()),
        ('30000000-0000-0000-0000-000000000002', 'SG-SIN', 'Singapore Regional Hub', 'Singapore', 10, 3, TRUE, NOW(), NOW()),
        ('30000000-0000-0000-0000-000000000003', 'US-LAX', 'Los Angeles Warehouse', 'United States', 28, 7, TRUE, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO site_inventories (id, site_id, sku_id, quantity, created_at, updated_at)
    VALUES
        ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 80, NOW(), NOW()),
        ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', 140, NOW(), NOW()),
        ('40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000003', 240, NOW(), NOW()),
        ('40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', 60, NOW(), NOW()),
        ('40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000004', 300, NOW(), NOW()),
        ('40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000005', 180, NOW(), NOW()),
        ('40000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002', 90, NOW(), NOW()),
        ('40000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 160, NOW(), NOW()),
        ('40000000-0000-0000-0000-000000000009', '30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000005', 110, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO order_requests (id, code, expected_date, notes, status, cancel_reason, created_by_id, processed_by_id, created_at, updated_at)
    VALUES
        ('50000000-0000-0000-0000-000000000001', 'YC-TEST-DRAFT', CURRENT_DATE + 30, 'Yeu cau nhap dang soan thao', 'DRAFT', NULL, admin_id, NULL, NOW() - INTERVAL '6 days', NOW()),
        ('50000000-0000-0000-0000-000000000002', 'YC-TEST-PENDING', CURRENT_DATE + 25, 'Yeu cau dang cho xu ly', 'PENDING', NULL, admin_id, NULL, NOW() - INTERVAL '5 days', NOW()),
        ('50000000-0000-0000-0000-000000000003', 'YC-TEST-PROCESSING', CURRENT_DATE + 20, 'Yeu cau dang toi uu nguon cung', 'PROCESSING', NULL, admin_id, '10000000-0000-0000-0000-000000000002', NOW() - INTERVAL '4 days', NOW()),
        ('50000000-0000-0000-0000-000000000004', 'YC-TEST-ORDERED-A', CURRENT_DATE + 15, 'Yeu cau da tao nhieu PO', 'ORDERED', NULL, admin_id, '10000000-0000-0000-0000-000000000002', NOW() - INTERVAL '12 days', NOW()),
        ('50000000-0000-0000-0000-000000000005', 'YC-TEST-ORDERED-B', CURRENT_DATE + 10, 'Yeu cau co PO da hoan tat', 'ORDERED', NULL, admin_id, '10000000-0000-0000-0000-000000000002', NOW() - INTERVAL '20 days', NOW()),
        ('50000000-0000-0000-0000-000000000006', 'YC-TEST-CANCELLED', CURRENT_DATE + 35, 'Yeu cau da huy de test', 'CANCELLED', 'Khach hang thay doi ke hoach', admin_id, NULL, NOW() - INTERVAL '3 days', NOW()),
        ('50000000-0000-0000-0000-000000000007', 'YC-TEST-ORDERED-C', CURRENT_DATE + 18, 'Yeu cau them don hang test', 'ORDERED', NULL, admin_id, '10000000-0000-0000-0000-000000000002', NOW() - INTERVAL '11 days', NOW()),
        ('50000000-0000-0000-0000-000000000008', 'YC-TEST-ORDERED-D', CURRENT_DATE + 12, 'Yeu cau test them don hang', 'ORDERED', NULL, admin_id, '10000000-0000-0000-0000-000000000002', NOW() - INTERVAL '9 days', NOW())
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO order_request_items (id, request_id, sku_id, quantity, expected_date, created_at, updated_at)
    VALUES
        ('60000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 10, CURRENT_DATE + 30, NOW(), NOW()),
        ('60000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000004', 25, CURRENT_DATE + 30, NOW(), NOW()),
        ('60000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 20, CURRENT_DATE + 25, NOW(), NOW()),
        ('60000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 30, CURRENT_DATE + 20, NOW(), NOW()),
        ('60000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', 15, CURRENT_DATE + 15, NOW(), NOW()),
        ('60000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 40, CURRENT_DATE + 15, NOW(), NOW()),
        ('60000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002', 12, CURRENT_DATE + 10, NOW(), NOW()),
        ('60000000-0000-0000-0000-000000000008', '50000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 24, CURRENT_DATE + 10, NOW(), NOW()),
        ('60000000-0000-0000-0000-000000000009', '50000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000005', 50, CURRENT_DATE + 35, NOW(), NOW()),
        ('60000000-0000-0000-0000-000000000010', '50000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000003', 18, CURRENT_DATE + 18, NOW(), NOW()),
        ('60000000-0000-0000-0000-000000000011', '50000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000005', 36, CURRENT_DATE + 18, NOW(), NOW()),
        ('60000000-0000-0000-0000-000000000012', '50000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000001', 22, CURRENT_DATE + 12, NOW(), NOW()),
        ('60000000-0000-0000-0000-000000000013', '50000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000002', 14, CURRENT_DATE + 12, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO purchase_orders (id, code, request_id, site_id, created_by_id, transport_method, status, expected_arrival_date, actual_arrival_date, cancel_reason, created_at, updated_at)
    VALUES
        ('70000000-0000-0000-0000-000000000001', 'PO-TEST-PENDING', '50000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000001', admin_id, 'SEA', 'PENDING_CONFIRM', CURRENT_DATE + 15, NULL, NULL, NOW() - INTERVAL '9 days', NOW()),
        ('70000000-0000-0000-0000-000000000002', 'PO-TEST-PREPARING', '50000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000002', admin_id, 'AIR', 'PREPARING', CURRENT_DATE + 7, NULL, NULL, NOW() - INTERVAL '8 days', NOW()),
        ('70000000-0000-0000-0000-000000000003', 'PO-TEST-SHIPPING', '50000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000002', admin_id, 'SEA', 'SHIPPING', CURRENT_DATE + 5, NULL, NULL, NOW() - INTERVAL '7 days', NOW()),
        ('70000000-0000-0000-0000-000000000004', 'PO-TEST-ARRIVED', '50000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000003', admin_id, 'AIR', 'ARRIVED', CURRENT_DATE - 1, CURRENT_DATE, NULL, NOW() - INTERVAL '15 days', NOW()),
        ('70000000-0000-0000-0000-000000000005', 'PO-TEST-COMPLETED', '50000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000001', admin_id, 'SEA', 'COMPLETED', CURRENT_DATE - 5, CURRENT_DATE - 4, NULL, NOW() - INTERVAL '20 days', NOW()),
        ('70000000-0000-0000-0000-000000000006', 'PO-TEST-CANCELLED', '50000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000003', admin_id, 'AIR', 'CANCELLED', CURRENT_DATE + 3, NULL, 'Nha cung cap het hang', NOW() - INTERVAL '6 days', NOW()),
        ('70000000-0000-0000-0000-000000000007', 'PO-TEST-SHIPPING-2', '50000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000003', admin_id, 'SEA', 'SHIPPING', CURRENT_DATE + 6, NULL, NULL, NOW() - INTERVAL '5 days', NOW()),
        ('70000000-0000-0000-0000-000000000008', 'PO-TEST-ARRIVED-2', '50000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000002', admin_id, 'AIR', 'ARRIVED', CURRENT_DATE - 2, CURRENT_DATE - 1, NULL, NOW() - INTERVAL '10 days', NOW()),
        ('70000000-0000-0000-0000-000000000009', 'PO-TEST-COMPLETED-2', '50000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000001', admin_id, 'AIR', 'COMPLETED', CURRENT_DATE - 7, CURRENT_DATE - 5, NULL, NOW() - INTERVAL '16 days', NOW())
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO purchase_order_items (id, purchase_order_id, sku_id, quantity_ordered, quantity_received, difference, notes, created_at, updated_at)
    VALUES
        ('80000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 15, 0, 0, NULL, NOW(), NOW()),
        ('80000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000004', 20, 0, 0, NULL, NOW(), NOW()),
        ('80000000-0000-0000-0000-000000000003', '70000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000004', 20, 0, 0, NULL, NOW(), NOW()),
        ('80000000-0000-0000-0000-000000000004', '70000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002', 12, 0, 0, NULL, NOW(), NOW()),
        ('80000000-0000-0000-0000-000000000005', '70000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 24, 23, -1, 'Thieu 1 san pham khi kiem dem', NOW(), NOW()),
        ('80000000-0000-0000-0000-000000000006', '70000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000003', 10, 0, 0, NULL, NOW(), NOW()),
        ('80000000-0000-0000-0000-000000000007', '70000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000004', 18, 0, 0, NULL, NOW(), NOW()),
        ('80000000-0000-0000-0000-000000000008', '70000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000005', 42, 0, 0, NULL, NOW(), NOW()),
        ('80000000-0000-0000-0000-000000000009', '70000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000001', 22, 21, -1, 'Thieu 1 san pham tren kho', NOW(), NOW()),
        ('80000000-0000-0000-0000-000000001000', '70000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000002', 14, 16, 2, 'Thua 2 san pham', NOW(), NOW()),
        ('80000000-0000-0000-0000-000000001001', '70000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000003', 8, 8, 0, NULL, NOW(), NOW()),
        ('80000000-0000-0000-0000-000000001002', '70000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000001', 26, 27, 1, 'Thua 1 san pham', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

    -- Twenty additional purchase orders dedicated to testing the warehouse inspection use case.
    FOR test_index IN 1..20 LOOP
        request_id := 'a1000000-0000-0000-0000-' || LPAD(test_index::TEXT, 12, '0');
        purchase_order_id := 'b1000000-0000-0000-0000-' || LPAD(test_index::TEXT, 12, '0');
        test_site_id := CASE MOD(test_index, 3)
            WHEN 1 THEN '30000000-0000-0000-0000-000000000001'
            WHEN 2 THEN '30000000-0000-0000-0000-000000000002'
            ELSE '30000000-0000-0000-0000-000000000003'
        END;
        first_sku_id := CASE MOD(test_index, 5)
            WHEN 1 THEN '20000000-0000-0000-0000-000000000001'
            WHEN 2 THEN '20000000-0000-0000-0000-000000000002'
            WHEN 3 THEN '20000000-0000-0000-0000-000000000003'
            WHEN 4 THEN '20000000-0000-0000-0000-000000000004'
            ELSE '20000000-0000-0000-0000-000000000005'
        END;
        second_sku_id := CASE MOD(test_index + 2, 5)
            WHEN 1 THEN '20000000-0000-0000-0000-000000000001'
            WHEN 2 THEN '20000000-0000-0000-0000-000000000002'
            WHEN 3 THEN '20000000-0000-0000-0000-000000000003'
            WHEN 4 THEN '20000000-0000-0000-0000-000000000004'
            ELSE '20000000-0000-0000-0000-000000000005'
        END;
        test_status := CASE
            WHEN test_index <= 8 THEN 'SHIPPING'
            WHEN test_index <= 14 THEN 'ARRIVED'
            ELSE 'COMPLETED'
        END;

        INSERT INTO order_requests (id, code, expected_date, notes, status, cancel_reason, created_by_id, processed_by_id, created_at, updated_at)
        VALUES (
            request_id,
            'YC-WMS-' || LPAD(test_index::TEXT, 3, '0'),
            CURRENT_DATE + (test_index - 10),
            'Yeu cau du lieu bo sung de kiem thu nghiep vu kiem hang',
            'ORDERED',
            NULL,
            admin_id,
            '10000000-0000-0000-0000-000000000002',
            NOW() - (test_index || ' days')::INTERVAL,
            NOW()
        )
        ON CONFLICT (id) DO NOTHING;

        INSERT INTO purchase_orders (id, code, request_id, site_id, created_by_id, transport_method, status, expected_arrival_date, actual_arrival_date, cancel_reason, created_at, updated_at)
        VALUES (
            purchase_order_id,
            'PO-WMS-' || LPAD(test_index::TEXT, 3, '0'),
            request_id,
            test_site_id,
            admin_id,
            CASE WHEN MOD(test_index, 2) = 0 THEN 'AIR' ELSE 'SEA' END,
            test_status,
            CURRENT_DATE + (test_index - 10),
            CASE WHEN test_status IN ('ARRIVED', 'COMPLETED') THEN CURRENT_DATE - MOD(test_index, 4) ELSE NULL END,
            NULL,
            NOW() - (test_index || ' days')::INTERVAL,
            NOW()
        )
        ON CONFLICT (id) DO NOTHING;

        ordered_quantity := 10 + test_index;
        received_quantity := CASE
            WHEN test_status <> 'COMPLETED' THEN 0
            WHEN MOD(test_index, 3) = 0 THEN ordered_quantity - 2
            WHEN MOD(test_index, 3) = 1 THEN ordered_quantity + 1
            ELSE ordered_quantity
        END;
        inspection_notes := CASE
            WHEN test_status <> 'COMPLETED' THEN NULL
            WHEN MOD(test_index, 3) = 0 THEN 'Kiểm kê: Tình trạng=Đạt; Bao bì=Nguyên vẹn; Mã lô/Serial=LOT-WMS-' || test_index || '-A; HSD=; Ghi chú kiểm kê=; Lý do=Site giao thiếu; Xử lý=Yêu cầu Site bổ sung; Ghi chú xử lý=Đã lập biên bản thiếu 2 sản phẩm'
            WHEN MOD(test_index, 3) = 1 THEN 'Kiểm kê: Tình trạng=Đạt; Bao bì=Nguyên vẹn; Mã lô/Serial=LOT-WMS-' || test_index || '-A; HSD=; Ghi chú kiểm kê=; Lý do=Site giao thừa; Xử lý=Trả lại hàng thừa; Ghi chú xử lý=Đã ghi nhận thừa 1 sản phẩm'
            ELSE 'Kiểm kê: Tình trạng=Đạt; Bao bì=Nguyên vẹn; Mã lô/Serial=LOT-WMS-' || test_index || '-A; HSD=; Ghi chú kiểm kê=Hàng đạt yêu cầu'
        END;
        purchase_order_item_id := 'c1000000-0000-0000-0000-' || LPAD((test_index * 2 - 1)::TEXT, 12, '0');

        INSERT INTO purchase_order_items (id, purchase_order_id, sku_id, quantity_ordered, quantity_received, difference, notes, created_at, updated_at)
        VALUES (
            purchase_order_item_id,
            purchase_order_id,
            first_sku_id,
            ordered_quantity,
            received_quantity,
            CASE WHEN test_status = 'COMPLETED' THEN received_quantity - ordered_quantity ELSE 0 END,
            inspection_notes,
            NOW(),
            NOW()
        )
        ON CONFLICT (id) DO NOTHING;

        ordered_quantity := 20 + test_index * 2;
        received_quantity := CASE
            WHEN test_status <> 'COMPLETED' THEN 0
            WHEN MOD(test_index, 4) = 0 THEN ordered_quantity - 1
            ELSE ordered_quantity
        END;
        inspection_notes := CASE
            WHEN test_status <> 'COMPLETED' THEN NULL
            WHEN MOD(test_index, 4) = 0 THEN 'Kiểm kê: Tình trạng=Hư hỏng; Bao bì=Móp/vỡ; Mã lô/Serial=LOT-WMS-' || test_index || '-B; HSD=; Ghi chú kiểm kê=Phát hiện một sản phẩm hư hỏng; Lý do=Hư hỏng trong vận chuyển; Xử lý=Lập biên bản sai lệch; Ghi chú xử lý=Chờ Site phản hồi'
            ELSE 'Kiểm kê: Tình trạng=Đạt; Bao bì=Nguyên vẹn; Mã lô/Serial=LOT-WMS-' || test_index || '-B; HSD=; Ghi chú kiểm kê=Hàng đạt yêu cầu'
        END;
        purchase_order_item_id := 'c1000000-0000-0000-0000-' || LPAD((test_index * 2)::TEXT, 12, '0');

        INSERT INTO purchase_order_items (id, purchase_order_id, sku_id, quantity_ordered, quantity_received, difference, notes, created_at, updated_at)
        VALUES (
            purchase_order_item_id,
            purchase_order_id,
            second_sku_id,
            ordered_quantity,
            received_quantity,
            CASE WHEN test_status = 'COMPLETED' THEN received_quantity - ordered_quantity ELSE 0 END,
            inspection_notes,
            NOW(),
            NOW()
        )
        ON CONFLICT (id) DO NOTHING;
    END LOOP;

    INSERT INTO shipment_trackings (id, purchase_order_id, status, location, notes, evidence_file_url, updated_by_id, timestamp, created_at, updated_at)
    VALUES
        ('90000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000003', 'PREPARING', 'Singapore Regional Hub', 'Da dong goi hang', NULL, '10000000-0000-0000-0000-000000000004', NOW() - INTERVAL '4 days', NOW(), NOW()),
        ('90000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000003', 'SHIPPING', 'Cang Singapore', 'Tau da khoi hanh', 'https://example.com/evidence/shipping.jpg', '10000000-0000-0000-0000-000000000004', NOW() - INTERVAL '2 days', NOW(), NOW()),
        ('90000000-0000-0000-0000-000000000003', '70000000-0000-0000-0000-000000000004', 'SHIPPING', 'Los Angeles Airport', 'Hang dang van chuyen bang duong hang khong', NULL, '10000000-0000-0000-0000-000000000004', NOW() - INTERVAL '3 days', NOW(), NOW()),
        ('90000000-0000-0000-0000-000000000004', '70000000-0000-0000-0000-000000000004', 'ARRIVED', 'Kho dich', 'Hang da den va cho nhap kho', 'https://example.com/evidence/arrived.jpg', '10000000-0000-0000-0000-000000000003', NOW() - INTERVAL '2 hours', NOW(), NOW()),
        ('90000000-0000-0000-0000-000000000005', '70000000-0000-0000-0000-000000000005', 'COMPLETED', 'Kho dich', 'Da kiem dem va nhap kho', NULL, '10000000-0000-0000-0000-000000000003', NOW() - INTERVAL '4 days', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;
END $$;
