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
  userName: string = '';
  pwd: string = '';
  errorMessage: string = '';

  
  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  login() {
    this.errorMessage = ''; 

    this.authService.login(this.userName, this.pwd).subscribe({
      next: (response) => {
        localStorage.setItem('userToken', response);
        this.router.navigate(['/comprar']);
        this.cdr.detectChanges(); 
      },
      error: (err) => {
        this.errorMessage = "Credenciales incorrectas. Inténtalo de nuevo.";
        console.error('Error en el login:', err);
        this.cdr.detectChanges();
      }
    });
  }
}