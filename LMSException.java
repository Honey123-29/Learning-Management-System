package com.lms.service;

/** Thrown for any business-rule violation (duplicate email, wrong id, not allowed, ...). */
public class LMSException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LMSException(String message) {
        super(message);
    }
}
