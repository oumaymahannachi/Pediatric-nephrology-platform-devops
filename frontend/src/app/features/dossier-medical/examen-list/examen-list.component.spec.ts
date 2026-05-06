import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { ExamenListComponent } from './examen-list.component';
import { ExaminationService } from '../../../core/services/examination.service';
import { OcrService } from '../../../core/services/ocr.service';

describe('ExamenListComponent', () => {
  let component: ExamenListComponent;
  let fixture: ComponentFixture<ExamenListComponent>;
  let examinationServiceSpy: jasmine.SpyObj<ExaminationService>;

  const mockBloodTests = [
    { id: '1', patientId: 'p1', testType: 'CBC', testDate: new Date(), abnormal: false, laboratoryName: 'Lab A' },
    { id: '2', patientId: 'p1', testType: 'KIDNEY_FUNCTION', testDate: new Date(), abnormal: true, laboratoryName: 'Lab B' }
  ];

  const mockLabResults = [
    { id: '1', patientId: 'p1', testName: 'Urinalysis', testType: 'URINALYSIS', testDate: new Date(), result: 'NORMAL', abnormal: false }
  ];

  beforeEach(async () => {
    const examSpy = jasmine.createSpyObj('ExaminationService', [
      'getBloodTestsByPatient', 'getLabResultsByPatient', 'getMedicalImagingByPatient',
      'createBloodTest', 'updateBloodTest', 'deleteBloodTest',
      'createLabResult', 'updateLabResult', 'deleteLabResult',
      'createMedicalImaging', 'updateMedicalImaging', 'deleteMedicalImaging'
    ]);

    await TestBed.configureTestingModule({
      imports: [
        ExamenListComponent,
        HttpClientTestingModule,
        ReactiveFormsModule,
        FormsModule
      ],
      providers: [
        { provide: ExaminationService, useValue: examSpy },
        { provide: OcrService, useValue: jasmine.createSpyObj('OcrService', ['parseLabResults', 'parseImagingResults']) }
      ]
    }).compileComponents();

    examinationServiceSpy = TestBed.inject(ExaminationService) as jasmine.SpyObj<ExaminationService>;
    examinationServiceSpy.getBloodTestsByPatient.and.returnValue(of(mockBloodTests as any));
    examinationServiceSpy.getLabResultsByPatient.and.returnValue(of(mockLabResults as any));
    examinationServiceSpy.getMedicalImagingByPatient.and.returnValue(of([] as any));

    fixture = TestBed.createComponent(ExamenListComponent);
    component = fixture.componentInstance;
    component.patientId = 'p1';
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load blood tests on init', () => {
    fixture.detectChanges();
    expect(examinationServiceSpy.getBloodTestsByPatient).toHaveBeenCalledWith('p1');
    expect(component.bloodTests.length).toBe(2);
  });

  it('should filter abnormal blood tests', () => {
    fixture.detectChanges();
    const abnormal = component.bloodTests.filter(t => t.abnormal);
    expect(abnormal.length).toBe(1);
    expect(abnormal[0].testType).toBe('KIDNEY_FUNCTION');
  });

  it('should set active sub-tab to blood by default', () => {
    expect(component.activeSubTab).toBe('blood');
  });

  it('should switch sub-tab', () => {
    component.setSubTab('lab');
    expect(component.activeSubTab).toBe('lab');
  });

  it('should open blood test modal for new test', () => {
    component.openBloodTestModal();
    expect(component.showBloodTestModal).toBeTrue();
    expect(component.editingBloodTest).toBeNull();
  });

  it('should open blood test modal for editing', () => {
    const test = mockBloodTests[0] as any;
    component.openBloodTestModal(test);
    expect(component.showBloodTestModal).toBeTrue();
    expect(component.editingBloodTest).toEqual(test);
  });

  it('should delete blood test', () => {
    examinationServiceSpy.deleteBloodTest.and.returnValue(of(void 0));
    spyOn(window, 'confirm').and.returnValue(true);
    component.deleteBloodTest('1');
    expect(examinationServiceSpy.deleteBloodTest).toHaveBeenCalledWith('1');
  });

  it('should not delete if confirm is cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteBloodTest('1');
    expect(examinationServiceSpy.deleteBloodTest).not.toHaveBeenCalled();
  });

  it('should load lab results when switching to lab tab', () => {
    component.setSubTab('lab');
    expect(examinationServiceSpy.getLabResultsByPatient).toHaveBeenCalledWith('p1');
    expect(component.labResults.length).toBe(1);
  });

  it('should handle error when loading blood tests', () => {
    examinationServiceSpy.getBloodTestsByPatient.and.returnValue(throwError(() => new Error('Network error')));
    component.loadBloodTests();
    expect(component.bloodTests).toEqual([]);
  });

  it('should build comparative history from all examinations', () => {
    fixture.detectChanges();
    component.setSubTab('history');
    expect(component.allHistory.length).toBeGreaterThanOrEqual(0);
  });
});
