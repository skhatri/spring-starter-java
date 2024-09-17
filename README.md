# microservices-starter

spring starter project using pokemon example

### How to run

This project uses a postgres database to store pokemon data. Once the database is started,
the spring boot app can be run using the runApp task.

#### Start Database

Start a postgres container with sample data

```shell 
docker-compose up -d
```

#### Run App

Starter can be run with runApp task

```shell

./gradlew app:runApp 
  

```

#### Test Endpoint

Launch the endpoint ```http://localhost:8080/pokemon/list``` to view the list of pokemons

#### Test API

Run Karate Test to test status page and pokemon data

```
./gradlew test
```

result can be viewed at

```shell 
open app/build/reports/tests/test/index.html
```
There are spring boot integration tests as well as wiremock tests using testcontainers.
You can disable either of them by providing something like

```shell
 export NO_WIREMOCK=true
 export NO_BOOT=true
```

#### Testing GraphQL

You can test it using embedded graphiql client ```http://localhost:8080/graphiql?path=/graphql```
or invoke through curl

Check Status

``` 
curl 'http://localhost:8080/graphql' \
  -H 'content-type: application/json' \
  --data-raw '{
        "query":"query HealthCheck {\n  health{\n    status\n    server_time\n  }\n}",
        "operationName":"HealthCheck"
    }'
```

Get Pokemon List

``` 
curl 'http://localhost:8080/graphql' \
-H 'content-type: application/json' \
--data-raw '{
  "query": "query PokemonList {\n getPokemonList{\n name\n primaryType\n baseStat\n secondaryType\n location\n legendary\n weakness\n height\n weight\n }\n}\n",
  "operationName": "PokemonList"
}'
```

EPL Queries

| EPL                                                              |
|------------------------------------------------------------------|
| best performance ever                                            |
| team performance by season                                       |
| table for season                                                 |
| team position by year                                            |
| winners by season                                                |
| all teams in history                                             |

#### Build

Build image like so

```
./images.sh 
```

Once the image is built, run it like this

```shell
#run with embedded duckdb
docker run -p 8080:8080 --rm -it app
#run with adjacent postgres
docker run -p 8080:8080 -e APP_DB=pg -e POSTGRES_HOST=postgres --network spring-starter-java_starter --rm -it app
```


