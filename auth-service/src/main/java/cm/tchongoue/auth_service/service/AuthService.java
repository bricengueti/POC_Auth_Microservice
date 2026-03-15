package cm.tchongoue.auth_service.service;

import cm.tchongoue.auth_service.DTO.requestDTO.*;
import cm.tchongoue.auth_service.DTO.responseDTO.AuthResponse;
import cm.tchongoue.auth_service.DTO.responseDTO.MessageResponse;
import cm.tchongoue.auth_service.DTO.responseDTO.OtpVerifyResponse;
import cm.tchongoue.auth_service.DTO.responseDTO.RegisterInitResponse;
import cm.tchongoue.auth_service.entity.OtpType;
import cm.tchongoue.auth_service.entity.User;
import cm.tchongoue.auth_service.exception.AuthException;
import cm.tchongoue.auth_service.exception.OtpException;
import cm.tchongoue.auth_service.exception.UserAlreadyExistsException;
import cm.tchongoue.auth_service.exception.UserNotFoundException;
import cm.tchongoue.auth_service.mapper.UserMapper;
import cm.tchongoue.auth_service.repository.jpa.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final OtpService otpService;
    private final SmsService smsService;
    private final KeycloakService keycloakService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String TEMP_PREFIX = "temp:user:";
    private static final String OTP_VERIFIED_PREFIX = "otp:verified:";
    private static final long TEMP_TTL = 30;

    public AuthService(OtpService otpService,
                       SmsService smsService,
                       KeycloakService keycloakService,
                       UserRepository userRepository,
                       UserMapper userMapper,
                       RedisTemplate<String, String> redisTemplate) {
        this.otpService = otpService;
        this.smsService = smsService;
        this.keycloakService = keycloakService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    // ─────────────────────────────────────────────
    // STEP 1 : entre son numéro → génère OTP → envoie SMS
    // ─────────────────────────────────────────────
    public RegisterInitResponse initRegister(RegisterInitRequest request) {
//        String userId = request.phoneNumber().replaceAll("[^0-9]", "");

        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new UserAlreadyExistsException("Phone number already registered");
        }

        String code = otpService.generate(request.phoneNumber(), OtpType.REGISTER);
        smsService.sendOtp(request.phoneNumber(), code);

        return new RegisterInitResponse(request.phoneNumber(), "OTP sent successfully", 300);
    }

    // ─────────────────────────────────────────────
    // STEP 2 : confirme OTP → marque vérifié dans Redis
    // ─────────────────────────────────────────────
    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        boolean valid = otpService.verify(request.userId(), request.code(), OtpType.REGISTER);
        if (!valid) throw new OtpException("Invalid or expired OTP");

        otpService.invalidate(request.userId(), OtpType.REGISTER);

        redisTemplate.opsForValue().set(
                OTP_VERIFIED_PREFIX + request.userId(),
                "true",
                TEMP_TTL, TimeUnit.MINUTES
        );

        return new OtpVerifyResponse(request.userId(), true, "OTP verified successfully");
    }

    // ─────────────────────────────────────────────
    // STEP 3 : entre ses infos → stocke dans Redis
    // ─────────────────────────────────────────────
    public MessageResponse completeProfile(RegisterProfileRequest request) {
        String verified = redisTemplate.opsForValue().get(OTP_VERIFIED_PREFIX + request.userId());
        if (verified == null) throw new OtpException("OTP not verified or session expired");

        redisTemplate.opsForValue().set(TEMP_PREFIX + request.userId() + ":email", request.email(), TEMP_TTL, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(TEMP_PREFIX + request.userId() + ":firstName", request.firstName(), TEMP_TTL, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(TEMP_PREFIX + request.userId() + ":lastName", request.lastName(), TEMP_TTL, TimeUnit.MINUTES);

        return new MessageResponse("Profile saved successfully", true);
    }

    // ─────────────────────────────────────────────
    // STEP 4 : entre password → récupère profil Redis
    //          → crée Keycloak → crée DB → retourne tokens
    // ─────────────────────────────────────────────
    public AuthResponse completeRegister(RegisterCompleteRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new AuthException("Passwords do not match", 400);
        }

        String verified = redisTemplate.opsForValue().get(OTP_VERIFIED_PREFIX + request.userId());
        if (verified == null) throw new OtpException("OTP not verified or session expired");

        String email = redisTemplate.opsForValue().get(TEMP_PREFIX + request.userId() + ":email");
        String firstName = redisTemplate.opsForValue().get(TEMP_PREFIX + request.userId() + ":firstName");
        String lastName = redisTemplate.opsForValue().get(TEMP_PREFIX + request.userId() + ":lastName");

        if (email == null || firstName == null || lastName == null) {
            throw new AuthException("Profile not found, please restart registration", 400);
        }

        RegisterProfileRequest profile = new RegisterProfileRequest(
                request.userId(), firstName, lastName, email
        );

        String keycloakId = keycloakService.createUser(profile, request);

        User user = userMapper.toEntity(profile, keycloakId);
        user.setPhoneNumber(request.userId());
        userRepository.save(user);

        redisTemplate.delete(OTP_VERIFIED_PREFIX + request.userId());
        redisTemplate.delete(TEMP_PREFIX + request.userId() + ":email");
        redisTemplate.delete(TEMP_PREFIX + request.userId() + ":firstName");
        redisTemplate.delete(TEMP_PREFIX + request.userId() + ":lastName");

        AuthResponse auth = keycloakService.authenticate(request.userId(), request.password());
        User savedUser = userRepository.findById(keycloakId).orElseThrow();

        return new AuthResponse(
                auth.accessToken(),
                auth.refreshToken(),
                auth.expiresIn(),
                auth.tokenType(),
                userMapper.toResponse(savedUser)
        );
    }

    // ─────────────────────────────────────────────
    // LOGIN : phoneNumber + password → tokens
    // ─────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {
        // normalise le phoneNumber comme à l'inscription
//        String userId = request.phoneNumber().replaceAll("[^0-9]", "");

        AuthResponse auth = keycloakService.authenticate(request.phoneNumber(), request.password());

        User user = userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new AuthResponse(
                auth.accessToken(),
                auth.refreshToken(),
                auth.expiresIn(),
                auth.tokenType(),
                userMapper.toResponse(user)
        );
    }

    // ─────────────────────────────────────────────
    // REFRESH : refreshToken → nouvel accessToken
    // ─────────────────────────────────────────────
    public AuthResponse refresh(RefreshTokenRequest request) {
        return keycloakService.refreshToken(request.refreshToken());
    }

    // ─────────────────────────────────────────────
    // LOGOUT : révoque refreshToken dans Keycloak
    // ─────────────────────────────────────────────
    public MessageResponse logout(String refreshToken) {
        keycloakService.logout(refreshToken);
        return new MessageResponse("Logged out successfully", true);
    }
}
