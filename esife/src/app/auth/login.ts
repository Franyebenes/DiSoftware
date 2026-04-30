import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule], // Necesarios para [(ngModel)] y errores
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  // Variables para el formulario
  userName: string = '';
  pwd: string = '';
  errorMessage: string = '';

  // Inyectamos los servicios
  private authService = inject(AuthService);
  private router = inject(Router);

  login() {
    this.authService.login(this.userName, this.pwd).subscribe({
      next: (response) => {
        // Guardamos el resultado (token o nombre) en el localStorage (de momento, necesitamos luego que sea con base de datos)
        localStorage.setItem('userToken', response);

        // Una vez logueado, lo mandamos a la compra automáticamente
        this.router.navigate(['/comprar']);
      },
      error: (err) => {
        this.errorMessage = "Credenciales incorrectas. Inténtalo de nuevo.";
        console.error('Error en el login:', err);
      }
    });
  }
}