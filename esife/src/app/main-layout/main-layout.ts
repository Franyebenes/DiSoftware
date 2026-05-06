import { Component, ChangeDetectorRef, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Espectaculos } from '../espectaculos/espectaculos';
import { EscenariosComponent } from '../escenarios/escenarios.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, FormsModule, Espectaculos, EscenariosComponent],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css',
})
export class MainLayoutComponent {
  activeTab: 'escenarios' | 'espectaculos' | 'cuenta' = 'escenarios';

  // 1. Inyectamos PLATFORM_ID en el constructor
  constructor(
    private router: Router, 
    private cdr: ChangeDetectorRef,
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
    // 3. (Opcional pero recomendado) Protegemos también el borrado por si acaso
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('userToken');
      localStorage.removeItem('selectedEntries');
      localStorage.removeItem('selectedEspectaculo');
    }
    this.router.navigate(['/login']);
  }
}