package com.calligraphy.platform.exception;

public class AdminUserAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public AdminUserAlreadyExistsException(String message) {
        super(message);
    }

    public AdminUserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}


