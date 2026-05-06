import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EspectaculosService } from './espectaculos.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-espectaculos',
  imports: [CommonModule, FormsModule],
  standalone: true,
  templateUrl: './espectaculos.html',
  styleUrl: './espectaculos.css',
})
export class Espectaculos {
  searchArtista: string = '';
  searchFecha: string = '';
  searchResults: any[] = [];
  isSearching: boolean = false;

  constructor(
    private espectaculosService: EspectaculosService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  getEntradas(espectaculo: any) {
    this.espectaculosService.getEntradas(espectaculo.id).subscribe(
      (response: any) => {
        espectaculo.entradasDisponibles = response;
        espectaculo.selectedEntradas = espectaculo.selectedEntradas || [];
        this.cdr.detectChanges();
      },
      (error: any) => {
        console.error('Error al obtener entradas disponibles', error);
      }
    );
  }

  toggleEntradaSeleccionada(espectaculo: any, entrada: any) {
    espectaculo.selectedEntradas = espectaculo.selectedEntradas || [];
    if (entrada.seleccionada) {
      if ((espectaculo.selectedEntradas || []).length >= 5) {
        entrada.seleccionada = false;
        alert('Solo puedes seleccionar hasta 5 entradas.');
        return;
      }
      espectaculo.selectedEntradas.push(entrada);
    } else {
      espectaculo.selectedEntradas = espectaculo.selectedEntradas.filter(
        (item: any) => item.id !== entrada.id
      );
    }
  }

  irAComprarEntradas(espectaculo: any) {
    if (!espectaculo.selectedEntradas || espectaculo.selectedEntradas.length === 0) {
      alert('Selecciona al menos una entrada antes de continuar con la compra.');
      return;
    }

    localStorage.setItem('selectedEntries', JSON.stringify(espectaculo.selectedEntradas));
    localStorage.setItem(
      'selectedEspectaculo',
      JSON.stringify({
        artista: espectaculo.artista,
        fecha: espectaculo.fecha,
        escenario: espectaculo.escenario,
      })
    );

    const token = localStorage.getItem('userToken');
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }

    this.router.navigate(['/comprar']);
  }

  searchByArtista() {
    if (!this.searchArtista.trim()) {
      this.searchResults = [];
      this.isSearching = false;
      return;
    }

    this.isSearching = true;
    this.espectaculosService.searchEspectaculosByArtista(this.searchArtista).subscribe(
      (response: any) => {
        this.searchResults = response;
        this.isSearching = false;
        this.cdr.detectChanges();
      },
      (error: any) => {
        console.error('Error al buscar espectáculos por artista', error);
        this.searchResults = [];
        this.isSearching = false;
        this.cdr.detectChanges();
      }
    );
  }

  searchByFecha() {
    if (!this.searchFecha) {
      this.searchResults = [];
      this.isSearching = false;
      return;
    }

    this.isSearching = true;
    this.espectaculosService.searchEspectaculosByFecha(this.searchFecha).subscribe(
      (response: any) => {
        this.searchResults = response;
        this.isSearching = false;
        this.cdr.detectChanges();
      },
      (error: any) => {
        console.error('Error al buscar espectáculos por fecha', error);
        this.searchResults = [];
        this.isSearching = false;
        this.cdr.detectChanges();
      }
    );
  }

  clearSearch() {
    this.searchArtista = '';
    this.searchFecha = '';
    this.searchResults = [];
  }
}
