import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
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

  // Datos del contexto
  usuarioEmail  = '';
  espectaculoId = 0;
  espectaculoNombre = '';

  // Estado de la cola
  enCola       = false;
  posicion      = 0;
  esTurno       = false;
  cargando      = false;
  error         = '';

  // Polling — intervalo en ms (5 segundos)
  private readonly INTERVALO_POLLING = 5000;
  private pollingTimer: any = null;

  constructor(
    private colaService: ColaService,
    private router:      Router,
    private route:       ActivatedRoute,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    // Recuperar datos del espectáculo pasados por queryParams o localStorage
    this.route.queryParams.subscribe(params => {
      this.espectaculoId     = Number(params['espectaculoId']) || 0;
      this.espectaculoNombre = params['nombre'] || 'Espectáculo';
    });

    this.usuarioEmail = localStorage.getItem('userEmail') || '';

    if (!this.usuarioEmail) {
      this.router.navigate(['/login']);
      return;
    }

    if (!this.espectaculoId) {
      this.router.navigate(['/']);
      return;
    }
  }

  ngOnDestroy(): void {
    this.detenerPolling();
  }

  // ── UNIRSE A LA COLA ─────────────────────────────────────────────────────

  unirseCola(): void {
    this.error    = '';
    this.cargando = true;

    this.colaService.unirseCola(this.usuarioEmail, this.espectaculoId).subscribe({
      next: () => {
        this.enCola   = true;
        this.cargando = false;
        // Consultar posición inmediatamente y luego iniciar polling
        this.consultarEstado();
        this.iniciarPolling();
      },
      error: (err) => {
        this.error    = err.error || 'Error al unirse a la cola.';
        this.cargando = false;
      }
    });
  }

  // ── POLLING ──────────────────────────────────────────────────────────────

  private iniciarPolling(): void {
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
    // 1. Consultar si es su turno
    this.colaService.esTurnoUsuario(this.usuarioEmail, this.espectaculoId).subscribe({
      next: (esTurno) => {
        this.esTurno = esTurno;

        if (esTurno) {
          // Es su turno — detener polling y redirigir a compra
          this.detenerPolling();
        } else {
          // Aún esperando — consultar posición
          this.colaService.obtenerPosicion(this.usuarioEmail, this.espectaculoId).subscribe({
            next: (posicion) => {
              this.posicion = posicion;
            },
            error: () => {}
          });
        }
      },
      error: () => {}
    });
  }

  // ── CUANDO LE LLEGA EL TURNO ─────────────────────────────────────────────

  irAComprar(): void {
    // Navegar a la selección de entradas del espectáculo
    // El turno se libera automáticamente cuando el backend detecta que ha expirado,
    // o cuando se llama a liberarTurno al completar/cancelar la compra
    this.router.navigate(['/espectaculos', this.espectaculoId]);
  }

  // ── SALIR DE LA COLA ─────────────────────────────────────────────────────

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