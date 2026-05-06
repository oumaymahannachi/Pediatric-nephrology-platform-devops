package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.dto.GFRCalculationDto;
import tn.pedialink.dossiermedical.model.kidney.CKDStage;
import tn.pedialink.dossiermedical.model.kidney.GFRCalculation;
import tn.pedialink.dossiermedical.repository.GFRCalculationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GFRCalculationService {
    
    private final GFRCalculationRepository gfrRepository;
    
    // Constante de Schwartz pour enfants
    private static final double SCHWARTZ_CONSTANT = 0.413;
    
    /**
     * Calcule le DFG selon la formule de Schwartz pédiatrique
     * DFG = (0.413 × taille en cm) / créatinine sérique
     */
    public GFRCalculation calculateGFR(GFRCalculationDto dto) {
        GFRCalculation calculation = new GFRCalculation();
        calculation.setPatientId(dto.getPatientId());
        calculation.setPatientName(dto.getPatientName());
        calculation.setHeightCm(dto.getHeightCm());
        calculation.setSerumCreatinine(dto.getSerumCreatinine());
        calculation.setMedecinId(dto.getMedecinId());
        calculation.setNotes(dto.getNotes());
        calculation.setCalculationDate(LocalDateTime.now());
        
        // Calcul du DFG
        double gfrValue = (SCHWARTZ_CONSTANT * dto.getHeightCm()) / dto.getSerumCreatinine();
        calculation.setGfrValue(Math.round(gfrValue * 100.0) / 100.0); // Arrondi à 2 décimales
        
        // Détermination du stade CKD
        CKDStage stage = CKDStage.fromGFR(gfrValue);
        calculation.setCkdStage(stage);
        
        // Génération de l'interprétation
        calculation.setInterpretation(generateInterpretation(gfrValue, stage));
        
        return gfrRepository.save(calculation);
    }
    
    private String generateInterpretation(double gfrValue, CKDStage stage) {
        StringBuilder interpretation = new StringBuilder();
        interpretation.append(String.format("GFR: %.2f mL/min/1.73m² - ", gfrValue));
        interpretation.append(stage.getName()).append(" - ");
        interpretation.append(stage.getDescription());
        
        if (stage == CKDStage.STAGE_5) {
            interpretation.append(". URGENT: Kidney failure - Immediate nephrology consultation required.");
        } else if (stage == CKDStage.STAGE_4) {
            interpretation.append(". Severe CKD - Close monitoring and treatment adjustment needed.");
        } else if (stage == CKDStage.STAGE_3A || stage == CKDStage.STAGE_3B) {
            interpretation.append(". Moderate CKD - Regular follow-up recommended.");
        } else if (stage == CKDStage.STAGE_2) {
            interpretation.append(". Mild CKD - Monitor kidney function regularly.");
        } else {
            interpretation.append(". Normal kidney function - Continue routine monitoring.");
        }
        
        return interpretation.toString();
    }
    
    public List<GFRCalculation> getPatientGFRHistory(String patientId) {
        return gfrRepository.findByPatientIdOrderByCalculationDateDesc(patientId);
    }
    
    public GFRCalculation getLatestGFR(String patientId) {
        List<GFRCalculation> history = getPatientGFRHistory(patientId);
        return history.isEmpty() ? null : history.get(0);
    }
    
    public List<GFRCalculation> getDoctorGFRCalculations(String medecinId) {
        return gfrRepository.findByMedecinId(medecinId);
    }
}
