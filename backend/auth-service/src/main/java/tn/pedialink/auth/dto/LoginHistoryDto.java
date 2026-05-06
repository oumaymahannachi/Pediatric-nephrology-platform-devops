package tn.pedialink.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryDto {
    private String id;
    private String userId;
    private String email;
    private String ipAddress;
    private String userAgent;
    private boolean success;
    private String failureReason;
    private String role;
    private Instant timestamp;
}
