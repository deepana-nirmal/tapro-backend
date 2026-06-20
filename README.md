# Tapro Backend

Spring Boot backend for the Tapro QR ordering platform.

## Requirements

- Java 17
- Maven Wrapper included

## Build

```bash
./mvnw test
```

## Run

```bash
./mvnw spring-boot:run
```

## Environment Variables

Required for production:

- `SPRING_PROFILES_ACTIVE`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `FRONTEND_URL`
- `PORT`

Common optional variables:

- `SPRING_PROFILES_ACTIVE`
- `APP_UPLOAD_DIR`
- `APP_EMAIL_DEV_MODE`
- `JWT_EXPIRATION_MS`
- `MAIL_AUTH`
- `MAIL_STARTTLS_ENABLE`
- `JPA_SHOW_SQL`

## Database Notes

- Development defaults to in-memory H2 unless `DB_URL` is provided.
- Production is expected to use PostgreSQL or another external database via `DB_*` variables.

## SMTP Notes

- Mail delivery is configured through `MAIL_*` environment variables.
- Leave `APP_EMAIL_DEV_MODE=true` for local development if you want SMTP failures to be logged instead of surfaced as API errors.

## Azure App Service Notes

- Configure all required environment variables in App Service configuration.
- Persist uploads using a mounted storage path and set `APP_UPLOAD_DIR` to that location.
- Run with `SPRING_PROFILES_ACTIVE=prod`.
- Configure these App Settings exactly:

```dotenv
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://tapro-db.postgres.database.azure.com:5432/postgres?sslmode=require
DB_USERNAME=taproadmin
DB_PASSWORD=<real password>
JWT_SECRET=<long secret>
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<email>
MAIL_PASSWORD=<gmail app password>
FRONTEND_URLS=https://tapro-frontend.vercel.app,http://localhost:3000,http://127.0.0.1:3000
```
