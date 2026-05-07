import { Injectable, Inject, PLATFORM_ID } from '@angular/core'; // 1. Añadimos Inject y PLATFORM_ID
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common'; // 2. Añadimos isPlatformBrowser

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  // 3. Inyectamos el platformId en el constructor
  constructor(
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object 
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    
    // 4. Envolvemos tu lógica en el 'if' para protegerla del servidor
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('userToken');
      
      if (token) {
        return true;
      } else {
        this.router.navigate(['/login']);
        return false;
      }
    }

    // 5. Si es el servidor Node.js, simplemente devolvemos false para que no colapse
    return false;
  }
}