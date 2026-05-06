package tn.pedialink.dossiermedical.model.consultation;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "consultations")
public class Consultation {
    @Id
    private String id;
    private String patientId;  // ID de l'enfant
    private String patientName; // Nom de l'enfant (optionnel)
    private String parentId;   // ID du parent (ajouté pour filtrage)
    private String medecinId;
    private LocalDateTime dateRendezVous;
    private LocalDateTime dateProposee; // Date proposée par médecin si refus
    private String motifConsultation;
    private String observationsCliniques;
    private String diagnostic;
    private String recommandations;
    private String compteRendu;
    private StatutConsultation statut;
    private String raisonRefus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
