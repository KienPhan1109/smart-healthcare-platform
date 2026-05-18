# ĐẶC TẢ LUỒNG NGHIỆP VỤ Y TẾ SỐ VÀ LỘ TRÌNH TRIỂN KHAI CUỐN CHIẾU
## Dự án Nền tảng Y tế Số Toàn diện (Smart Healthcare Platform)

Tài liệu này trình bày chi tiết luồng nghiệp vụ y tế lâm sàng và cận lâm sàng (xét nghiệm) nâng cao, thiết kế chi tiết 5 màn hình Dashboard độc lập của từng vai trò và lộ trình 6 cột mốc triển khai cuốn chiếu của dự án.

---

## I. LUỒNG NGHIỆP VỤ Y TẾ SỐ TÍCH HỢP XÉT NGHIỆM LÂM SÀNG VÀ CẬN LÂM SÀNG

Quy trình luân chuyển bệnh nhân khám bệnh được thiết kế tối ưu, mô phỏng chính xác nghiệp vụ thực tế tại các bệnh viện lớn ở Việt Nam:

1.  **Bước 1: Đặt lịch và Khám lâm sàng lần 1 (Khám sơ bộ)**
    *   Bệnh nhân đặt lịch khám trực tuyến trên web/app hoặc Nhân viên điều phối hỗ trợ đăng ký trực tiếp tại quầy của bệnh viện. Bệnh nhân chọn Chuyên khoa, Bác sĩ khám và khung giờ khám trống chính xác.
    *   Sau khi bệnh nhân hoàn tất đóng phí khám ban đầu, lịch hẹn ở trạng thái chờ khám.
    *   Nhân viên điều phối tiếp nhận yêu cầu, duyệt đơn khám và tự động cấp Số thứ tự khám (queueNumber) của bác sĩ được chỉ định trong ngày.
    *   Bác sĩ tiếp nhận bệnh nhân khám lâm sàng lần 1, hỏi triệu chứng sơ bộ và thực hiện chỉ định các dịch vụ xét nghiệm cận lâm sàng (như xét nghiệm máu, siêu âm, chụp X-quang) phù hợp với tình trạng bệnh nhân.
2.  **Bước 2: Thực hiện Xét nghiệm (Cận lâm sàng)**
    *   Bệnh nhân di chuyển sang khu vực cận lâm sàng để thực hiện các chỉ định xét nghiệm. Các loại xét nghiệm được cấu hình cố định theo từng chuyên khoa và quyền hạn chỉ định của từng bác sĩ.
    *   Kỹ thuật viên tại phòng xét nghiệm tiến hành dịch vụ và nhập kết quả y khoa trực tiếp lên hệ thống (lưu vào bảng lab_results).
3.  **Bước 3: Tái khám lần 2 (Đưa ra kết luận chẩn đoán cuối cùng)**
    *   Ngay khi có đầy đủ kết quả xét nghiệm cận lâm sàng, hệ thống tự động đưa bệnh nhân trở lại hàng chờ ưu tiên của Bác sĩ ban đầu để tái khám lần 2.
    *   Bác sĩ đọc kết quả chỉ số xét nghiệm trực quan trên màn hình, đưa ra kết luận chẩn đoán cuối cùng và thực hiện kê đơn thuốc điện tử. Hệ thống tự động chuyển trạng thái lịch hẹn thành hoàn thành (COMPLETED).
4.  **Bước 4: Tất toán tiền thuốc, áp dụng BHYT và Phát thuốc**
    *   Đơn thuốc được chuyển thẳng sang nhà thuốc bệnh viện. Dược sĩ tiếp nhận đơn thuốc trạng thái chờ cấp phát.
    *   Hệ thống tự động kiểm tra mã thẻ BHYT của bệnh nhân: Nếu thẻ BHYT hợp lệ và còn hạn sử dụng, hệ thống tự động áp dụng chiết khấu giảm giá 80% tổng giá trị đơn thuốc.
    *   Bệnh nhân thanh toán 20% chi phí thực tế còn lại cho Dược sĩ.
    *   Dược sĩ bấm xác nhận đã tất toán tiền thuốc để hệ thống tự động trừ số lượng tồn kho dược phẩm (medicines.stock_quantity) và chuyển trạng thái đơn thuốc thành đã phát (DISPENSED). Bệnh nhân nhận thuốc và kết thúc ca khám.

### Sơ đồ luồng y tế số nâng cao bằng Mermaid:

```mermaid
sequenceDiagram
    autonumber
    actor P as Benh Nhan
    actor C as Dieu Phoi Vien
    actor D as Bac Si
    actor Ph as Duoc Si
    database DB as Co So Du Lieu

    %% Buoc 1: Kham lam sang lan 1
    Note over P, D: KHAM LAM SANG LAN 1
    P->>DB: Dat lich kham (Chuyen khoa, Bac si, Gio kham)
    C->>DB: Duyet don, cap so thu tu (queueNumber)
    D->>DB: Kham lam sang & Chi dinh xet nghiem

    %% Buoc 2: Xet nghiem
    Note over P, DB: THUC HIEN CAN LAM SANG
    P->>DB: Thuc hien cac xet nghiem duoc chi dinh
    DB->>DB: Ghi nhan ket qua xet nghiem (lab_results)

    %% Buoc 3: Kham lan 2 & Ke don
    Note over P, D: KHAM LAN 2 (TAI KHAM)
    P->>D: Quay lai hang cho cua Bac si ban dau
    D->>DB: Doc ket qua xet nghiem & Chuan doan cuoi cung
    D->>DB: Ke don thuoc dien tu, chuyen trang thai hoan tat (COMPLETED)

    %% Buoc 4: Duoc si phat thuoc
    Note over P, Ph: PHAT THUOC & TRU KHO
    Ph->>DB: Doc don thuoc truc tuyen, ap dung BHYT 80%
    P->>Ph: Nop tien thuoc thực te
    Ph->>DB: Xac nhan phat thuoc (Tru medicines.stock_quantity)
```

---

## II. ĐẶC TẢ CHI TIẾT 5 MÀN HÌNH DASHBOARD ĐỘC LẬP THEO PHÂN QUYỀN

Mỗi vai trò trong hệ thống sở hữu một giao diện làm việc độc lập hoàn toàn, được thiết kế chuyên biệt và chống gộp trang tuyệt đối:

### 1. Dashboard Bệnh Nhân (templates/patient/dashboard.html)
Giao diện đơn giản, rõ ràng, tập trung vào dịch vụ cá nhân:
*   **Đặt lịch khám:** Chọn chuyên khoa, chọn bác sĩ thuộc chuyên khoa (hiển thị học hàm/học vị), chọn ngày khám và khung giờ khám còn trống chính xác, thanh toán phí khám.
*   **Xem lịch khám cá nhân:** Quản lý danh sách lịch khám trực quan (chờ khám, đã khám, đã hủy).
*   **Tra cứu sức khỏe:** Xem hồ sơ bệnh án cũ, kết quả xét nghiệm chi tiết và danh sách đơn thuốc đã kê.

### 2. Dashboard Nhân Viên Điều Phối (templates/coordinator/dashboard.html)
Giao diện có tần suất xử lý thông tin cao, hỗ trợ thao tác nhanh:
*   **Duyệt đơn khám:** Danh sách các ca đặt lịch trực tuyến mới thanh toán thành công. Điều phối viên bấm nút xác nhận duyệt để hệ thống tự động cấp số thứ tự khám (queueNumber) cho bác sĩ phụ trách.
*   **Tạo lịch tại quầy:** Form đăng ký nhanh cho bệnh nhân đăng ký trực tiếp tại quầy bệnh viện.
*   **Giám sát hàng đợi:** Theo dõi trực quan số lượng bệnh nhân đang chờ tại từng phòng khám để hỗ trợ điều phối luồng bệnh nhân nếu xảy ra quá tải.

### 3. Dashboard Bác Sĩ (templates/doctor/dashboard.html)
Giao diện chuyên môn sâu, tích hợp công cụ hỗ trợ lâm sàng:
*   **Hàng đợi bệnh nhân khám lâm sàng:** Danh sách bệnh nhân chờ khám lần 1 được sắp xếp theo số thứ tự (queueNumber) tăng dần trong ngày.
*   **Giao diện ca khám lần 1:** Đọc thông tin hành chính, tiền sử dị ứng, nhóm máu. Nhập triệu chứng sơ bộ và click chọn chỉ định dịch vụ xét nghiệm cận lâm sàng tương ứng.
*   **Hàng đợi bệnh nhân chờ tái khám:** Danh sách bệnh nhân đã hoàn thành xét nghiệm quay lại phòng khám. Bác sĩ đọc kết quả xét nghiệm được đồng bộ trên hệ thống, nhập kết luận chẩn đoán cuối cùng, kê đơn thuốc và hoàn tất ca khám.

### 4. Dashboard Dược Sĩ (templates/pharmacist/dashboard.html)
Giao diện quản lý hóa đơn thuốc và cấp phát kho dược:
*   **Hàng chờ cấp phát thuốc:** Danh sách các đơn thuốc trạng thái chờ phát được gửi tự động từ các phòng khám.
*   **Tất toán tài chính:** Hiển thị chi tiết đơn thuốc, kiểm tra hiệu lực thẻ BHYT của bệnh nhân để tự động tính toán chiết khấu giảm giá 80%, tính ra số tiền bệnh nhân thực tế phải nộp.
*   **Cấp phát và Trừ kho:** Sau khi bệnh nhân nộp tiền thuốc thành công, Dược sĩ bấm nút xác nhận đã thu tiền để hệ thống tự động trừ kho thuốc và chuyển trạng thái đơn thuốc thành hoàn tất.

### 5. Dashboard Quản Trị Viên (templates/admin/dashboard.html)
Giao diện quản lý cấp cao và vận hành hệ thống:
*   **Quản lý nhân viên:** Cấp tài khoản mới cho bác sĩ, dược sĩ, nhân viên điều phối và quản lý khóa/mở khóa tài khoản khi có sự cố.
*   **Báo cáo doanh thu:** Thống kê doanh thu từ tiền bán thuốc và doanh thu khám bệnh dưới dạng biểu đồ số liệu trực quan theo tuần, tháng.
*   **Ràng buộc bảo mật dữ liệu:** Quản trị viên tuyệt đối không được phép chỉnh sửa danh mục chuyên khoa và loại xét nghiệm mẫu. Toàn bộ danh mục này được thiết lập cứng từ cơ sở dữ liệu gốc để bảo vệ tính đồng nhất của hệ thống y tế.

---

## III. LỘ TRÌNH TRIỂN KHAI CUỐN CHIẾU 6 CỘT MỐC (ROADMAP)

Dự án được phân chia thành 6 cột mốc rõ rệt để thực hiện cuốn chiếu, đảm bảo chất lượng kiểm thử tốt nhất:

### Cột Mốc 1: Xác Thực (Auth) và Phân Hướng Dashboard Độc Lập (Thực hiện ngay)
*   **Giao diện:** Trang Đăng nhập (login.html) và Đăng ký (register.html) sử dụng hệ thống biến màu đồng nhất, validation email nghiêm ngặt (strict regex) và số điện thoại Việt Nam.
*   **Logic Backend:** Spring Security CustomSuccessHandler tự động nhận dạng quyền (Role) của tài khoản sau khi đăng nhập thành công để redirect chính xác về 5 trang giao diện chính độc lập.
*   **Khung Giao Diện:** Master Layout main.html, hệ thống Sidebar điều động menu theo phân quyền và tạo sẵn 5 file HTML giao diện trống tương ứng với 5 vai trò để phục vụ kiểm thử phân hướng.

### Cột Mốc 2: Phân Hệ Bệnh Nhân (ROLE_PATIENT)
*   Hoàn thiện toàn bộ các chức năng đặt lịch khám trực tuyến, chọn chuyên khoa, chọn bác sĩ và khung giờ khám trống, thanh toán phí khám giả lập và tra cứu hồ sơ BHYT/Bệnh án cá nhân.

### Cột Mốc 3: Phân Hệ Nhân Viên Điều Phối (ROLE_COORDINATOR)
*   Hoàn thiện luồng tiếp nhận duyệt lịch online và offline tại quầy, tự động tính số thứ tự khám (queueNumber) của bác sĩ và quản lý hàng đợi phòng khám.

### Cột Mốc 4: Phân Hệ Bác Sĩ (ROLE_DOCTOR)
*   Hoàn thiện luồng khám lâm sàng lần 1, chỉ định xét nghiệm cận lâm sàng cho bệnh nhân, tiếp nhận kết quả xét nghiệm tự động và chẩn đoán cuối cùng kết hợp kê đơn thuốc điện tử.

### Cột Mốc 5: Phân Hệ Dược Sĩ (ROLE_PHARMACIST)
*   Hoàn thiện luồng cấp phát thuốc y tế, tự động áp dụng chiết khấu BHYT giảm giá 80%, tất toán hóa đơn và tự động trừ số lượng tồn kho dược phẩm an toàn.

### Cột Mốc 6: Phân Hệ Admin và Nghiệm Thu Hệ Thống
*   Quản lý danh sách nhân sự y khoa, xem biểu đồ báo cáo thống kê doanh thu và tiến hành tổng duyệt toàn bộ dự án.
