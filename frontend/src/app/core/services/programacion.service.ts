import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProgramacionService {

  private readonly apiUrl = `${environment.apiUrl}/programacion`;

  constructor(private http: HttpClient) {}

  getSugerencias(cedulas: string[], espacioId: string): Observable<any[]> {
    const params = new HttpParams({
      fromObject: {
        espacioId: espacioId,
        cedulas: cedulas.join(',')
      }
    });
    return this.http.get<any[]>(`${this.apiUrl}/sugerencias`, { params });
  }
}
