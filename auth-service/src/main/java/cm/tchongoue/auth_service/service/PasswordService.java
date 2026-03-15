package cm.tchongoue.auth_service.service;

import cm.tchongoue.auth_service.DTO.requestDTO.ChangePasswordRequest;
import cm.tchongoue.auth_service.DTO.requestDTO.ForgotPasswordRequest;
import cm.tchongoue.auth_service.DTO.requestDTO.OtpVerifyRequest;
import cm.tchongoue.auth_service.DTO.requestDTO.ResetPasswordRequest;
import cm.tchongoue.auth_service.DTO.responseDTO.MessageResponse;
import cm.tchongoue.auth_service.DTO.responseDTO.OtpVerifyResponse;
import cm.tchongoue.auth_service.entity.OtpType;
import cm.tchongoue.auth_service.exception.*;
import cm.tchongoue.auth_service.repository.jpa.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PasswordService {

    private final OtpService otpService;
    private final SmsService smsService;
    private final KeycloakService keycloakService;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String RESET_TOKEN_PREFIX = "reset:token:";
    private static final long RESET_TOKEN_TTL = 15;

    public PasswordService(OtpService otpService,
                           SmsService smsService,
                           KeycloakService keycloakService,
                           UserRepository userRepository,
                           RedisTemplate<String, String> redisTemplate) {
        this.otpService = otpService;
        this.smsService = smsService;
        this.keycloakService = keycloakService;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    // ─────────────────────────────────────────────
    // FORGOT STEP 1 : entre son numéro
    //                → vérifie existence → envoie OTP
    // ─────────────────────────────────────────────
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String userId = request.phoneNumber().replaceAll("[^0-9]", "");

        userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new UserNotFoundException("No account found with this phone number"));

        String code = otpService.generate(userId, OtpType.FORGOT_PASSWORD);
        smsService.sendOtp(request.phoneNumber(), code);

        return new MessageResponse("OTP sent successfully", true);
    }

    // ─────────────────────────────────────────────
    // FORGOT STEP 2 : confirme OTP
    //                → génère resetToken (15min) → stocke Redis
    // ─────────────────────────────────────────────
    public OtpVerifyResponse verifyForgotOtp(OtpVerifyRequest request) {
        boolean valid = otpService.verify(request.userId(), request.code(), OtpType.FORGOT_PASSWORD);
        if (!valid) throw new OtpException("Invalid or expired OTP");

        otpService.invalidate(request.userId(), OtpType.FORGOT_PASSWORD);

        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                RESET_TOKEN_PREFIX + resetToken,
                request.userId(),
                RESET_TOKEN_TTL, TimeUnit.MINUTES
        );

        return new OtpVerifyResponse(resetToken, true, "OTP verified");
    }

    // ─────────────────────────────────────────────
    // FORGOT STEP 3 : entre nouveau password
    //                → vérifie resetToken → update Keycloak
    // ─────────────────────────────────────────────
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        String userId = redisTemplate.opsForValue().get(RESET_TOKEN_PREFIX + request.resetToken());
        if (userId == null) throw new InvalidTokenException("Invalid or expired reset token");

        var user = userRepository.findByPhoneNumber(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        keycloakService.updatePassword(user.getId(), request.password());
        redisTemplate.delete(RESET_TOKEN_PREFIX + request.resetToken());

        return new MessageResponse("Password reset successfully", true);
    }

    // ─────────────────────────────────────────────
    // CHANGE PASSWORD : user connecté
    //                  → vérifie passwords → update Keycloak
    // ─────────────────────────────────────────────
    public MessageResponse changePassword(String keycloakUserId, ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        keycloakService.updatePassword(keycloakUserId, request.newPassword());
        return new MessageResponse("Password changed successfully", true);
    }
}
