// app/src/main/java/com/example/lazer_sport_app/ui/navigation/Navegacao.kt
//
// Todo o roteamento do app em um lugar. Cada tela vira uma rota; o
// NavHost troca o conteúdo sem criar Activity nova.
//
// Detalhe importante no fluxo de login: quando o usuário entra,
// usamos popUpTo(...) { inclusive = true } pra APAGAR a tela de login
// da pilha. Sem isso, apertar "voltar" no menu joga o cliente de volta
// no login já autenticado -- bug clássico e confuso.

package com.example.lazer_sport_app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lazer_sport_app.data.AuthRepository
import com.example.lazer_sport_app.ui.login.LoginScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// ============================================================
// ROTAS
// ============================================================
// Strings soltas espalhadas pelo código viram erro de digitação que
// só aparece em runtime. Centralizadas aqui, o compilador ajuda.

object Rotas {
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
// VIEWMODEL DE SESSÃO
// ============================================================

@HiltViewModel
class SessaoViewModel @Inject constructor(
    repositorio: AuthRepository,
) : ViewModel() {

    /** Se já existe token salvo, o app abre direto no menu. */
    val estaLogado: StateFlow<Boolean?> = repositorio.estaLogado
        .stateIn(
            scope = androidx.lifecycle.viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,   // null = ainda lendo o DataStore
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

    // Enquanto lê o DataStore (null), não decide nada -- evita piscar
    // a tela de login pra quem já está autenticado.
    if (logado == null) return

    val destinoInicial = if (logado == true) Rotas.MENU else Rotas.LOGIN

    NavHost(
        navController = navController,
        startDestination = destinoInicial,
    ) {

        composable(Rotas.LOGIN) {
            LoginScreen(
                aoEntrar = {
                    navController.navigate(Rotas.MENU) {
                        popUpTo(Rotas.LOGIN) { inclusive = true }
                    }
                },
                aoCriarConta = { navController.navigate(Rotas.REGISTRO) },
                aoEntrarSemConta = {
                    navController.navigate(Rotas.CATALOGO)
                },
            )
        }

        // ---- As rotas abaixo entram nas próximas fatias ----
        // Deixe comentadas até criar cada tela; descomente uma a uma.
        //
        // composable(Rotas.REGISTRO) { RegistroScreen(...) }
        // composable(Rotas.MENU)     { MenuScreen(...) }
        // composable(Rotas.CATALOGO) { CatalogoScreen(...) }
        //
        // composable(
        //     route = Rotas.DETALHE,
        //     arguments = listOf(navArgument("id") { type = NavType.IntType }),
        // ) { entrada ->
        //     val id = entrada.arguments?.getInt("id") ?: return@composable
        //     DetalheScreen(id = id)
        // }

        // Placeholder temporário: sem isso o app quebra ao logar,
        // porque a rota MENU ainda não existe. Apague quando criar
        // a MenuScreen de verdade.
        composable(Rotas.MENU) {
            TelaEmConstrucao("Menu")
        }
        composable(Rotas.CATALOGO) {
            TelaEmConstrucao("Catálogo")
        }
        composable(Rotas.REGISTRO) {
            TelaEmConstrucao("Criar conta")
        }
    }
}

@Composable
private fun TelaEmConstrucao(nome: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Tela \"$nome\" em construção")
    }
}