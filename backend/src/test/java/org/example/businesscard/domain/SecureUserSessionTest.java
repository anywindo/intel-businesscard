package org.example.businesscard.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SecureUserSessionTest {

    @Test
    public void testValidCredentialsCreatesSession() {
        SecureUserSession session = SecureUserSession.create("user@intel.com", "UserP@ss2024!");
        assertNotNull(session);
        assertNotNull(session.getSessionToken());
        assertEquals("user@intel.com", session.getUsername());
        assertEquals(Role.USER, session.getRole());
        assertFalse(session.isAdmin());
        assertFalse(session.isExpired());
    }

    @Test
    public void testAdminCredentialsCreatesAdminSession() {
        SecureUserSession session = SecureUserSession.create("admin@intel.com", "SecureP@ss2024!");
        assertEquals(Role.ADMIN, session.getRole());
        assertTrue(session.isAdmin());
    }

    @Test
    public void testInvalidPasswordThrows() {
        SecurityException ex = assertThrows(SecurityException.class, () -> {
            SecureUserSession.create("user@intel.com", "WrongPassword");
        });
        assertTrue(ex.getMessage().contains("Invalid credentials"));
    }

    @Test
    public void testUnknownUserThrows() {
        SecurityException ex = assertThrows(SecurityException.class, () -> {
            SecureUserSession.create("unknown@intel.com", "AnyPassword");
        });
        assertTrue(ex.getMessage().contains("Invalid credentials"));
    }

    @Test
    public void testNullUsernameThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            SecureUserSession.create(null, "UserP@ss2024!");
        });
    }

    @Test
    public void testFromTokenSuccess() {
        SecureUserSession session1 = SecureUserSession.create("user@intel.com", "UserP@ss2024!");
        String token = session1.getSessionToken();
        
        SecureUserSession session2 = SecureUserSession.fromToken(token);
        assertEquals(session1.getUsername(), session2.getUsername());
    }

    @Test
    public void testFromInvalidTokenThrows() {
        assertThrows(SecurityException.class, () -> {
            SecureUserSession.fromToken("invalid-token");
        });
    }
}
