package tn.pedialink.prescription.dto.traitement;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TraitementResponse {
    private String id;
    private String patientId;
    private String medecinPrescripteurId;
    private String nomTraitement;
    private String pathologie;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private LocalDate dateFinEffective;
    private Boolean actif;
    private List<MedicamentChroniqueResponse> medicaments;
    private Integer dureeEntreRenouvellementsJours;
    private LocalDate dateDernierRenouvellement;
    private LocalDate dateProchainRenouvellement;
    private List<RenouvellementResponse> historiqueRenouvellements;
    private List<BilanResponse> bilansProgrammes;
    private String notesEvolution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer joursAvantRenouvellement;
    private Boolean renouvellementUrgent;

    @Data
    @Builder
    public static class MedicamentChroniqueResponse {
        private String nomCommercial;
        private String dci;
        private String dosage;
        private String posologie;
        private Boolean medicamentPrincipal;
    }

    @Data
    @Builder
    public static class RenouvellementResponse {
        private LocalDate dateRenouvellement;
        private String prescriptionId;
        private String medecinId;
        private Integer nombreUnitesDelivrees;
        private Boolean avecConsultation;
    }

    @Data
    @Builder
    public static class BilanResponse {
        private String typeBilan;
        private String description;
        private LocalDate datePrevue;
        private LocalDate dateRealisation;
        private Boolean realise;
        private Boolean enRetard;
    }
}