package cm.tchongoue.auth_service.DTO.responseDTO;

public record RegisterInitResponse(
        String userId,
        String message,
        long otpExpiresIn
) {}