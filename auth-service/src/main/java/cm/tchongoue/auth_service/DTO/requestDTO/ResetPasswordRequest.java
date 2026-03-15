package cm.tchongoue.auth_service.DTO.requestDTO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String resetToken,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String confirmPassword
) {}