import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPasswordComponent implements OnInit {
  newPassword     = '';
  confirmPassword = '';
  errorMessage    = '';
  successMessage  = '';
  resetDone       = false;
  showPassword    = false;
  showConfirm     = false;
  token   = '';

  constructor(
    private route:       ActivatedRoute,
    private authService: AuthService,
    private router:      Router
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!this.token) {
      this.errorMessage = 'Token no válido o caducado.';
    }
  }

  reset() {
    this.errorMessage   = '';
    this.successMessage = '';

    if (!this.newPassword || !this.confirmPassword) {
      this.errorMessage = 'Por favor rellena ambos campos.';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Las contraseñas no coinciden.';
      return;
    }

    this.authService.resetPassword(this.token, this.newPassword).subscribe({
      next: () => {
        this.resetDone      = true;
        this.successMessage = 'Contraseña cambiada correctamente. Redirigiendo al login...';
        setTimeout(() => this.router.navigate(['/login']), 3000);
      },
      error: (err) => {
        try {
          const body = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          this.errorMessage = body.message || 'Error al cambiar la contraseña.';
        } catch {
          this.errorMessage = 'Error al cambiar la contraseña.';
        }
      }
    });
  }

  togglePassword()  { this.showPassword = !this.showPassword; }
  toggleConfirm()   { this.showConfirm  = !this.showConfirm; }
  irAlLogin()       { this.router.navigate(['/login']); }
}