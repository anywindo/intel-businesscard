import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TokenResponse {
    accessToken: string;
}

export interface Employee {
    id: number;
    fullName: string;
    role: string;
    manager: string;
    email: string;
    phoneNumber: string;
}

export interface EmployeeResponse {
    count: number;
    data: Employee[];
}

export interface ErrorResponse {
    error: string;
}

/**
 * API Service — connects to the Business Card System backend endpoints.
 *
 * Vulnerability 1: getAccessToken() calls the unauthenticated token API.
 *   Any anonymous user can obtain a privileged token.
 *
 * Vulnerability 2: getEmployees() passes a raw string search filter.
 *   If the filter is empty, the API dumps the entire employee database.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
    private readonly baseUrl = '/api';

    constructor(private http: HttpClient) { }

    /*
     * SHALLOW MODEL (Vulnerable)
     * GET /api/token
     * Unauthenticated — returns a privileged token to anyone.
     */
    /*
    getAccessToken(): Observable<TokenResponse> {
        // INSECURE-5 - Client-side calls an unauthenticated endpoint to obtain an administrative token.
        return this.http.get<TokenResponse>(`${this.baseUrl}/token`);
    }
    */

    /**
     * DEEP MODEL (Fixed INSECURE-5)
     * POST /api/auth/login
     * Server-backed authentication. Requires valid credentials.
     */
    login(credentials: { username: string; password: string }): Observable<{ sessionToken: string; username: string; role: string }> {
        return this.http.post<{ sessionToken: string; username: string; role: string }>(`${this.baseUrl}/auth/login`, credentials);
    }

    /*
     * SHALLOW MODEL (Vulnerable)
     * GET /api/employees?token=...&search=...
     * If search is empty, returns ALL records (data over-fetching vulnerability).
     */
    /*
    getEmployees(token: string, search: string): Observable<EmployeeResponse> {
        // INSECURE-6 - Search functionality uses raw primitive strings with no domain constraints, facilitating mass data extraction.
        return this.http.get<EmployeeResponse>(`${this.baseUrl}/employees`, {
            params: { token, search }
        });
    }
    */

    /**
     * DEEP MODEL (Fixed INSECURE-6)
     * GET /api/secure/employees
     * Uses Authorization header and server-validated search inputs.
     */
    getSecureEmployees(search: string): Observable<EmployeeResponse> {
        // The token is now retrieved from a secure client-side storage (e.g., localStorage)
        const token = localStorage.getItem('sessionToken') || '';
        return this.http.get<EmployeeResponse>(`${this.baseUrl}/secure/employees`, {
            params: { search },
            headers: { 'Authorization': `Bearer ${token}` }
        });
    }

    /**
     * DEEP MODEL - Detailed View
     * GET /api/secure/employees/{id}
     */
    getSecureEmployeeDetails(id: number): Observable<{ data: { fullName: string, role: string, email: string }, authenticatedAs: string }> {
        const token = localStorage.getItem('sessionToken') || '';
        return this.http.get<{ data: { fullName: string, role: string, email: string }, authenticatedAs: string }>(`${this.baseUrl}/secure/employees/${id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
    }
}
