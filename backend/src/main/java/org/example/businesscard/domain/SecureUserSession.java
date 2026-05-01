package org.example.businesscard.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DEEP MODEL — Domain Primitive for Authenticated Sessions
 *
 * Fixes INSECURE-1: Unauthenticated privileged token generation.
 *
 * Before (Shallow Model):
 *   generateToken() returned a static string "SUPER_PRIVILEGED_TOKEN_123"
 *   to any anonymous caller without any authentication.
 *
 * After (Deep Model):
 *   Sessions can ONLY be created via SecureUserSession.create(username, password).
 *   The factory method validates credentials before issuing a cryptographic
 *   session token (UUID). Invalid credentials throw an exception.
 *
 * Invariants:
 *   - Private constructor — cannot be instantiated directly
 *   - Credentials must match the known user store
 *   - Session token is a cryptographic UUID (not a static string)
 *   - Session has an expiry time (1 hour)
 *   - No setters — immutable after construction
 */
public final class SecureUserSession {

    // Simulated credential store (in production: database + bcrypt)
    private static final Map<String, String> CREDENTIAL_STORE = Map.of(
            "admin@intel.com", "SecureP@ss2024!",
            "user@intel.com", "UserP@ss2024!"
    );

    // In-memory session store — maps token → session
    private static final ConcurrentHashMap<String, SecureUserSession> SESSION_STORE = new ConcurrentHashMap<>();

    private final String username;
    private final Role role;
    private final String sessionToken;
    private final Instant expiresAt;

    // Private constructor — only accessible via create()
    private SecureUserSession(String username, Role role, String sessionToken, Instant expiresAt) {
        this.username = username;
        this.role = role;
        this.sessionToken = sessionToken;
        this.expiresAt = expiresAt;
    }

    /**
     * The ONLY way to create a session.
     * Validates credentials, then issues a cryptographic session token.
     *
     * @throws SecurityException if credentials are invalid
     */
    public static SecureUserSession create(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be empty");
        }

        String expectedPassword = CREDENTIAL_STORE.get(username);
        if (expectedPassword == null || !expectedPassword.equals(password)) {
            throw new SecurityException("Invalid credentials — access denied");
        }

        Role role = username.startsWith("admin") ? Role.ADMIN : Role.USER;
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plusSeconds(3600); // 1 hour

        SecureUserSession session = new SecureUserSession(username, role, token, expiry);
        SESSION_STORE.put(token, session);
        return session;
    }

    /**
     * Looks up and validates a session by its token.
     *
     * @throws SecurityException if token is unknown or expired
     */
    public static SecureUserSession fromToken(String token) {
        if (token == null || token.isBlank()) {
            throw new SecurityException("Session token must not be empty");
        }
        SecureUserSession session = SESSION_STORE.get(token);
        if (session == null) {
            throw new SecurityException("Invalid session token — access denied");
        }
        if (session.isExpired()) {
            SESSION_STORE.remove(token);
            throw new SecurityException("Session has expired — please login again");
        }
        return session;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    // NO setAuthenticated() — state is immutable
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public String getSessionToken() { return sessionToken; }
    public boolean isAdmin() { return role == Role.ADMIN; }
}
