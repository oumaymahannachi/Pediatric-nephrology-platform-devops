package tn.pedialink.treatment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class TreatmentTranslationService {

    @Value("${translation.provider:simulated}")
    private String translationProvider;

    @Value("${translation.libretranslate.url:https://libretranslate.com/translate}")
    private String libreTranslateUrl;

    private final RestTemplate restTemplate;

    public TreatmentTranslationService() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> translateTreatment(Map<String, Object> treatment, String targetLanguage) {
        Map<String, Object> translatedTreatment = new HashMap<>(treatment);

        try {
            // Translate diagnostic
            if (treatment.containsKey("diagnostic")) {
                String diagnostic = (String) treatment.get("diagnostic");
                translatedTreatment.put("diagnostic", translateText(diagnostic, targetLanguage));
            }

            // Translate objectifTraitement
            if (treatment.containsKey("objectifTraitement")) {
                String objectif = (String) treatment.get("objectifTraitement");
                translatedTreatment.put("objectifTraitement", translateText(objectif, targetLanguage));
            }

            // Translate notes
            if (treatment.containsKey("notes")) {
                String notes = (String) treatment.get("notes");
                translatedTreatment.put("notes", translateText(notes, targetLanguage));
            }

            // Translate medications
            if (treatment.containsKey("medicaments")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> medicaments = (List<Map<String, Object>>) treatment.get("medicaments");
                List<Map<String, Object>> translatedMedicaments = new ArrayList<>();

                for (Map<String, Object> med : medicaments) {
                    Map<String, Object> translatedMed = new HashMap<>(med);

                    // Translate medication fields
                    if (med.containsKey("formePharmaceutique")) {
                        translatedMed.put("formePharmaceutique", 
                            translateText((String) med.get("formePharmaceutique"), targetLanguage));
                    }

                    if (med.containsKey("instructionsSpeciales")) {
                        translatedMed.put("instructionsSpeciales", 
                            translateText((String) med.get("instructionsSpeciales"), targetLanguage));
                    }

                    // Translate posologie
                    if (med.containsKey("posologie")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> posologie = (Map<String, Object>) med.get("posologie");
                        Map<String, Object> translatedPosologie = new HashMap<>(posologie);

                        if (posologie.containsKey("frequence")) {
                            translatedPosologie.put("frequence", 
                                translateText((String) posologie.get("frequence"), targetLanguage));
                        }

                        if (posologie.containsKey("momentPrise")) {
                            translatedPosologie.put("momentPrise", 
                                translateText((String) posologie.get("momentPrise"), targetLanguage));
                        }

                        translatedMed.put("posologie", translatedPosologie);
                    }

                    translatedMedicaments.add(translatedMed);
                }

                translatedTreatment.put("medicaments", translatedMedicaments);
            }

            // Translate recommendations
            if (treatment.containsKey("recommandations")) {
                @SuppressWarnings("unchecked")
                List<String> recommandations = (List<String>) treatment.get("recommandations");
                List<String> translatedRecommandations = new ArrayList<>();

                for (String rec : recommandations) {
                    translatedRecommandations.add(translateText(rec, targetLanguage));
                }

                translatedTreatment.put("recommandations", translatedRecommandations);
            }

            log.info("Treatment translated successfully to {}", targetLanguage);

        } catch (Exception e) {
            log.error("Error translating treatment: {}", e.getMessage(), e);
            // Return original treatment if translation fails
            return treatment;
        }

        return translatedTreatment;
    }

    private String translateText(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            if ("libretranslate".equals(translationProvider)) {
                return translateWithLibreTranslate(text, targetLanguage);
            } else {
                return simulateTranslation(text, targetLanguage);
            }
        } catch (Exception e) {
            log.warn("Translation failed, returning original text: {}", e.getMessage());
            return text;
        }
    }

    private String translateWithLibreTranslate(String text, String targetLanguage) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("q", text);
            requestBody.put("source", "en");
            requestBody.put("target", targetLanguage);
            requestBody.put("format", "text");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                libreTranslateUrl,
                entity,
                Map.class
            );

            if (response != null && response.containsKey("translatedText")) {
                return (String) response.get("translatedText");
            }

            return text;

        } catch (Exception e) {
            log.warn("LibreTranslate API error: {}", e.getMessage());
            return text;
        }
    }

    private String simulateTranslation(String text, String targetLanguage) {
        return "[" + targetLanguage.toUpperCase() + "] " + text;
    }
}
