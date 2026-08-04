// CARRINHO -- com itens e vazio.
//
// Fonte da verdade e o DataStore local (CarrinhoRepository). O
// fechamento do pedido ainda acontece no site ou no WhatsApp, porque
// /api/v1/ nao tem checkout. As duas saidas ja levam o resumo pronto.

package com.example.lazer_sport_app.ui.carrinho

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
import androidx.compose.material.icons.filled.DeleteOutline
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
import com.example.lazer_sport_app.data.CarrinhoRepository
import com.example.lazer_sport_app.data.Contato
import com.example.lazer_sport_app.data.EstadoCarrinho
import com.example.lazer_sport_app.data.ItemCarrinho
import com.example.lazer_sport_app.ui.components.BotaoPrincipal
import com.example.lazer_sport_app.ui.components.BotaoVidro
import com.example.lazer_sport_app.ui.components.CampoLazer
import com.example.lazer_sport_app.ui.components.EstadoVazio
import com.example.lazer_sport_app.ui.components.TopoTela
import com.example.lazer_sport_app.ui.menu.fundoNoite
import com.example.lazer_sport_app.ui.menu.vidro
import com.example.lazer_sport_app.ui.menu.vidroTingido
import com.example.lazer_sport_app.ui.theme.Amarelo
import com.example.lazer_sport_app.ui.theme.AzulDardo
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.RosaMarca
import com.example.lazer_sport_app.ui.theme.TextoForte
import com.example.lazer_sport_app.ui.theme.TextoFraco
import com.example.lazer_sport_app.ui.theme.TextoMedio
import com.example.lazer_sport_app.ui.theme.Verde
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CarrinhoViewModel @Inject constructor(
    private val repositorio: CarrinhoRepository,
) : ViewModel() {

    val estado: StateFlow<EstadoCarrinho> = repositorio.estado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoCarrinho())

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
    val uriHandler = LocalUriHandler.current

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
                        BotaoPrincipal(
                            texto = "Finalizar pedido",
                            aoClicar = { uriHandler.openUri(Contato.site("carrinho/")) },
                            cor = RosaMarca,
                            icone = Icons.Filled.Lock,
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
                            text = "O pagamento é concluído no site, com PIX ou cartão " +
                                    "pelo Mercado Pago. O frete é calculado após o endereço.",
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