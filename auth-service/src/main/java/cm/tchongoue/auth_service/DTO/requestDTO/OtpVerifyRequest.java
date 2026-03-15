package cm.tchongoue.auth_service.DTO.requestDTO;


import jakarta.validation.constraints.NotBlank;

public record OtpVerifyRequest(
        @NotBlank String userId,
        @NotBlank String code
) {}