#!/bin/bash
# Attend que Keycloak soit prêt
echo "Waiting for Keycloak..."
until curl -sf http://keycloak:8080/health/ready > /dev/null 2>&1; do
  sleep 3
done

echo "Keycloak is ready. Getting admin token..."

# Récupère le token admin
TOKEN=$(curl -s -X POST http://keycloak:8080/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin&grant_type=password&client_id=admin-cli" \
  | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

echo "Creating client auth-service-client in master realm..."

# Crée le client
curl -s -X POST http://keycloak:8080/admin/realms/master/clients \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "auth-service-client",
    "name": "Auth Service Client",
    "protocol": "openid-connect",
    "publicClient": false,
    "secret": "poc-auth",
    "directAccessGrantsEnabled": true,
    "standardFlowEnabled": true,
    "serviceAccountsEnabled": true,
    "authorizationServicesEnabled": true,
    "redirectUris": ["http://localhost:8081/*"],
    "webOrigins": ["*"]
  }'

echo "Creating test-user in master realm..."

# Crée le user
curl -s -X POST http://keycloak:8080/admin/realms/master/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test-user",
    "enabled": true,
    "emailVerified": true,
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "credentials": [{"type": "password", "value": "password", "temporary": false}]
  }'

echo "Done!"