import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TutorSugerido } from '../models/tutor-sugerido.model';

@Injectable({ providedIn: 'root' })
export class TutorSugeridoService {

  private readonly apiUrl = 'https://ecosistema-defensas-urbe-production.up.railway.app/api/tutores-sugeridos';

  constructor(private http: HttpClient) {}

  sugerirTutor(tutor: Partial<TutorSugerido>): Observable<TutorSugerido> {
    return this.http.post<TutorSugerido>(this.apiUrl, tutor);
  }

  obtenerPendientes(): Observable<TutorSugerido[]> {
    return this.http.get<TutorSugerido[]>(`${this.apiUrl}/pendientes`);
  }

  actualizarEstado(id: string, estado: string): Observable<TutorSugerido> {
    return this.http.patch<TutorSugerido>(`${this.apiUrl}/${id}/estado`, { estado });
  }
}
