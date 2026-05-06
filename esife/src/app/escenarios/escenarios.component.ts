import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EspectaculosService } from '../espectaculos/espectaculos.service';

@Component({
  selector: 'app-escenarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './escenarios.html',
  styleUrl: './escenarios.css',
})
export class EscenariosComponent {
  escenarios: any[] = [];

  constructor(
    private espectaculosService: EspectaculosService,
    private cdr: ChangeDetectorRef
  ) {
    this.getEscenarios();
  }

  getEscenarios() {
    this.espectaculosService.getEscenarios().subscribe(
      (response: any) => {
        this.escenarios = response.map((e: any) => ({ ...e, visible: false }));
      },
      (error: any) => {
        console.error('Error al obtener los escenarios', error);
      }
    );
  }

  getEspectaculos(escenario: any) {
    if (escenario.visible) {
      escenario.visible = false;
      
      // AÑADIDO: Limpiamos los datos de los espectáculos para que se cierren las vistas
      if (escenario.espectaculos) {
        escenario.espectaculos.forEach((espectaculo: any) => {
          espectaculo.entradas = null;             // Cierra "Ver Disponibilidad"
          espectaculo.entradasDisponibles = null;  // Cierra "Ver Entradas Detalladas"
          espectaculo.selectedEntradas = [];       // Opcional: Limpia si habían seleccionado alguna
        });
      }
      return;
    }

    if (escenario.espectaculos) {
      escenario.visible = true;
      return;
    }

    this.espectaculosService.getEspectaculos(escenario).subscribe(
      (response: any) => {
        escenario.espectaculos = response;
        escenario.visible = true;
        this.cdr.detectChanges();
      },
      (error: any) => {
        console.error('Error al obtener los espectáculos', error);
      }
    );
  }

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
      window.location.href = '/login';
      return;
    }

    window.location.href = '/comprar';
  }

  getImagenEscenario(nombre: string): string {
    const imagenes: { [key: string]: string } = {
      'Auditorio Municipal': 'auditorio.jpg',
      'Teatro Metropólitan': 'teatro.jpg',
      'Palacio de Congresos': 'palacio.jpg',
      'default': 'escenario.jpg',
    };
    return imagenes[nombre] || imagenes['default'];
  }
}
