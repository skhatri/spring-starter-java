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