import { Component, ChangeDetectorRef, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Espectaculos } from '../espectaculos/espectaculos';
import { EscenariosComponent } from '../escenarios/escenarios.component';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, FormsModule, Espectaculos, EscenariosComponent],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css',
})
export class MainLayoutComponent {
  activeTab: 'escenarios' | 'espectaculos' | 'cuenta' = 'escenarios';
  showUserMenu: boolean = false;
  showDeleteConfirm: boolean = false;

  // 1. Inyectamos PLATFORM_ID en el constructor
  constructor(
    private router: Router, 
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  isUserAuthenticated(): boolean {
    // 2. Angular comprueba de forma segura si el código se está ejecutando en el navegador
    if (isPlatformBrowser(this.platformId)) {
      return !!localStorage.getItem('userToken');
    }
    // Si se está ejecutando en el servidor (SSR), devuelve false de forma segura
    return false;
  }

  setActiveTab(tab: 'escenarios' | 'espectaculos' | 'cuenta') {
    if (tab === 'cuenta') {
      this.router.navigate(['/login']);
      return;
    }
    this.activeTab = tab;
  }

  logout() {
    // Protegemos también el borrado por si acaso
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('userToken');
      localStorage.removeItem('selectedEntries');
      localStorage.removeItem('selectedEspectaculo');
      localStorage.removeItem('userEmail');
    }
    this.router.navigate(['/']); // Redirigimos al inicio después de cerrar sesión
    this.cdr.detectChanges();
  }

  getUserEmail(): string {
    if (isPlatformBrowser(this.platformId)) {
        return localStorage.getItem('userEmail') || 'Usuario';
    }
    return 'Usuario';
  }

  toggleUserMenu() {
    this.showUserMenu = !this.showUserMenu;
  }

  deleteAccount() {
    this.showDeleteConfirm = true;
  }

  confirmDelete() {
    const token = localStorage.getItem('userToken');
    if (!token) {
        console.log('No hay token');
        return;
    }

    this.authService.deleteAccount(token).subscribe({
        next: (response) => {
            console.log('Cuenta eliminada:', response);
            if (isPlatformBrowser(this.platformId)) {
                localStorage.removeItem('userToken');
                localStorage.removeItem('userEmail');
                localStorage.removeItem('selectedEntries');
                localStorage.removeItem('selectedEspectaculo');
            }
            this.showUserMenu = false;
            this.showDeleteConfirm = false;
            this.router.navigate(['/']);
            this.cdr.detectChanges();
        },
        error: (err: any) => {
            console.error('Error al eliminar cuenta:', err);
        }
    });
  }

cancelDelete() {
    this.showDeleteConfirm = false;
}
}