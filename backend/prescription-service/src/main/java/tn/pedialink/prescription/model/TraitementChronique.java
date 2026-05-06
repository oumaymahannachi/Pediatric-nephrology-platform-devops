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
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "traitements_chroniques")
@CompoundIndexes({
        @CompoundIndex(name = "patient_actif_idx", def = "{'patientId': 1, 'actif': 1}"),
        @CompoundIndex(name = "prochain_bilan_idx", def = "{'bilansProgrammes.datePrevue': 1}"),
        @CompoundIndex(name = "renouvellement_idx", def = "{'dateProchainRenouvellement': 1}")
})
public class TraitementChronique {

    @Id
    private String id;

    private String patientId;

    private String medecinPrescripteurId;

    private String nomTraitement;

    private String pathologie;

    private LocalDate dateDebut;

    private LocalDate dateFinPrevue;
    private LocalDate dateFinEffective;

    private Boolean actif;

    @Builder.Default
    private List<MedicamentChronique> medicaments = new ArrayList<>();

    private Integer dureeEntreRenouvellementsJours;

    private LocalDate dateDernierRenouvellement;
    private LocalDate dateProchainRenouvellement;

    @Builder.Default
    private List<Renouvellement> historiqueRenouvellements = new ArrayList<>();

    @Builder.Default
    private List<Bilan> bilansProgrammes = new ArrayList<>();

    @Builder.Default
    private List<Observance> historiqueObservance = new ArrayList<>();

    private String notesEvolution;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicamentChronique {
        private String nomCommercial;
        private String dci;
        private String dosage;
        private Prescription.Posologie posologie;
        private Boolean medicamentPrincipal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Renouvellement {
        private LocalDate dateRenouvellement;
        private String prescriptionId;
        private String medecinId;
        private Integer nombreUnitesDelivrees;
        private String pharmacienId;
        private Boolean avecConsultation;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bilan {
        private String typeBilan;
        private String description;
        private LocalDate datePrevue;
        private LocalDate dateRealisation;
        private String resultat;
        private Boolean realise;
        private String alerteSiAnomalie;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Observance {
        private LocalDate date;
        private Boolean pris;
        private Integer heurePrise;
        private String commentaire;
        private Double quantitePrise;
        private Boolean oublie;
        private Boolean prisHorsHoraire;
    }
}