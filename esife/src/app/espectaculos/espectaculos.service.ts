import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class EspectaculosService {

  private readonly http = inject(HttpClient);

  getEscenarios() {
    return this.http.get('http://localhost:8080/busqueda/getEscenarios');
  }

  getEspectaculos(escenario: any) {
    return this.http.get(`http://localhost:8080/busqueda/getEspectaculos/${escenario.id}`);
  }

  searchEspectaculosByArtista(artista: string) {
    return this.http.get(`http://localhost:8080/busqueda/getEspectaculos?artista=${encodeURIComponent(artista)}`);
  }

  searchEspectaculosByFecha(fecha: string) {
    return this.http.get(`http://localhost:8080/busqueda/getEspectaculosByFecha?fecha=${fecha}`);
  }

  getNumeroDeEntradas(id: any) {
    return this.http.get(`http://localhost:8080/busqueda/getNumeroDeEntradas?idEspectaculo=${id}`);
  }

  getEntradasLibres(id: any) {
    return this.http.get(`http://localhost:8080/busqueda/getEntradasLibres?idEspectaculo=${id}`);
  }

  getNumeroDeEntradasComoDto(espectaculo: any) {
    return this.http.get(`http://localhost:8080/busqueda/getNumeroDeEntradasComoDto?espectaculoId=${espectaculo.id}`);
  }
}