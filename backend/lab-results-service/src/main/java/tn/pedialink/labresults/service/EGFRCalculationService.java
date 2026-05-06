package tn.pedialink.labresults.service;

import org.springframework.stereotype.Service;
import tn.pedialink.labresults.entity.CKDStage;

/**
 * Service pour calculer le DFG estimé (eGFR) et classifier les stades de maladie rénale chronique
 */
@Service
public class EGFRCalculationService {
    
    /**
     * Calcule l'eGFR en utilisant la formule de Schwartz pour enfants
     * eGFR (mL/min/1.73m²) = (k × Taille en cm) / Créatinine sérique (mg/dL)
     * 
     * @param creatinine Créatinine sérique en mg/dL
     * @param heightCm Taille de l'enfant en cm
     * @param isMale Sexe de l'enfant (true = garçon, false = fille)
     * @return eGFR calculé
     */
    public Double calculateEGFR(Double creatinine, Double heightCm, Boolean isMale) {
        if (creatinine == null || creatinine <= 0 || heightCm == null || heightCm <= 0) {
            return null;
        }
        
        // Constante k pour la formule de Schwartz
        // k = 0.413 pour enfants (standard)
        // Peut être ajusté selon l'âge et le sexe
        double k = 0.413;
        
        // Calcul: eGFR = (k × Taille) / Créatinine
        double eGFR = (k * heightCm) / creatinine;
        
        // Arrondir à 2 décimales
        return Math.round(eGFR * 100.0) / 100.0;
    }
    
    /**
     * Détermine le stade de maladie rénale chronique (CKD) basé sur l'eGFR
     * 
     * @param eGFR Débit de filtration glomérulaire estimé
     * @return Stade CKD
     */
    public CKDStage determineCKDStage(Double eGFR) {
        if (eGFR == null) {
            return CKDStage.UNKNOWN;
        }
        
        if (eGFR >= 90) {
            return CKDStage.STAGE_1;  // Normal ou élevé
        } else if (eGFR >= 60) {
            return CKDStage.STAGE_2;  // Légèrement diminué
        } else if (eGFR >= 45) {
            return CKDStage.STAGE_3A; // Modérément diminué
        } else if (eGFR >= 30) {
            return CKDStage.STAGE_3B; // Modérément à sévèrement diminué
        } else if (eGFR >= 15) {
            return CKDStage.STAGE_4;  // Sévèrement diminué
        } else {
            return CKDStage.STAGE_5;  // Insuffisance rénale terminale
        }
    }
    
    /**
     * Calcule le ratio protéine/créatinine urinaire
     * 
     * @param urineProtein Protéine urinaire (mg/L)
     * @param urineCreatinine Créatinine urinaire (mg/dL)
     * @return Ratio protéine/créatinine
     */
    public Double calculateProteinCreatinineRatio(Double urineProtein, Double urineCreatinine) {
        if (urineProtein == null || urineCreatinine == null || urineCreatinine <= 0) {
            return null;
        }
        
        double ratio = urineProtein / urineCreatinine;
        return Math.round(ratio * 100.0) / 100.0;
    }
}
