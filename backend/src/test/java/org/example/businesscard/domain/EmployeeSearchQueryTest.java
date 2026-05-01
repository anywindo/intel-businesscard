package org.example.businesscard.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmployeeSearchQueryTest {

    @Test
    public void testValidQuery() {
        EmployeeSearchQuery query = new EmployeeSearchQuery("Anders");
        assertEquals("Anders", query.getValue());
    }

    @Test
    public void testQueryTrimmed() {
        EmployeeSearchQuery query = new EmployeeSearchQuery("  Smith  ");
        assertEquals("Smith", query.getValue());
    }

    @Test
    public void testNullQueryThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new EmployeeSearchQuery(null);
        });
        assertTrue(ex.getMessage().contains("must not be empty"));
    }

    @Test
    public void testEmptyQueryThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new EmployeeSearchQuery("   ");
        });
        assertTrue(ex.getMessage().contains("must not be empty"));
    }

    @Test
    public void testTooShortQueryThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new EmployeeSearchQuery("A");
        });
        assertTrue(ex.getMessage().contains("must be at least"));
    }
}
