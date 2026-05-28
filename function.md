# TÀI LIỆU ĐẶC TẢ TÍNH NĂNG CHI TIẾT THEO MÀN HÌNH (FUNCTIONAL SPECIFICATIONS)
## HỆ THỐNG TỰ ĐỘNG HÓA ĐẶT HÀNG QUỐC TẾ (OOAS)

Tài liệu này chi tiết hóa toàn bộ các trường thông tin, chức năng xử lý, quy tắc kiểm tra dữ liệu (validation) và các tương tác người dùng (UI controls) trên 17 màn hình thuộc 8 nhóm chức năng chính của hệ thống OOAS.

---

## NHÓM 1 – XÁC THỰC & TÀI KHOẢN

### MH01. Màn hình Đăng nhập
*   **Mục đích:** Xác thực người dùng truy cập vào hệ thống theo quyền hạn của từng bộ phận.
*   **Trường thông tin nhập liệu:**
    *   `Email` (Bắt buộc, đúng định dạng email).
    *   `Mật khẩu` (Bắt buộc).
*   **Các thành phần điều khiển (UI Controls):**
    *   Nút **Đăng nhập**: Gửi thông tin để hệ thống xác thực.
    *   Link **Quên mật khẩu**: Chuyển tới giao diện khôi phục mật khẩu.
    *   Link **Đăng ký tài khoản**: Chuyển sang màn hình `MH02` nếu chưa có tài khoản.
*   **Quy tắc xử lý (Validation & Business Logic):**
    *   Kiểm tra trống và định dạng email.
    *   Nếu thông tin đăng nhập sai hoặc tài khoản chưa được duyệt/đã bị vô hiệu hóa, hiển thị thông báo lỗi rõ ràng bên dưới form đăng nhập.

---

### MH02. Màn hình Đăng ký tài khoản
*   **Mục đích:** Cho phép nhân viên các bộ phận tự đăng ký tài khoản tham gia hệ thống và chờ Admin duyệt.
*   **Trường thông tin nhập liệu:**
    *   `Họ và tên` (Bắt buộc, không chứa ký tự đặc biệt).
    *   `Email công ty` (Bắt buộc, phải thuộc domain công ty).
    *   `Mật khẩu` (Độ dài từ 8 ký tự, gồm chữ hoa, chữ thường và số).
    *   `Xác nhận mật khẩu` (Phải khớp với mật khẩu đã nhập).
    *   `Bộ phận`: Dropdown lựa chọn (`Bộ phận Bán hàng / Sales`, `Bộ phận Đặt hàng Quốc tế`, `Bộ phận Quản lý Kho`).
    *   `Mã nhân viên` (Bắt buộc, định dạng theo chuẩn công ty).
*   **Các thành phần điều khiển (UI Controls):**
    *   Nút **Gửi đăng ký**: Đẩy dữ liệu đăng ký lên hệ thống.
    *   Link **Quay lại đăng nhập**: Chuyển về màn hình `MH01`.
*   **Quy tắc xử lý (Validation & Business Logic):**
    *   Validate tất cả các trường bắt buộc và định dạng mật khẩu trùng khớp.
    *   Sau khi đăng ký thành công, lưu trạng thái tài khoản là `Chờ duyệt` (Pending) và hiển thị thông báo: *"Đăng ký tài khoản thành công! Vui lòng chờ phê duyệt từ Ban quản trị."*

---

### MH03. Màn hình Danh sách tài khoản & phân quyền (Dành riêng cho Admin)
*   **Mục đích:** Cho phép Quản trị viên hệ thống phê duyệt thành viên mới, phân quyền vai trò và quản lý trạng thái tài khoản.
*   **Trường thông tin hiển thị (Bảng danh sách):**
    *   `Mã tài khoản`, `Họ tên`, `Email`, `Bộ phận`, `Vai trò/Quyền hạn`, `Ngày đăng ký`, `Trạng thái hoạt động` (Hoạt động / Chờ duyệt / Vô hiệu hóa).
*   **Tính năng Tìm kiếm & Lọc:**
    *   Tìm kiếm nhanh theo: `Họ tên` hoặc `Email`.
    *   Bộ lọc (Filter) theo: `Trạng thái` và `Bộ phận`.
*   **Các thành phần điều khiển (UI Controls):**
    *   Nút **Chi tiết tài khoản**: Mở popup/màn hình xem thông tin chi tiết.
    *   Nút **Duyệt / Từ chối**: Phê duyệt nhanh tài khoản ở trạng thái `Chờ duyệt`.
    *   Dropdown **Chỉnh sửa vai trò & Bộ phận**: Thay đổi quyền hạn (Admin / User) và phòng ban hoạt động.
    *   Nút **Vô hiệu hóa tài khoản / Kích hoạt lại**: Thay đổi trạng thái hoạt động của tài khoản.
    *   Nút **Lưu thay đổi**: Lưu lại các chỉnh sửa cấu hình.

---

## NHÓM 2 – TRANG CHỦ

### MH04. Màn hình Trang chủ / Menu chính
*   **Mục đích:** Cổng thông tin trung tâm giúp người dùng xem nhanh trạng thái công việc và điều hướng.
*   **Thành phần hiển thị thông tin cá nhân:**
    *   Hiển thị: `Họ và tên`, `Bộ phận` và `Vai trò` của tài khoản hiện tại.
*   **Hệ thống Menu điều hướng (Dynamic Navigation):**
    *   Hiển thị danh mục tính năng tự động dựa theo bộ phận đăng nhập (ví dụ: Nhân viên Sales chỉ thấy Menu tạo Yêu cầu, Kho chỉ thấy Menu Nhập kho).
*   **Chỉ số thống kê nhanh (Dashboard Widgets):**
    *   `Số Yêu cầu (YC) chờ xử lý` (Liên kết sang MH05).
    *   `Số Đơn hàng đang vận chuyển` (Liên kết sang MH16).
    *   `Số Đơn hàng cần nhập kho` (Liên kết sang MH14).
*   **Bảng tin thông báo:**
    *   Hiển thị danh sách các thông báo mới nhất (ví dụ: *"Có yêu cầu nhập hàng mới từ Sales Dept"*, *"Đơn hàng PO-1002 đã được Site xác nhận"*).
*   **Thao tác điều khiển:**
    *   Nút **Đăng xuất**: Xóa phiên làm việc hiện tại và quay về màn hình Đăng nhập `MH01`.

---

## NHÓM 3 – YÊU CẦU NHẬP HÀNG

### MH05. Màn hình Danh sách yêu cầu nhập hàng
*   **Mục đích:** Quản lý tập trung các yêu cầu nhập hàng do Sales Dept gửi lên.
*   **Trường thông tin hiển thị (Bảng danh sách):**
    *   `Mã Yêu cầu (YC)`, `Ngày tạo`, `Người tạo`, `Số lượng mặt hàng (SKUs)`, `Ngày nhận mong muốn nhất`, `Trạng thái` (Nháp / Chờ xử lý / Đang xử lý / Đã chốt PO / Đã hủy).
*   **Tính năng Tìm kiếm & Lọc:**
    *   Tìm kiếm theo: `Mã YC` hoặc `Mã hàng (SKU)`.
    *   Lọc theo: `Trạng thái yêu cầu`.
    *   Phân trang (Pagination) thông minh.
*   **Các thành phần điều khiển (UI Controls):**
    *   Nút **Tạo yêu cầu mới**: Chuyển hướng sang màn hình `MH06`.
    *   Nút **Xem chi tiết**: Chuyển hướng sang màn hình `MH07`.

---

### MH06. Màn hình Tạo / Sửa yêu cầu nhập hàng
*   **Mục đích:** Nhập liệu các mặt hàng cần cung ứng từ nước ngoài.
*   **Trường thông tin nhập liệu:**
    *   Thông tin chung: `Ngày nhận mong muốn nhất` (Date Picker), `Ghi chú chung` (Textarea).
    *   Bảng danh sách mặt hàng yêu cầu (Dynamic Grid):
        *   `Mã hàng (SKU)` (Dropdown danh mục hoặc ô autocomplete).
        *   `Số lượng` (Input Number).
        *   `Đơn vị tính` (Tự động điền theo SKU).
        *   `Ngày nhận mong muốn riêng của mặt hàng` (Tùy chọn).
*   **Các thành phần điều khiển (UI Controls):**
    *   Nút **Thêm dòng** / **Xóa dòng**: Để bổ sung hoặc bớt SKU trong yêu cầu.
    *   Nút **Lưu nháp**: Lưu thông tin dưới dạng `Nháp` (có thể sửa tiếp).
    *   Nút **Gửi yêu cầu**: Gửi chính thức sang Bộ phận đặt hàng quốc tế (trạng thái chuyển thành `Chờ xử lý`).
    *   Nút **Hủy bỏ**: Quay lại màn hình `MH05` không lưu.
*   **Quy tắc xử lý & Validate dữ liệu:**
    *   Không được phép để trống các trường: SKU, Số lượng, Ngày mong muốn.
    *   Số lượng đặt của mỗi SKU phải là số nguyên lớn hơn 0 (`Qty > 0`).
    *   Ngày nhận mong muốn phải lớn hơn ngày hiện tại (`Expected Date > Current Date`).
    *   **Quyền chỉnh sửa:** Chỉ cho phép sửa nội dung khi trạng thái yêu cầu đang ở mức `Nháp` hoặc `Chờ xử lý`. Khi trạng thái đã chuyển sang `Đang xử lý` hoặc `Đã chốt PO`, hệ thống sẽ khóa toàn bộ trường nhập liệu.

---

### MH07. Màn hình Chi tiết yêu cầu nhập hàng
*   **Mục đích:** Xem toàn bộ lịch sử và nội dung chi tiết của một yêu cầu nhập hàng cụ thể.
*   **Thông tin hiển thị:**
    *   Mã YC, Ngày tạo, Người tạo, Trạng thái hiện tại.
    *   Bảng danh sách mặt hàng: Mã SKU, Tên sản phẩm, Số lượng, Đơn vị tính, Ngày nhận mong muốn.
    *   Lịch sử thay đổi trạng thái (Audit Trail): Ghi rõ ai, thời gian nào đã thay đổi trạng thái (ví dụ: Chờ xử lý -> Đang xử lý bởi Nam).
*   **Các thành phần điều khiển (UI Controls):**
    *   Nút **Chỉnh sửa**: Chuyển sang `MH06` để sửa (chỉ hiện khi ở trạng thái Chờ xử lý).
    *   Nút **Hủy yêu cầu**: Kích hoạt Popup yêu cầu nhập lý do hủy. Sau khi nhập lý do và xác nhận, trạng thái YC chuyển thành `Đã hủy`.

---

## NHÓM 4 – XỬ LÝ TỒN KHO & ĐẶT HÀNG

### MH08. Màn hình Kết quả truy vấn tồn kho
*   **Mục đích:** Hiển thị kết quả đối chiếu cung - cầu tự động của hệ thống đối với danh sách SKU trong yêu cầu.
*   **Thành phần hiển thị thông tin:**
    *   Danh sách các SKU được yêu cầu hiển thị dưới dạng **Thẻ thông tin (Cards)** trực quan.
    *   Mỗi Card hiển thị: `Mã hàng`, `Tên hàng`, `Số lượng Sales yêu cầu`, `Trạng thái khả thi chung` (Đủ số lượng / Thiếu hàng / Cần gom từ nhiều Site).
    *   Bảng chi tiết các **Site có khả năng đáp ứng** bên trong mỗi SKU Card:
        *   Tên Site.
        *   Số lượng tồn kho hiện tại tại Site đó.
        *   Thời gian giao hàng tương ứng: `Đường Tàu` (Sea) vs `Đường Bay` (Air).
        *   Ngày dự kiến hàng về tương ứng với từng phương thức.
*   **Quy tắc cảnh báo & Xử lý lỗi (Validation Engine):**
    *   Hệ thống tự động loại trừ các phương thức vận chuyển không kịp ngày nhận mong muốn.
    *   Nếu tổng số lượng hàng khả thi của tất cả các Site được liên kết vẫn không đáp ứng đủ yêu cầu, hệ thống sẽ **báo lỗi thiếu hàng màu đỏ trực tiếp** và ghi rõ số lượng còn thiếu.
*   **Các thành phần điều khiển (UI Controls):**
    *   Nút **Chạy phân bổ tối ưu**: Tự động áp dụng thuật toán chia đơn.
    *   Nút **Quay lại**: Quay về danh sách yêu cầu.

---

### MH09. Màn hình Xác nhận & gửi đơn đặt hàng
*   **Mục đích:** Hiển thị kết quả phân chia đơn hàng tối ưu do hệ thống đề xuất và chốt xuất đơn.
*   **Thông tin hiển thị:**
    *   Bảng tổng hợp phân bổ đơn đặt hàng đề xuất:
        *   `Mã Site` (Nhận đơn).
        *   `Mã hàng (SKU)`.
        *   `Số lượng phân bổ`.
        *   `Phương thức vận chuyển được chọn` (Ưu tiên Tàu biển, chỉ đi Máy bay khi đi Tàu bị trễ).
        *   `Ngày dự kiến hàng về`.
    *   Cảnh báo danh sách các mặt hàng không thể đặt hàng (do thiếu nguồn cung hoặc trễ hạn).
*   **Thao tác điều khiển:**
    *   Checkbox: **"Tôi xác nhận thông tin đơn đặt hàng đã khớp và tối ưu"** (Bắt buộc check để kích hoạt nút gửi).
    *   Nút **Xác nhận & Gửi đơn**: Tạo các Đơn đặt hàng (PO) tương ứng trong cơ sở dữ liệu và gửi thông báo tới các Site.
    *   Hiển thị thông báo thành công: *"Đã chốt và gửi đơn hàng thành công tới [Tên các Site]!"*

---

## NHÓM 5 – ĐƠN HÀNG

### MH10. Màn hình Danh sách đơn hàng (PO)
*   **Mục đích:** Quản lý và giám sát tất cả các đơn đặt hàng chính thức (PO) phát sinh từ hệ thống.
*   **Trường thông tin hiển thị (Bảng danh sách):**
    *   `Mã đơn đặt hàng (PO)`, `Mã yêu cầu gốc`, `Site tiếp nhận`, `Số mặt hàng`, `Phương thức vận chuyển`, `Ngày gửi PO`, `Trạng thái đơn hàng` (Chờ Site xác nhận / Đang chuẩn bị / Đang vận chuyển / Đã đến kho / Hoàn tất / Đã hủy).
*   **Tìm kiếm & Bộ lọc:**
    *   Tìm kiếm theo: `Mã đơn`, `Mã Site`, hoặc `Mã hàng (SKU)`.
    *   Lọc theo: `Trạng thái đơn hàng`, `Site cung cấp`, và `Khoảng thời gian tạo`.
*   **Thao tác điều khiển:**
    *   Nút **Xem chi tiết**: Chuyển hướng sang màn hình `MH11`.

---

### MH11. Màn hình Chi tiết / Sửa đơn hàng
*   **Mục đích:** Quản lý chi tiết một PO, hỗ trợ điều chỉnh phương án đặt hàng khi đơn hàng ở trạng thái ban đầu.
*   **Thông tin hiển thị:**
    *   Mã đơn, Mã site cung cấp, Ngày gửi, Trạng thái đơn, Phương thức vận chuyển.
    *   **Bảng mặt hàng chi tiết:** SKU, Tên hàng, Số lượng, Đơn vị tính.
    *   **Timeline trạng thái đơn hàng (Động):** Biểu diễn trực quan các bước xử lý (Chờ xác nhận -> Đã xác nhận -> Đang vận chuyển -> Đã tới kho -> Hoàn tất).
    *   **Thông tin vận chuyển:** Ngày dự kiến về, số ngày vận chuyển thực tế.
*   **Chức năng chỉnh sửa (Điều kiện: Đơn hàng ở trạng thái "Chờ Site xác nhận"):**
    *   Cho phép nhân viên Đặt hàng quốc tế điều chỉnh `Số lượng` hoặc thay đổi `Phương thức vận chuyển` (Sea <-> Air) nếu có phát sinh thay đổi đột xuất.
*   **Các thành phần điều khiển (UI Controls):**
    *   Nút **Lưu thay đổi**: Cập nhật lại dữ liệu đơn đặt hàng.
    *   Nút **Hủy đơn hàng**: Mở popup yêu cầu nhập lý do hủy, sau đó hủy đơn.

---

## NHÓM 6 – SITE & TỒN KHO

### MH12. Màn hình Danh sách site (Supplier Sites)
*   **Mục đích:** Quản lý danh sách các địa điểm cung cấp (Site) nước ngoài và hiệu suất giao hàng của họ.
*   **Trường thông tin hiển thị (Bảng danh sách):**
    *   `Mã Site`, `Tên Site`, `Quốc gia/Vị trí`, `Số mặt hàng kinh doanh`, `Thời gian giao hàng biển (ngày)`, `Thời gian giao hàng bay (ngày)`, `Trạng thái hoạt động`.
*   **Tìm kiếm & Bộ lọc:**
    *   Tìm kiếm theo: `Mã Site` hoặc `Tên Site`.
    *   Lọc theo: `Trạng thái hoạt động`.
*   **Thao tác điều khiển:**
    *   Nút **Thêm site mới**: Tạo thực thể nhà cung cấp mới.
    *   Nút **Xem chi tiết**: Chuyển hướng đến màn hình `MH13`.

---

### MH13. Màn hình Chi tiết / Quản lý site
*   **Mục đích:** Cập nhật thông số vận chuyển và danh mục tồn kho của từng Site nước ngoài.
*   **Thông tin chỉnh sửa chung:**
    *   `Tên Site`, `Trạng thái hoạt động` (Hoạt động / Tạm ngưng).
    *   `Số ngày vận chuyển đường biển (Sea Lead Time)` (Bắt buộc > 0).
    *   `Số ngày vận chuyển đường hàng không (Air Lead Time)` (Bắt buộc > 0).
*   **Cấu trúc Tab chức năng:**
    *   **Tab 1: Danh sách mặt hàng kinh doanh**:
        *   Cho phép liên kết thêm mặt hàng (SKU) mới vào danh mục bán của Site hoặc xóa bớt.
    *   **Tab 2: Tồn kho hiện tại**:
        *   Hiển thị và cho phép cập nhật số lượng tồn kho khả dụng của từng SKU đang có tại Site (`Mã hàng`, `Số lượng tồn`, `Đơn vị tính`).
*   **Quy tắc kiểm tra (Validation):**
    *   Mã Site thêm mới không được trùng lặp.
    *   Số ngày vận chuyển Sea/Air Lead Time phải là số nguyên dương lớn hơn 0.
*   **Các thành phần điều khiển (UI Controls):**
    *   Nút **Lưu thay đổi**: Lưu cập nhật thông số và tồn kho của Site.
    *   Nút **Vô hiệu hóa site**: Mở popup xác nhận + nhập lý do (chuyển trạng thái hoạt động về Ngưng hoạt động).

---

## NHÓM 7 – NHẬP KHO

### MH14. Màn hình Danh sách đơn hàng cần nhập kho
*   **Mục đích:** Cung cấp cho Bộ phận Kho danh sách các đơn hàng (PO) sắp về hoặc đã cập cảng để thực hiện quy trình nhập kho đối chiếu.
*   **Trường thông tin hiển thị (Bảng danh sách):**
    *   `Mã đơn (PO)`, `Mã Site cung cấp`, `Số mặt hàng`, `Phương thức vận chuyển`, `Ngày gửi đơn`, `Ngày dự kiến đến kho`, `Trạng thái kiểm hàng` (Chờ kiểm hàng / Đang kiểm hàng / Đã nhập kho đủ / Đã nhập kho lệch).
*   **Thao tác điều khiển:**
    *   Nút **Bắt đầu kiểm hàng**: Kích hoạt trạng thái chuyển sang "Đang kiểm hàng" và điều hướng sang màn hình đối chiếu `MH15`.
    *   Nút **Xem chi tiết**: Xem lịch sử nhập kho nếu đơn đã hoàn thành.

---

### MH15. Màn hình Kiểm tra & xác nhận nhập kho
*   **Mục đích:** Bộ phận kho đối chiếu số lượng hàng thực tế nhận được so với số lượng đặt trên PO, phát hiện chênh lệch và cập nhật tồn kho nội bộ.
*   **Thông tin đơn hàng hiển thị:**
    *   Mã đơn, Mã site cung cấp, Phương tiện vận chuyển, Ngày gửi, Ngày đến thực tế.
*   **Bảng đối chiếu hàng hóa (Comparison Grid):**
    *   `Mã hàng (SKU)`, `Tên sản phẩm`, `Số lượng đặt trên PO`, `Số lượng thực nhận` (Input Number bắt buộc).
    *   `Chênh lệch` (Tự động tính: `Thực nhận - Số lượng đặt`).
    *   `Ghi chú lệch` (Input Text, hiển thị bắt buộc khi có chênh lệch).
*   **Quy tắc hiển thị và Xử lý chênh lệch:**
    *   Nếu `Chênh lệch < 0` (thiếu hàng), dòng sản phẩm đó sẽ được **highlight màu đỏ nổi bật** để cảnh báo.
    *   Khung tóm tắt kết quả kiểm hàng tự động hiển thị số lượng SKU: Đủ, Thiếu, Thừa.
*   **Thao tác điều khiển:**
    *   Checkbox **"Tôi xác nhận đã hoàn tất kiểm đếm số lượng thực tế nhận được"** (Bắt buộc check để mở khóa nút).
    *   Nút **Xác nhận nhập kho**: Lưu biên bản kiểm kho, chuyển trạng thái PO thành `Hoàn tất`, tự động ghi nhận số lượng thực tế để cập nhật hệ thống kho nội bộ.
    *   Hiển thị thông báo: *"Xác nhận nhập kho thành công! Tồn kho hệ thống đã được cập nhật."*

---

## NHÓM 8 – THEO DÕI VẬN CHUYỂN

### MH16. Màn hình Danh sách đơn hàng đang vận chuyển
*   **Mục đích:** Giám sát thời gian thực toàn bộ các chuyến hàng (đường biển, hàng không) đang di chuyển.
*   **Trường thông tin hiển thị (Bảng danh sách):**
    *   `Mã đơn (PO)`, `Site gửi`, `Phương thức vận chuyển`, `Ngày gửi đi`, `Ngày dự kiến cập kho`, `Số ngày còn lại (dự kiến)`, `Trạng thái vận chuyển` (Đang chuẩn bị / Đang vận chuyển / Đã đến cảng / Chờ thông quan).
*   **Hệ thống cảnh báo trễ hạn (SLA Alerts):**
    *   Nếu `Ngày hiện tại > Ngày dự kiến cập kho` mà trạng thái chưa chuyển thành đã nhận hàng, hiển thị **Badge cảnh báo trễ hạn màu đỏ nhấp nháy** để nhân viên kịp thời liên hệ đơn vị logistics.
*   **Thao tác điều khiển:**
    *   Nút **Xem chi tiết**: Điều hướng tới `MH17`.

---

### MH17. Màn hình Chi tiết & cập nhật trạng thái vận chuyển
*   **Mục đích:** Cập nhật các cột mốc vận chuyển của đơn hàng và ghi nhận các sự cố phát sinh trên lộ trình.
*   **Timeline trạng thái vận chuyển (Cập nhật tuần tự):**
    *   Các mốc: `Đã gửi đơn` -> `Site xác nhận` -> `Đang vận chuyển` -> `Đã đến kho` -> `Hoàn tất`.
    *   **Quy tắc:** Dropdown cập nhật trạng thái mới chỉ cho phép tiến lên mốc tiếp theo, không cho phép cập nhật nhảy cóc hoặc lùi lại mốc cũ.
*   **Ghi nhận sự cố vận chuyển (Issue Tracker):**
    *   Cho phép tích chọn báo cáo lỗi: `Mất mát hàng hóa`, `Trễ hạn (Delay)`, `Hàng hóa bị hỏng hóc`.
    *   Trường ghi chú sự cố và nút **Đính kèm file bằng chứng** (hình ảnh, biên bản sự cố dạng PDF/JPG).
*   **Bàn giao kho:**
    *   Khi trạng thái chuyển sang `Đã đến kho`, hệ thống hiển thị Popup xác nhận bàn giao hàng cho bộ phận kho để chuẩn bị cho quy trình kiểm kho ở `MH15`.
*   **Thao tác điều khiển:**
    *   Nút **Lưu cập nhật trạng thái**: Cập nhật thông tin lộ trình mới của đơn hàng vào cơ sở dữ liệu.
