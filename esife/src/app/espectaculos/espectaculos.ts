import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EspectaculosService } from './espectaculos.service';
import { Router } from '@angular/router';


@Component({
  selector: 'app-espectaculos',
  imports: [CommonModule],
  standalone: true,
  templateUrl: './espectaculos.html',
  styleUrl: './espectaculos.css',
})
export class Espectaculos {
  escenarios: any[] = [];
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

  irAComprarEntradas() {
    const token = localStorage.getItem('userToken');
    if (token) {
      this.router.navigate(['/comprar']);
    } else {
      this.router.navigate(['/login']);
    }
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
