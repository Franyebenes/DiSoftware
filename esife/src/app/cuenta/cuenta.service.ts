import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EntradaComprada {
  idEntrada: number;
  espectaculo: string;
  fechaEspectaculo: string;
  escenario: string;
  precioCentimos: number;
  precioEuros: string;
}

export interface CompraHistorial {
  id: number;
  fecha: string;
  totalCentimos: number;
  totalEuros: string;
  entradas: EntradaComprada[];
}

@Injectable({ providedIn: 'root' })
export class CuentaService {

  constructor(private http: HttpClient) {}

  getMisCompras(userToken: string): Observable<CompraHistorial[]> {
    return this.http.get<CompraHistorial[]>(
      `http://localhost:8080/cuenta/mis-compras?userToken=${encodeURIComponent(userToken)}`
    );
  }
}