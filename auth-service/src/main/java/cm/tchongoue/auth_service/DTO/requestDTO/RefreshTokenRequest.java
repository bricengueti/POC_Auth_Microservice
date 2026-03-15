package cm.tchongoue.auth_service.DTO.requestDTO;


import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank String refreshToken
) {}