// settings.gradle.kts  (RAIZ do projeto)
//
// Define de onde as bibliotecas são baixadas. Se o Gradle reclamar
// "Could not find com.google.dagger:hilt-android" ou algo parecido,
// quase sempre é porque este arquivo está faltando um repositório.
//
// O bloco versionCatalogs registra o gradle/libs.versions.toml —
// é o que faz `libs.plugins.hilt` existir. Em projetos criados pelo
// Android Studio recente isso já é automático, mas deixo explícito
// pra não depender da versão.

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Lazer-Sport_APP"
include(":app")