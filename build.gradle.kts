// build.gradle.kts  (RAIZ do projeto — o que aparece como
//                    "build.gradle.kts (Project: Lazer-Sport_APP)")
//
// Este arquivo NÃO configura nada: só declara quais plugins existem
// no projeto. O `apply false` significa "registre este plugin, mas
// não aplique aqui" — quem aplica de fato é o app/build.gradle.kts.
//
// Sem estas linhas, o alias(libs.plugins.xxx) do módulo app falha e
// o compilador do Compose nem roda — por isso TODA tela fica vermelha.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}