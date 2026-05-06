package tn.pedialink.treatment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.pedialink.treatment.dto.ProgressLogRequest;
import tn.pedialink.treatment.entity.ProgressLog;
import tn.pedialink.treatment.entity.Treatment;
import tn.pedialink.treatment.repository.ProgressLogRepository;
import tn.pedialink.treatment.repository.TreatmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressService {
    
    private final ProgressLogRepository progressLogRepository;
    private final TreatmentRepository treatmentRepository;
    
    public ProgressLog logProgress(String patientId, ProgressLogRequest request) {
        Treatment treatment = treatmentRepository.findById(request.getTreatmentId())
                .orElseThrow(() -> new RuntimeException("Traitement non trouvé"));
        
        ProgressLog log = ProgressLog.builder()
                .treatmentId(request.getTreatmentId())
                .patientId(patientId)
                .date(request.getDate())
                .symptomes(request.getSymptomes())
                .notes(request.getNotes())
                .poids(request.getPoids())
                .taille(request.getTaille())
                .humeur(request.getHumeur())
                .niveauEnergie(request.getNiveauEnergie())
                .qualiteSommeil(request.getQualiteSommeil())
                .createdAt(LocalDateTime.now())
                .build();
        
        return progressLogRepository.save(log);
    }
    
    public List<ProgressLog> getProgressByTreatment(String treatmentId) {
        return progressLogRepository.findByTreatmentIdOrderByDateDesc(treatmentId);
    }
    
    public List<ProgressLog> getProgressByPatient(String patientId) {
        return progressLogRepository.findByPatientIdOrderByDateDesc(patientId);
    }
    
    public Map<String, Object> analyzeProgress(String treatmentId, int dernierJours) {
        Treatment treatment = treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new RuntimeException("Traitement non trouvé"));
        
        LocalDate debut = LocalDate.now().minusDays(dernierJours);
        LocalDate fin = LocalDate.now();
        
        List<ProgressLog> logs = progressLogRepository
                .findByPatientIdAndDateBetween(treatment.getPatientId(), debut, fin);
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("periode", dernierJours + " jours");
        analysis.put("nombreEntrees", logs.size());
        
        // Analyse des symptômes
        Map<String, Double> symptomesEvolution = analyzeSymptoms(logs);
        analysis.put("symptomesEvolution", symptomesEvolution);
        
        // Analyse du poids
        if (logs.stream().anyMatch(l -> l.getPoids() != null)) {
            Map<String, Object> poidsAnalysis = analyzeWeight(logs);
            analysis.put("poids", poidsAnalysis);
        }
        
        // Analyse de l'énergie et sommeil
        Map<String, Object> wellbeing = analyzeWellbeing(logs);
        analysis.put("bienEtre", wellbeing);
        
        return analysis;
    }
    
    private Map<String, Double> analyzeSymptoms(List<ProgressLog> logs) {
        Map<String, List<Integer>> symptomValues = new HashMap<>();
        
        for (ProgressLog log : logs) {
            if (log.getSymptomes() != null) {
                log.getSymptomes().forEach((symptom, severity) -> {
                    symptomValues.computeIfAbsent(symptom, k -> new ArrayList<>()).add(severity);
                });
            }
        }
        
        Map<String, Double> evolution = new HashMap<>();
        symptomValues.forEach((symptom, values) -> {
            double moyenne = values.stream().mapToInt(Integer::intValue).average().orElse(0);
            evolution.put(symptom, Math.round(moyenne * 100.0) / 100.0);
        });
        
        return evolution;
    }
    
    private Map<String, Object> analyzeWeight(List<ProgressLog> logs) {
        List<Double> poids = logs.stream()
                .filter(l -> l.getPoids() != null)
                .map(ProgressLog::getPoids)
                .collect(Collectors.toList());
        
        Map<String, Object> analysis = new HashMap<>();
        if (!poids.isEmpty()) {
            analysis.put("poidsActuel", poids.get(0));
            analysis.put("poidsInitial", poids.get(poids.size() - 1));
            analysis.put("variation", poids.get(0) - poids.get(poids.size() - 1));
        }
        
        return analysis;
    }
    
    private Map<String, Object> analyzeWellbeing(List<ProgressLog> logs) {
        Map<String, Object> wellbeing = new HashMap<>();
        
        double avgEnergie = logs.stream()
                .filter(l -> l.getNiveauEnergie() != null)
                .mapToInt(ProgressLog::getNiveauEnergie)
                .average()
                .orElse(0);
        
        double avgSommeil = logs.stream()
                .filter(l -> l.getQualiteSommeil() != null)
                .mapToInt(ProgressLog::getQualiteSommeil)
                .average()
                .orElse(0);
        
        wellbeing.put("niveauEnergieMoyen", Math.round(avgEnergie * 100.0) / 100.0);
        wellbeing.put("qualiteSommeilMoyenne", Math.round(avgSommeil * 100.0) / 100.0);
        
        return wellbeing;
    }
}
