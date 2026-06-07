import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page, PageParams } from '../dtos/pagination.dto';
import { RequestRequest, RequestResponse, RequestUpdate } from './request.dto';

@Injectable({ providedIn: 'root' })
export class RequestsApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/requests`;

  getAll(params?: PageParams): Observable<Page<RequestResponse>> {
    let httpParams = new HttpParams();
    if (params?.page !== undefined) httpParams = httpParams.set('page', params.page);
    if (params?.size !== undefined) httpParams = httpParams.set('size', params.size);
    if (params?.sort) httpParams = httpParams.set('sort', params.sort);
    if (params?.search) httpParams = httpParams.set('search', params.search);
    return this.http.get<Page<RequestResponse>>(this.baseUrl, { params: httpParams });
  }

  getById(id: string): Observable<RequestResponse> {
    return this.http.get<RequestResponse>(`${this.baseUrl}/${id}`);
  }

  create(body: RequestRequest): Observable<RequestResponse> {
    return this.http.post<RequestResponse>(this.baseUrl, body);
  }

  update(id: string, body: RequestUpdate): Observable<RequestResponse> {
    return this.http.put<RequestResponse>(`${this.baseUrl}/${id}`, body);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  lock(id: string): Observable<RequestResponse> {
    return this.http.post<RequestResponse>(`${this.baseUrl}/${id}/lock`, null);
  }

  unlock(id: string): Observable<RequestResponse> {
    return this.http.post<RequestResponse>(`${this.baseUrl}/${id}/unlock`, null);
  }
}
