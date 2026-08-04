// A GAVETA -- saiu de dentro do MenuScreen.kt, que tinha 1138 linhas
// fazendo tela + gaveta + cartoes + dados de demo ao mesmo tempo.
//
// O QUE MUDOU:
//   - o ModalDrawerSheet era branco (drawerContainerColor = surface).
//     Agora e transparente e o conteudo pinta o proprio azul-noite.
//   - troquei NavigationDrawerItem por linha propria: o componente do
//     Material impoe ripple e cores dele, que brigavam com o icone
//     colorido a cada toque.
//   - itens agrupados por secao em vez de uma lista corrida de oito.
//   - cabecalho mostra quem esta logado; visitante ve convite a entrar.
//   - rodape com link pro site e versao.

package com.example.lazer_sport_app.ui.menu

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lazer_sport_app.data.FonteLista
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

// ============ ROTAS ============
// A rota da lista e montada aqui e registrada em Navegacao.kt como
// "lista/{fonte}/{filtro}". Uma tela generica atende seis secoes;
// filtro 0 = sem filtro de categoria.

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
)

data class GrupoGaveta(val titulo: String, val itens: List<ItemGaveta>)

val gruposDoMenu: List<GrupoGaveta> = listOf(
    GrupoGaveta(
        titulo = "LOJA",
        itens = listOf(
            ItemGaveta("Início", Icons.Filled.Home, ROTA_MENU, AzulPastel),
            ItemGaveta(
                "Brinquedos",
                Icons.Filled.Widgets,
                rotaLista(FonteLista.BRINQUEDOS),
                AzulVivo,
            ),
            ItemGaveta(
                "Promoções",
                Icons.Filled.LocalOffer,
                rotaLista(FonteLista.PROMOCOES),
                RosaMarca,
            ),
            ItemGaveta("Combos", Icons.Filled.Star, rotaLista(FonteLista.COMBOS), Amarelo),
        ),
    ),
    GrupoGaveta(
        titulo = "SERVIÇOS",
        itens = listOf(
            ItemGaveta(
                "Peças de Reposição",
                Icons.Filled.Build,
                rotaLista(FonteLista.PECAS),
                AzulDardo,
            ),
            ItemGaveta("Manutenções", Icons.Filled.Handyman, ROTA_MANUTENCAO, RosaEscuro),
        ),
    ),
    GrupoGaveta(
        titulo = "INSTITUCIONAL",
        itens = listOf(
            ItemGaveta(
                "Estabelecimentos",
                Icons.Filled.Storefront,
                rotaLista(FonteLista.ESTABELECIMENTOS),
                Verde,
            ),
            ItemGaveta(
                "Eventos",
                Icons.Filled.CalendarMonth,
                rotaLista(FonteLista.EVENTOS),
                RosaMarca,
            ),
            ItemGaveta("Fale conosco", Icons.Filled.SupportAgent, ROTA_CONTATO, AzulDardo),
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
) {
    val uriHandler = LocalUriHandler.current

    ModalDrawerSheet(
        // Transparente de proposito: o fundo e desenhado abaixo.
        // Sem isso o Material pinta a folha inteira de surface.
        drawerContainerColor = Color.Transparent,
        drawerContentColor = TextoForte,
        drawerShape = RoundedCornerShape(topEnd = 26.dp, bottomEnd = 26.dp),
        modifier = Modifier.width(310.dp),
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
                aoFechar = aoFechar,
                aoAbrirConta = { aoEscolher(ROTA_CONTA) },
            )

            Spacer(Modifier.height(6.dp))

            gruposDoMenu.forEach { grupo ->
                TituloGrupo(grupo.titulo)
                grupo.itens.forEach { item ->
                    LinhaGaveta(item = item, aoClicar = { aoEscolher(item.rota) })
                }
                Spacer(Modifier.height(10.dp))
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )

            if (estaLogado) {
                TituloGrupo("MINHA CONTA")
                LinhaGaveta(
                    ItemGaveta("Carrinho", Icons.Filled.ShoppingCart, ROTA_CARRINHO, AzulVivo),
                ) { aoEscolher(ROTA_CARRINHO) }
                LinhaGaveta(
                    ItemGaveta(
                        "Meus pedidos",
                        Icons.AutoMirrored.Filled.ReceiptLong,
                        ROTA_PEDIDOS,
                        AzulDardo,
                    ),
                ) { aoEscolher(ROTA_PEDIDOS) }
                LinhaGaveta(
                    ItemGaveta("Minha conta", Icons.Filled.Person, ROTA_CONTA, AzulPastel),
                ) { aoEscolher(ROTA_CONTA) }
            } else {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    BotaoPrincipal(
                        texto = "Entrar",
                        aoClicar = { aoEscolher(ROTA_LOGIN) },
                        cor = RosaMarca,
                        icone = Icons.AutoMirrored.Filled.Login,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { aoEscolher(ROTA_REGISTRO) }
                            .padding(vertical = 12.dp),
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

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri("https://www.lazersport.com.br/") }
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Language,
                    contentDescription = null,
                    tint = TextoFraco,
                    modifier = Modifier.size(17.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "lazersport.com.br",
                    color = TextoFraco,
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Text(
                text = "Lazer & Sport Brinquedos · v1.0",
                color = TextoFraco.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp),
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CabecalhoGaveta(
    estaLogado: Boolean,
    nomeUsuario: String?,
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
            Spacer(Modifier.height(20.dp))
            LogoCompleta(largura = 180.dp)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .vidroTingido(
                        cor = if (estaLogado) Verde else AzulDardo,
                        raio = 16.dp,
                        intensidade = 0.16f,
                    )
                    .clickable(enabled = estaLogado, onClick = aoAbrirConta)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = if (estaLogado) Verde else AzulDardo,
                    modifier = Modifier.size(22.dp),
                )
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
                            "Entre para pedir e acompanhar"
                        },
                        color = TextoMedio,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun TituloGrupo(texto: String) {
    Text(
        text = texto,
        color = TextoFraco,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.4.sp,
        modifier = Modifier.padding(start = 22.dp, top = 12.dp, bottom = 6.dp),
    )
}

@Composable
private fun LinhaGaveta(item: ItemGaveta, aoClicar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clickable(onClick = aoClicar)
            .padding(horizontal = 10.dp, vertical = 9.dp),
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
        Text(
            text = item.rotulo,
            color = TextoForte,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}