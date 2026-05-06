import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  generatePDFReport(patientId: string, patientName: string): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/pdf/patient/${patientId}?patientName=${encodeURIComponent(patientName)}`,
      { responseType: 'blob' }
    );
  }

  downloadPDF(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
  }
}
