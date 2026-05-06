package tn.pedialink.prescription.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.pedialink.prescription.dto.ApiResponse;
import tn.pedialink.prescription.service.OcrService;
import tn.pedialink.prescription.service.TranslationService;
import tn.pedialink.prescription.service.PrescriptionInterpretationService;
import tn.pedialink.prescription.service.PrescriptionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions/ai")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AiController {

    private final OcrService ocrService;
    private final TranslationService translationService;
    private final PrescriptionInterpretationService interpretationService;
    private final PrescriptionService prescriptionService;

    /**
     * Extract prescription data from image using OCR
     */
    @PostMapping(value = "/ocr/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> extractPrescription(
            @RequestParam("image") MultipartFile image) {
        try {
            log.info("Extracting prescription from image: {}", image.getOriginalFilename());
            
            Map<String, Object> extractedData = ocrService.extractPrescriptionFromImage(image);
            
            return ResponseEntity.ok(ApiResponse.ok("Prescription extracted successfully", extractedData));
        } catch (Exception e) {
            log.error("Error extracting prescription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to extract prescription: " + e.getMessage()));
        }
    }

    /**
     * Translate text to target language
     */
    @PostMapping("/translate")
    public ResponseEntity<ApiResponse<Map<String, String>>> translateText(
            @RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String targetLanguage = request.get("targetLanguage");
            
            log.info("Translating text to: {}", targetLanguage);
            
            String translatedText = translationService.translate(text, targetLanguage);
            
            Map<String, String> result = new HashMap<>();
            result.put("originalText", text);
            result.put("translatedText", translatedText);
            result.put("targetLanguage", targetLanguage);
            
            return ResponseEntity.ok(ApiResponse.ok("Translation successful", result));
        } catch (Exception e) {
            log.error("Error translating text", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Translation failed: " + e.getMessage()));
        }
    }

    /**
     * Translate text to multiple languages
     */
    @PostMapping("/translate/multiple")
    public ResponseEntity<ApiResponse<Map<String, String>>> translateToMultiple(
            @RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            
            log.info("Translating text to multiple languages");
            
            Map<String, String> translations = new HashMap<>();
            translations.put("en", translationService.translate(text, "en"));
            translations.put("ar", translationService.translate(text, "ar"));
            translations.put("fr", translationService.translate(text, "fr"));
            
            return ResponseEntity.ok(ApiResponse.ok("Multiple translations successful", translations));
        } catch (Exception e) {
            log.error("Error translating to multiple languages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Multiple translation failed: " + e.getMessage()));
        }
    }

    /**
     * Get supported languages
     */
    @GetMapping("/translate/languages")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSupportedLanguages() {
        Map<String, String> languages = new HashMap<>();
        languages.put("en", "English");
        languages.put("ar", "Arabic");
        languages.put("fr", "French");
        
        return ResponseEntity.ok(ApiResponse.ok("Supported languages retrieved", languages));
    }

    /**
     * Translate entire prescription including medications
     */
    @PostMapping("/translate/prescription")
    public ResponseEntity<ApiResponse<Map<String, Object>>> translatePrescription(
            @RequestBody Map<String, Object> request) {
        try {
            String targetLanguage = (String) request.get("targetLanguage");
            Map<String, Object> prescription = (Map<String, Object>) request.get("prescription");
            
            if (targetLanguage == null || targetLanguage.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Target language is required"));
            }
            
            if (prescription == null || prescription.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Prescription data is required"));
            }
            
            log.info("Translating prescription to: {}", targetLanguage);
            
            Map<String, Object> translatedPrescription = new HashMap<>(prescription);
            
            // Translate diagnostic
            if (prescription.containsKey("diagnostic") && prescription.get("diagnostic") != null) {
                String diagnostic = (String) prescription.get("diagnostic");
                String translatedDiagnostic = translationService.translate(diagnostic, targetLanguage);
                translatedPrescription.put("diagnostic", translatedDiagnostic != null ? translatedDiagnostic : diagnostic);
            }
            
            // Translate notes
            if (prescription.containsKey("notes") && prescription.get("notes") != null) {
                String notes = (String) prescription.get("notes");
                String translatedNotes = translationService.translate(notes, targetLanguage);
                translatedPrescription.put("notes", translatedNotes != null ? translatedNotes : notes);
            }
            
            // Translate medications
            if (prescription.containsKey("medicaments") && prescription.get("medicaments") != null) {
                List<Map<String, Object>> medicaments = (List<Map<String, Object>>) prescription.get("medicaments");
                List<Map<String, Object>> translatedMedicaments = new ArrayList<>();
                
                for (Map<String, Object> med : medicaments) {
                    Map<String, Object> translatedMed = new HashMap<>(med);
                    
                    // Translate medication name
                    if (med.containsKey("nomCommercial") && med.get("nomCommercial") != null) {
                        String name = (String) med.get("nomCommercial");
                        translatedMed.put("nomCommercial", translationService.translate(name, targetLanguage));
                    }
                    
                    // Translate pharmaceutical form
                    if (med.containsKey("formePharmaceutique") && med.get("formePharmaceutique") != null) {
                        String form = (String) med.get("formePharmaceutique");
                        translatedMed.put("formePharmaceutique", translationService.translate(form, targetLanguage));
                    }
                    
                    // Translate special instructions
                    if (med.containsKey("instructionsSpeciales") && med.get("instructionsSpeciales") != null) {
                        String instructions = (String) med.get("instructionsSpeciales");
                        translatedMed.put("instructionsSpeciales", translationService.translate(instructions, targetLanguage));
                    }
                    
                    // Translate posology
                    if (med.containsKey("posologie") && med.get("posologie") != null) {
                        Map<String, Object> posologie = (Map<String, Object>) med.get("posologie");
                        Map<String, Object> translatedPosologie = new HashMap<>(posologie);
                        
                        if (posologie.containsKey("frequence") && posologie.get("frequence") != null) {
                            String freq = (String) posologie.get("frequence");
                            translatedPosologie.put("frequence", translationService.translate(freq, targetLanguage));
                        }
                        
                        if (posologie.containsKey("momentPrise") && posologie.get("momentPrise") != null) {
                            String moment = (String) posologie.get("momentPrise");
                            translatedPosologie.put("momentPrise", translationService.translate(moment, targetLanguage));
                        }
                        
                        if (posologie.containsKey("unite") && posologie.get("unite") != null) {
                            String unite = (String) posologie.get("unite");
                            translatedPosologie.put("unite", translationService.translate(unite, targetLanguage));
                        }
                        
                        translatedMed.put("posologie", translatedPosologie);
                    }
                    
                    translatedMedicaments.add(translatedMed);
                }
                
                translatedPrescription.put("medicaments", translatedMedicaments);
            }
            
            // Add translation metadata
            translatedPrescription.put("translatedTo", targetLanguage);
            translatedPrescription.put("translatedAt", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(ApiResponse.ok("Prescription translated successfully", translatedPrescription));
        } catch (Exception e) {
            log.error("Error translating prescription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Prescription translation failed: " + e.getMessage()));
        }
    }

    /**
     * Get AI interpretation of prescription in simple language for parents
     */
    @PostMapping("/interpret/{prescriptionId}")
    public ResponseEntity<ApiResponse<PrescriptionInterpretationService.PrescriptionInterpretation>> interpretPrescription(
            @PathVariable String prescriptionId,
            @RequestParam(required = false, defaultValue = "en") String language) {
        try {
            log.info("Generating AI interpretation for prescription: {} in language: {}", prescriptionId, language);
            
            // Get prescription from service
            tn.pedialink.prescription.dto.prescription.PrescriptionResponse prescriptionResponse = 
                    prescriptionService.getPrescription(prescriptionId);
            
            // Convert to entity for interpretation
            tn.pedialink.prescription.model.Prescription prescription = convertToPrescriptionEntity(prescriptionResponse);
            
            // Generate interpretation in English first
            PrescriptionInterpretationService.PrescriptionInterpretation interpretation = 
                    interpretationService.interpretPrescription(prescription);
            
            // Translate if language is not English
            if (!language.equals("en")) {
                interpretation = translateInterpretation(interpretation, language);
            }
            
            return ResponseEntity.ok(ApiResponse.ok("Interpretation generated successfully", interpretation));
        } catch (Exception e) {
            log.error("Error generating prescription interpretation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate interpretation: " + e.getMessage()));
        }
    }
    
    /**
     * Translate interpretation to target language
     */
    private PrescriptionInterpretationService.PrescriptionInterpretation translateInterpretation(
            PrescriptionInterpretationService.PrescriptionInterpretation interpretation, String targetLanguage) {
        try {
            // Translate diagnosis explanation
            if (interpretation.getDiagnosisExplanation() != null) {
                String translated = translationService.translate(interpretation.getDiagnosisExplanation(), targetLanguage);
                interpretation.setDiagnosisExplanation(translated != null ? translated : interpretation.getDiagnosisExplanation());
            }
            
            // Translate medication explanations
            if (interpretation.getMedicationExplanations() != null) {
                for (PrescriptionInterpretationService.MedicationExplanation medExplanation : interpretation.getMedicationExplanations()) {
                    if (medExplanation.getSimpleName() != null) {
                        String translated = translationService.translate(medExplanation.getSimpleName(), targetLanguage);
                        medExplanation.setSimpleName(translated != null ? translated : medExplanation.getSimpleName());
                    }
                    if (medExplanation.getWhatItDoes() != null) {
                        String translated = translationService.translate(medExplanation.getWhatItDoes(), targetLanguage);
                        medExplanation.setWhatItDoes(translated != null ? translated : medExplanation.getWhatItDoes());
                    }
                    if (medExplanation.getWhyPrescribed() != null) {
                        String translated = translationService.translate(medExplanation.getWhyPrescribed(), targetLanguage);
                        medExplanation.setWhyPrescribed(translated != null ? translated : medExplanation.getWhyPrescribed());
                    }
                    if (medExplanation.getPracticalTips() != null) {
                        List<String> translatedTips = new ArrayList<>();
                        for (String tip : medExplanation.getPracticalTips()) {
                            String translated = translationService.translate(tip, targetLanguage);
                            translatedTips.add(translated != null ? translated : tip);
                        }
                        medExplanation.setPracticalTips(translatedTips);
                    }
                    if (medExplanation.getQuestionsToAsk() != null) {
                        List<String> translatedQuestions = new ArrayList<>();
                        for (String question : medExplanation.getQuestionsToAsk()) {
                            String translated = translationService.translate(question, targetLanguage);
                            translatedQuestions.add(translated != null ? translated : question);
                        }
                        medExplanation.setQuestionsToAsk(translatedQuestions);
                    }
                }
            }
            
            // Translate general advice
            if (interpretation.getGeneralAdvice() != null) {
                List<String> translatedAdvice = new ArrayList<>();
                for (String advice : interpretation.getGeneralAdvice()) {
                    String translated = translationService.translate(advice, targetLanguage);
                    translatedAdvice.add(translated != null ? translated : advice);
                }
                interpretation.setGeneralAdvice(translatedAdvice);
            }
            
            // Translate suggested questions
            if (interpretation.getSuggestedQuestions() != null) {
                List<String> translatedQuestions = new ArrayList<>();
                for (String question : interpretation.getSuggestedQuestions()) {
                    String translated = translationService.translate(question, targetLanguage);
                    translatedQuestions.add(translated != null ? translated : question);
                }
                interpretation.setSuggestedQuestions(translatedQuestions);
            }
            
            // Translate warning signs
            if (interpretation.getWarningSignsToWatch() != null) {
                List<String> translatedWarnings = new ArrayList<>();
                for (String warning : interpretation.getWarningSignsToWatch()) {
                    String translated = translationService.translate(warning, targetLanguage);
                    translatedWarnings.add(translated != null ? translated : warning);
                }
                interpretation.setWarningSignsToWatch(translatedWarnings);
            }
            
            return interpretation;
        } catch (Exception e) {
            log.error("Error translating interpretation", e);
            // Return original interpretation if translation fails
            return interpretation;
        }
    }
    
    /**
     * Convert PrescriptionResponse to Prescription entity for interpretation
     */
    private tn.pedialink.prescription.model.Prescription convertToPrescriptionEntity(
            tn.pedialink.prescription.dto.prescription.PrescriptionResponse response) {
        
        List<tn.pedialink.prescription.model.Prescription.Medicament> medicaments = 
                response.getMedicaments().stream()
                .map(this::convertToMedicamentEntity)
                .collect(java.util.stream.Collectors.toList());
        
        return tn.pedialink.prescription.model.Prescription.builder()
                .id(response.getId())
                .diagnostic(response.getDiagnostic())
                .medicaments(medicaments)
                .notes(response.getNotes())
                .build();
    }
    
    private tn.pedialink.prescription.model.Prescription.Medicament convertToMedicamentEntity(
            tn.pedialink.prescription.dto.prescription.PrescriptionResponse.MedicamentResponse response) {
        
        tn.pedialink.prescription.model.Prescription.Posologie posologie = null;
        if (response.getPosologie() != null) {
            posologie = tn.pedialink.prescription.model.Prescription.Posologie.builder()
                    .quantite(response.getPosologie().getQuantite())
                    .unite(response.getPosologie().getUnite())
                    .frequence(response.getPosologie().getFrequence())
                    .momentPrise(response.getPosologie().getMomentPrise())
                    .dureeTraitementJours(response.getPosologie().getDureeTraitementJours())
                    .build();
        }
        
        return tn.pedialink.prescription.model.Prescription.Medicament.builder()
                .nomCommercial(response.getNomCommercial())
                .dci(response.getDci())
                .formePharmaceutique(response.getFormePharmaceutique())
                .dosage(response.getDosage())
                .posologie(posologie)
                .instructionsSpeciales(response.getInstructionsSpeciales())
                .build();
    }
}
