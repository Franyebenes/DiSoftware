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
  escenarios: any[] = [];
  searchArtista: string = '';
  searchFecha: string = '';
  searchResults: any[] = [];
  isSearching: boolean = false;
  selectedEntryIds: number[] = [];
  selectedEspectaculo: any = null;

  constructor(private espectaculosService: EspectaculosService, private router: Router, private cdr: ChangeDetectorRef) {
    this.getEscenarios();
   }

  getEscenarios() {
    this.espectaculosService.getEscenarios().subscribe(
      (response: any) => {
        // añadimos la propiedad `visible` para el control de despliegue
        this.escenarios = response.map((e: any) => ({ ...e, visible: false }));
      },
      (error: any) => {
        console.error('Error al obtener los escenarios', error);
      }
    )
  }

  getEspectaculos(escenario: any) {
    if (escenario.visible) {
      escenario.visible = false;
      return;
    }
    
    if (escenario.espectaculos) {
      escenario.visible = true;
      return;
    }
    
    // carga inicial
    this.espectaculosService.getEspectaculos(escenario).subscribe(
      (response: any) => {
        escenario.espectaculos = response;
        escenario.visible = true; 
        
        this.cdr.detectChanges(); 
      },
      (error: any) => {
        console.error('Error al obtener los espectáculos', error);
      }
    )
  }

  /* Ejemplo de una petición anidada (se envía cuando se recibe la respuesta de la primera petición)
  getNumeroDeEntradas(espectaculo: any){
  this.espectaculosService.getNumeroDeEntradas(espectaculo.id).subscribe(
    (response: any) => {
      espectaculo.entradas = response;
      this.getEntradasLibres(espectaculo)
    },
    (error:any) => {
      console.error('Error al obtener las entradas', error);
    }
  );
  } 

  getEntradasLibres(espectaculo: any) {
    this.espectaculosService.getEntradasLibres(espectaculo.id).subscribe(
    (response: any) => {
      espectaculo.getEntradasLibres = response
    },
    (error:any) => {
      console.error('Error al obtener las entradas', error);
    }
  );
  } */

  getNumeroDeEntradas(espectaculo: any) {
    this.espectaculosService.getNumeroDeEntradasComoDto(espectaculo).subscribe(
      (response: any) => {
        espectaculo.entradas = response;
        this.cdr.detectChanges();
      },
      (error: any) => {
        console.error('Error al obtener las entradas', error);
      }
    );
  }

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
      if (this.selectedEntryIds.length >= 5) {
        entrada.seleccionada = false;
        alert('Solo puedes seleccionar hasta 5 entradas.');
        return;
      }
      this.selectedEntryIds.push(entrada.id);
      espectaculo.selectedEntradas.push(entrada);
      this.selectedEspectaculo = espectaculo;
    } else {
      this.selectedEntryIds = this.selectedEntryIds.filter((id) => id !== entrada.id);
      espectaculo.selectedEntradas = espectaculo.selectedEntradas.filter((item: any) => item.id !== entrada.id);
      if (this.selectedEntryIds.length === 0) {
        this.selectedEspectaculo = null;
      }
    }
  }

  irAComprarEntradas(espectaculo: any) {
    if (!espectaculo.selectedEntradas || espectaculo.selectedEntradas.length === 0) {
      alert('Selecciona al menos una entrada antes de continuar con la compra.');
      return;
    }

    const token = localStorage.getItem('userToken');
    localStorage.setItem('selectedEntries', JSON.stringify(espectaculo.selectedEntradas));
    localStorage.setItem('selectedEspectaculo', JSON.stringify({ artista: espectaculo.artista, fecha: espectaculo.fecha, escenario: espectaculo.escenario }));

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
    this.isSearching = false;
  }

  getImagenEscenario(nombre: string): string {
    const imagenes: any = {
      'Auditorio Principal': 'AuditorioPrincipal.jpg', //
      'Teatro Clásico': 'teatroclasico.jpg',          //
      'Sala Experimental': 'salaexperimental.jpg',    //
      'Estadio Municipal': 'estadiomunicipal.jpeg',   //
      'Plaza Abierta': 'plazaAbierta.jpg',             //
      'Teatro Quijano': 'TeatroQuijano.jpg'                    //
    };
    return imagenes[nombre] || 'FondoEspectaculos.jpg'; //
  }
}
