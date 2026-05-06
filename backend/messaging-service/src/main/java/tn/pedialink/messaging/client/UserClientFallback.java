package tn.pedialink.messaging.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tn.pedialink.messaging.dto.UserDto;

import java.util.Collections;
import java.util.List;

/**
 * Fallback implementation for UserClient.
 * Called when auth-service is unavailable.
 */
@Component
@Slf4j
public class UserClientFallback implements UserClient {

    @Override
    public UserDto getUserById(String id) {
        log.warn("auth-service unavailable, returning fallback for user: {}", id);
        UserDto fallback = new UserDto();
        fallback.setId(id);
        fallback.setFullName("Utilisateur inconnu");
        return fallback;
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.warn("auth-service unavailable, returning empty list");
        return Collections.emptyList();
    }
}
