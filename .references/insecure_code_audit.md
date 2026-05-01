# Insecure Code Audit — Intel Business Card System

This document catalogs the security vulnerabilities identified in the "Shallow Model" implementation of the Intel Business Card System, alongside the "Deep Model" fixes applied. These vulnerabilities simulate the real-world design flaws discovered in the 2024 "Intel Outside" data breach.

## 1. Backend Vulnerabilities

### [INSECURE-1] Unauthenticated Token Generation
- **File**: `backend/src/main/java/org/example/businesscard/service/BusinessCardService.java`
- **Logic**: The `generateToken()` method returns a static, highly privileged administrative token (`SUPER_PRIVILEGED_TOKEN_123`) to any caller without performing any authentication or identity verification.
- **Impact**: Any anonymous user can obtain a valid access token to query sensitive employee data.
- **Resolution [DEEP MODEL]**: The method was replaced with `generateToken(username, password)` which returns a `SecureUserSession` domain primitive. This primitive guarantees that a token is only issued if the credentials are valid, enforcing authentication at the domain level.

**Shallow Model (Vulnerable)**
```java
public String generateAnonymousToken() {
    return "SUPER_PRIVILEGED_TOKEN_123";
}
```

**Deep Model (Secure)**
```java
public SecureUserSession generateToken(String username, String password) {
    return SecureUserSession.create(username, password);
}
```

### [INSECURE-2] Data Over-fetching via Primitive Obsession
- **File**: `backend/src/main/java/org/example/businesscard/service/BusinessCardService.java`
- **Logic**: The `getEmployees()` method accepts a raw `String` search filter. If the filter is null or empty, the logic defaults to `employeeRepository.findAll()`.
- **Impact**: Attackers can dump the entire employee database (all 151 records in this demo, 270,000 in the real breach) by simply sending an empty search query.
- **Resolution [DEEP MODEL]**: The method signature was changed to `getEmployees(VerifiedAuthSession session, EmployeeSearchQuery query)`. The `EmployeeSearchQuery` domain primitive throws an exception if the query is empty or less than 2 characters, structurally eliminating the `findAll()` code path.

**Shallow Model (Vulnerable)**
```java
public List<Employee> getEmployees(String token, String searchFilter) {
    if (searchFilter == null || searchFilter.trim().isEmpty()) {
        return employeeRepository.findAll();
    }
    return employeeRepository.findByFullNameContainingIgnoreCase(searchFilter);
}
```

**Deep Model (Secure)**
```java
public List<Employee> getEmployees(VerifiedAuthSession session, EmployeeSearchQuery query) {
    return employeeRepository.findByFullNameContainingIgnoreCase(query.getValue());
}
```

### [INSECURE-3] Overly Permissive Security Configuration
- **File**: `backend/src/main/java/org/example/businesscard/config/SecurityConfig.java`
- **Logic**: The Spring Security configuration uses `.requestMatchers("/api/**").permitAll()`.
- **Impact**: Disables all framework-level protection for the API endpoints, relying solely on the insecure token check within the service layer.
- **Resolution [DEEP MODEL]**: Updated to `denyAll()` by default. Specifically opened `/api/auth/**` for login and `/api/secure/**` for secure access.

**Shallow Model (Vulnerable)**
```java
http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/**").permitAll()
        .anyRequest().authenticated()
);
```

**Deep Model (Secure)**
```java
http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/secure/**").permitAll()
        .anyRequest().denyAll()
);
```

---

## 2. Frontend Vulnerabilities

### [INSECURE-4] Client-Side Only Authentication Bypass
- **File**: `src/app/services/auth.service.ts`
- **Logic**: The `isAuthenticated()` method checks the length of an array in a global `window.msal` object.
- **Impact**: Attackers can bypass the login screen by injecting a fake account object into the global `msal` variable via the browser's developer console.
- **Resolution [DEEP MODEL]**: Modified `isAuthenticated()` to verify the presence of a secure, server-issued `sessionToken` in localStorage, eliminating the reliance on manipulatable global variables.

**Shallow Model (Vulnerable)**
```typescript
isAuthenticated(): boolean {
    const w = window as any;
    if (w.msal && w.msal.getAllAccounts) {
        const accounts = w.msal.getAllAccounts();
        return accounts && accounts.length > 0;
    }
    return false;
}
```

**Deep Model (Secure)**
```typescript
isAuthenticated(): boolean {
    return !!localStorage.getItem('sessionToken');
}
```

### [INSECURE-5] Insecure Token Acquisition
- **File**: `src/app/services/api.service.ts`
- **Logic**: The frontend proactively calls `/api/token` to fetch a privileged token before making data requests.
- **Impact**: Exposes the insecure token generation mechanism to any browser-based user, facilitating session hijacking and unauthorized data access.
- **Resolution [DEEP MODEL]**: Replaced with a real `login(credentials)` method that hits the secure backend `/api/auth/login` endpoint, returning a token only upon successful verification.

**Shallow Model (Vulnerable)**
```typescript
getAccessToken(): Observable<{ accessToken: string }> {
    return this.http.get<{ accessToken: string }>(`${this.baseUrl}/token`);
}
```

**Deep Model (Secure)**
```typescript
login(credentials: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/auth/login`, credentials);
}
```

### [INSECURE-6] Unconstrained Primitive Search
- **File**: `src/app/services/api.service.ts`
- **Logic**: The `getEmployees()` method passes raw, unvalidated primitive strings directly to the backend.
- **Impact**: Lack of domain-level constraints in the frontend allows the "empty search" attack to be triggered easily from the UI.
- **Resolution [DEEP MODEL]**: Updated to use `getSecureEmployees(search)`, seamlessly integrating with the backend's strict domain primitives and securely attaching the `Bearer` token to headers instead of query params.

**Shallow Model (Vulnerable)**
```typescript
getEmployees(token: string, search: string): Observable<EmployeeResponse> {
    let params = new HttpParams().set('token', token);
    if (search) params = params.set('search', search);
    return this.http.get<EmployeeResponse>(`${this.baseUrl}/search`, { params });
}
```

**Deep Model (Secure)**
```typescript
getSecureEmployees(search: string): Observable<EmployeeResponse> {
    const token = localStorage.getItem('sessionToken');
    const headers = new HttpHeaders({
        'Authorization': `Bearer ${token}`
    });
    let params = new HttpParams();
    if (search) params = params.set('search', search);
    return this.http.get<EmployeeResponse>(`${this.baseUrl}/secure/search`, { headers, params });
}
```
