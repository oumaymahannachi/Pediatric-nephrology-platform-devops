package tn.pedialink.dossiermedical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.pedialink.dossiermedical.model.examen.TypeExamen;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ExamenDto {
    @NotBlank
    private String patientId;
    @NotBlank
    private String medecinId;
    private String consultationId;
    @NotNull
    private TypeExamen typeExamen;
    @NotBlank
    private String nomExamen;
    @NotNull
    private LocalDateTime dateExamen;
    private Map<String, Object> resultats;
    private String interpretation;
    private String laboratoire;
    private String fichierUrl;
}
