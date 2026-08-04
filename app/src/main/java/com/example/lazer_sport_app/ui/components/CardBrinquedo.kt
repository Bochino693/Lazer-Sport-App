// Modelos de apresentacao + todos os cartoes do app.
// Este arquivo estava vazio (so a linha de package).

package com.example.lazer_sport_app.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.lazer_sport_app.ui.menu.vidro
import com.example.lazer_sport_app.ui.theme.Amarelo
import com.example.lazer_sport_app.ui.theme.AzulDardo
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.RaioCard
import com.example.lazer_sport_app.ui.theme.RosaMarca
import com.example.lazer_sport_app.ui.theme.TextoForte
import com.example.lazer_sport_app.ui.theme.TextoMedio

// ============ MODELOS DE APRESENTACAO ============
// O que a tela desenha, nao o que a API manda. Ficam aqui porque
// catalogo, pecas, eventos e estabelecimentos usam os mesmos tipos.

enum class TipoItemVitrine {
    BRINQUEDO,
    PECA,
    COMBO,
    PROMOCAO,
    ESTABELECIMENTO,
    EVENTO,
}

data class ItemVitrine(
    val id: Int,
    val nome: String,
    val preco: String? = null,
    val imagemUrl: String? = null,
    val selo: String? = null,
    val avaliacao: String? = null,
    val descricao: String? = null,
    val disponivelParaCompra: Boolean = false,
    val tipo: TipoItemVitrine = TipoItemVitrine.BRINQUEDO,
    val demonstracao: Boolean = false,
)

data class CategoriaVitrine(
    val id: Int,
    val nome: String,
    val imagemUrl: String? = null,
)

data class ConteudoMenu(
    val categorias: List<CategoriaVitrine> = emptyList(),
    val promocoes: List<ItemVitrine> = emptyList(),
    val destaques: List<ItemVitrine> = emptyList(),
    val pecas: List<ItemVitrine> = emptyList(),
    val combos: List<ItemVitrine> = emptyList(),
    val estabelecimentos: List<ItemVitrine> = emptyList(),
    val eventos: List<ItemVitrine> = emptyList(),
)

// ============ CARTOES ============

@Composable
fun CartaoItem(
    item: ItemVitrine,
    aoClicar: () -> Unit,
    aoAdicionarCarrinho: (ItemVitrine) -> Unit = {},
    aoConsultarPreco: (ItemVitrine) -> Unit = {},
    modifier: Modifier = Modifier,
    largura: Dp? = 180.dp,
) {
    Column(
        modifier = modifier
            .then(if (largura != null) Modifier.width(largura) else Modifier)
            .vidro(raio = RaioCard, intensidade = 0.07f)
            .clickable(enabled = !item.demonstracao, onClick = aoClicar),
    ) {
        Box {
            AsyncImage(
                model = item.imagemUrl,
                contentDescription = item.nome,
                // Fit e nao Crop: voce ja tinha reclamado no site de
                // brinquedo cortado no card. Aqui a foto aparece inteira.
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(6.dp),
            )
            if (item.selo != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(bottomEnd = 12.dp))
                        .background(RosaMarca)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = item.selo,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
        Column(Modifier.padding(12.dp)) {
            Text(
                text = item.nome,
                style = MaterialTheme.typography.titleMedium,
                color = TextoForte,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.avaliacao != null) {
                Spacer(Modifier.height(4.dp))
                SeloAvaliacao(item.avaliacao)
            }
            Spacer(Modifier.height(8.dp))
            if (item.demonstracao) {
                Text(
                    text = "Carregando catálogo...",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextoMedio,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(42.dp))
            } else if (item.disponivelParaCompra && item.preco != null) {
                Text(
                    text = item.preco,
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulDardo,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { aoAdicionarCarrinho(item) },
                    shape = RoundedCornerShape(13.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulDardo,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                ) {
                    Icon(
                        Icons.Filled.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "Adicionar",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                    )
                }
            } else {
                Text(
                    text = "Preço sob consulta",
                    style = MaterialTheme.typography.labelLarge,
                    color = AzulPastel,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(RosaMarca.copy(alpha = 0.16f))
                        .border(
                            1.dp,
                            RosaMarca.copy(alpha = 0.38f),
                            RoundedCornerShape(13.dp),
                        )
                        .clickable { aoConsultarPreco(item) }
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = RosaMarca,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "Consultar",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun SeloAvaliacao(avaliacao: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Star,
            contentDescription = null,
            tint = Amarelo,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = avaliacao,
            style = MaterialTheme.typography.labelSmall,
            color = TextoMedio,
        )
    }
}

/** Cartao largo com foto sangrada -- estabelecimentos e eventos. */
@Composable
fun CartaoLargo(
    item: ItemVitrine,
    aoClicar: () -> Unit = {},
    modifier: Modifier = Modifier,
    largura: Dp? = 280.dp,
    altura: Dp = 180.dp,
) {
    Box(
        modifier = modifier
            .then(if (largura != null) Modifier.width(largura) else Modifier.fillMaxWidth())
            .height(altura)
            .vidro(raio = RaioCard, intensidade = 0.05f)
            .clickable(onClick = aoClicar),
    ) {
        AsyncImage(
            model = item.imagemUrl,
            contentDescription = item.nome,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xFF050B14).copy(alpha = 0.90f)),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(15.dp),
        ) {
            Text(
                text = item.nome,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.descricao != null) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = item.descricao,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoMedio,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun BolhaCategoria(
    categoria: CategoriaVitrine,
    aoClicar: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(94.dp)
            .clickable(onClick = aoClicar),
    ) {
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
                .border(1.dp, AzulDardo.copy(alpha = 0.30f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (categoria.imagemUrl != null) {
                AsyncImage(
                    model = categoria.imagemUrl,
                    contentDescription = categoria.nome,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                )
            } else {
                Icon(Icons.Filled.Widgets, contentDescription = null, tint = AzulPastel)
            }
        }
        Spacer(Modifier.height(9.dp))
        Text(
            text = categoria.nome,
            style = MaterialTheme.typography.labelSmall,
            color = TextoForte.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CarrosselItens(
    itens: List<ItemVitrine>,
    aoAbrirItem: (Int) -> Unit,
    aoAdicionarCarrinho: (ItemVitrine) -> Unit = {},
    aoConsultarPreco: (ItemVitrine) -> Unit = {},
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        items(itens, key = { it.id }) { item ->
            CartaoItem(
                item = item,
                aoClicar = { aoAbrirItem(item.id) },
                aoAdicionarCarrinho = aoAdicionarCarrinho,
                aoConsultarPreco = aoConsultarPreco,
            )
        }
    }
}

@Composable
fun CarrosselLargo(itens: List<ItemVitrine>, aoAbrirItem: (Int) -> Unit = {}) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        items(itens, key = { it.id }) { item ->
            CartaoLargo(item = item, aoClicar = { aoAbrirItem(item.id) })
        }
    }
}