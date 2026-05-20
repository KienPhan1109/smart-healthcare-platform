package com.ptit.smart_healthcare_platform.model.dto.patient;

import com.ptit.smart_healthcare_platform.model.enums.BloodType;
import com.ptit.smart_healthcare_platform.model.enums.Gender;
import com.ptit.smart_healthcare_platform.model.enums.PatientRelation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PatientProfileCreateDto {

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotNull(message = "Mối quan hệ không được để trống")
    private PatientRelation relation;

    @NotNull(message = "Ngày sinh không được để trống")
    @PastOrPresent(message = "Ngày sinh phải ở trong quá khứ hoặc hiện tại")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    @Pattern(regexp = "^([0-9]{12})?$", message = "Căn cước công dân phải bao gồm đúng 12 chữ số")
    private String identityCard;

    private BigDecimal height;

    private BigDecimal weight;

    private BloodType bloodType;

    private String medicalHistory;

    private String allergies;

    @Pattern(regexp = "^([A-Z]{2}[0-9]{13})?$", message = "Mã bảo hiểm y tế phải gồm 2 chữ in hoa và 13 chữ số (VD: GD4797918800001)")
    private String insuranceNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate insuranceExpiryDate;
}
