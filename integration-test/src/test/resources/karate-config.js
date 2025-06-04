function fn() {
    karate.configure('connectTimeout', 5000);
    karate.configure('readTimeout', 5000);
    let port = karate.properties['server.port'] || '8080';
    var config = {
        baseUrl: `http://localhost:${port}`
    };

    return config;
}

