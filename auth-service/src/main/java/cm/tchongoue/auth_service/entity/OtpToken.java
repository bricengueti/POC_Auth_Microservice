package cm.tchongoue.auth_service.entity;

import org.springframework.data.annotation.Id;  // ← pas jakarta.persistence.Id
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("otp")
public class OtpToken {
    public OtpToken(String id, String userId, String code, OtpType type, long ttl) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.type = type;
        this.ttl = ttl;
    }
    public OtpToken() {

    }

    @Id
    private String id;         // {userId}:{type}

    public String getId() {
        return id;
    }

    public OtpToken setId(String id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public OtpToken setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getCode() {
        return code;
    }

    public OtpToken setCode(String code) {
        this.code = code;
        return this;
    }

    public OtpType getType() {
        return type;
    }

    public OtpToken setType(OtpType type) {
        this.type = type;
        return this;
    }

    public long getTtl() {
        return ttl;
    }

    public OtpToken setTtl(long ttl) {
        this.ttl = ttl;
        return this;
    }

    private String userId;
    private String code;
    private OtpType type;      // REGISTER, FORGOT_PASSWORD
    @TimeToLive
    private long ttl = 300;
}