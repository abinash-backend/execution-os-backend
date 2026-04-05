# Execution OS Backend

`execution-os-backend` is a Spring Boot 3 REST API for user authentication, task tracking, daily execution logging, streak calculation, and consistency leaderboards.

The codebase is organized as a layered monolith with `controller`, `service`, `repository`, `entity`, `dto`, and shared `common` packages.

## Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL
- JWT (`jjwt`)
- Maven
- Lombok

## What The Application Does

- Registers users with a BCrypt-hashed password
- Authenticates users and returns a JWT token
- Creates tasks scoped to the authenticated user
- Lists tasks with optional `status` and `priority` filters
- Records one execution log per task per day
- Calculates current streak, longest streak, and consistency percentage
- Builds a leaderboard from average task consistency per user

## Project Structure

```text
src/main/java/com/executionos
|-- auth
|   |-- controller
|   |-- entity
|   `-- repository
|-- common
|   |-- config
|   |-- exception
|   |-- security
|   `-- util
|-- execution
|   |-- controller
|   |-- dto
|   |-- entity
|   |-- repository
|   `-- service
`-- task
    |-- controller
    |-- dto
    |-- entity
    |-- repository
    `-- service
```

## Configuration

Application settings are in [`src/main/resources/application.yaml`](/C:/Users/Abinash/Downloads/execution-os-backend/src/main/resources/application.yaml).

Default local configuration:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/execution_os
    username: execution_user
    password: secure123
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## Local Setup

### Prerequisites

- JDK 17
- PostgreSQL

### Database

Create the database and user used by `application.yaml`, or change the config before starting:

```sql
CREATE DATABASE execution_os;
CREATE USER execution_user WITH PASSWORD 'secure123';
GRANT ALL PRIVILEGES ON DATABASE execution_os TO execution_user;
```

### Run

```powershell
.\mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080`.

### Test

```powershell
.\mvnw.cmd test
```

## Authentication

All endpoints except `/api/v1/auth/**` require:

```http
Authorization: Bearer <jwt-token>
```

The JWT subject is the user UUID and the token lifetime is 24 hours.

## Domain Model

### User

- `id: UUID`
- `email: String`
- `password: String`
- `createdAt: LocalDateTime`

### Task

- `id: UUID`
- `title: String`
- `description: String | null`
- `frequency: DAILY | WEEKLY | MONTHLY`
- `deadline: LocalDate | null`
- `priority: LOW | MEDIUM | HIGH`
- `status: PENDING | DONE | FAILED`
- `createdAt: LocalDateTime`

### Execution Log

- `id: UUID`
- `task: Task`
- `date: LocalDate`
- `status: DONE | MISSED`

The database enforces one execution log per `task_id` and `date`.

## API

### Auth

#### `POST /api/v1/auth/register`

Request body:

```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

Response:

```text
User registered
```

#### `POST /api/v1/auth/login`

Request body:

```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

Response:

```text
<jwt-token>
```

### Tasks

#### `POST /api/v1/tasks`

Creates a task for the authenticated user.

Request body:

```json
{
  "title": "Write daily report",
  "description": "Submit progress report before 6 PM",
  "frequency": "DAILY",
  "deadline": "2026-04-30",
  "priority": "HIGH"
}
```

Notes:

- `title` is required and trimmed before save
- `frequency` is required
- `priority` defaults to `MEDIUM` if omitted
- `status` is always created as `PENDING`
- duplicate titles are blocked per user, case-insensitively

Response:

```json
{
  "id": "task-uuid",
  "title": "Write daily report",
  "description": "Submit progress report before 6 PM",
  "frequency": "DAILY",
  "deadline": "2026-04-30",
  "priority": "HIGH",
  "status": "PENDING",
  "createdAt": "2026-04-06T10:00:00"
}
```

#### `GET /api/v1/tasks`

Returns the authenticated user's tasks.

Optional query parameters:

- `status=PENDING|DONE|FAILED`
- `priority=LOW|MEDIUM|HIGH`

Example:

```http
GET /api/v1/tasks?status=PENDING&priority=HIGH
```

#### `GET /api/v1/tasks/{taskId}/streak`

Returns streak metrics for a task:

```json
{
  "taskId": "task-uuid",
  "currentStreak": 3,
  "longestStreak": 7,
  "consistency": 75.0
}
```

#### `GET /api/v1/tasks/leaderboard?page=0&size=5`

Returns a consistency leaderboard:

```json
[
  {
    "userId": "user-uuid",
    "consistencyScore": 82.5
  }
]
```

### Execution Logs

#### `POST /api/v1/tasks/{taskId}/execution`

Creates today's execution log for the task owned by the authenticated user.

Request body:

```json
{
  "status": "DONE"
}
```

Allowed values:

- `DONE`
- `MISSED`

Response:

```json
{
  "date": "2026-04-06",
  "status": "DONE"
}
```

#### `GET /api/v1/tasks/{taskId}/execution`

Returns all execution logs for the task owned by the authenticated user.

Response:

```json
[
  {
    "date": "2026-04-04",
    "status": "DONE"
  },
  {
    "date": "2026-04-05",
    "status": "MISSED"
  }
]
```

## Error Handling

Global exception handling returns a structured payload for common failures:

```json
{
  "status": 400,
  "message": "Invalid request payload",
  "method": "POST",
  "path": "/api/v1/tasks"
}
```

Implemented mappings include:

- `400 Bad Request` for validation, enum parsing, and malformed payloads
- `401 Unauthorized` for invalid login credentials
- `403 Forbidden` for cross-user task access
- `404 Not Found` when a user or task does not exist
- `409 Conflict` for duplicate tasks and data integrity conflicts
- `500 Internal Server Error` for unhandled runtime failures

## Codebase Notes

This README reflects the code currently in the repository, including a few implementation details worth knowing:

- registration does not currently block duplicate emails before hitting the database constraint
- auth responses are plain strings, not JSON objects
- there are no update or delete endpoints for tasks
- execution logs are always written for `LocalDate.now()`; clients cannot submit a custom date
- task streak lookup does not verify task ownership before computing metrics
- leaderboard pagination is applied before global sorting, so it is page-scoped rather than a true global ranking
- the JWT secret is hardcoded in the application code
- schema management currently relies on `spring.jpa.hibernate.ddl-auto=update`

## Suggested Next Steps

- move database and JWT secrets to environment-based configuration
- add Flyway or Liquibase migrations
- add DTOs for auth requests and responses
- add update and delete task endpoints
- secure streak queries with ownership checks
- expand automated tests beyond the default application context test
