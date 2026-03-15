package cm.tchongoue.auth_service.controller;

import cm.tchongoue.auth_service.DTO.requestDTO.*;
import cm.tchongoue.auth_service.DTO.responseDTO.AuthResponse;
import cm.tchongoue.auth_service.DTO.responseDTO.MessageResponse;
import cm.tchongoue.auth_service.DTO.responseDTO.OtpVerifyResponse;
import cm.tchongoue.auth_service.DTO.responseDTO.RegisterInitResponse;
import cm.tchongoue.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Gestion de l'inscription et de la connexion")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Initialiser l'inscription",
            description = "Reçoit le numéro de téléphone, génère un OTP et l'envoie par SMS"
    )
    @PostMapping("/register/init")
    public ResponseEntity<RegisterInitResponse> initRegister(
            @Valid @RequestBody RegisterInitRequest request) {
        return ResponseEntity.ok(authService.initRegister(request));
    }

    @Operation(
            summary = "Vérifier l'OTP d'inscription",
            description = "Vérifie le code OTP reçu par SMS. Retourne un userId si le code est valide"
    )
    @PostMapping("/register/verify-otp")
    public ResponseEntity<OtpVerifyResponse> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @Operation(
            summary = "Compléter le profil",
            description = "Reçoit les informations complémentaires (prénom, nom, email) et les stocke temporairement"
    )
    @PostMapping("/register/profile")
    public ResponseEntity<MessageResponse> completeProfile(
            @Valid @RequestBody RegisterProfileRequest request) {
        return ResponseEntity.ok(authService.completeProfile(request));
    }

    @Operation(
            summary = "Finaliser l'inscription",
            description = "Reçoit le mot de passe, crée le compte dans Keycloak et en base de données, puis retourne les tokens"
    )
    @PostMapping("/register/complete")
    public ResponseEntity<AuthResponse> completeRegister(
            @Valid @RequestBody RegisterCompleteRequest request) {
        return ResponseEntity.status(201).body(authService.completeRegister(request));
    }

    @Operation(
            summary = "Connexion",
            description = "Authentifie l'utilisateur avec son numéro de téléphone et son mot de passe. Retourne un accessToken et un refreshToken"
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Rafraîchir le token",
            description = "Génère un nouvel accessToken à partir d'un refreshToken valide"
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(
            summary = "Déconnexion",
            description = "Révoque le refreshToken dans Keycloak et invalide la session de l'utilisateur"
    )
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @RequestHeader("X-Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(authService.logout(refreshToken));
    }
}