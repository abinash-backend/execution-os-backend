# Execution OS Backend

Spring Boot backend for tracking tasks, daily execution logs, consistency streaks, and leaderboard performance.

## What It Solves

This project is built for products that need to:

- create and manage user tasks
- record daily execution/completion status
- prevent duplicate daily logs
- calculate streaks and consistency
- secure user data with JWT authentication

It fits habit-tracking products, internal productivity systems, and execution dashboards.

## Architecture

This application uses a layered monolith structure:

- `controller` for HTTP endpoints
- `service` for business logic
- `repository` for database access
- `entity` for persistence models
- `dto` for API request/response models
- `common` for shared enums, security, and configuration

## Tech Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA / Hibernate
- Spring Security
- JWT
- PostgreSQL
- Maven
- Lombok

## Core Features

- User registration and login
- JWT-based stateless authentication
- Authenticated task creation
- Task filtering by status and priority
- Daily execution logging
- Duplicate execution prevention per task per day
- Streak calculation
- Leaderboard and consistency scoring

## API Overview

### Authentication

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

### Tasks

- `POST /api/v1/tasks`
- `GET /api/v1/tasks`
- `GET /api/v1/tasks/{taskId}/streak`
- `GET /api/v1/tasks/leaderboard`

### Execution

- `POST /api/v1/tasks/{taskId}/execution`
- `GET /api/v1/tasks/{taskId}/execution`

## Example Flow

1. Register a user
2. Login and get JWT token
3. Send `Authorization: Bearer <token>`
4. Create tasks
5. Log daily execution
6. Fetch streak and leaderboard metrics

## Local Setup

### Prerequisites

- Java 17+
- Maven
- PostgreSQL

### Database

Create a PostgreSQL database and configure credentials in `src/main/resources/application.yaml`.

Current defaults:

- database: `execution_os`
- username: `execution_user`
- password: `secure123`

### Run

```powershell
./mvnw.cmd spring-boot:run
```

### Test

```powershell
./mvnw.cmd test
```

## Example Request

### Create Task

```http
POST /api/v1/tasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Write daily report",
  "description": "Submit progress report before 6 PM",
  "deadline": "2026-03-30",
  "priority": "HIGH"
}
```

## Current Gaps

This project is functional as an MVP, but the next production-readiness steps are:

- add global exception handling
- add OpenAPI / Swagger documentation
- move secrets to environment variables
- add unit and integration tests
- add Flyway or Liquibase migrations
- refactor execution logs to use a relational task reference

## Freelance Positioning

This project demonstrates:

- Spring Boot backend development
- REST API implementation
- PostgreSQL schema design
- JWT authentication
- service-layer business logic
- backend bug fixing and feature development

It is suitable as a portfolio sample for backend freelance work involving APIs, internal tools, and MVP systems.
