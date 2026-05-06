package tn.pedialink.prescription.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.pedialink.prescription.model.Prescription;
import tn.pedialink.prescription.model.Prescription.Medicament;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PrescriptionInterpretationService {

    // Base de connaissances médicales simplifiées
    private static final Map<String, String> DIAGNOSIS_EXPLANATIONS = new HashMap<>();
    private static final Map<String, MedicationInfo> MEDICATION_DATABASE = new HashMap<>();
    
    static {
        // Diagnostics courants en néphrologie pédiatrique
        DIAGNOSIS_EXPLANATIONS.put("chronic kidney disease", 
            "The kidneys are not working as well as they should. They help clean the blood and remove waste from the body.");
        DIAGNOSIS_EXPLANATIONS.put("nephrotic syndrome", 
            "The kidneys are letting too much protein leak into the urine. This can cause swelling and other problems.");
        DIAGNOSIS_EXPLANATIONS.put("urinary tract infection", 
            "There is an infection in the urinary system (kidneys, bladder, or tubes). This needs to be treated with medicine.");
        DIAGNOSIS_EXPLANATIONS.put("hypertension", 
            "The blood pressure is higher than normal. This means the heart is working harder than it should.");
        DIAGNOSIS_EXPLANATIONS.put("proteinuria", 
            "There is too much protein in the urine. The kidneys should keep protein in the blood, not let it leak out.");
        DIAGNOSIS_EXPLANATIONS.put("anemia", 
            "There are not enough red blood cells in the blood. Red blood cells carry oxygen to all parts of the body.");
        DIAGNOSIS_EXPLANATIONS.put("edema", 
            "There is extra fluid building up in the body, causing swelling, usually in the legs, feet, or around the eyes.");
        
        // Médicaments courants
        MEDICATION_DATABASE.put("furosemide", new MedicationInfo(
            "Water Pill (Diuretic)",
            "Helps the body get rid of extra water and salt through urine",
            "Reduces swelling and helps the kidneys work better",
            List.of("Give in the morning to avoid nighttime bathroom trips", 
                   "Your child may need to urinate more often"),
            List.of("Is the swelling getting better?", 
                   "Should we adjust the timing of the dose?")
        ));
        
        MEDICATION_DATABASE.put("enalapril", new MedicationInfo(
            "Blood Pressure Medicine (ACE Inhibitor)",
            "Helps relax blood vessels and lower blood pressure",
            "Protects the kidneys and heart from damage",
            List.of("Give at the same time each day", 
                   "Don't skip doses even if your child feels fine"),
            List.of("How often should we check blood pressure?", 
                   "What blood pressure numbers should we aim for?")
        ));
        
        MEDICATION_DATABASE.put("prednisone", new MedicationInfo(
            "Steroid Medicine",
            "Reduces inflammation and helps the immune system",
            "Helps reduce protein in urine and swelling",
            List.of("Give with food to protect the stomach", 
                   "Never stop suddenly - must taper slowly",
                   "May increase appetite and energy"),
            List.of("How long will my child need this medicine?", 
                   "What side effects should I watch for?",
                   "Can we reduce the dose gradually?")
        ));
        
        MEDICATION_DATABASE.put("erythropoietin", new MedicationInfo(
            "Red Blood Cell Booster",
            "Helps the body make more red blood cells",
            "Treats anemia caused by kidney disease",
            List.of("Usually given as an injection", 
                   "May take several weeks to see improvement"),
            List.of("How often will my child need blood tests?", 
                   "When should we see improvement in energy levels?")
        ));
        
        MEDICATION_DATABASE.put("calcium carbonate", new MedicationInfo(
            "Calcium Supplement",
            "Provides calcium and helps control phosphorus",
            "Keeps bones strong and balances minerals",
            List.of("Give with meals for best absorption", 
                   "Important for growing bones"),
            List.of("Should we also give vitamin D?", 
                   "How much calcium does my child need daily?")
        ));
        
        MEDICATION_DATABASE.put("amoxicillin", new MedicationInfo(
            "Antibiotic",
            "Kills bacteria causing infection",
            "Treats urinary tract infections and prevents complications",
            List.of("Complete the full course even if feeling better", 
                   "Give at evenly spaced times",
                   "Can be taken with or without food"),
            List.of("How long until symptoms improve?", 
                   "What if my child vomits after taking it?")
        ));
    }
    
    public PrescriptionInterpretation interpretPrescription(Prescription prescription) {
        log.info("Generating AI interpretation for prescription");
        
        PrescriptionInterpretation interpretation = new PrescriptionInterpretation();
        
        // 1. Expliquer le diagnostic
        interpretation.setDiagnosisExplanation(explainDiagnosis(prescription.getDiagnostic()));
        
        // 2. Expliquer chaque médicament
        List<MedicationExplanation> medicationExplanations = new ArrayList<>();
        if (prescription.getMedicaments() != null) {
            for (Medicament med : prescription.getMedicaments()) {
                medicationExplanations.add(explainMedication(med));
            }
        }
        interpretation.setMedicationExplanations(medicationExplanations);
        
        // 3. Conseils généraux
        interpretation.setGeneralAdvice(generateGeneralAdvice(prescription));
        
        // 4. Questions suggérées
        interpretation.setSuggestedQuestions(generateSuggestedQuestions(prescription));
        
        // 5. Signes d'alerte
        interpretation.setWarningSignsToWatch(generateWarningSigns(prescription));
        
        log.info("AI interpretation generated successfully");
        return interpretation;
    }
    
    private String explainDiagnosis(String diagnosis) {
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            return "Your doctor has prescribed medication to help with your child's condition.";
        }
        
        String lowerDiagnosis = diagnosis.toLowerCase();
        
        // Chercher une correspondance dans la base de connaissances
        for (Map.Entry<String, String> entry : DIAGNOSIS_EXPLANATIONS.entrySet()) {
            if (lowerDiagnosis.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Explication générique si pas trouvé
        return "Your child has been diagnosed with " + diagnosis + ". Your doctor has prescribed medication to help treat this condition.";
    }
    
    private MedicationExplanation explainMedication(Medicament medication) {
        MedicationExplanation explanation = new MedicationExplanation();
        explanation.setMedicationName(medication.getNomCommercial());
        explanation.setDosage(medication.getDosage());
        explanation.setFrequency(medication.getPosologie() != null ? medication.getPosologie().getFrequence() : "");
        
        String medNameLower = medication.getNomCommercial().toLowerCase();
        
        // Chercher dans la base de données
        MedicationInfo info = null;
        for (Map.Entry<String, MedicationInfo> entry : MEDICATION_DATABASE.entrySet()) {
            if (medNameLower.contains(entry.getKey())) {
                info = entry.getValue();
                break;
            }
        }
        
        if (info != null) {
            explanation.setSimpleName(info.simpleName);
            explanation.setWhatItDoes(info.whatItDoes);
            explanation.setWhyPrescribed(info.whyPrescribed);
            explanation.setPracticalTips(info.practicalTips);
            explanation.setQuestionsToAsk(info.questionsToAsk);
        } else {
            // Explication générique
            explanation.setSimpleName("Medicine");
            explanation.setWhatItDoes("This medication helps treat your child's condition");
            explanation.setWhyPrescribed("Your doctor prescribed this to help your child feel better");
            explanation.setPracticalTips(List.of(
                "Give at the same time each day",
                "Don't skip doses",
                "Contact your doctor if you have concerns"
            ));
            explanation.setQuestionsToAsk(List.of(
                "What should I do if my child misses a dose?",
                "Are there any side effects I should watch for?"
            ));
        }
        
        return explanation;
    }
    
    private List<String> generateGeneralAdvice(Prescription prescription) {
        List<String> advice = new ArrayList<>();
        
        advice.add("💊 Give all medications exactly as prescribed by your doctor");
        advice.add("📅 Set reminders to help remember medication times");
        advice.add("📝 Keep a medication diary to track doses given");
        advice.add("💧 Ensure your child drinks enough water (unless told otherwise)");
        advice.add("🍎 Maintain a healthy, balanced diet");
        advice.add("😴 Make sure your child gets enough rest");
        advice.add("📞 Contact your doctor if you notice any concerning symptoms");
        advice.add("🏥 Keep all follow-up appointments");
        
        return advice;
    }
    
    private List<String> generateSuggestedQuestions(Prescription prescription) {
        List<String> questions = new ArrayList<>();
        
        questions.add("How long will my child need to take these medications?");
        questions.add("What improvements should I expect to see, and when?");
        questions.add("Are there any foods or activities my child should avoid?");
        questions.add("What should I do if my child vomits after taking medication?");
        questions.add("When should I schedule the next follow-up appointment?");
        questions.add("Are there any tests we need to do to monitor progress?");
        questions.add("What symptoms mean I should call you right away?");
        
        return questions;
    }
    
    private List<String> generateWarningSigns(Prescription prescription) {
        List<String> warnings = new ArrayList<>();
        
        warnings.add("⚠️ Severe allergic reaction (rash, difficulty breathing, swelling)");
        warnings.add("⚠️ Persistent vomiting or diarrhea");
        warnings.add("⚠️ Severe stomach pain");
        warnings.add("⚠️ Blood in urine or very dark urine");
        warnings.add("⚠️ Extreme tiredness or weakness");
        warnings.add("⚠️ Fever over 38.5°C (101.3°F)");
        warnings.add("⚠️ Unusual swelling, especially of face or throat");
        warnings.add("⚠️ Confusion or unusual behavior");
        
        return warnings;
    }
    
    // Classes internes pour structurer les données
    public static class PrescriptionInterpretation {
        private String diagnosisExplanation;
        private List<MedicationExplanation> medicationExplanations;
        private List<String> generalAdvice;
        private List<String> suggestedQuestions;
        private List<String> warningSignsToWatch;
        
        // Getters et Setters
        public String getDiagnosisExplanation() { return diagnosisExplanation; }
        public void setDiagnosisExplanation(String diagnosisExplanation) { this.diagnosisExplanation = diagnosisExplanation; }
        
        public List<MedicationExplanation> getMedicationExplanations() { return medicationExplanations; }
        public void setMedicationExplanations(List<MedicationExplanation> medicationExplanations) { 
            this.medicationExplanations = medicationExplanations; 
        }
        
        public List<String> getGeneralAdvice() { return generalAdvice; }
        public void setGeneralAdvice(List<String> generalAdvice) { this.generalAdvice = generalAdvice; }
        
        public List<String> getSuggestedQuestions() { return suggestedQuestions; }
        public void setSuggestedQuestions(List<String> suggestedQuestions) { 
            this.suggestedQuestions = suggestedQuestions; 
        }
        
        public List<String> getWarningSignsToWatch() { return warningSignsToWatch; }
        public void setWarningSignsToWatch(List<String> warningSignsToWatch) { 
            this.warningSignsToWatch = warningSignsToWatch; 
        }
    }
    
    public static class MedicationExplanation {
        private String medicationName;
        private String dosage;
        private String frequency;
        private String simpleName;
        private String whatItDoes;
        private String whyPrescribed;
        private List<String> practicalTips;
        private List<String> questionsToAsk;
        
        // Getters et Setters
        public String getMedicationName() { return medicationName; }
        public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
        
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        
        public String getSimpleName() { return simpleName; }
        public void setSimpleName(String simpleName) { this.simpleName = simpleName; }
        
        public String getWhatItDoes() { return whatItDoes; }
        public void setWhatItDoes(String whatItDoes) { this.whatItDoes = whatItDoes; }
        
        public String getWhyPrescribed() { return whyPrescribed; }
        public void setWhyPrescribed(String whyPrescribed) { this.whyPrescribed = whyPrescribed; }
        
        public List<String> getPracticalTips() { return practicalTips; }
        public void setPracticalTips(List<String> practicalTips) { this.practicalTips = practicalTips; }
        
        public List<String> getQuestionsToAsk() { return questionsToAsk; }
        public void setQuestionsToAsk(List<String> questionsToAsk) { this.questionsToAsk = questionsToAsk; }
    }
    
    private static class MedicationInfo {
        String simpleName;
        String whatItDoes;
        String whyPrescribed;
        List<String> practicalTips;
        List<String> questionsToAsk;
        
        MedicationInfo(String simpleName, String whatItDoes, String whyPrescribed,
                      List<String> practicalTips, List<String> questionsToAsk) {
            this.simpleName = simpleName;
            this.whatItDoes = whatItDoes;
            this.whyPrescribed = whyPrescribed;
            this.practicalTips = practicalTips;
            this.questionsToAsk = questionsToAsk;
        }
    }
}
