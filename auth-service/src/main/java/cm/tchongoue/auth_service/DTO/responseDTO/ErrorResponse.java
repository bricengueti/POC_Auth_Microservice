package cm.tchongoue.auth_service.DTO.responseDTO;

// ErrorResponse.java

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp
) {}
