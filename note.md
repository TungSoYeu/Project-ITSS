# Phân quyền và Chức năng (Roles & Responsibilities)

**Quản trị viên (Admin)**
- Quản lý tài khoản (tạo/sửa/xóa/khóa user, gán role)
- Cập nhật thông tin Site (số ngày vận chuyển tàu/HK, danh sách mặt hàng site kinh doanh — vì đề nói "khi thay đổi, site thông báo cho BPĐHQT")
- Quản lý mặt hàng kinh doanh (gán mặt hàng cho từng site)

**Bộ phận bán hàng (Sales)**
- Quản lý yêu cầu đặt hàng — tạo danh sách yêu cầu gồm: mã hàng, số lượng, đơn vị, ngày nhận mong muốn. Tại cùng thời điểm có thể có nhiều danh sách.
- Quản lý danh sách mặt hàng — xem catalog mặt hàng hệ thống.
- Quản lý site — xem thông tin các site (chỉ đọc).

**Bộ phận đặt hàng quốc tế (Overseas Order - OVS)**
- Xem danh sách yêu cầu đặt hàng đã nhận — lọc theo trạng thái, ngày.
- Xem chi tiết yêu cầu đặt hàng đã nhận — xem từng mặt hàng, số lượng, ngày nhận mong muốn.
- Xử lý yêu cầu đặt hàng — luồng chính theo đề bài: 
  - Tìm site có kinh doanh ít nhất 1 mặt hàng trong danh sách.
  - Với mỗi site: lọc danh sách mặt hàng site kinh doanh → **gửi hỏi tồn kho (Site Inquiry)**.
  - Nhận phản hồi tồn kho → ghi vào Tệp thông tin kho.
  - Kiểm tra ngày vận chuyển từ Tệp thông tin site.
  - Phân bổ số lượng theo tiêu chí ưu tiên: tàu > hàng không → tồn kho lớn hơn → ít site nhất.
  - Gửi thông tin đặt hàng tới các site đã chọn (gồm: mã site, mã hàng, số lượng, đơn vị, phương tiện vận chuyển).
- Quản lý đơn hàng đã tạo — xem lịch sử đơn đã gửi, trạng thái từng đơn.
- Xử lý đơn hàng bị hủy — hủy đơn, thông báo site liên quan, cập nhật trạng thái.

**Site (Đối tác nước ngoài — External Portal)**
- Xem danh sách đơn hàng — các đơn được gửi đến site mình.
- Xem chi tiết đơn hàng — mặt hàng, số lượng, phương tiện, ngày dự kiến.
- Xác nhận đơn hàng — site cam kết thực hiện đơn.
- Cập nhật mặt hàng kinh doanh — khi thay đổi danh sách mặt hàng, gửi lại cho BPĐHQT (theo đề bài: "mỗi site sẽ gửi lại danh sách mới").
- *Lưu ý:* Phản hồi yêu cầu hỏi tồn kho từ OVS.

**Bộ phận quản lý kho (Warehouse)**
- Xác nhận đơn hàng giao tới — đánh dấu hàng đã về, đối chiếu thực tế vs đặt.
- Xem danh sách đơn hàng giao tới — lọc theo ngày, trạng thái.
- Xem chi tiết đơn hàng giao tới — mặt hàng, số lượng thực tế.
- *Theo đề bài:* "Bộ phận quản lý kho kiểm hàng, so sánh hàng về thực tế với hàng đặt trong danh sách, rồi lưu vào hệ thống quản lý kho của riêng họ".

**Hệ thống quản lý kho (External System — Tự động)**
- Xác nhận đơn hàng giao tới — đồng bộ tự động khi hàng nhập kho (không có giao diện người dùng).
