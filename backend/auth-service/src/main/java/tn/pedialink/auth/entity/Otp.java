package tn.pedialink.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("otps")
public class Otp {

    @Id
    private String id;

    private String email;
    private String code;
    private OtpPurpose purpose;
    private Instant expiresAt;

    @Builder.Default
    private boolean used = false;
}
