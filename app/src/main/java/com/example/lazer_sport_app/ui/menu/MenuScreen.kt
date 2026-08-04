// TELA PRINCIPAL -- espelha a home do lazersport.com.br.
//
// Passou de 1138 pra ~330 linhas: gaveta foi pra GavetaMenu.kt, modelos
// e cartoes pra ui/components.
//
// RITMO VISUAL (agora sem nenhum bloco claro):
//   hero -> escuro -> azul-aco -> escuro -> faixa azul -> azul-aco ->
//   escuro -> azul-aco -> escuro -> faixa rosa
// A alternancia e o que da o ritmo que antes vinha do branco.

package com.example.lazer_sport_app.ui.menu

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lazer_sport_app.data.FonteLista
import com.example.lazer_sport_app.ui.components.BolhaCategoria
import com.example.lazer_sport_app.ui.components.BotaoPrincipal
import com.example.lazer_sport_app.ui.components.CarrosselItens
import com.example.lazer_sport_app.ui.components.CarrosselLargo
import com.example.lazer_sport_app.ui.components.ConteudoMenu
import com.example.lazer_sport_app.ui.components.Kicker
import com.example.lazer_sport_app.ui.components.SecaoAzul
import com.example.lazer_sport_app.ui.components.SecaoEscura
import com.example.lazer_sport_app.ui.theme.Amarelo
import com.example.lazer_sport_app.ui.theme.AzulDardo
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.AzulVivo
import com.example.lazer_sport_app.ui.theme.NoiteMeio
import com.example.lazer_sport_app.ui.theme.RaioBotao
import com.example.lazer_sport_app.ui.theme.RaioSecao
import com.example.lazer_sport_app.ui.theme.RosaMarca
import com.example.lazer_sport_app.ui.theme.TextoFraco
import com.example.lazer_sport_app.ui.theme.TextoMedio
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    conteudo: ConteudoMenu,
    itensNoCarrinho: Int = 0,
    estaLogado: Boolean = false,
    nomeUsuario: String? = null,
    aoNavegar: (String) -> Unit = {},
    aoAbrirItem: (Int) -> Unit = {},
) {
    val estadoGaveta = rememberDrawerState(DrawerValue.Closed)
    val escopo = rememberCoroutineScope()
    var abaSelecionada by remember { mutableIntStateOf(0) }

    ModalNavigationDrawer(
        drawerState = estadoGaveta,
        scrimColor = Color(0xFF03070F).copy(alpha = 0.72f),
        drawerContent = {
            GavetaLazerSport(
                estaLogado = estaLogado,
                nomeUsuario = nomeUsuario,
                aoEscolher = { rota ->
                    escopo.launch { estadoGaveta.close() }
                    if (rota != ROTA_MENU) aoNavegar(rota)
                },
                aoFechar = { escopo.launch { estadoGaveta.close() } },
            )
        },
    ) {
        Scaffold(
            // Transparente: o LazyColumn abaixo pinta o fundoNoite.
            containerColor = Color.Transparent,
            topBar = {
                // O Box carrega o degrade; a TopAppBar fica transparente
                // por cima. containerColor so aceita cor solida.
                Box(modifier = Modifier.fundoHero()) {
                    TopAppBar(
                        title = {
                            LogoComNome(tamanhoSimbolo = 34.dp, mostrarAssinatura = true)
                        },
                        navigationIcon = {
                            IconButton(onClick = { escopo.launch { estadoGaveta.open() } }) {
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
                                FundoIcone(corFundo = AzulDardo.copy(alpha = 0.18f)) {
                                    IconButton(
                                        onClick = {
                                            aoNavegar(rotaLista(FonteLista.BRINQUEDOS))
                                        },
                                    ) {
                                        Icon(
                                            Icons.Filled.Search,
                                            contentDescription = "Buscar",
                                            tint = AzulDardo,
                                        )
                                    }
                                }
                                FundoIcone(corFundo = RosaMarca.copy(alpha = 0.22f)) {
                                    IconButton(onClick = { aoNavegar(ROTA_CARRINHO) }) {
                                        BadgedBox(
                                            badge = {
                                                if (itensNoCarrinho > 0) {
                                                    Badge(
                                                        containerColor = RosaMarca,
                                                        contentColor = Color.White,
                                                    ) { Text("$itensNoCarrinho") }
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
                NavigationBar(containerColor = NoiteMeio) {
                    val abas = listOf(
                        Triple("Início", Icons.Filled.Home, ROTA_MENU),
                        Triple(
                            "Catálogo",
                            Icons.Filled.Widgets,
                            rotaLista(FonteLista.BRINQUEDOS),
                        ),
                        Triple("Carrinho", Icons.Filled.ShoppingCart, ROTA_CARRINHO),
                        Triple("Conta", Icons.Filled.Person, ROTA_CONTA),
                    )
                    abas.forEachIndexed { indice, (rotulo, icone, rota) ->
                        NavigationBarItem(
                            selected = abaSelecionada == indice,
                            onClick = {
                                abaSelecionada = indice
                                if (rota != ROTA_MENU) aoNavegar(rota)
                            },
                            icon = { Icon(icone, contentDescription = rotulo) },
                            label = { Text(rotulo) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = AzulPastel,
                                indicatorColor = AzulVivo,
                                unselectedIconColor = TextoFraco,
                                unselectedTextColor = TextoFraco,
                            ),
                        )
                    }
                }
            },
        ) { padding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .fundoNoite()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {

                item {
                    Hero(
                        aoVerCatalogo = { aoNavegar(rotaLista(FonteLista.BRINQUEDOS)) },
                        aoOrcamento = { aoNavegar(ROTA_CONTATO) },
                    )
                }

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
                                            aoNavegar(
                                                rotaLista(
                                                    FonteLista.BRINQUEDOS,
                                                    categoria.id,
                                                )
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                if (conteudo.promocoes.isNotEmpty()) {
                    item {
                        SecaoAzul(
                            kicker = "OFERTAS",
                            titulo = "Promoções",
                            subtitulo = "Condições especiais por tempo limitado",
                            acao = "Ver todas",
                            aoAcao = { aoNavegar(rotaLista(FonteLista.PROMOCOES)) },
                            corKicker = RosaMarca,
                        ) {
                            CarrosselItens(conteudo.promocoes, aoAbrirItem)
                        }
                    }
                }

                if (conteudo.destaques.isNotEmpty()) {
                    item {
                        SecaoEscura(
                            kicker = "MAIS PROCURADOS",
                            titulo = "Brinquedos em Destaque",
                            subtitulo = "Os campeões de festas, eventos e parques",
                            acao = "Ver catálogo",
                            aoAcao = { aoNavegar(rotaLista(FonteLista.BRINQUEDOS)) },
                        ) {
                            CarrosselItens(conteudo.destaques, aoAbrirItem)
                        }
                    }
                }

                item { FaixaManutencao(aoSolicitar = { aoNavegar(ROTA_MANUTENCAO) }) }

                if (conteudo.pecas.isNotEmpty()) {
                    item {
                        SecaoAzul(
                            kicker = "ASSISTÊNCIA",
                            titulo = "Peças de Reposição",
                            subtitulo = "Componentes originais para o seu equipamento",
                            acao = "Ver todas",
                            aoAcao = { aoNavegar(rotaLista(FonteLista.PECAS)) },
                        ) {
                            CarrosselItens(conteudo.pecas, aoAbrirItem)
                        }
                    }
                }

                if (conteudo.combos.isNotEmpty()) {
                    item {
                        SecaoEscura(
                            kicker = "PACOTES",
                            titulo = "Combos",
                            subtitulo = "Montados com o melhor custo-benefício",
                            acao = "Ver combos",
                            aoAcao = { aoNavegar(rotaLista(FonteLista.COMBOS)) },
                            corKicker = Amarelo,
                        ) {
                            CarrosselItens(conteudo.combos, aoAbrirItem)
                        }
                    }
                }

                if (conteudo.estabelecimentos.isNotEmpty()) {
                    item {
                        SecaoAzul(
                            kicker = "PARCEIROS",
                            titulo = "Onde nossos brinquedos estão",
                            subtitulo = "Parques, buffets e espaços que confiam na gente",
                            acao = "Ver todos",
                            aoAcao = { aoNavegar(rotaLista(FonteLista.ESTABELECIMENTOS)) },
                        ) {
                            CarrosselLargo(conteudo.estabelecimentos)
                        }
                    }
                }

                if (conteudo.eventos.isNotEmpty()) {
                    item {
                        SecaoEscura(
                            kicker = "PORTFÓLIO",
                            titulo = "Eventos Realizados",
                            subtitulo = "Um pouco do que já montamos por aí",
                            acao = "Ver todos",
                            aoAcao = { aoNavegar(rotaLista(FonteLista.EVENTOS)) },
                            corKicker = RosaMarca,
                        ) {
                            CarrosselLargo(conteudo.eventos)
                        }
                    }
                }

                item { FaixaContato(aoFalarConosco = { aoNavegar(ROTA_CONTATO) }) }
            }
        }
    }
}

// ============ HERO ============

@Composable
private fun Hero(aoVerCatalogo: () -> Unit, aoOrcamento: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fundoHero()
            .padding(horizontal = 24.dp, vertical = 38.dp),
    ) {
        Kicker("LAZER & SPORT BRINQUEDOS")
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
            color = TextoMedio,
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotaoPrincipal(
                texto = "Ver catálogo",
                aoClicar = aoVerCatalogo,
                cor = RosaMarca,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .vidro(raio = RaioBotao, intensidade = 0.10f)
                    .clickable(onClick = aoOrcamento),
                contentAlignment = Alignment.Center,
            ) {
                Text("Orçamento", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(28.dp))
        FaixaNumeros()
    }
}

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
                    .vidro(raio = 16.dp, intensidade = 0.08f)
                    .padding(vertical = 14.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = numero,
                    style = MaterialTheme.typography.titleLarge,
                    color = AzulPastel,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = rotulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoMedio,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ============ FAIXAS DE ACAO ============

@Composable
private fun FaixaManutencao(aoSolicitar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(RaioSecao))
            .fundoFaixaAzul()
            .padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .vidro(raio = 14.dp, intensidade = 0.18f),
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
            color = Color.White.copy(alpha = 0.88f),
        )
        Spacer(Modifier.height(18.dp))
        BotaoPrincipal(
            texto = "Solicitar atendimento",
            aoClicar = aoSolicitar,
            cor = Amarelo,
            corTexto = Color(0xFF06284F),
        )
    }
}

@Composable
private fun FaixaContato(aoFalarConosco: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(RaioSecao))
            .fundoFaixaRosa()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Kicker("VISITE", Color.White)
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Venha até a Lazer & Sport",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Fale com a gente para montar o orçamento do seu evento ou espaço.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.88f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        BotaoPrincipal(
            texto = "Falar com a equipe",
            aoClicar = aoFalarConosco,
            cor = Color.White,
            corTexto = Color(0xFF8E1F33),
        )
    }
}