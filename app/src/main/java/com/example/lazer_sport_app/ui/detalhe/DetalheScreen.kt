// Detalhe do brinquedo. Rota "detalhe/{id}".
// Usa CatalogoRepository.detalhe(), que ja existe no Network.kt.

package com.example.lazer_sport_app.ui.detalhe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.example.lazer_sport_app.data.BrinquedoDetalheDto
import com.example.lazer_sport_app.data.CarrinhoRepository
import com.example.lazer_sport_app.data.CatalogoRepository
import com.example.lazer_sport_app.data.Contato
import com.example.lazer_sport_app.data.Resultado
import com.example.lazer_sport_app.data.formatarReal
import com.example.lazer_sport_app.ui.components.BotaoPrincipal
import com.example.lazer_sport_app.ui.components.BotaoVidro
import com.example.lazer_sport_app.ui.components.EstadoCarregando
import com.example.lazer_sport_app.ui.components.EstadoVazio
import com.example.lazer_sport_app.ui.components.ItemVitrine
import com.example.lazer_sport_app.ui.components.Kicker
import com.example.lazer_sport_app.ui.components.SeloAvaliacao
import com.example.lazer_sport_app.ui.components.TipoItemVitrine
import com.example.lazer_sport_app.ui.components.TopoTela
import com.example.lazer_sport_app.ui.menu.FundoIcone
import com.example.lazer_sport_app.ui.menu.fundoNoite
import com.example.lazer_sport_app.ui.menu.vidro
import com.example.lazer_sport_app.ui.menu.vidroTingido
import com.example.lazer_sport_app.ui.theme.AzulDardo
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.RosaMarca
import com.example.lazer_sport_app.ui.theme.TextoForte
import com.example.lazer_sport_app.ui.theme.TextoMedio
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoDetalhe(
    val carregando: Boolean = true,
    val item: BrinquedoDetalheDto? = null,
    val erro: String? = null,
    val quantidade: Int = 1,
)

@HiltViewModel
class DetalheViewModel @Inject constructor(
    private val catalogo: CatalogoRepository,
    private val carrinho: CarrinhoRepository,
    handle: SavedStateHandle,
) : ViewModel() {

    private val id: Int = handle.get<String>("id")?.toIntOrNull() ?: 0

    private val _estado = MutableStateFlow(EstadoDetalhe())
    val estado: StateFlow<EstadoDetalhe> = _estado.asStateFlow()

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem: StateFlow<String?> = _mensagem.asStateFlow()

    val quantidadeCarrinho: StateFlow<Int> = carrinho.quantidade
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init { carregar() }

    fun carregar() {
        viewModelScope.launch {
            _estado.update { it.copy(carregando = true, erro = null) }
            when (val r = catalogo.detalhe(id)) {
                is Resultado.Sucesso ->
                    _estado.update { it.copy(carregando = false, item = r.dados) }
                is Resultado.Erro ->
                    _estado.update { it.copy(carregando = false, erro = r.mensagem) }
            }
        }
    }

    fun mais() = _estado.update { it.copy(quantidade = (it.quantidade + 1).coerceAtMost(99)) }
    fun menos() = _estado.update { it.copy(quantidade = (it.quantidade - 1).coerceAtLeast(1)) }

    fun adicionar() {
        val atual = _estado.value
        val item = atual.item ?: return
        viewModelScope.launch {
            carrinho.adicionar(
                ItemVitrine(
                    id = item.id,
                    nome = item.nome,
                    preco = item.valor,
                    imagemUrl = item.imagem,
                    disponivelParaCompra = true,
                    tipo = TipoItemVitrine.BRINQUEDO,
                ),
                quantidade = atual.quantidade,
            )
            _mensagem.value = "${atual.quantidade}x ${item.nome} no carrinho."
        }
    }

    fun limparMensagem() { _mensagem.value = null }
}

@Composable
fun DetalheScreen(
    aoVoltar: () -> Unit,
    aoIrCarrinho: () -> Unit,
    viewModel: DetalheViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsState()
    val noCarrinho by viewModel.quantidadeCarrinho.collectAsState()
    val mensagem by viewModel.mensagem.collectAsState()
    val uriHandler = LocalUriHandler.current
    val hostSnackbar = remember { SnackbarHostState() }

    LaunchedEffect(mensagem) {
        mensagem?.let {
            hostSnackbar.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostSnackbar) },
        topBar = {
            TopoTela(
                titulo = estado.item?.nome ?: "Detalhes",
                subtitulo = "Lazer & Sport Brinquedos",
                aoVoltar = aoVoltar,
                acoes = {
                    FundoIcone(corFundo = RosaMarca.copy(alpha = 0.22f)) {
                        IconButton(onClick = aoIrCarrinho) {
                            BadgedBox(
                                badge = {
                                    if (noCarrinho > 0) {
                                        Badge(
                                            containerColor = RosaMarca,
                                            contentColor = Color.White,
                                        ) { Text("$noCarrinho") }
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Filled.ShoppingCart,
                                    contentDescription = "Carrinho",
                                    tint = Color.White,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                },
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
                estado.carregando -> EstadoCarregando("Buscando informações...")

                estado.item == null -> EstadoVazio(
                    icone = Icons.Filled.CloudOff,
                    titulo = "Não deu para carregar",
                    mensagem = estado.erro
                        ?: "Este item pode ter saído do catálogo. Veja no site ou tente de novo.",
                    textoAcao = "Tentar novamente",
                    aoAcao = viewModel::carregar,
                    corIcone = RosaMarca,
                )

                else -> Conteudo(
                    item = estado.item!!,
                    quantidade = estado.quantidade,
                    aoMais = viewModel::mais,
                    aoMenos = viewModel::menos,
                    aoAdicionar = viewModel::adicionar,
                    aoConsultar = {
                        uriHandler.openUri(
                            Contato.whatsapp(
                                "Olá! Vim pelo app e quero um orçamento de: " +
                                        estado.item!!.nome
                            )
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun Conteudo(
    item: BrinquedoDetalheDto,
    quantidade: Int,
    aoMais: () -> Unit,
    aoMenos: () -> Unit,
    aoAdicionar: () -> Unit,
    aoConsultar: () -> Unit,
) {
    val valor = com.example.lazer_sport_app.data.precoParaDouble(item.valor)
    val vendeDireto = item.exibirNaLoja && valor > 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(bottom = 34.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        AsyncImage(
            model = item.imagem,
            contentDescription = item.nome,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .vidro(raio = 26.dp, intensidade = 0.05f)
                .padding(14.dp),
        )

        Spacer(Modifier.height(20.dp))

        if (item.categorias.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item.categorias.take(3).forEach { categoria ->
                    Kicker(categoria.nome.orEmpty().uppercase(), AzulDardo)
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        Text(
            text = item.nome,
            style = MaterialTheme.typography.headlineMedium,
            color = TextoForte,
        )

        if (!item.avaliacao.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            SeloAvaliacao(item.avaliacao!!)
        }

        Spacer(Modifier.height(18.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .vidro(raio = 22.dp, intensidade = 0.07f)
                .padding(18.dp),
        ) {
            if (vendeDireto) {
                Text(
                    text = "à vista",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoMedio,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatarReal(valor),
                    style = MaterialTheme.typography.displayLarge,
                    color = AzulDardo,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Contador(quantidade = quantidade, aoMais = aoMais, aoMenos = aoMenos)
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = "Subtotal ${formatarReal(valor * quantidade)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoMedio,
                    )
                }

                Spacer(Modifier.height(16.dp))
                BotaoPrincipal(
                    texto = "Adicionar ao carrinho",
                    aoClicar = aoAdicionar,
                    cor = AzulDardo,
                    icone = Icons.Filled.AddShoppingCart,
                )
                Spacer(Modifier.height(10.dp))
                BotaoVidro(
                    texto = "Falar com um vendedor",
                    aoClicar = aoConsultar,
                    icone = Icons.AutoMirrored.Filled.Chat,
                )
            } else {
                Text(
                    text = "Preço sob consulta",
                    style = MaterialTheme.typography.titleLarge,
                    color = AzulPastel,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Este equipamento é orçado conforme locação, " +
                            "período e local de montagem.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
                Spacer(Modifier.height(18.dp))
                BotaoPrincipal(
                    texto = "Pedir orçamento no WhatsApp",
                    aoClicar = aoConsultar,
                    cor = RosaMarca,
                    icone = Icons.AutoMirrored.Filled.Chat,
                )
            }
        }

        val temFicha = !item.voltz.isNullOrBlank() || item.dimensoes != null
        if (temFicha) {
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Ficha técnica",
                style = MaterialTheme.typography.titleLarge,
                color = TextoForte,
            )
            Spacer(Modifier.height(12.dp))

            if (!item.voltz.isNullOrBlank()) {
                LinhaFicha(Icons.Filled.Bolt, "Voltagem", item.voltz!!)
            }
            item.dimensoes?.let { d ->
                val medidas = listOfNotNull(
                    d.alturaM?.takeIf { it.isNotBlank() }?.let { "Altura $it m" },
                    d.larguraM?.takeIf { it.isNotBlank() }?.let { "Largura $it m" },
                    d.profundidadeM?.takeIf { it.isNotBlank() }?.let { "Profund. $it m" },
                )
                if (medidas.isNotEmpty()) {
                    Spacer(Modifier.height(9.dp))
                    LinhaFicha(Icons.Filled.Straighten, "Medidas", medidas.joinToString(" · "))
                }
            }
        }

        if (!item.descricao.isNullOrBlank()) {
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Sobre o equipamento",
                style = MaterialTheme.typography.titleLarge,
                color = TextoForte,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = item.descricao!!,
                style = MaterialTheme.typography.bodyMedium,
                color = TextoMedio,
            )
        }
    }
}

@Composable
private fun Contador(quantidade: Int, aoMais: () -> Unit, aoMenos: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = aoMenos) {
            Icon(Icons.Filled.Remove, contentDescription = "Menos", tint = TextoMedio)
        }
        Text(
            text = "$quantidade",
            color = TextoForte,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(28.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = aoMais) {
            Icon(Icons.Filled.Add, contentDescription = "Mais", tint = AzulDardo)
        }
    }
}

@Composable
private fun LinhaFicha(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    rotulo: String,
    valor: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .vidro(raio = 16.dp, intensidade = 0.05f)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .vidroTingido(AzulDardo, raio = 11.dp, intensidade = 0.16f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = AzulDardo, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(rotulo, style = MaterialTheme.typography.labelSmall, color = TextoMedio)
            Text(valor, style = MaterialTheme.typography.titleMedium, color = TextoForte)
        }
    }
}