// OtpTokenRepository.java
package cm.tchongoue.auth_service.repository.redis;

import cm.tchongoue.auth_service.entity.OtpToken;
import cm.tchongoue.auth_service.entity.OtpType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpTokenRepository extends CrudRepository<OtpToken, String> {
    OtpToken findByUserIdAndType(String userId, OtpType type);
    void deleteByUserId(String userId);
}