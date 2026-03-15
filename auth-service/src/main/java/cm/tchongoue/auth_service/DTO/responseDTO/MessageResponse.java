package cm.tchongoue.auth_service.DTO.responseDTO;

public record MessageResponse(
        String message,
        boolean success
) {}