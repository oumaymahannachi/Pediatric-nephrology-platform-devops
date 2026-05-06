package tn.pedialink.dossiermedical.controller;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.pedialink.dossiermedical.service.OCRService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OCRController {
    private final OCRService ocrService;

    @PostMapping("/extract-text/image")
    public ResponseEntity<Map<String, String>> extractTextFromImage(@RequestParam("file") MultipartFile file) {
        try {
            String text = ocrService.extractTextFromImage(file);
            Map<String, String> response = new HashMap<>();
            response.put("extractedText", text);
            response.put("success", "true");
            return ResponseEntity.ok(response);
        } catch (IOException | TesseractException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("success", "false");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/extract-text/pdf")
    public ResponseEntity<Map<String, String>> extractTextFromPDF(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File is empty");
                error.put("success", "false");
                return ResponseEntity.badRequest().body(error);
            }
            
            String text = ocrService.extractTextFromPDF(file);
            Map<String, String> response = new HashMap<>();
            response.put("extractedText", text);
            response.put("success", "true");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "IO Error: " + e.getMessage());
            error.put("success", "false");
            return ResponseEntity.badRequest().body(error);
        } catch (TesseractException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "OCR Error: " + e.getMessage());
            error.put("success", "false");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unexpected error: " + e.getMessage());
            error.put("success", "false");
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/extract-text/base64")
    public ResponseEntity<Map<String, String>> extractTextFromBase64(@RequestBody Map<String, String> request) {
        try {
            String base64Data = request.get("base64Data");
            String text = ocrService.extractTextFromBase64(base64Data);
            Map<String, String> response = new HashMap<>();
            response.put("extractedText", text);
            response.put("success", "true");
            return ResponseEntity.ok(response);
        } catch (IOException | TesseractException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("success", "false");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/parse/lab-results")
    public ResponseEntity<OCRService.LabResultOCR> parseLabResults(@RequestParam("file") MultipartFile file) {
        try {
            String text = file.getContentType() != null && file.getContentType().contains("pdf")
                    ? ocrService.extractTextFromPDF(file)
                    : ocrService.extractTextFromImage(file);
            
            OCRService.LabResultOCR result = ocrService.parseLabResults(text);
            return ResponseEntity.ok(result);
        } catch (IOException | TesseractException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/parse/imaging-results")
    public ResponseEntity<OCRService.ImagingResultOCR> parseImagingResults(@RequestParam("file") MultipartFile file) {
        try {
            String text = file.getContentType() != null && file.getContentType().contains("pdf")
                    ? ocrService.extractTextFromPDF(file)
                    : ocrService.extractTextFromImage(file);
            
            OCRService.ImagingResultOCR result = ocrService.parseImagingResults(text);
            return ResponseEntity.ok(result);
        } catch (IOException | TesseractException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/parse/lab-results/base64")
    public ResponseEntity<OCRService.LabResultOCR> parseLabResultsFromBase64(@RequestBody Map<String, String> request) {
        try {
            String base64Data = request.get("base64Data");
            String text = ocrService.extractTextFromBase64(base64Data);
            OCRService.LabResultOCR result = ocrService.parseLabResults(text);
            return ResponseEntity.ok(result);
        } catch (IOException | TesseractException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/parse/imaging-results/base64")
    public ResponseEntity<OCRService.ImagingResultOCR> parseImagingResultsFromBase64(@RequestBody Map<String, String> request) {
        try {
            String base64Data = request.get("base64Data");
            String text = ocrService.extractTextFromBase64(base64Data);
            OCRService.ImagingResultOCR result = ocrService.parseImagingResults(text);
            return ResponseEntity.ok(result);
        } catch (IOException | TesseractException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
