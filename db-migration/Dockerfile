FROM flyway/flyway:10.21.0

COPY src/main/database/pg /flyway/sql

ENV FLYWAY_URL=jdbc:postgresql://postgres:5432/starter
ENV FLYWAY_USER=postgres
ENV FLYWAY_PASSWORD=password
ENV FLYWAY_SCHEMAS=app
ENV FLYWAY_BASELINE_ON_MIGRATE=true
ENV FLYWAY_VALIDATE_ON_MIGRATE=true

CMD ["migrate"] 