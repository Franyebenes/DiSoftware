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
  email: string = '';
  password: string = '';
  errorMessage: string = '';
  showRegister: boolean = false;
  registerEmail: string = '';
  registerPassword1: string = '';
  registerPassword2: string = '';
  registerErrorMessage: string = '';
  registerSuccessMessage: string = '';
  showForgotPassword: boolean = false;
  forgotEmail: string = '';
  forgotMessage: string = '';
  showResetPassword: boolean = false;
  resetToken: string = '';
  newPassword: string = '';
  resetMessage: string = '';
  showPassword: boolean = false;
  showRegisterPassword1: boolean = false;
  showRegisterPassword2: boolean = false;
  showResetNewPassword: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  get userToken(): string | null {
    return typeof window !== 'undefined' && window.localStorage ? window.localStorage.getItem('userToken') : null;
  }

  login() {
    this.errorMessage = ''; 

    this.authService.login(this.email, this.password).subscribe({
      next: (token) => {
        localStorage.setItem('userToken', token);
        localStorage.setItem('userEmail', this.email); // Guardamos el email para mostrarlo en la cuenta

        const selectedEntriesJson = localStorage.getItem('selectedEntries');
        const selectedEntries = selectedEntriesJson ? JSON.parse(selectedEntriesJson) : [];
        if (Array.isArray(selectedEntries) && selectedEntries.length > 0) {
          this.router.navigate(['/comprar']);
        } else {
          this.router.navigate(['/']);
        }

        this.cdr.detectChanges(); 
      },
      error: (err) => {
        this.errorMessage = "Email o contraseña incorrectos. Inténtalo de nuevo.";
        console.error('Error en el login:', err);
        this.cdr.detectChanges();
      }
    });
  }

  register() {
    this.registerErrorMessage = '';
    this.registerSuccessMessage = '';

    if (this.registerPassword1 !== this.registerPassword2) {
      this.registerErrorMessage = "Las contraseñas no coinciden.";
      this.cdr.detectChanges();
      return;
    }

    this.authService.register(this.registerEmail, this.registerPassword1, this.registerPassword2).subscribe({
      next: (response) => {
        this.registerSuccessMessage = response;
        this.registerEmail = '';
        this.registerPassword1 = '';
        this.registerPassword2 = '';
        this.showRegister = false;
        this.email = '';        
        this.password = '';    
        this.errorMessage = '';
        this.cdr.detectChanges(); 
      },
      error: (err) => {
        if (err.status === 0) {
          this.registerErrorMessage = 'No se puede conectar con el servidor.';
        } else {
          try {
              const errorBody = typeof err.error === 'string' ? JSON.parse(err.error) : err.error; //Para que se pueda ver en pantalla (de String a JSON)
              this.registerErrorMessage = errorBody.message || 'Error al registrar. Verifique los datos.';
          } catch {
              this.registerErrorMessage = 'Error al registrar. Verifique los datos.';
          }
      }
      this.cdr.detectChanges();
    }
    });
  }

  toggleRegister() {
    this.showRegister = !this.showRegister;
    this.registerErrorMessage = '';
    this.registerSuccessMessage = '';
    this.email = '';     
    this.password = '';     
    this.errorMessage = ''; 
  }

  toggleForgotPassword() {
    this.showForgotPassword = !this.showForgotPassword;
    this.forgotMessage = '';
    this.forgotEmail = '';
  }

  toggleResetNewPasswordVisibility() {
    this.showResetNewPassword = !this.showResetNewPassword;
  }

  forgotPassword() {
    this.forgotMessage = '';

    this.authService.forgotPassword(this.forgotEmail).subscribe({
      next: (response) => {
        this.forgotMessage = response;
        this.forgotEmail = '';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.forgotMessage = "Error al enviar el email de recuperación.";
        console.error('Error en forgot password:', err);
        this.cdr.detectChanges();
      }
    });
  }

  toggleResetPassword() {
    this.showResetPassword = !this.showResetPassword;
    this.resetMessage = '';
    this.resetToken = '';
    this.newPassword = '';
  }

  resetPassword() {
    this.resetMessage = '';

    this.authService.resetPassword(this.resetToken, this.newPassword).subscribe({
      next: (response) => {
        this.resetMessage = response;
        this.resetToken = '';
        this.newPassword = '';
        this.showResetPassword = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.resetMessage = "Error al resetear la contraseña. Verifique el token.";
        console.error('Error en reset password:', err);
        this.cdr.detectChanges();
      }
    });
  }

  deleteAccount() {
    const token = localStorage.getItem('userToken');
    if (!token) {
      this.errorMessage = "No hay sesión activa.";
      this.cdr.detectChanges();
      return;
    }

    if (confirm('¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.')) {
      this.authService.deleteAccount(token).subscribe({
        next: (response) => {
          localStorage.removeItem('userToken');
          this.router.navigate(['/']);
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorMessage = "Error al eliminar la cuenta.";
          console.error('Error al eliminar cuenta:', err);
          this.cdr.detectChanges();
        }
      });
    }
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  toggleRegisterPassword1Visibility() {
    this.showRegisterPassword1 = !this.showRegisterPassword1;
  }

  toggleRegisterPassword2Visibility() {
    this.showRegisterPassword2 = !this.showRegisterPassword2;
  }
}
