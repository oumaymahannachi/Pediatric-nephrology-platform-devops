package tn.pedialink.dossiermedical.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tn.pedialink.dossiermedical.client.dto.UserInfoDto;

/**
 * Fallback pour AuthServiceClient - retourne des données par défaut
 * si auth-service est indisponible (circuit breaker).
 */
@Slf4j
@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    @Override
    public UserInfoDto getUserById(String userId) {
        log.warn("auth-service unavailable - fallback for getUserById({})", userId);
        UserInfoDto fallback = new UserInfoDto();
        fallback.setId(userId);
        fallback.setFullName("Unknown User");
        return fallback;
    }

    @Override
    public UserInfoDto getUserByEmail(String email) {
        log.warn("auth-service unavailable - fallback for getUserByEmail({})", email);
        UserInfoDto fallback = new UserInfoDto();
        fallback.setEmail(email);
        fallback.setFullName("Unknown User");
        return fallback;
    }
}
