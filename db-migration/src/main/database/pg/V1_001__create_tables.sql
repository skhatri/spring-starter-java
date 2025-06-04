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