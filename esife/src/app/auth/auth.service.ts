import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly URL = 'http://localhost:8081/users';
  
  login(nombre: string, password: string) {
    const body = { 
      name: nombre, 
      password: password 
    };
    return this.http.post(`${this.URL}/login`, body, { responseType: 'text' });
  }

  register(email: string, p1: string, p2: string) {
    const body = { 
      email: email, 
      pwd1: p1, 
      pwd2: p2 
    };
    
    return this.http.post(`${this.URL}/register`, body, { responseType: 'text' });
  }
}
