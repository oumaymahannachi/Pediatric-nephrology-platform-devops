package tn.pedialink.messaging.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.pedialink.messaging.dto.UserDto;

import java.util.List;

/**
 * OpenFeign client for inter-service communication with auth-service.
 * messaging-service → auth-service
 */
@FeignClient(name = "auth-service", path = "/api/users", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") String id);

    @GetMapping
    List<UserDto> getAllUsers();
}
