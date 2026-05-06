package tn.pedialink.treatment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.treatment.dto.TreatmentRequest;
import tn.pedialink.treatment.entity.Treatment;
import tn.pedialink.treatment.repository.TreatmentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TreatmentService {
    
    private final TreatmentRepository treatmentRepository;
    
    public Treatment createTreatment(String medecinId, TreatmentRequest request) {
        Treatment treatment = new Treatment();
        treatment.setPatientId(request.getPatientId());
        treatment.setMedecinId(medecinId);
        treatment.setDiagnostic(request.getDiagnostic());
        treatment.setDateDebut(request.getDateDebut());
        treatment.setDateFin(request.getDateFin());
        treatment.setObjectifTraitement(request.getObjectifTraitement());
        treatment.setNotes(request.getNotes());
        treatment.setStatut(Treatment.StatutTraitement.EN_COURS);
        treatment.setMedicaments(convertMedicaments(request.getMedicaments()));
        treatment.setRecommandations(request.getRecommandations());
        
        return treatmentRepository.save(treatment);
    }
    
    public List<Treatment> getTreatmentsByPatient(String patientId) {
        return treatmentRepository.findByPatientId(patientId);
    }
    
    public Treatment getTreatment(String id) {
        return treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traitement non trouvé"));
    }
    
    public Treatment updateTreatment(String id, String medecinId, TreatmentRequest request) {
        Treatment treatment = getTreatment(id);
        
        if (!treatment.getMedecinId().equals(medecinId)) {
            throw new RuntimeException("Non autorisé");
        }
        
        treatment.setDiagnostic(request.getDiagnostic());
        treatment.setDateDebut(request.getDateDebut());
        treatment.setDateFin(request.getDateFin());
        treatment.setObjectifTraitement(request.getObjectifTraitement());
        treatment.setNotes(request.getNotes());
        treatment.setMedicaments(convertMedicaments(request.getMedicaments()));
        treatment.setRecommandations(request.getRecommandations());
        
        return treatmentRepository.save(treatment);
    }
    
    public void deleteTreatment(String id, String medecinId) {
        Treatment treatment = getTreatment(id);
        
        if (!treatment.getMedecinId().equals(medecinId)) {
            throw new RuntimeException("Non autorisé");
        }
        
        treatmentRepository.deleteById(id);
    }
    
    private List<Treatment.Medicament> convertMedicaments(List<TreatmentRequest.MedicamentRequest> requests) {
        return requests.stream().map(this::convertMedicament).collect(Collectors.toList());
    }
    
    private Treatment.Medicament convertMedicament(TreatmentRequest.MedicamentRequest request) {
        Treatment.Medicament med = new Treatment.Medicament();
        med.setNomCommercial(request.getNomCommercial());
        med.setDci(request.getDci());
        med.setFormePharmaceutique(request.getFormePharmaceutique());
        med.setDosage(request.getDosage());
        med.setPosologie(convertPosologie(request.getPosologie()));
        med.setInstructionsSpeciales(request.getInstructionsSpeciales());
        return med;
    }
    
    private Treatment.Posologie convertPosologie(TreatmentRequest.PosologieRequest request) {
        Treatment.Posologie pos = new Treatment.Posologie();
        pos.setQuantite(request.getQuantite());
        pos.setUnite(request.getUnite());
        pos.setFrequence(request.getFrequence());
        pos.setMomentPrise(request.getMomentPrise());
        pos.setDureeTraitementJours(request.getDureeTraitementJours());
        return pos;
    }
}
