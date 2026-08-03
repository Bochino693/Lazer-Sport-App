// app/src/main/java/com/example/lazer_sport_app/ui/navigation/Navegacao.kt
//
// Todo o roteamento do app em um lugar. Cada tela vira uma rota; o
// NavHost troca o conteudo sem criar Activity nova.
//
// FLUXO DE ABERTURA:
//   tem token salvo? -> sim: abre no MENU
//                    -> nao: abre em BEM_VINDO
//
// Enquanto o DataStore esta sendo lido, estaLogado vale null e o
// loading aparece. Sem isso o app piscaria a tela de boas-vindas por
// um instante pra quem ja esta logado.
//
// popUpTo(...) { inclusive = true } no login APAGA as telas de entrada
// da pilha. Sem isso, apertar "voltar" no menu joga o cliente de volta
// no login ja autenticado -- bug classico e confuso.

package com.example.lazer_sport_app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lazer_sport_app.data.AuthRepository
import com.example.lazer_sport_app.ui.login.BemVindoScreen
import com.example.lazer_sport_app.ui.login.LoginScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.lang.reflect.Modifier
import javax.inject.Inject

// ============================================================
// ROTAS
// ============================================================
// Strings soltas espalhadas pelo codigo viram erro de digitacao que
// so aparece em runtime. Centralizadas aqui, o compilador ajuda.

object Rotas {
    const val BEM_VINDO = "bem_vindo"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val MENU = "menu"
    const val CATALOGO = "catalogo"
    const val PECAS = "pecas"
    const val MANUTENCAO = "manutencao"
    const val CARRINHO = "carrinho"
    const val PEDIDOS = "pedidos"
    const val CONTA = "conta"

    // Rota com argumento: navegue com "detalhe/42"
    const val DETALHE = "detalhe/{id}"
    fun detalhe(id: Int) = "detalhe/$id"
}

// ============================================================
// VIEWMODEL DE SESSAO
// ============================================================

@HiltViewModel
class SessaoViewModel @Inject constructor(
    repositorio: AuthRepository,
) : ViewModel() {

    /** null = ainda lendo o DataStore. */
    val estaLogado: StateFlow<Boolean?> = repositorio.estaLogado
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
}

// ============================================================
// GRAFO
// ============================================================

@Composable
fun NavegacaoApp(
    navController: NavHostController = rememberNavController(),
    sessaoViewModel: SessaoViewModel = hiltViewModel(),
) {
    val logado by sessaoViewModel.estaLogado.collectAsState()
    val uriHandler = LocalUriHandler.current

    if (logado == null) {
        Carregando()
        return
    }

    val destinoInicial =
        if (logado == true) Rotas.MENU else Rotas.BEM_VINDO

    NavHost(
        navController = navController,
        startDestination = destinoInicial,
    ) {

        composable(Rotas.BEM_VINDO) {
            BemVindoScreen(
                aoEntrar = { navController.navigate(Rotas.LOGIN) },
                aoCriarConta = { navController.navigate(Rotas.REGISTRO) },
                aoContinuarSemLogin = { navController.navigate(Rotas.CATALOGO) },
                aoContinuarComGoogle = {
                    uriHandler.openUri(
                        "https://www.lazersport.com.br/accounts/google/login/?process=login"
                    )
                },
            )
        }

        composable(Rotas.LOGIN) {
            LoginScreen(
                aoEntrar = {
                    navController.navigate(Rotas.MENU) {
                        popUpTo(Rotas.BEM_VINDO) { inclusive = true }
                    }
                },
                aoCriarConta = { navController.navigate(Rotas.REGISTRO) },
                aoEntrarSemConta = { navController.navigate(Rotas.CATALOGO) },
            )
        }

        // ---- Placeholders: troque um a um pelas telas reais ----
        composable(Rotas.MENU) {
            EmConstrucao(
                nome = "Início",
                mensagem = "Sua sessão está pronta. A próxima entrega conecta o menu aos dados reais do site.",
                textoAcao = "Abrir o site",
                aoAcao = { uriHandler.openUri("https://www.lazersport.com.br/") },
            )
        }
        composable(Rotas.CATALOGO) {
            EmConstrucao(
                nome = "Catálogo",
                mensagem = "O catálogo nativo será a próxima tela. Enquanto isso, você já pode ver todos os brinquedos.",
                textoAcao = "Ver brinquedos agora",
                aoAcao = {
                    uriHandler.openUri("https://www.lazersport.com.br/brinquedos/")
                },
            )
        }
        composable(Rotas.REGISTRO) {
            EmConstrucao(
                nome = "Criar conta",
                mensagem = "O cadastro nativo está sendo preparado para usar a mesma conta do site.",
                textoAcao = "Cadastrar pelo site",
                aoAcao = {
                    uriHandler.openUri("https://www.lazersport.com.br/registrar/")
                },
            )
        }
    }
}

@Composable
private fun Carregando() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmConstrucao(
    nome: String,
    mensagem: String,
    textoAcao: String,
    aoAcao: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = nome,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = aoAcao,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Text(textoAcao)
        }
    }
}