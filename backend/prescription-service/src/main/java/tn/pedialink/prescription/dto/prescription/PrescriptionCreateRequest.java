package tn.pedialink.prescription.dto.prescription;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PrescriptionCreateRequest {

    @NotBlank(message = "L'ID patient est obligatoire")
    private String patientId;

    @NotBlank(message = "Le diagnostic est obligatoire")
    @Size(max = 500, message = "Le diagnostic ne doit pas dépasser 500 caractères")
    private String diagnostic;

    @NotNull(message = "La date de prescription est obligatoire")
    @FutureOrPresent(message = "La date doit être aujourd'hui ou dans le futur")
    private LocalDate datePrescription;

    @NotNull(message = "La durée de validité est obligatoire")
    @Min(value = 1, message = "La durée minimale est 1 jour")
    @Max(value = 365, message = "La durée maximale est 365 jours")
    private Integer dureeValiditeJours;

    @NotEmpty(message = "Au moins un médicament est requis")
    @Valid
    private List<MedicamentRequest> medicaments;

    private String notes;
    private Boolean renouvelable;
    private Integer nombreRenouvellementsAutorises;

    @Data
    public static class MedicamentRequest {
        @NotBlank
        private String nomCommercial;

        @NotBlank
        private String dci;

        private String formePharmaceutique;
        private String dosage;

        @NotNull
        @Valid
        private PosologieRequest posologie;

        private String instructionsSpeciales;
        private Boolean substitutable;
    }

    @Data
    public static class PosologieRequest {
        @NotNull
        @Min(0)
        private Double quantite;

        @NotBlank
        private String unite;

        @NotBlank
        private String frequence;

        private String momentPrise;
        private Integer dureeTraitementJours;

        private Boolean isPediatrique;
        private Double poidsPatientKg;
        private Double doseParKg;
        private Double surfaceCorporelleM2;

        private Boolean ajustementRenal;
        private Boolean ajustementHepatique;
        private String justificationAjustement;
    }
}