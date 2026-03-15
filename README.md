# 🔐 POC Auth Microservice

> Architecture microservices complète pour la gestion de l'authentification en microService avec Spring Boot, Keycloak et Redis.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-brightgreen)
![Keycloak](https://img.shields.io/badge/Keycloak-22.0.1-blue)
![Redis](https://img.shields.io/badge/Redis-7-red)
![Docker](https://img.shields.io/badge/Docker-28.5.0-blue)
![Java](https://img.shields.io/badge/Java-17-orange)

---

## 📋 Table des matières

- [Présentation](#-présentation)
- [Architecture](#-architecture)
- [Structure du projet](#-structure-du-projet)
- [Services](#-services)
- [Flux d'authentification](#-flux-dauthentification)
- [Lancement](#-lancement)
- [Accès aux services](#-accès-aux-services)
- [Profils](#-profils)
- [Logs](#-logs)
- [Sécurité](#-sécurité)
- [Évolutions prévues](#-évolutions-prévues)

---

## 🎯 Présentation

Ce POC implémente une architecture microservices pour la gestion de l'authentification et des utilisateurs. Il démontre les bonnes pratiques avec Spring Boot 3, Spring Cloud, Keycloak et Redis dans un environnement conteneurisé.

### Stack technique

| Technologie | Version | Rôle |
|-------------|---------|------|
| Spring Boot | 3.2.4 | Framework principal |
| Spring Cloud | 2023.0.1 | Gateway, Eureka |
| Keycloak | 22.0.1 | Serveur d'identité (IAM) |
| Redis | 7 | Cache / Stockage OTP |
| H2 Database | Runtime | Base de données embarquée |
| Docker | 28.5.0 | Containerisation |
| Java | 17 | Langage |

---

## 🏗 Architecture

```
                        ┌─────────────────┐
                        │     Client      │
                        └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │ Gateway Service │  :8888
                        │  (JWT Validation│
                        │   + Routing)    │
                        └────────┬────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
     ┌────────▼────────┐ ┌───────▼───────┐ ┌───────▼────────┐
     │  Auth Service   │ │   Discovery   │ │   Keycloak     │
     │     :8081       │ │   Service     │ │    :8080       │
     │                 │ │   (Eureka)    │ │                │
     │  ┌───────────┐  │ │    :8761      │ └────────────────┘
     │  │   Redis   │  │ └───────────────┘
     │  │   :6379   │  │
     │  └───────────┘  │
     │  ┌───────────┐  │
     │  │  H2 DB    │  │
     │  └───────────┘  │
     └─────────────────┘
```

### Flux d'une requête

```
Client → Gateway (valide JWT) → injecte X-User-Id → route via Eureka → Microservice
```

---

## 📁 Structure du projet

```
POC_auth_microservice/
├── docker-compose.yml              # Orchestration globale
├── .env                            # Variables d'environnement
│
├── auth-service/
│   ├── src/main/
│   │   ├── java/cm/tchongoue/auth_service/
│   │   │   ├── config/             # KeycloakProperties, RestTemplate, Swagger
│   │   │   ├── controller/         # AuthController, PasswordController
│   │   │   ├── dto/
│   │   │   │   ├── request/        # Records RequestDTO
│   │   │   │   └── response/       # Records ResponseDTO
│   │   │   ├── entity/             # User, OtpToken, OtpType
│   │   │   ├── exception/          # Exceptions custom + GlobalExceptionHandler
│   │   │   ├── mapper/             # UserMapper
│   │   │   ├── repository/
│   │   │   │   ├── jpa/            # UserRepository
│   │   │   │   └── redis/          # OtpTokenRepository
│   │   │   └── service/            # AuthService, KeycloakService, OtpService...
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-local.properties
│   │       ├── application-docker.properties
│   │       └── logback-spring.xml
│   ├── init-keycloak.sh            # Script init client Keycloak
│   └── Dockerfile
│
├── discovery-service/
│   ├── src/
│   └── Dockerfile
│
└── gatwayService/
    ├── src/main/java/cm/tchongoue/gateway_service/
    │   ├── config/                 # SecurityConfig, SwaggerConfig
    │   └── filter/                 # AuthenticationFilter
    └── Dockerfile
```

---

## 🔧 Services

### Auth Service (:8081)
Microservice dédié à l'authentification et à la gestion des utilisateurs. Communique avec Keycloak via l'Admin Client REST.

### Gateway Service (:8888)
Point d'entrée unique. Valide les tokens JWT via la clé publique Keycloak (JWKS) et injecte les informations utilisateur dans les headers.

### Discovery Service (:8761)
Eureka Server. Registre de tous les microservices pour la découverte dynamique.

### Keycloak (:8080)
Serveur d'identité. Gère les tokens JWT, les utilisateurs et les clients OAuth2.

### Redis (:6379)
Stockage temporaire des OTP, sessions d'inscription et reset tokens.

---

## 🔄 Flux d'authentification

### Inscription (4 étapes)

```
1. POST /api/auth/auth/register/init
   → { phoneNumber }
   ← { userId, message, otpExpiresIn }

2. POST /api/auth/auth/register/verify-otp
   → { userId, code }
   ← { userId, verified, message }

3. POST /api/auth/auth/register/profile
   → { userId, firstName, lastName, email }
   ← { message, success }

4. POST /api/auth/auth/register/complete
   → { userId, password, confirmPassword }
   ← { accessToken, refreshToken, expiresIn, user }
```

### Connexion

```
POST /api/auth/auth/login
→ { phoneNumber, password }
← { accessToken, refreshToken, expiresIn, user }
```

### Mot de passe oublié (3 étapes)

```
1. POST /api/auth/auth/password/forgot
   → { phoneNumber }
   ← { message, success }

2. POST /api/auth/auth/password/forgot/verify-otp
   → { userId, code }
   ← { resetToken, verified, message }

3. POST /api/auth/auth/password/reset
   → { resetToken, password, confirmPassword }
   ← { message, success }
```

### Changement de mot de passe *(JWT requis)*

```
POST /api/auth/auth/password/change
Header: X-User-Id: <keycloakUserId>
→ { currentPassword, newPassword, confirmPassword }
← { message, success }
```

### Stockage Redis

```
otp:{userId}:{type}           TTL: 300s   # Code OTP
otp:verified:{userId}         TTL: 30min  # OTP validé
temp:user:{userId}:email      TTL: 30min  # Profil temporaire
temp:user:{userId}:firstName  TTL: 30min
temp:user:{userId}:lastName   TTL: 30min
reset:token:{uuid}            TTL: 15min  # Token reset password
```

---

## 🚀 Lancement

### Prérequis

- Docker 20.10+
- Docker Compose 2.0+
- Java 17 *(développement local)*

### Variables d'environnement

Créez un fichier `.env` à la racine :

```properties
SPRING_PROFILES_ACTIVE=docker
REDIS_HOST=redis
REDIS_PORT=6379
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KEYCLOAK_URL=http://keycloak:8080
KEYCLOAK_CLIENT_SECRET=my-secret-value
KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/master
EUREKA_URL=http://discovery-service:8761/eureka/
SERVER_PORT=8888
```

### Démarrer tous les services

```bash
docker compose --env-file .env up --build -d
```

Ordre de démarrage automatique grâce aux healthchecks :

```
keycloak + redis
    ↓
keycloak-init + discovery-service
    ↓
auth-service
    ↓
gateway-service
```

### Développement local

```bash
# Auth Service
cd auth-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Gateway Service
cd gatwayService
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Commandes utiles

```bash
# Voir l'état des containers
docker compose ps

# Logs d'un service
docker compose logs -f auth-service

# Arrêter et supprimer les volumes
docker compose --env-file .env down -v

# Rebuild un service
docker compose --env-file .env up --build -d auth-service

# Vérifier un healthcheck
docker inspect auth-service --format='{{json .State.Health}}'
```

---

## 🌐 Accès aux services

| Service | URL | Credentials |
|---------|-----|-------------|
| Keycloak Admin | http://localhost:8080 | admin / admin |
| Eureka Dashboard | http://localhost:8761 | - |
| Swagger Gateway | http://localhost:8888/swagger-ui.html | Bearer JWT |
| Swagger Auth | http://localhost:8081/api/auth/swagger-ui/index.html | Bearer JWT |
| H2 Console | http://localhost:8081/api/auth/h2-console | SA / (vide) |

---

## ⚙️ Profils

| Profil | Fichier | Usage |
|--------|---------|-------|
| *(commun)* | `application.properties` | Config partagée |
| `local` | `application-local.properties` | Développement sans Docker |
| `docker` | `application-docker.properties` | Containers Docker |


---

## 📊 Logs

Les logs sont générés en **JSON Logstash** pour une compatibilité ELK Stack :

```json
{
  "@timestamp": "2026-03-14T06:12:22.269Z",
  "level": "INFO",
  "logger_name": "cm.tchongoue.auth_service.service.SmsService",
  "message": "[SMS] Sending OTP 131329 to +237612345678",
  "service": "auth-service",
  "env": "docker"
}
```

| Logger | Local | Docker | Prod |
|--------|-------|--------|------|
| `cm.tchongoue` | DEBUG | DEBUG | INFO |
| `org.springframework.web` | DEBUG | INFO | WARN |
| `org.springframework.data.redis` | DEBUG | INFO | WARN |

Fichiers de logs : `logs/auth-service.log`, `logs/gateway-service.log`
Rotation quotidienne — 30 jours d'historique.

---

## 🔒 Sécurité

### Validation JWT

```
Client → Bearer Token → Gateway
                           ↓
                   Clé publique Keycloak (JWKS)
                   Validation locale (sans appel Keycloak)
                           ↓
                   Headers enrichis → Microservice
                   X-User-Id, X-User-Email, X-User-Username
```

### Routes Gateway

| Route | Accès |
|-------|-------|
| `/api/auth/auth/register/**` | ✅ Public |
| `/api/auth/auth/login` | ✅ Public |
| `/api/auth/auth/refresh` | ✅ Public |
| `/api/auth/auth/password/forgot/**` | ✅ Public |
| `/api/auth/auth/password/reset` | ✅ Public |
| `/api/auth/auth/logout` | 🔒 JWT requis |
| `/api/auth/auth/password/change` | 🔒 JWT requis |
| Toutes les autres routes | 🔒 JWT requis |

### Bonnes pratiques

- Les secrets ne sont jamais dans le code source
- Les fichiers `.env` sont exclus du git (`.gitignore`)
- Les mots de passe sont gérés exclusivement par Keycloak
- Les OTP expirent automatiquement via le TTL Redis
- CSRF désactivé (API REST stateless)

---

## 🔮 Évolutions prévues

- [ ] Intégration provider SMS réel (Twilio / Vonage / Orange SMS)
- [ ] Stack ELK pour la centralisation des logs
- [ ] Prometheus + Grafana pour les métriques
- [ ] Migration PostgreSQL en production
- [ ] Implémentation du `transfer-service`
- [ ] Tests unitaires et d'intégration
- [ ] Pipeline CI/CD avec GitHub Actions

---

## 👤 Auteur

**Brice Tchongoue Ngueti**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-brice--tchongoue-blue)](https://www.linkedin.com/in/brice-tchongoue)
[![Email](https://img.shields.io/badge/Email-tchongouebricengueti%40gmail.com-red)](mailto:tchongouebricengueti@gmail.com)