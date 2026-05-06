import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { DoctorLabResultsComponent } from './doctor-lab-results.component';
import { LabResultService } from '../services/lab-result.service';
import { PdfExportService } from '../services/pdf-export.service';
import { TestType, ResultStatus, CKDStage } from '../models/lab-result.model';

describe('DoctorLabResultsComponent', () => {
  let component: DoctorLabResultsComponent;
  let fixture: ComponentFixture<DoctorLabResultsComponent>;
  let mockLabResultService: jasmine.SpyObj<LabResultService>;
  let mockPdfExportService: jasmine.SpyObj<PdfExportService>;

  const mockLabResults = [
    {
      id: '1', patientId: 'p1', doctorId: 'd1',
      testType: TestType.BLOOD, testDate: '2026-04-01',
      eGFR: 85, ckdStage: CKDStage.STAGE_1,
      isAbnormal: false, alerts: [],
      status: ResultStatus.PENDING,
      creatinine: 0.8, potassium: 4.0,
      createdAt: '2026-04-01'
    },
    {
      id: '2', patientId: 'p1', doctorId: 'd1',
      testType: TestType.BLOOD, testDate: '2026-03-01',
      eGFR: 45, ckdStage: CKDStage.STAGE_3A,
      isAbnormal: true, alerts: ['MODERATE: Reduced kidney function'],
      status: ResultStatus.VALIDATED,
      creatinine: 1.5, potassium: 5.8,
      createdAt: '2026-03-01'
    }
  ];

  beforeEach(async () => {
    mockLabResultService = jasmine.createSpyObj('LabResultService', [
      'getLabResultsByPatient', 'createLabResult', 'updateLabResult',
      'deleteLabResult', 'validateLabResult'
    ]);
    mockPdfExportService = jasmine.createSpyObj('PdfExportService', ['exportLabResult']);

    await TestBed.configureTestingModule({
      imports: [
        DoctorLabResultsComponent,
        HttpClientTestingModule,
        RouterTestingModule,
        FormsModule
      ],
      providers: [
        { provide: LabResultService, useValue: mockLabResultService },
        { provide: PdfExportService, useValue: mockPdfExportService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DoctorLabResultsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Initialization', () => {
    it('should initialize with empty results', () => {
      expect(component.labResults).toEqual([]);
      expect(component.filteredResults).toEqual([]);
    });

    it('should have default filter values', () => {
      expect(component.filterType).toBe('ALL');
      expect(component.filterAbnormal).toBe('ALL');
      expect(component.sortBy).toBe('date');
      expect(component.sortOrder).toBe('desc');
    });
  });

  describe('loadLabResults', () => {
    it('should load lab results for selected patient', () => {
      mockLabResultService.getLabResultsByPatient.and.returnValue(
        of({ success: true, data: mockLabResults })
      );
      component.selectedChildId = 'p1';

      component.loadLabResults('p1');

      expect(mockLabResultService.getLabResultsByPatient).toHaveBeenCalledWith('p1');
      expect(component.labResults.length).toBe(2);
    });

    it('should set loading to false after load', () => {
      mockLabResultService.getLabResultsByPatient.and.returnValue(
        of({ success: true, data: [] })
      );

      component.loadLabResults('p1');

      expect(component.loading).toBeFalse();
    });
  });

  describe('applyFilters', () => {
    beforeEach(() => {
      component.labResults = mockLabResults as any;
    });

    it('should show all results with ALL filter', () => {
      component.filterType = 'ALL';
      component.filterAbnormal = 'ALL';
      component.applyFilters();

      expect(component.filteredResults.length).toBe(2);
    });

    it('should filter abnormal results only', () => {
      component.filterAbnormal = 'ABNORMAL';
      component.applyFilters();

      expect(component.filteredResults.length).toBe(1);
      expect(component.filteredResults[0].isAbnormal).toBeTrue();
    });

    it('should filter normal results only', () => {
      component.filterAbnormal = 'NORMAL';
      component.applyFilters();

      expect(component.filteredResults.length).toBe(1);
      expect(component.filteredResults[0].isAbnormal).toBeFalse();
    });

    it('should filter by test type BLOOD', () => {
      component.filterType = TestType.BLOOD;
      component.applyFilters();

      expect(component.filteredResults.every(r => r.testType === TestType.BLOOD)).toBeTrue();
    });

    it('should sort by eGFR ascending', () => {
      component.sortBy = 'eGFR';
      component.sortOrder = 'asc';
      component.applyFilters();

      expect(component.filteredResults[0].eGFR).toBeLessThanOrEqual(
        component.filteredResults[1].eGFR!
      );
    });
  });

  describe('getStatusClass', () => {
    it('should return correct class for PENDING status', () => {
      expect(component.getStatusClass(ResultStatus.PENDING)).toBe('status-pending');
    });

    it('should return correct class for VALIDATED status', () => {
      expect(component.getStatusClass(ResultStatus.VALIDATED)).toBe('status-validated');
    });
  });

  describe('getCKDStageLabel', () => {
    it('should return correct label for Stage 1', () => {
      expect(component.getCKDStageLabel(CKDStage.STAGE_1)).toContain('Stage 1');
    });

    it('should return correct label for Stage 5', () => {
      expect(component.getCKDStageLabel(CKDStage.STAGE_5)).toContain('Stage 5');
    });
  });

  describe('deleteLabResult', () => {
    it('should call service and reload results after deletion', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      mockLabResultService.deleteLabResult.and.returnValue(of({ success: true, data: undefined as any }));
      mockLabResultService.getLabResultsByPatient.and.returnValue(of({ success: true, data: [] }));
      component.selectedChildId = 'p1';

      component.deleteLabResult({ id: '1' } as any);

      expect(mockLabResultService.deleteLabResult).toHaveBeenCalledWith('1');
    });

    it('should not delete if user cancels confirmation', () => {
      spyOn(window, 'confirm').and.returnValue(false);

      component.deleteLabResult({ id: '1' } as any);

      expect(mockLabResultService.deleteLabResult).not.toHaveBeenCalled();
    });
  });

  describe('exportDoctorPDF', () => {
    it('should call pdf export service', () => {
      const mockResult = mockLabResults[0] as any;
      component.exportDoctorPDF(mockResult);

      expect(mockPdfExportService.exportLabResult).toHaveBeenCalledWith(mockResult, 'doctor');
    });
  });
});
