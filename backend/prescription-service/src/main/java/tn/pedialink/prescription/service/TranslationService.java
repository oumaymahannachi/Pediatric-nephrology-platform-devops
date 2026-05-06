package tn.pedialink.prescription.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TranslationService {

    @Value("${translation.provider:simulated}")
    private String translationProvider;

    @Value("${translation.libretranslate.url:https://libretranslate.com/translate}")
    private String libreTranslateUrl;

    @Value("${translation.mymemory.url:https://api.mymemory.translated.net/get}")
    private String myMemoryUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    /**
     * Translate text to target language
     * Supports multiple providers: libretranslate, mymemory, simulated
     */
    public String translate(String text, String targetLanguage) {
        log.info("Translating text to: {} using provider: {}", targetLanguage, translationProvider);
        
        // Handle null or empty text
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        try {
            switch (translationProvider.toLowerCase()) {
                case "libretranslate":
                    return translateWithLibreTranslate(text, targetLanguage);
                case "mymemory":
                    return translateWithMyMemory(text, targetLanguage);
                default:
                    return simulateTranslation(text, targetLanguage);
            }
        } catch (Exception e) {
            log.error("Error translating with {}, falling back to simulation", translationProvider, e);
            return simulateTranslation(text, targetLanguage);
        }
    }

    /**
     * Translate using LibreTranslate API (FREE and Open Source)
     */
    private String translateWithLibreTranslate(String text, String targetLanguage) throws Exception {
        log.info("Using LibreTranslate API");
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("q", text);
        requestBody.addProperty("source", "fr");
        requestBody.addProperty("target", targetLanguage);
        requestBody.addProperty("format", "text");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(libreTranslateUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            return jsonResponse.get("translatedText").getAsString();
        } else {
            log.error("LibreTranslate API error: {} - {}", response.statusCode(), response.body());
            throw new RuntimeException("Translation API error: " + response.statusCode());
        }
    }

    /**
     * Translate using MyMemory API (FREE with limits)
     */
    private String translateWithMyMemory(String text, String targetLanguage) throws Exception {
        log.info("Using MyMemory API");
        
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String url = String.format("%s?q=%s&langpair=fr|%s", myMemoryUrl, encodedText, targetLanguage);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            JsonObject responseData = jsonResponse.getAsJsonObject("responseData");
            return responseData.get("translatedText").getAsString();
        } else {
            log.error("MyMemory API error: {} - {}", response.statusCode(), response.body());
            throw new RuntimeException("Translation API error: " + response.statusCode());
        }
    }

    /**
     * Translate prescription instructions to multiple languages
     */
    public Map<String, String> translateToMultipleLanguages(String text) {
        Map<String, String> translations = new HashMap<>();
        
        translations.put("fr", text); // Original French
        translations.put("ar", translate(text, "ar")); // Arabic
        translations.put("en", translate(text, "en")); // English
        
        return translations;
    }

    /**
     * Simulate translation (replace with actual API call in production)
     */
    private String simulateTranslation(String text, String targetLanguage) {
        // This is a placeholder. In production, you would:
        // 1. Call Google Translate API or DeepL API
        // 2. Return the translated text
        
        log.info("Simulating translation to: {}", targetLanguage);
        
        // Handle null or empty text
        if (text == null || text.trim().isEmpty()) {
            return text != null ? text : "";
        }
        
        // If target language is French, return original text
        if (targetLanguage == null || targetLanguage.equals("fr")) {
            return text;
        }
        
        // Enhanced medical dictionary with more terms
        Map<String, Map<String, String>> translations = new HashMap<>();
        
        // French to Arabic - Medical terms
        Map<String, String> frToAr = new HashMap<>();
        // Diseases
        frToAr.put("Angine", "التهاب اللوزتين");
        frToAr.put("angine", "التهاب اللوزتين");
        frToAr.put("bactérienne", "بكتيري");
        frToAr.put("virale", "فيروسي");
        frToAr.put("infection", "عدوى");
        frToAr.put("fièvre", "حمى");
        frToAr.put("toux", "سعال");
        frToAr.put("douleur", "ألم");
        
        // Medications
        frToAr.put("Amoxicilline", "أموكسيسيلين");
        frToAr.put("AMOXICILLINE", "أموكسيسيلين");
        frToAr.put("Paracétamol", "باراسيتامول");
        frToAr.put("Ibuprofène", "إيبوبروفين");
        
        // Instructions
        frToAr.put("Prendre", "خذ");
        frToAr.put("prendre", "خذ");
        frToAr.put("comprimé", "قرص");
        frToAr.put("comprimés", "أقراص");
        frToAr.put("fois par jour", "مرات في اليوم");
        frToAr.put("avant les repas", "قبل الوجبات");
        frToAr.put("après les repas", "بعد الوجبات");
        frToAr.put("Après les repas", "بعد الوجبات");
        frToAr.put("Avant les repas", "قبل الوجبات");
        frToAr.put("avec de l'eau", "مع الماء");
        frToAr.put("avec de l eau", "مع الماء");
        frToAr.put("avec eau", "مع الماء");
        frToAr.put("Ne pas dépasser", "لا تتجاوز");
        frToAr.put("ne pas dépasser", "لا تتجاوز");
        
        // Time
        frToAr.put("jours", "أيام");
        frToAr.put("jour", "يوم");
        frToAr.put("matin", "صباح");
        frToAr.put("midi", "ظهر");
        frToAr.put("soir", "مساء");
        frToAr.put("nuit", "ليل");
        
        // Numbers
        frToAr.put("1", "١");
        frToAr.put("2", "٢");
        frToAr.put("3", "٣");
        frToAr.put("4", "٤");
        frToAr.put("5", "٥");
        frToAr.put("6", "٦");
        frToAr.put("7", "٧");
        frToAr.put("8", "٨");
        frToAr.put("9", "٩");
        
        // French to English - Medical terms
        Map<String, String> frToEn = new HashMap<>();
        // Diseases
        frToEn.put("Angine", "Tonsillitis");
        frToEn.put("angine", "tonsillitis");
        frToEn.put("bactérienne", "bacterial");
        frToEn.put("virale", "viral");
        frToEn.put("infection", "infection");
        frToEn.put("fièvre", "fever");
        frToEn.put("toux", "cough");
        frToEn.put("douleur", "pain");
        
        // Medications
        frToEn.put("Amoxicilline", "Amoxicillin");
        frToEn.put("AMOXICILLINE", "Amoxicillin");
        frToEn.put("Paracétamol", "Paracetamol");
        frToEn.put("Ibuprofène", "Ibuprofen");
        
        // Instructions
        frToEn.put("Prendre", "Take");
        frToEn.put("prendre", "take");
        frToEn.put("comprimé", "tablet");
        frToEn.put("comprimés", "tablets");
        frToEn.put("fois par jour", "times per day");
        frToEn.put("avant les repas", "before meals");
        frToEn.put("après les repas", "after meals");
        frToEn.put("Après les repas", "After meals");
        frToEn.put("Avant les repas", "Before meals");
        frToEn.put("avec de l'eau", "with water");
        frToEn.put("avec de l eau", "with water");
        frToEn.put("avec eau", "with water");
        frToEn.put("Ne pas dépasser", "Do not exceed");
        frToEn.put("ne pas dépasser", "do not exceed");
        
        // Time
        frToEn.put("jours", "days");
        frToEn.put("jour", "day");
        frToEn.put("matin", "morning");
        frToEn.put("midi", "noon");
        frToEn.put("soir", "evening");
        frToEn.put("nuit", "night");
        
        translations.put("ar", frToAr);
        translations.put("en", frToEn);
        
        String translatedText = text;
        Map<String, String> targetTranslations = translations.get(targetLanguage);
        
        if (targetTranslations != null) {
            // Sort by length (longest first) to avoid partial replacements
            List<Map.Entry<String, String>> sortedEntries = new ArrayList<>(targetTranslations.entrySet());
            sortedEntries.sort((a, b) -> b.getKey().length() - a.getKey().length());
            
            for (Map.Entry<String, String> entry : sortedEntries) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    translatedText = translatedText.replace(entry.getKey(), entry.getValue());
                }
            }
        }
        
        // If no translation was applied, return original text without prefix
        // (API will handle it better than showing [AR] or [EN])
        return translatedText;
    }

    /**
     * Get supported languages
     */
    public Map<String, String> getSupportedLanguages() {
        Map<String, String> languages = new HashMap<>();
        languages.put("fr", "Français");
        languages.put("ar", "العربية");
        languages.put("en", "English");
        return languages;
    }

    /**
     * Translate prescription object
     */
    public Map<String, Object> translatePrescription(Map<String, Object> prescription, String targetLanguage) {
        Map<String, Object> translated = new HashMap<>(prescription);
        
        // Translate diagnostic
        if (prescription.containsKey("diagnostic")) {
            translated.put("diagnostic", translate((String) prescription.get("diagnostic"), targetLanguage));
        }
        
        // Translate notes
        if (prescription.containsKey("notes")) {
            translated.put("notes", translate((String) prescription.get("notes"), targetLanguage));
        }
        
        // Translate medication instructions
        if (prescription.containsKey("medicaments")) {
            // Handle medication translation
            translated.put("medicaments", prescription.get("medicaments"));
        }
        
        return translated;
    }
}
