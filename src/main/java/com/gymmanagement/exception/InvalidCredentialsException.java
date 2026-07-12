package com.gymmanagement.exception;

/** Thrown when login credentials (email/password) are invalid. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
