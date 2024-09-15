CREATE DATABASE starter;

\c starter 

CREATE USER starter_owner WITH NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT LOGIN NOREPLICATION NOBYPASSRLS;
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

create table app.pokemons(
  name text primary key,
  primary_type varchar(20),
  secondary_type varchar(20),
  base_stat int,
  location varchar(30),
  legendary boolean,
  weakness text[],
  height decimal(4, 2),
  weight decimal(5, 2)
);

insert into app.pokemons(name, primary_type, base_stat, location, weakness, height, weight)
values('pikachu', 'electric', 320, '25.749537,-80.258957', '{"ground"}', 0.4, 6.0);
insert into app.pokemons(name, primary_type, base_stat, location, legendary, weakness, height, weight)
values('arceus', 'normal', 720, '25.749537,-80.258957', true, '{"fighting"}', 3.2, 320.0);
insert into app.pokemons(name, primary_type, secondary_type, base_stat, location, legendary, weakness, height, weight)
values('articuno', 'ice', 'flying', 550, '-34.915440,138.595520', true, '{"electric", "rock", "fire", "steel"}', 1.7, 55.4);
insert into app.pokemons(name, primary_type, base_stat, location, weakness, height, weight)
values('charmeleon', 'fire', 405, '30.494345,-90.916779', '{"rock", "water", "ground"}', 1.1, 19.0);
insert into app.pokemons(name, primary_type, secondary_type, base_stat, location, weakness, height, weight)
values('dragonite', 'dragon', 'flying', 600, '33.799973,-117.934158', '{"rock", "fairy", "ice", "dragon"}', 2.2, 210.0);
insert into app.pokemons(name, primary_type, secondary_type, base_stat, location, weakness, height, weight)
values('baxcalibur', 'dragon', 'ice', 600, '38.937969,-77.282272', '{"fairy", "steel", "dragon", "fighting", "rock"}', 2.1, 210.0);
insert into app.pokemons(name, primary_type, secondary_type, base_stat, location, legendary, weakness, height, weight)
values('chien-pao', 'dark', 'ice', 570, '35.658455,139.745026', true, '{"fire", "fighting", "bug", "rock", "steel", "fairy"}', 1.9, 152.2);
insert into app.pokemons(name, primary_type, secondary_type, base_stat, location, legendary, weakness, height, weight)
values('koraidon', 'fighting', 'dragon', 670, '43.130077,-80.758316', true, '{"fairy", "ice", "dragon", "psychic", "flying"}', 2.5, 303.0);
insert into app.pokemons(name, primary_type, base_stat, location, weakness, height, weight)
values('arcanine', 'fire', 555, '-6.129323,28.986500', '{"ground", "rock", "water"}', 1.9, 155.0);


CREATE TABLE app.epl_standings (
                               season INT NOT NULL,
                               ranking INT NOT NULL,
                               team VARCHAR(255) NOT NULL,
                               played INT NOT NULL,
                               gf INT NOT NULL,
                               ga INT NOT NULL,
                               gd INT NOT NULL,
                               points INT NOT NULL,
                               PRIMARY KEY (season, team)
);

CREATE TABLE app.epl_team_match (
    season INT NOT NULL,
    wk INT NOT NULL,
    matchDate DATE NOT NULL,
    team VARCHAR(255) NOT NULL,
    opponent VARCHAR(255) NOT NULL,
    venue VARCHAR(255),
    result VARCHAR(10),
    gf INT NOT NULL,
    ga INT NOT NULL,
    points INT NOT NULL,
    PRIMARY KEY (season, wk, matchDate, team, opponent)
);

set role postgres;
COPY app.epl_standings (season, ranking, team, played, gf, ga, gd, points)
    FROM '/tmp/csv/epl-table-1992-2024.csv'
    WITH (FORMAT csv, HEADER false, DELIMITER ',');

copy app.epl_team_match (season, wk, matchDate, team, opponent, venue, result, gf, ga, points)
    FROM '/tmp/csv/epl-historical-1992-2024.csv'
    WITH (FORMAT csv, HEADER false, DELIMITER ',');

set role starter_owner;