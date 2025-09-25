# API Gateway

## Overview
This project is an **API Gateway** built with **Spring Boot 3.2.5** and **Spring Cloud Gateway 2023.0.1**.  
It serves as the single entry point for all microservices in the system, handling:

- **Routing** requests to backend microservices
- **Load balancing** (if combined with service discovery like Eureka/Consul)
- **Security** (authentication, authorization, rate limiting)
- **Cross-cutting concerns** such as logging, tracing, monitoring

---

## Tech Stack
- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Cloud Gateway 2024.0.1**
- **Lombok 1.18.30**
- Build tool: **Maven**

---

## Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- (Optional) Docker if you want to containerize

### Run locally
```bash
# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run
