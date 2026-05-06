package tn.pedialink.labresults.service;

import org.springframework.stereotype.Service;
import tn.pedialink.labresults.entity.CKDStage;
import tn.pedialink.labresults.entity.LabResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Service pour générer des alertes basées sur les résultats de laboratoire
 */
@Service
public class AlertService {
    
    public List<String> generateAlerts(LabResult result) {
        List<String> alerts = new ArrayList<>();
        
        // Alertes pour les résultats sanguins
        if (result.getPotassium() != null) {
            if (result.getPotassium() > 6.0) {
                alerts.add("URGENT: Potassium dangerously high (>6.0 mmol/L) - Risk of cardiac arrhythmia");
            } else if (result.getPotassium() > 5.5) {
                alerts.add("WARNING: Potassium elevated (>5.5 mmol/L)");
            } else if (result.getPotassium() < 3.5) {
                alerts.add("WARNING: Potassium low (<3.5 mmol/L)");
            }
        }
        
        if (result.getSodium() != null) {
            if (result.getSodium() > 145) {
                alerts.add("WARNING: Sodium elevated (>145 mmol/L)");
            } else if (result.getSodium() < 135) {
                alerts.add("WARNING: Sodium low (<135 mmol/L)");
            }
        }
        
        if (result.getHemoglobin() != null) {
            if (result.getHemoglobin() < 7.0) {
                alerts.add("URGENT: Severe anemia (Hb <7.0 g/dL) - Consider transfusion");
            } else if (result.getHemoglobin() < 10.0) {
                alerts.add("WARNING: Anemia detected (Hb <10.0 g/dL)");
            }
        }
        
        if (result.getCalcium() != null) {
            if (result.getCalcium() > 10.5) {
                alerts.add("WARNING: Calcium elevated (>10.5 mg/dL)");
            } else if (result.getCalcium() < 8.5) {
                alerts.add("WARNING: Calcium low (<8.5 mg/dL)");
            }
        }
        
        if (result.getPhosphorus() != null) {
            if (result.getPhosphorus() > 5.5) {
                alerts.add("WARNING: Phosphorus elevated (>5.5 mg/dL)");
            }
        }
        
        if (result.getBicarbonate() != null) {
            if (result.getBicarbonate() < 22) {
                alerts.add("WARNING: Metabolic acidosis (Bicarbonate <22 mmol/L)");
            }
        }
        
        // Alertes pour l'eGFR et stade CKD
        if (result.getEGFR() != null) {
            if (result.getEGFR() < 15) {
                alerts.add("CRITICAL: Kidney failure (eGFR <15) - Stage 5 CKD - Dialysis may be needed");
            } else if (result.getEGFR() < 30) {
                alerts.add("SEVERE: Advanced kidney disease (eGFR <30) - Stage 4 CKD");
            } else if (result.getEGFR() < 60) {
                alerts.add("MODERATE: Reduced kidney function (eGFR <60) - Stage 3 CKD");
            }
        }
        
        // Alertes pour protéinurie
        if (result.getUrineProtein() != null) {
            if (result.getUrineProtein() > 3.0) {
                alerts.add("SEVERE: Heavy proteinuria (>3.0 g/24h) - Nephrotic range");
            } else if (result.getUrineProtein() > 0.5) {
                alerts.add("WARNING: Significant proteinuria (>0.5 g/24h)");
            }
        }
        
        return alerts;
    }
    
    public boolean hasAbnormalValues(LabResult result) {
        return !generateAlerts(result).isEmpty();
    }
}
