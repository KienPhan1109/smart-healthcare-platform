package com.ptit.smart_healthcare_platform.config;

import com.ptit.smart_healthcare_platform.model.entity.*;
import com.ptit.smart_healthcare_platform.model.enums.*;
import com.ptit.smart_healthcare_platform.repository.PatientRepository;
import com.ptit.smart_healthcare_platform.repository.RoleRepository;
import com.ptit.smart_healthcare_platform.repository.UserRepository;
import com.ptit.smart_healthcare_platform.repository.SpecialtyRepository;
import com.ptit.smart_healthcare_platform.repository.DoctorRepository;
import com.ptit.smart_healthcare_platform.repository.MedicineRepository;
import com.ptit.smart_healthcare_platform.repository.LabTestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;
    private final MedicineRepository medicineRepository;
    private final LabTestRepository labTestRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      UserRepository userRepository,
                      PatientRepository patientRepository,
                      SpecialtyRepository specialtyRepository,
                      DoctorRepository doctorRepository,
                      MedicineRepository medicineRepository,
                      LabTestRepository labTestRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.specialtyRepository = specialtyRepository;
        this.doctorRepository = doctorRepository;
        this.medicineRepository = medicineRepository;
        this.labTestRepository = labTestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            System.out.println("[DataSeeder] Dữ liệu đã tồn tại, bỏ qua seed.");
            return;
        }

        System.out.println("[DataSeeder] Bắt đầu nạp dữ liệu mẫu...");

        // === 1. Tao 4 Role (Patient, Doctor, Admin, Technician) ===
        Role rolePatient = createRole(RoleName.ROLE_PATIENT);
        Role roleDoctor = createRole(RoleName.ROLE_DOCTOR);
        Role roleAdmin = createRole(RoleName.ROLE_ADMIN);
        Role roleTechnician = createRole(RoleName.ROLE_TECHNICIAN);

        // === 2. Tao tai khoan mau - Mat khau chung: 123456 ===
        User patient = createUser("0901000001", "Nguyen Van Benh", "patient1@shp.vn", true, rolePatient);
        User doc1 = createUser("0901000002", "Tran Thi Bac Si", "doctor1@shp.vn", true, roleDoctor);
        createUser("0901000003", "Kỹ Thuật Viên Cận Lâm Sàng", "technician@shp.vn", true, roleTechnician);
        createUser("0901000006", "Quan Tri Vien", "admin@shp.vn", true, roleAdmin);

        // === 3. Tao ho so benh nhan mac dinh (SELF) cho tai khoan benh nhan ===
        Patient selfPatient = new Patient();
        selfPatient.setUser(patient);
        selfPatient.setFullName(patient.getFullName());
        selfPatient.setRelation(PatientRelation.SELF);
        selfPatient.setDateOfBirth(java.time.LocalDate.of(1995, 10, 15));
        selfPatient.setGender(Gender.MALE);
        selfPatient.setIdentityCard("001095000123");
        selfPatient.setCreatedBy("SYSTEM");
        selfPatient.setCreatedAt(LocalDateTime.now());

        PatientProfile profile = new PatientProfile();
        profile.setPatient(selfPatient);
        profile.setMedicalHistory("Tiền sử dạ dày nhẹ, hay đau khi ăn đồ chua cay.");
        profile.setAllergies("Dị ứng hải sản.");
        profile.setBloodType(BloodType.O_POSITIVE);
        profile.setHeight(new BigDecimal("170.0"));
        profile.setWeight(new BigDecimal("65.0"));
        profile.setInsuranceNumber("GD4797918800001");
        profile.setInsuranceExpiryDate(java.time.LocalDate.of(2030, 12, 31));
        profile.setCreatedBy("SYSTEM");
        profile.setCreatedAt(LocalDateTime.now());

        selfPatient.setPatientProfile(profile);
        patientRepository.save(selfPatient);

        // === 4. Tao 8 Chuyen Khoa Mau ===
        Specialty spTimMach = createSpecialty("Khoa Tim Mạch", "Khám và điều trị các bệnh lý tim mạch, huyết áp, rối loạn nhịp tim.");
        Specialty spNhi = createSpecialty("Khoa Nhi", "Chăm sóc sức khỏe toàn diện cho trẻ sơ sinh, trẻ nhỏ và trẻ vị thành niên.");
        Specialty spSanPhuKhoa = createSpecialty("Khoa Sản Phụ Khoa", "Khám thai, theo dõi thai kỳ, điều trị các bệnh lý phụ khoa.");
        Specialty spDaLieu = createSpecialty("Khoa Da Liễu", "Chẩn đoán và điều trị các bệnh lý về da, tóc, móng và thẩm mỹ da.");
        Specialty spTaiMuiHong = createSpecialty("Khoa Tai Mũi Họng", "Điều trị các bệnh lý về tai, mũi, họng và vùng đầu cổ.");
        Specialty spRangHamMat = createSpecialty("Khoa Răng Hàm Mặt", "Chăm sóc sức khỏe răng miệng, nhổ răng, bọc sứ, chỉnh nha.");
        Specialty spMat = createSpecialty("Khoa Mắt", "Chẩn đoán và điều trị tật khúc xạ, các bệnh lý về mắt và võng mạc.");
        Specialty spNoiTongQuat = createSpecialty("Khoa Nội Tổng Quát", "Khám, chẩn đoán và điều trị các bệnh nội khoa tổng quát.");

        // === 5. Tao 3 Bac Si cho moi Chuyen Khoa ===
        // Tim Mạch
        createDoctor(createUser("0901000101", "PGS.TS.BS. Nguyễn Văn Hùng", "hung.nv@shp.vn", true, roleDoctor),
                spTimMach, "PGS.TS.BS", "Phó Giáo sư, Tiến sĩ, Bác sĩ Nguyễn Văn Hùng có hơn 25 năm kinh nghiệm trong lĩnh vực Tim mạch can thiệp, nguyên Trưởng khoa Tim mạch BV Bạch Mai.", 25, new BigDecimal("500000"));
        createDoctor(createUser("0901000102", "ThS.BS. Trần Tuấn Anh", "tuananh.t@shp.vn", true, roleDoctor),
                spTimMach, "ThS.BS", "Thạc sĩ, Bác sĩ Trần Tuấn Anh là bác sĩ điều trị giàu kinh nghiệm về bệnh lý mạch vành và tăng huyết áp.", 12, new BigDecimal("300000"));
        createDoctor(createUser("0901000103", "BS.CKII. Lê Thị Mai", "mai.lt@shp.vn", true, roleDoctor),
                spTimMach, "BS.CKII", "Bác sĩ Chuyên khoa II Lê Thị Mai chuyên khám và tư vấn điều trị suy tim, hẹp hở van tim.", 18, new BigDecimal("400000"));

        // Nhi
        createDoctor(createUser("0901000104", "BS.CKII. Lê Thị Kim Anh", "kimanh.lt@shp.vn", true, roleDoctor),
                spNhi, "BS.CKII", "Bác sĩ Chuyên khoa II Lê Thị Kim Anh chuyên khám và điều trị các bệnh lý nhi khoa, dinh dưỡng trẻ em với hơn 20 năm công tác tại BV Nhi Trung ương.", 20, new BigDecimal("300000"));
        createDoctor(createUser("0901000105", "ThS.BS. Phạm Minh Tuấn", "tuan.pm@shp.vn", true, roleDoctor),
                spNhi, "ThS.BS", "Thạc sĩ, Bác sĩ Phạm Minh Tuấn có nhiều năm nghiên cứu về các bệnh lý hô hấp và truyền nhiễm ở trẻ nhỏ.", 10, new BigDecimal("250000"));
        createDoctor(createUser("0901000106", "BS.CKI. Nguyễn Hồng Hạnh", "hanh.nh@shp.vn", true, roleDoctor),
                spNhi, "BS.CKI", "Bác sĩ Chuyên khoa I Nguyễn Hồng Hạnh chuyên tư vấn phát triển thể chất và tiêm chủng cho trẻ em.", 8, new BigDecimal("200000"));

        // Sản Phụ Khoa
        createDoctor(createUser("0901000107", "ThS.BS. Trần Quốc Tuấn", "tuan.tq@shp.vn", true, roleDoctor),
                spSanPhuKhoa, "ThS.BS", "Thạc sĩ, Bác sĩ Trần Quốc Tuấn tốt nghiệp ĐH Y Hà Nội, chuyên gia về sản khoa, hỗ trợ sinh sản và phẫu thuật nội soi phụ khoa.", 15, new BigDecimal("400000"));
        createDoctor(createUser("0901000108", "BS.CKII. Nguyễn Thị Minh Thư", "thu.ntm@shp.vn", true, roleDoctor),
                spSanPhuKhoa, "BS.CKII", "Bác sĩ Chuyên khoa II Nguyễn Thị Minh Thư có hơn 22 năm kinh nghiệm trong quản lý thai kỳ nguy cơ cao và sinh nở an toàn.", 22, new BigDecimal("450000"));
        createDoctor(createUser("0901000109", "BS.CKI. Phan Huy Hải", "hai.ph@shp.vn", true, roleDoctor),
                spSanPhuKhoa, "BS.CKI", "Bác sĩ Chuyên khoa I Phan Huy Hải chuyên khám phụ khoa định kỳ, tầm soát sớm ung thư cổ tử cung.", 9, new BigDecimal("250000"));

        // Da Liễu
        createDoctor(createUser("0901000110", "BS.CKI. Nguyễn Thị Thu Trang", "trang.ntt@shp.vn", true, roleDoctor),
                spDaLieu, "BS.CKI", "Bác sĩ Chuyên khoa I Nguyễn Thị Thu Trang chuyên điều trị mụn trứng cá, sẹo, viêm da cơ địa và các liệu trình thẩm mỹ da công nghệ cao.", 12, new BigDecimal("300000"));
        createDoctor(createUser("0901000111", "ThS.BS. Vũ Hoàng Nam", "nam.vh@shp.vn", true, roleDoctor),
                spDaLieu, "ThS.BS", "Thạc sĩ, Bác sĩ Vũ Hoàng Nam chuyên điều trị các bệnh da tự miễn, vảy nến và nấm da.", 11, new BigDecimal("250000"));
        createDoctor(createUser("0901000112", "BS.CKII. Đặng Minh Anh", "minhanh.d@shp.vn", true, roleDoctor),
                spDaLieu, "BS.CKII", "Bác sĩ Chuyên khoa II Đặng Minh Anh có hơn 17 năm kinh nghiệm điều trị các bệnh lý da liễu phức tạp ở người lớn và trẻ em.", 17, new BigDecimal("350000"));

        // Tai Mũi Họng
        createDoctor(createUser("0901000113", "ThS.BS. Phạm Hồng Sơn", "son.ph@shp.vn", true, roleDoctor),
                spTaiMuiHong, "ThS.BS", "Thạc sĩ, Bác sĩ Phạm Hồng Sơn chuyên phẫu thuật nội soi tai mũi họng, điều trị viêm xoang, viêm VA và các bệnh lý thanh quản.", 10, new BigDecimal("250000"));
        createDoctor(createUser("0901000114", "BS.CKII. Nguyễn Văn Trung", "trung.nv@shp.vn", true, roleDoctor),
                spTaiMuiHong, "BS.CKII", "Bác sĩ Chuyên khoa II Nguyễn Văn Trung chuyên phẫu thuật tái tạo màng nhĩ, điều trị điếc và các bệnh lý tai xương chũm.", 19, new BigDecimal("350000"));
        createDoctor(createUser("0901000115", "BS.CKI. Lê Thị Thủy", "thuy.lt@shp.vn", true, roleDoctor),
                spTaiMuiHong, "BS.CKI", "Bác sĩ Chuyên khoa I Lê Thị Thủy chuyên điều trị viêm họng hạt, viêm amidan và các bệnh dị ứng mũi xoang.", 8, new BigDecimal("200000"));

        // Răng Hàm Mặt
        createDoctor(createUser("0901000116", "BS.CKII. Đỗ Minh Trí", "tri.dm@shp.vn", true, roleDoctor),
                spRangHamMat, "BS.CKII", "Bác sĩ Chuyên khoa II Đỗ Minh Trí là chuyên gia về phục hình răng sứ, cấy ghép Implant và chỉnh nha thẩm mỹ với hơn 18 năm kinh nghiệm.", 18, new BigDecimal("350000"));
        createDoctor(createUser("0901000117", "ThS.BS. Trần Văn Hoàng", "hoang.tv@shp.vn", true, roleDoctor),
                spRangHamMat, "ThS.BS", "Thạc sĩ, Bác sĩ Trần Văn Hoàng chuyên sâu về nhổ răng khôn không đau và điều trị nội nha.", 13, new BigDecimal("250000"));
        createDoctor(createUser("0901000118", "BS.CKI. Nguyễn Thu Hà", "ha.nt@shp.vn", true, roleDoctor),
                spRangHamMat, "BS.CKI", "Bác sĩ Chuyên khoa I Nguyễn Thu Hà chuyên chỉnh nha (niềng răng) mắc cài và khay trong suốt trẻ em và người lớn.", 8, new BigDecimal("250000"));

        // Mắt
        createDoctor(createUser("0901000119", "ThS.BS. Vũ Thị Hồng Nhung", "nhung.vth@shp.vn", true, roleDoctor),
                spMat, "ThS.BS", "Thạc sĩ, Bác sĩ Vũ Thị Hồng Nhung chuyên khám điều trị tật khúc xạ, mổ cận thị bằng phương pháp Lasik và điều trị đục thủy tinh thể.", 14, new BigDecimal("300000"));
        createDoctor(createUser("0901000120", "BS.CKII. Lê Anh Đức", "duc.la@shp.vn", true, roleDoctor),
                spMat, "BS.CKII", "Bác sĩ Chuyên khoa II Lê Anh Đức chuyên sâu về phẫu thuật Phaco điều trị đục thủy tinh thể và các bệnh võng mạc đái tháo đường.", 21, new BigDecimal("400000"));
        createDoctor(createUser("0901000121", "BS.CKI. Nguyễn Thị Lan", "lan.nt@shp.vn", true, roleDoctor),
                spMat, "BS.CKI", "Bác sĩ Chuyên khoa I Nguyễn Thị Lan chuyên khám và điều trị hội chứng khô mắt, viêm kết mạc, đo khúc xạ trẻ em.", 7, new BigDecimal("200000"));

        // Nội Tổng Quát
        createDoctor(createUser("0901000122", "PGS.TS.BS. Hoàng Trung Kiên", "kien.ht@shp.vn", true, roleDoctor),
                spNoiTongQuat, "PGS.TS.BS", "Phó Giáo sư, Tiến sĩ, Bác sĩ Hoàng Trung Kiên có kinh nghiệm sâu rộng trong chẩn đoán và điều trị các bệnh mạn tính như tiểu đường, huyết áp, gout.", 30, new BigDecimal("450000"));
        createDoctor(createUser("0901000124", "ThS.BS. Phạm Thanh Sơn", "son.pt@shp.vn", true, roleDoctor),
                spNoiTongQuat, "ThS.BS", "Thạc sĩ, Bác sĩ Phạm Thanh Sơn tốt nghiệp ĐH Y Hà Nội, chuyên điều trị các bệnh lý đường tiêu hóa và gan mật.", 11, new BigDecimal("250000"));
        // Gắn bác sĩ Trần Thị Bác Sĩ hiện tại vào Khoa Nội Tổng Quát
        createDoctor(doc1, spNoiTongQuat, "BS.CKI", "Bác sĩ chuyên khoa I Trần Thị Bác Sĩ chuyên khám và điều trị các bệnh nội khoa.", 10, new BigDecimal("200000"));

        // === 6. Tao 15 loai thuoc mau ===
        createMedicine("Paracetamol 500mg", "Viên", "2000", 10000, "Giảm đau, hạ sốt (Ngày 2-3 lần, mỗi lần 1 viên, cách nhau 4-6 tiếng)");
        createMedicine("Amoxicillin 500mg", "Viên", "3500", 5000, "Kháng sinh điều trị nhiễm khuẩn (Uống sau ăn)");
        createMedicine("Omeprazol 20mg", "Viên", "5000", 8000, "Điều trị viêm loét dạ dày, trào ngược (Uống trước ăn 30 phút)");
        createMedicine("Cetirizin 10mg", "Viên", "4000", 7000, "Thuốc kháng histamin, trị dị ứng, sổ mũi (Ngày 1 viên)");
        createMedicine("Ibuprofen 400mg", "Viên", "3000", 6000, "Kháng viêm, giảm đau (Uống sau ăn no)");
        createMedicine("Vitamin C 500mg", "Viên", "1500", 15000, "Bổ sung vitamin, tăng đề kháng");
        createMedicine("Oresol", "Gói", "2500", 12000, "Bù nước và điện giải (Pha với lượng nước vừa đủ theo hướng dẫn)");
        createMedicine("Berberin 10mg", "Viên", "500", 20000, "Điều trị tiêu chảy, hội chứng ruột kích thích");
        createMedicine("Salbutamol 2mg", "Viên", "1800", 4000, "Giãn phế quản, dùng trong hen suyễn");
        createMedicine("Loratadin 10mg", "Viên", "3500", 6500, "Thuốc chống dị ứng (Không gây buồn ngủ)");
        createMedicine("Metformin 500mg", "Viên", "2500", 8000, "Điều trị đái tháo đường tuýp 2");
        createMedicine("Amlodipin 5mg", "Viên", "3000", 7500, "Hạ huyết áp, chống đau thắt ngực");
        createMedicine("Azithromycin 500mg", "Viên", "12000", 3000, "Kháng sinh (Dùng theo chỉ định bác sĩ, thường dùng 3-5 ngày)");
        createMedicine("Smecta", "Gói", "4500", 5000, "Điều trị tiêu chảy cấp (Pha vào nửa ly nước)");
        createMedicine("Natri Clorid 0.9%", "Chai", "15000", 2000, "Nước muối sinh lý, rửa mắt mũi hoặc vết thương");

        // === 7. Tao 6 Loai Xet Nghiem Co Dinh (Lab Tests) ===
        createLabTest("Xét nghiệm máu toàn bộ (CBC)", "150000", "Đếm và phân loại các tế bào máu: Hồng cầu, Bạch cầu, Tiểu cầu, Hemoglobin, Hematocrit.", "HEMATOLOGY");
        createLabTest("Sinh hóa máu (Glucose, Ure, Creatinin)", "200000", "Đánh giá chức năng gan thận, đường huyết lúc đói, chỉ số mỡ máu cơ bản.", "HEMATOLOGY");
        createLabTest("Xét nghiệm nước tiểu toàn bộ", "80000", "Phân tích thành phần nước tiểu: pH, protein, glucose, hồng cầu, bạch cầu, vi khuẩn.", "HEMATOLOGY");
        createLabTest("Siêu âm ổ bụng tổng quát", "250000", "Khảo sát hình ảnh gan, mật, tụy, lách, thận và bàng quang bằng sóng siêu âm.", "ULTRASOUND");
        createLabTest("Điện tâm đồ (ECG)", "100000", "Ghi nhận hoạt động điện tim, phát hiện rối loạn nhịp, thiếu máu cơ tim.", "ULTRASOUND");
        createLabTest("Chụp X-Quang ngực thẳng", "150000", "Chụp hình ảnh phổi, tim, xương sườn để phát hiện viêm phổi, tràn dịch, u bướu.", "IMAGING");

        System.out.println("[DataSeeder] Hoàn tất! Đã tạo 8 chuyên khoa, 24 bác sĩ, 6 loại xét nghiệm mẫu.");
        System.out.println("[DataSeeder] Đăng nhập bằng số điện thoại, mật khẩu: 123456");
        System.out.println("  - 0901000001 (Bệnh nhân) / 0901000002 (Bác sĩ) / 0901000003 (Kỹ thuật viên)");
    }

    private Role createRole(RoleName name) {
        Role role = new Role();
        role.setName(name);
        return roleRepository.save(role);
    }

    private User createUser(String phone, String fullName, String email,
                            boolean profileCompleted, Role role) {
        User user = new User();
        user.setPhoneNumber(phone);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setFullName(fullName);
        user.setStatus(UserStatus.ACTIVE);
        user.setProfileCompleted(profileCompleted);
        user.setCreatedBy("SYSTEM");
        user.setCreatedAt(LocalDateTime.now());

        user = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setCreatedBy("SYSTEM");
        userRole.setCreatedAt(LocalDateTime.now());

        user.getUserRoles().add(userRole);
        userRepository.save(user);

        return user;
    }

    private Specialty createSpecialty(String name, String description) {
        Specialty specialty = new Specialty();
        specialty.setName(name);
        specialty.setDescription(description);
        specialty.setCreatedBy("SYSTEM");
        specialty.setCreatedAt(LocalDateTime.now());
        return specialtyRepository.save(specialty);
    }

    private Doctor createDoctor(User user, Specialty specialty, String academicRank, String biography,
                                 int expYears, BigDecimal examFee) {
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialty(specialty);
        doctor.setAcademicRank(academicRank);
        doctor.setBiography(biography);
        doctor.setExperienceYears(expYears);
        doctor.setExamFee(examFee);
        doctor.setStatus(DoctorStatus.ACTIVE);
        doctor.setCreatedBy("SYSTEM");
        doctor.setCreatedAt(LocalDateTime.now());
        return doctorRepository.save(doctor);
    }

    private void createMedicine(String name, String unit, String price, int stock, String instruction) {
        Medicine medicine = new Medicine();
        medicine.setName(name);
        medicine.setUnit(unit);
        medicine.setPrice(new BigDecimal(price));
        medicine.setStockQuantity(stock);
        medicine.setUsageInstruction(instruction);
        medicine.setStatus(MedicineStatus.SELLING);
        medicine.setCreatedBy("SYSTEM");
        medicine.setCreatedAt(LocalDateTime.now());
        medicineRepository.save(medicine);
    }

    private void createLabTest(String name, String price, String description, String roomType) {
        LabTest labTest = new LabTest();
        labTest.setName(name);
        labTest.setPrice(new BigDecimal(price));
        labTest.setDescription(description);
        labTest.setRoomType(roomType);
        labTest.setCreatedBy("SYSTEM");
        labTest.setCreatedAt(LocalDateTime.now());
        labTestRepository.save(labTest);
    }
}
