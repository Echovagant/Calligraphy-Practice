package com.calligraphy.platform.exception;

/**
 * 涓氬姟閫昏緫寮傚父绫伙紝鐢ㄤ簬澶勭悊涓氬姟灞傞潰鐨勯敊璇? */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    private String errorCode;
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}


