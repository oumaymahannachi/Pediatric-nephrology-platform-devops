package tn.pedialink.dossiermedical.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.pedialink.dossiermedical.client.dto.UserInfoDto;

/**
 * Feign Client pour communiquer avec auth-service.
 * Permet de récupérer les informations des utilisateurs (médecins, parents)
 * sans dupliquer les données dans dossiermedical-service.
 */
@FeignClient(name = "auth-service", fallback = AuthServiceClientFallback.class)
public interface AuthServiceClient {

    @GetMapping("/api/users/{userId}")
    UserInfoDto getUserById(@PathVariable("userId") String userId);

    @GetMapping("/api/users/email/{email}")
    UserInfoDto getUserByEmail(@PathVariable("email") String email);
}
