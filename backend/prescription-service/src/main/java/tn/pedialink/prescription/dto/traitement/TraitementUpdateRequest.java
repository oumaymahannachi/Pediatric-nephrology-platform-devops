package tn.pedialink.prescription.dto.traitement;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TraitementUpdateRequest {

    private String nomTraitement;
    private String pathologie;
    private LocalDate dateFinPrevue;
    private Boolean actif;
    private Integer dureeEntreRenouvellementsJours;
    private List<TraitementCreateRequest.MedicamentChroniqueRequest> medicaments;
    private String notesEvolution;
}