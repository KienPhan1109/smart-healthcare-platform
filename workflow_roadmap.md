# ĐẶC TẢ LUỒNG NGHIỆP VỤ Y TẾ SỐ VÀ LỘ TRÌNH TRIỂN KHAI CUỐN CHIẾU V2
## Dự án Nền tảng Y tế Số Toàn diện (Smart Healthcare Platform)

Tài liệu này đóng vai trò là "Bản đồ Dẫn đường" (Roadmap) của dự án. Tài liệu trình bày chi tiết luồng nghiệp vụ y tế linh hoạt, các kịch bản ngoại lệ, quy trình thanh toán minh bạch qua Thu ngân và Lộ trình 7 Cột mốc Triển khai.

---

## I. LUỒNG KHÁM CHỮA BỆNH LINH HOẠT VÀ TỐI ƯU THỰC TẾ

Quy trình luân chuyển bệnh nhân khám bệnh được thiết kế tối ưu, mô phỏng chính xác nghiệp vụ thực tế linh hoạt tại các bệnh viện lớn ở Việt Nam. Bác sĩ không bị ép buộc theo 1 luồng cứng nhắc mà được phép đưa ra 3 kịch bản kết thúc ca bệnh.

### 1. Bước 1: Đặt lịch và Khám lâm sàng lần 1
*   **Giữ chỗ thanh toán (3 phút):** Bệnh nhân đặt lịch trực tuyến, hệ thống giữ chỗ trong đúng 3 phút. Bệnh nhân phải thanh toán trực tuyến (VNPAY/MOMO). Quá 3 phút chưa nhả tiền, hệ thống tự động hủy đơn (`CANCELLED`).
*   **Tiếp nhận:** Điều phối viên hoặc hệ thống duyệt đơn và tự động cấp Số thứ tự khám (queueNumber) của bác sĩ được chỉ định trong ngày.
*   **Khám lâm sàng:** Bác sĩ tiếp nhận bệnh nhân khám lâm sàng lần 1, hỏi triệu chứng sơ bộ.

Tới đây, Bác sĩ có **3 Lựa Chọn (Kịch Bản Khám)**:
1.  **Kịch bản A (Kết thúc ngay):** Bác sĩ chẩn đoán, kê đơn thuốc và chuyển bệnh nhân sang thẳng Bước 4 (Thanh toán thuốc). Không cần xét nghiệm.
2.  **Kịch bản B (Xét nghiệm KHÔNG bắt buộc tái khám):** Bác sĩ tạo Phiếu chỉ định (`LabOrder`), yêu cầu bệnh nhân đi đóng phí xét nghiệm và làm xét nghiệm. Kết quả trả về qua App, bệnh nhân tự đọc, không bắt buộc quay lại phòng khám.
3.  **Kịch bản C (Xét nghiệm VÀ Hẹn tái khám):** Bác sĩ tạo Phiếu chỉ định, yêu cầu bệnh nhân làm xét nghiệm và quay lại tái khám. Chuyển sang Bước 2.

### 2. Bước 2: Thanh toán Xét nghiệm & Thực hiện Cận lâm sàng
*   **Thu ngân (Cashier):** Bệnh nhân cầm Phiếu chỉ định qua quầy Thu Ngân thanh toán phí xét nghiệm (Billing/Payment). Trạng thái lịch khám chuyển sang `WAITING_FOR_LAB`.
*   **Kỹ thuật viên:** Bệnh nhân thực hiện dịch vụ. Kỹ thuật viên nhập Kết quả (`LabResult`) lên hệ thống. Trạng thái chuyển thành `LAB_COMPLETED`.

### 3. Bước 3: Đưa vào Danh sách chờ Tái khám Ưu tiên
*   Khi có đầy đủ kết quả, hệ thống tự động đẩy Notification thông báo cho Bác sĩ và Bệnh nhân. Trạng thái chuyển sang `READY_FOR_REEXAM`.
*   Bệnh nhân KHÔNG bị ép đẩy vào hàng đợi chung. Thay vào đó, hệ thống đẩy bệnh nhân vào **Danh sách chờ ưu tiên tái khám** trên Dashboard của Bác sĩ. Bác sĩ sẽ tự gọi dựa theo tình hình rảnh rỗi tại phòng khám.
*   Bác sĩ đọc kết quả, đưa ra kết luận cuối cùng, kê đơn thuốc điện tử. Lịch khám hoàn thành (`COMPLETED`).
*   **Ngoại lệ:** Nếu Bác sĩ bận/hết giờ hoặc bệnh nhân không quay lại, **Điều phối viên** sẽ có quyền đóng thủ công ca khám này. Không dùng auto-cancel.

### 4. Bước 4: Thanh toán Tiền thuốc & Phát thuốc (Cashier & Pharmacist)
*   Quy trình phân tách rạch ròi chống thất thoát tài chính:
*   **Thu ngân (Cashier):** Tính toán bảo hiểm BHYT tự động giảm 80%, thu nốt 20% tiền thực tế từ bệnh nhân. In hóa đơn.
*   **Dược sĩ (Pharmacist):** Không đụng đến tiền. Chỉ tiếp nhận đơn thuốc đã thanh toán, soạn thuốc, kiểm kê và ấn xác nhận phát thuốc (Trừ số lượng `medicines.stock_quantity`).

---

## II. LỘ TRÌNH 7 CỘT MỐC PHÁT TRIỂN CUỐN CHIẾU

Dự án được phân chia thành 7 Cột mốc chuẩn xác dựa trên nguyên lý Dependency (Phụ thuộc), tức là Module sau chỉ làm được khi Module trước đã hoàn thiện.

| Cột mốc | Tên gọi | Nội dung chính | Phụ thuộc |
| :--- | :--- | :--- | :--- |
| **1** | **Foundation** | Triển khai Authentication, Role Management, Dashboard Skeleton (Bộ khung giao diện), Layout Fragment chung. | Không có |
| **2** | **Patient Module** | Bệnh nhân Đăng ký, Đặt lịch, Thanh toán phí khám (giữ chỗ 3 phút), Hồ sơ cá nhân. | 1 |
| **3** | **Coordinator + Queue** | Điều phối viên tiếp nhận, Duyệt lịch, Quản lý cấp số thứ tự, Xử lý ngoại lệ đóng ca. | 2 |
| **4** | **Laboratory Module** | Quản lý danh mục xét nghiệm (`LabTest`), Kỹ thuật viên tiếp nhận Phiếu chỉ định (`LabOrder`), Trả kết quả (`LabResult`). | 3 |
| **5** | **Doctor Module** | Khám lâm sàng linh hoạt 3 kịch bản, Chỉ định Lab, Kê đơn thuốc, Gọi bệnh nhân từ danh sách tái khám. | 4 |
| **6** | **Pharmacy + Payment** | Thu ngân (`ROLE_CASHIER`) thu mọi loại chi phí, áp dụng BHYT. Dược sĩ cấp phát thuốc, trừ lùi kho. | 5 |
| **7** | **Admin + Reporting** | Quản trị hệ thống, Báo cáo doanh thu tài chính tổng hợp, Audit log, Quản lý tài khoản. | Tất cả |

---

## III. HỆ THỐNG CẢNH BÁO VÀ THEO DÕI

1.  **Hệ thống Thông báo (Notification System):**
    *   Bệnh nhân: Gửi thông báo khi có kết quả xét nghiệm, đến lượt vào khám, đơn thuốc sẵn sàng nhận, nhắc nhở thanh toán trong 3 phút.
    *   Bác sĩ: Báo cáo khi có kết quả xét nghiệm của bệnh nhân vừa hoàn tất.
2.  **Bảo mật & Lịch sử (Auditing & Soft Delete):**
    *   Tất cả các bản ghi y tế, xét nghiệm, hóa đơn đều bắt buộc gắn Audit (`createdBy`, `updatedAt`) và Xóa mềm tường minh. Nghiêm cấm sử dụng Magic Annotations (Như `@SQLDelete`). Mọi hành động xóa đều phải thông qua Code Logic ở Service để kiểm soát.
3.  **Xử lý Ngoại lệ Tối cao (Exception & Error Handling):**
    *   Bác sĩ hủy chỉ định xét nghiệm do gõ nhầm.
    *   Điều phối viên đóng ca khám do bệnh nhân không quay lại sau khi xét nghiệm.
    *   Lịch khám tự hủy do quá 3 phút thanh toán Gateway (VNPAY).
