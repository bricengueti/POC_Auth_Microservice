package cm.tchongoue.auth_service.DTO.responseDTO;


import java.time.LocalDateTime;

public record UserResponse(
        String id,
        String username,
        String email,
        String phoneNumber,
        LocalDateTime createdAt
) {}