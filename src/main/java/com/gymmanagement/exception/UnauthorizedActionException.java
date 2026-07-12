package com.gymmanagement.exception;

/** Thrown when an authenticated user attempts an action they are not permitted to perform. */
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
