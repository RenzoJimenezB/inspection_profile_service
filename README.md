# Inspection Profile Service

A profile management microservice for the InspectPro property inspection
platform. Handles user identities, professional profiles, credential
verification, and subscription-based feature access.

## Tech Stack

- **Language:** Kotlin 2+
- **Framework:** Spring Boot 3.5 + Spring Web MVC
- **Database:** PostgreSQL 16
- **Cache:** Redis 7
- **Migrations:** Flyway
- **Build:** Maven
- **Infrastructure:** Docker + Docker Compose

## Setup

### Prerequisites

- Docker Desktop

### Running the application

1. Clone the repository:

```bash
git clone https://github.com/RenzoJimenezB/inspection_profile_service.git
cd inspection_profile_service
```

2. Copy the environment file:

```bash
cp .env.example .env
```

3. Start the application:

```bash
docker compose up -d
```

4. Verify the application is running:

```bash
curl http://localhost:8080/actuator/health
```

The API will be available at `http://localhost:8080`

## API Documentation

Swagger UI is available at `http://localhost:8080/swagger-ui.html`

A Postman collection is available at `postman/collection.json` with a
complete end-to-end testing workflow documented in the collection description.

## Admin Credentials

| Field    | Value                |
|----------|----------------------|
| Email    | admin@inspectpro.com |
| Password | Admin123!            |

## Running Tests

Tests run automatically via the CI pipeline on every push. To run locally:

```bash
docker compose up -d db redis
./mvnw test
```

## Bonus Features

- OpenAPI/Swagger documentation
- GitHub Actions CI pipeline (build → test → docker build)
- Redis-based rate limiting (100 requests/minute per user)
- Structured JSON logging with correlation IDs