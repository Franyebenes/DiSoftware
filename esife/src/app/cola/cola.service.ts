import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ColaService {

  private base = 'http://localhost:8080/cola';

  constructor(private http: HttpClient) {}

  unirseCola(usuarioEmail: string, espectaculoId: number): Observable<string> {
    return this.http.post(
      `${this.base}/unirse?usuarioEmail=${encodeURIComponent(usuarioEmail)}&espectaculoId=${espectaculoId}`,
      {},
      { responseType: 'text' }
    );
  }

  obtenerPosicion(usuarioEmail: string, espectaculoId: number): Observable<number> {
    return this.http.get<number>(
      `${this.base}/posicion?usuarioEmail=${encodeURIComponent(usuarioEmail)}&espectaculoId=${espectaculoId}`
    );
  }

  esTurnoUsuario(usuarioEmail: string, espectaculoId: number): Observable<boolean> {
    return this.http.get<boolean>(
      `${this.base}/turno?usuarioEmail=${encodeURIComponent(usuarioEmail)}&espectaculoId=${espectaculoId}`
    );
  }

  liberarTurno(usuarioEmail: string, espectaculoId: number): Observable<string> {
    return this.http.post(
      `${this.base}/liberar-turno?usuarioEmail=${encodeURIComponent(usuarioEmail)}&espectaculoId=${espectaculoId}`,
      {},
      { responseType: 'text' }
    );
  }
}