package com.github.starter;

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PostgreSQL Data Validation Tests")
class PostgreSQLDataValidationTest extends PostgreSQLTestBase {
    
    @Autowired
    private ConnectionFactory connectionFactory;
    
    @Test
    @DisplayName("Should load all Pokemon data from CSV")
    void shouldLoadAllPokemonDataFromCsv() {
        DatabaseClient client = DatabaseClient.create(connectionFactory);
        
        Long pokemonCount = client.sql("SELECT COUNT(*) FROM app.pokemons")
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
        
        assertNotNull(pokemonCount);
        assertEquals(808L, pokemonCount, "Should have exactly 808 Pokemon loaded from CSV");
    }
    
    @Test
    @DisplayName("Should load all country data from CSV")
    void shouldLoadAllCountryDataFromCsv() {
        DatabaseClient client = DatabaseClient.create(connectionFactory);
        
        Long countryCount = client.sql("SELECT COUNT(*) FROM app.countries")
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
        
        assertNotNull(countryCount);
        assertTrue(countryCount >= 175L, "Should have at least 175 countries loaded from CSV");
    }
    
    @Test
    @DisplayName("Should load all effectiveness data from CSV")
    void shouldLoadAllEffectivenessDataFromCsv() {
        DatabaseClient client = DatabaseClient.create(connectionFactory);
        
        Long effectivenessCount = client.sql("SELECT COUNT(*) FROM app.effectiveness")
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
        
        assertNotNull(effectivenessCount);
        assertTrue(effectivenessCount >= 160L, "Should have at least 160 effectiveness records loaded from CSV");
    }
    
    @Test
    @DisplayName("Should have PostgreSQL-specific enum type working")
    void shouldHavePostgreSQLEnumTypeWorking() {
        DatabaseClient client = DatabaseClient.create(connectionFactory);
        
        Long typeEffectivenessCount = client.sql("SELECT COUNT(*) FROM app.type_effectiveness")
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
        
        assertNotNull(typeEffectivenessCount);
        assertTrue(typeEffectivenessCount >= 18L, "Should have type effectiveness data for all 18 Pokemon types");
    }
    
    @Test
    @DisplayName("Should verify Pokemon region mapping worked correctly")
    void shouldVerifyPokemonRegionMappingWorkedCorrectly() {
        DatabaseClient client = DatabaseClient.create(connectionFactory);
        
        Long kantoCount = client.sql("SELECT COUNT(*) FROM app.pokemons WHERE region = 'kanto'")
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
        
        Long johtoCount = client.sql("SELECT COUNT(*) FROM app.pokemons WHERE region = 'johto'")
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
        
        assertNotNull(kantoCount);
        assertNotNull(johtoCount);
        assertTrue(kantoCount > 0, "Should have Pokemon from Kanto region");
        assertTrue(johtoCount > 0, "Should have Pokemon from Johto region");
    }
    
    @Test
    @DisplayName("Should verify PostgreSQL array operations work")
    void shouldVerifyPostgreSQLArrayOperationsWork() {
        DatabaseClient client = DatabaseClient.create(connectionFactory);
        
        String typeName = client.sql("SELECT type_name FROM app.type_effectiveness WHERE array_length(m_2, 1) > 0 LIMIT 1")
            .map(row -> row.get("type_name", String.class))
            .one()
            .block();
        
        assertNotNull(typeName);
        assertFalse(typeName.isEmpty(), "Should find type effectiveness with array data");
    }
    
    @Test
    @DisplayName("Should verify database users and permissions")
    void shouldVerifyDatabaseUsersAndPermissions() {
        DatabaseClient client = DatabaseClient.create(connectionFactory);
        
        String currentUser = client.sql("SELECT current_user")
            .map(row -> row.get(0, String.class))
            .one()
            .block();
        
        assertNotNull(currentUser);
        assertEquals("starter_user", currentUser, "Should be connected as starter_user");
    }
    
    @Test
    @DisplayName("Should verify schema exists and is accessible")
    void shouldVerifySchemaExistsAndIsAccessible() {
        DatabaseClient client = DatabaseClient.create(connectionFactory);
        
        Long schemaCount = client.sql("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'app'")
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
        
        assertNotNull(schemaCount);
        assertEquals(1L, schemaCount, "App schema should exist");
    }
    
    @Test
    @DisplayName("Should verify all expected tables exist")
    void shouldVerifyAllExpectedTablesExist() {
        DatabaseClient client = DatabaseClient.create(connectionFactory);
        
        Long tableCount = client.sql("""
            SELECT COUNT(*) FROM information_schema.tables 
            WHERE table_schema = 'app' 
            AND table_name IN ('pokemons', 'countries', 'effectiveness', 'type_effectiveness')
            """)
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
        
        assertNotNull(tableCount);
        assertEquals(4L, tableCount, "Should have all 4 expected tables in app schema");
    }
    
    @Test
    @DisplayName("Should verify sample Pokemon data integrity")
    void shouldVerifySamplePokemonDataIntegrity() {
        DatabaseClient client = DatabaseClient.create(connectionFactory);
        
        String pikachuType = client.sql("SELECT primary_type FROM app.pokemons WHERE name = 'Pikachu'")
            .map(row -> row.get("primary_type", String.class))
            .one()
            .block();
        
        assertNotNull(pikachuType);
        assertEquals("Electric", pikachuType, "Pikachu should be Electric type");
    }
} 