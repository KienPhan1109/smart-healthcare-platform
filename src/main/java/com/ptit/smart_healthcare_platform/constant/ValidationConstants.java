package com.ptit.smart_healthcare_platform.constant;

public final class ValidationConstants {
    private ValidationConstants() {}

    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String PHONE_REGEX = "^(0[0-9]{9})$";
    public static final String BHYT_REGEX = "^([A-Z]{2}[0-9]{13})$";
}
