package com.secureflow.exception;

public class PayPalServiceException extends RuntimeException {

    public PayPalServiceException(String message) {
        super(message);
    }

    public PayPalServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PayPalServiceException(Throwable cause) {
        super(cause);
    }
}