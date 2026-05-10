import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  // Login
  email               = '';
  password            = '';
  errorMessage        = '';
  loginSuccessMessage = '';   // ← mensaje de confirmación tras registro
  showPassword        = false;

  // Registro
  showRegister           = false;
  registerEmail          = '';
  registerPassword1      = '';
  registerPassword2      = '';
  registerErrorMessage   = '';
  registerSuccessMessage = '';
  showRegisterPassword1  = false;
  showRegisterPassword2  = false;

  // Recuperar contraseña
  showForgotPassword = false;
  forgotEmail        = '';
  forgotMessage      = '';

  constructor(
    private authService: AuthService,
    private router:      Router,
    private cdr:         ChangeDetectorRef
  ) {}

  get userToken(): string | null {
    return typeof window !== 'undefined' && window.localStorage
      ? window.localStorage.getItem('userToken')
      : null;
  }

  // ── LOGIN ────────────────────────────────────────────────────────────────

  login() {
    this.errorMessage        = '';
    this.loginSuccessMessage = '';
    this.authService.login(this.email, this.password).subscribe({
      next: (token) => {
        localStorage.setItem('userToken', token);
        localStorage.setItem('userEmail', this.email);
        const entries = localStorage.getItem('selectedEntries');
        const parsed  = entries ? JSON.parse(entries) : [];
        this.router.navigate(Array.isArray(parsed) && parsed.length > 0 ? ['/comprar'] : ['/']);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Email o contraseña incorrectos. Inténtalo de nuevo.';
        console.error(err);
        this.cdr.detectChanges();
      }
    });
  }

  togglePasswordVisibility() { this.showPassword = !this.showPassword; }

  // ── REGISTRO ─────────────────────────────────────────────────────────────

  register() {
    this.registerErrorMessage  = '';
    this.registerSuccessMessage = '';

    if (this.registerPassword1 !== this.registerPassword2) {
      this.registerErrorMessage = 'Las contraseñas no coinciden.';
      this.cdr.detectChanges();
      return;
    }

    this.authService.register(this.registerEmail, this.registerPassword1, this.registerPassword2).subscribe({
      next: () => {
        // Volver al login y mostrar mensaje de confirmación
        this.showRegister      = false;
        this.registerEmail     = '';
        this.registerPassword1 = '';
        this.registerPassword2 = '';
        this.email             = '';
        this.password          = '';
        this.errorMessage      = '';
        this.loginSuccessMessage = 'Te hemos enviado un email de confirmación. Revisa tu bandeja de entrada antes de iniciar sesión.';
        this.cdr.detectChanges();
      },
      error: (err) => {
        if (err.status === 0) {
          this.registerErrorMessage = 'No se puede conectar con el servidor.';
        } else {
          try {
            const body = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
            this.registerErrorMessage = body.message || 'Error al registrar. Verifique los datos.';
          } catch {
            this.registerErrorMessage = 'Error al registrar. Verifique los datos.';
          }
        }
        this.cdr.detectChanges();
      }
    });
  }

  toggleRegister() {
    this.showRegister           = !this.showRegister;
    this.showForgotPassword     = false;
    this.registerErrorMessage   = '';
    this.registerSuccessMessage = '';
    this.loginSuccessMessage    = '';
    this.email                  = '';
    this.password               = '';
    this.errorMessage           = '';
  }

  toggleRegisterPassword1Visibility() { this.showRegisterPassword1 = !this.showRegisterPassword1; }
  toggleRegisterPassword2Visibility() { this.showRegisterPassword2 = !this.showRegisterPassword2; }

  // ── RECUPERAR CONTRASEÑA ─────────────────────────────────────────────────

  forgotPassword() {
    this.forgotMessage = '';
    this.authService.forgotPassword(this.forgotEmail).subscribe({
      next: (response) => {
        this.forgotMessage = response;
        this.forgotEmail   = '';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.forgotMessage = 'Error al enviar el email de recuperación.';
        console.error(err);
        this.cdr.detectChanges();
      }
    });
  }

  toggleForgotPassword() {
    this.showForgotPassword  = !this.showForgotPassword;
    this.showRegister        = false;
    this.forgotMessage       = '';
    this.forgotEmail         = '';
    this.loginSuccessMessage = '';
  }

  // ── ELIMINAR CUENTA ──────────────────────────────────────────────────────

  deleteAccount() {
    const token = localStorage.getItem('userToken');
    if (!token) { this.errorMessage = 'No hay sesión activa.'; return; }

    if (confirm('¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.')) {
      this.authService.deleteAccount(token).subscribe({
        next: () => {
          localStorage.removeItem('userToken');
          this.router.navigate(['/']);
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorMessage = 'Error al eliminar la cuenta.';
          console.error(err);
          this.cdr.detectChanges();
        }
      });
    }
  }
}