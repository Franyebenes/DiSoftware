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

  
  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  login() {
    this.errorMessage = ''; 

    this.authService.login(this.email, this.password).subscribe({
      next: (token) => {
        localStorage.setItem('userToken', token);
        this.router.navigate(['/comprar']);
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
        this.cdr.detectChanges(); 
      },
      error: (err) => {
        this.registerErrorMessage = err.error || "Error al registrar. Verifique los datos.";
        console.error('Error en el registro:', err);
        this.cdr.detectChanges();
      }
    });
  }

  toggleRegister() {
    this.showRegister = !this.showRegister;
    this.registerErrorMessage = '';
    this.registerSuccessMessage = '';
  }
}
