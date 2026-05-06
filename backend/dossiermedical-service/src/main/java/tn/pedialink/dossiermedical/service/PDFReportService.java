package tn.pedialink.dossiermedical.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.model.consultation.Consultation;
import tn.pedialink.dossiermedical.model.examen.BloodTest;
import tn.pedialink.dossiermedical.model.examen.LabResult;
import tn.pedialink.dossiermedical.model.examen.MedicalImaging;
import tn.pedialink.dossiermedical.model.dialyse.DialysisPrescription;
import tn.pedialink.dossiermedical.repository.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PDFReportService {
    
    private final ConsultationRepository consultationRepository;
    private final BloodTestRepository bloodTestRepository;
    private final LabResultRepository labResultRepository;
    private final MedicalImagingRepository medicalImagingRepository;
    private final DialysisPrescriptionRepository dialysisPrescriptionRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public byte[] generateMedicalReport(String patientId, String patientName) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Header
            addHeader(document, patientName);
            
            // Consultations
            addConsultationsSection(document, patientId);
            
            // Blood Tests
            addBloodTestsSection(document, patientId);
            
            // Lab Results
            addLabResultsSection(document, patientId);
            
            // Medical Imaging
            addMedicalImagingSection(document, patientId);
            
            // Dialysis
            addDialysisSection(document, patientId);
            
            // Footer
            addFooter(document);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }
    
    private void addHeader(Document document, String patientName) {
        document.add(new Paragraph("PEDIALINK - MEDICAL REPORT")
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER));
        
        document.add(new Paragraph("Patient: " + patientName)
            .setFontSize(14)
            .setTextAlignment(TextAlignment.CENTER));
        
        document.add(new Paragraph("Generated: " + LocalDateTime.now().format(DATE_FORMATTER))
            .setFontSize(10)
            .setTextAlignment(TextAlignment.CENTER));
        
        document.add(new Paragraph("\n"));
    }
    
    private void addConsultationsSection(Document document, String patientId) {
        List<Consultation> consultations = consultationRepository.findByPatientId(patientId);
        
        document.add(new Paragraph("CONSULTATIONS")
            .setFontSize(16)
            .setBold());
        
        if (consultations.isEmpty()) {
            document.add(new Paragraph("No consultations recorded."));
        } else {
            for (Consultation c : consultations) {
                if (c.getDateRendezVous() != null) {
                    document.add(new Paragraph("Date: " + c.getDateRendezVous().format(DATE_FORMATTER)));
                }
                if (c.getObservationsCliniques() != null) {
                    document.add(new Paragraph("Observations: " + c.getObservationsCliniques()));
                }
                if (c.getDiagnostic() != null) {
                    document.add(new Paragraph("Diagnosis: " + c.getDiagnostic()));
                }
                document.add(new Paragraph("\n"));
            }
        }
    }
    
    private void addBloodTestsSection(Document document, String patientId) {
        List<BloodTest> tests = bloodTestRepository.findByPatientId(patientId);
        
        document.add(new Paragraph("BLOOD TESTS")
            .setFontSize(16)
            .setBold());
        
        if (tests.isEmpty()) {
            document.add(new Paragraph("No blood tests recorded."));
        } else {
            for (BloodTest test : tests) {
                document.add(new Paragraph("Date: " + test.getTestDate().format(DATE_FORMATTER)));
                document.add(new Paragraph("Type: " + test.getTestType()));
                document.add(new Paragraph("Status: " + (test.getAbnormal() ? "ABNORMAL" : "NORMAL")));
                document.add(new Paragraph("\n"));
            }
        }
    }
    
    private void addLabResultsSection(Document document, String patientId) {
        List<LabResult> results = labResultRepository.findByPatientId(patientId);
        
        document.add(new Paragraph("LABORATORY RESULTS")
            .setFontSize(16)
            .setBold());
        
        if (results.isEmpty()) {
            document.add(new Paragraph("No lab results recorded."));
        } else {
            for (LabResult result : results) {
                document.add(new Paragraph("Date: " + result.getTestDate().format(DATE_FORMATTER)));
                document.add(new Paragraph("Test: " + result.getTestName()));
                document.add(new Paragraph("Result: " + result.getResult()));
                document.add(new Paragraph("\n"));
            }
        }
    }
    
    private void addMedicalImagingSection(Document document, String patientId) {
        List<MedicalImaging> imagings = medicalImagingRepository.findByPatientId(patientId);
        
        document.add(new Paragraph("MEDICAL IMAGING")
            .setFontSize(16)
            .setBold());
        
        if (imagings.isEmpty()) {
            document.add(new Paragraph("No imaging records."));
        } else {
            for (MedicalImaging img : imagings) {
                document.add(new Paragraph("Date: " + img.getImagingDate().format(DATE_FORMATTER)));
                document.add(new Paragraph("Type: " + img.getImagingType() + " - " + img.getBodyPart()));
                if (img.getFindings() != null) {
                    document.add(new Paragraph("Findings: " + img.getFindings()));
                }
                document.add(new Paragraph("\n"));
            }
        }
    }
    
    private void addDialysisSection(Document document, String patientId) {
        List<DialysisPrescription> prescriptions = dialysisPrescriptionRepository.findByPatientId(patientId);
        
        document.add(new Paragraph("DIALYSIS PRESCRIPTIONS")
            .setFontSize(16)
            .setBold());
        
        if (prescriptions.isEmpty()) {
            document.add(new Paragraph("No dialysis prescriptions."));
        } else {
            for (DialysisPrescription p : prescriptions) {
                document.add(new Paragraph("Type: " + p.getType()));
                document.add(new Paragraph("Frequency: " + p.getFrequencyPerWeek() + " times/week"));
                document.add(new Paragraph("Duration: " + p.getSessionDurationMinutes() + " minutes"));
                document.add(new Paragraph("\n"));
            }
        }
    }
    
    private void addFooter(Document document) {
        document.add(new Paragraph("\n\n"));
        document.add(new Paragraph("This is a confidential medical document.")
            .setFontSize(8)
            .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("PediaLink - Pediatric Nephrology Platform")
            .setFontSize(8)
            .setTextAlignment(TextAlignment.CENTER));
    }
}
