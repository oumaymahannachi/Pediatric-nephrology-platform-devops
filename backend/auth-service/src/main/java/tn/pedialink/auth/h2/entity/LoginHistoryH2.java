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
@Table(name = "login_history")
public class LoginHistoryH2 {

    @Id
    @Column(name = "mongo_id", nullable = false, unique = true)
    private String mongoId;

    @Column(name = "user_id")
    private String userId;

    private String email;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Builder.Default
    private boolean success = true;

    @Column(name = "failure_reason")
    private String failureReason;

    private String role;

    @Builder.Default
    @Column(name = "ts")
    private Instant timestamp = Instant.now();
}
