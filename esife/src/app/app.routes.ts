import { Routes } from '@angular/router';
import { CompraComponent } from './compra/compra';
import { Login } from './auth/login';
import { AuthGuard } from './auth/auth.guard';
import { MainLayoutComponent } from './main-layout/main-layout';
import { ConfirmComponent } from './auth/confirm';
import { ResetPasswordComponent } from './auth/reset-password';
import { CuentaComponent } from './cuenta/cuenta.component';

export const routes: Routes = [
  { path: "", component: MainLayoutComponent },
  { path: "comprar", component: CompraComponent, canActivate: [AuthGuard] },
  { path: "login", component: Login },
  { path: "confirm", component: ConfirmComponent },
  { path: "reset-password", component: ResetPasswordComponent },
  { path: 'cuenta', component: CuentaComponent },
];