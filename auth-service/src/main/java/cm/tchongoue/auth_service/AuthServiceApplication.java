package cm.tchongoue.auth_service;

import cm.tchongoue.auth_service.config.KeycloakProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(KeycloakProperties.class)
@EnableJpaRepositories(basePackages = "cm.tchongoue.auth_service.repository.jpa")
@EnableRedisRepositories(basePackages = "cm.tchongoue.auth_service.repository.redis")
public class AuthServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}
