package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class OCRService {
    
    private final Tesseract tesseract;
    
    public OCRService() {
        this.tesseract = new Tesseract();
        try {
            // Configuration Tesseract
            // Try different possible paths
            String[] possiblePaths = {
                "/usr/share/tesseract-ocr/4.00/tessdata",  // Linux
                "/usr/share/tesseract-ocr/5.00/tessdata",  // Linux newer
                "/usr/share/tessdata",                      // Linux alternative
                "C:/Program Files/Tesseract-OCR/tessdata", // Windows
                "C:/Program Files (x86)/Tesseract-OCR/tessdata" // Windows 32-bit
            };
            
            String dataPath = null;
            for (String path : possiblePaths) {
                java.io.File dir = new java.io.File(path);
                if (dir.exists() && dir.isDirectory()) {
                    dataPath = path;
                    break;
                }
            }
            
            if (dataPath != null) {
                tesseract.setDatapath(dataPath);
                log.info("Tesseract data path set to: {}", dataPath);
            } else {
                log.warn("Tesseract data path not found. OCR may not work correctly.");
                log.warn("Please install Tesseract OCR and ensure tessdata directory exists.");
            }
            
            tesseract.setLanguage("eng+fra"); // Anglais et Français
            tesseract.setPageSegMode(1); // Automatic page segmentation with OSD
            tesseract.setOcrEngineMode(1); // Neural nets LSTM engine only
        } catch (Exception e) {
            log.error("Error initializing Tesseract: {}", e.getMessage());
            log.error("OCR functionality will not be available. Please install Tesseract OCR.");
        }
    }

    /**
     * Extrait le texte d'une image
     */
    public String extractTextFromImage(MultipartFile file) throws IOException, TesseractException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        return tesseract.doOCR(image);
    }

    /**
     * Extrait le texte d'un PDF
     */
    public String extractTextFromPDF(MultipartFile file) throws IOException, TesseractException {
        log.info("Starting PDF OCR extraction for file: {}", file.getOriginalFilename());
        StringBuilder extractedText = new StringBuilder();
        
        // Créer un fichier temporaire
        java.io.File tempFile = java.io.File.createTempFile("ocr_pdf_", ".pdf");
        try {
            log.info("Created temp file: {}", tempFile.getAbsolutePath());
            file.transferTo(tempFile);
            log.info("File transferred to temp location, size: {} bytes", tempFile.length());
            
            try (PDDocument document = Loader.loadPDF(tempFile)) {
                int pageCount = document.getNumberOfPages();
                log.info("PDF has {} pages", pageCount);
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                
                for (int page = 0; page < pageCount; page++) {
                    log.info("Processing page {}/{}", page + 1, pageCount);
                    BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300); // 300 DPI pour meilleure qualité
                    log.info("Rendered page {} to image: {}x{}", page + 1, image.getWidth(), image.getHeight());
                    String pageText = tesseract.doOCR(image);
                    log.info("Extracted {} characters from page {}", pageText.length(), page + 1);
                    extractedText.append(pageText).append("\n\n");
                }
            }
            log.info("PDF OCR extraction completed successfully");
        } catch (Exception e) {
            log.error("Error during PDF OCR extraction: {}", e.getMessage(), e);
            throw e;
        } finally {
            // Supprimer le fichier temporaire
            if (tempFile.exists()) {
                boolean deleted = tempFile.delete();
                log.info("Temp file deleted: {}", deleted);
            }
        }
        
        return extractedText.toString();
    }

    /**
     * Extrait le texte depuis base64
     */
    public String extractTextFromBase64(String base64Data) throws IOException, TesseractException {
        // Enlever le préfixe data:image/...;base64,
        String base64Image = base64Data;
        if (base64Data.contains(",")) {
            base64Image = base64Data.split(",")[1];
        }
        
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        
        return tesseract.doOCR(image);
    }

    /**
     * Analyse les résultats de laboratoire depuis le texte OCR
     */
    public LabResultOCR parseLabResults(String ocrText) {
        LabResultOCR result = new LabResultOCR();
        result.setRawText(ocrText);
        result.setExtractedValues(new HashMap<>());
        
        // Patterns pour détecter les valeurs de laboratoire
        Map<String, Pattern> patterns = new HashMap<>();
        patterns.put("hemoglobin", Pattern.compile("h[ée]moglobine?\\s*:?\\s*([0-9.,]+)\\s*(g/dl|g/l)?", Pattern.CASE_INSENSITIVE));
        patterns.put("glucose", Pattern.compile("glucose?\\s*:?\\s*([0-9.,]+)\\s*(mg/dl|mmol/l)?", Pattern.CASE_INSENSITIVE));
        patterns.put("creatinine", Pattern.compile("cr[ée]atinine?\\s*:?\\s*([0-9.,]+)\\s*(mg/dl|µmol/l)?", Pattern.CASE_INSENSITIVE));
        patterns.put("urea", Pattern.compile("ur[ée]e?\\s*:?\\s*([0-9.,]+)\\s*(mg/dl|mmol/l)?", Pattern.CASE_INSENSITIVE));
        patterns.put("sodium", Pattern.compile("sodium\\s*:?\\s*([0-9.,]+)\\s*(mmol/l|meq/l)?", Pattern.CASE_INSENSITIVE));
        patterns.put("potassium", Pattern.compile("potassium\\s*:?\\s*([0-9.,]+)\\s*(mmol/l|meq/l)?", Pattern.CASE_INSENSITIVE));
        patterns.put("wbc", Pattern.compile("(wbc|globules?\\s*blancs?)\\s*:?\\s*([0-9.,]+)\\s*(/mm3|x10\\^9/l)?", Pattern.CASE_INSENSITIVE));
        patterns.put("rbc", Pattern.compile("(rbc|globules?\\s*rouges?)\\s*:?\\s*([0-9.,]+)\\s*(/mm3|x10\\^12/l)?", Pattern.CASE_INSENSITIVE));
        patterns.put("platelets", Pattern.compile("plaquettes?\\s*:?\\s*([0-9.,]+)\\s*(/mm3|x10\\^9/l)?", Pattern.CASE_INSENSITIVE));
        
        // Extraire les valeurs
        for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {
            Matcher matcher = entry.getValue().matcher(ocrText);
            if (matcher.find()) {
                String value = matcher.group(1);
                String unit = matcher.groupCount() > 1 ? matcher.group(2) : "";
                result.getExtractedValues().put(entry.getKey(), value + (unit != null ? " " + unit : ""));
            }
        }
        
        // Détecter la date du test
        Pattern datePattern = Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})");
        Matcher dateMatcher = datePattern.matcher(ocrText);
        if (dateMatcher.find()) {
            result.setTestDate(dateMatcher.group(1));
        }
        
        // Détecter le nom du laboratoire
        Pattern labPattern = Pattern.compile("laboratoire\\s+([\\w\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher labMatcher = labPattern.matcher(ocrText);
        if (labMatcher.find()) {
            result.setLaboratoryName(labMatcher.group(1).trim());
        }
        
        // Calculer le score de confiance
        result.setConfidenceScore(calculateConfidence(result));
        
        return result;
    }

    /**
     * Analyse les résultats d'imagerie médicale
     */
    public ImagingResultOCR parseImagingResults(String ocrText) {
        ImagingResultOCR result = new ImagingResultOCR();
        result.setRawText(ocrText);
        
        // Détecter le type d'imagerie
        if (ocrText.toLowerCase().contains("radiographie") || ocrText.toLowerCase().contains("x-ray")) {
            result.setImagingType("XRAY");
        } else if (ocrText.toLowerCase().contains("échographie") || ocrText.toLowerCase().contains("ultrasound")) {
            result.setImagingType("ULTRASOUND");
        } else if (ocrText.toLowerCase().contains("scanner") || ocrText.toLowerCase().contains("ct scan")) {
            result.setImagingType("CT_SCAN");
        } else if (ocrText.toLowerCase().contains("irm") || ocrText.toLowerCase().contains("mri")) {
            result.setImagingType("MRI");
        }
        
        // Extraire les sections
        result.setFindings(extractSection(ocrText, "findings|constatations|résultats"));
        result.setImpression(extractSection(ocrText, "impression|conclusion"));
        result.setRecommendation(extractSection(ocrText, "recommendation|recommandation"));
        
        // Détecter la date
        Pattern datePattern = Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})");
        Matcher dateMatcher = datePattern.matcher(ocrText);
        if (dateMatcher.find()) {
            result.setImagingDate(dateMatcher.group(1));
        }
        
        // Détecter le radiologue
        Pattern radioPattern = Pattern.compile("(dr|docteur)\\.?\\s+([\\w\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher radioMatcher = radioPattern.matcher(ocrText);
        if (radioMatcher.find()) {
            result.setRadiologistName(radioMatcher.group(2).trim());
        }
        
        result.setConfidenceScore(calculateConfidence(result));
        
        return result;
    }

    /**
     * Extrait une section spécifique du texte
     */
    private String extractSection(String text, String sectionKeywords) {
        Pattern pattern = Pattern.compile("(" + sectionKeywords + ")\\s*:?\\s*([^\\n]+(?:\\n(?!\\w+:)[^\\n]+)*)", 
                                        Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(2).trim();
        }
        return "";
    }

    /**
     * Calcule le score de confiance de l'extraction
     */
    private double calculateConfidence(LabResultOCR result) {
        int totalFields = 10; // Nombre de champs possibles
        int extractedFields = result.getExtractedValues().size();
        
        double baseScore = (double) extractedFields / totalFields;
        
        // Bonus si date et laboratoire détectés
        if (result.getTestDate() != null && !result.getTestDate().isEmpty()) {
            baseScore += 0.1;
        }
        if (result.getLaboratoryName() != null && !result.getLaboratoryName().isEmpty()) {
            baseScore += 0.1;
        }
        
        return Math.min(baseScore, 1.0);
    }

    private double calculateConfidence(ImagingResultOCR result) {
        double score = 0.0;
        
        if (result.getImagingType() != null && !result.getImagingType().isEmpty()) score += 0.2;
        if (result.getFindings() != null && !result.getFindings().isEmpty()) score += 0.3;
        if (result.getImpression() != null && !result.getImpression().isEmpty()) score += 0.2;
        if (result.getImagingDate() != null && !result.getImagingDate().isEmpty()) score += 0.15;
        if (result.getRadiologistName() != null && !result.getRadiologistName().isEmpty()) score += 0.15;
        
        return score;
    }

    // Classes de résultat OCR
    
    public static class LabResultOCR {
        private String rawText;
        private Map<String, String> extractedValues;
        private String testDate;
        private String laboratoryName;
        private double confidenceScore;

        public String getRawText() { return rawText; }
        public void setRawText(String rawText) { this.rawText = rawText; }
        public Map<String, String> getExtractedValues() { return extractedValues; }
        public void setExtractedValues(Map<String, String> extractedValues) { this.extractedValues = extractedValues; }
        public String getTestDate() { return testDate; }
        public void setTestDate(String testDate) { this.testDate = testDate; }
        public String getLaboratoryName() { return laboratoryName; }
        public void setLaboratoryName(String laboratoryName) { this.laboratoryName = laboratoryName; }
        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    }

    public static class ImagingResultOCR {
        private String rawText;
        private String imagingType;
        private String findings;
        private String impression;
        private String recommendation;
        private String imagingDate;
        private String radiologistName;
        private double confidenceScore;

        public String getRawText() { return rawText; }
        public void setRawText(String rawText) { this.rawText = rawText; }
        public String getImagingType() { return imagingType; }
        public void setImagingType(String imagingType) { this.imagingType = imagingType; }
        public String getFindings() { return findings; }
        public void setFindings(String findings) { this.findings = findings; }
        public String getImpression() { return impression; }
        public void setImpression(String impression) { this.impression = impression; }
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
        public String getImagingDate() { return imagingDate; }
        public void setImagingDate(String imagingDate) { this.imagingDate = imagingDate; }
        public String getRadiologistName() { return radiologistName; }
        public void setRadiologistName(String radiologistName) { this.radiologistName = radiologistName; }
        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    }
}
