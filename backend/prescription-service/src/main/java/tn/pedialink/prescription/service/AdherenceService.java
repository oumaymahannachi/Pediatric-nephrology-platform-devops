package tn.pedialink.prescription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.pedialink.prescription.dto.adherence.AdherenceLogRequest;
import tn.pedialink.prescription.model.AdherenceLog;
import tn.pedialink.prescription.model.Prescription;
import tn.pedialink.prescription.repository.AdherenceLogRepository;
import tn.pedialink.prescription.repository.PrescriptionRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdherenceService {
    
    private final AdherenceLogRepository adherenceLogRepository;
    private final PrescriptionRepository prescriptionRepository;
    
    public AdherenceLog logPriseMedicament(String patientId, AdherenceLogRequest request) {
        Prescription prescription = prescriptionRepository.findById(request.getPrescriptionId())
                .orElseThrow(() -> new RuntimeException("Prescription non trouvée"));
        
        AdherenceLog log = AdherenceLog.builder()
                .prescriptionId(request.getPrescriptionId())
                .patientId(patientId)
                .medicamentNom(request.getMedicamentNom())
                .datePrise(request.getDatePrise())
                .prise(request.getPrise())
                .raison(request.getRaison())
                .notes(request.getNotes())
                .effetsSecondaires(request.getEffetsSecondaires())
                .createdAt(LocalDateTime.now())
                .build();
        
        return adherenceLogRepository.save(log);
    }
    
    public List<AdherenceLog> getLogsPatient(String patientId) {
        return adherenceLogRepository.findByPatientIdOrderByDatePriseDesc(patientId);
    }
    
    public List<AdherenceLog> getLogsPrescription(String prescriptionId) {
        return adherenceLogRepository.findByPrescriptionIdOrderByDatePriseDesc(prescriptionId);
    }
    
    public Map<String, Object> calculerStatistiquesAdherence(String patientId, int dernierJours) {
        LocalDateTime debut = LocalDateTime.now().minus(dernierJours, ChronoUnit.DAYS);
        LocalDateTime fin = LocalDateTime.now();
        
        List<AdherenceLog> logs = adherenceLogRepository
                .findByPatientIdAndDatePriseBetween(patientId, debut, fin);
        
        long totalPrevu = logs.size();
        long totalPris = logs.stream().filter(l -> Boolean.TRUE.equals(l.getPrise())).count();
        long totalOublie = totalPrevu - totalPris;
        
        double tauxAdherence = totalPrevu > 0 ? (totalPris * 100.0 / totalPrevu) : 0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("periode", dernierJours + " jours");
        stats.put("totalPrevu", totalPrevu);
        stats.put("totalPris", totalPris);
        stats.put("totalOublie", totalOublie);
        stats.put("tauxAdherence", Math.round(tauxAdherence * 100.0) / 100.0);
        stats.put("niveau", getNiveauAdherence(tauxAdherence));
        
        return stats;
    }
    
    private String getNiveauAdherence(double taux) {
        if (taux >= 90) return "EXCELLENT";
        if (taux >= 75) return "BON";
        if (taux >= 50) return "MOYEN";
        return "FAIBLE";
    }
}
