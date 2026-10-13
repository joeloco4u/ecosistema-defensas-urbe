import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProgramacionService {

  private readonly apiUrl = `${environment.apiUrl}/programacion`;

  constructor(private http: HttpClient) {}

  getSugerencias(cedulas: string[], espacioId: string, semana?: number | null, proyectoId?: string | null): Observable<any[]> {
    const fromObject: Record<string, string> = {
      espacioId: espacioId,
      cedulas: cedulas.join(','),
    };
    if (semana != null) {
      fromObject['semana'] = String(semana);
    }
    if (proyectoId) {
      fromObject['proyectoId'] = proyectoId;
    }
    const params = new HttpParams({ fromObject });
    return this.http.get<any[]>(`${this.apiUrl}/sugerencias`, { params });
  }
}
