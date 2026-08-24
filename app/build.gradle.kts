import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.android.legacy-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "br.com.lazersport.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // Definitivo: a Play Store recusa "com.example.*" e o
        // applicationId nao pode mudar depois da primeira publicacao.
        applicationId = "br.com.lazersport.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // O app instalado pelo Android Studio também usa a API real.
        // 10.0.2.2 só funcionaria em emulador com Django local aberto.
        buildConfigField(
            "String",
            "BASE_URL",
            "\"https://www.lazersport.com.br/api/v1/\"",
        )
    }

    // Assinatura de release lida de variaveis de ambiente ou de
    // keystore.properties, nunca do repositorio: chave versionada e chave
    // perdida. Sem os dados, o build de release continua sendo gerado sem
    // assinatura, e o Android Studio avisa na hora de publicar.
    val arquivoChaves = rootProject.file("keystore.properties")
    val chaves = Properties().apply {
        if (arquivoChaves.exists()) {
            arquivoChaves.inputStream().use { load(it) }
        }
    }

    fun chave(nome: String, ambiente: String): String? =
        (chaves.getProperty(nome) ?: System.getenv(ambiente))?.takeIf { it.isNotBlank() }

    signingConfigs {
        create("release") {
            val caminho = chave("storeFile", "LAZER_KEYSTORE")
            if (caminho != null) {
                storeFile = file(caminho)
                storePassword = chave("storePassword", "LAZER_KEYSTORE_PASSWORD")
                keyAlias = chave("keyAlias", "LAZER_KEY_ALIAS")
                keyPassword = chave("keyPassword", "LAZER_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            // Não sobrescrever BASE_URL.
            // Assim celular físico e emulador consultam o site publicado.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            // R8 fica DESLIGADO ate alguem rodar um APK de release de verdade
            // no aparelho. Retrofit, OkHttp, Hilt e kotlinx.serialization
            // trazem as proprias regras, e proguard-rules.pro cobre o resto,
            // mas ofuscacao quebra em runtime, nao no build: ligar sem testar
            // significa descobrir o problema com o app ja publicado.
            // Para ligar: troque os dois para true, gere o release, instale e
            // percorra login, catalogo, carrinho e checkout.
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")

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

    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
