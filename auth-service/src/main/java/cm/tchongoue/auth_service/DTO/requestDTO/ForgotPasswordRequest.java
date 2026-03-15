package cm.tchongoue.auth_service.DTO.requestDTO;


import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank String phoneNumber
) {}
