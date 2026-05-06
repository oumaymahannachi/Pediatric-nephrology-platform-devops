package tn.pedialink.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    private String role;

    @Pattern(regexp = "^$|^\\+?[0-9\\s\\-]{8,15}$", message = "Invalid phone number")
    private String phone;

    @Pattern(regexp = "^$|^\\d{8}$", message = "CIN must be 8 digits")
    private String cin;

    private String specialization;
    private String licenseNumber;
    private String clinicName;

    private String serviceUnit;
}
