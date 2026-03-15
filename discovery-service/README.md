# 📡 discovery-service

`discovery-service` est le **registre de services Eureka** utilisé dans l’architecture microservices de ce projet. Il permet aux microservices de **s’enregistrer dynamiquement** et de **découvrir** les autres services via Spring Cloud.

---

## 🎯 Objectifs

- Centraliser la **découverte des services**
- Réduire le **couplage statique** entre les microservices
- Permettre le **scalabilité dynamique** des instances
- Offrir une **interface web** pour visualiser les services disponibles

---

## 🧠 Fonctionnement

Ce projet utilise **Spring Cloud Netflix Eureka Server**, configuré comme **registre Eureka principal**. Les services clients s’y connectent via leur configuration et se réenregistrent périodiquement.

---

## 🌍 Interface Eureka

Une interface web est disponible pour visualiser les services enregistrés.

- URL par défaut : [http://localhost:8761](http://localhost:8761)
- Elle affiche :
  - Les services actifs
  - Le nombre d’instances par service
  - Les métadonnées comme le port, l'IP, etc.

---

## ⚙️ Technologies utilisées

- **Java 17**
- **Spring Boot 3.x**
- **Spring Cloud Eureka Server**
- **Maven**

---

## 🧩 Configuration minimale (`application.yml`)

```yaml
server:
  port: 8761

spring:
  application:
    name: discovery-service

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false






discovery-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── tnbtech/
│   │   │           └── discoveryservice/
│   │   │               ├── DiscoveryServiceApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   └── test/
│       └── java/
│           └── com/
│               └── tnbtech/
│                   └── discoveryservice/
│                       └── DiscoveryServiceApplicationTests.java
├── .gitignore
├── mvnw / mvnw.cmd
├── pom.xml
└── README.md
