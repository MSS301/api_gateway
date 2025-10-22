# API Gateway

The **API Gateway** is built with **Spring Boot 3.2.5** and **Spring Cloud Gateway 2023.0.1**.
It serves as the **single entry point** for all microservices in the ecosystem, handling routing, load balancing, security, and cross-cutting concerns such as logging and monitoring.

---

## Table of Contents

* [Overview](#overview)
* [Tech Stack](#tech-stack)
* [Prerequisites](#prerequisites)
* [Getting Started](#getting-started)
* [Running the Application](#running-the-application)
* [Configuration](#configuration)
* [Contributing](#contributing)
* [License](#license)

---

## Overview

The API Gateway provides the following core functionalities:

* **Routing** requests to backend microservices.
* **Load Balancing** (when integrated with service discovery like Eureka/Consul).
* **Security**: authentication, authorization, and rate limiting.
* **Cross-Cutting Concerns**: logging, tracing, monitoring.

---

## Tech Stack

* **Java 21** – Core programming language.
* **Spring Boot 3.5.6** – Application framework.
* **Spring Cloud Gateway 2024.0.1** – For routing and filtering.
* **Lombok 1.18.30** – Reduces boilerplate code.
* **Maven** – Build tool and dependency management.

---

## Prerequisites

Ensure the following are installed:

* **Java 21**
* **Maven 3.9+**
* **Docker** (optional, for containerized deployment)

---

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/your-org/api-gateway.git
cd api-gateway
```

### Install Dependencies

```bash
mvn clean install
```

### Configure Environment (Optional)

Copy `.env.example` → `.env` instruction if env not work: link (https://drive.google.com/drive/folders/1gvLupFafEiIawT8r3zC2EZoUc3K0NXjB?usp=sharing).

---



## Running the Application

### Build

```bash
mvn clean package -DskipTests
```

### Run

```bash
mvn spring-boot:run
```

### Access

The gateway runs at [http://localhost:8080](http://localhost:8080) (configurable via `application.yml`).

### Docker (Optional)

```bash
docker build -t api-gateway .
docker run -p 8080:8080 api-gateway
```

---

## Configuration

The API Gateway configuration is managed via `application.yml`.

Example:

```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      routes:
        - id: auth_service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - AuthenticationFilter
            - RateLimitFilter
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true

# Security settings (example)
jwt:
  secret: your_jwt_secret_key
  access-token-expiration: 3600000 # 1 hour
```

* **Routes**: Define routing rules for microservices (e.g., `auth-service`).
* **Service Discovery**: Enable with `spring.cloud.gateway.discovery.locator.enabled`.
* **Security**: Configure JWT and custom filters (authentication, rate limiting).

---

## Contributing

1. Fork the repository.
2. Create a feature branch:

   ```bash
   git checkout -b feature/your-feature
   ```
3. Commit changes:

   ```bash
   git commit -m "Add your feature"
   ```
4. Push to origin:

   ```bash
   git push origin feature/your-feature
   ```
5. Open a Pull Request.

Please ensure contributions follow project standards and include tests.

---

## License

This project is licensed under the **MIT License**.
See [LICENSE](LICENSE) for details.
