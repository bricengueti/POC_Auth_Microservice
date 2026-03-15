package cm.tchongoue.auth_service.mapper;


import cm.tchongoue.auth_service.DTO.requestDTO.RegisterProfileRequest;
import cm.tchongoue.auth_service.DTO.responseDTO.UserResponse;
import cm.tchongoue.auth_service.entity.User;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class UserMapper {

    public User toEntity(RegisterProfileRequest profile, String keycloakId) {
        User user = new User();
        user.setId(keycloakId);
        user.setEmail(profile.email());
        user.setUsername(profile.firstName() + "." + profile.lastName());
        user.setPhoneNumber(profile.userId());
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCreatedAt()
        );
    }
}