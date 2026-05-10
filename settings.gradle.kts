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
include("delivery-worker")

project(":notification-contracts").projectDir = file("notification-ingest/notification-contracts")
project(":ingest-api").projectDir = file("notification-ingest/ingest-api")
project(":notification-router").projectDir = file("notification-ingest/notification-router")
project(":delivery-worker").projectDir = file("notification-ingest/delivery-worker")
