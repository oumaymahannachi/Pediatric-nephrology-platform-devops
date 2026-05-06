package tn.pedialink.auth.h2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserH2 {

    @Id
    @Column(name = "mongo_id", nullable = false, unique = true)
    private String mongoId;

    @Column(name = "full_name")
    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "role")
    private String role;

    private String phone;
    private String cin;

    @Column(name = "status")
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "email_verified")
    @Builder.Default
    private boolean emailVerified = false;

    private String specialization;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "clinic_name")
    private String clinicName;

    @Column(name = "service_unit")
    private String serviceUnit;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
