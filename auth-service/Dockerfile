# Étape 1 : Build avec Maven
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copier pom.xml et sources
COPY pom.xml .
COPY src ./src

# Build du projet principal (sans tests pour accélérer)
RUN mvn clean package -Dmaven.test.skip=true

# Étape 2 : Image finale légère
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copier le jar généré
COPY --from=build /app/target/*.jar app.jar

# Exposer le port de ton AuthService (8081)
EXPOSE 8081


# Lancer l’application
ENTRYPOINT ["java", "-jar", "app.jar"]
