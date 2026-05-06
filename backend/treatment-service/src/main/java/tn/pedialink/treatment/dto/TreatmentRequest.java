package tn.pedialink.treatment.dto;

import lombok.Data;
import tn.pedialink.treatment.entity.Treatment;

import java.time.LocalDate;
import java.util.List;

@Data
public class TreatmentRequest {
    private String patientId;
    private String diagnostic;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String objectifTraitement;
    private String notes;
    private List<MedicamentRequest> medicaments;
    private List<String> recommandations;
    
    @Data
    public static class MedicamentRequest {
        private String nomCommercial;
        private String dci;
        private String formePharmaceutique;
        private String dosage;
        private PosologieRequest posologie;
        private String instructionsSpeciales;
    }
    
    @Data
    public static class PosologieRequest {
        private Double quantite;
        private String unite;
        private String frequence;
        private String momentPrise;
        private Integer dureeTraitementJours;
    }
}
