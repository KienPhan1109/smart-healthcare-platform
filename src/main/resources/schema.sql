-- TÀI LIỆU KHỞI TẠO CƠ SỞ DỮ LIỆU CHUẨN (SCHEMA.SQL)
-- Phiên bản: V2 (Bổ sung Phân hệ Xét Nghiệm và Thanh Toán)
-- Lịch sử: Chuyển đổi Auditing/Soft Delete sang Explicit Columns. Bổ sung ROLE_CASHIER, luồng Khám linh hoạt.

CREATE DATABASE IF NOT EXISTS smart_healthcare;
USE smart_healthcare;

-- 1. Bảng Quyền (Roles)
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- 2. Bảng Người dùng (Users) - Đăng nhập bằng phone_number, không có username
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    profile_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL
);

-- 3. Bảng Phân Quyền (User_Roles)
CREATE TABLE user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    UNIQUE KEY uk_user_role (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- 4. Bảng Hồ Sơ Bệnh Nhân (Patients) - Một User có nhiều Patient (1-N)
CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    relation VARCHAR(20) NOT NULL DEFAULT 'SELF',
    date_of_birth DATE NULL,
    gender VARCHAR(20) NULL,
    identity_card VARCHAR(20) UNIQUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_patient_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 5. Bảng Hồ Sơ Sức Khỏe (Patient_Profiles) - Dùng Shared PK với Patients
CREATE TABLE patient_profiles (
    patient_id BIGINT PRIMARY KEY,
    medical_history TEXT,
    allergies TEXT,
    blood_type VARCHAR(20),
    height DECIMAL(5,2) NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    insurance_number VARCHAR(15) UNIQUE,
    insurance_expiry_date DATE,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_profile_patient FOREIGN KEY (patient_id) REFERENCES patients(id)
);

-- 6. Bảng Chuyên Khoa (Specialties)
CREATE TABLE specialties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL
);

-- 7. Bảng Bác Sĩ (Doctors)
CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    specialty_id BIGINT NOT NULL,
    academic_rank VARCHAR(50),
    biography TEXT,
    experience_years INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_doctor_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id)
);

-- 8. Bảng Thuốc & Vật Tư Y Tế (Medicines)
CREATE TABLE medicines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    usage_instruction TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'SELLING',
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL
);

-- 9. Bảng Hẹn Khám (Appointments)
CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    queue_number INT,
    appointment_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    symptoms TEXT,
    cancel_reason TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    UNIQUE KEY uk_appt_doctor_time (doctor_id, appointment_time),
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_appt_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT fk_appt_createdby FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

-- 10. Bảng Hồ Sơ Bệnh Án (Medical_Records)
CREATE TABLE medical_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT UNIQUE,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    symptoms TEXT NOT NULL,
    diagnosis TEXT NOT NULL,
    notes TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_record_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    CONSTRAINT fk_record_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_record_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- 11. Bảng Đơn Thuốc (Prescriptions) - Shared PK với Medical Records
CREATE TABLE prescriptions (
    medical_record_id BIGINT PRIMARY KEY,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_insurance_applied BOOLEAN NOT NULL DEFAULT FALSE,
    original_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    final_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    notes TEXT,
    issued_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_prescription_record FOREIGN KEY (medical_record_id) REFERENCES medical_records(id)
);

-- 12. Bảng Chi Tiết Đơn Thuốc (Prescription_Details)
CREATE TABLE prescription_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(200) NOT NULL,
    note TEXT,
    price_at_prescription DECIMAL(12,2) NOT NULL,
    unit_at_prescription VARCHAR(50) NOT NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_detail_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions(medical_record_id),
    CONSTRAINT fk_detail_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

-- ========================================================
-- PHẦN BỔ SUNG: PHÂN HỆ CẬN LÂM SÀNG & THANH TOÁN (PHASE 0)
-- ========================================================



-- 16. Bảng Thanh Toán Hóa Đơn (Payments)
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    appointment_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(20),
    transaction_id VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    paid_at TIMESTAMP NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_payment_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_payment_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

-- ========================================================
-- PHẦN BỔ SUNG: PHÂN HỆ CẬN LÂM SÀNG (LABORATORY MODULE)
-- ========================================================

-- 17. Bảng Danh Mục Loại Xét Nghiệm (Lab_Tests) - Dữ liệu cố định (Set cứng)
CREATE TABLE lab_tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    description TEXT,
    room_type VARCHAR(50) NOT NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL
);

-- 18. Bảng Phiếu Chỉ Định Xét Nghiệm (Lab_Orders)
CREATE TABLE lab_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_laborder_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    CONSTRAINT fk_laborder_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- 19. Bảng Chi Tiết Phiếu Chỉ Định & Kết Quả Xét Nghiệm (Lab_Order_Details)
CREATE TABLE lab_order_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lab_order_id BIGINT NOT NULL,
    lab_test_id BIGINT NOT NULL,
    price_at_order DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    result TEXT,
    completed_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_detail_laborder FOREIGN KEY (lab_order_id) REFERENCES lab_orders(id),
    CONSTRAINT fk_detail_labtest FOREIGN KEY (lab_test_id) REFERENCES lab_tests(id)
);
