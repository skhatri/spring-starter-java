CREATE DATABASE pokemon;

\c pokemon

CREATE USER pokemon_owner WITH NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT LOGIN NOREPLICATION NOBYPASSRLS;
GRANT pokemon_owner to postgres;
GRANT CONNECT,CREATE ON DATABASE pokemon TO pokemon_owner;

CREATE USER pokemon_user WITH NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT LOGIN NOREPLICATION NOBYPASSRLS;
GRANT pokemon_user to postgres;
GRANT CONNECT ON DATABASE pokemon TO pokemon_user;


CREATE SCHEMA pokedex AUTHORIZATION pokemon_owner;

GRANT USAGE ON SCHEMA pokedex TO pokemon_user;

GRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA pokedex TO pokemon_user;
GRANT SELECT,UPDATE ON ALL SEQUENCES IN SCHEMA pokedex TO pokemon_user;
ALTER DEFAULT PRIVILEGES FOR ROLE pokemon_owner IN SCHEMA pokedex GRANT SELECT,INSERT,UPDATE,DELETE ON TABLES TO pokemon_user;
ALTER DEFAULT PRIVILEGES FOR ROLE pokemon_owner IN SCHEMA pokedex GRANT SELECT,UPDATE ON SEQUENCES TO pokemon_user;

ALTER ROLE pokemon_owner IN DATABASE pokemon SET search_path TO pokedex,"$user",public;
ALTER ROLE pokemon_user IN DATABASE pokemon SET search_path TO pokedex,"$user",public;

ALTER USER pokemon_owner WITH PASSWORD 'pokemon';
ALTER USER pokemon_user WITH PASSWORD 'pokemon';

set role pokemon_owner;

create table pokedex.entries(
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

insert into pokedex.entries(name, primary_type, base_stat, location, weakness, height, weight)
values('pikachu', 'electric', 320, '25.749537,-80.258957', '{"ground"}', 0.4, 6.0);
insert into pokedex.entries(name, primary_type, base_stat, location, legendary, weakness, height, weight)
values('arceus', 'normal', 720, '25.749537,-80.258957', true, '{"fighting"}', 3.2, 320.0);
insert into pokedex.entries(name, primary_type, secondary_type, base_stat, location, legendary, weakness, height, weight)
values('articuno', 'ice', 'flying', 550, '-34.915440,138.595520', true, '{"electric", "rock", "fire", "steel"}', 1.7, 55.4);
insert into pokedex.entries(name, primary_type, base_stat, location, weakness, height, weight)
values('charmeleon', 'fire', 405, '30.494345,-90.916779', '{"rock", "water", "ground"}', 1.1, 19.0);
insert into pokedex.entries(name, primary_type, secondary_type, base_stat, location, weakness, height, weight)
values('dragonite', 'dragon', 'flying', 600, '33.799973,-117.934158', '{"rock", "fairy", "ice", "dragon"}', 2.2, 210.0);
insert into pokedex.entries(name, primary_type, secondary_type, base_stat, location, weakness, height, weight)
values('baxcalibur', 'dragon', 'ice', 600, '38.937969,-77.282272', '{"fairy", "steel", "dragon", "fighting", "rock"}', 2.1, 210.0);
insert into pokedex.entries(name, primary_type, secondary_type, base_stat, location, legendary, weakness, height, weight)
values('chien-pao', 'dark', 'ice', 570, '35.658455,139.745026', true, '{"fire", "fighting", "bug", "rock", "steel", "fairy"}', 1.9, 152.2);
insert into pokedex.entries(name, primary_type, secondary_type, base_stat, location, legendary, weakness, height, weight)
values('koraidon', 'fighting', 'dragon', 670, '43.130077,-80.758316', true, '{"fairy", "ice", "dragon", "psychic", "flying"}', 2.5, 303.0);
insert into pokedex.entries(name, primary_type, base_stat, location, weakness, height, weight)
values('arcanine', 'fire', 555, '-6.129323,28.986500', '{"ground", "rock", "water"}', 1.9, 155.0);

