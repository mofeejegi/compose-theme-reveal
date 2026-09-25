pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "compose-theme-reveal"
include(":theme-reveal")
include(":sample:shared")
include(":sample:androidApp")
include(":sample:desktopApp")
include(":sample:webApp")
