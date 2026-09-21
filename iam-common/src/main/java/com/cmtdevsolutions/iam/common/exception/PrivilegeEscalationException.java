package com.cmtdevsolutions.iam.common.exception;

public class PrivilegeEscalationException extends RuntimeException {

    public PrivilegeEscalationException(String message) {
        super(message);
    }

    public PrivilegeEscalationException(String message, Throwable cause) {
        super(message, cause);
    }
}