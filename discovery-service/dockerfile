FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# 1. Copie uniquement les fichiers nécessaires pour le cache
COPY pom.xml .
COPY src ./src

# 2. Build en deux étapes (meilleur cache)
RUN mvn dependency:go-offline -B
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]