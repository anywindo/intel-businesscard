package org.example.businesscard.service;

import org.example.businesscard.model.Employee;
import org.example.businesscard.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BusinessCardService — handles token generation and employee search.
 *
 * This is the "Shallow Model" implementation for demonstration purposes.
 * It simulates the two vulnerabilities from the Intel "Intel Outside" breach:
 *
 * Vulnerability 1 (Unauthenticated Token):
 *   generateToken() issues a privileged token without any authentication.
 *   Any anonymous caller can obtain a valid token.
 *
 * Vulnerability 2 (Data Over-fetching / Primitive Obsession):
 *   getEmployees() accepts a raw String search filter with no invariants.
 *   Sending an empty or null filter dumps the entire employee database.
 */
@Service
public class BusinessCardService {

    private static final String PRIVILEGED_TOKEN = "SUPER_PRIVILEGED_TOKEN_123";

    private final EmployeeRepository employeeRepository;

    public BusinessCardService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /*
     * SHALLOW MODEL (Vulnerable)
     * Anyone can call this endpoint and receive a valid access token.
     */
    /*
    public String generateToken() {
        // INSECURE-1 - Unauthenticated privileged token generation.
        return PRIVILEGED_TOKEN;
    }
    */

    /**
     * DEEP MODEL (Fixed INSECURE-1)
     * Requires valid credentials to generate a SecureUserSession.
     */
    public org.example.businesscard.domain.SecureUserSession generateToken(String username, String password) {
        return org.example.businesscard.domain.SecureUserSession.create(username, password);
    }

    /*
     * SHALLOW MODEL (Vulnerable)
     * No invariants are enforced on searchFilter — a null or empty value
     * triggers findAll(), dumping the entire employee database.
     */
    /*
    public List<Employee> getEmployees(String token, String searchFilter) {
        if (!PRIVILEGED_TOKEN.equals(token)) {
            throw new SecurityException("Invalid token");
        }

        // INSECURE-2 - Primitive obsession allows null/empty filter to dump entire database.
        if (searchFilter == null || searchFilter.trim().isEmpty()) {
            return employeeRepository.findAll();
        }

        return employeeRepository.findByFullNameContainingIgnoreCase(searchFilter);
    }
    */

    /**
     * DEEP MODEL (Fixed INSECURE-2)
     * Requires a VerifiedAuthSession and a validated EmployeeSearchQuery.
     * The findAll() code path is eliminated entirely.
     */
    public List<Employee> getEmployees(
            org.example.businesscard.domain.VerifiedAuthSession session, 
            org.example.businesscard.domain.EmployeeSearchQuery query) {
        return employeeRepository.findByFullNameContainingIgnoreCase(query.getValue());
    }
}
