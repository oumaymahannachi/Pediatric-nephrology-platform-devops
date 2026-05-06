package tn.pedialink.prescription.dto.prescription;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tn.pedialink.prescription.model.Prescription;

import java.util.List;

@Data
public class PrescriptionUpdateRequest {

    @Size(max = 500, message = "Le diagnostic ne doit pas dépasser 500 caractères")
    private String diagnostic;

    @Min(value = 1, message = "La durée minimale est 1 jour")
    @Max(value = 365, message = "La durée maximale est 365 jours")
    private Integer dureeValiditeJours;

    private List<PrescriptionCreateRequest.MedicamentRequest> medicaments;

    private String notes;
    private Prescription.StatutPrescription statut;
    private Boolean renouvelable;
    private Integer nombreRenouvellementsRestants;
}