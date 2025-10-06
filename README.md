# Digital Banking Onboarding Service

## Overview

Spring Boot microservice for customer onboarding, account funding, and loan applications.

## Running Locally

- `mvn spring-boot:run`
- Access H2 console: http://localhost:8080/h2-console
- APIs: Base URL http://localhost:8080/api/v1

## Testing

- Unit: `mvn test`
- Integration: Use Postman for APIs.

## Deployment & Scaling

- **Build**: `mvn package` -> JAR in target/
- **Docker**: `docker build -t digital-banking .` then `docker run -p 8080:8080 digital-banking`
- **Cloud (AWS Example)**:
    - Deploy JAR/Docker to ECS (Fargate) or EKS for auto-scaling based on CPU (>70% -> scale out).
    - DB: Migrate to RDS PostgreSQL (update `spring.datasource.url` via env vars).
    - Config/Secrets: AWS Secrets Manager for passwords, Parameter Store for business params.
    - Monitoring: CloudWatch for logs/metrics.
    - 12-Factor: Env vars for all config, stateless (DB external), logs to stdout.
    - Scaling: Horizontal pod autoscaler in EKS, handle traffic spikes for onboarding.

## APIs

- POST /api/v1/customers/register {name, email, password}
- POST /api/v1/customers/verify {email, verificationCode}
- POST /api/v1/accounts/fund {accountId, amount}
- POST /api/v1/loans/apply {accountId, amount, tenure}