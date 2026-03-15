package cm.tchongoue.auth_service.controller;

import cm.tchongoue.auth_service.DTO.requestDTO.ChangePasswordRequest;
import cm.tchongoue.auth_service.DTO.requestDTO.ForgotPasswordRequest;
import cm.tchongoue.auth_service.DTO.requestDTO.OtpVerifyRequest;
import cm.tchongoue.auth_service.DTO.requestDTO.ResetPasswordRequest;
import cm.tchongoue.auth_service.DTO.responseDTO.MessageResponse;
import cm.tchongoue.auth_service.DTO.responseDTO.OtpVerifyResponse;
import cm.tchongoue.auth_service.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password")
@Tag(name = "Password", description = "Gestion des mots de passe")
public class PasswordController {

    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @Operation(
            summary = "Mot de passe oublié",
            description = "Reçoit le numéro de téléphone, vérifie que le compte existe et envoie un OTP par SMS"
    )
    @PostMapping("/forgot")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(passwordService.forgotPassword(request));
    }

    @Operation(
            summary = "Vérifier l'OTP de réinitialisation",
            description = "Vérifie le code OTP reçu par SMS. Retourne un resetToken valable 15 minutes"
    )
    @PostMapping("/forgot/verify-otp")
    public ResponseEntity<OtpVerifyResponse> verifyForgotOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(passwordService.verifyForgotOtp(request));
    }

    @Operation(
            summary = "Réinitialiser le mot de passe",
            description = "Reçoit le resetToken et le nouveau mot de passe, met à jour le mot de passe dans Keycloak"
    )
    @PostMapping("/reset")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(passwordService.resetPassword(request));
    }

    @PostMapping("/change")
    @Operation(
            summary = "Changer le mot de passe",
            description = "Permet à un utilisateur connecté de changer son mot de passe. Le userId est transmis par la Gateway via le header X-User-Id",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> changePassword(
            @RequestHeader("X-User-Id") String keycloakUserId,
            @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(passwordService.changePassword(keycloakUserId, request));
    }
}