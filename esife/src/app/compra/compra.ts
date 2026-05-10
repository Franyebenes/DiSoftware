import {
  Component,
  Inject,
  PLATFORM_ID,
  OnInit,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Pagos } from '../pagos';

declare var Stripe: any;

@Component({
  selector: 'app-compra',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compra.html',
  styleUrl: './compra.css',
})
export class CompraComponent implements OnInit {

  importe = 0;
  totalCentimos = 0;

  clientSecret?: string;

  selectedEntries: any[] = [];
  selectedEspectaculo: any = null;

  pagoPreparado = false;
  cargando = false;
  mensajeExito: string = '';

  stripe: any;
  card: any;

  constructor(
    private service: Pagos,
    private router: Router,
    private cdr: ChangeDetectorRef,           // ← fuerza detección de cambios
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    const entriesJson    = localStorage.getItem('selectedEntries');
    const espectaculoJson = localStorage.getItem('selectedEspectaculo');

    if (!entriesJson || !espectaculoJson) {
      this.router.navigate(['/']);
      return;
    }

    this.selectedEntries    = JSON.parse(entriesJson);
    this.selectedEspectaculo = JSON.parse(espectaculoJson);

    this.totalCentimos = this.selectedEntries.reduce((sum: number, e: any) => {
      const precio = e.precioCentimos ?? Math.round((e.precioEuros ?? 0) * 100);
      return sum + precio;
    }, 0);

    this.importe = this.totalCentimos / 100;

    if (typeof Stripe === 'undefined') {
      console.error('Stripe.js no está cargado. Añade el script en index.html.');
      return;
    }

    //Clave de STRIPE pública
    this.stripe = Stripe(
      'pk_test_51T57EhRer5FYzgoYICR8epqmn0lfWbgROUQSdBryko5ajUTHQ52ox5Vk64fz8XhsWnV1EoinINhc5XsokWxd6ntr000yJmorn9'
    );
  }

  irAtras(): void {
    this.router.navigate(['/']);
  }

  irAlPago(): void {
    const token = localStorage.getItem('userToken');
    if (!token) { this.router.navigate(['/login']); return; }

    this.cargando = true;

    const idEntradas = this.selectedEntries.map((e: any) => e.id);

    this.service.reservarMultiples(idEntradas).subscribe({
      next: () => {
        this.service.prepararPago({ centimos: this.totalCentimos }).subscribe({
          next: (clientSecret: string) => {
            this.clientSecret  = clientSecret;
            this.pagoPreparado = true;
            this.cargando      = false;

            // 1) Forzamos que Angular procese el *ngIf y añada #card-element al DOM
            this.cdr.detectChanges();

            // 2) Ahora el elemento ya existe — montamos Stripe
            this.mountStripeElement();
          },
          error: (err: any) => {
            console.error('Error preparando pago', err);
            alert('No se pudo preparar el pago. Inténtalo de nuevo.');
            this.cargando = false;
          }
        });
      },
      error: (err: any) => {
        console.error('Error reservando entradas', err);
        alert('No se pudieron reservar las entradas. Inténtalo de nuevo.');
        this.cargando = false;
      }
    });
  }

  mountStripeElement(): void {
    if (!isPlatformBrowser(this.platformId) || !this.stripe || this.card) return;

    const container = document.getElementById('card-element');
    if (!container) {
      console.error('#card-element no existe en el DOM');
      return;
    }

    const elements = this.stripe.elements();

    this.card = elements.create('card', {
      hidePostalCode: true,
      style: {
        base: {
          color: '#1e293b',
          fontFamily: 'Inter, -apple-system, sans-serif',
          fontSize: '16px',
          '::placeholder': { color: '#94a3b8' },
          iconColor: '#1e3a8a',
        },
        invalid: {
          color: '#ef4444',
          iconColor: '#ef4444',
        },
      },
    });

    this.card.mount('#card-element');

    this.card.on('change', (event: any) => {
      const errorEl  = document.getElementById('card-error');
      const submitBtn = document.getElementById('submit') as HTMLButtonElement;
      if (submitBtn) submitBtn.disabled = event.empty || !!event.error;
      if (errorEl)   errorEl.textContent = event.error?.message || '';
    });

    const form = document.getElementById('payment-form');
    form?.addEventListener('submit', (e) => {
      e.preventDefault();
      this.payWithCard();
    });
  }

  payWithCard(): void {
    if (!this.clientSecret || !this.card) return;

    const submitBtn = document.getElementById('submit') as HTMLButtonElement;
    if (submitBtn) {
      submitBtn.disabled  = true;
      submitBtn.innerText = '⏳ Procesando...';
    }

    this.stripe.confirmCardPayment(this.clientSecret, {
      payment_method: { card: this.card },
    }).then((result: any) => {

      if (result.error) {
        const errorEl = document.getElementById('card-error');
        if (errorEl)   errorEl.textContent = result.error.message;
        if (submitBtn) {
          submitBtn.disabled  = false;
          submitBtn.innerText = `🔒 Pagar ${this.importe.toFixed(2)} € ahora`;
        }
        return;
      }

      if (result.paymentIntent?.status === 'succeeded') {
        this.confirmarCompra();
      }
    });
  }

  confirmarCompra(): void {
    if (!this.clientSecret) return;

    this.service.confirmarPago({ clientSecret: this.clientSecret }).subscribe({
      next: () => {
        const token = localStorage.getItem('userToken');
        if (!token) { this.router.navigate(['/login']); return; }

        const compraInfo = {
          idEntradas:   this.selectedEntries.map((e: any) => e.id),
          clientSecret: this.clientSecret,
        };

        this.service.comprar(compraInfo, token).subscribe({
          next: () => {
            this.mensajeExito = 'Compra realizada correctamente. ¡Disfruta del espectáculo!';
            this.cdr.detectChanges();
            localStorage.removeItem('selectedEntries');
            localStorage.removeItem('selectedEspectaculo');
            setTimeout(() => this.router.navigate(['/']), 3000);
          },
          error: (err: any) => {
            console.error(err);
            alert('El pago se realizó, pero hubo un problema al registrar la compra. Contacta con soporte.');
          }
        });
      },
      error: (err: any) => {
        console.error(err);
        alert('No se pudo confirmar el pago en el servidor.');
      }
    });
  }
}