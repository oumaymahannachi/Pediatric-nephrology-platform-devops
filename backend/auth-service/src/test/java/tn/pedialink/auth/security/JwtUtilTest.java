package tn.pedialink.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de sécurité JWT - Génération et validation des tokens")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "PediaLinkSuperSecretKeyForJwtToken2024MustBeAtLeast256BitsLong!!";
    private static final long EXPIRATION = 86400000L; // 24h

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION);
    }

    // ===== Tests génération de token =====

    @Test
    @DisplayName("Génération d'un token JWT valide pour un médecin")
    void generateToken_doctorUser_returnsValidToken() {
        String token = jwtUtil.generateToken("doctor-001", "doctor@pedialink.tn", "DOCTOR");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains(".")); // Format JWT: header.payload.signature
    }

    @Test
    @DisplayName("Génération d'un token JWT valide pour un parent")
    void generateToken_parentUser_returnsValidToken() {
        String token = jwtUtil.generateToken("parent-001", "parent@pedialink.tn", "PARENT");

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
    }

    // ===== Tests validation de token =====

    @Test
    @DisplayName("Token valide - validateToken retourne true")
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("user-001", "user@test.com", "DOCTOR");

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("Token invalide - validateToken retourne false")
    void validateToken_invalidToken_returnsFalse() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    @DisplayName("Token vide - validateToken retourne false")
    void validateToken_emptyToken_returnsFalse() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    @DisplayName("Token null - validateToken retourne false")
    void validateToken_nullToken_returnsFalse() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    @DisplayName("Token expiré - validateToken retourne false")
    void validateToken_expiredToken_returnsFalse() {
        JwtUtil shortExpirationUtil = new JwtUtil(SECRET, -1000L); // Déjà expiré
        String expiredToken = shortExpirationUtil.generateToken("user-001", "user@test.com", "DOCTOR");

        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    // ===== Tests extraction des claims =====

    @Test
    @DisplayName("Extraction de l'ID utilisateur depuis le token")
    void getUserIdFromToken_validToken_returnsUserId() {
        String token = jwtUtil.generateToken("doctor-001", "doctor@pedialink.tn", "DOCTOR");

        String userId = jwtUtil.getUserIdFromToken(token);

        assertEquals("doctor-001", userId);
    }

    @Test
    @DisplayName("Extraction du rôle depuis le token - DOCTOR")
    void getRoleFromToken_doctorToken_returnsDoctorRole() {
        String token = jwtUtil.generateToken("doctor-001", "doctor@pedialink.tn", "DOCTOR");

        String role = jwtUtil.getRoleFromToken(token);

        assertEquals("DOCTOR", role);
    }

    @Test
    @DisplayName("Extraction du rôle depuis le token - PARENT")
    void getRoleFromToken_parentToken_returnsParentRole() {
        String token = jwtUtil.generateToken("parent-001", "parent@pedialink.tn", "PARENT");

        String role = jwtUtil.getRoleFromToken(token);

        assertEquals("PARENT", role);
    }

    @Test
    @DisplayName("Extraction du rôle depuis le token - ADMIN")
    void getRoleFromToken_adminToken_returnsAdminRole() {
        String token = jwtUtil.generateToken("admin-001", "admin@pedialink.tn", "ADMIN");

        String role = jwtUtil.getRoleFromToken(token);

        assertEquals("ADMIN", role);
    }

    // ===== Tests de sécurité - Isolation des rôles =====

    @Test
    @DisplayName("Token médecin ne peut pas être utilisé comme token parent")
    void roleIsolation_doctorTokenHasDoctorRole() {
        String doctorToken = jwtUtil.generateToken("doctor-001", "doctor@pedialink.tn", "DOCTOR");

        String role = jwtUtil.getRoleFromToken(doctorToken);

        assertNotEquals("PARENT", role, "Un token médecin ne doit pas avoir le rôle PARENT");
        assertNotEquals("ADMIN", role, "Un token médecin ne doit pas avoir le rôle ADMIN");
    }

    @Test
    @DisplayName("Deux tokens différents pour deux utilisateurs différents")
    void generateToken_differentUsers_generatesDifferentTokens() {
        String token1 = jwtUtil.generateToken("user-001", "user1@test.com", "DOCTOR");
        String token2 = jwtUtil.generateToken("user-002", "user2@test.com", "DOCTOR");

        assertNotEquals(token1, token2, "Deux utilisateurs différents doivent avoir des tokens différents");
    }

    @Test
    @DisplayName("Token modifié est invalide - protection contre la falsification")
    void validateToken_tamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken("user-001", "user@test.com", "DOCTOR");
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        assertFalse(jwtUtil.validateToken(tamperedToken),
            "Un token modifié doit être rejeté");
    }
}
