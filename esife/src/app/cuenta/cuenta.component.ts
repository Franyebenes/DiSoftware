import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CuentaService, CompraHistorial } from './cuenta.service';

@Component({
  selector: 'app-cuenta',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cuenta.component.html',
  styleUrl: './cuenta.component.css',
})
export class CuentaComponent implements OnInit {

  email = '';
  compras: CompraHistorial[] = [];
  cargando = true;
  error = '';

  // Controla qué compra está expandida (para ver las entradas)
  expandida: number | null = null;

  constructor(
    private cuentaService: CuentaService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    const token = localStorage.getItem('userToken');
    this.email  = localStorage.getItem('userEmail') || '';

    if (!token) {
      this.router.navigate(['/login']);
      return;
    }

    this.cuentaService.getMisCompras(token).subscribe({
      next: (data) => {
        this.compras  = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error(err);
        this.error    = 'No se pudieron cargar tus compras.';
        this.cargando = false;
      }
    });
  }

  toggleExpandida(idCompra: number): void {
    this.expandida = this.expandida === idCompra ? null : idCompra;
  }

  irAlInicio(): void {
    this.router.navigate(['/']);
  }

  formatFecha(fecha: string): string {
    return new Date(fecha).toLocaleDateString('es-ES', {
      day: '2-digit', month: 'long', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }
}