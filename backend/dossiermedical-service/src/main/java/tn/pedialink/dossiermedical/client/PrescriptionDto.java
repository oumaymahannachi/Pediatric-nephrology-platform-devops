package tn.pedialink.dossiermedical.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDto {
    private String id;
    private String patientId;
    private String medecinId;
    private LocalDate datePrescription;
    private String statut;
}
