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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lazer_sport_app.data.Contato
import com.example.lazer_sport_app.data.FonteLista
import com.example.lazer_sport_app.data.SaudeApi
import com.example.lazer_sport_app.ui.components.BolhaCategoria
import com.example.lazer_sport_app.ui.components.BotaoPrincipal
import com.example.lazer_sport_app.ui.components.CarrosselItens
import com.example.lazer_sport_app.ui.components.CarrosselLargo
import com.example.lazer_sport_app.ui.components.ConteudoMenu
import com.example.lazer_sport_app.ui.components.ItemVitrine
import com.example.lazer_sport_app.ui.components.Kicker
import com.example.lazer_sport_app.ui.components.SecaoAzul
import com.example.lazer_sport_app.ui.components.SecaoEscura
import com.example.lazer_sport_app.ui.theme.Amarelo
import com.example.lazer_sport_app.ui.theme.AzulDardo
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.AzulVivo
import com.example.lazer_sport_app.ui.theme.Esqueleto
import com.example.lazer_sport_app.ui.theme.EsqueletoCartao
import com.example.lazer_sport_app.ui.theme.EsqueletoLargo
import com.example.lazer_sport_app.ui.theme.NoiteMeio
import com.example.lazer_sport_app.ui.theme.RaioBotao
import com.example.lazer_sport_app.ui.theme.RaioSecao
import com.example.lazer_sport_app.ui.theme.RosaMarca
import com.example.lazer_sport_app.ui.theme.TextoFraco
import com.example.lazer_sport_app.ui.theme.TextoMedio
import com.example.lazer_sport_app.ui.theme.brilhoCarregando
import com.example.lazer_sport_app.ui.theme.tocavel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    conteudo: ConteudoMenu,
    carregando: Boolean = false,
    itensNoCarrinho: Int = 0,
    estaLogado: Boolean = false,
    nomeUsuario: String? = null,
    saudeApi: SaudeApi = SaudeApi.COMPLETA,
    recursosDisponiveis: Set<String> = emptySet(),
    aoNavegar: (String) -> Unit = {},
    aoAbrirItem: (Int) -> Unit = {},
    aoAdicionarCarrinho: (ItemVitrine) -> Unit = {},
) {
    val estadoGaveta = rememberDrawerState(DrawerValue.Closed)
    val escopo = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    var abaSelecionada by remember { mutableIntStateOf(0) }

    fun recursoDisponivel(recurso: String): Boolean {
        return saudeApi == SaudeApi.FORA ||
                recursosDisponiveis.isEmpty() ||
                recurso in recursosDisponiveis
    }

    fun consultarPreco(item: ItemVitrine) {
        uriHandler.openUri(
            Contato.whatsapp(
                "Olá! Vi ${item.nome} no aplicativo da Lazer & Sport " +
                        "e gostaria de consultar o preço."
            )
        )
    }

    ModalNavigationDrawer(
        drawerState = estadoGaveta,
        scrimColor = Color(0xFF03070F).copy(alpha = 0.72f),
        drawerContent = {
            GavetaLazerSport(
                estaLogado = estaLogado,
                nomeUsuario = nomeUsuario,
                itensNoCarrinho = itensNoCarrinho,
                saudeApi = saudeApi,
                recursosDisponiveis = recursosDisponiveis,
                aoEscolher = { rota ->
                    escopo.launch {
                        estadoGaveta.close()
                    }

                    if (rota != ROTA_MENU) {
                        aoNavegar(rota)
                    }
                },
                aoFechar = {
                    escopo.launch {
                        estadoGaveta.close()
                    }
                },
            )
        },
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Box(modifier = Modifier.fundoHero()) {
                    TopAppBar(
                        title = {
                            LogoComNome(
                                tamanhoSimbolo = 34.dp,
                                mostrarAssinatura = true,
                            )
                        },
                        navigationIcon = {
                            BotaoCabecalho(
                                icone = Icons.Filled.Menu,
                                descricao = "Abrir menu",
                                cor = AzulDardo,
                                aoClicar = {
                                    escopo.launch {
                                        estadoGaveta.open()
                                    }
                                },
                                modifier = Modifier.padding(start = 6.dp),
                            )
                        },
                        actions = {
                            Row(
                                horizontalArrangement = EspacoIcones,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BotaoCabecalho(
                                    icone = Icons.Filled.Search,
                                    descricao = "Buscar no catálogo",
                                    cor = AzulDardo,
                                    aoClicar = {
                                        aoNavegar(
                                            rotaLista(FonteLista.BRINQUEDOS)
                                        )
                                    },
                                )

                                BotaoCabecalho(
                                    icone = Icons.Filled.ShoppingCart,
                                    descricao = "Abrir carrinho",
                                    cor = RosaMarca,
                                    contador = itensNoCarrinho,
                                    aoClicar = {
                                        aoNavegar(ROTA_CARRINHO)
                                    },
                                )

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
                NavigationBar(
                    containerColor = NoiteMeio,
                ) {
                    val abas = listOf(
                        Triple(
                            "Início",
                            Icons.Filled.Home,
                            ROTA_MENU,
                        ),
                        Triple(
                            "Catálogo",
                            Icons.Filled.Widgets,
                            rotaLista(FonteLista.BRINQUEDOS),
                        ),
                        Triple(
                            "Carrinho",
                            Icons.Filled.ShoppingCart,
                            ROTA_CARRINHO,
                        ),
                        Triple(
                            "Conta",
                            Icons.Filled.Person,
                            ROTA_CONTA,
                        ),
                    )

                    abas.forEachIndexed { indice, item ->
                        val rotulo = item.first
                        val icone = item.second
                        val rota = item.third

                        NavigationBarItem(
                            selected = abaSelecionada == indice,
                            onClick = {
                                abaSelecionada = indice

                                if (rota != ROTA_MENU) {
                                    aoNavegar(rota)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = icone,
                                    contentDescription = rotulo,
                                )
                            },
                            label = {
                                Text(rotulo)
                            },
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
                        aoVerCatalogo = {
                            aoNavegar(
                                rotaLista(FonteLista.BRINQUEDOS)
                            )
                        },
                        aoOrcamento = {
                            aoNavegar(ROTA_CONTATO)
                        },
                    )
                }

                if (
                    recursoDisponivel("categorias") &&
                    (carregando || conteudo.categorias.isNotEmpty())
                ) {
                    item {
                        SecaoEscura(
                            kicker = "CATEGORIAS",
                            titulo = "Encontre pela categoria",
                            subtitulo = "Escolha o tipo de diversão para o seu espaço",
                        ) {
                            if (
                                carregando &&
                                conteudo.categorias.isEmpty()
                            ) {
                                EsqueletoCategorias()
                            } else {
                                LazyRow(
                                    contentPadding = PaddingValues(
                                        horizontal = 22.dp
                                    ),
                                    horizontalArrangement =
                                        Arrangement.spacedBy(14.dp),
                                ) {
                                    items(
                                        items = conteudo.categorias,
                                        key = { it.id },
                                    ) { categoria ->
                                        BolhaCategoria(
                                            categoria = categoria,
                                            aoClicar = {
                                                aoNavegar(
                                                    rotaLista(
                                                        fonte = FonteLista.BRINQUEDOS,
                                                        filtro = categoria.id,
                                                    )
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (
                    recursoDisponivel("promocoes") &&
                    (carregando || conteudo.promocoes.isNotEmpty())
                ) {
                    item {
                        SecaoAzul(
                            kicker = "OFERTAS",
                            titulo = "Promoções",
                            subtitulo = "Condições especiais por tempo limitado",
                            acao = "Ver todas",
                            aoAcao = {
                                aoNavegar(
                                    rotaLista(FonteLista.PROMOCOES)
                                )
                            },
                            corKicker = RosaMarca,
                        ) {
                            ConteudoCarrossel(
                                carregando = carregando,
                                itens = conteudo.promocoes,
                                aoAbrirItem = aoAbrirItem,
                                aoAdicionarCarrinho = aoAdicionarCarrinho,
                                aoConsultarPreco = ::consultarPreco,
                            )
                        }
                    }
                }

                if (
                    recursoDisponivel("brinquedos") &&
                    (carregando || conteudo.destaques.isNotEmpty())
                ) {
                    item {
                        SecaoEscura(
                            kicker = "MAIS PROCURADOS",
                            titulo = "Brinquedos em destaque",
                            subtitulo = "Os campeões de festas, eventos e parques",
                            acao = "Ver catálogo",
                            aoAcao = {
                                aoNavegar(
                                    rotaLista(FonteLista.BRINQUEDOS)
                                )
                            },
                        ) {
                            ConteudoCarrossel(
                                carregando = carregando,
                                itens = conteudo.destaques,
                                aoAbrirItem = aoAbrirItem,
                                aoAdicionarCarrinho = aoAdicionarCarrinho,
                                aoConsultarPreco = ::consultarPreco,
                            )
                        }
                    }
                }

                if (recursoDisponivel("manutencoes")) {
                    item {
                        FaixaManutencao(
                            aoSolicitar = {
                                aoNavegar(ROTA_MANUTENCAO)
                            }
                        )
                    }
                }

                if (
                    recursoDisponivel("pecas") &&
                    (carregando || conteudo.pecas.isNotEmpty())
                ) {
                    item {
                        SecaoAzul(
                            kicker = "ASSISTÊNCIA",
                            titulo = "Peças de reposição",
                            subtitulo = "Componentes originais para o seu equipamento",
                            acao = "Ver todas",
                            aoAcao = {
                                aoNavegar(
                                    rotaLista(FonteLista.PECAS)
                                )
                            },
                        ) {
                            ConteudoCarrossel(
                                carregando = carregando,
                                itens = conteudo.pecas,
                                aoAbrirItem = aoAbrirItem,
                                aoAdicionarCarrinho = aoAdicionarCarrinho,
                                aoConsultarPreco = ::consultarPreco,
                            )
                        }
                    }
                }

                if (
                    recursoDisponivel("combos") &&
                    (carregando || conteudo.combos.isNotEmpty())
                ) {
                    item {
                        SecaoEscura(
                            kicker = "PACOTES",
                            titulo = "Combos",
                            subtitulo = "Montados com o melhor custo-benefício",
                            acao = "Ver combos",
                            aoAcao = {
                                aoNavegar(
                                    rotaLista(FonteLista.COMBOS)
                                )
                            },
                            corKicker = Amarelo,
                        ) {
                            ConteudoCarrossel(
                                carregando = carregando,
                                itens = conteudo.combos,
                                aoAbrirItem = aoAbrirItem,
                                aoAdicionarCarrinho = aoAdicionarCarrinho,
                                aoConsultarPreco = ::consultarPreco,
                            )
                        }
                    }
                }

                if (
                    recursoDisponivel("estabelecimentos") &&
                    (
                            carregando ||
                                    conteudo.estabelecimentos.isNotEmpty()
                            )
                ) {
                    item {
                        SecaoAzul(
                            kicker = "PARCEIROS",
                            titulo = "Onde nossos brinquedos estão",
                            subtitulo = "Espaços que confiam na Lazer & Sport",
                            acao = "Ver todos",
                            aoAcao = {
                                aoNavegar(
                                    rotaLista(
                                        FonteLista.ESTABELECIMENTOS
                                    )
                                )
                            },
                        ) {
                            ConteudoCarrosselLargo(
                                carregando = carregando,
                                itens = conteudo.estabelecimentos,
                            )
                        }
                    }
                }

                if (
                    recursoDisponivel("eventos") &&
                    (carregando || conteudo.eventos.isNotEmpty())
                ) {
                    item {
                        SecaoEscura(
                            kicker = "PORTFÓLIO",
                            titulo = "Eventos realizados",
                            subtitulo = "Um pouco do que já montamos por aí",
                            acao = "Ver todos",
                            aoAcao = {
                                aoNavegar(
                                    rotaLista(FonteLista.EVENTOS)
                                )
                            },
                            corKicker = RosaMarca,
                        ) {
                            ConteudoCarrosselLargo(
                                carregando = carregando,
                                itens = conteudo.eventos,
                            )
                        }
                    }
                }

                item {
                    FaixaContato(
                        aoFalarConosco = {
                            aoNavegar(ROTA_CONTATO)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BotaoCabecalho(
    icone: ImageVector,
    descricao: String,
    cor: Color,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
    contador: Int = 0,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .tocavel(
                aoClicar = aoClicar,
                raio = 14.dp,
                corRealce = cor,
                escalaMinima = 0.93f,
            ),
        contentAlignment = Alignment.Center,
    ) {
        BadgedBox(
            badge = {
                if (contador > 0) {
                    Badge(
                        containerColor = RosaMarca,
                        contentColor = Color.White,
                    ) {
                        Text(
                            contador
                                .coerceAtMost(99)
                                .toString()
                        )
                    }
                }
            },
        ) {
            Icon(
                imageVector = icone,
                contentDescription = descricao,
                tint = if (cor == AzulDardo) {
                    AzulPastel
                } else {
                    Color.White
                },
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun EsqueletoCategorias() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(5) {
            Column(
                modifier = Modifier.width(94.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(78.dp)
                        .brilhoCarregando(39.dp)
                )

                Spacer(Modifier.height(9.dp))

                Esqueleto(
                    altura = 12.dp,
                    largura = 68.dp,
                )
            }
        }
    }
}

@Composable
private fun ConteudoCarrossel(
    carregando: Boolean,
    itens: List<ItemVitrine>,
    aoAbrirItem: (Int) -> Unit,
    aoAdicionarCarrinho: (ItemVitrine) -> Unit,
    aoConsultarPreco: (ItemVitrine) -> Unit,
) {
    if (carregando && itens.isEmpty()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            items(4) {
                EsqueletoCartao()
            }
        }
    } else {
        CarrosselItens(
            itens = itens,
            aoAbrirItem = aoAbrirItem,
            aoAdicionarCarrinho = aoAdicionarCarrinho,
            aoConsultarPreco = aoConsultarPreco,
        )
    }
}

@Composable
private fun ConteudoCarrosselLargo(
    carregando: Boolean,
    itens: List<ItemVitrine>,
) {
    if (carregando && itens.isEmpty()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            items(3) {
                EsqueletoLargo()
            }
        }
    } else {
        CarrosselLargo(
            itens = itens,
        )
    }
}

@Composable
private fun Hero(
    aoVerCatalogo: () -> Unit,
    aoOrcamento: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fundoHero()
            .padding(
                horizontal = 24.dp,
                vertical = 38.dp,
            ),
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
            text = "Projetos, fabricação e venda de brinquedos, " +
                    "arcades e equipamentos, com assistência técnica especializada.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoMedio,
        )

        Spacer(Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
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
                    .vidro(
                        raio = RaioBotao,
                        intensidade = 0.10f,
                    )
                    .clickable(onClick = aoOrcamento),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Orçamento",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        FaixaNumeros()
    }
}

@Composable
private fun FaixaNumeros() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        listOf(
            "+20" to "anos de estrada",
            "+500" to "eventos montados",
            "100%" to "fabricação própria",
        ).forEach { item ->
            val numero = item.first
            val rotulo = item.second

            Column(
                modifier = Modifier
                    .weight(1f)
                    .vidro(
                        raio = 16.dp,
                        intensidade = 0.08f,
                    )
                    .padding(
                        vertical = 14.dp,
                        horizontal = 8.dp,
                    ),
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

@Composable
private fun FaixaManutencao(
    aoSolicitar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(RaioSecao))
            .fundoFaixaAzul()
            .padding(24.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .vidro(
                        raio = 14.dp,
                        intensidade = 0.18f,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Build,
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
private fun FaixaContato(
    aoFalarConosco: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(RaioSecao))
            .fundoFaixaRosa()
            .padding(
                horizontal = 24.dp,
                vertical = 32.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Kicker(
            texto = "VISITE",
            cor = Color.White,
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Venha até a Lazer & Sport",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Fale com nossa equipe para montar o projeto ideal para o seu espaço.",
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