package org.example.businesscard.controller;

import org.example.businesscard.model.Employee;
import org.example.businesscard.service.BusinessCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BusinessCardController — exposes the Business Card System REST API.
 *
 * Endpoints:
 *   GET /api/token      — Returns an access token (no authentication required)
 *   GET /api/employees  — Returns employee records matching the search filter
 */
@RestController
@RequestMapping("/api")
public class BusinessCardController {

    private final BusinessCardService businessCardService;

    public BusinessCardController(BusinessCardService businessCardService) {
        this.businessCardService = businessCardService;
    }

    /*
     * SHALLOW MODEL (Vulnerable)
     * Returns a privileged access token without requiring any credentials.
     * Simulates the unauthenticated token API from the Intel breach.
     */
    /*
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getToken() {
        String token = businessCardService.generateToken();
        return ResponseEntity.ok(Map.of("accessToken", token));
    }
    */

    /**
     * DEEP MODEL (Fixed INSECURE-1)
     * Authenticates the user and returns a session token.
     * Requires valid credentials to instantiate SecureUserSession.
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");

            org.example.businesscard.domain.SecureUserSession session = businessCardService.generateToken(username, password);

            return ResponseEntity.ok(Map.of(
                    "sessionToken", session.getSessionToken(),
                    "username", session.getUsername(),
                    "role", session.getRole().name()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /*
     * SHALLOW MODEL (Vulnerable)
     * Returns employees matching the search filter.
     * If search is empty, all employee records are returned (data dump vulnerability).
     */
    /*
    @GetMapping("/employees")
    public ResponseEntity<?> getEmployees(
            @RequestParam String token,
            @RequestParam(defaultValue = "") String search) {

        try {
            List<Employee> employees = businessCardService.getEmployees(token, search);
            return ResponseEntity.ok(Map.of(
                    "count", employees.size(),
                    "data", employees));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
    */

    /**
     * DEEP MODEL (Fixed INSECURE-2)
     * Requires Authorization header (VerifiedAuthSession) and
     * a valid search query (EmployeeSearchQuery).
     *
     * If either domain primitive fails construction, the request is rejected.
     */
    @GetMapping("/secure/employees")
    public ResponseEntity<?> getSecureEmployees(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "") String search) {

        try {
            org.example.businesscard.domain.VerifiedAuthSession session = 
                org.example.businesscard.domain.VerifiedAuthSession.fromAuthorizationHeader(authHeader);

            org.example.businesscard.domain.EmployeeSearchQuery query = 
                new org.example.businesscard.domain.EmployeeSearchQuery(search);

            List<Employee> employees = businessCardService.getEmployees(session, query);

            return ResponseEntity.ok(Map.of(
                    "count", employees.size(),
                    "data", employees,
                    "authenticatedAs", session.getUsername()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
