package tn.pedialink.dossiermedical.model.dialyse;

import lombok.Data;

@Data
public class ParametresTechniques {
    private Double dureeSeance;
    private Double debitSanguin;
    private Double debitDialysat;
    private Double ultrafiltration;
    private String anticoagulation;
}
