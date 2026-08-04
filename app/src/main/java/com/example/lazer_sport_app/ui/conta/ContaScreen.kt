// MINHA CONTA e MEUS PEDIDOS -- as duas moram aqui porque dividem
// cabecalho, cartoes e estados vazios.

package com.example.lazer_sport_app.ui.conta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lazer_sport_app.data.AuthRepository
import com.example.lazer_sport_app.data.Contato
import com.example.lazer_sport_app.data.PedidoDto
import com.example.lazer_sport_app.data.PedidosRepository
import com.example.lazer_sport_app.ui.components.BotaoVidro
import com.example.lazer_sport_app.ui.components.EstadoCarregando
import com.example.lazer_sport_app.ui.components.EstadoVazio
import com.example.lazer_sport_app.ui.components.TopoTela
import com.example.lazer_sport_app.ui.menu.fundoHero
import com.example.lazer_sport_app.ui.menu.fundoNoite
import com.example.lazer_sport_app.ui.menu.vidro
import com.example.lazer_sport_app.ui.menu.vidroTingido
import com.example.lazer_sport_app.ui.theme.Amarelo
import com.example.lazer_sport_app.ui.theme.AzulDardo
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.AzulVivo
import com.example.lazer_sport_app.ui.theme.RosaMarca
import com.example.lazer_sport_app.ui.theme.TextoForte
import com.example.lazer_sport_app.ui.theme.TextoFraco
import com.example.lazer_sport_app.ui.theme.TextoMedio
import com.example.lazer_sport_app.ui.theme.Verde
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ============ VIEWMODELS ============

@HiltViewModel
class ContaViewModel @Inject constructor(
    private val auth: AuthRepository,
) : ViewModel() {

    val estaLogado: StateFlow<Boolean> = auth.estaLogado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val nome: StateFlow<String?> = auth.nomeUsuario
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val email: StateFlow<String?> = auth.emailUsuario
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun sair(aoConcluir: () -> Unit) {
        viewModelScope.launch {
            auth.sair()
            aoConcluir()
        }
    }
}

@HiltViewModel
class PedidosViewModel @Inject constructor(
    private val repositorio: PedidosRepository,
    auth: AuthRepository,
) : ViewModel() {

    val estaLogado: StateFlow<Boolean> = auth.estaLogado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _carregando = MutableStateFlow(true)
    val carregando: StateFlow<Boolean> = _carregando.asStateFlow()

    private val _pedidos = MutableStateFlow<List<PedidoDto>>(emptyList())
    val pedidos: StateFlow<List<PedidoDto>> = _pedidos.asStateFlow()

    init { carregar() }

    fun carregar() {
        viewModelScope.launch {
            _carregando.value = true
            _pedidos.value = repositorio.listar()
            _carregando.value = false
        }
    }
}

// ============ MINHA CONTA ============

@Composable
fun ContaScreen(
    aoVoltar: () -> Unit,
    aoEntrar: () -> Unit,
    aoNavegar: (String) -> Unit,
    viewModel: ContaViewModel = hiltViewModel(),
) {
    val logado by viewModel.estaLogado.collectAsState()
    val nome by viewModel.nome.collectAsState()
    val email by viewModel.email.collectAsState()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopoTela(
                titulo = "Minha conta",
                subtitulo = "Lazer & Sport Brinquedos",
                aoVoltar = aoVoltar,
            )
        },
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .fundoNoite()
                .padding(padding),
        ) {
            if (!logado) {
                EstadoVazio(
                    icone = Icons.Filled.Person,
                    titulo = "Você está como visitante",
                    mensagem = "Entre para acompanhar pedidos, abrir chamados de " +
                            "manutenção e guardar seus dados de entrega.",
                    textoAcao = "Entrar ou criar conta",
                    aoAcao = aoEntrar,
                    corIcone = AzulDardo,
                )
                return@Box
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fundoHero()
                        .padding(horizontal = 22.dp, vertical = 26.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .background(AzulVivo, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = iniciais(nome),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = nome?.takeIf { it.isNotBlank() } ?: "Cliente",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = email.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = AzulPastel,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CartaoAcao(
                        icone = Icons.AutoMirrored.Filled.ReceiptLong,
                        titulo = "Meus pedidos",
                        descricao = "Acompanhe status, valores e entregas",
                        cor = AzulDardo,
                        aoClicar = { aoNavegar("pedidos") },
                    )
                    CartaoAcao(
                        icone = Icons.Filled.ShoppingCart,
                        titulo = "Carrinho",
                        descricao = "Continue de onde você parou",
                        cor = AzulVivo,
                        aoClicar = { aoNavegar("carrinho") },
                    )
                    CartaoAcao(
                        icone = Icons.Filled.Handyman,
                        titulo = "Manutenções",
                        descricao = "Abra e acompanhe chamados técnicos",
                        cor = RosaMarca,
                        aoClicar = { aoNavegar("manutencao") },
                    )
                    CartaoAcao(
                        icone = Icons.Filled.SupportAgent,
                        titulo = "Falar com a equipe",
                        descricao = "Orçamentos, dúvidas e visitas",
                        cor = Verde,
                        aoClicar = { aoNavegar("contato") },
                    )
                    CartaoAcao(
                        icone = Icons.Filled.Language,
                        titulo = "Abrir o site",
                        descricao = "lazersport.com.br",
                        cor = AzulPastel,
                        aoClicar = { uriHandler.openUri(Contato.site("perfil/")) },
                    )
                }

                Spacer(Modifier.height(26.dp))

                Column(Modifier.padding(horizontal = 16.dp)) {
                    BotaoVidro(
                        texto = "Sair da conta",
                        aoClicar = { viewModel.sair(aoEntrar) },
                        icone = Icons.AutoMirrored.Filled.Logout,
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Lazer & Sport Brinquedos · v1.0",
                    color = TextoFraco.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 22.dp),
                )

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

// ============ MEUS PEDIDOS ============

@Composable
fun PedidosScreen(
    aoVoltar: () -> Unit,
    aoEntrar: () -> Unit,
    aoVerCatalogo: () -> Unit,
    viewModel: PedidosViewModel = hiltViewModel(),
) {
    val logado by viewModel.estaLogado.collectAsState()
    val carregando by viewModel.carregando.collectAsState()
    val pedidos by viewModel.pedidos.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopoTela(
                titulo = "Meus pedidos",
                subtitulo = if (pedidos.isEmpty()) "Histórico de compras" else "${pedidos.size} pedido(s)",
                aoVoltar = aoVoltar,
            )
        },
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .fundoNoite()
                .padding(padding),
        ) {
            when {
                !logado -> EstadoVazio(
                    icone = Icons.Filled.Person,
                    titulo = "Entre para ver seus pedidos",
                    mensagem = "Seus pedidos ficam vinculados à sua conta " +
                            "Lazer & Sport.",
                    textoAcao = "Entrar",
                    aoAcao = aoEntrar,
                    corIcone = AzulDardo,
                )

                carregando -> EstadoCarregando("Buscando seus pedidos...")

                pedidos.isEmpty() -> EstadoVazio(
                    icone = Icons.AutoMirrored.Filled.ReceiptLong,
                    titulo = "Nenhum pedido ainda",
                    mensagem = "Quando você fechar uma compra, ela aparece aqui " +
                            "com status, itens e valores.",
                    textoAcao = "Ver catálogo",
                    aoAcao = aoVerCatalogo,
                    corIcone = AzulPastel,
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 14.dp, bottom = 34.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.navigationBarsPadding(),
                ) {
                    items(pedidos, key = { it.id }) { pedido -> CartaoPedido(pedido) }
                }
            }
        }
    }
}

@Composable
private fun CartaoPedido(pedido: PedidoDto) {
    val (cor, rotulo) = when (pedido.status) {
        "pago" -> AzulDardo to "Pago"
        "em_preparacao" -> AzulVivo to "Em preparação"
        "saiu_entrega" -> AzulPastel to "Saiu para entrega"
        "finalizado" -> Verde to "Finalizado"
        "cancelado" -> RosaMarca to "Cancelado"
        else -> Amarelo to "Aguardando pagamento"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .vidro(raio = 20.dp, intensidade = 0.07f)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Pedido #${pedido.id}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextoForte,
                )
                Text(
                    text = listOfNotNull(
                        pedido.criadoEm?.take(10),
                        if (pedido.tipoEnvio == "retirada") "Retirada" else "Entrega",
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoFraco,
                )
            }
            Box(
                modifier = Modifier
                    .vidroTingido(cor, raio = 999.dp, intensidade = 0.18f)
                    .padding(horizontal = 11.dp, vertical = 6.dp),
            ) {
                Text(
                    text = pedido.statusDisplay ?: rotulo,
                    color = cor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                )
            }
        }

        if (pedido.itens.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            pedido.itens.take(4).forEach { item ->
                Text(
                    text = "${item.quantidade}x ${item.nomeItem}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (pedido.itens.size > 4) {
                Text(
                    text = "+ ${pedido.itens.size - 4} item(ns)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoFraco,
                )
            }
        }

        pedido.totalFinal?.let { total ->
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoMedio,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = com.example.lazer_sport_app.data.formatarReal(total),
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulDardo,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

// ============ COMPARTILHADO ============

@Composable
private fun CartaoAcao(
    icone: ImageVector,
    titulo: String,
    descricao: String,
    cor: Color,
    aoClicar: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .vidro(raio = 18.dp, intensidade = 0.06f)
            .clickable(onClick = aoClicar)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .vidroTingido(cor, raio = 13.dp, intensidade = 0.16f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = TextoForte)
            Text(descricao, style = MaterialTheme.typography.labelSmall, color = TextoFraco)
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = TextoFraco,
            modifier = Modifier.size(17.dp),
        )
    }
}

private fun iniciais(nome: String?): String {
    val limpo = nome?.trim().orEmpty()
    if (limpo.isBlank()) return "LS"
    val partes = limpo.split(" ").filter { it.isNotBlank() }
    return if (partes.size == 1) {
        partes.first().take(2).uppercase()
    } else {
        (partes.first().take(1) + partes.last().take(1)).uppercase()
    }
}