package tn.pedialink.dossiermedical.model.examen;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "examens")
public class Examen {
    @Id
    private String id;
    private String patientId;
    private String medecinId;
    private String consultationId;
    private TypeExamen typeExamen;
    private String nomExamen;
    private LocalDateTime dateExamen;
    private Map<String, Object> resultats;
    private String interpretation;
    private String laboratoire;
    private String fichierUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
