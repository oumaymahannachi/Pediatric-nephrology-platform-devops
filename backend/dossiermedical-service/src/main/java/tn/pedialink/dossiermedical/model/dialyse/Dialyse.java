package tn.pedialink.dossiermedical.model.dialyse;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "dialyses")
public class Dialyse {
    @Id
    private String id;
    private String patientId;
    private String medecinId;
    private String consultationId;
    private LocalDateTime dateSeance;
    private TypeDialyse typeDialyse;
    private ParametresTechniques parametres;
    private String accesVasculaire;
    private String complications;
    private String observations;
    private StatutDialyse statut;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
