// RegisterInitRequest.java
package cm.tchongoue.auth_service.DTO.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterInitRequest(
        @NotBlank
        @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Invalid phone number")
        String phoneNumber
) {}