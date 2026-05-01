import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';

@Component({
    selector: 'app-login',
    imports: [FormsModule],
    templateUrl: './login.component.html',
    styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit, AfterViewInit, OnDestroy {
    email = '';
    password = '';
    errorMessage = '';
    @ViewChild('bgCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;
    private animId = 0;
    private checkInterval: any;

    constructor(
        private authService: AuthService,
        private apiService: ApiService,
        private router: Router
    ) { }

    ngOnInit(): void {
        /*
         * SHALLOW MODEL (Vulnerable)
         * Periodically check if MSAL state has been modified via browser console
         * (simulating the client-side bypass).
         */
        /*
        this.checkInterval = setInterval(() => {
            if (this.authService.isAuthenticated()) {
                this.router.navigate(['/card']);
            }
        }, 1000);
        */

        // DEEP MODEL
        // Just check if we are already securely authenticated
        if (this.authService.isAuthenticated()) {
            this.router.navigate(['/card']);
        }
    }

    ngAfterViewInit(): void {
        this.initCanvas();
    }

    ngOnDestroy(): void {
        if (this.checkInterval) {
            clearInterval(this.checkInterval);
        }
        cancelAnimationFrame(this.animId);
    }

    onNext(): void {
        if (!this.email.trim() || !this.password.trim()) {
            this.errorMessage = 'Please enter both your email and password.';
            return;
        }

        /*
         * SHALLOW MODEL (Vulnerable)
         * Simulate SSO redirect failure — user must bypass via browser console
         */
        /*
        this.authService.login(this.email);

        if (this.authService.isAuthenticated()) {
            this.router.navigate(['/card']);
        } else {
            this.errorMessage = 'Authentication Failed.';
        }
        */

        /**
         * DEEP MODEL (Secure)
         * Calls the real backend endpoint to validate credentials and get a session token.
         */
        this.apiService.login({ username: this.email, password: this.password }).subscribe({
            next: (res) => {
                // Store the secure session token
                localStorage.setItem('sessionToken', res.sessionToken);
                localStorage.setItem('username', res.username);
                localStorage.setItem('role', res.role);
                this.router.navigate(['/card']);
            },
            error: (err) => {
                this.errorMessage = err.error?.error || 'Authentication Failed.';
            }
        });
    }

    private initCanvas(): void {
        const canvas = this.canvasRef.nativeElement;
        const ctx = canvas.getContext('2d')!;
        let W = 0, H = 0;
        let nodes: { x: number; y: number; vx: number; vy: number; r: number }[] = [];

        const resize = () => {
            W = canvas.width = window.innerWidth;
            H = canvas.height = window.innerHeight;
            nodes = createNodes(90);
        };

        const createNodes = (count: number) =>
            Array.from({ length: count }, () => ({
                x: Math.random() * W,
                y: Math.random() * H,
                vx: (Math.random() - 0.5) * 0.4,
                vy: (Math.random() - 0.5) * 0.4,
                r: Math.random() * 1.8 + 0.8
            }));

        const draw = () => {
            ctx.clearRect(0, 0, W, H);
            const grad = ctx.createRadialGradient(W * 0.35, H * 0.45, 0, W * 0.5, H * 0.5, Math.max(W, H) * 0.75);
            grad.addColorStop(0, '#0e1e3a');
            grad.addColorStop(0.5, '#080f20');
            grad.addColorStop(1, '#03070f');
            ctx.fillStyle = grad;
            ctx.fillRect(0, 0, W, H);

            const LINK_DIST = 160;
            for (let i = 0; i < nodes.length; i++) {
                for (let j = i + 1; j < nodes.length; j++) {
                    const dx = nodes[i].x - nodes[j].x;
                    const dy = nodes[i].y - nodes[j].y;
                    const dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < LINK_DIST) {
                        const alpha = (1 - dist / LINK_DIST) * 0.28;
                        ctx.beginPath();
                        ctx.moveTo(nodes[i].x, nodes[i].y);
                        ctx.lineTo(nodes[j].x, nodes[j].y);
                        ctx.strokeStyle = `rgba(80,160,255,${alpha})`;
                        ctx.lineWidth = 0.6;
                        ctx.stroke();
                    }
                }
            }
            for (const n of nodes) {
                ctx.beginPath();
                ctx.arc(n.x, n.y, n.r, 0, Math.PI * 2);
                ctx.fillStyle = 'rgba(100,175,255,0.55)';
                ctx.fill();
            }
        };

        const update = () => {
            for (const n of nodes) {
                n.x += n.vx; n.y += n.vy;
                if (n.x < 0 || n.x > W) n.vx *= -1;
                if (n.y < 0 || n.y > H) n.vy *= -1;
            }
        };

        const loop = () => {
            update(); draw();
            this.animId = requestAnimationFrame(loop);
        };

        window.addEventListener('resize', resize);
        resize();
        loop();
    }
}
