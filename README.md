# BJMICROS: A Reactive Blackjack Microservices Platform

BJMICROS is a full-fledged, event-driven application that simulates a Blackjack game, built on a robust microservices architecture using Java, Spring Boot, and Apache Kafka. This project serves as a comprehensive showcase of modern cloud-native principles, including asynchronous communication, centralized security, advanced data persistence, and a professional, multi-layered testing strategy.

## Architecture Overview

The system is composed of four distinct microservices, an API Gateway, a message broker, and a shared database, all orchestrated via Docker Compose. The architecture is designed to separate concerns and handle different communication patterns: synchronous requests for user interactions and asynchronous events for system reactions.



* **API Gateway (Apache APISIX)**: The single entry point for all client requests. It handles routing, JWT-based security, and enriches requests with user data before forwarding them to the internal services.
* **Auth Service**: Manages user authentication and JWT generation.
* **User Service**: Handles user registration and manages user data, including balances.
* **Bets Service**: Manages the lifecycle of bets. It receives synchronous requests to place bets and consumes asynchronous events to settle them.
* **Game Core Service**: Contains the pure business logic for the Blackjack game itself. It orchestrates game flow and publishes events upon game completion.
* **Apache Kafka**: Acts as the central nervous system for asynchronous communication, decoupling the `game-core` service from the `bets` and `user` services.

## Tech Stack

### Backend & Frameworks
* **Java 17** & **Spring Boot 3**
* **Spring Security** (for JWT Authentication)
* **Spring Data JPA** / **Hibernate**

### Database
* **MySQL 8.0**
* **Hypersistence Utils**: For advanced persistence of complex objects as JSON.

### Messaging
* **Apache Kafka**: For event-driven communication.

### API Gateway
* **Apache APISIX**: For routing, security, and request transformation.

### Testing
* **JUnit 5** & **Mockito**: For unit and service-level testing.
* **Testcontainers**: For high-fidelity integration tests against real infrastructure (Kafka, MySQL) in Docker.
* **Awaitility**: For handling asynchronous assertions in tests.

### Build & Orchestration
* **Apache Maven**: For dependency management and building the project.
* **Docker** & **Docker Compose**: For containerizing and running the entire ecosystem locally.

## Key Features

* **Clean Microservice Architecture**: Each service has a single, well-defined responsibility (SRP).
* **Hybrid Communication Model**: Combines synchronous REST APIs for immediate user feedback with an asynchronous, event-driven workflow (using Kafka) for background processing and system resilience.
* **Centralized Security**: The API Gateway handles JWT validation, freeing the internal services from security concerns. It injects user context (`X-User-ID`) into requests.
* **Advanced Data Persistence**: The core game logic object, a pure POJO, is serialized into a single JSON column using Hypersistence Utils. This decouples the domain model from the persistence schema and simplifies database design.
* **Robust Multi-Layered Testing**:
    * **Service Tests**: Isolate and verify the business logic of each service using mocks.
    * **Integration Tests**: Use Testcontainers to validate the "wiring" between services and real infrastructure (database and Kafka), ensuring contracts are met.
    * **E2E Tests**: A dedicated container runs tests against the entire deployed system through the API Gateway.

## Getting Started

### Prerequisites
* Java 17+
* Apache Maven 3.8+
* Docker
* Docker Compose

### Running the Application
The entire ecosystem is orchestrated with Docker Compose. To build the images and start all the services, simply run the following command from the project's root directory:

```bash
docker-compose up --build
```

This command will:
1.  Build the Docker image for each microservice.
2.  Start the infrastructure containers (MySQL, Kafka).
3.  Start the application microservices and the APISIX gateway.
4.  The application will be accessible through the API Gateway on port `9080`.

## API Endpoints

All requests are routed through the API Gateway.

### Public Endpoints
* `POST /api/auth/login`: Authenticate a user and receive a JWT.
* `POST /api/v1/users/register`: Register a new user.

### Protected Endpoints (Requires JWT)
* `POST /api/v1/bets`: Place a new bet before starting a game.
* `POST /api/v1/games/start`: Start a new game using a `betId` from a previously placed bet.
* `POST /api/v1/games/{gameId}/hit`: The player requests another card.
* `POST /api/v1/games/{gameId}/stand`: The player ends their turn.
* `POST /api/v1/games/{gameId}/double`: The player doubles their bet.