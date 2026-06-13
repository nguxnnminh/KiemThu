package com.smartdental.exception;

/**
 * Loi nghiep vu chung. Thong diep hien thi truc tiep cho nguoi dung bang tieng Viet.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
