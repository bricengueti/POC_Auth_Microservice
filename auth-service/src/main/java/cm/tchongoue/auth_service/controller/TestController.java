package cm.tchongoue.auth_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
// VERSION CORRIGÉE
@RestController
public class TestController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${keycloak.server-url}")         // ← lu depuis les properties
    private String keycloakServerUrl;

    @GetMapping("/public")
    public String publicEndpoint() {
        return "✅ Endpoint public accessible sans token";
    }

    @GetMapping("/redis-test")
    public String redisTest() {
        try {
            redisTemplate.opsForValue().set("health-check", "ok");
            String val = redisTemplate.opsForValue().get("health-check");
            return "✅ Redis OK — valeur lue : " + val;
        } catch (Exception e) {
            return "❌ Redis KO: " + e.getMessage();
        }
    }

    @GetMapping("/keycloak-test")
    public String keycloakTest() {
        // Affiche l'URL configurée, pas hardcodée
        return "✅ Keycloak configuré sur : " + keycloakServerUrl;
    }
}