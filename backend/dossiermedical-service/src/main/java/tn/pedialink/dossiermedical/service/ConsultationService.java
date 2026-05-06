package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.dto.ConsultationDto;
import tn.pedialink.dossiermedical.model.consultation.Consultation;
import tn.pedialink.dossiermedical.model.consultation.StatutConsultation;
import tn.pedialink.dossiermedical.repository.ConsultationRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationService {
    private final ConsultationRepository consultationRepository;

    // Parent crée une demande de consultation
    public Consultation createConsultationRequest(ConsultationDto dto) {
        Consultation consultation = new Consultation();
        consultation.setPatientId(dto.getPatientId());
        consultation.setPatientName(dto.getPatientName()); // Sauvegarder le nom
        consultation.setMedecinId(dto.getMedecinId());
        consultation.setParentId(dto.getParentId()); // Ajouter le parentId
        consultation.setDateRendezVous(dto.getDateRendezVous());
        consultation.setMotifConsultation(dto.getMotifConsultation());
        consultation.setStatut(StatutConsultation.EN_ATTENTE);
        consultation.setCreatedAt(LocalDateTime.now());
        consultation.setUpdatedAt(LocalDateTime.now());
        return consultationRepository.save(consultation);
    }

    // Médecin accepte la consultation
    public Consultation acceptConsultation(String id) {
        Consultation consultation = consultationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));
        
        if (consultation.getStatut() != StatutConsultation.EN_ATTENTE) {
            throw new RuntimeException("Cette consultation ne peut plus être acceptée");
        }
        
        consultation.setStatut(StatutConsultation.ACCEPTEE);
        consultation.setUpdatedAt(LocalDateTime.now());
        return consultationRepository.save(consultation);
    }

    // Médecin refuse et propose une autre date
    public Consultation refuseConsultation(String id, LocalDateTime dateProposee, String raisonRefus) {
        Consultation consultation = consultationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));
        
        if (consultation.getStatut() != StatutConsultation.EN_ATTENTE) {
            throw new RuntimeException("Cette consultation ne peut plus être refusée");
        }
        
        consultation.setStatut(StatutConsultation.REFUSEE);
        consultation.setDateProposee(dateProposee);
        consultation.setRaisonRefus(raisonRefus);
        consultation.setUpdatedAt(LocalDateTime.now());
        return consultationRepository.save(consultation);
    }

    // Parent accepte la date proposée par le médecin
    public Consultation acceptProposedDate(String id) {
        Consultation consultation = consultationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));
        
        if (consultation.getStatut() != StatutConsultation.REFUSEE) {
            throw new RuntimeException("Aucune date proposée à accepter");
        }
        
        if (consultation.getDateProposee() == null) {
            throw new RuntimeException("Aucune date proposée");
        }
        
        consultation.setDateRendezVous(consultation.getDateProposee());
        consultation.setDateProposee(null);
        consultation.setStatut(StatutConsultation.ACCEPTEE);
        consultation.setUpdatedAt(LocalDateTime.now());
        return consultationRepository.save(consultation);
    }

    // Médecin complète la consultation après le rendez-vous
    public Consultation completeConsultation(String id, ConsultationDto dto) {
        Consultation consultation = consultationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));
        
        if (consultation.getStatut() != StatutConsultation.ACCEPTEE) {
            throw new RuntimeException("La consultation doit être acceptée avant d'être complétée");
        }
        
        consultation.setObservationsCliniques(dto.getObservationsCliniques());
        consultation.setDiagnostic(dto.getDiagnostic());
        consultation.setRecommandations(dto.getRecommandations());
        consultation.setCompteRendu(dto.getCompteRendu());
        consultation.setStatut(StatutConsultation.TERMINEE);
        consultation.setUpdatedAt(LocalDateTime.now());
        return consultationRepository.save(consultation);
    }

    public Consultation getConsultationById(String id) {
        return consultationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));
    }

    public List<Consultation> getConsultationsByPatient(String patientId) {
        return consultationRepository.findByPatientId(patientId);
    }

    public List<Consultation> getConsultationsByParent(String parentId) {
        return consultationRepository.findByParentId(parentId);
    }

    public List<Consultation> getConsultationsByMedecin(String medecinId) {
        return consultationRepository.findByMedecinId(medecinId);
    }

    public List<Consultation> getPendingConsultationsForMedecin(String medecinId) {
        return consultationRepository.findByMedecinIdAndStatut(medecinId, StatutConsultation.EN_ATTENTE);
    }

    public List<Consultation> getAllConsultations() {
        return consultationRepository.findAll();
    }

    public void deleteConsultation(String id) {
        consultationRepository.deleteById(id);
    }

    public Consultation updateConsultation(Consultation consultation) {
        consultation.setUpdatedAt(LocalDateTime.now());
        return consultationRepository.save(consultation);
    }
}
