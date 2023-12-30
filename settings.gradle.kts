rootProject.name="microservices-starter"

listOf("spring-starter", "quarkus-starter", "pokemon-common").forEach { folder ->
    include(folder)
    project(":${folder}").projectDir = file(folder)
}

