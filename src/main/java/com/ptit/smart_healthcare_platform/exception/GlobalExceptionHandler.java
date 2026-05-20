package com.ptit.smart_healthcare_platform.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Bắt lỗi 404
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFoundError(NoHandlerFoundException ex, Model model) {
        logger.error("404 Error: {}", ex.getMessage());
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "Không tìm thấy trang yêu cầu.");
        return "error";
    }

    // Bắt lỗi hệ thống 500
    @ExceptionHandler(Exception.class)
    public String handleSystemError(Exception ex, Model model) {
        logger.error("500 System Error: ", ex);
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống cục bộ. Đội ngũ kỹ thuật đã được thông báo.");
        return "error";
    }
}
