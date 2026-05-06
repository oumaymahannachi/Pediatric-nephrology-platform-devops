package tn.pedialink.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String fullName;
    private String email;
    private String role;
    private String phone;
    private String cin;
    private String status;
    private boolean emailVerified;
    private String specialization;
    private String licenseNumber;
    private String clinicName;
    private String serviceUnit;
}
