package Poc.gatwayService.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges

                        // ─────────────────────────────────────────────
                        // Swagger — public
                        // ─────────────────────────────────────────────
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()

                        // ─────────────────────────────────────────────
                        // Auth — routes publiques
                        // ─────────────────────────────────────────────
                        .pathMatchers(HttpMethod.POST,
                                "/api/auth/auth/register/**",
                                "/api/auth/auth/login",
                                "/api/auth/auth/refresh",
                                "/api/auth/auth/password/forgot/**",
                                "/api/auth/auth/password/reset"
                        ).permitAll()

                        // ─────────────────────────────────────────────
                        // Auth — swagger docs public
                        // ─────────────────────────────────────────────
                        .pathMatchers("/api/auth/v3/api-docs/**").permitAll()

                        // ─────────────────────────────────────────────
                        // Toutes les autres routes — JWT requis
                        // ─────────────────────────────────────────────
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                )
                .build();
    }
}