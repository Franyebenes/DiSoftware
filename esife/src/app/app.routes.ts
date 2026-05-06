import { Routes } from '@angular/router';
import { CompraComponent } from './compra/compra';
import { Login } from './auth/login'; // es el component, habria que cambiarlo a LoginComponent para mas consistencia
import { AuthGuard } from './auth/auth.guard';
import { MainLayoutComponent } from './main-layout/main-layout';

export const routes: Routes = [
  { path: "", component: MainLayoutComponent },
  { path: "comprar", component: CompraComponent, canActivate: [AuthGuard] },
  { path: "login", component: Login }
];