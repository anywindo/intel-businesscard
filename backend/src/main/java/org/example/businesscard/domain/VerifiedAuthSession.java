package org.example.businesscard.domain;

/**
 * DEEP MODEL — Domain Primitive for Verified Auth Sessions
 *
 * Validates an incoming HTTP request's session token and binds it
 * to an authenticated identity. This is the "gateway" object used
 * by the secure service layer — if you have a VerifiedAuthSession,
 * you are guaranteed to be an authenticated user with a valid session.
 *
 * Invariants:
 *   - Session token must resolve to a valid, non-expired SecureUserSession
 *   - Identity (username) is bound to the session at construction
 *   - Immutable after construction
 */
public final class VerifiedAuthSession {

    private final String username;
    private final Role role;

    private VerifiedAuthSession(String username, Role role) {
        this.username = username;
        this.role = role;
    }

    /**
     * Factory method: validates a Bearer token from an HTTP Authorization header.
     *
     * @param authHeader the raw "Bearer <token>" header value
     * @throws SecurityException if the header is missing, malformed, or the token is invalid/expired
     */
    public static VerifiedAuthSession fromAuthorizationHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Missing or malformed Authorization header");
        }
        String token = authHeader.substring(7).trim();

        // Delegates to SecureUserSession which handles lookup + expiry check
        SecureUserSession session = SecureUserSession.fromToken(token);

        return new VerifiedAuthSession(session.getUsername(), session.getRole());
    }

    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public boolean isAdmin() { return role == Role.ADMIN; }
}
