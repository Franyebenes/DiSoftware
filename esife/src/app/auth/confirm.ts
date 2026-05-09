import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirm',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="text-align:center; margin-top: 100px;">
      <p *ngIf="message">{{ message }}</p>
    </div>
  `
})
export class ConfirmComponent implements OnInit {
  message: string = 'Confirmando tu cuenta...';

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.message = 'Token no válido.';
      return;
    }

    this.authService.confirmEmail(token).subscribe({
      next: (response) => {
        this.message = response + ' Ya puedes iniciar sesión.';
        setTimeout(() => this.router.navigate(['/login']), 3000);
      },
      error: () => {
        this.message = 'Token inválido o expirado.';
      }
    });
  }
}