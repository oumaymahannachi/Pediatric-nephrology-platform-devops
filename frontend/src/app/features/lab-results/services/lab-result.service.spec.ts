import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LabResultService } from './lab-result.service';
import { environment } from '../../../../environments/environment';
import { TestType } from '../models/lab-result.model';

describe('LabResultService', () => {
  let service: LabResultService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/lab-results`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LabResultService]
    });
    service = TestBed.inject(LabResultService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getLabResultsByPatient', () => {
    it('should return lab results for a patient', () => {
      const mockResponse = {
        success: true,
        data: [
          { id: '1', patientId: 'p1', testType: TestType.BLOOD, eGFR: 85 }
        ]
      };

      service.getLabResultsByPatient('p1').subscribe(response => {
        expect(response.data.length).toBe(1);
        expect(response.data[0].patientId).toBe('p1');
      });

      const req = httpMock.expectOne(`${apiUrl}/patient/p1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('createLabResult', () => {
    it('should create a lab result and return it', () => {
      const request = {
        patientId: 'p1',
        testType: TestType.BLOOD,
        testDate: new Date().toISOString(),
        creatinine: 0.8
      };
      const mockResponse = { success: true, data: { id: '1', ...request } };

      service.createLabResult(request as any).subscribe(response => {
        expect(response.data.id).toBe('1');
        expect(response.data.patientId).toBe('p1');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockResponse);
    });
  });

  describe('getLabResultById', () => {
    it('should return a specific lab result', () => {
      const mockResponse = { success: true, data: { id: 'lab-1', patientId: 'p1' } };

      service.getLabResultById('lab-1').subscribe(response => {
        expect(response.data.id).toBe('lab-1');
      });

      const req = httpMock.expectOne(`${apiUrl}/lab-1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('updateLabResult', () => {
    it('should update a lab result', () => {
      const updateRequest = { creatinine: 1.2 } as any;
      const mockResponse = { success: true, data: { id: 'lab-1', creatinine: 1.2 } };

      service.updateLabResult('lab-1', updateRequest).subscribe(response => {
        expect(response.data.creatinine).toBe(1.2);
      });

      const req = httpMock.expectOne(`${apiUrl}/lab-1`);
      expect(req.request.method).toBe('PUT');
      req.flush(mockResponse);
    });
  });

  describe('deleteLabResult', () => {
    it('should delete a lab result', () => {
      const mockResponse = { success: true, data: null };

      service.deleteLabResult('lab-1').subscribe(response => {
        expect(response.success).toBeTrue();
      });

      const req = httpMock.expectOne(`${apiUrl}/lab-1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);
    });
  });

  describe('validateLabResult', () => {
    it('should validate a lab result', () => {
      const mockResponse = { success: true, data: { id: 'lab-1', status: 'VALIDATED' } };

      service.validateLabResult('lab-1').subscribe(response => {
        expect(response.data.status).toBe('VALIDATED');
      });

      const req = httpMock.expectOne(`${apiUrl}/lab-1/validate`);
      expect(req.request.method).toBe('PUT');
      req.flush(mockResponse);
    });
  });

  describe('getAbnormalLabResults', () => {
    it('should return only abnormal results', () => {
      const mockResponse = {
        success: true,
        data: [{ id: '1', isAbnormal: true, alerts: ['URGENT: High potassium'] }]
      };

      service.getAbnormalLabResults('p1').subscribe(response => {
        expect(response.data.length).toBe(1);
        expect(response.data[0].isAbnormal).toBeTrue();
      });

      const req = httpMock.expectOne(`${apiUrl}/patient/p1/abnormal`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });
});
