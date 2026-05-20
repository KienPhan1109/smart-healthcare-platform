package com.ptit.smart_healthcare_platform.model.enums;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Định nghĩa 13 khung giờ khám cố định trong ngày cho mỗi bác sĩ.
 * Mỗi slot kéo dài 30 phút. Lịch cố định tuần nào cũng vậy.
 * Ca sáng: 7 slot (08:00 - 11:30)
 * Ca chiều: 6 slot (13:30 - 16:30)
 */
public enum TimeSlot {
    // Ca sáng (7 slot)
    SLOT_08_00("08:00 - 08:30", 8, 0),
    SLOT_08_30("08:30 - 09:00", 8, 30),
    SLOT_09_00("09:00 - 09:30", 9, 0),
    SLOT_09_30("09:30 - 10:00", 9, 30),
    SLOT_10_00("10:00 - 10:30", 10, 0),
    SLOT_10_30("10:30 - 11:00", 10, 30),
    SLOT_11_00("11:00 - 11:30", 11, 0),

    // Ca chiều (6 slot)
    SLOT_13_30("13:30 - 14:00", 13, 30),
    SLOT_14_00("14:00 - 14:30", 14, 0),
    SLOT_14_30("14:30 - 15:00", 14, 30),
    SLOT_15_00("15:00 - 15:30", 15, 0),
    SLOT_15_30("15:30 - 16:00", 15, 30),
    SLOT_16_00("16:00 - 16:30", 16, 0);

    private final String displayName;
    private final int hour;
    private final int minute;

    TimeSlot(String displayName, int hour, int minute) {
        this.displayName = displayName;
        this.hour = hour;
        this.minute = minute;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    /**
     * Chuyển đổi slot giờ + ngày thành LocalDateTime để lưu vào cột appointment_time.
     */
    public LocalDateTime toLocalDateTime(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }

    /**
     * Kiểm tra xem slot này có thuộc ca sáng hay không.
     */
    public boolean isMorning() {
        return hour < 12;
    }
}
