TRUNCATE TABLE app.countries;
COPY app.countries(country,capital,area,continent)
FROM '/tmp/data/csv/country.csv'
WITH (FORMAT csv, HEADER true, DELIMITER ',');


TRUNCATE TABLE app.pokemons;
copy app.pokemons(pokedex,name,primary_type,secondary_type,total,hp,attack,defence,sp_attack,sp_defence,speed,generation,legendary)
    FROM '/tmp/data/csv/pokemon.csv'
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
FROM '/tmp/data/csv/effectiveness.csv'
WITH (FORMAT csv, HEADER true, DELIMITER ','); 