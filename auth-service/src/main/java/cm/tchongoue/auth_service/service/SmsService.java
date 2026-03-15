package cm.tchongoue.auth_service.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    // ─────────────────────────────────────────────
    // TODO : brancher Twilio / Vonage / Orange SMS
    // En attendant → log le code en dev
    // ─────────────────────────────────────────────
    public void sendOtp(String phoneNumber, String code) {
        log.info("[SMS] Sending OTP {} to {}", code, phoneNumber);
    }
}
