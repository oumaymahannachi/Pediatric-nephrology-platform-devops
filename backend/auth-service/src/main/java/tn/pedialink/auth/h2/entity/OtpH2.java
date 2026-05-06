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
@Table(name = "otps")
public class OtpH2 {

    @Id
    @Column(name = "mongo_id", nullable = false, unique = true)
    private String mongoId;

    private String email;
    private String code;
    private String purpose;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Builder.Default
    private boolean used = false;
}
