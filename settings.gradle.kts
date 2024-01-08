rootProject.name = "vsds-dataspace-connector"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

include("http-pull-connector")
include("federated-catalog-connector")
//include("extended-public-api-connector")
