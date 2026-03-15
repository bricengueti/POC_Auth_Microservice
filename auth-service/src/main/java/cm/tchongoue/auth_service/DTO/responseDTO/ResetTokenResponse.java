package cm.tchongoue.auth_service.DTO.responseDTO;


public record ResetTokenResponse(
        String resetToken,
        String userId,
        long expiresIn
) {}