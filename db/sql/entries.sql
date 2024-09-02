create table pokedex.entries(
  name text primary key,
  primary_type varchar(20),
  secondary_type varchar(20),
  base_stat int,
  location varchar(20),
  legendary boolean,
  weakness text[],
  height decimal(4, 2),
  weight decimal(5, 2)
);

insert into pokedex.entries(name, primary_type, base_stat, location, weakness, height, weight)
values('pikachu', 'electric', 320, '25.749537,-80.258957', '{"ground"}', 0.4, 6.0);
insert into pokedex.entries(name, primary_type, base_stat, location, weakness, height, weight)
values('pikachu', 'electric', 320, '25.749537,-80.258957', {'ground'}, 0.4, 6.0);
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

