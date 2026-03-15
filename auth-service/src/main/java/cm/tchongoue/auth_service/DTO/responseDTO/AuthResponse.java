package cm.tchongoue.auth_service.DTO.responseDTO;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        UserResponse user
) {}