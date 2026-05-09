import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div style="text-align:center; margin-top: 100px;">
      <div *ngIf="!resetDone">
        <h2>Nueva contraseña</h2>
        <input type="password" [(ngModel)]="newPassword" placeholder="Nueva contraseña" /><br><br>
        <button (click)="reset()">Cambiar contraseña</button>
        <p *ngIf="errorMessage" style="color:red">{{ errorMessage }}</p>
      </div>
      <p *ngIf="resetDone"> Contraseña cambiada. Redirigiendo al login...</p>
    </div>
  `
})
export class ResetPasswordComponent implements OnInit {
  newPassword: string = '';
  errorMessage: string = '';
  resetDone: boolean = false;
  private token: string = '';

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!this.token) {
      this.errorMessage = 'Token no válido.';
    }
  }

  reset() {
    this.authService.resetPassword(this.token, this.newPassword).subscribe({
      next: () => {
        this.resetDone = true;
        setTimeout(() => this.router.navigate(['/login']), 3000);
      },
      error: (err) => {
        try {
          const errorBody = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          this.errorMessage = errorBody.message || 'Error al resetear la contraseña.';
        } catch {
          this.errorMessage = 'Error al resetear la contraseña.';
        }
      }
    });
  }
}