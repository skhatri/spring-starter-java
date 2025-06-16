plugins {
    alias(libs.plugins.flyway)
}

group = "com.github"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.postgresql.jdbc)
}

flyway {
    url = "jdbc:postgresql://localhost:5432/starter"
    user = "starter_owner"
    password = "starter"
    schemas = arrayOf("app")
    locations = arrayOf("filesystem:src/main/database/pg")
    baselineOnMigrate = true
    validateOnMigrate = true
} 