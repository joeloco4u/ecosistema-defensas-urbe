import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class EspacioFisicoService {

  private readonly apiUrl = 'https://ecosistema-defensas-urbe-production.up.railway.app/api/espacios';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  getEspaciosFisicos(): Observable<any[]> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.get<any[]>(this.apiUrl, { headers });
  }
}
