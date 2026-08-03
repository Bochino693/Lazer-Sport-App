// app/src/main/java/com/example/lazer_sport_app/ui/menu/MenuScreen.kt
//
// TELA PRINCIPAL DO APP -- espelha a home do lazersport.com.br.
//
// RITMO VISUAL (o que mudou nesta versao):
// O site nao e uma pagina branca com listas. Ele alterna blocos:
//
//   hero            -> azul profundo, brilhos radiais
//   categorias      -> bloco ESCURO de contraste
//   promocoes       -> cartao CLARO arredondado, com grade de fundo
//   destaques       -> cartao CLARO
//   manutencao      -> faixa AZUL solida (chamada pra acao)
//   pecas / combos  -> cartoes CLAROS
//   estabelecim.    -> bloco ESCURO
//   eventos         -> bloco ESCURO
//   contato         -> cartao CLARO
//
// Os fundos vem de FundoSecoes.kt -- degrade + brilhos + grade, tudo
// vetorial, sem nenhuma imagem no APK.
//
// DEPENDENCIAS no app/build.gradle.kts:
//   implementation("io.coil-kt.coil3:coil-compose:3.1.0")
//   implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")

package com.example.lazer_sport_app.ui.menu

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.lazer_sport_app.ui.theme.Amarelo
import com.example.lazer_sport_app.ui.theme.AzulEscuro
import com.example.lazer_sport_app.ui.theme.RaioBotao
import com.example.lazer_sport_app.ui.theme.RaioCard
import com.example.lazer_sport_app.ui.theme.Vermelho
import kotlinx.coroutines.launch

// ============================================================
// MODELOS DE APRESENTACAO
// ============================================================

data class ItemVitrine(
    val id: Int,
    val nome: String,
    val preco: String? = null,
    val imagemUrl: String? = null,
    val selo: String? = null,
    val avaliacao: String? = null,
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

private data class ItemGaveta(
    val rotulo: String,
    val icone: ImageVector,
    val rota: String,
    val cor: Color,
)

private val BrancoSuave = Color(0xFFF4F8FF)
private val AzulPill = Color(0xFF91C2FF)
private val AzulProfundo = Color(0xFF063D83)

// ============================================================
// TELA
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    conteudo: ConteudoMenu = dadosDemo(),
    itensNoCarrinho: Int = 0,
    estaLogado: Boolean = false,
    aoNavegar: (String) -> Unit = {},
    aoAbrirItem: (Int) -> Unit = {},
) {
    val estadoGaveta = rememberDrawerState(DrawerValue.Closed)
    val escopo = rememberCoroutineScope()
    var abaSelecionada by remember { mutableIntStateOf(0) }

    // Cada item ganha uma cor da marca -- a gaveta deixa de ser uma
    // lista cinza e passa a ser navegavel de relance.
    val itensGaveta = listOf(
        ItemGaveta("Início", Icons.Filled.Home, "menu", MarcaAzulProfundo),
        ItemGaveta("Brinquedos", Icons.Filled.Widgets, "catalogo", MarcaAzulVivo),
        ItemGaveta("Promoções", Icons.Filled.LocalOffer, "promocoes", MarcaRosa),
        ItemGaveta("Combos", Icons.Filled.Star, "combos", Amarelo),
        ItemGaveta("Peças de Reposição", Icons.Filled.Build, "pecas", MarcaAzulDardo),
        ItemGaveta("Manutenções", Icons.Filled.Build, "manutencao", MarcaRosaEscuro),
        ItemGaveta(
            "Estabelecimentos",
            Icons.Filled.Storefront,
            "estabelecimentos",
            MarcaAzulVivo,
        ),
        ItemGaveta("Eventos", Icons.Filled.CalendarMonth, "eventos", MarcaRosa),
    )

    ModalNavigationDrawer(
        drawerState = estadoGaveta,
        drawerContent = {
            GavetaLazerSport(
                itens = itensGaveta,
                estaLogado = estaLogado,
                aoEscolher = { rota ->
                    escopo.launch { estadoGaveta.close() }
                    aoNavegar(rota)
                },
                aoFechar = { escopo.launch { estadoGaveta.close() } },
            )
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                // O Box carrega o degrade azul; a TopAppBar fica
                // transparente por cima. E o unico jeito de ter
                // degrade no cabecalho -- containerColor so aceita
                // cor solida.
                Box(modifier = Modifier.fundoHero()) {
                    TopAppBar(
                        title = {
                            LogoComNome(
                                tamanhoSimbolo = 34.dp,
                                sobreEscuro = true,
                                mostrarAssinatura = true,
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { escopo.launch { estadoGaveta.open() } },
                            ) {
                                Icon(
                                    Icons.Filled.Menu,
                                    contentDescription = "Abrir menu",
                                    tint = Color.White,
                                )
                            }
                        },
                        actions = {
                            Row(
                                horizontalArrangement = EspacoIcones,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FundoIcone {
                                    IconButton(onClick = { aoNavegar("busca") }) {
                                        Icon(
                                            Icons.Filled.Search,
                                            contentDescription = "Buscar",
                                            tint = MarcaAzulDardo,
                                        )
                                    }
                                }
                                FundoIcone(
                                    corFundo = MarcaRosa.copy(alpha = 0.22f),
                                ) {
                                    IconButton(onClick = { aoNavegar("carrinho") }) {
                                        BadgedBox(
                                            badge = {
                                                if (itensNoCarrinho > 0) {
                                                    Badge(
                                                        containerColor = MarcaRosa,
                                                        contentColor = Color.White,
                                                    ) {
                                                        Text("$itensNoCarrinho")
                                                    }
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
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                        ),
                    )
                }
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val abas = listOf(
                        Triple("Início", Icons.Filled.Home, "menu"),
                        Triple("Catálogo", Icons.Filled.Widgets, "catalogo"),
                        Triple("Carrinho", Icons.Filled.ShoppingCart, "carrinho"),
                        Triple("Conta", Icons.Filled.Person, "conta"),
                    )
                    abas.forEachIndexed { indice, (rotulo, icone, rota) ->
                        NavigationBarItem(
                            selected = abaSelecionada == indice,
                            onClick = {
                                abaSelecionada = indice
                                aoNavegar(rota)
                            },
                            icon = { Icon(icone, contentDescription = rotulo) },
                            label = { Text(rotulo) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = MarcaAzulProfundo,
                                indicatorColor = MarcaAzulProfundo,
                                unselectedIconColor =
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor =
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            },
        ) { padding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {

                // ---------- HERO (escuro, sangrando ate as bordas) ----------
                item {
                    Hero(
                        aoVerCatalogo = { aoNavegar("catalogo") },
                        aoOrcamento = { aoNavegar("contato") },
                    )
                }

                // ---------- CATEGORIAS (bloco escuro de contraste) ----------
                if (conteudo.categorias.isNotEmpty()) {
                    item {
                        SecaoEscura(
                            kicker = "CATEGORIAS",
                            titulo = "Encontre pela categoria",
                            subtitulo = "Escolha o tipo de diversão para o seu espaço",
                        ) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 22.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                items(conteudo.categorias, key = { it.id }) { categoria ->
                                    BolhaCategoria(
                                        categoria = categoria,
                                        aoClicar = {
                                            aoNavegar("categoria/${categoria.id}")
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                // ---------- PROMOCOES (cartao claro) ----------
                if (conteudo.promocoes.isNotEmpty()) {
                    item {
                        SecaoClara(
                            kicker = "OFERTAS",
                            titulo = "Promoções",
                            subtitulo = "Condições especiais por tempo limitado",
                            acao = "Ver todas",
                            aoAcao = { aoNavegar("promocoes") },
                        ) {
                            CarrosselItens(conteudo.promocoes, aoAbrirItem)
                        }
                    }
                }

                // ---------- DESTAQUES (cartao claro) ----------
                if (conteudo.destaques.isNotEmpty()) {
                    item {
                        SecaoClara(
                            kicker = "MAIS PROCURADOS",
                            titulo = "Brinquedos em Destaque",
                            subtitulo = "Os campeões de festas, eventos e parques",
                            acao = "Ver catálogo",
                            aoAcao = { aoNavegar("catalogo") },
                        ) {
                            CarrosselItens(conteudo.destaques, aoAbrirItem)
                        }
                    }
                }

                // ---------- MANUTENCAO (faixa azul solida) ----------
                item {
                    FaixaManutencao(aoSolicitar = { aoNavegar("manutencao") })
                }

                // ---------- PECAS (cartao claro) ----------
                if (conteudo.pecas.isNotEmpty()) {
                    item {
                        SecaoClara(
                            kicker = "ASSISTÊNCIA",
                            titulo = "Peças de Reposição",
                            subtitulo = "Componentes originais para o seu equipamento",
                            acao = "Ver todas",
                            aoAcao = { aoNavegar("pecas") },
                        ) {
                            CarrosselItens(conteudo.pecas, aoAbrirItem)
                        }
                    }
                }

                // ---------- COMBOS (cartao claro) ----------
                if (conteudo.combos.isNotEmpty()) {
                    item {
                        SecaoClara(
                            kicker = "PACOTES",
                            titulo = "Combos",
                            subtitulo = "Montados com o melhor custo-benefício",
                            acao = "Ver combos",
                            aoAcao = { aoNavegar("combos") },
                        ) {
                            CarrosselItens(conteudo.combos, aoAbrirItem)
                        }
                    }
                }

                // ---------- ESTABELECIMENTOS (bloco escuro) ----------
                if (conteudo.estabelecimentos.isNotEmpty()) {
                    item {
                        SecaoEscura(
                            kicker = "PARCEIROS",
                            titulo = "Onde nossos brinquedos fazem a diferença",
                            subtitulo = "Parques, buffets e espaços que confiam na gente",
                        ) {
                            CarrosselLargo(conteudo.estabelecimentos)
                        }
                    }
                }

                // ---------- EVENTOS (bloco escuro) ----------
                if (conteudo.eventos.isNotEmpty()) {
                    item {
                        SecaoEscura(
                            kicker = "PORTFÓLIO",
                            titulo = "Eventos Realizados",
                            subtitulo = "Um pouco do que já montamos por aí",
                        ) {
                            CarrosselLargo(conteudo.eventos)
                        }
                    }
                }

                // ---------- CONTATO ----------
                item { BlocoContato(aoFalarConosco = { aoNavegar("contato") }) }
            }
        }
    }
}

// ============================================================
// ESTRUTURA DAS SECOES
// ============================================================

/** Pilula de rotulo acima do titulo -- o `.ls-section-kicker` do site. */
@Composable
private fun Kicker(texto: String, sobreEscuro: Boolean) {
    val corTexto = if (sobreEscuro) AzulPill else Color(0xFF0758C9)
    val corFundo = Color(0xFF0878F9).copy(alpha = if (sobreEscuro) 0.16f else 0.10f)
    val corBorda = Color(0xFF63A6FF).copy(alpha = 0.28f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(corFundo)
            .border(1.dp, corBorda, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            text = texto,
            color = corTexto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.4.sp,
        )
    }
}

/** Cartao claro arredondado, com grade de fundo. */
@Composable
private fun SecaoClara(
    kicker: String,
    titulo: String,
    subtitulo: String?,
    acao: String? = null,
    aoAcao: () -> Unit = {},
    conteudo: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(RaioSecao))
            .fundoSecaoClara()
            .padding(vertical = 30.dp),
    ) {
        CabecalhoSecao(
            kicker = kicker,
            titulo = titulo,
            subtitulo = subtitulo,
            acao = acao,
            aoAcao = aoAcao,
            corTitulo = corTituloSecaoClara(),
            corSubtitulo = corSubtituloSecaoClara(),
            sobreEscuro = false,
        )
        Spacer(Modifier.height(20.dp))
        conteudo()
    }
}

/** Bloco escuro sangrando ate as bordas -- quebra o branco da lista. */
@Composable
private fun SecaoEscura(
    kicker: String,
    titulo: String,
    subtitulo: String?,
    acao: String? = null,
    aoAcao: () -> Unit = {},
    conteudo: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fundoSecaoEscura()
            .padding(vertical = 34.dp),
    ) {
        CabecalhoSecao(
            kicker = kicker,
            titulo = titulo,
            subtitulo = subtitulo,
            acao = acao,
            aoAcao = aoAcao,
            corTitulo = BrancoSuave,
            corSubtitulo = Color(0xFFA9BBD4),
            sobreEscuro = true,
        )
        Spacer(Modifier.height(22.dp))
        conteudo()
    }
}

@Composable
private fun CabecalhoSecao(
    kicker: String,
    titulo: String,
    subtitulo: String?,
    acao: String?,
    aoAcao: () -> Unit,
    corTitulo: Color,
    corSubtitulo: Color,
    sobreEscuro: Boolean,
) {
    Column(Modifier.padding(horizontal = 22.dp)) {
        Kicker(kicker, sobreEscuro)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    color = corTitulo,
                    lineHeight = 30.sp,
                )
                if (subtitulo != null) {
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = corSubtitulo,
                    )
                }
            }
            if (acao != null) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF004AAD), Color(0xFF0878F9)),
                            ),
                        )
                        .clickable(onClick = aoAcao)
                        .padding(horizontal = 15.dp, vertical = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = acao,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Spacer(Modifier.width(6.dp))
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
}

// ============================================================
// HERO
// ============================================================

@Composable
private fun Hero(
    aoVerCatalogo: () -> Unit,
    aoOrcamento: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fundoHero()
            .padding(horizontal = 24.dp, vertical = 38.dp),
    ) {
        Kicker("LAZER & SPORT BRINQUEDOS", sobreEscuro = true)
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Diversão que\nmovimenta o seu\nnegócio",
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
            lineHeight = 40.sp,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Locação e venda de brinquedos, arcades e equipamentos, " +
                    "com fabricação própria e assistência técnica.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.82f),
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = aoVerCatalogo,
                shape = RoundedCornerShape(RaioBotao),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = AzulProfundo,
                ),
            ) {
                Text("Ver catálogo", fontWeight = FontWeight.Black)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(RaioBotao))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.35f),
                        RoundedCornerShape(RaioBotao),
                    )
                    .clickable(onClick = aoOrcamento)
                    .padding(horizontal = 20.dp, vertical = 13.dp),
            ) {
                Text("Orçamento", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(28.dp))
        FaixaNumeros()
    }
}

/** Numeros da empresa, em cartoes translucidos sobre o hero. */
@Composable
private fun FaixaNumeros() {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        listOf(
            "+20" to "anos de estrada",
            "+500" to "eventos montados",
            "100%" to "fabricação própria",
        ).forEach { (numero, rotulo) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.14f),
                        RoundedCornerShape(16.dp),
                    )
                    .padding(vertical = 14.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = numero,
                    style = MaterialTheme.typography.titleLarge,
                    color = AzulPill,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = rotulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                )
            }
        }
    }
}

// ============================================================
// GAVETA LATERAL
// ============================================================

@Composable
private fun GavetaLazerSport(
    itens: List<ItemGaveta>,
    estaLogado: Boolean,
    aoEscolher: (String) -> Unit,
    aoFechar: () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(178.dp)
                .fundoHero(),
        ) {
            IconButton(
                onClick = aoFechar,
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Fechar menu",
                    tint = Color.White,
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
            ) {
                LogoCompleta(largura = 186.dp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Brinquedos e diversão para o seu espaço",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        itens.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.rotulo, fontWeight = FontWeight.SemiBold) },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(item.cor.copy(alpha = 0.13f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            item.icone,
                            contentDescription = null,
                            tint = item.cor,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                },
                selected = false,
                onClick = { aoEscolher(item.rota) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        if (estaLogado) {
            NavigationDrawerItem(
                label = { Text("Meus pedidos") },
                icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
                selected = false,
                onClick = { aoEscolher("pedidos") },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
            NavigationDrawerItem(
                label = { Text("Minha conta") },
                icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                selected = false,
                onClick = { aoEscolher("conta") },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
        } else {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                Button(
                    onClick = { aoEscolher("login") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(RaioBotao),
                ) {
                    Text("Entrar", fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = { aoEscolher("registro") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Criar conta")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ============================================================
// COMPONENTES DE CONTEUDO
// ============================================================

@Composable
private fun BolhaCategoria(
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
                .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape),
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
                Icon(
                    Icons.Filled.Widgets,
                    contentDescription = null,
                    tint = AzulPill,
                )
            }
        }
        Spacer(Modifier.height(9.dp))
        Text(
            text = categoria.nome,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CarrosselItens(
    itens: List<ItemVitrine>,
    aoAbrirItem: (Int) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        items(itens, key = { it.id }) { item ->
            CartaoItem(item = item, aoClicar = { aoAbrirItem(item.id) })
        }
    }
}

@Composable
private fun CartaoItem(
    item: ItemVitrine,
    aoClicar: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = aoClicar),
        shape = RoundedCornerShape(RaioCard),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Box {
            AsyncImage(
                model = item.imagemUrl,
                contentDescription = item.nome,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            if (item.selo != null) {
                Surface(
                    color = Vermelho,
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                ) {
                    Text(
                        text = item.selo,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }
        }
        Column(Modifier.padding(12.dp)) {
            Text(
                text = item.nome,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.avaliacao != null) {
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Amarelo,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = item.avaliacao,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (item.preco != null) {
                Spacer(Modifier.height(5.dp))
                Text(
                    text = item.preco,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun CarrosselLargo(itens: List<ItemVitrine>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        items(itens, key = { it.id }) { item ->
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(180.dp)
                    .clip(RoundedCornerShape(RaioCard))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.12f),
                        RoundedCornerShape(RaioCard),
                    ),
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
                                listOf(
                                    Color.Transparent,
                                    Color(0xFF050B14).copy(alpha = 0.85f),
                                ),
                            ),
                        ),
                )
                Text(
                    text = item.nome,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(15.dp),
                )
            }
        }
    }
}

@Composable
private fun FaixaManutencao(aoSolicitar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(RaioSecao))
            .background(
                Brush.linearGradient(listOf(Color(0xFF004AAD), Color(0xFF0878F9))),
            )
            .padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Build,
                    contentDescription = null,
                    tint = Amarelo,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = "Precisa de manutenção?",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Assistência técnica para brinquedos e arcades, " +
                    "com peças originais e equipe própria.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = aoSolicitar,
            shape = RoundedCornerShape(RaioBotao),
            colors = ButtonDefaults.buttonColors(
                containerColor = Amarelo,
                contentColor = AzulProfundo,
            ),
        ) {
            Text("Solicitar atendimento", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun BlocoContato(aoFalarConosco: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(RaioSecao))
            .fundoSecaoClara()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Kicker("VISITE", sobreEscuro = false)
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Venha até a Lazer & Sport",
            style = MaterialTheme.typography.headlineMedium,
            color = corTituloSecaoClara(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Fale com a gente para montar o orçamento do seu evento ou espaço.",
            style = MaterialTheme.typography.bodyMedium,
            color = corSubtituloSecaoClara(),
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = aoFalarConosco,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(RaioBotao),
            colors = ButtonDefaults.buttonColors(containerColor = AzulEscuro),
        ) {
            Text("Falar com a equipe", fontWeight = FontWeight.Black)
        }
    }
}

// ============================================================
// DADOS DE DEMONSTRACAO
// ============================================================

fun dadosDemo(): ConteudoMenu {
    fun itens(prefixo: String, quantidade: Int, base: Int) =
        (1..quantidade).map { indice ->
            ItemVitrine(
                id = base + indice,
                nome = "$prefixo $indice",
                preco = "R$ ${(indice * 137) + 290},00",
                avaliacao = "4,${5 + (indice % 5)}",
                selo = if (indice == 1) "NOVO" else null,
            )
        }

    return ConteudoMenu(
        categorias = listOf(
            "Infláveis", "Arcades", "Mesas", "Simuladores", "Kids", "Radicais",
        ).mapIndexed { indice, nome -> CategoriaVitrine(indice + 1, nome) },
        promocoes = itens("Promoção", 5, 100),
        destaques = itens("Brinquedo", 6, 200),
        pecas = itens("Peça", 5, 300),
        combos = itens("Combo", 4, 400),
        estabelecimentos = (1..4).map {
            ItemVitrine(500 + it, "Estabelecimento parceiro $it")
        },
        eventos = (1..4).map {
            ItemVitrine(600 + it, "Evento realizado $it")
        },
    )
}