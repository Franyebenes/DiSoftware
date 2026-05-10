import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { ColaService } from './cola.service';

@Component({
  selector: 'app-cola',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cola.component.html',
  styleUrl: './cola.component.css',
})
export class ColaComponent implements OnInit, OnDestroy {

  usuarioEmail      = '';
  espectaculoId     = 0;
  espectaculoNombre = '';

  enCola   = false;
  posicion = 0;
  esTurno  = false;
  cargando = false;
  error    = '';

  private readonly INTERVALO_POLLING = 5000;
  private pollingTimer: any = null;

  constructor(
    private colaService: ColaService,
    private cdr:         ChangeDetectorRef,
    private router:      Router,
    private route:       ActivatedRoute,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    this.route.queryParams.subscribe(params => {
      this.espectaculoId     = Number(params['espectaculoId']) || 0;
      this.espectaculoNombre = params['nombre'] || 'Espectáculo';
    });

    this.usuarioEmail = localStorage.getItem('userEmail') || '';
    if (!this.usuarioEmail) { this.router.navigate(['/login']); return; }
    if (!this.espectaculoId) { this.router.navigate(['/']); return; }

    // Comprobar si ya estaba en la cola (recarga de página)
    this.comprobarSiYaEnCola();
  }

  ngOnDestroy(): void {
    this.detenerPolling();
  }

  private comprobarSiYaEnCola(): void {
    this.colaService.obtenerPosicion(this.usuarioEmail, this.espectaculoId).subscribe({
      next: (posicion) => {
        this.enCola   = true;
        this.posicion = posicion;
        this.cdr.detectChanges();
        this.iniciarPolling();
      },
      error: () => {
        // No está en la cola aún — mostrar botón de unirse
        this.enCola = false;
        this.cdr.detectChanges();
      }
    });
  }

  unirseCola(): void {
    this.error    = '';
    this.cargando = true;
    this.cdr.detectChanges();

    this.colaService.unirseCola(this.usuarioEmail, this.espectaculoId).subscribe({
      next: () => {
        this.enCola   = true;
        this.cargando = false;
        this.cdr.detectChanges();
        this.consultarEstado();
        this.iniciarPolling();
      },
      error: (err) => {
        const msg = err.error || '';
        if (msg.includes('Ya estás en la cola')) {
          this.enCola   = true;
          this.cargando = false;
          this.cdr.detectChanges();
          this.consultarEstado();
          this.iniciarPolling();
        } else {
          this.error    = msg || 'Error al unirse a la cola.';
          this.cargando = false;
          this.cdr.detectChanges();
        }
      }
    });
  }

  private iniciarPolling(): void {
    this.detenerPolling();
    this.pollingTimer = setInterval(() => {
      this.consultarEstado();
    }, this.INTERVALO_POLLING);
  }

  private detenerPolling(): void {
    if (this.pollingTimer) {
      clearInterval(this.pollingTimer);
      this.pollingTimer = null;
    }
  }

  private consultarEstado(): void {
    this.colaService.esTurnoUsuario(this.usuarioEmail, this.espectaculoId).subscribe({
      next: (esTurno) => {
        this.esTurno = esTurno;
        this.cdr.detectChanges();

        if (esTurno) {
          // Es el turno — detener polling y mostrar botón de compra
          this.detenerPolling();
        } else {
          // Aún esperando — actualizar posición
          this.colaService.obtenerPosicion(this.usuarioEmail, this.espectaculoId).subscribe({
            next: (posicion) => {
              this.posicion = posicion;
              this.cdr.detectChanges();
            },
            error: (err) => {
              // Si devuelve 404 es que ya no está en la cola (fue eliminado)
              console.warn('Posición no encontrada:', err.status);
            }
          });
        }
      },
      error: (err) => {
        // Error de red — el polling sigue, se reintentará en 5 segundos
        console.warn('Error consultando turno:', err.status);
      }
    });
  }

  irAComprar(): void {
    // Las entradas ya están guardadas en localStorage desde escenarios.component.ts
    // Navegamos directamente a la página de compra
    this.router.navigate(['/comprar']);
  }

  salirCola(): void {
    this.detenerPolling();
    this.colaService.liberarTurno(this.usuarioEmail, this.espectaculoId).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => this.router.navigate(['/'])
    });
  }

  irAlInicio(): void {
    this.router.navigate(['/']);
  }
}