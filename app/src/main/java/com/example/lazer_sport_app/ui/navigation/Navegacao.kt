// app/src/main/java/com/example/lazer_sport_app/ui/navigation/Navegacao.kt
//
// Todo o roteamento do app em um lugar.
//
// FLUXO DE ABERTURA -- tres estados possiveis:
//   token salvo      -> abre no MENU (logado de verdade)
//   modo visitante   -> abre no MENU (escolheu "continuar sem login")
//   nenhum dos dois  -> abre em BEM_VINDO
//
// Enquanto o DataStore esta sendo lido, `entrada` vale null e o loading
// aparece. Sem isso o app piscaria a tela de boas-vindas pra quem ja
// passou por ela.
//
// popUpTo(BEM_VINDO) { inclusive = true } APAGA as telas de entrada da
// pilha. Sem isso, apertar "voltar" no menu joga o cliente de volta na
// tela de login -- bug classico e confuso.

package com.example.lazer_sport_app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.lazer_sport_app.ui.menu.MenuScreen
import com.example.lazer_sport_app.ui.menu.MenuViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// ROTAS
// ============================================================

object Rotas {
    const val BEM_VINDO = "bem_vindo"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val MENU = "menu"
    const val CATALOGO = "catalogo"
    const val BUSCA = "busca"
    const val PROMOCOES = "promocoes"
    const val COMBOS = "combos"
    const val PECAS = "pecas"
    const val MANUTENCAO = "manutencao"
    const val ESTABELECIMENTOS = "estabelecimentos"
    const val EVENTOS = "eventos"
    const val CONTATO = "contato"
    const val CARRINHO = "carrinho"
    const val PEDIDOS = "pedidos"
    const val CONTA = "conta"

    const val DETALHE = "detalhe/{id}"
    fun detalhe(id: Int) = "detalhe/$id"

    const val CATEGORIA = "categoria/{id}"
    fun categoria(id: Int) = "categoria/$id"
}

// ============================================================
// ESTADO DE ENTRADA
// ============================================================

/** Por onde o app deve abrir. */
enum class Entrada { MENU, BEM_VINDO }

@HiltViewModel
class SessaoViewModel @Inject constructor(
    private val repositorio: AuthRepository,
) : ViewModel() {

    val nomeUsuario: StateFlow<String?> = repositorio.nomeUsuario
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val estaLogado: StateFlow<Boolean> = repositorio.estaLogado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

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
    val uriHandler = LocalUriHandler.current

    if (entrada == null) {
        Carregando()
        return
    }

    // Leva pro menu limpando a pilha de entrada. Usado tanto pelo login
    // quanto pelo "continuar sem login" -- os dois terminam no mesmo lugar.
    fun irParaMenu() {
        navController.navigate(Rotas.MENU) {
            popUpTo(Rotas.BEM_VINDO) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (entrada == Entrada.MENU) Rotas.MENU else Rotas.BEM_VINDO,
    ) {

        composable(Rotas.BEM_VINDO) {
            BemVindoScreen(
                aoEntrar = { navController.navigate(Rotas.LOGIN) },
                aoCriarConta = { navController.navigate(Rotas.REGISTRO) },
                // ERA o bug: mandava pro CATALOGO (placeholder) em vez do MENU.
                // Agora marca o modo visitante e abre o menu de verdade.
                aoContinuarSemLogin = {
                    sessaoViewModel.continuarSemLogin { irParaMenu() }
                },
                aoContinuarComGoogle = {
                    uriHandler.openUri(
                        "https://www.lazersport.com.br/accounts/google/login/?process=login"
                    )
                },
            )
        }

        composable(Rotas.LOGIN) {
            LoginScreen(
                aoEntrar = { irParaMenu() },
                aoCriarConta = { navController.navigate(Rotas.REGISTRO) },
                aoEntrarSemConta = {
                    sessaoViewModel.continuarSemLogin { irParaMenu() }
                },
            )
        }

        // ---------------- MENU ----------------
        composable(Rotas.MENU) {
            val menuViewModel: MenuViewModel = hiltViewModel()
            val estado by menuViewModel.estado.collectAsState()

            MenuScreen(
                conteudo = estado.conteudo,
                estaLogado = logado,
                aoNavegar = { rota -> navController.navigate(rota) },
                aoAbrirItem = { id -> navController.navigate(Rotas.detalhe(id)) },
            )
        }

        // ---------------- PLACEHOLDERS ----------------
        // Troque um a um pelas telas reais. Todas precisam existir aqui:
        // clicar numa rota nao registrada derruba o app.

        placeholder(Rotas.CATALOGO, "Catálogo", "brinquedos/")
        placeholder(Rotas.BUSCA, "Busca", "brinquedos/")
        placeholder(Rotas.PROMOCOES, "Promoções", "loja/")
        placeholder(Rotas.COMBOS, "Combos", "loja/")
        placeholder(Rotas.PECAS, "Peças de Reposição", "pecas-reposicao/")
        placeholder(Rotas.MANUTENCAO, "Manutenções", "manutencoes/")
        placeholder(Rotas.ESTABELECIMENTOS, "Estabelecimentos", "estabelecimentos/")
        placeholder(Rotas.EVENTOS, "Eventos", "eventos/")
        placeholder(Rotas.CONTATO, "Contato", "")
        placeholder(Rotas.CARRINHO, "Carrinho", "carrinho/")
        placeholder(Rotas.PEDIDOS, "Meus pedidos", "meus-pedidos/")
        placeholder(Rotas.CONTA, "Minha conta", "perfil/")
        placeholder(Rotas.REGISTRO, "Criar conta", "registrar/")
        placeholder(Rotas.DETALHE, "Detalhe", "brinquedos/")
        placeholder(Rotas.CATEGORIA, "Categoria", "brinquedos/")
    }
}

// Registra uma rota placeholder que aponta pro trecho equivalente do site.
private fun androidx.navigation.NavGraphBuilder.placeholder(
    rota: String,
    nome: String,
    caminhoNoSite: String,
) {
    composable(rota) {
        val uriHandler = LocalUriHandler.current
        EmConstrucao(
            nome = nome,
            mensagem = "Esta tela nativa está sendo construída. " +
                    "Por enquanto você pode acessar pelo site.",
            textoAcao = "Abrir no site",
            aoAcao = {
                uriHandler.openUri("https://www.lazersport.com.br/$caminhoNoSite")
            },
        )
    }
}

@Composable
private fun Carregando() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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