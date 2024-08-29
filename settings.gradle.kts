//pluginManagement {
//    repositories {
//        google()
//        mavenCentral()
//        gradlePluginPortal()
//    }
//}
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
            flatDir {
                dirs("libs")
            }
        }

        mavenCentral()
        gradlePluginPortal()
    }
}
//dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositories {
//        google()
//        mavenCentral()
//    }
//}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
            credentials {
                username = "jp_54lm5g4c9icl9uscqje2dpqf3v"
            }
        }
        flatDir {
            dirs("libs")
        }
    }
}

rootProject.name = "Map Application"
include(":app")
include(":Maps_Sdk")
