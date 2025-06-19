plugins {
    `java-library`
}

val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

dependencies {
    main(libs.bundle("postgres-database"))
    test(libs.bundle("postgres-testing"))
} 