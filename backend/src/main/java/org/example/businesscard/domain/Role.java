package org.example.businesscard.domain;

/**
 * DEEP MODEL — Domain Primitive for User Roles
 *
 * Replaces raw String role checks with a bounded enum.
 * The state space is restricted to only known, valid values.
 */
public enum Role {
    USER,
    ADMIN
}
