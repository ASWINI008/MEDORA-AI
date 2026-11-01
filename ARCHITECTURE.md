# Medora AI Architecture & Scalability Plan

## Current Architecture (Monolith)

Currently, the application runs as a modular monolith:
1. **Frontend**: React (Vite) application managing role-based dashboards and state.
2. **Backend**: Spring Boot 3.x exposing RESTful APIs with Spring Security and JWT-based authentication.
3. **Database**: PostgreSQL (or H2 in Postgres compatibility mode for local testing) containing all operational tables (`patients`, `appointments`, `staff`, etc.).
4. **AI Layer**: A standalone Python FastAPI service handling specific predictive models, communicating synchronously over HTTP with the Spring Boot backend.

All backend components (Auth, Patient Flow, Staff, Resources, Appointments) are packaged inside the single Spring Boot artifact. Events are currently handled locally using simple logging or `@Async` execution.

## Target Scalable Architecture (Microservices + Event-Driven)

To support massive hospital network scalability, the platform is preparing for a transition to an event-driven microservices architecture deployed on a managed cloud provider (AWS/Azure/GCP).

### Future Architecture Components

1. **Frontend (React)**: Deployed to a CDN (e.g., AWS CloudFront + S3, or Azure Static Web Apps).
2. **API Gateway**: A reverse proxy (e.g., Spring Cloud Gateway or AWS API Gateway) to route requests, terminate SSL, and enforce rate limits.
3. **Core Microservices**:
   - `auth-service`: Handles JWT issuance, registration, and logout (token blacklisting).
   - `patient-service`: Manages patient records, triage, and demographics.
   - `clinical-ops-service`: Manages doctors, nurses, paramedical staff, and departments.
   - `resource-service`: Manages beds, equipment, and operating theaters.
   - `appointment-service`: Manages scheduling.
   - `ambulance-service`: Real-time tracking of emergencies.
4. **AI / Prediction Layer**: Specialized Python microservices listening for events and generating predictions autonomously.
5. **Event Broker (Kafka)**: The central nervous system of the hospital application. Microservices will no longer communicate synchronously for data updates. Instead, they will publish and subscribe to topics.

### Kafka Event Topics

We have prepared the configuration and producer interfaces for the following mission-critical topics:
- `patient-arrival`: Triggered when a new patient registers.
- `patient-flow-update`: Triggered when a patient moves from WAITING -> IN_TRIAGE -> CONSULTATION.
- `staff-availability`: Triggered when doctors or nurses clock in, clock out, or are assigned to cases.
- `resource-update`: Triggered when a bed or OT changes status (AVAILABLE <-> OCCUPIED).
- `appointment-update`: Triggered on appointment scheduling or cancellation.
- `ambulance-update`: Triggered when an ambulance status or ETA changes.
- `prediction-generated`: Triggered by the AI layer when a new waiting time or congestion prediction is ready.
- `bottleneck-detected`: Triggered when the Analytics service detects a severe anomaly in patient flow.
- `recommendation-generated`: Triggered when the AI Recommendation agent suggests an action (e.g., "Reallocate 2 nurses to Cardiology").

## Containerization and Cloud Deployment Strategy

The application is fully containerized utilizing multi-stage Docker builds:
- **Frontend**: `Dockerfile.frontend` builds via Node and serves static files via an Nginx alpine container.
- **Backend**: `Dockerfile.backend` compiles via Maven and runs the JRE 21 alpine image.
- **AI Service**: `Dockerfile.ai` sets up the Python 3.11 environment and runs Uvicorn.

### 12-Factor App Compliance
Configuration is strictly managed via environment variables (no hardcoded credentials):
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `MEDORA_JWT_SECRET`, `MEDORA_JWT_EXPIRATION`
- `KAFKA_ENABLED`, `KAFKA_BOOTSTRAP_SERVERS`
- `DB_POOL_MAX`, `DB_POOL_MIN`

### Scalability Enhancements Implemented
1. **Database Connection Pooling**: Explicitly configured HikariCP to support higher throughput concurrently (20 connections max per instance, configurable via ENV).
2. **Stateless Authentication**: JWT with a backend database blacklist ensures instances can scale horizontally without sticky sessions.
3. **Asynchronous Execution**: Backend events are offloaded to `@Async` background threads (and eventually Kafka) to free up HTTP request threads immediately.
