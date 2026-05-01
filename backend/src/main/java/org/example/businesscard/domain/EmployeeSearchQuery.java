package org.example.businesscard.domain;

/**
 * DEEP MODEL — Domain Primitive for Employee Search
 *
 * Fixes INSECURE-2: Data over-fetching via primitive obsession.
 *
 * Before (Shallow Model):
 *   The service accepted a raw String searchFilter. If null or empty,
 *   it called findAll(), dumping the entire employee database (270,000 records).
 *
 * After (Deep Model):
 *   This domain primitive enforces invariants at the constructor level.
 *   An empty or too-short query throws an exception immediately —
 *   the findAll() code path can never be reached.
 *
 * Invariants:
 *   - Query must not be null or blank
 *   - Query must be at least 2 characters after trimming
 */
public final class EmployeeSearchQuery {

    private static final int MINIMUM_QUERY_LENGTH = 2;
    private final String value;

    public EmployeeSearchQuery(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Search query must not be empty");
        }
        String trimmed = value.trim();
        if (trimmed.length() < MINIMUM_QUERY_LENGTH) {
            throw new IllegalArgumentException(
                    "Search query must be at least " + MINIMUM_QUERY_LENGTH + " characters"
            );
        }
        this.value = trimmed;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "EmployeeSearchQuery{" + value + "}";
    }
}
