pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "nepleaks-clj"

include("notification-contracts")
include("ingest-api")
include("notification-router")

project(":notification-contracts").projectDir = file("notification-ingest/notification-contracts")
project(":ingest-api").projectDir = file("notification-ingest/ingest-api")
project(":notification-router").projectDir = file("notification-ingest/notification-router")
