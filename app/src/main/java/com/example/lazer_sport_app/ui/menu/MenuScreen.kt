// app/src/main/java/com/example/lazer_sport_app/ui/menu/MenuScreen.kt
//
// TELA PRINCIPAL DO APP -- espelha a home do lazersport.com.br.
//
// Estrutura, de fora pra dentro:
//   ModalNavigationDrawer  -> gaveta lateral (o "drawer" do cabecalho do site)
//     Scaffold             -> TopAppBar + NavigationBar inferior
//       LazyColumn         -> as secoes roláveis, na mesma ordem do site
//
// As secoes seguem home.html:
//   hero / stats / categorias / promocoes / brinquedos em destaque /
//   manutencao / pecas / combos / estabelecimentos / eventos / contato
//
// DADOS: por enquanto vem de `dadosDemo()` no fim do arquivo, pra tela
// rodar sem depender da API (que esta fora do ar). Quando o Django
// voltar, troque `dadosDemo()` por um MenuViewModel com Hilt -- as
// data classes ja espelham os DTOs de Network.kt.
//
// DEPENDENCIA NOVA no app/build.gradle.kts:
//   implementation("io.coil-kt.coil3:coil-compose:3.1.0")
//   implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")

package com.example.lazer_sport_app.ui.menu

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
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
// Separados dos DTOs de rede de proposito: a tela nao deve quebrar
// quando o serializer do Django mudar um campo.

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
)

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

    val itensGaveta = listOf(
        ItemGaveta("Início", Icons.Filled.Home, "menu"),
        ItemGaveta("Brinquedos", Icons.Filled.Widgets, "catalogo"),
        ItemGaveta("Promoções", Icons.Filled.LocalOffer, "promocoes"),
        ItemGaveta("Combos", Icons.Filled.Star, "combos"),
        ItemGaveta("Peças de Reposição", Icons.Filled.Build, "pecas"),
        ItemGaveta("Manutenções", Icons.Filled.Build, "manutencao"),
        ItemGaveta("Estabelecimentos", Icons.Filled.Storefront, "estabelecimentos"),
        ItemGaveta("Eventos", Icons.Filled.CalendarMonth, "eventos"),
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
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "LAZER & SPORT",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { escopo.launch { estadoGaveta.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { aoNavegar("busca") }) {
                            Icon(Icons.Filled.Search, contentDescription = "Buscar")
                        }
                        IconButton(onClick = { aoNavegar("carrinho") }) {
                            BadgedBox(
                                badge = {
                                    if (itensNoCarrinho > 0) {
                                        Badge(containerColor = Vermelho) {
                                            Text("$itensNoCarrinho")
                                        }
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Filled.ShoppingCart,
                                    contentDescription = "Carrinho",
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                )
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
                        )
                    }
                }
            },
        ) { padding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {

                // ---------- HERO ----------
                item {
                    Hero(
                        aoVerCatalogo = { aoNavegar("catalogo") },
                        aoOrcamento = { aoNavegar("contato") },
                    )
                }

                // ---------- NUMEROS ----------
                item { FaixaNumeros() }

                // ---------- CATEGORIAS ----------
                if (conteudo.categorias.isNotEmpty()) {
                    item {
                        TituloSecao(
                            titulo = "Encontre pela categoria",
                            subtitulo = "Escolha o tipo de diversão para o seu espaço",
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(conteudo.categorias, key = { it.id }) { categoria ->
                                BolhaCategoria(
                                    categoria = categoria,
                                    aoClicar = { aoNavegar("categoria/${categoria.id}") },
                                )
                            }
                        }
                    }
                }

                // ---------- PROMOCOES ----------
                if (conteudo.promocoes.isNotEmpty()) {
                    item {
                        TituloSecao(
                            titulo = "Promoções",
                            subtitulo = "Condições especiais por tempo limitado",
                            acao = "Ver todas",
                            aoAcao = { aoNavegar("promocoes") },
                        )
                    }
                    item { CarrosselItens(conteudo.promocoes, aoAbrirItem) }
                }

                // ---------- DESTAQUES ----------
                if (conteudo.destaques.isNotEmpty()) {
                    item {
                        TituloSecao(
                            titulo = "Brinquedos em Destaque",
                            subtitulo = "Os mais procurados para festas e eventos",
                            acao = "Ver catálogo",
                            aoAcao = { aoNavegar("catalogo") },
                        )
                    }
                    item { CarrosselItens(conteudo.destaques, aoAbrirItem) }
                }

                // ---------- MANUTENCAO ----------
                item {
                    FaixaManutencao(aoSolicitar = { aoNavegar("manutencao") })
                }

                // ---------- PECAS ----------
                if (conteudo.pecas.isNotEmpty()) {
                    item {
                        TituloSecao(
                            titulo = "Peças de Reposição",
                            subtitulo = "Componentes originais para o seu equipamento",
                            acao = "Ver todas",
                            aoAcao = { aoNavegar("pecas") },
                        )
                    }
                    item { CarrosselItens(conteudo.pecas, aoAbrirItem) }
                }

                // ---------- COMBOS ----------
                if (conteudo.combos.isNotEmpty()) {
                    item {
                        TituloSecao(
                            titulo = "Combos",
                            subtitulo = "Pacotes montados com o melhor custo-benefício",
                        )
                    }
                    item { CarrosselItens(conteudo.combos, aoAbrirItem) }
                }

                // ---------- ESTABELECIMENTOS ----------
                if (conteudo.estabelecimentos.isNotEmpty()) {
                    item {
                        TituloSecao(
                            titulo = "Onde nossos brinquedos fazem a diferença",
                            subtitulo = "Parques, buffets e espaços que confiam na gente",
                        )
                    }
                    item { CarrosselLargo(conteudo.estabelecimentos) }
                }

                // ---------- EVENTOS ----------
                if (conteudo.eventos.isNotEmpty()) {
                    item {
                        TituloSecao(
                            titulo = "Eventos Realizados",
                            subtitulo = "Um pouco do que já montamos por aí",
                        )
                    }
                    item { CarrosselLargo(conteudo.eventos) }
                }

                // ---------- CONTATO ----------
                item { BlocoContato(aoFalarConosco = { aoNavegar("contato") }) }
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
        // Cabecalho com o degrade da marca
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .background(
                    Brush.linearGradient(
                        listOf(AzulEscuro, MaterialTheme.colorScheme.primary, Vermelho),
                    ),
                ),
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
                Text(
                    text = "LAZER & SPORT",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "Brinquedos e diversão para o seu espaço",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        itens.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.rotulo) },
                icon = { Icon(item.icone, contentDescription = null) },
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
                    Text("Entrar")
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
// SECOES
// ============================================================

@Composable
private fun Hero(
    aoVerCatalogo: () -> Unit,
    aoOrcamento: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(RaioCard))
            .background(
                Brush.linearGradient(listOf(AzulEscuro, Vermelho)),
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(24.dp),
        ) {
            Text(
                text = "Diversão que\nmovimenta o seu\nnegócio",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Locação e venda de brinquedos, arcades e equipamentos.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = aoVerCatalogo,
                    shape = RoundedCornerShape(RaioBotao),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = AzulEscuro,
                    ),
                ) {
                    Text("Ver catálogo", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = aoOrcamento) {
                    Text("Orçamento", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FaixaNumeros() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        listOf(
            "+20" to "anos de estrada",
            "+500" to "eventos montados",
            "100%" to "fabricação própria",
        ).forEach { (numero, rotulo) ->
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(RaioBotao),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = numero,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = rotulo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TituloSecao(
    titulo: String,
    subtitulo: String? = null,
    acao: String? = null,
    aoAcao: () -> Unit = {},
) {
    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            if (acao != null) {
                TextButton(onClick = aoAcao) {
                    Text(acao, style = MaterialTheme.typography.labelLarge)
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        if (subtitulo != null) {
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BolhaCategoria(
    categoria: CategoriaVitrine,
    aoClicar: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(92.dp)
            .clickable(onClick = aoClicar),
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (categoria.imagemUrl != null) {
                AsyncImage(
                    model = categoria.imagemUrl,
                    contentDescription = categoria.nome,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    Icons.Filled.Widgets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = categoria.nome,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
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
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                Spacer(Modifier.height(4.dp))
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
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(itens, key = { it.id }) { item ->
            Card(
                modifier = Modifier.width(280.dp),
                shape = RoundedCornerShape(RaioCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Box {
                    AsyncImage(
                        model = item.imagemUrl,
                        contentDescription = item.nome,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                                ),
                            ),
                    )
                    Text(
                        text = item.nome,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FaixaManutencao(aoSolicitar: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 28.dp),
        shape = RoundedCornerShape(RaioCard),
        colors = CardDefaults.cardColors(containerColor = AzulEscuro),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Build,
                contentDescription = null,
                tint = Amarelo,
                modifier = Modifier.size(38.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Precisa de manutenção?",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Assistência técnica para brinquedos e arcades, com peças originais.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = aoSolicitar,
                    shape = RoundedCornerShape(RaioBotao),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amarelo,
                        contentColor = AzulEscuro,
                    ),
                ) {
                    Text("Solicitar atendimento", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BlocoContato(aoFalarConosco: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Venha até a Lazer & Sport",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Fale com a gente para montar o orçamento do seu evento ou espaço.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = aoFalarConosco,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(RaioBotao),
        ) {
            Text("Falar com a equipe", fontWeight = FontWeight.Bold)
        }
    }
}

// ============================================================
// DADOS DE DEMONSTRACAO
// ============================================================
// Some com isso quando o MenuViewModel entrar. Existe pra tela abrir
// bonita mesmo com a API fora do ar.

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