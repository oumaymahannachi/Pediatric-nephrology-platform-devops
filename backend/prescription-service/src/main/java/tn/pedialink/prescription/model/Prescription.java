package tn.pedialink.prescription.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "prescriptions")
@CompoundIndexes({
        @CompoundIndex(name = "patient_date_idx", def = "{'patientId': 1, 'datePrescription': -1}"),
        @CompoundIndex(name = "medecin_statut_idx", def = "{'medecinId': 1, 'statut': 1}")
})
public class Prescription {

    @Id
    private String id;

    private String patientId;

    private String medecinId;

    private LocalDate datePrescription;

    @Indexed(expireAfter = "0s")
    private LocalDate dateExpiration;

    private Integer dureeValiditeJours;

    private String diagnostic;

    @Builder.Default
    private List<Medicament> medicaments = new ArrayList<>();

    private String notes;

    private StatutPrescription statut;

    private Boolean renouvelable;
    private Integer nombreRenouvellementsRestants;
    private Integer nombreRenouvellementsEffectues;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum StatutPrescription {
        ACTIVE, EXPIREE, TERMINEE, ANNULEE, RENOUVELEE
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Medicament {
        private String nomCommercial;
        private String dci;
        private String formePharmaceutique;
        private String dosage;
        private Posologie posologie;
        private String instructionsSpeciales;
        private Boolean substitutable;
        private List<String> contreIndications = new ArrayList<>();
        private List<String> interactionsConnues = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Posologie {
        private Double quantite;
        private String unite;
        private String frequence;
        private String momentPrise;
        private Integer dureeTraitementJours;
        private Boolean isPediatrique;
        private Double poidsPatientKg;
        private Double doseParKg;
        private Double surfaceCorporelleM2;
        private Double doseTotaleCalculee;
        private String calculDoseDetails;
        private Boolean ajustementRenal;
        private Boolean ajustementHepatique;
        private String justificationAjustement;
    }
}