package tn.pedialink.prescription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.pedialink.prescription.model.Prescription;
import tn.pedialink.prescription.repository.PrescriptionRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {
    
    private final PrescriptionRepository prescriptionRepository;
    
    public Map<String, Object> getStatistiquesMedecin(String medecinId, int dernierJours) {
        LocalDate dateDebut = LocalDate.now().minusDays(dernierJours);
        
        List<Prescription> prescriptions = prescriptionRepository.findAll().stream()
                .filter(p -> p.getMedecinId().equals(medecinId))
                .filter(p -> p.getDatePrescription().isAfter(dateDebut))
                .collect(Collectors.toList());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("periode", dernierJours + " jours");
        stats.put("totalPrescriptions", prescriptions.size());
        
        // Répartition par statut
        Map<String, Long> parStatut = prescriptions.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getStatut().toString(),
                        Collectors.counting()
                ));
        stats.put("repartitionStatut", parStatut);
        
        // Médicaments les plus prescrits
        Map<String, Long> topMedicaments = prescriptions.stream()
                .flatMap(p -> p.getMedicaments().stream())
                .collect(Collectors.groupingBy(
                        Prescription.Medicament::getDci,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        stats.put("topMedicaments", topMedicaments);
        
        // Taux de renouvellement
        long renouvelables = prescriptions.stream()
                .filter(p -> Boolean.TRUE.equals(p.getRenouvelable()))
                .count();
        long renouvelees = prescriptions.stream()
                .filter(p -> p.getStatut() == Prescription.StatutPrescription.RENOUVELEE)
                .count();
        
        stats.put("prescriptionsRenouvelables", renouvelables);
        stats.put("prescriptionsRenouvelees", renouvelees);
        stats.put("tauxRenouvellement", 
                renouvelables > 0 ? Math.round((renouvelees * 100.0 / renouvelables) * 100.0) / 100.0 : 0);
        
        return stats;
    }
    
    public Map<String, Object> getTopMedicaments(int dernierJours, int limit) {
        LocalDate dateDebut = LocalDate.now().minusDays(dernierJours);
        
        List<Prescription> prescriptions = prescriptionRepository.findAll().stream()
                .filter(p -> p.getDatePrescription().isAfter(dateDebut))
                .collect(Collectors.toList());
        
        Map<String, Long> medicamentCount = prescriptions.stream()
                .flatMap(p -> p.getMedicaments().stream())
                .collect(Collectors.groupingBy(
                        Prescription.Medicament::getDci,
                        Collectors.counting()
                ));
        
        List<Map<String, Object>> topList = medicamentCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("medicament", entry.getKey());
                    item.put("nombrePrescriptions", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("periode", dernierJours + " jours");
        result.put("topMedicaments", topList);
        
        return result;
    }
    
    public Map<String, Object> getInteractionsFrequentes() {
        // Simuler des interactions fréquentes (à remplacer par vraie logique)
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> interactions = new ArrayList<>();
        
        Map<String, Object> interaction1 = new HashMap<>();
        interaction1.put("medicament1", "Warfarine");
        interaction1.put("medicament2", "Aspirine");
        interaction1.put("severite", "MAJEURE");
        interaction1.put("description", "Risque accru de saignement");
        interaction1.put("frequence", 15);
        interactions.add(interaction1);
        
        Map<String, Object> interaction2 = new HashMap<>();
        interaction2.put("medicament1", "Metformine");
        interaction2.put("medicament2", "Contraste iodé");
        interaction2.put("severite", "MAJEURE");
        interaction2.put("description", "Risque d'acidose lactique");
        interaction2.put("frequence", 12);
        interactions.add(interaction2);
        
        result.put("interactionsFrequentes", interactions);
        result.put("total", interactions.size());
        
        return result;
    }
    
    public Map<String, Object> getHistoriquePatient(String patientId) {
        List<Prescription> prescriptions = prescriptionRepository
                .findByPatientIdOrderByDatePrescriptionDesc(patientId);
        
        Map<String, Object> historique = new HashMap<>();
        historique.put("totalPrescriptions", prescriptions.size());
        
        // Médicaments uniques prescrits
        Set<String> medicamentsUniques = prescriptions.stream()
                .flatMap(p -> p.getMedicaments().stream())
                .map(Prescription.Medicament::getDci)
                .collect(Collectors.toSet());
        historique.put("medicamentsUniques", medicamentsUniques.size());
        
        // Timeline des prescriptions
        List<Map<String, Object>> timeline = prescriptions.stream()
                .limit(20)
                .map(p -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", p.getDatePrescription());
                    item.put("diagnostic", p.getDiagnostic());
                    item.put("nombreMedicaments", p.getMedicaments().size());
                    item.put("statut", p.getStatut());
                    return item;
                })
                .collect(Collectors.toList());
        historique.put("timeline", timeline);
        
        return historique;
    }
}
