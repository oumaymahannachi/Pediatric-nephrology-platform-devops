package tn.pedialink.treatment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "treatments")
public class Treatment {
    
    @Id
    private String id;
    private String patientId;
    private String medecinId;
    private String diagnostic;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String objectifTraitement;
    private String notes;
    private StatutTraitement statut;
    private List<Medicament> medicaments;
    private List<String> recommandations;
    
    public enum StatutTraitement {
        EN_COURS,
        TERMINE,
        SUSPENDU,
        ANNULE
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Medicament {
        private String nomCommercial;
        private String dci;
        private String formePharmaceutique;
        private String dosage;
        private Posologie posologie;
        private String instructionsSpeciales;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Posologie {
        private Double quantite;
        private String unite;
        private String frequence;
        private String momentPrise;
        private Integer dureeTraitementJours;
    }
}
