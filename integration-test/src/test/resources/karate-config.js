function fn() {
    karate.configure('connectTimeout', 5000);
    karate.configure('readTimeout', 5000);
    
    var pokemonHost = karate.properties['pokemon.host'] || 'localhost';
    var pokemonPort = karate.properties['pokemon.port'] || '8080';
    
    var mathHost = karate.properties['math.host'] || 'localhost';
    var mathPort = karate.properties['math.port'] || '8082';
    
    var systemPort = karate.properties['server.port'];
    if (systemPort) {
        pokemonPort = systemPort;
    }
    
    var config = {
        baseUrl: `http://${pokemonHost}:${pokemonPort}`,
        pokemonBaseUrl: `http://${pokemonHost}:${pokemonPort}`,
        mathBaseUrl: `http://${mathHost}:${mathPort}`,
        pokemonHost: pokemonHost,
        pokemonPort: pokemonPort,
        mathHost: mathHost,
        mathPort: mathPort
    };

    karate.log('Pokemon service configured at:', config.pokemonBaseUrl);
    karate.log('Math service configured at:', config.mathBaseUrl);

    return config;
}

