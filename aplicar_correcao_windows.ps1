param(
    [switch]$PularCompilacao
)

$ErrorActionPreference = "Stop"
$raiz = (Get-Location).Path

if (-not (Test-Path (Join-Path $raiz "settings.gradle.kts"))) {
    throw "Execute este script na raiz do projeto, onde fica settings.gradle.kts."
}

if (-not (Test-Path (Join-Path $raiz "app"))) {
    throw "A pasta app nao foi encontrada. Abra o PowerShell na raiz do projeto."
}

$dataBackup = Get-Date -Format "yyyyMMdd-HHmmss"
$pastaBackup = Join-Path $raiz "_backup_correcao_$dataBackup"
New-Item -ItemType Directory -Path $pastaBackup -Force | Out-Null

function Salvar-Utf8SemBom {
    param(
        [string]$Caminho,
        [string]$Conteudo
    )

    $pasta = Split-Path -Parent $Caminho
    if ($pasta -and -not (Test-Path $pasta)) {
        New-Item -ItemType Directory -Path $pasta -Force | Out-Null
    }

    $utf8SemBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Caminho, $Conteudo, $utf8SemBom)
}

function Fazer-Backup {
    param([string]$Relativo)

    $origem = Join-Path $raiz $Relativo
    if (Test-Path $origem) {
        $destino = Join-Path $pastaBackup $Relativo
        $pastaDestino = Split-Path -Parent $destino
        New-Item -ItemType Directory -Path $pastaDestino -Force | Out-Null
        Copy-Item $origem $destino -Force
    }
}

$arquivosAlterados = @(
    "build.gradle.kts",
    "app/build.gradle.kts",
    "app/src/main/AndroidManifest.xml",
    "app/src/main/java/com/example/lazer_sport_app/MainActivity.kt",
    "app/src/main/java/com/example/lazer_sport_app/ui/theme/Theme.kt",
    "app/src/main/res/values/colors.xml",
    "app/src/main/res/values/strings.xml",
    "app/src/main/res/values/themes.xml",
    "app/src/main/res/values-night/themes.xml",
    "app/src/main/res/drawable/ilustracao_abertura.xml"
)

foreach ($arquivo in $arquivosAlterados) {
    Fazer-Backup $arquivo
}

Write-Host "Backup criado em: $pastaBackup" -ForegroundColor Cyan

$gradleRaiz = @'
plugins {
    id("com.android.application") version "9.3.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.10" apply false
    id("com.android.legacy-kapt") version "9.3.1" apply false
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
}
'@

$gradleApp = @'
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.android.legacy-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.lazer_sport_app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.lazer_sport_app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "BASE_URL",
            "\"https://www.lazersport.com.br/api/\"",
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kapt {
    correctErrorTypes = true
}

hilt {
    enableAggregatingTask = true
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.datastore:datastore-preferences:1.2.1")

    implementation(platform("androidx.compose:compose-bom:2026.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("com.google.dagger:hilt-android:2.60.1")
    kapt("com.google.dagger:hilt-compiler:2.60.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.android.material:material:1.13.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
'@

$cores = @'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ls_azul">#0758C9</color>
    <color name="ls_azul_escuro">#082A5E</color>
    <color name="ls_azul_claro">#E8F1FF</color>
    <color name="ls_vermelho">#E5262B</color>
    <color name="ls_vermelho_escuro">#B91C1C</color>
    <color name="ls_verde">#169B55</color>
    <color name="ls_amarelo">#FFD700</color>
    <color name="ls_texto">#172033</color>
    <color name="ls_cinza">#667085</color>
    <color name="ls_borda">#DFE7F2</color>
    <color name="ls_fundo">#F6F8FC</color>
    <color name="ls_superficie">#FFFFFF</color>
    <color name="ls_erro_fundo">#FDECEC</color>
    <color name="ls_sucesso_fundo">#EAF8F0</color>
    <color name="white">#FFFFFFFF</color>
    <color name="black">#FF000000</color>
</resources>
'@

$textos = @'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Lazer &amp; Sport</string>
    <string name="marca_nome">LAZER &amp; SPORT</string>
    <string name="marca_iniciais">L&amp;S</string>
    <string name="marca_desde">DESDE 1996</string>
    <string name="marca_slogan">Brinquedos que fazem a diferença</string>
    <string name="abertura_titulo">Diversão começa aqui.</string>
    <string name="abertura_subtitulo">Explore brinquedos, peças e serviços Lazer &amp; Sport de um jeito simples.</string>
    <string name="abertura_pergunta">Como você quer continuar?</string>
    <string name="abertura_ilustracao_descricao">Ilustração de diversão e jogos</string>
    <string name="termos_resumo">Ao continuar, você concorda com nossos Termos e Política de Privacidade.</string>
    <string name="acao_entrar">Entrar</string>
    <string name="acao_entrar_maiusculo">ENTRAR</string>
    <string name="acao_google">Continuar com Google</string>
    <string name="acao_criar_conta">Criar uma conta</string>
    <string name="acao_continuar_sem_login">Continuar sem login</string>
    <string name="acao_abrir_site">Abrir o site</string>
    <string name="acao_ver_brinquedos">Ver brinquedos agora</string>
    <string name="acao_cadastrar_site">Cadastrar pelo site</string>
    <string name="login_titulo">Entrar na sua conta</string>
    <string name="login_identificador">E-mail ou usuário</string>
    <string name="login_senha">Senha</string>
    <string name="senha_mostrar">Mostrar senha</string>
    <string name="senha_ocultar">Ocultar senha</string>
    <string name="login_sem_conta">Ainda não tenho conta</string>
    <string name="catalogo_sem_login">Ver catálogo sem entrar</string>
    <string name="tela_inicio">Início</string>
    <string name="tela_catalogo">Catálogo</string>
    <string name="tela_criar_conta">Criar conta</string>
    <string name="inicio_mensagem">Sua sessão está pronta. A próxima entrega conecta o menu aos dados reais do site.</string>
    <string name="catalogo_mensagem">O catálogo nativo será a próxima tela. Enquanto isso, você já pode ver todos os brinquedos.</string>
    <string name="cadastro_mensagem">O cadastro nativo está sendo preparado para usar a mesma conta do site.</string>
    <string name="erro_login_invalido">Login ou senha inválidos.</string>
    <string name="erro_dados_invalidos">Confira os dados informados.</string>
    <string name="erro_servidor">O servidor está fora do ar. Tente em instantes.</string>
    <string name="erro_sem_conexao">Sem conexão. Verifique sua internet.</string>
    <string name="erro_tempo_conexao">A conexão demorou demais. Tente de novo.</string>
    <string name="erro_generico">Algo deu errado. Tente novamente.</string>
</resources>
'@

$tema = @'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.LazerSport_APP" parent="Theme.Material3.Light.NoActionBar">
        <item name="colorPrimary">@color/ls_azul</item>
        <item name="colorSecondary">@color/ls_azul_escuro</item>
        <item name="colorTertiary">@color/ls_vermelho</item>
        <item name="android:fontFamily">sans</item>
        <item name="android:windowLightStatusBar">false</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowActionModeOverlay">true</item>
        <item name="android:windowNoTitle">true</item>
    </style>
</resources>
'@

$ilustracao = @'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="360dp"
    android:height="240dp"
    android:viewportWidth="360"
    android:viewportHeight="240">
    <path android:fillColor="@color/ls_azul_escuro" android:pathData="M24,0 H336 Q360,0 360,24 V216 Q360,240 336,240 H24 Q0,240 0,216 V24 Q0,0 24,0 Z" />
    <path android:fillColor="#14FFFFFF" android:pathData="M18,38 A7,7 0,1 0,32 38 A7,7 0,1 0,18 38 M323,196 A11,11 0,1 0,345 196 A11,11 0,1 0,323 196" />
    <path android:fillColor="@color/ls_azul" android:fillAlpha="0.30" android:pathData="M0,182 C76,133 135,230 205,178 C264,134 309,143 360,111 V240 H0 Z" />
    <path android:fillColor="@color/ls_texto" android:pathData="M111,31 H249 L265,190 Q266,205 251,205 H109 Q94,205 95,190 Z" />
    <path android:fillColor="@color/ls_azul" android:pathData="M126,50 H234 L244,124 H116 Z" />
    <path android:fillColor="#38FFFFFF" android:pathData="M131,57 H229 L235,106 C208,83 167,78 124,103 Z" />
    <path android:fillColor="#FFFFFF" android:pathData="M180,69 A25,25 0,1 0,180 119 A25,25 0,1 0,180 69" />
    <path android:fillColor="@color/ls_vermelho" android:pathData="M180,77 A17,17 0,1 0,180 111 A17,17 0,1 0,180 77" />
    <path android:fillColor="#FFFFFF" android:pathData="M180,85 A9,9 0,1 0,180 103 A9,9 0,1 0,180 85" />
    <path android:fillColor="#F4F6FA" android:pathData="M108,135 H252 L262,171 H98 Z" />
    <path android:fillColor="@color/ls_vermelho" android:pathData="M141,130 H148 V151 H141 Z M135,126 A10,10 0,1 0,155 126 A10,10 0,1 0,135 126" />
    <path android:fillColor="@color/ls_azul" android:pathData="M201,150 A8,8 0,1 0,217 150 A8,8 0,1 0,201 150 M224,150 A8,8 0,1 0,240 150 A8,8 0,1 0,224 150" />
    <path android:fillColor="@color/ls_texto" android:pathData="M112,202 H139 V224 H106 Z M221,202 H248 L254,224 H221 Z" />
    <path android:fillColor="@color/ls_amarelo" android:pathData="M70,59 L74,69 L85,73 L74,77 L70,88 L66,77 L55,73 L66,69 Z M294,83 L298,91 L306,95 L298,99 L294,107 L290,99 L282,95 L290,91 Z" />
</vector>
'@

Salvar-Utf8SemBom (Join-Path $raiz "build.gradle.kts") $gradleRaiz
Salvar-Utf8SemBom (Join-Path $raiz "app/build.gradle.kts") $gradleApp
Salvar-Utf8SemBom (Join-Path $raiz "app/src/main/res/values/colors.xml") $cores
Salvar-Utf8SemBom (Join-Path $raiz "app/src/main/res/values/strings.xml") $textos
Salvar-Utf8SemBom (Join-Path $raiz "app/src/main/res/values/themes.xml") $tema
Salvar-Utf8SemBom (Join-Path $raiz "app/src/main/res/values-night/themes.xml") $tema
Salvar-Utf8SemBom (Join-Path $raiz "app/src/main/res/drawable/ilustracao_abertura.xml") $ilustracao

$mainActivity = Join-Path $raiz "app/src/main/java/com/example/lazer_sport_app/MainActivity.kt"
if (Test-Path $mainActivity) {
    $conteudoMain = [System.IO.File]::ReadAllText($mainActivity)
    $conteudoMain = $conteudoMain -replace '(?m)^\s*import android\.view\.Surface\s*\r?\n', ''
    $conteudoMain = $conteudoMain -replace '(?m)^\s*import java\.lang\.reflect\.Modifier\s*\r?\n', ''
    Salvar-Utf8SemBom $mainActivity $conteudoMain
}

$themeKt = Join-Path $raiz "app/src/main/java/com/example/lazer_sport_app/ui/theme/Theme.kt"
if (Test-Path $themeKt) {
    $conteudoTema = [System.IO.File]::ReadAllText($themeKt)
    $conteudoTema = $conteudoTema.Replace("0xFF004AAD", "0xFF0758C9")
    $conteudoTema = $conteudoTema.Replace("0xFF007BFF", "0xFF0758C9")
    $conteudoTema = $conteudoTema.Replace("0xFFE63946", "0xFFE5262B")
    $conteudoTema = $conteudoTema.Replace("0xFF0D1B2E", "0xFF172033")
    $conteudoTema = $conteudoTema.Replace("0xFF1B3659", "0xFF082A5E")
    $conteudoTema = $conteudoTema.Replace("0xFFF4F6FA", "0xFFF6F8FC")
    $conteudoTema = $conteudoTema.Replace("0xFF2ECC71", "0xFF169B55")
    Salvar-Utf8SemBom $themeKt $conteudoTema
}

Write-Host "Arquivos corrigidos. Compose, View Binding e Data Binding estao ativos." -ForegroundColor Green

$gradleAtual = [System.IO.File]::ReadAllText((Join-Path $raiz "build.gradle.kts"))
if ($gradleAtual.Contains("alias(libs.plugins")) {
    throw "A correcao nao foi aplicada: ainda existe alias(libs.plugins) no Gradle da raiz."
}

if ($PularCompilacao) {
    Write-Host "Compilacao pulada. Execute depois: .\gradlew.bat :app:assembleDebug" -ForegroundColor Yellow
    exit 0
}

$gradlew = Join-Path $raiz "gradlew.bat"
if (-not (Test-Path $gradlew)) {
    throw "gradlew.bat nao foi encontrado na raiz do projeto."
}

if (-not $env:JAVA_HOME) {
    $jbrAndroidStudio = Join-Path $env:ProgramFiles "Android/Android Studio/jbr"
    if (Test-Path $jbrAndroidStudio) {
        $env:JAVA_HOME = $jbrAndroidStudio
        $env:Path = "$env:JAVA_HOME/bin;$env:Path"
        Write-Host "JDK do Android Studio configurado: $env:JAVA_HOME" -ForegroundColor Cyan
    }
}

Write-Host "Parando o Gradle e limpando somente caches gerados do projeto..." -ForegroundColor Cyan
& $gradlew --stop

$cacheProjeto = Join-Path $raiz ".gradle"
$buildApp = Join-Path $raiz "app/build"
if (Test-Path $cacheProjeto) { Remove-Item $cacheProjeto -Recurse -Force }
if (Test-Path $buildApp) { Remove-Item $buildApp -Recurse -Force }

Write-Host "Baixando dependencias e compilando o APK de teste..." -ForegroundColor Cyan
& $gradlew :app:assembleDebug --refresh-dependencies --stacktrace

if ($LASTEXITCODE -ne 0) {
    throw "A correcao do Gradle antigo foi aplicada, mas a compilacao encontrou outro erro. Envie as ultimas linhas acima, com 'Caused by'."
}

$apk = Join-Path $raiz "app/build/outputs/apk/debug/app-debug.apk"
if (Test-Path $apk) {
    Write-Host "SUCESSO! APK gerado em: $apk" -ForegroundColor Green
} else {
    Write-Host "Build concluido, mas confirme o APK em app/build/outputs/apk/debug/." -ForegroundColor Yellow
}
