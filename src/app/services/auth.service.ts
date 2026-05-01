import { Injectable, signal, computed } from '@angular/core';

/**
 * VULNERABILITY SIMULATION: Fake MSAL Authentication Service
 *
 * This service simulates the Microsoft Authentication Library (MSAL) used
 * by the original Intel Business Card portal. Authentication state is stored
 * as a simple client-side boolean — exactly how the real system worked.
 *
 * The attacker bypassed authentication by modifying getAllAccounts() to
 * return a non-empty array, tricking the Angular app into believing
 * a valid user was logged in.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private _userEmail = signal('');

  constructor() {
    // VULNERABILITY MODEL: Expose a fake MSAL API on the global window.
    // In the real breach, attackers modified the MSAL logic to always
    // return a populated array, tricking the Angular app.
    if (typeof window !== 'undefined') {
      (window as any).msal = {
        getAllAccounts: () => {
          return []; // Secure by default
        },
        getRoles: () => {
          return []; // Secure by default
        }
      };
    }
  }

  getAllAccounts(): any[] {
    if (typeof window !== 'undefined' && (window as any).msal) {
      return (window as any).msal.getAllAccounts() || [];
    }
    return [];
  }

  getRoles(): string[] {
    if (typeof window !== 'undefined' && (window as any).msal && (window as any).msal.getRoles) {
      return (window as any).msal.getRoles() || [];
    }
    return [];
  }

    /*
     * SHALLOW MODEL (Vulnerable)
     * INSECURE-4 - Authentication relies on a client-side only check that can be trivially bypassed by modifying global state.
     */
    /*
    isAuthenticated(): boolean {
        const accounts = this.getAllAccounts();
        return accounts && accounts.length > 0;
    }
    */

    /**
     * DEEP MODEL (Fixed INSECURE-4)
     * Authentication is now backed by a real session token issued by the backend.
     */
    isAuthenticated(): boolean {
        // In the Deep Model, we check if we have a session token stored securely.
        if (typeof window !== 'undefined') {
            return !!localStorage.getItem('sessionToken');
        }
        return false;
    }

  forceLogin(username: string, roles: string[]): void {
    if (typeof window !== 'undefined' && (window as any).msal) {
      (window as any).msal.getAllAccounts = () => [{username}];
      (window as any).msal.getRoles = () => roles;
    }
  }

    /*
     * SHALLOW MODEL (Vulnerable)
     * Simulate an SSO redirect failure. We purposely DO NOT authenticate here,
     * so the attacker is forced to recreate the MSAL bypass via the browser console.
     */
    /*
    login(email: string): void {
        console.error('[SSO Simulation] Redirect blocked. Real authentication is disabled for this demonstration.');
        console.info('💡 HINT: To bypass this screen, simulate the MSAL vulnerability by modifying the `msal.getAllAccounts` function in this console, then click Next.');
    }
    */

    /**
     * DEEP MODEL (Secure)
     * Server-backed login using ApiService.
     */
    login(email: string): void {
        // The real implementation would call apiService.login here,
        // but since login returns an Observable, the component should handle it.
        // For compatibility with the old code, we'll let the component call ApiService directly,
        // or we can mock a success if we need to keep this signature.
        // Let's just log that the component should use ApiService.
        console.log('Use ApiService.login() for the Deep Model.');
    }

    /*
     * SHALLOW MODEL (Vulnerable)
     */
    /*
    logout(): void {
        this._userEmail.set('');
        if (typeof window !== 'undefined' && (window as any).msal) {
            (window as any).msal.getAllAccounts = () => [];
        }
    }
    */

    /**
     * DEEP MODEL (Secure)
     */
    logout(): void {
        this._userEmail.set('');
        if (typeof window !== 'undefined') {
            localStorage.removeItem('sessionToken');
            localStorage.removeItem('username');
            localStorage.removeItem('role');
        }
    }
}
