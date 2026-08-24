// CARRINHO -- com itens e vazio.
//
// Fonte da verdade e o DataStore local (CarrinhoRepository). O
// fechamento do pedido ainda acontece no site ou no WhatsApp, porque
// /api/v1/ nao tem checkout. As duas saidas ja levam o resumo pronto.

package br.com.lazersport.app.ui.carrinho

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import br.com.lazersport.app.data.AuthRepository
import br.com.lazersport.app.data.CarrinhoRepository
import br.com.lazersport.app.data.Resultado
import br.com.lazersport.app.data.Contato
import br.com.lazersport.app.data.EstadoCarrinho
import br.com.lazersport.app.data.ItemCarrinho
import br.com.lazersport.app.ui.components.BotaoPrincipal
import br.com.lazersport.app.ui.components.BotaoVidro
import br.com.lazersport.app.ui.components.CampoLazer
import br.com.lazersport.app.ui.components.EstadoVazio
import br.com.lazersport.app.ui.components.TopoTela
import br.com.lazersport.app.ui.menu.fundoNoite
import br.com.lazersport.app.ui.menu.vidro
import br.com.lazersport.app.ui.menu.vidroTingido
import br.com.lazersport.app.ui.theme.Amarelo
import br.com.lazersport.app.ui.theme.AzulDardo
import br.com.lazersport.app.ui.theme.AzulPastel
import br.com.lazersport.app.ui.theme.RosaMarca
import br.com.lazersport.app.ui.theme.TextoForte
import br.com.lazersport.app.ui.theme.TextoFraco
import br.com.lazersport.app.ui.theme.TextoMedio
import br.com.lazersport.app.ui.theme.Verde
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** O que a tela precisa saber enquanto o pedido e preparado no servidor. */
data class EstadoFinalizacao(
    val preparando: Boolean = false,
    val erro: String? = null,
    val abrirCheckout: String? = null,
)

@HiltViewModel
class CarrinhoViewModel @Inject constructor(
    private val repositorio: CarrinhoRepository,
    auth: AuthRepository,
) : ViewModel() {

    val estado: StateFlow<EstadoCarrinho> = repositorio.estado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoCarrinho())

    val logado: StateFlow<Boolean> = auth.estaLogado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _finalizacao = MutableStateFlow(EstadoFinalizacao())
    val finalizacao: StateFlow<EstadoFinalizacao> = _finalizacao.asStateFlow()

    /**
     * Manda o carrinho para o site e devolve o endereco do pagamento.
     *
     * O carrinho e montado offline no aparelho; e aqui que ele vira o
     * carrinho do cliente no Django. Antes desta chamada, "Finalizar
     * pedido" abria o site com o carrinho vazio -- o cliente perdia tudo
     * o que tinha escolhido.
     */
    fun finalizar() {
        if (_finalizacao.value.preparando) return

        viewModelScope.launch {
            _finalizacao.value = EstadoFinalizacao(preparando = true)

            when (val r = repositorio.sincronizar(estado.value)) {
                is Resultado.Sucesso ->
                    _finalizacao.value = EstadoFinalizacao(
                        abrirCheckout = r.dados.checkoutUrl,
                    )

                is Resultado.Erro ->
                    _finalizacao.value = EstadoFinalizacao(erro = r.mensagem)
            }
        }
    }

    /** Chamado depois que o navegador abriu, para nao reabrir na volta. */
    fun checkoutAberto() {
        _finalizacao.value = EstadoFinalizacao()
    }

    fun limparErro() {
        _finalizacao.update { it.copy(erro = null) }
    }

    fun mais(item: ItemCarrinho) = viewModelScope.launch {
        repositorio.definirQuantidade(item.chave, item.quantidade + 1)
    }

    fun menos(item: ItemCarrinho) = viewModelScope.launch {
        repositorio.definirQuantidade(item.chave, item.quantidade - 1)
    }

    fun remover(item: ItemCarrinho) = viewModelScope.launch {
        repositorio.remover(item.chave)
    }

    fun limpar() = viewModelScope.launch { repositorio.limpar() }

    fun envio(tipo: String) = viewModelScope.launch { repositorio.definirTipoEnvio(tipo) }

    fun cupom(texto: String) = viewModelScope.launch { repositorio.definirCupom(texto) }
}

@Composable
fun CarrinhoScreen(
    aoVoltar: () -> Unit,
    aoVerCatalogo: () -> Unit,
    viewModel: CarrinhoViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsState()
    val finalizacao by viewModel.finalizacao.collectAsState()
    val logado by viewModel.logado.collectAsState()
    val uriHandler = LocalUriHandler.current

    // O servidor devolveu o endereco do pagamento: abre o navegador uma vez
    // so e limpa o estado, senao voltar para o app reabriria o checkout.
    LaunchedEffect(finalizacao.abrirCheckout) {
        finalizacao.abrirCheckout?.let { url ->
            uriHandler.openUri(url)
            viewModel.checkoutAberto()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopoTela(
                titulo = "Carrinho",
                subtitulo = if (estado.vazio) {
                    "Nenhum item por enquanto"
                } else {
                    "${estado.quantidade} ${if (estado.quantidade == 1) "item" else "itens"}"
                },
                aoVoltar = aoVoltar,
                acoes = {
                    if (!estado.vazio) {
                        IconButton(onClick = viewModel::limpar) {
                            Icon(
                                Icons.Filled.DeleteOutline,
                                contentDescription = "Esvaziar carrinho",
                                tint = RosaMarca,
                            )
                        }
                    }
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
            if (estado.vazio) {
                EstadoVazio(
                    icone = Icons.Filled.RemoveShoppingCart,
                    titulo = "Seu carrinho está vazio",
                    mensagem = "Escolha brinquedos, peças ou combos no catálogo " +
                            "e eles aparecem aqui prontinhos para o orçamento.",
                    textoAcao = "Ver catálogo",
                    aoAcao = aoVerCatalogo,
                    corIcone = AzulDardo,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 14.dp, bottom = 34.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.navigationBarsPadding(),
                ) {

                    items(estado.itens, key = { it.chave }) { item ->
                        LinhaCarrinho(
                            item = item,
                            aoMais = { viewModel.mais(item) },
                            aoMenos = { viewModel.menos(item) },
                            aoRemover = { viewModel.remover(item) },
                        )
                    }

                    item {
                        Spacer(Modifier.height(6.dp))
                        SeletorEnvio(
                            selecionado = estado.tipoEnvio,
                            aoSelecionar = viewModel::envio,
                        )
                    }

                    item {
                        CampoLazer(
                            valor = estado.cupom,
                            aoMudar = viewModel::cupom,
                            rotulo = "Cupom de desconto (opcional)",
                        )
                    }

                    item {
                        Resumo(estado = estado)
                    }

                    item {
                        Spacer(Modifier.height(4.dp))

                        if (finalizacao.erro != null) {
                            AvisoFinalizacao(
                                mensagem = finalizacao.erro.orEmpty(),
                                aoFechar = viewModel::limparErro,
                            )
                            Spacer(Modifier.height(10.dp))
                        }

                        BotaoPrincipal(
                            texto = when {
                                finalizacao.preparando -> "Preparando o pagamento..."
                                !logado -> "Entrar para finalizar"
                                else -> "Finalizar pedido"
                            },
                            aoClicar = {
                                // Sem conta nao ha carrinho no servidor para
                                // receber os itens: o cliente entra primeiro.
                                if (!logado) {
                                    uriHandler.openUri(Contato.site("login/"))
                                } else {
                                    viewModel.finalizar()
                                }
                            },
                            cor = RosaMarca,
                            icone = Icons.Filled.Lock,
                            // O proprio botao mostra o giro: o cliente ve que
                            // algo esta acontecendo sem a tela mudar de lugar.
                            carregando = finalizacao.preparando,
                        )
                        Spacer(Modifier.height(10.dp))
                        BotaoVidro(
                            texto = "Enviar pelo WhatsApp",
                            aoClicar = {
                                uriHandler.openUri(Contato.whatsapp(mensagemPedido(estado)))
                            },
                            icone = Icons.AutoMirrored.Filled.Chat,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Ao finalizar, seu carrinho é enviado para o site e o " +
                                    "pagamento abre no navegador, com PIX ou cartão pelo " +
                                    "Mercado Pago. O frete é calculado após o endereço.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoFraco,
                        )
                    }
                }
            }
        }
    }
}

// ============ PARTES ============

@Composable
private fun LinhaCarrinho(
    item: ItemCarrinho,
    aoMais: () -> Unit,
    aoMenos: () -> Unit,
    aoRemover: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .vidro(raio = 20.dp, intensidade = 0.07f)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = item.imagemUrl,
                contentDescription = item.nome,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
            )
        }

        Spacer(Modifier.width(13.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = item.nome,
                style = MaterialTheme.typography.titleMedium,
                color = TextoForte,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "${item.precoFormatado} · un.",
                style = MaterialTheme.typography.labelSmall,
                color = TextoMedio,
            )
            Spacer(Modifier.height(9.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotaoContador(Icons.Filled.Remove, "Diminuir", aoMenos)
                Text(
                    text = "${item.quantidade}",
                    color = TextoForte,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                BotaoContador(Icons.Filled.Add, "Aumentar", aoMais)
                Spacer(Modifier.weight(1f))
                Text(
                    text = item.subtotalFormatado,
                    color = AzulDardo,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        IconButton(onClick = aoRemover) {
            Icon(
                Icons.Filled.DeleteOutline,
                contentDescription = "Remover",
                tint = TextoFraco,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun BotaoContador(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    descricao: String,
    aoClicar: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .clickable(onClick = aoClicar),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, contentDescription = descricao, tint = AzulPastel, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SeletorEnvio(selecionado: String, aoSelecionar: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OpcaoEnvio(
            titulo = "Entrega",
            descricao = "Calculamos o frete",
            icone = Icons.Filled.LocalShipping,
            ativa = selecionado == "frete",
            aoClicar = { aoSelecionar("frete") },
            modifier = Modifier.weight(1f),
        )
        OpcaoEnvio(
            titulo = "Retirada",
            descricao = "Jardim Peri, SP",
            icone = Icons.Filled.Storefront,
            ativa = selecionado == "retirada",
            aoClicar = { aoSelecionar("retirada") },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OpcaoEnvio(
    titulo: String,
    descricao: String,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    ativa: Boolean,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .then(
                if (ativa) {
                    Modifier.vidroTingido(AzulDardo, raio = 18.dp, intensidade = 0.18f)
                } else {
                    Modifier.vidro(raio = 18.dp, intensidade = 0.05f)
                }
            )
            .clickable(onClick = aoClicar)
            .padding(14.dp),
    ) {
        Icon(
            icone,
            contentDescription = null,
            tint = if (ativa) AzulDardo else TextoFraco,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(9.dp))
        Text(
            text = titulo,
            color = if (ativa) Color.White else TextoMedio,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = descricao,
            color = TextoFraco,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/** Erro da finalizacao, no lugar onde o cliente esta olhando.
 *  Um Toast sumiria antes de ele ler, e a decisao (tentar de novo ou
 *  entrar na conta) depende de entender o motivo. */
@Composable
private fun AvisoFinalizacao(
    mensagem: String,
    aoFechar: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RosaMarca.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = RosaMarca,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodySmall,
            color = TextoMedio,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = aoFechar) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Fechar aviso",
                tint = TextoFraco,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun Resumo(estado: EstadoCarrinho) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .vidro(raio = 22.dp, intensidade = 0.08f)
            .padding(18.dp),
    ) {
        LinhaResumo("Subtotal", estado.totalFormatado, TextoMedio)
        Spacer(Modifier.height(9.dp))
        LinhaResumo(
            rotulo = if (estado.tipoEnvio == "retirada") "Retirada" else "Frete",
            valor = if (estado.tipoEnvio == "retirada") "Grátis" else "A calcular",
            cor = if (estado.tipoEnvio == "retirada") Verde else Amarelo,
        )
        if (estado.cupom.isNotBlank()) {
            Spacer(Modifier.height(9.dp))
            LinhaResumo("Cupom ${estado.cupom.uppercase()}", "Validamos no checkout", Amarelo)
        }
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.10f))
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.titleLarge,
                color = TextoForte,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = estado.totalFormatado,
                style = MaterialTheme.typography.headlineMedium,
                color = AzulDardo,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun LinhaResumo(rotulo: String, valor: String, cor: Color) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoMedio,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = cor,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun mensagemPedido(estado: EstadoCarrinho): String {
    val linhas = estado.itens.joinToString("\n") {
        "• ${it.quantidade}x ${it.nome} — ${it.subtotalFormatado}"
    }
    val envio = if (estado.tipoEnvio == "retirada") "Retirada no local" else "Entrega (frete a calcular)"
    val cupom = if (estado.cupom.isBlank()) "" else "\nCupom: ${estado.cupom.uppercase()}"
    return "Olá! Montei este pedido no app da Lazer & Sport:\n\n$linhas" +
            "\n\nTotal dos itens: ${estado.totalFormatado}\nEnvio: $envio$cupom"
}