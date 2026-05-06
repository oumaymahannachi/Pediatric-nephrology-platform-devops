package tn.pedialink.dossiermedical.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tn.pedialink.dossiermedical.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // Active @PreAuthorize sur les controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(java.util.List.of("http://localhost:4200"));
                config.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
                config.setAllowedHeaders(java.util.List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .authorizeHttpRequests(auth -> auth
                // Preflight CORS requests
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // Endpoints publics (lecture parent)
                .requestMatchers("GET", "/api/examinations/*/patient/**").permitAll()
                .requestMatchers("GET", "/api/gfr/patient/**").permitAll()
                .requestMatchers("GET", "/api/dialyses/sessions/patient/**").permitAll()
                // Medical Intelligence - permit all for now
                .requestMatchers("/api/medical-intelligence/**").permitAll()
                // Appointments & Consultations
                .requestMatchers("/api/parent/appointments/**").permitAll()
                .requestMatchers("/api/doctor/appointments/**").permitAll()
                .requestMatchers("/api/consultations/**").permitAll()
                // Tout le reste nécessite authentification
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
