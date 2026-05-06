package tn.pedialink.prescription.dto.traitement;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TraitementCreateRequest {

    @NotBlank(message = "L'ID patient est obligatoire")
    private String patientId;

    @NotBlank(message = "Le nom du traitement est obligatoire")
    @Size(max = 200)
    private String nomTraitement;

    @Size(max = 500)
    private String pathologie;

    @NotNull
    @FutureOrPresent
    private LocalDate dateDebut;

    private LocalDate dateFinPrevue;

    @NotNull
    @Min(1)
    @Max(365)
    private Integer dureeEntreRenouvellementsJours;

    @NotEmpty
    @Valid
    private List<MedicamentChroniqueRequest> medicaments;

    @Valid
    private List<BilanRequest> bilansProgrammes;

    private String prescriptionOrigineId;
    private String notesEvolution;

    @Data
    public static class MedicamentChroniqueRequest {
        @NotBlank
        private String nomCommercial;
        private String dci;
        private String dosage;
        private PosologieRequest posologie;
        private Boolean medicamentPrincipal;
    }

    @Data
    public static class PosologieRequest {
        private Double quantite;
        private String unite;
        private String frequence;
        private String momentPrise;
    }

    @Data
    public static class BilanRequest {
        @NotBlank
        private String typeBilan;
        private String description;
        @NotNull
        @FutureOrPresent
        private LocalDate datePrevue;
        private String alerteSiAnomalie;
    }
}