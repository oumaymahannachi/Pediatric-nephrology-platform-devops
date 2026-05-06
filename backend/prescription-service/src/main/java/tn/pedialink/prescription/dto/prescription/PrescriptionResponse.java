package tn.pedialink.prescription.dto.prescription;

import lombok.Builder;
import lombok.Data;
import tn.pedialink.prescription.model.Prescription;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PrescriptionResponse {
    private String id;
    private String patientId;
    private String medecinId;
    private LocalDate datePrescription;
    private LocalDate dateExpiration;
    private Integer dureeValiditeJours;
    private String diagnostic;
    private List<MedicamentResponse> medicaments;
    private String notes;
    private Prescription.StatutPrescription statut;
    private Boolean renouvelable;
    private Integer nombreRenouvellementsRestants;
    private Integer nombreRenouvellementsEffectues;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> alertes;

    @Data
    @Builder
    public static class MedicamentResponse {
        private String nomCommercial;
        private String dci;
        private String formePharmaceutique;
        private String dosage;
        private PosologieResponse posologie;
        private String instructionsSpeciales;
        private Boolean substitutable;
        private List<String> interactionsDetectees;
    }

    @Data
    @Builder
    public static class PosologieResponse {
        private Double quantite;
        private String unite;
        private String frequence;
        private String momentPrise;
        private Integer dureeTraitementJours;
        private Boolean isPediatrique;
        private Double poidsPatientKg;
        private Double doseParKg;
        private Double doseTotaleCalculee;
        private String calculDoseDetails;
        private Boolean ajustementRenal;
        private Boolean ajustementHepatique;
    }
}