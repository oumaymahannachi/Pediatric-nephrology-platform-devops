package tn.pedialink.dossiermedical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.pedialink.dossiermedical.model.dialyse.ParametresTechniques;
import tn.pedialink.dossiermedical.model.dialyse.StatutDialyse;
import tn.pedialink.dossiermedical.model.dialyse.TypeDialyse;
import java.time.LocalDateTime;

@Data
public class DialyseDto {
    @NotBlank
    private String patientId;
    @NotBlank
    private String medecinId;
    private String consultationId;
    @NotNull
    private LocalDateTime dateSeance;
    @NotNull
    private TypeDialyse typeDialyse;
    private ParametresTechniques parametres;
    private String accesVasculaire;
    private String complications;
    private String observations;
    private StatutDialyse statut;
}
