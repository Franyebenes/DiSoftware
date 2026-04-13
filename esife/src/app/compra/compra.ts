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

  importe: number = 20.00;
  clientSecret?: string;
  stripe: any;

  constructor(private service: Pagos,
              private router: Router,
              @Inject(PLATFORM_ID) private platformId: Object) { };

  ngOnInit() {
    // sólo inicializamos Stripe en el navegador, no en SSR
    if (isPlatformBrowser(this.platformId)) {
      this.stripe = Stripe("pk_test_51T57EhRer5FYzgoYICR8epqmn0lfWbgROUQSdBryko5ajUTHQ52ox5Vk64fz8XhsWnV1EoinINhc5XsokWxd6ntr000yJmorn9");
    }
  }

  irAtras() {
    // volver a la página anterior o al listado de espectáculos
    this.router.navigate(['/']);
  }

  irAlPago() {
    let info = {
      centimo: Math.floor(this.importe.valueOf() * 100),
    }
    this.service.prepararPago(info).subscribe(
      (response: any) => {
        this.clientSecret = response;
        this.showForm();
      },
      (error: any) => {
        console.error('Error al preparar el pago:', error);
        // Aquí puedes mostrar un mensaje de error al usuario
      }
    );
  }

  showForm() {
    let elements = this.stripe.elements()
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
    }
    let card = elements.create("card", { style: style })
    card.mount("#card-element")
    card.on("change", function (event: any) {
      document.querySelector("button")!.disabled = event.empty;
      document.querySelector("#card-error")!.textContent =
        event.error ? event.error.message : "";
    });
    let self = this
    let form = document.getElementById("payment-form");
    form!.addEventListener("submit", function (event) {
      event.preventDefault();
      self.payWithCard(card);
    });
    form!.style.display = "block"
  }

  payWithCard(card: any) {
    this.stripe.confirmCardPayment(this.clientSecret, {
      payment_method: {
        card: card
      }
    }).then((response: any) => {
      if (response.error) {
        alert(response.error.message);
      } else if (response.paymentIntent && response.paymentIntent.status === 'succeeded') {
        alert("Pago exitoso");
        // notificar al backend para que verifique en Stripe y actualice la base de datos
        const info = {
          clientSecret: this.clientSecret,
          paymentIntentId: response.paymentIntent.id
        };
        this.service.confirmarPago(info).subscribe(
          (res: any) => {
            console.log('Respuesta del servidor:', res);
            // aquí puedes mostrar un mensaje al usuario o redirigir
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

