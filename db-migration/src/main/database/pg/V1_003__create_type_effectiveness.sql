DROP TABLE IF EXISTS app.type_effectiveness;
DROP TYPE IF EXISTS pokemon_type;

CREATE TYPE pokemon_type AS ENUM (
    'normal', 'fire', 'water', 'electric', 'grass', 'ice', 
    'fighting', 'poison', 'ground', 'flying', 'psychic', 
    'bug', 'rock', 'ghost', 'dragon', 'dark', 'steel', 'fairy'
);
GRANT USAGE ON TYPE pokemon_type TO PUBLIC;

CREATE TABLE app.type_effectiveness (
    id SERIAL PRIMARY KEY,
    type_name varchar(30) NOT NULL,
    m_0 pokemon_type[] DEFAULT '{}', 
    m_025 pokemon_type[] DEFAULT '{}', 
    m_05 pokemon_type[] DEFAULT '{}', 
    m_1 pokemon_type[] DEFAULT '{}', 
    m_2 pokemon_type[] DEFAULT '{}', 
    m_4 pokemon_type[] DEFAULT '{}', 
    UNIQUE(type_name)
); 