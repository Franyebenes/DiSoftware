  import { inject, Injectable } from '@angular/core';
  import { HttpClient } from '@angular/common/http';

  @Injectable({
    providedIn: 'root',
  })
  export class AuthService {
    private readonly http = inject(HttpClient);

    private readonly URL = 'http://localhost:8081/users';
    
  login(email: string, password: string) {
    const body = { 
      email: email, 
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

  confirmEmail(token: string) {
    return this.http.get(`${this.URL}/confirm`, {
      params: { token: token },
      responseType: 'text'
    });
  }

  forgotPassword(email: string) {
    const body = { email: email };
    return this.http.post(`${this.URL}/forgot-password`, body, { responseType: 'text' });
  }

  resetPassword(resetToken: string, newPassword: string) {
    const body = { 
      resetToken: resetToken, 
      newPassword: newPassword 
    };
    return this.http.post(`${this.URL}/reset-password`, body, { responseType: 'text' });
  }

  deleteAccount(token: string) {
    const headers = { 'Authorization': `Bearer ${token}` };
    return this.http.delete(`${this.URL}/account`, { 
      headers: headers, 
      responseType: 'text' 
    });
  }

  validateToken(token: string) {
    return this.http.get(`${this.URL}/validate-token`, {
      params: { token: token },
      responseType: 'text'
    });
  }
}
