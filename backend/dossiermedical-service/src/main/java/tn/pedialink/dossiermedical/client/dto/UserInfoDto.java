package tn.pedialink.dossiermedical.client.dto;

import lombok.Data;

/**
 * DTO pour les informations utilisateur reçues depuis auth-service via Feign.
 */
@Data
public class UserInfoDto {
    private String id;
    private String email;
    private String fullName;
    private String role;
    private String phoneNumber;
    private String specialization; // Pour les médecins
}
