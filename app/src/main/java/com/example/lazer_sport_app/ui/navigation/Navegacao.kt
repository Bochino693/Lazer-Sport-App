// Roteamento + sessão + estado da API.
//
// A abertura agora é ABERTURA -> (BEM_VINDO | MENU). A consulta a
// /status/ acontece lá e vale pra sessão inteira: quem decide o que
// aparece na gaveta e no menu é o resultado dela, não um 404 tardio.

package com.example.lazer_sport_app.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.lazer_sport_app.data.EstadoApi
import com.example.lazer_sport_app.data.FonteLista
import com.example.lazer_sport_app.data.LoginSocialRepository
import com.example.lazer_sport_app.data.StatusRepository
import com.example.lazer_sport_app.ui.abertura.AberturaScreen
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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val ROTA_ABERTURA = "abertura"
const val ROTA_BEM_VINDO = "bem_vindo"
const val ROTA_LISTA = "lista/{fonte}/{filtro}"
const val ROTA_DETALHE = "detalhe/{id}"

fun rotaDetalhe(id: Int) = "detalhe/$id"

enum class Entrada { MENU, BEM_VINDO }

@HiltViewModel
class SessaoViewModel @Inject constructor(
    private val repositorio: AuthRepository,
    private val loginSocial: LoginSocialRepository,
    statusRepository: StatusRepository,
    carrinho: CarrinhoRepository,
) : ViewModel() {

    val estadoApi: StateFlow<EstadoApi> = statusRepository.estado

    val nomeUsuario: StateFlow<String?> = repositorio.nomeUsuario
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val estaLogado: StateFlow<Boolean> = repositorio.estaLogado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val itensNoCarrinho: StateFlow<Int> = carrinho.quantidade
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val entrada: StateFlow<Entrada?> =
        combine(repositorio.estaLogado, repositorio.modoVisitante) { logado, visitante ->
            if (logado || visitante) Entrada.MENU else Entrada.BEM_VINDO
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun urlLoginSocial(provedor: String) = loginSocial.urlEntrada(provedor)

    fun continuarSemLogin(aoConcluir: () -> Unit) {
        viewModelScope.launch {
            repositorio.continuarSemLogin()
            aoConcluir()
        }
    }
}

@Composable
fun NavegacaoApp(
    navController: NavHostController = rememberNavController(),
    sessaoViewModel: SessaoViewModel = hiltViewModel(),
) {
    val entrada by sessaoViewModel.entrada.collectAsState()
    val logado by sessaoViewModel.estaLogado.collectAsState()
    val nome by sessaoViewModel.nomeUsuario.collectAsState()
    val noCarrinho by sessaoViewModel.itensNoCarrinho.collectAsState()
    val api by sessaoViewModel.estadoApi.collectAsState()
    val uriHandler = LocalUriHandler.current

    fun irParaInicio() {
        val destino = if (entrada == Entrada.BEM_VINDO) ROTA_BEM_VINDO else ROTA_MENU
        navController.navigate(destino) {
            popUpTo(ROTA_ABERTURA) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun irParaMenu() {
        navController.navigate(ROTA_MENU) {
            popUpTo(ROTA_ABERTURA) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun irParaCatalogo() {
        navController.navigate(rotaLista(FonteLista.BRINQUEDOS)) { launchSingleTop = true }
    }

    fun irParaLogin() {
        navController.navigate(ROTA_LOGIN) { launchSingleTop = true }
    }

    // O deep link do Google chega pelo TokenStore: quando `logado` vira
    // true e a tela atual ainda é de entrada, o app já pula pro menu.
    LaunchedEffect(logado) {
        val atual = navController.currentDestination?.route
        if (logado && (atual == ROTA_LOGIN || atual == ROTA_BEM_VINDO || atual == ROTA_REGISTRO)) {
            irParaMenu()
        }
    }

    NavHost(navController = navController, startDestination = ROTA_ABERTURA) {

        composable(ROTA_ABERTURA) {
            AberturaScreen(aoContinuar = { irParaInicio() })
        }

        composable(ROTA_BEM_VINDO) {
            BemVindoScreen(
                aoEntrar = { navController.navigate(ROTA_LOGIN) },
                aoCriarConta = { navController.navigate(ROTA_REGISTRO) },
                aoContinuarSemLogin = { sessaoViewModel.continuarSemLogin { irParaMenu() } },
                aoContinuarComGoogle = {
                    // ERA a URL do site (o token nunca voltava). Agora é o
                    // fluxo /auth/app/entrar/, que devolve pelo deep link.
                    uriHandler.openUri(sessaoViewModel.urlLoginSocial("google"))
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
                aoJaTenhoConta = { navController.navigate(ROTA_LOGIN) { launchSingleTop = true } },
            )
        }

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
                carregando = estado.carregando,
                itensNoCarrinho = noCarrinho,
                estaLogado = logado,
                nomeUsuario = nome,
                saudeApi = api.saude,
                recursosDisponiveis = api.recursos,
                aoNavegar = { rota -> navController.navigate(rota) { launchSingleTop = true } },
                aoAbrirItem = { id -> navController.navigate(rotaDetalhe(id)) },
            )
        }

        composable(ROTA_LISTA) {
            ListaScreen(
                aoVoltar = { navController.popBackStack() },
                aoAbrirBrinquedo = { id -> navController.navigate(rotaDetalhe(id)) },
                aoIrCarrinho = { navController.navigate(ROTA_CARRINHO) { launchSingleTop = true } },
            )
        }

        composable(ROTA_DETALHE) {
            DetalheScreen(
                aoVoltar = { navController.popBackStack() },
                aoIrCarrinho = { navController.navigate(ROTA_CARRINHO) { launchSingleTop = true } },
            )
        }

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

        composable(ROTA_MANUTENCAO) {
            ManutencaoScreen(
                aoVoltar = { navController.popBackStack() },
                aoEntrar = { irParaLogin() },
            )
        }

        composable(ROTA_CONTATO) {
            ContatoScreen(aoVoltar = { navController.popBackStack() })
        }

        composable(ROTA_CONTA) {
            ContaScreen(
                aoVoltar = { navController.popBackStack() },
                aoEntrar = { irParaLogin() },
                aoNavegar = { rota -> navController.navigate(rota) { launchSingleTop = true } },
            )
        }
    }
}