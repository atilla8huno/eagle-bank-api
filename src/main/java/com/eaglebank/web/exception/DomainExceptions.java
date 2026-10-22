package com.eaglebank.web.exception;

public final class DomainExceptions {

    private DomainExceptions() {
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class ForbiddenOperationException extends RuntimeException {
        public ForbiddenOperationException(String message) {
            super(message);
        }
    }

    public static class AuthenticationFailedException extends RuntimeException {
        public AuthenticationFailedException(String message) {
            super(message);
        }
    }

    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

    public static class InvalidTransactionException extends RuntimeException {
        public InvalidTransactionException(String message) {
            super(message);
        }
    }
}
