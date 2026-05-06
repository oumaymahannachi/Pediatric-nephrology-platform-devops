package tn.pedialink.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("login_history")
public class LoginHistory {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String email;
    private String ipAddress;
    private String userAgent;

    @Builder.Default
    private boolean success = true;

    private String failureReason;

    private Role role;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
