package tn.pedialink.dossiermedical.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.dossiermedical.service.PDFReportService;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class MedicalReportController {
    
    private final PDFReportService pdfReportService;
    
    @GetMapping("/pdf/patient/{patientId}")
    public ResponseEntity<byte[]> generatePDFReport(
            @PathVariable String patientId,
            @RequestParam(required = false, defaultValue = "Patient") String patientName) {
        
        try {
            byte[] pdfBytes = pdfReportService.generateMedicalReport(patientId, patientName);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                "medical_report_" + patientId + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
