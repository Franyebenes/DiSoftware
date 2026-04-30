import { Routes } from '@angular/router';
import { CompraComponent } from './compra/compra';
import { Espectaculos } from './espectaculos/espectaculos';
import { Login } from './auth/login'; // es el component, habria que cambiarlo a LoginComponent para mas consistencia

export const routes: Routes = [
  { path: "", component: Espectaculos },
  { path: "comprar", component: CompraComponent },
  { path: "login", component: Login }
];