package cm.tchongoue.auth_service.DTO.responseDTO;


public record OtpVerifyResponse(
        String userId,
        boolean verified,
        String message
) {}