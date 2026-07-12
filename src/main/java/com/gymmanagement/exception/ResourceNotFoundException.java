package com.gymmanagement.exception;

/** Thrown when a requested entity (member, plan, trainer, etc.) does not exist. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
