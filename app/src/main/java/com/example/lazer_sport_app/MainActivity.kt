// app/src/main/java/com/example/lazer_sport_app/MainActivity.kt
//
// A ÚNICA Activity do app. No Compose ela é só o casulo: liga o tema,
// chama o grafo de navegação e sai da frente. Toda tela nova vira uma
// função @Composable + uma rota em Navegacao.kt -- você não cria
// Activity nem mexe no AndroidManifest de novo.
//
// @AndroidEntryPoint é obrigatório: sem ele o Hilt não consegue
// injetar os ViewModels e o app crasha ao abrir com
// "hiltViewModel() must be called from a @AndroidEntryPoint".

package com.example.lazer_sport_app

import android.os.Bundle
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.lazer_sport_app.ui.navigation.NavegacaoApp
import com.example.lazer_sport_app.ui.theme.LazerSportTheme
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Modifier

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Faz o conteúdo desenhar atrás das barras do sistema --
        // é o que deixa o degradê da tela de login subir até o topo.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            LazerSportTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NavegacaoApp()
                }
            }
        }
    }
}