import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DocenteService {

  private readonly apiUrl = `${environment.apiUrl}/docentes`;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  getDocentes(): Observable<any[]> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.get<any[]>(this.apiUrl, { headers });
  }

  getJuradosDisponibles(): Observable<any[]> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.get<any[]>(`${this.apiUrl}/jurados`, { headers });
  }

  actualizarEstado(id: number, estado: string): Observable<any> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.patch<any>(`${this.apiUrl}/${id}/estado`, { estado }, { headers });
  }

  getExpediente(id: number): Observable<any> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.get<any>(`${this.apiUrl}/${id}/expediente`, { headers });
  }

  crearDocente(docente: any): Observable<any> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.post<any>(this.apiUrl, docente, { headers });
  }
}
