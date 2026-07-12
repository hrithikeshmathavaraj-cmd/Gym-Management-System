package com.gymmanagement.exception;

/** Thrown when attempting to create a resource that violates a uniqueness constraint (e.g. email already registered). */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
