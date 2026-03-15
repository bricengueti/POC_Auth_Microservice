package cm.tchongoue.auth_service.service;


import cm.tchongoue.auth_service.DTO.requestDTO.RegisterCompleteRequest;
import cm.tchongoue.auth_service.DTO.requestDTO.RegisterProfileRequest;
import cm.tchongoue.auth_service.DTO.responseDTO.AuthResponse;
import cm.tchongoue.auth_service.config.KeycloakProperties;
import cm.tchongoue.auth_service.exception.KeycloakException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {

    private final KeycloakProperties props;
    private final RestTemplate restTemplate;

    public KeycloakService(KeycloakProperties props, RestTemplate restTemplate) {
        this.props = props;
        this.restTemplate = restTemplate;
    }

    private String tokenUrl() {
        return props.getServerUrl() + "/realms/" + props.getRealm() + "/protocol/openid-connect/token";
    }

    private String logoutUrl() {
        return props.getServerUrl() + "/realms/" + props.getRealm() + "/protocol/openid-connect/logout";
    }

    private Keycloak adminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(props.getServerUrl())
                .realm(props.getRealm())
                .clientId(props.getClientId())
                .clientSecret(props.getClientSecret())
                .username(props.getAdmin().getUsername())
                .password(props.getAdmin().getPassword())
                .grantType("password")
                .build();
    }

    // ─────────────────────────────────────────────
    // Crée le user dans Keycloak → retourne keycloakId
    // ─────────────────────────────────────────────
    public String createUser(RegisterProfileRequest profile, RegisterCompleteRequest complete) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(complete.password());
        credential.setTemporary(false);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(profile.userId());
        user.setEmail(profile.email());
        user.setFirstName(profile.firstName());
        user.setLastName(profile.lastName());
        user.setEnabled(true);
        user.setCredentials(List.of(credential));
        user.setAttributes(Map.of("phoneNumber", List.of(profile.userId())));

        try (var response = adminClient().realm(props.getRealm()).users().create(user)) {
            if (response.getStatus() != 201) {
                throw new KeycloakException("Failed to create user : " + response.getStatus());
            }
            String location = response.getHeaderString("Location");
            return location.substring(location.lastIndexOf("/") + 1);
        }
    }

    // ─────────────────────────────────────────────
    // Authentifie phoneNumber + password → retourne tokens
    // ─────────────────────────────────────────────
    public AuthResponse authenticate(String phoneNumber, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());
        form.add("username", phoneNumber);
        form.add("password", password);

        // log temporaire
        System.out.println("=== Keycloak authenticate ===");
        System.out.println("URL: " + tokenUrl());
        System.out.println("client_id: " + props.getClientId());
        System.out.println("client_secret: " + props.getClientSecret());
        System.out.println("username: " + phoneNumber);

        return exchangeToken(form);
    }

    // ─────────────────────────────────────────────
    // Rafraîchit l'accessToken via refreshToken
    // ─────────────────────────────────────────────
    public AuthResponse refreshToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());
        form.add("refresh_token", refreshToken);
        return exchangeToken(form);
    }

    // ─────────────────────────────────────────────
    // Révoque le refreshToken → déconnecte le user
    // ─────────────────────────────────────────────
    public void logout(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());
        form.add("refresh_token", refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        restTemplate.postForEntity(
                logoutUrl(),
                new HttpEntity<>(form, headers),
                Void.class
        );
    }

    // ─────────────────────────────────────────────
    // Met à jour le password dans Keycloak
    // ─────────────────────────────────────────────
    public void updatePassword(String keycloakUserId, String newPassword) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(false);

        adminClient().realm(props.getRealm())
                .users()
                .get(keycloakUserId)
                .resetPassword(credential);
    }

    // ─────────────────────────────────────────────
    // Appel commun /token Keycloak
    // ─────────────────────────────────────────────
    private AuthResponse exchangeToken(MultiValueMap<String, String> form) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUrl(),
                new HttpEntity<>(form, headers),
                Map.class
        );

        Map body = response.getBody();
        if (body == null) throw new KeycloakException("Empty response from Keycloak");

        return new AuthResponse(
                (String) body.get("access_token"),
                (String) body.get("refresh_token"),
                ((Number) body.get("expires_in")).longValue(),
                "Bearer",
                null
        );
    }
}
