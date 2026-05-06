package tn.pedialink.prescription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class OcrService {

    @Value("${google.vision.api.key:}")
    private String googleVisionApiKey;

    /**
     * Extract prescription data from image using OCR
     * This is a simplified implementation. In production, integrate with Google Vision API or Azure Computer Vision
     */
    public Map<String, Object> extractPrescriptionFromImage(MultipartFile image) {
        log.info("Processing prescription image: {}", image.getOriginalFilename());
        
        try {
            // Validate image
            if (!isValidImage(image)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Invalid image format. Please upload JPG or PNG");
                return error;
            }
            
            // Simulate OCR processing
            // In production, call Google Vision API or Azure Computer Vision here
            String extractedText = simulateOcrExtraction(image);
            
            // Parse the extracted text
            return parsePrescriptionText(extractedText);
            
        } catch (Exception e) {
            log.error("Error processing image", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error processing image: " + e.getMessage());
            return error;
        }
    }

    /**
     * Simulate OCR extraction (replace with actual API call in production)
     */
    private String simulateOcrExtraction(MultipartFile image) {
        // This is a placeholder. In production, you would:
        // 1. Convert image to base64 or byte array
        // 2. Call Google Vision API or Azure Computer Vision
        // 3. Return the extracted text
        
        log.info("Simulating OCR extraction for image: {}", image.getOriginalFilename());
        
        // Return sample extracted text for demonstration
        return """
            Dr. Ahmed Ben Salem
            Prescription Médicale
            
            Patient: Mohamed Ali
            Date: 15/01/2024
            
            Diagnostic: Infection respiratoire
            
            Médicaments:
            1. Amoxicilline 500mg
               Posologie: 1 comprimé 3 fois par jour
               Durée: 7 jours
               
            2. Paracétamol 500mg
               Posologie: 1 comprimé toutes les 6 heures si fièvre
               Durée: 5 jours
               
            Notes: Prendre avec de la nourriture
            Renouvellement: Non
            """;
    }

    /**
     * Parse extracted text to structured prescription data
     */
    private Map<String, Object> parsePrescriptionText(String text) {
        Map<String, Object> prescription = new HashMap<>();
        
        try {
            // Extract diagnostic
            String diagnostic = extractField(text, "Diagnostic:\\s*(.+?)(?=\\n|Médicaments|$)");
            prescription.put("diagnostic", diagnostic != null ? diagnostic : "");
            
            // Extract medications
            List<Map<String, Object>> medications = extractMedications(text);
            prescription.put("medicaments", medications);
            
            // Extract notes
            String notes = extractField(text, "Notes:\\s*(.+?)(?=\\n|Renouvellement|$)");
            prescription.put("notes", notes != null ? notes : "");
            
            // Extract renewable status
            String renewable = extractField(text, "Renouvellement:\\s*(.+?)(?=\\n|$)");
            prescription.put("renouvelable", renewable != null && renewable.toLowerCase().contains("oui"));
            
            // Extract date
            String date = extractField(text, "Date:\\s*(\\d{2}/\\d{2}/\\d{4})");
            prescription.put("datePrescription", date != null ? date : "");
            
            prescription.put("success", true);
            prescription.put("message", "Prescription extracted successfully");
            
        } catch (Exception e) {
            log.error("Error parsing prescription text", e);
            prescription.put("success", false);
            prescription.put("message", "Error parsing prescription: " + e.getMessage());
        }
        
        return prescription;
    }

    /**
     * Extract medications from text
     */
    private List<Map<String, Object>> extractMedications(String text) {
        List<Map<String, Object>> medications = new ArrayList<>();
        
        // Pattern to match medication blocks
        Pattern medPattern = Pattern.compile(
            "(\\d+\\.\\s+)?([A-Za-zÀ-ÿ\\s]+\\d+mg)\\s+" +
            "Posologie:\\s*(.+?)\\s+" +
            "Durée:\\s*(\\d+)\\s*jours?",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        
        Matcher matcher = medPattern.matcher(text);
        
        while (matcher.find()) {
            Map<String, Object> medication = new HashMap<>();
            
            String fullName = matcher.group(2).trim();
            String[] parts = fullName.split("\\s+");
            String name = String.join(" ", Arrays.copyOf(parts, parts.length - 1));
            String dosage = parts[parts.length - 1];
            
            medication.put("nomCommercial", name);
            medication.put("dosage", dosage);
            medication.put("dci", name); // In production, lookup DCI from database
            
            Map<String, Object> posologie = new HashMap<>();
            posologie.put("instructions", matcher.group(3).trim());
            posologie.put("dureeTraitementJours", Integer.parseInt(matcher.group(4)));
            
            medication.put("posologie", posologie);
            medications.add(medication);
        }
        
        return medications;
    }

    /**
     * Extract a field using regex
     */
    private String extractField(String text, String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    /**
     * Validate image file
     */
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/jpg")
        );
    }
}
