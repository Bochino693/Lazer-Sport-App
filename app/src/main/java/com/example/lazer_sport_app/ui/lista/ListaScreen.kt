// A TELA GENERICA DE LISTA -- atende as seis fontes do FonteLista.
// Rota registrada em Navegacao.kt como "lista/{fonte}/{filtro}".
//
// Brinquedos e promocoes abrem a tela de detalhe; peca, combo,
// estabelecimento e evento abrem a folha inferior (nao tem endpoint
// de detalhe pra eles ainda, e o dado da lista ja basta).

package com.example.lazer_sport_app.ui.lista

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.lazer_sport_app.data.CatalogoRepository
import com.example.lazer_sport_app.data.CarrinhoRepository
import com.example.lazer_sport_app.data.Contato
import com.example.lazer_sport_app.data.FonteLista
import com.example.lazer_sport_app.ui.components.BotaoPrincipal
import com.example.lazer_sport_app.ui.components.BotaoVidro
import com.example.lazer_sport_app.ui.components.BarraBusca
import com.example.lazer_sport_app.ui.components.CartaoItem
import com.example.lazer_sport_app.ui.components.CartaoLargo
import com.example.lazer_sport_app.ui.components.CategoriaVitrine
import com.example.lazer_sport_app.ui.components.EstadoCarregando
import com.example.lazer_sport_app.ui.components.EstadoVazio
import com.example.lazer_sport_app.ui.components.ItemVitrine
import com.example.lazer_sport_app.ui.components.SeloAvaliacao
import com.example.lazer_sport_app.ui.components.TipoItemVitrine
import com.example.lazer_sport_app.ui.components.TopoTela
import com.example.lazer_sport_app.ui.menu.FundoIcone
import com.example.lazer_sport_app.ui.menu.fundoNoite
import com.example.lazer_sport_app.ui.menu.vidro
import com.example.lazer_sport_app.ui.menu.vidroTingido
import com.example.lazer_sport_app.ui.theme.AzulDardo
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.NoiteCartao
import com.example.lazer_sport_app.ui.theme.RosaMarca
import com.example.lazer_sport_app.ui.theme.TextoForte
import com.example.lazer_sport_app.ui.theme.TextoMedio
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============ ESTADO ============

data class EstadoLista(
    val carregando: Boolean = true,
    val carregandoMais: Boolean = false,
    val itens: List<ItemVitrine> = emptyList(),
    val categorias: List<CategoriaVitrine> = emptyList(),
    val categoriaSelecionada: Int = 0,
    val busca: String = "",
    val temMais: Boolean = false,
)

@HiltViewModel
class ListaViewModel @Inject constructor(
    private val catalogo: CatalogoRepository,
    private val carrinho: CarrinhoRepository,
    handle: SavedStateHandle,
) : ViewModel() {

    val fonte: FonteLista = runCatching {
        FonteLista.valueOf(handle.get<String>("fonte").orEmpty())
    }.getOrDefault(FonteLista.BRINQUEDOS)

    private var pagina = 1
    private var jobBusca: Job? = null

    private val _estado = MutableStateFlow(
        EstadoLista(categoriaSelecionada = handle.get<String>("filtro")?.toIntOrNull() ?: 0)
    )
    val estado: StateFlow<EstadoLista> = _estado.asStateFlow()

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem: StateFlow<String?> = _mensagem.asStateFlow()

    val quantidadeCarrinho: StateFlow<Int> = carrinho.quantidade
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        if (fonte == FonteLista.BRINQUEDOS) {
            viewModelScope.launch {
                _estado.update { it.copy(categorias = catalogo.categorias()) }
            }
        }
        recarregar()
    }

    fun recarregar() {
        pagina = 1
        viewModelScope.launch {
            _estado.update { it.copy(carregando = true) }
            val atual = _estado.value
            val p = catalogo.listar(fonte, 1, atual.busca, atual.categoriaSelecionada)
            _estado.update {
                it.copy(carregando = false, itens = p.itens, temMais = p.temMais)
            }
        }
    }

    fun carregarMais() {
        val atual = _estado.value
        if (atual.carregando || atual.carregandoMais || !atual.temMais) return

        viewModelScope.launch {
            _estado.update { it.copy(carregandoMais = true) }
            pagina += 1
            val p = catalogo.listar(fonte, pagina, atual.busca, atual.categoriaSelecionada)
            _estado.update {
                it.copy(
                    carregandoMais = false,
                    itens = it.itens + p.itens,
                    temMais = p.temMais,
                )
            }
        }
    }

    /** Debounce de 380 ms: sem isso cada tecla vira uma chamada de rede. */
    fun buscar(texto: String) {
        _estado.update { it.copy(busca = texto) }
        jobBusca?.cancel()
        jobBusca = viewModelScope.launch {
            delay(380)
            recarregar()
        }
    }

    fun selecionarCategoria(id: Int) {
        _estado.update {
            it.copy(categoriaSelecionada = if (it.categoriaSelecionada == id) 0 else id)
        }
        recarregar()
    }

    fun adicionar(item: ItemVitrine) {
        viewModelScope.launch {
            carrinho.adicionar(item)
            _mensagem.value = "${item.nome} foi para o carrinho."
        }
    }

    fun limparMensagem() {
        _mensagem.value = null
    }
}

// ============ TELA ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaScreen(
    aoVoltar: () -> Unit,
    aoAbrirBrinquedo: (Int) -> Unit,
    aoIrCarrinho: () -> Unit,
    viewModel: ListaViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsState()
    val noCarrinho by viewModel.quantidadeCarrinho.collectAsState()
    val mensagem by viewModel.mensagem.collectAsState()
    val uriHandler = LocalUriHandler.current

    val hostSnackbar = remember { SnackbarHostState() }
    var itemDaFolha by remember { mutableStateOf<ItemVitrine?>(null) }
    val estadoFolha = rememberModalBottomSheetState()

    LaunchedEffect(mensagem) {
        mensagem?.let {
            hostSnackbar.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    fun abrir(item: ItemVitrine) {
        when (item.tipo) {
            TipoItemVitrine.BRINQUEDO, TipoItemVitrine.PROMOCAO -> aoAbrirBrinquedo(item.id)
            else -> itemDaFolha = item
        }
    }

    fun consultar(item: ItemVitrine) {
        uriHandler.openUri(
            Contato.whatsapp("Olá! Vim pelo app e quero saber sobre: ${item.nome}")
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostSnackbar) },
        topBar = {
            TopoTela(
                titulo = viewModel.fonte.titulo,
                subtitulo = viewModel.fonte.subtitulo,
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .fundoNoite()
                .padding(padding),
        ) {

            BarraBusca(
                valor = estado.busca,
                aoMudar = viewModel::buscar,
                dica = "Buscar em ${viewModel.fonte.titulo.lowercase()}...",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            )

            if (estado.categorias.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    item {
                        Pilula(
                            texto = "Todas",
                            ativa = estado.categoriaSelecionada == 0,
                            aoClicar = { viewModel.selecionarCategoria(0) },
                        )
                    }
                    items(estado.categorias, key = { it.id }) { categoria ->
                        Pilula(
                            texto = categoria.nome,
                            ativa = estado.categoriaSelecionada == categoria.id,
                            aoClicar = { viewModel.selecionarCategoria(categoria.id) },
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            when {
                estado.carregando -> EstadoCarregando("Carregando ${viewModel.fonte.titulo.lowercase()}...")

                estado.itens.isEmpty() -> EstadoVazio(
                    icone = Icons.Filled.SearchOff,
                    titulo = if (estado.busca.isBlank()) {
                        "Nada por aqui ainda"
                    } else {
                        "Nenhum resultado"
                    },
                    mensagem = if (estado.busca.isBlank()) {
                        "Esta seção ainda não tem itens publicados. " +
                                "Fale com a gente que montamos sob medida."
                    } else {
                        "Não encontramos nada para \"${estado.busca}\". " +
                                "Tente outra palavra."
                    },
                    textoAcao = if (estado.busca.isBlank()) "Falar com a equipe" else "Limpar busca",
                    aoAcao = {
                        if (estado.busca.isBlank()) {
                            uriHandler.openUri(
                                Contato.whatsapp("Olá! Procuro algo em ${viewModel.fonte.titulo}.")
                            )
                        } else {
                            viewModel.buscar("")
                        }
                    },
                    corIcone = if (estado.busca.isBlank()) AzulDardo else RosaMarca,
                )

                viewModel.fonte.largo -> ListaLarga(
                    itens = estado.itens,
                    carregandoMais = estado.carregandoMais,
                    aoChegarNoFim = viewModel::carregarMais,
                    aoClicar = { abrir(it) },
                )

                else -> GradeItens(
                    itens = estado.itens,
                    carregandoMais = estado.carregandoMais,
                    aoChegarNoFim = viewModel::carregarMais,
                    aoClicar = { abrir(it) },
                    aoAdicionar = viewModel::adicionar,
                    aoConsultar = { consultar(it) },
                )
            }
        }
    }

    itemDaFolha?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { itemDaFolha = null },
            sheetState = estadoFolha,
            containerColor = NoiteCartao,
            contentColor = TextoForte,
            scrimColor = Color(0xFF03070F).copy(alpha = 0.72f),
        ) {
            FolhaItem(
                item = item,
                aoAdicionar = {
                    viewModel.adicionar(item)
                    itemDaFolha = null
                },
                aoConsultar = {
                    consultar(item)
                    itemDaFolha = null
                },
            )
        }
    }
}

// ============ PARTES ============

@Composable
private fun Pilula(texto: String, ativa: Boolean, aoClicar: () -> Unit) {
    Box(
        modifier = Modifier
            .then(
                if (ativa) {
                    Modifier.vidroTingido(AzulDardo, raio = 999.dp, intensidade = 0.24f)
                } else {
                    Modifier.vidro(raio = 999.dp, intensidade = 0.06f)
                }
            )
            .clickable(onClick = aoClicar)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = texto,
            color = if (ativa) Color.White else TextoMedio,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun GradeItens(
    itens: List<ItemVitrine>,
    carregandoMais: Boolean,
    aoChegarNoFim: () -> Unit,
    aoClicar: (ItemVitrine) -> Unit,
    aoAdicionar: (ItemVitrine) -> Unit,
    aoConsultar: (ItemVitrine) -> Unit,
) {
    val estadoGrade = rememberLazyGridState()

    LaunchedEffect(estadoGrade) {
        snapshotFlow {
            val info = estadoGrade.layoutInfo
            val ultimo = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            ultimo >= info.totalItemsCount - 4 && info.totalItemsCount > 0
        }.collect { perto -> if (perto) aoChegarNoFim() }
    }

    LazyVerticalGrid(
        state = estadoGrade,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        items(itens, key = { "${it.tipo}-${it.id}" }) { item ->
            CartaoItem(
                item = item,
                largura = null,
                aoClicar = { aoClicar(item) },
                aoAdicionarCarrinho = aoAdicionar,
                aoConsultarPreco = aoConsultar,
            )
        }
        if (carregandoMais) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = AzulDardo, strokeWidth = 3.dp)
                }
            }
        }
    }
}

@Composable
private fun ListaLarga(
    itens: List<ItemVitrine>,
    carregandoMais: Boolean,
    aoChegarNoFim: () -> Unit,
    aoClicar: (ItemVitrine) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        items(itens, key = { "${it.tipo}-${it.id}" }) { item ->
            CartaoLargo(
                item = item,
                largura = null,
                altura = 200.dp,
                aoClicar = { aoClicar(item) },
            )
        }
        item {
            if (carregandoMais) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = AzulDardo, strokeWidth = 3.dp)
                }
            } else {
                LaunchedEffect(itens.size) { aoChegarNoFim() }
            }
        }
    }
}

@Composable
private fun FolhaItem(
    item: ItemVitrine,
    aoAdicionar: () -> Unit,
    aoConsultar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(bottom = 30.dp),
    ) {
        if (item.imagemUrl != null) {
            AsyncImage(
                model = item.imagemUrl,
                contentDescription = item.nome,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f)
                    .vidro(raio = 20.dp, intensidade = 0.05f)
                    .padding(10.dp),
            )
            Spacer(Modifier.height(18.dp))
        }

        if (item.selo != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(RosaMarca)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = item.selo,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        Text(
            text = item.nome,
            style = MaterialTheme.typography.headlineMedium,
            color = TextoForte,
        )

        if (item.avaliacao != null) {
            Spacer(Modifier.height(8.dp))
            SeloAvaliacao(item.avaliacao)
        }

        if (item.descricao != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = item.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = TextoMedio,
            )
        }

        Spacer(Modifier.height(20.dp))

        if (item.disponivelParaCompra && item.preco != null) {
            Text(
                text = item.preco,
                style = MaterialTheme.typography.headlineMedium,
                color = AzulDardo,
                fontWeight = FontWeight.Black,
            )
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
                style = MaterialTheme.typography.titleMedium,
                color = AzulPastel,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))
            BotaoPrincipal(
                texto = "Pedir orçamento no WhatsApp",
                aoClicar = aoConsultar,
                cor = RosaMarca,
                icone = Icons.AutoMirrored.Filled.Chat,
            )
        }
    }
}