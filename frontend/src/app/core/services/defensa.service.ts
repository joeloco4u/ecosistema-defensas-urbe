import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DefensaService {

  private readonly apiUrl = `${environment.apiUrl}/defensas`;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  listarDefensas(tutorId?: number, proyectoId?: string, escuela?: string): Observable<any[]> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    let params = new HttpParams();
    if (tutorId != null) { params = params.set('tutorId', tutorId.toString()); }
    if (proyectoId != null) { params = params.set('proyectoId', proyectoId); }
    if (escuela != null) { params = params.set('escuela', escuela); }
    return this.http.get<any[]>(this.apiUrl, { headers, params });
  }

  crearDefensa(payload: any): Observable<any> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.post<any>(this.apiUrl, payload, { headers });
  }
}
