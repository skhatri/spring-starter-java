# microservices-starter
starter project with various frameworks using pokemon example

### How to run
This project uses a postgres database to store pokemon data. Once the database is started,
a specific app flavour can be run using the runApp task in each project. 

#### Start Database
Start a postgres container with sample data

```shell 
docker-compose up -d
```

#### Run App
Each starter can be run with runApp task
```shell

./gradlew spring-starter:runApp 

./gradlew helidon-starter:runApp

./gradlew quarkus-starter:runApp

```

#### Test Endpoint
Launch the endpoint ```http://localhost:8080/pokemon/list``` to view the list of pokemons

#### Build

Build image for each framework like so
```
./images.sh spring
./images.sh quarkus
```

check ```images.sh``` for more


