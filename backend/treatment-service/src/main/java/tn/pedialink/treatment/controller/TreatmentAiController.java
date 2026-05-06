package tn.pedialink.treatment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.treatment.dto.ApiResponse;
import tn.pedialink.treatment.service.TreatmentTranslationService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/treatments/ai")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TreatmentAiController {

    private final TreatmentTranslationService translationService;

    @PostMapping("/translate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> translateTreatment(
            @RequestBody Map<String, Object> request
    ) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> treatment = (Map<String, Object>) request.get("treatment");
            String targetLanguage = (String) request.get("targetLanguage");

            log.info("Translating treatment to language: {}", targetLanguage);

            Map<String, Object> translatedTreatment = translationService.translateTreatment(treatment, targetLanguage);

            return ResponseEntity.ok(ApiResponse.ok(translatedTreatment));

        } catch (Exception e) {
            log.error("Error translating treatment: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Error translating treatment: " + e.getMessage()));
        }
    }

    @GetMapping("/translate/languages")
    public ResponseEntity<ApiResponse<String[]>> getSupportedLanguages() {
        String[] languages = {"fr", "es", "de", "it", "ar", "en"};
        return ResponseEntity.ok(ApiResponse.ok(languages));
    }
}
