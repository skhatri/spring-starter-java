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

#### Build

Build image like so
```
./images.sh spring
```



