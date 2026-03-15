package cm.tchongoue.gateway_service.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    var authentication = securityContext.getAuthentication();

                    // ─────────────────────────────────────────────
                    // Si pas authentifié → on laisse passer
                    // (les routes publiques ne ont pas de token)
                    // ─────────────────────────────────────────────
                    if (authentication == null || !authentication.isAuthenticated()) {
                        return chain.filter(exchange);
                    }

                    // ─────────────────────────────────────────────
                    // Extrait le JWT → récupère le subject (keycloakId)
                    // → injecte X-User-Id dans le header
                    // ─────────────────────────────────────────────
                    if (authentication.getPrincipal() instanceof Jwt jwt) {
                        String userId = jwt.getSubject();
                        String email = jwt.getClaimAsString("email");
                        String username = jwt.getClaimAsString("preferred_username");

                        ServerHttpRequest mutatedRequest = exchange.getRequest()
                                .mutate()
                                .header("X-User-Id", userId)
                                .header("X-User-Email", email)
                                .header("X-User-Username", username)
                                .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }

                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    // ─────────────────────────────────────────────
    // Ordre 0 → s'exécute avant tous les autres filtres
    // ─────────────────────────────────────────────
    @Override
    public int getOrder() {
        return 0;
    }
}