// A GAVETA -- versão com botões de verdade.
//
// O QUE MUDOU NESTA PASSADA:
//   - cada item virou um botão com resposta física: encolhe ao apertar,
//     acende no hover, borda que ganha luz. Antes era Row + clickable
//     sem retorno nenhum.
//   - cabeçalho reorganizado: marca em cima, cartão de conta destacado,
//     e um chip de estado da API (verde/amarelo/vermelho) -- o cliente
//     vê que é o servidor, não o celular dele.
//   - itens de seção que a API ainda não publicou ficam esmaecidos e
//     avisam, em vez de abrir tela vazia.
//   - rodapé com site, WhatsApp e versão.

package com.example.lazer_sport_app.ui.menu

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lazer_sport_app.data.Contato
import com.example.lazer_sport_app.data.FonteLista
import com.example.lazer_sport_app.data.SaudeApi
import com.example.lazer_sport_app.ui.components.BotaoPrincipal
import com.example.lazer_sport_app.ui.theme.Amarelo
import com.example.lazer_sport_app.ui.theme.AzulDardo
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.AzulVivo
import com.example.lazer_sport_app.ui.theme.RosaEscuro
import com.example.lazer_sport_app.ui.theme.RosaMarca
import com.example.lazer_sport_app.ui.theme.TextoForte
import com.example.lazer_sport_app.ui.theme.TextoFraco
import com.example.lazer_sport_app.ui.theme.TextoMedio
import com.example.lazer_sport_app.ui.theme.Verde
import com.example.lazer_sport_app.ui.theme.tocavel

// ============ ROTAS ============

fun rotaLista(fonte: FonteLista, filtro: Int = 0) = "lista/${fonte.name}/$filtro"

const val ROTA_MENU = "menu"
const val ROTA_CARRINHO = "carrinho"
const val ROTA_PEDIDOS = "pedidos"
const val ROTA_CONTA = "conta"
const val ROTA_CONTATO = "contato"
const val ROTA_MANUTENCAO = "manutencao"
const val ROTA_LOGIN = "login"
const val ROTA_REGISTRO = "registro"

// ============ MODELO ============

data class ItemGaveta(
    val rotulo: String,
    val icone: ImageVector,
    val rota: String,
    val cor: Color,
    /** Nome do recurso em /status/. null = sempre disponível. */
    val recurso: String? = null,
)

data class GrupoGaveta(val titulo: String, val itens: List<ItemGaveta>)

val gruposDoMenu: List<GrupoGaveta> = listOf(
    GrupoGaveta(
        titulo = "COMPRAR",
        itens = listOf(
            ItemGaveta("Início", Icons.Filled.Home, ROTA_MENU, AzulPastel),
            ItemGaveta(
                "Brinquedos",
                Icons.Filled.Widgets,
                rotaLista(FonteLista.BRINQUEDOS),
                AzulVivo,
                "brinquedos",
            ),
            ItemGaveta(
                "Peças de reposição",
                Icons.Filled.Build,
                rotaLista(FonteLista.PECAS),
                AzulDardo,
                "pecas",
            ),
            ItemGaveta(
                "Combos",
                Icons.Filled.Star,
                rotaLista(FonteLista.COMBOS),
                Amarelo,
                "combos",
            ),
            ItemGaveta(
                "Promoções",
                Icons.Filled.LocalOffer,
                rotaLista(FonteLista.PROMOCOES),
                RosaMarca,
                "promocoes",
            ),
            ItemGaveta("Carrinho", Icons.Filled.ShoppingCart, ROTA_CARRINHO, AzulVivo),
        ),
    ),
    GrupoGaveta(
        titulo = "ATENDIMENTO",
        itens = listOf(
            ItemGaveta(
                "Manutenções",
                Icons.Filled.Handyman,
                ROTA_MANUTENCAO,
                RosaEscuro,
                "manutencoes",
            ),
            ItemGaveta(
                "Fale com um especialista",
                Icons.Filled.SupportAgent,
                ROTA_CONTATO,
                AzulDardo,
            ),
        ),
    ),
    GrupoGaveta(
        titulo = "CONHEÇA A LAZER & SPORT",
        itens = listOf(
            ItemGaveta(
                "Estabelecimentos",
                Icons.Filled.Storefront,
                rotaLista(FonteLista.ESTABELECIMENTOS),
                Verde,
                "estabelecimentos",
            ),
            ItemGaveta(
                "Eventos",
                Icons.Filled.CalendarMonth,
                rotaLista(FonteLista.EVENTOS),
                RosaMarca,
                "eventos",
            ),
        ),
    ),
)

// ============ GAVETA ============

@Composable
fun GavetaLazerSport(
    estaLogado: Boolean,
    nomeUsuario: String?,
    aoEscolher: (String) -> Unit,
    aoFechar: () -> Unit,
    itensNoCarrinho: Int = 0,
    saudeApi: SaudeApi = SaudeApi.COMPLETA,
    recursosDisponiveis: Set<String> = emptySet(),
) {
    val uriHandler = LocalUriHandler.current

    fun disponivel(recurso: String?): Boolean =
        recurso == null || recursosDisponiveis.isEmpty() || recurso in recursosDisponiveis

    ModalDrawerSheet(
        drawerContainerColor = Color.Transparent,
        drawerContentColor = TextoForte,
        drawerShape = RoundedCornerShape(topEnd = 26.dp, bottomEnd = 26.dp),
        modifier = Modifier.width(318.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fundoNoite()
                .verticalScroll(rememberScrollState()),
        ) {
            CabecalhoGaveta(
                estaLogado = estaLogado,
                nomeUsuario = nomeUsuario,
                saudeApi = saudeApi,
                aoFechar = aoFechar,
                aoAbrirConta = { aoEscolher(if (estaLogado) ROTA_CONTA else ROTA_LOGIN) },
            )

            Spacer(Modifier.height(10.dp))

            gruposDoMenu.forEach { grupo ->
                TituloGrupo(grupo.titulo)
                grupo.itens.forEach { item ->
                    BotaoGaveta(
                        item = item,
                        habilitado = disponivel(item.recurso),
                        contador = if (item.rota == ROTA_CARRINHO) itensNoCarrinho else 0,
                        aoClicar = { aoEscolher(item.rota) },
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp),
            )

            if (estaLogado) {
                TituloGrupo("MINHA CONTA")
                BotaoGaveta(
                    item = ItemGaveta(
                        "Meus pedidos",
                        Icons.AutoMirrored.Filled.ReceiptLong,
                        ROTA_PEDIDOS,
                        AzulDardo,
                        "pedidos",
                    ),
                    habilitado = disponivel("pedidos"),
                    aoClicar = { aoEscolher(ROTA_PEDIDOS) },
                )
                BotaoGaveta(
                    item = ItemGaveta(
                        "Minha conta",
                        Icons.Filled.Person,
                        ROTA_CONTA,
                        AzulPastel,
                    ),
                    aoClicar = { aoEscolher(ROTA_CONTA) },
                )
            } else {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    BotaoPrincipal(
                        texto = "Entrar",
                        aoClicar = { aoEscolher(ROTA_LOGIN) },
                        cor = RosaMarca,
                        icone = Icons.AutoMirrored.Filled.Login,
                        habilitado = saudeApi != SaudeApi.FORA,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .tocavel(
                                aoClicar = { aoEscolher(ROTA_REGISTRO) },
                                raio = 14.dp,
                                corRealce = AzulPastel,
                                habilitado = saudeApi != SaudeApi.FORA,
                            )
                            .padding(vertical = 13.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Criar conta",
                            color = AzulPastel,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = AzulPastel,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AtalhoRodape(
                    icone = Icons.Filled.Language,
                    texto = "Site",
                    cor = AzulDardo,
                    aoClicar = { uriHandler.openUri(Contato.SITE) },
                    modifier = Modifier.weight(1f),
                )
                AtalhoRodape(
                    icone = Icons.Filled.SupportAgent,
                    texto = "WhatsApp",
                    cor = Verde,
                    aoClicar = {
                        uriHandler.openUri(
                            Contato.whatsapp("Olá! Vim pelo app da Lazer & Sport.")
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Lazer & Sport Brinquedos · v1.0",
                color = TextoFraco.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 22.dp),
            )

            Spacer(Modifier.height(26.dp))
        }
    }
}

// ============ CABEÇALHO ============

@Composable
private fun CabecalhoGaveta(
    estaLogado: Boolean,
    nomeUsuario: String?,
    saudeApi: SaudeApi,
    aoFechar: () -> Unit,
    aoAbrirConta: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fundoHero()
            .statusBarsPadding(),
    ) {
        IconButton(onClick = aoFechar, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Filled.Close, contentDescription = "Fechar menu", tint = Color.White)
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Spacer(Modifier.height(14.dp))
            LogoComNome(tamanhoSimbolo = 52.dp, mostrarAssinatura = true)

            Spacer(Modifier.height(12.dp))
            ChipSaude(saudeApi)

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .tocavel(
                        aoClicar = aoAbrirConta,
                        raio = 18.dp,
                        corRealce = if (estaLogado) Verde else AzulDardo,
                    )
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            (if (estaLogado) Verde else AzulDardo).copy(alpha = 0.22f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = if (estaLogado) Verde else AzulDardo,
                        modifier = Modifier.size(21.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (estaLogado) {
                            nomeUsuario?.takeIf { it.isNotBlank() } ?: "Cliente"
                        } else {
                            "Você está como visitante"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (estaLogado) {
                            "Ver minha conta"
                        } else {
                            "Toque para entrar ou criar sua conta"
                        },
                        color = TextoMedio,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TextoFraco,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ChipSaude(saude: SaudeApi) {
    val (cor, texto) = when (saude) {
        SaudeApi.VERIFICANDO -> TextoFraco to "Conectando..."
        SaudeApi.COMPLETA -> Verde to "Catálogo sincronizado"
        SaudeApi.PARCIAL -> Amarelo to "Algumas seções em publicação"
        SaudeApi.FORA -> RosaMarca to "Sem conexão com o servidor"
    }

    Row(
        modifier = Modifier
            .vidroTingido(cor, raio = 999.dp, intensidade = 0.14f)
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).background(cor, CircleShape))
        Spacer(Modifier.width(7.dp))
        Text(
            text = texto,
            color = cor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.4.sp,
        )
    }
}

// ============ ITENS ============

@Composable
private fun TituloGrupo(texto: String) {
    Text(
        text = texto,
        color = TextoFraco,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.4.sp,
        modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 8.dp),
    )
}

@Composable
private fun BotaoGaveta(
    item: ItemGaveta,
    aoClicar: () -> Unit,
    habilitado: Boolean = true,
    contador: Int = 0,
) {
    val opacidade by animateFloatAsState(
        targetValue = if (habilitado) 1f else 0.42f,
        animationSpec = tween(220),
        label = "opacidade",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .tocavel(
                aoClicar = aoClicar,
                raio = 15.dp,
                corRealce = item.cor,
                habilitado = habilitado,
            )
            .alpha(opacidade)
            .padding(horizontal = 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .vidroTingido(item.cor, raio = 12.dp, intensidade = 0.16f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                item.icone,
                contentDescription = null,
                tint = item.cor,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.rotulo,
                color = TextoForte,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!habilitado) {
                Text(
                    text = "Em publicação",
                    color = TextoFraco,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        if (contador > 0) {
            Badge(containerColor = RosaMarca, contentColor = Color.White) {
                Text("$contador")
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun AtalhoRodape(
    icone: ImageVector,
    texto: String,
    cor: Color,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .tocavel(aoClicar = aoClicar, raio = 14.dp, corRealce = cor)
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = texto,
            color = TextoForte,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}