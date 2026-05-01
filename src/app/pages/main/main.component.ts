import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService, Employee } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-main',
    imports: [FormsModule],
    templateUrl: './main.component.html',
    styleUrl: './main.component.css'
})
export class MainComponent implements OnInit {
    searchQuery = '';
    accessToken = signal('');
    employees = signal<Employee[]>([]);
    resultCount = signal(0);
    isLoading = signal(false);
    errorMessage = signal('');
    hasSearched = signal(false);
    searchTimeout: any;
    showDropdown = false;

    // Form fields for the business card preview
    email = '';
    fullName = '';
    title = '';
    linkedIn = '';
    consentChecked = false;

    constructor(
        private apiService: ApiService,
        private authService: AuthService,
        private router: Router
    ) { }

    ngOnInit(): void {
        /*
         * SHALLOW MODEL (Vulnerable)
         * VULNERABILITY 2: Unauthenticated Token API
         *
         * On page load, we immediately request a privileged token from the
         * backend without any credentials. In the real breach, this was the
         * unauthenticated getAccessToken API that issued highly privileged
         * tokens to anonymous users.
         */
        /*
        this.apiService.getAccessToken().subscribe({
            next: (response) => {
                this.accessToken.set(response.accessToken);
                console.log('[MSAL] Token acquired:', response.accessToken);
            },
            error: (err) => {
                console.error('[MSAL] Token request failed:', err);
                this.errorMessage.set('Failed to acquire access token.');
            }
        });
        */

        // DEEP MODEL (Fixed)
        // No unauthenticated token requests are made.
        // We rely on the session token generated at login.
    }

    /*
     * SHALLOW MODEL (Vulnerable)
     * VULNERABILITY 3: Data Over-fetching via Primitive Obsession
     *
     * The search filter is a primitive String with no invariants (no min
     * length, no null check enforced). If the user submits an empty search,
     * the backend dumps the entire database — 151 records in our demo,
     * 270,000 records in the real breach.
     */
    onSearch(): void {
        /*
        // SHALLOW MODEL LOGIC:
        const token = this.accessToken();
        if (!token) {
            this.errorMessage.set('No access token available.');
            return;
        }

        this.isLoading.set(true);
        this.errorMessage.set('');
        this.hasSearched.set(true);

        this.apiService.getEmployees(token, this.searchQuery).subscribe({
            next: (response) => {
                this.employees.set(response.data);
                this.resultCount.set(response.count);
                this.isLoading.set(false);

                // Populate the business card preview if exactly 1 result
                if (response.data.length === 1) {
                    const emp = response.data[0];
                    this.email = emp.email;
                    this.fullName = emp.fullName;
                    this.title = emp.role;
                }
            },
            error: (err) => {
                this.isLoading.set(false);
                this.errorMessage.set(err.error?.error || 'Request failed.');
            }
        });
        */

        /**
         * DEEP MODEL (Fixed INSECURE-6)
         * Uses getSecureEmployees.
         */
        if (!this.authService.isAuthenticated()) {
            this.errorMessage.set('Not authenticated.');
            this.router.navigate(['/login']);
            return;
        }

        this.isLoading.set(true);
        this.errorMessage.set('');
        this.hasSearched.set(true);

        this.apiService.getSecureEmployees(this.searchQuery).subscribe({
            next: (response) => {
                this.employees.set(response.data);
                this.resultCount.set(response.count);
                this.isLoading.set(false);

                // Populate the business card preview if exactly 1 result
                if (response.data.length === 1) {
                    const emp = response.data[0];
                    this.email = emp.email;
                    this.fullName = emp.fullName;
                    this.title = emp.role;
                }
            },
            error: (err) => {
                this.isLoading.set(false);
                this.errorMessage.set(err.error?.error || 'Request failed.');
            }
        });
    }

    onSearchInput(query: string): void {
        this.searchQuery = query;
        this.showDropdown = true;

        if (this.searchTimeout) {
            clearTimeout(this.searchTimeout);
        }

        this.searchTimeout = setTimeout(() => {
            this.onSearch();
        }, 300);
    }

    onBlur(): void {
        this.showDropdown = false;
    }

    onSelectCustom(emp: Employee): void {
        this.searchQuery = emp.fullName;
        this.email = emp.email;
        this.fullName = emp.fullName;
        this.title = emp.role;
        this.showDropdown = false;
    }

    onLogout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
