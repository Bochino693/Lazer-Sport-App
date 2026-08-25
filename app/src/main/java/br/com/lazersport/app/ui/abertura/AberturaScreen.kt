// TELA DE ABERTURA -- a marca entrando em cena e o estado real da API.
//
// Não é enfeite: é aqui que o app pergunta /status/ e decide o que vai
// existir na sessão. Sem essa consulta o cliente descobria que a API
// estava fora só depois de três telas vazias.
//
// A cena tem três tempos, e é essa sequência que faz a abertura parecer
// intencional em vez de uma tela que pisca:
//
//   ENTRADA  a logotipia sobe e chega por mola, passando de leve do
//            ponto antes de assentar; o resto entra depois dela,
//            escalonado, para o olho ir primeiro para a marca;
//   IMPACTO  no quadro em que ela assenta, um anel abre do centro para
//            fora -- lê como o dardo acertando o alvo, que é o que a
//            própria logotipia desenha;
//   RESPIRO  enquanto a API não responde, a marca respira sobre um halo
//            que pulsa junto e o anel volta a abrir de tempos em tempos,
//            como um radar saindo do alvo: "estou trabalhando" sem
//            escrever nada;
//   SAÍDA    ao ficar pronto, tudo cresce de novo e some junto, e só
//            então a próxima tela entra. Cortar seco aqui era o que
//            fazia a abertura parecer um flash.
//
// Os dois crescimentos -- o da entrada e o da saída -- vão na mesma
// direção de propósito: é isso que faz a passagem parecer contínua.

package br.com.lazersport.app.ui.abertura

// De onde a marca parte ao entrar. Perto de 1 para a mola ter para onde
// passar sem que o movimento vire um salto.
private const val ESCALA_INICIAL = 0.86f

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lazersport.app.BuildConfig
import br.com.lazersport.app.data.EstadoApi
import br.com.lazersport.app.data.SaudeApi
import br.com.lazersport.app.data.StatusRepository
import br.com.lazersport.app.ui.components.BotaoPrincipal
import br.com.lazersport.app.ui.components.BotaoVidro
import br.com.lazersport.app.ui.menu.LogoCompleta
import br.com.lazersport.app.ui.menu.fundoHero
import br.com.lazersport.app.ui.menu.vidroTingido
import br.com.lazersport.app.ui.theme.Amarelo
import br.com.lazersport.app.ui.theme.AzulDardo
import br.com.lazersport.app.ui.theme.RosaMarca
import br.com.lazersport.app.ui.theme.TextoFraco
import br.com.lazersport.app.ui.theme.TextoMedio
import br.com.lazersport.app.ui.theme.Verde
import br.com.lazersport.app.ui.theme.brilhoCarregando
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AberturaViewModel @Inject constructor(
    private val repositorio: StatusRepository,
) : ViewModel() {

    val estado: StateFlow<EstadoApi> = repositorio.estado

    init { verificar() }

    fun verificar() {
        viewModelScope.launch { repositorio.verificar() }
    }
}

@Composable
fun AberturaScreen(
    aoContinuar: () -> Unit,
    viewModel: AberturaViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsState()

    val respirando = estado.saude == SaudeApi.VERIFICANDO

    val entrada = remember { Animatable(0f) }
    val escala = remember { Animatable(ESCALA_INICIAL) }
    val anel = remember { Animatable(0f) }
    val saida = remember { Animatable(0f) }

    // Aparecer e assentar são duas animações separadas de propósito: a
    // opacidade sobe reta, enquanto a escala chega por mola e passa de
    // leve do ponto antes de parar. É esse excesso mínimo que faz a marca
    // parecer pousar, em vez de apenas aumentar de tamanho.
    LaunchedEffect(Unit) {
        // A mola assenta por volta de 400 ms -- antes de a opacidade
        // fechar, de propósito: quando a marca termina de aparecer ela já
        // está parada, e não chegando.
        launch {
            escala.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.5f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
        entrada.animateTo(1f, tween(520, easing = FastOutSlowInEasing))

        // Só agora, com a marca inteira na tela e imóvel, o anel abre do
        // centro para fora: lê como o dardo acertando o alvo, que é o que
        // a própria logotipia desenha. Amarrar o anel ao fim da mola não
        // serviria -- ela devolve o controle assim que cruza o limiar de
        // equilíbrio, com a marca ainda quase transparente.
        anel.animateTo(1f, tween(820, easing = LinearOutSlowInEasing))

        // Depois do impacto o mesmo anel vira radar e reabre de tempos em
        // tempos: é o "estou trabalhando" sem escrever nada, e é o único
        // efeito que de fato se enxerga nesta arte -- uma varredura de luz
        // branca sumia por cima de uma logotipia que já é quase toda
        // branca.
        //
        // O laço fica aqui, no efeito que nunca é recomposto, e não preso
        // ao estado da API: cancelar no meio deixaria um anel parado na
        // tela pelos 560 ms que antecedem a saída. Como a opacidade do
        // anel acompanha a da marca, ele some junto na saída sozinho.
        while (true) {
            delay(340)
            anel.snapTo(0f)
            anel.animateTo(1f, tween(1600, easing = LinearOutSlowInEasing))
        }
    }

    // Segura um instante mesmo quando a resposta é imediata, e só então
    // encena a saída: piscar a marca e sumir fica pior do que não ter
    // abertura nenhuma.
    LaunchedEffect(estado.saude) {
        if (estado.saude == SaudeApi.COMPLETA || estado.saude == SaudeApi.PARCIAL) {
            delay(560)
            saida.animateTo(1f, tween(420, easing = LinearOutSlowInEasing))
            aoContinuar()
        }
    }

    val pulso = rememberInfiniteTransition(label = "pulso")
    val respiro by pulso.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "respiro",
    )

    val abriu = entrada.value
    val fechou = saida.value

    // A marca chega pela mola, respira enquanto espera e cresce de novo
    // ao sair. Os dois crescimentos vão na mesma direção: é isso que faz
    // a passagem para a próxima tela parecer contínua, e não um corte.
    val escalaMarca = escala.value *
            (if (respirando) respiro else 1f) *
            (1f + 0.09f * fechou)

    // coerceIn porque alpha fora de 0..1 é comportamento indefinido, e a
    // mola do assentamento passa de 1 por alguns quadros.
    val opacidade = (abriu * (1f - fechou)).coerceIn(0f, 1f)

    // O que vem depois da marca entra escalonado: só começa a aparecer
    // quando ela já percorreu metade do caminho.
    val opacidadeApoio = (((abriu - 0.5f) / 0.5f).coerceIn(0f, 1f)) * (1f - fechou)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .fundoHero(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Halo atrás da marca: dá profundidade e é o que faz o
                // respiro ser percebido, já que a logotipia sozinha varia
                // pouco demais para o olho notar.
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .graphicsLayer {
                            val expansao = if (respirando) respiro else 1f
                            scaleX = expansao
                            scaleY = expansao
                            alpha = opacidade * 0.9f
                        }
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AzulDardo.copy(alpha = 0.20f),
                                    RosaMarca.copy(alpha = 0.08f),
                                    Color.Transparent,
                                ),
                            ),
                            shape = CircleShape,
                        ),
                )

                // Anel: abre do centro para fora e some. Uma vez quando a
                // marca assenta, e depois em laço enquanto a API não
                // responde. Fica atrás da marca para não lavar o desenho.
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .drawBehind {
                            val avanco = anel.value
                            if (avanco <= 0f || avanco >= 1f) return@drawBehind

                            val raio = size.minDimension / 2f *
                                    (0.42f + 0.68f * avanco)
                            drawCircle(
                                color = AzulDardo.copy(
                                    alpha = 0.55f * (1f - avanco) * opacidade,
                                ),
                                radius = raio,
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        },
                )

                LogoCompleta(
                    largura = 250.dp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = escalaMarca
                        scaleY = escalaMarca
                        alpha = opacidade
                        // Sobe ao entrar; a distância diminui junto com a
                        // escala, então a marca chega ao lugar e para.
                        translationY = (1f - abriu) * 30.dp.toPx()
                    },
                )
            }

            Spacer(Modifier.height(44.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = opacidadeApoio
                        translationY = (1f - opacidadeApoio) * 16.dp.toPx()
                    },
            ) {
                when (estado.saude) {
                    SaudeApi.VERIFICANDO -> Carregando()

                    SaudeApi.COMPLETA -> Aviso(
                        cor = Verde,
                        icone = Icons.Filled.CheckCircle,
                        titulo = "Tudo pronto",
                        detalhe = "Catálogo sincronizado · API v${estado.versao}",
                    )

                    SaudeApi.PARCIAL -> Aviso(
                        cor = Amarelo,
                        icone = Icons.Filled.WarningAmber,
                        titulo = "Quase lá",
                        detalhe = "${estado.recursos.size} seções no ar · " +
                                "as demais aparecem assim que forem publicadas",
                    )

                    SaudeApi.FORA -> Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Aviso(
                            cor = RosaMarca,
                            icone = Icons.Filled.CloudOff,
                            titulo = "Servidor fora de alcance",
                            detalhe = estado.detalhe,
                        )
                        Spacer(Modifier.height(26.dp))
                        BotaoPrincipal(
                            texto = "Tentar de novo",
                            aoClicar = viewModel::verificar,
                            cor = RosaMarca,
                            icone = Icons.Filled.Sync,
                        )
                        Spacer(Modifier.height(10.dp))
                        BotaoVidro(
                            texto = "Entrar mesmo assim",
                            aoClicar = aoContinuar,
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "Sem conexão você navega no que já foi visto, " +
                                    "mas preços e disponibilidade podem estar desatualizados.",
                            color = TextoFraco,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        Text(
            // Versão vinda do build, e não escrita à mão: assim a tela não
            // volta a anunciar "v1.0" depois de o app ser atualizado.
            text = "Lazer & Sport Brinquedos · v${BuildConfig.VERSION_NAME}",
            color = TextoFraco.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp)
                .graphicsLayer { alpha = opacidadeApoio },
        )
    }
}

@Composable
private fun Carregando() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(5.dp)
                .brilhoCarregando(raio = 999.dp),
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Conectando ao catálogo...",
            color = TextoMedio,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun Aviso(
    cor: Color,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    detalhe: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .vidroTingido(cor, raio = 18.dp, intensidade = 0.14f)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .vidroTingido(cor, raio = 12.dp, intensidade = 0.18f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(13.dp))
        // O width(0.dp) antigo espremia o texto em uma coluna de zero
        // pixels, fazendo cada letra aparecer em uma linha. O weight ocupa
        // somente o espaço restante ao lado do ícone e continua responsivo.
        Column(Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = detalhe,
                color = TextoMedio,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}