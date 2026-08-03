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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                aoVerCatalogo = { navController.navigate(Rotas.CATALOGO) },
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
        composable(Rotas.MENU) { EmConstrucao("Menu") }
        composable(Rotas.CATALOGO) { EmConstrucao("Catalogo") }
        composable(Rotas.REGISTRO) { EmConstrucao("Criar conta") }
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
private fun EmConstrucao(nome: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Tela \"$nome\" em construcao")
    }
}