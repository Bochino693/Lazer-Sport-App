// Todo o roteamento do app.
//
// O QUE MUDOU: sumiram os placeholder() e o objeto Rotas. As rotas
// agora sao as constantes de GavetaMenu.kt (ROTA_*) + rotaLista(),
// que e o que MenuScreen e a gaveta ja chamavam -- antes clicar em
// qualquer item da gaveta caia em rota nao registrada.
//
// FLUXO DE ABERTURA:
//   token salvo    -> MENU
//   modo visitante -> MENU
//   nenhum         -> BEM_VINDO

package com.example.lazer_sport_app.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lazer_sport_app.data.AuthRepository
import com.example.lazer_sport_app.data.CarrinhoRepository
import com.example.lazer_sport_app.data.FonteLista
import com.example.lazer_sport_app.ui.carrinho.CarrinhoScreen
import com.example.lazer_sport_app.ui.conta.ContaScreen
import com.example.lazer_sport_app.ui.conta.PedidosScreen
import com.example.lazer_sport_app.ui.contato.ContatoScreen
import com.example.lazer_sport_app.ui.detalhe.DetalheScreen
import com.example.lazer_sport_app.ui.lista.ListaScreen
import com.example.lazer_sport_app.ui.login.BemVindoScreen
import com.example.lazer_sport_app.ui.login.LoginScreen
import com.example.lazer_sport_app.ui.login.RegistroScreen
import com.example.lazer_sport_app.ui.manutencao.ManutencaoScreen
import com.example.lazer_sport_app.ui.menu.MenuScreen
import com.example.lazer_sport_app.ui.menu.MenuViewModel
import com.example.lazer_sport_app.ui.menu.ROTA_CARRINHO
import com.example.lazer_sport_app.ui.menu.ROTA_CONTA
import com.example.lazer_sport_app.ui.menu.ROTA_CONTATO
import com.example.lazer_sport_app.ui.menu.ROTA_LOGIN
import com.example.lazer_sport_app.ui.menu.ROTA_MANUTENCAO
import com.example.lazer_sport_app.ui.menu.ROTA_MENU
import com.example.lazer_sport_app.ui.menu.ROTA_PEDIDOS
import com.example.lazer_sport_app.ui.menu.ROTA_REGISTRO
import com.example.lazer_sport_app.ui.menu.rotaLista
import com.example.lazer_sport_app.ui.theme.AzulDardo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// ROTAS EXTRAS (as demais vem de GavetaMenu.kt)
// ============================================================

const val ROTA_BEM_VINDO = "bem_vindo"
const val ROTA_LISTA = "lista/{fonte}/{filtro}"
const val ROTA_DETALHE = "detalhe/{id}"

fun rotaDetalhe(id: Int) = "detalhe/$id"

// ============================================================
// SESSAO
// ============================================================

enum class Entrada { MENU, BEM_VINDO }

@HiltViewModel
class SessaoViewModel @Inject constructor(
    private val repositorio: AuthRepository,
    carrinho: CarrinhoRepository,
) : ViewModel() {

    val nomeUsuario: StateFlow<String?> = repositorio.nomeUsuario
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val estaLogado: StateFlow<Boolean> = repositorio.estaLogado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val itensNoCarrinho: StateFlow<Int> = carrinho.quantidade
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** null = ainda lendo o DataStore. */
    val entrada: StateFlow<Entrada?> =
        combine(repositorio.estaLogado, repositorio.modoVisitante) { logado, visitante ->
            if (logado || visitante) Entrada.MENU else Entrada.BEM_VINDO
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun continuarSemLogin(aoConcluir: () -> Unit) {
        viewModelScope.launch {
            repositorio.continuarSemLogin()
            aoConcluir()
        }
    }
}

// ============================================================
// GRAFO
// ============================================================

@Composable
fun NavegacaoApp(
    navController: NavHostController = rememberNavController(),
    sessaoViewModel: SessaoViewModel = hiltViewModel(),
) {
    val entrada by sessaoViewModel.entrada.collectAsState()
    val logado by sessaoViewModel.estaLogado.collectAsState()
    val nome by sessaoViewModel.nomeUsuario.collectAsState()
    val noCarrinho by sessaoViewModel.itensNoCarrinho.collectAsState()
    val uriHandler = LocalUriHandler.current

    if (entrada == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AzulDardo)
        }
        return
    }

    fun irParaMenu() {
        navController.navigate(ROTA_MENU) {
            popUpTo(ROTA_BEM_VINDO) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun irParaCatalogo() {
        navController.navigate(rotaLista(FonteLista.BRINQUEDOS)) { launchSingleTop = true }
    }

    fun irParaLogin() {
        navController.navigate(ROTA_LOGIN) { launchSingleTop = true }
    }

    NavHost(
        navController = navController,
        startDestination = if (entrada == Entrada.MENU) ROTA_MENU else ROTA_BEM_VINDO,
    ) {

        // ---------------- ENTRADA ----------------

        composable(ROTA_BEM_VINDO) {
            BemVindoScreen(
                aoEntrar = { navController.navigate(ROTA_LOGIN) },
                aoCriarConta = { navController.navigate(ROTA_REGISTRO) },
                aoContinuarSemLogin = { sessaoViewModel.continuarSemLogin { irParaMenu() } },
                aoContinuarComGoogle = {
                    uriHandler.openUri(
                        "https://www.lazersport.com.br/accounts/google/login/?process=login"
                    )
                },
            )
        }

        composable(ROTA_LOGIN) {
            LoginScreen(
                aoEntrar = { irParaMenu() },
                aoCriarConta = { navController.navigate(ROTA_REGISTRO) },
                aoEntrarSemConta = { sessaoViewModel.continuarSemLogin { irParaMenu() } },
                aoVoltar = { navController.popBackStack() },
            )
        }

        composable(ROTA_REGISTRO) {
            RegistroScreen(
                aoConcluir = { irParaMenu() },
                aoVoltar = { navController.popBackStack() },
                aoJaTenhoConta = {
                    navController.navigate(ROTA_LOGIN) { launchSingleTop = true }
                },
            )
        }

        // ---------------- MENU ----------------

        composable(ROTA_MENU) {
            val menuViewModel: MenuViewModel = hiltViewModel()
            val estado by menuViewModel.estado.collectAsState()
            val contexto = LocalContext.current

            LaunchedEffect(estado.avisoOffline) {
                estado.avisoOffline?.let {
                    Toast.makeText(contexto, it, Toast.LENGTH_LONG).show()
                }
            }

            MenuScreen(
                conteudo = estado.conteudo,
                itensNoCarrinho = noCarrinho,
                estaLogado = logado,
                nomeUsuario = nome,
                aoNavegar = { rota -> navController.navigate(rota) { launchSingleTop = true } },
                aoAbrirItem = { id -> navController.navigate(rotaDetalhe(id)) },
            )
        }

        // ---------------- LISTAS ----------------

        composable(ROTA_LISTA) {
            ListaScreen(
                aoVoltar = { navController.popBackStack() },
                aoAbrirBrinquedo = { id -> navController.navigate(rotaDetalhe(id)) },
                aoIrCarrinho = {
                    navController.navigate(ROTA_CARRINHO) { launchSingleTop = true }
                },
            )
        }

        composable(ROTA_DETALHE) {
            DetalheScreen(
                aoVoltar = { navController.popBackStack() },
                aoIrCarrinho = {
                    navController.navigate(ROTA_CARRINHO) { launchSingleTop = true }
                },
            )
        }

        // ---------------- COMPRA ----------------

        composable(ROTA_CARRINHO) {
            CarrinhoScreen(
                aoVoltar = { navController.popBackStack() },
                aoVerCatalogo = { irParaCatalogo() },
            )
        }

        composable(ROTA_PEDIDOS) {
            PedidosScreen(
                aoVoltar = { navController.popBackStack() },
                aoEntrar = { irParaLogin() },
                aoVerCatalogo = { irParaCatalogo() },
            )
        }

        // ---------------- ATENDIMENTO ----------------

        composable(ROTA_MANUTENCAO) {
            ManutencaoScreen(
                aoVoltar = { navController.popBackStack() },
                aoEntrar = { irParaLogin() },
            )
        }

        composable(ROTA_CONTATO) {
            ContatoScreen(aoVoltar = { navController.popBackStack() })
        }

        // ---------------- CONTA ----------------

        composable(ROTA_CONTA) {
            ContaScreen(
                aoVoltar = { navController.popBackStack() },
                aoEntrar = { irParaLogin() },
                aoNavegar = { rota -> navController.navigate(rota) { launchSingleTop = true } },
            )
        }
    }
}