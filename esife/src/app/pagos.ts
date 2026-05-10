import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class Pagos {
  constructor(private http: HttpClient) {}
  
  prepararPago(info: any) {
    return this.http.post('http://localhost:8080/pago/prepararPago', info, { responseType: 'text' });
  }

  confirmarPago(info: any) {
    // envia el clientSecret al backend para que verifique con Stripe
    return this.http.post('http://localhost:8080/pago/confirmarPago', info, { responseType: 'text' });
  }

  reservarMultiples(idEntradas: number[]) {
    const token = localStorage.getItem('userToken') || '';
    const params = token ? `?userToken=${encodeURIComponent(token)}` : '';
    return this.http.post(
      `http://localhost:8080/reservas/reservar-multiples${params}`,
      idEntradas,
      { responseType: 'text' }
    );
  }

  comprar(info: any, token: string) {
    return this.http.post(`http://localhost:8080/compras/comprar?userToken=${encodeURIComponent(token)}`, info, { responseType: 'text' });
  }
}
