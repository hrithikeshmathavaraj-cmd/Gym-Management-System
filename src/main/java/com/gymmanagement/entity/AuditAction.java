package com.gymmanagement.entity;

/**
 * The kind of authentication event a {@link LoginAudit} record represents.
 */
public enum AuditAction {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT
}
