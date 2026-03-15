package cm.tchongoue.auth_service.service;


import cm.tchongoue.auth_service.entity.OtpToken;
import cm.tchongoue.auth_service.entity.OtpType;
import cm.tchongoue.auth_service.exception.OtpException;
import cm.tchongoue.auth_service.repository.redis.OtpTokenRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private static final SecureRandom random = new SecureRandom();

    public OtpService(OtpTokenRepository otpTokenRepository) {
        this.otpTokenRepository = otpTokenRepository;
    }

    // ─────────────────────────────────────────────
    // Génère un code 6 chiffres → stocke Redis TTL 5min
    // ─────────────────────────────────────────────
    public String generate(String userId, OtpType type) {
        invalidate(userId, type);

        String code = String.format("%06d", random.nextInt(999999));

        OtpToken otp = new OtpToken();
        otp.setId(userId + ":" + type.name());
        otp.setUserId(userId);
        otp.setCode(code);
        otp.setType(type);

        otpTokenRepository.save(otp);
        return code;
    }

    // ─────────────────────────────────────────────
    // Vérifie le code → compare avec Redis
    // ─────────────────────────────────────────────
    public boolean verify(String userId, String code, OtpType type) {
        String id = userId + ":" + type.name();
        return otpTokenRepository.findById(id)
                .map(otp -> otp.getCode().equals(code))
                .orElseThrow(() -> new OtpException("Invalid or expired OTP"));
    }

    // ─────────────────────────────────────────────
    // Supprime l'OTP → après validation
    // ─────────────────────────────────────────────
    public void invalidate(String userId, OtpType type) {
        otpTokenRepository.deleteById(userId + ":" + type.name());
    }
}