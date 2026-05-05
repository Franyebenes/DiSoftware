import { Component, Inject, PLATFORM_ID, OnInit } from '@angular/core';
import { Pagos } from '../pagos';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

declare let Stripe: any;

@Component({
  selector: 'app-compra',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './compra.html',
  styleUrl: './compra.css',
})
export class CompraComponent implements OnInit {

  importe: number = 0;
  clientSecret?: string;
  stripe: any;
  selectedEntries: any[] = [];
  totalCentimos: number = 0;
  selectedEspectaculo: any = null;
  pagoPreparado = false;

  constructor(private service: Pagos,
              private router: Router,
              @Inject(PLATFORM_ID) private platformId: Object) { };

  ngOnInit() {
    const entriesJson = localStorage.getItem('selectedEntries');
    const espectaculoJson = localStorage.getItem('selectedEspectaculo');
    if (!entriesJson || !espectaculoJson) {
      this.router.navigate(['/']);
      return;
    }

    this.selectedEntries = JSON.parse(entriesJson);
    this.selectedEspectaculo = JSON.parse(espectaculoJson);
    this.totalCentimos = 0;
    this.importe = 0;

    if (Array.isArray(this.selectedEntries) && this.selectedEntries.length > 0) {
      this.totalCentimos = this.selectedEntries.reduce((sum: number, entrada: any) => {
        const centimos = entrada.precioCentimos || Math.round((entrada.precioEuros || 0) * 100);
        return sum + centimos;
      }, 0);
      this.importe = this.totalCentimos / 100;
    } else {
      this.router.navigate(['/']);
    }

    if (isPlatformBrowser(this.platformId)) {
      this.stripe = Stripe("pk_test_51T57EhRer5FYzgoYICR8epqmn0lfWbgROUQSdBryko5ajUTHQ52ox5Vk64fz8XhsWnV1EoinINhc5XsokWxd6ntr000yJmorn9");
    }
  }

  irAtras() {
    this.router.navigate(['/']);
  }

  irAlPago() {
    const token = localStorage.getItem('userToken');
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }

    const info = {
      idEntradas: this.selectedEntries.map((entrada: any) => entrada.id),
    };

    this.service.reservarMultiples(info).subscribe(
      () => {
        const pagoInfo = {
          centimos: this.totalCentimos,
        };

        this.service.prepararPago(pagoInfo).subscribe(
          (response: any) => {
            this.clientSecret = response;
            this.pagoPreparado = true;
            this.showForm();
          },
          (error: any) => {
            console.error('Error al preparar el pago:', error);
          }
        );
      },
      (error: any) => {
        console.error('Error al reservar las entradas:', error);
        alert('No se pudieron reservar las entradas. Inténtalo de nuevo.');
      }
    );
  }

  showForm() {
    if (!this.stripe) {
      return;
    }
    let elements = this.stripe.elements();
    let style = {
      base: {
        color: "#32325d", fontFamily: 'Arial, sans-serif',
        fontSmoothing: "antialiased", fontSize: "16px",
        "::placeholder": {
          color: "#32325d"
        }
      }, invalid: {
        fontFamily: 'Arial, sans-serif', color: "#fa755a",
        iconColor: "#fa755a"
      }
    };
    let card = elements.create("card", { style: style });
    card.mount("#card-element");
    card.on("change", function (event: any) {
      const button = document.querySelector("#submit") as HTMLButtonElement;
      const errorEl = document.querySelector("#card-error") as HTMLElement;
      if (button) {
        button.disabled = event.empty;
      }
      if (errorEl) {
        errorEl.textContent = event.error ? event.error.message : "";
      }
    });
    let self = this;
    let form = document.getElementById("payment-form");
    if (form) {
      form.addEventListener("submit", function (event) {
        event.preventDefault();
        self.payWithCard(card);
      });
      form.style.display = "block";
    }
  }

  payWithCard(card: any) {
    if (!this.clientSecret) {
      return;
    }

    this.stripe.confirmCardPayment(this.clientSecret, {
      payment_method: {
        card: card
      }
    }).then((response: any) => {
      if (response.error) {
        alert(response.error.message);
      } else if (response.paymentIntent && response.paymentIntent.status === 'succeeded') {
        const info = {
          clientSecret: this.clientSecret,
          paymentIntentId: response.paymentIntent.id
        };
        this.service.confirmarPago(info).subscribe(
          () => {
            const token = localStorage.getItem('userToken');
            if (!token) {
              this.router.navigate(['/login']);
              return;
            }
            const compraInfo = {
              idEntradas: this.selectedEntries.map((entrada: any) => entrada.id),
              clientSecret: this.clientSecret
            };
            this.service.comprar(compraInfo, token).subscribe(
              () => {
                alert('Compra completada correctamente. Revisa tu correo electrónico.');
                localStorage.removeItem('selectedEntries');
                localStorage.removeItem('selectedEspectaculo');
                this.router.navigate(['/']);
              },
              (err: any) => {
                console.error('Error al registrar la compra:', err);
                alert('Pago confirmado, pero no se pudo completar la compra en el servidor.');
              }
            );
          },
          (err: any) => {
            console.error('Error al confirmar en backend:', err);
            alert('No se pudo confirmar el pago en el servidor.');
          }
        );
      }
    });
  }
}

