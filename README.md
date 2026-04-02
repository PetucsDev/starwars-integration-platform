# Star Wars Integration Platform

REST API built with **Java 21 + Spring Boot 3.3** that integrates with the public [Star Wars API (swapi.tech)](https://www.swapi.tech/documentation).

## Features

- **JWT Authentication** — register and login to obtain a Bearer token
- **People, Films, Starships, Vehicles** — paginated listing and detail endpoints
- **Filtering** — by ID (path variable) and by name (query parameter)
- **Input validation** — `page` and `limit` params reject values < 1 with a `400 Bad Request`
- **Response caching** — Caffeine in-memory cache (30 min TTL) on all SWAPI calls; repeated requests are served without hitting the external API
- **Health endpoint** — `/actuator/health` and `/actuator/info` publicly accessible (no token required)
- **Swagger UI** — interactive API docs at `/swagger-ui.html`
- **H2 Console** — in-memory database UI at `/h2-console`
- **66 automated tests** — unit tests (JwtTokenProvider, all 4 Services, AuthService) + integration tests (Auth flow, People/Films/Starships/Vehicles endpoints with WireMock, input validation)

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21 |
| Maven | 3.9+ |

---

## Running locally

```bash
# Clone the repo
git clone <repo-url>
cd starwars-integration-platform

# Run (Maven wrapper not included — use your local mvn)
mvn spring-boot:run
```

The app starts on **http://localhost:8080**.

> If your default Java is not 21, set `JAVA_HOME` first:
> ```bash
> export JAVA_HOME=/path/to/java21
> mvn spring-boot:run
> ```

---

## Running tests

```bash
mvn test
```

Expected output:
```
Tests run: 66, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Test coverage summary

| Class | Type | Tests |
|-------|------|-------|
| `JwtTokenProviderTest` | Unit | 7 |
| `AuthServiceTest` | Unit | 6 |
| `PeopleServiceTest` | Unit | 5 |
| `StarshipsServiceTest` | Unit | 5 |
| `VehiclesServiceTest` | Unit | 5 |
| `FilmsServiceTest` | Unit | 4 |
| `AuthControllerIntegrationTest` | Integration | 7 |
| `PeopleControllerIntegrationTest` | Integration (WireMock) | 7 |
| `FilmsControllerIntegrationTest` | Integration (WireMock) | 5 |
| `StarshipsControllerIntegrationTest` | Integration (WireMock) | 5 |
| `VehiclesControllerIntegrationTest` | Integration (WireMock) | 5 |
| `JwtAuthenticationFilterTest` | Integration | 4 |
| `StarWarsApplicationTests` | Smoke | 1 |

---

## API Quick Start

### 1. Register a user

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"jedi","password":"force123","email":"jedi@galaxy.com"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresAt": "2026-01-01T12:00:00.000+00:00",
  "username": "jedi"
}
```

### 2. Login (if already registered)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"jedi","password":"force123"}'
```

### 3. Use the token in all other requests

```bash
export TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# List people (paginated)
curl http://localhost:8080/api/people?page=1&limit=10 \
  -H "Authorization: Bearer $TOKEN"

# Filter by name
curl "http://localhost:8080/api/people?name=luke" \
  -H "Authorization: Bearer $TOKEN"

# Get person by ID
curl http://localhost:8080/api/people/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Endpoints

All endpoints except `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**` and `/h2-console/**` require a valid JWT token.

### Authentication

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login and get a JWT token |

### People

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/people` | Paginated list (`?page=1&limit=10&name=luke`) |
| `GET` | `/api/people/{id}` | Full details for a character by ID |

### Films

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/films` | Paginated list (`?page=1&limit=6&title=hope`) |
| `GET` | `/api/films/{id}` | Full details for a film by ID |

### Starships

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/starships` | Paginated list (`?page=1&limit=10&name=falcon`) |
| `GET` | `/api/starships/{id}` | Full details for a starship by ID |

### Vehicles

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/vehicles` | Paginated list (`?page=1&limit=10&name=speeder`) |
| `GET` | `/api/vehicles/{id}` | Full details for a vehicle by ID |

---

## Swagger UI

Open **http://localhost:8080/swagger-ui.html** in your browser.

1. Click **Authorize** (the padlock icon)
2. Paste your JWT token (without the `Bearer ` prefix)
3. All protected endpoints are now unlocked — try them directly from the browser

---

## H2 Console (dev)

Open **http://localhost:8080/h2-console**

| Setting | Value |
|---------|-------|
| JDBC URL | `jdbc:h2:mem:starwarsdb` |
| Username | `sa` |
| Password | *(empty)* |

---

## Health & Info endpoints

No token required — useful for deployment platforms and monitoring:

```bash
GET /actuator/health
{
  "status": "UP",
  "components": { "db": { "status": "UP" }, "diskSpace": { "status": "UP" } }
}

GET /actuator/info
{
  "app": {
    "name": "Star Wars Integration Platform",
    "version": "1.0.0",
    "description": "REST API integrating with swapi.tech"
  }
}
```

---

## Response caching

All SWAPI calls are cached with **Caffeine** (in-memory, 30-minute TTL, max 500 entries).

| Cache name | Keys cached |
|------------|-------------|
| `people` | per `page/limit/name` combination + per `id` |
| `films` | per `page/limit/title` combination + per `id` |
| `starships` | per `page/limit/name` combination + per `id` |
| `vehicles` | per `page/limit/name` combination + per `id` |

Since Star Wars data is essentially static, the first request fetches from swapi.tech and subsequent requests within the 30-minute window are served from memory — no external HTTP call is made.

---

## Docker

```bash
# Build the image
docker build -t starwars-platform .

# Run (JWT_SECRET is optional — a default is used if omitted)
docker run -p 8080:8080 \
  -e JWT_SECRET=<your-base64-secret> \
  starwars-platform
```

The app starts on **http://localhost:8080**.

---

## Environment variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `JWT_SECRET` | No | *(built-in dev key)* | Base64-encoded HMAC-SHA256 key (≥ 32 bytes). **Always override in production.** |
| `JWT_EXPIRATION_MS` | No | `3600000` | Token lifetime in milliseconds (default: 1 hour). |

> **Generating a secure secret:**
> ```bash
> openssl rand -base64 32
> ```

---

## Architecture

```
com.starwars
├── config/          Security, RestClient, OpenAPI, Cache configuration
├── auth/            Register/Login, User entity, JWT response DTOs
├── security/        JwtTokenProvider, JwtAuthenticationFilter
├── swapi/
│   ├── client/      SwapiClient (RestClient wrapper with error handling)
│   ├── dto/swapi/   Raw SWAPI response shapes (never exposed to callers)
│   ├── dto/api/     Clean DTOs returned to API consumers
│   ├── service/     Business logic + mapping (People, Films, Starships, Vehicles)
│   ├── controller/  REST controllers (one per resource type)
│   └── util/        PaginationUtils — shared pagination + parseLong helpers
└── exception/       GlobalExceptionHandler, typed exceptions, ErrorResponse
```

**Key design decisions:**

- **RestClient** (Spring 6.1) — modern synchronous HTTP client, no reactive overhead needed
- **H2 in-memory** — zero-config persistence for users; swap for PostgreSQL via `application.yml` only
- **SWAPI DTOs isolated** — raw SWAPI shapes never leak to API consumers; mapping is done in services
- **Stateless JWT** — no server-side session; token validated on every request via `OncePerRequestFilter`
- **`@Validated` + `@Min(1)`** — query params `page` and `limit` are validated at the controller level; violations return `400 Bad Request`
- **URL encoding** — name/title search params are encoded via `UriUtils` before being appended to the SWAPI URL, preventing breakage with multi-word names (e.g. "Millennium Falcon")
- **WireMock in tests** — stubbed HTTP server makes integration tests deterministic without network dependency; each resource type (People, Films, Starships, Vehicles) has its own WireMock port to avoid context conflicts

---

## Running with a different database (optional)

Replace the H2 config in `application.yml` with:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/starwars
    username: your_user
    password: your_password
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
```

Add the PostgreSQL driver to `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```