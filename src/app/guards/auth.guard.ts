import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * VULNERABILITY: Client-side route guard.
 *
 * This guard only checks the client-side boolean from AuthService.
 * It mirrors the original Intel system where the Angular frontend
 * used MSAL's getAllAccounts() to decide if a user was "logged in".
 *
 * The backend never independently validates the session — so if the
 * attacker sets isAuthenticated = true in the browser, they bypass
 * this guard entirely.
 */
export const authGuard: CanActivateFn = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.isAuthenticated()) {
        return true;
    }

    return router.createUrlTree(['/login']);
};
