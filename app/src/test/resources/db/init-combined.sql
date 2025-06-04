CREATE USER starter_owner WITH SUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT LOGIN NOREPLICATION NOBYPASSRLS;
GRANT starter_owner to postgres;
GRANT CONNECT,CREATE ON DATABASE starter TO starter_owner;

CREATE USER starter_user WITH NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT LOGIN NOREPLICATION NOBYPASSRLS;
GRANT starter_user to postgres;
GRANT CONNECT ON DATABASE starter TO starter_user;


CREATE SCHEMA app AUTHORIZATION starter_owner;

GRANT USAGE ON SCHEMA app TO starter_user;

GRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA app TO starter_user;
GRANT SELECT,UPDATE ON ALL SEQUENCES IN SCHEMA app TO starter_user;
ALTER DEFAULT PRIVILEGES FOR ROLE starter_owner IN SCHEMA app GRANT SELECT,INSERT,UPDATE,DELETE ON TABLES TO starter_user;
ALTER DEFAULT PRIVILEGES FOR ROLE starter_owner IN SCHEMA app GRANT SELECT,UPDATE ON SEQUENCES TO starter_user;

ALTER ROLE starter_owner IN DATABASE starter SET search_path TO app,"$user",public;
ALTER ROLE starter_user IN DATABASE starter SET search_path TO app,"$user",public;

ALTER USER starter_owner WITH PASSWORD 'starter';
ALTER USER starter_user WITH PASSWORD 'starter';

set role starter_owner; 

create table IF NOT EXISTS app.pokemons(
  pokedex int,
  name text primary key,
  primary_type varchar(20),
  secondary_type varchar(20),
  total int,
  hp int,
  attack int,
  defence int,
  sp_attack int,
  sp_defence int,
  speed int,
  generation int,
  legendary varchar(10),
  region varchar(20)
);

CREATE TABLE IF NOT EXISTS app.effectiveness (
    id SERIAL PRIMARY KEY,
    type_name varchar(30) NOT NULL,
    m_0 varchar[] DEFAULT '{}', 
    m_025 varchar[] DEFAULT '{}', 
    m_05 varchar[] DEFAULT '{}', 
    m_1 varchar[] DEFAULT '{}', 
    m_2 varchar[] DEFAULT '{}', 
    m_4 varchar[] DEFAULT '{}', 
    UNIQUE(type_name)
);

create table IF NOT EXISTS app.countries(
  country varchar(100) primary key,
  capital varchar(100),
  continent varchar(100),
  area int
); 

TRUNCATE TABLE app.countries;
COPY app.countries(country,capital,area,continent)
FROM '/docker-entrypoint-initdb.d/csv/country.csv'
WITH (FORMAT csv, HEADER true, DELIMITER ',');


TRUNCATE TABLE app.pokemons;
copy app.pokemons(pokedex,name,primary_type,secondary_type,total,hp,attack,defence,sp_attack,sp_defence,speed,generation,legendary)
    FROM '/docker-entrypoint-initdb.d/csv/pokemon.csv'
    WITH (FORMAT csv, HEADER true, DELIMITER ',');

update app.pokemons set region = 'kanto' where generation = 1;
update app.pokemons set region = 'johto' where generation = 2;
update app.pokemons set region = 'hoenn' where generation = 3;
update app.pokemons set region = 'sinnoh' where generation = 4;
update app.pokemons set region = 'unova' where generation = 5;
update app.pokemons set region = 'kalos' where generation = 6;
update app.pokemons set region = 'alola' where generation = 7;  
update app.pokemons set region = 'galar' where generation = 8;
update app.pokemons set region = 'hisui' where generation = 9;
update app.pokemons set region = 'paldea' where generation = 10;

TRUNCATE TABLE app.effectiveness;
COPY app.effectiveness(type_name, m_0, m_05, m_1, m_2, m_025, m_4)
FROM '/docker-entrypoint-initdb.d/csv/effectiveness.csv'
WITH (FORMAT csv, HEADER true, DELIMITER ','); 

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

INSERT INTO app.type_effectiveness (type_name, m_0, m_05, m_1, m_2)
VALUES (
    'normal',
    ARRAY['ghost']::pokemon_type[],
    ARRAY['rock', 'steel']::pokemon_type[],
    ARRAY['normal', 'fire', 'water', 'electric', 'grass', 'ice', 'poison', 'ground', 'flying', 'psychic', 'bug', 'dragon', 'dark', 'fairy']::pokemon_type[],
    ARRAY['fighting']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_05, m_1, m_2)
VALUES (
    'fire',
    ARRAY['fire', 'grass', 'ice', 'bug', 'steel', 'fairy']::pokemon_type[],
    ARRAY['normal', 'electric', 'fighting', 'poison', 'flying', 'psychic', 'ghost', 'dragon', 'dark']::pokemon_type[],
    ARRAY['water', 'ground', 'rock']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_05, m_1, m_2)
VALUES (
    'water',
    ARRAY['fire', 'water', 'ice', 'steel']::pokemon_type[],
    ARRAY['normal', 'fighting', 'poison', 'ground', 'flying', 'psychic', 'bug', 'rock', 'ghost', 'dragon', 'dark', 'fairy']::pokemon_type[],
    ARRAY['electric', 'grass']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_05, m_1, m_2)
VALUES (
    'electric',
    ARRAY['electric', 'flying', 'steel']::pokemon_type[],
    ARRAY['normal', 'fire', 'water', 'grass', 'ice', 'fighting', 'poison', 'psychic', 'bug', 'rock', 'ghost', 'dragon', 'dark', 'fairy']::pokemon_type[],
    ARRAY['ground']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_05, m_1, m_2)
VALUES (
    'grass',
    ARRAY['water', 'electric', 'grass', 'ground']::pokemon_type[],
    ARRAY['normal', 'fighting', 'rock', 'ghost', 'dragon', 'dark', 'steel', 'fairy']::pokemon_type[],
    ARRAY['fire', 'ice', 'poison', 'flying', 'bug']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_05, m_1, m_2)
VALUES (
    'ice',
    ARRAY['ice']::pokemon_type[],
    ARRAY['normal', 'water', 'electric', 'grass', 'poison', 'ground', 'flying', 'psychic', 'bug', 'ghost', 'dragon', 'dark', 'fairy']::pokemon_type[],
    ARRAY['fire', 'fighting', 'rock', 'steel']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_05, m_1, m_2)
VALUES (
    'fighting',
    ARRAY['bug', 'rock', 'dark']::pokemon_type[],
    ARRAY['normal', 'fire', 'water', 'electric', 'grass', 'ice', 'fighting', 'poison', 'ground', 'ghost', 'dragon', 'steel']::pokemon_type[],
    ARRAY['flying', 'psychic', 'fairy']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_05, m_1, m_2)
VALUES (
    'poison',
    ARRAY['grass', 'fighting', 'poison', 'bug', 'fairy']::pokemon_type[],
    ARRAY['normal', 'fire', 'water', 'electric', 'ice', 'flying', 'rock', 'ghost', 'dragon', 'dark', 'steel']::pokemon_type[],
    ARRAY['ground', 'psychic']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_0, m_05, m_1, m_2)
VALUES (
    'ground',
    ARRAY['electric']::pokemon_type[],
    ARRAY['poison', 'rock']::pokemon_type[],
    ARRAY['normal', 'fire', 'fighting', 'ground', 'flying', 'psychic', 'bug', 'ghost', 'dragon', 'dark', 'steel', 'fairy']::pokemon_type[],
    ARRAY['water', 'grass', 'ice']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_0, m_05, m_1, m_2)
VALUES (
    'flying',
    ARRAY['ground']::pokemon_type[],
    ARRAY['grass', 'fighting', 'bug']::pokemon_type[],
    ARRAY['normal', 'fire', 'water', 'poison', 'flying', 'psychic', 'ghost', 'dragon', 'dark', 'fairy', 'steel']::pokemon_type[],
    ARRAY['electric', 'ice', 'rock']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_05, m_1, m_2)
VALUES (
    'psychic',
    ARRAY['fighting', 'psychic']::pokemon_type[],
    ARRAY['normal', 'fire', 'water', 'electric', 'grass', 'ice', 'poison', 'ground', 'flying', 'bug', 'rock', 'dragon', 'fairy']::pokemon_type[],
    ARRAY['bug', 'ghost', 'dark']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_05, m_1, m_2)
VALUES (
    'bug',
    ARRAY['grass', 'fighting', 'ground']::pokemon_type[],
    ARRAY['normal', 'water', 'electric', 'ice', 'poison', 'psychic', 'bug', 'ghost', 'dragon', 'dark', 'fairy']::pokemon_type[],
    ARRAY['fire', 'flying', 'rock']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_05, m_1, m_2)
VALUES (
    'rock',
    ARRAY['normal', 'fire', 'poison', 'flying']::pokemon_type[],
    ARRAY['electric', 'ice', 'psychic', 'bug', 'rock', 'ghost', 'dragon', 'dark', 'fairy']::pokemon_type[],
    ARRAY['water', 'grass', 'fighting', 'ground', 'steel']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_0, m_05, m_1, m_2)
VALUES (
    'ghost',
    ARRAY['normal', 'fighting']::pokemon_type[],
    ARRAY['poison', 'bug']::pokemon_type[],
    ARRAY['fire', 'water', 'electric', 'grass', 'ice', 'ground', 'flying', 'psychic', 'rock', 'dragon', 'steel', 'fairy']::pokemon_type[],
    ARRAY['ghost', 'dark']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_05, m_1, m_2)
VALUES (
    'dragon',
    ARRAY['fire', 'water', 'electric', 'grass']::pokemon_type[],
    ARRAY['normal', 'fighting', 'poison', 'ground', 'flying', 'psychic', 'bug', 'rock', 'ghost', 'dark', 'steel']::pokemon_type[],
    ARRAY['ice', 'dragon', 'fairy']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_0, m_05, m_1, m_2)
VALUES (
    'dark',
    ARRAY['psychic']::pokemon_type[],
    ARRAY['ghost', 'dark']::pokemon_type[],
    ARRAY['normal', 'fire', 'water', 'electric', 'grass', 'ice', 'poison', 'ground', 'flying', 'rock', 'dragon', 'steel']::pokemon_type[],
    ARRAY['fighting', 'bug', 'fairy']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_0, m_05, m_1, m_2)
VALUES (
    'steel',
    ARRAY['poison']::pokemon_type[],
    ARRAY['normal', 'grass', 'ice', 'flying', 'psychic', 'bug', 'rock', 'dragon', 'steel', 'fairy']::pokemon_type[],
    ARRAY['water', 'electric', 'ghost', 'dark']::pokemon_type[],
    ARRAY['fire', 'fighting', 'ground']::pokemon_type[]
);

INSERT INTO app.type_effectiveness (type_name, m_0, m_05, m_1, m_2)
VALUES (
    'fairy',
    ARRAY['dragon']::pokemon_type[],
    ARRAY['fighting', 'bug', 'dark']::pokemon_type[],
    ARRAY['normal', 'fire', 'water', 'electric', 'grass', 'ice', 'ground', 'flying', 'psychic', 'rock', 'ghost', 'fairy']::pokemon_type[],
    ARRAY['poison', 'steel']::pokemon_type[]
);

UPDATE app.type_effectiveness
SET m_025 = ARRAY['grass']::pokemon_type[]
WHERE type_name = 'water';

UPDATE app.type_effectiveness
SET m_4 = ARRAY['ice']::pokemon_type[]
WHERE type_name = 'dragon'; 

