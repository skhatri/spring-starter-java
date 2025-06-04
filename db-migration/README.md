# Database Migration Module

This module contains database migration scripts organized using Flyway conventions.

## Structure

```
db-migration/
├── src/main/database/pg/          # Flyway migration files
│   ├── V1_001__create_tables.sql  # Initial table creation
│   ├── V1_002__load_data.sql      # Data loading from CSV files
│   ├── V1_003__create_type_effectiveness.sql  # Type system creation
│   └── V1_004__populate_type_effectiveness.sql # Type effectiveness data
├── 1-db-creation.sql              # Database creation (for docker-compose)
├── 2-db-user-setup.sql            # Users, schema, and permissions setup
├── Dockerfile                     # Flyway migration container
└── build.gradle.kts              # Gradle build with Flyway plugin
```

## Migration Files

### V1_001__create_tables.sql
Creates the core application tables:
- `app.pokemons` - Pokemon data
- `app.effectiveness` - Type effectiveness (legacy format)
- `app.countries` - Country reference data

### V1_002__load_data.sql
Loads initial data from CSV files:
- Pokemon data with region updates
- Country data
- Type effectiveness data

### V1_003__create_type_effectiveness.sql
Creates the enhanced type effectiveness system:
- `pokemon_type` enum
- `app.type_effectiveness` table with typed arrays

### V1_004__populate_type_effectiveness.sql
Populates the type effectiveness system with all Pokemon type interactions.

## Usage

### Local Development with Gradle

```bash
# Run migrations against local PostgreSQL
./gradlew db-migration:flywayMigrate

# Check migration status
./gradlew db-migration:flywayInfo

# Validate migrations
./gradlew db-migration:flywayValidate

# Clean database (development only)
./gradlew db-migration:flywayClean
```

### Docker Container

```bash
# Build the migration container
docker build -t starter-db-migration db-migration/

# Run migrations against PostgreSQL container
docker run --rm --network host starter-db-migration

# Or with custom database URL
docker run --rm \
  -e FLYWAY_URL=jdbc:postgresql://your-host:5432/starter \
  -e FLYWAY_USER=starter_owner \
  -e FLYWAY_PASSWORD=starter \
  starter-db-migration
```

### Integration with docker-compose

Add to your `docker-compose.yml`:

```yaml
services:
  postgres:
    image: postgres:17.0
    environment:
      POSTGRES_DB: starter
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    volumes:
      - ./scripts/containers/postgres/data:/tmp/data
      - ./db-migration/1-db-creation.sql:/docker-entrypoint-initdb.d/00-db-creation.sql
- ./db-migration/2-db-user-setup.sql:/docker-entrypoint-initdb.d/01-setup.sql

  db-migration:
    build: ./db-migration
    depends_on:
      - postgres
    environment:
      FLYWAY_URL: jdbc:postgresql://postgres:5432/starter
      FLYWAY_USER: starter_owner
      FLYWAY_PASSWORD: starter
```

## Configuration

The Flyway configuration is in `build.gradle.kts`:

- **URL**: `jdbc:postgresql://localhost:5432/starter`
- **User**: `starter_owner`
- **Password**: `starter`
- **Schema**: `app`
- **Location**: `filesystem:src/main/database/pg`

## Prerequisites

1. **Database Setup**: Run `1-db-creation.sql` and `2-db-user-setup.sql` first to create database, users, schema, and permissions
2. **CSV Data**: Ensure CSV files are available at `/tmp/data/csv/` (or `/docker-entrypoint-initdb.d/csv/` for containers)
3. **PostgreSQL**: Version 17.0 or compatible

## Testing Integration

The migration files are also used by integration tests:
- Test resources in `app/src/test/resources/db/` contain copies of migration files
- Tests use a combined initialization script for testcontainers
- CSV files are mounted to the correct paths for data loading 