import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class DefensaService {

  private readonly apiUrl = 'http://localhost:8080/api/defensas';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  crearDefensa(payload: any): Observable<any> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.post<any>(this.apiUrl, payload, { headers });
  }
}
