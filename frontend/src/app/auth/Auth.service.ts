import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface GlobalResponse<T> {
  status: string;
  data: T | null;
  errors: { message: string }[] | null;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/auth'; // Direct backend URL

  register(email: string, username: string, password: string): Observable<GlobalResponse<string>> {
    return this.http.post<GlobalResponse<string>>(`${this.apiUrl}/register`, { email, username, password }).pipe(
      map(response => {
        if (response.status !== 'success') {
          throw new Error(response.errors?.[0]?.message || 'Registration failed');
        }
        return response;
      })
    );
  }

  login(email: string, password: string): Observable<GlobalResponse<string>> {
    return this.http.post<GlobalResponse<string>>(`${this.apiUrl}/login`, { email, password }).pipe(
      map(response => {
        if (response.status !== 'success') {
          throw new Error(response.errors?.[0]?.message || 'Login failed');
        }
        return response;
      })
    );
  }

  sendPresence(userId: string) {
    this.http.post('http://localhost:8080/api/presence', { userId }).subscribe();
  }
}