plugins {
    `java-library`
}

val platformVersion = providers.gradleProperty("platform.version").getOrElse("0.1.0")

dependencies {
    api(platform("com.github.starter:starter-platform-bom:$platformVersion"))
    api("com.github.starter:starter-core:$platformVersion")
    api("org.yaml:snakeyaml")
} 