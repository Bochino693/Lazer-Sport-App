// TELA DE ABERTURA -- o dardo cravando a marca e o estado real da API.
//
// Nao e' enfeite: e' aqui que o app pergunta /status/ e decide o que vai
// existir na sessao. Sem essa consulta o cliente descobria que a API
// estava fora so' depois de tres telas vazias.
//
// A cena e' a marca se montando, e nao uma imagem que aparece:
//
//   ALVO     o alvo entra girando de leve e assenta por mola, passando
//            de raspao do ponto antes de parar;
//   ARREMESSO  o dardo cruza a tela pelo proprio eixo, de fora do canto
//            superior direito, acelerando ate' cravar no centro;
//   IMPACTO  no quadro em que a ponta encosta, o alvo leva o baque --
//            recua na diagonal do golpe, balanca amortecido e solta dois
//            aneis do centro para fora; a haste do dardo vibra junto;
//   NOME     so' depois de cravado o nome sobe por baixo do alvo. Antes
//            do impacto ele disputaria a atencao com o arremesso;
//   RESPIRO  enquanto a API nao responde, a marca respira e os aneis
//            reabrem de tempos em tempos, como um radar saindo do alvo:
//            "estou trabalhando" sem escrever nada;
//   SAIDA    ao ficar pronto, tudo cresce de novo e some junto, e so'
//            entao a proxima tela entra. Cortar seco aqui era o que
//            fazia a abertura parecer um flash.
//
// A saida espera o dardo cravar mesmo quando a API responde antes: uma
// cena de arremesso cortada no meio do voo fica pior do que nao ter
// abertura nenhuma.
//
// Antes daqui saia o PNG da logotipia inteira, pilula branca e tudo, com
// o alvo reduzido a um selo no meio da arte. O alvo e o dardo agora sao
// dois vetores separados (os mesmos do icone do app), e e' por serem
// separados que o arremesso existe.

package br.com.lazersport.app.ui.abertura

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
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
import br.com.lazersport.app.ui.menu.NomeMarca
import br.com.lazersport.app.ui.menu.SimboloMarca
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


/** De onde o alvo parte. Longe o bastante de 1 para a chegada ser vista,
 *  perto o bastante para nao virar um salto. */
private const val ESCALA_INICIAL = 0.78f

/** Inclinacao inicial do alvo, em graus. Ele entra torto e endireita. */
private const val GIRO_INICIAL = -13f

/** Lado do alvo na abertura. E' a peca principal da tela: o que antes
 *  aparecia aqui era a logotipia inteira, com o alvo do tamanho de um
 *  selo no meio dela. */
private val TAMANHO_ALVO = 196.dp

/** Diametro da area onde halo e aneis sao desenhados, atras do alvo. */
private val AREA_HALO = 320.dp

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
    val giro = remember { Animatable(GIRO_INICIAL) }
    // Percurso do dardo: 0 fora da tela, 1 cravado no centro do alvo.
    val voo = remember { Animatable(0f) }
    // Baque do impacto. Vai a 1 no quadro da pancada e volta a zero por
    // mola pouco amortecida -- e' a oscilacao dela que da' o tremor.
    val baque = remember { Animatable(0f) }
    val anel = remember { Animatable(0f) }
    val nome = remember { Animatable(0f) }
    val saida = remember { Animatable(0f) }

    // A saida espera por isto. Sem a trava, uma API que responde em 200 ms
    // faria a tela sumir com o dardo ainda no ar.
    var cravou by remember { mutableStateOf(false) }

    // A cena inteira em uma corrotina so', na ordem em que se ve': o alvo
    // chega, o dardo cruza a tela, a pancada sacode tudo e so' entao o
    // nome sobe. Encadeado, e nao disparado junto, porque o que faz uma
    // abertura parecer intencional e' a ordem dos tempos.
    LaunchedEffect(Unit) {
        // Opacidade e escala andam separadas de proposito: a opacidade
        // sobe reta, a escala chega por mola e passa de raspao do ponto
        // antes de parar. E' esse excesso minimo que faz o alvo parecer
        // pousar, em vez de apenas aumentar de tamanho.
        launch {
            escala.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.55f,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        launch {
            giro.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.62f,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        entrada.animateTo(1f, tween(400, easing = FastOutSlowInEasing))

        // Um respiro antes do arremesso. Sem ele o dardo entra junto com
        // o alvo e a cena vira uma coisa so', ilegivel.
        delay(120)

        // O arremesso. Acelerando ate' o fim (FastOutLinearIn) porque um
        // dardo nao freia antes de acertar -- com desaceleracao ele
        // parecia pousar no alvo, e nao cravar.
        voo.animateTo(1f, tween(300, easing = FastOutLinearInEasing))
        cravou = true

        // Impacto: tres coisas no mesmo quadro, cada uma no seu tempo.
        launch {
            // o alvo leva a pancada e balanca amortecido
            baque.snapTo(1f)
            baque.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.26f,
                    stiffness = Spring.StiffnessHigh,
                ),
            )
        }
        launch {
            // os aneis saem do centro
            anel.snapTo(0f)
            anel.animateTo(1f, tween(700, easing = LinearOutSlowInEasing))
        }
        launch {
            // e a haste do dardo recua um fio e volta, vibrando
            voo.animateTo(0.955f, tween(80, easing = LinearOutSlowInEasing))
            voo.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.34f,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }

        // O nome sobe por baixo do alvo com a pancada ainda acontecendo.
        // Esperar o tremor acabar deixaria um buraco na cena.
        delay(170)
        nome.animateTo(1f, tween(440, easing = FastOutSlowInEasing))

        // Passado o impacto, o mesmo anel vira radar e reabre de tempos
        // em tempos: e' o "estou trabalhando" sem escrever nada.
        //
        // O laco fica aqui, no efeito que nunca e' recomposto, e nao
        // preso ao estado da API: cancelar no meio deixaria um anel
        // parado na tela pelos milissegundos que antecedem a saida. Como
        // a opacidade do anel acompanha a da marca, ele some junto na
        // saida sozinho.
        while (true) {
            delay(420)
            anel.snapTo(0f)
            anel.animateTo(1f, tween(1600, easing = LinearOutSlowInEasing))
        }
    }

    // Segura um instante mesmo quando a resposta é imediata, e só então
    // encena a saída: piscar a marca e sumir fica pior do que não ter
    // abertura nenhuma.
    LaunchedEffect(estado.saude, cravou) {
        // cravou e' a trava do arremesso: sem ela uma API rapida cortava
        // a cena com o dardo ainda no ar.
        if (!cravou) return@LaunchedEffect

        if (estado.saude == SaudeApi.COMPLETA || estado.saude == SaudeApi.PARCIAL) {
            delay(480)
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
    val pancada = baque.value

    // O alvo chega pela mola, leva o baque, respira enquanto espera e
    // cresce de novo ao sair. Os dois crescimentos vao na mesma direcao:
    // e' isso que faz a passagem para a proxima tela parecer continua, e
    // nao um corte.
    val escalaMarca = escala.value *
            (if (respirando) respiro else 1f) *
            (1f + 0.07f * pancada) *
            (1f + 0.09f * fechou)

    // coerceIn porque alpha fora de 0..1 e' comportamento indefinido, e a
    // mola do assentamento passa de 1 por alguns quadros.
    val opacidade = (abriu * (1f - fechou)).coerceIn(0f, 1f)

    // Nome e cartao de status so' existem depois do impacto, e somem
    // junto com o resto na saida.
    val opacidadeApoio = (nome.value * (1f - fechou)).coerceIn(0f, 1f)

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
                // Halo atras do alvo: da' profundidade e e' o que faz o
                // respiro ser percebido, ja' que o alvo sozinho varia
                // pouco demais para o olho notar.
                Box(
                    modifier = Modifier
                        .size(AREA_HALO)
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

                // Aneis: abrem do centro para fora e somem. Dois, com o
                // segundo atrasado, porque um anel sozinho le' como
                // carregando e dois leem como onda de choque. No impacto
                // saem uma vez; depois viram radar enquanto a API nao
                // responde. Ficam atras do alvo para nao lavar o desenho.
                Box(
                    modifier = Modifier
                        .size(AREA_HALO)
                        .drawBehind {
                            val raioMaximo = size.minDimension / 2f
                            listOf(0f, 0.22f).forEach { atraso ->
                                val avanco = anel.value - atraso
                                if (avanco <= 0f || avanco >= 1f) {
                                    return@forEach
                                }
                                drawCircle(
                                    color = AzulDardo.copy(
                                        alpha = 0.55f * (1f - avanco) *
                                                opacidade,
                                    ),
                                    radius = raioMaximo *
                                            (0.40f + 0.66f * avanco),
                                    style = Stroke(width = 2.dp.toPx()),
                                )
                            }
                        },
                )

                SimboloMarca(
                    tamanho = TAMANHO_ALVO,
                    progressoDardo = voo.value,
                    modifier = Modifier.graphicsLayer {
                        scaleX = escalaMarca
                        scaleY = escalaMarca
                        alpha = opacidade
                        // Entra torto e endireita; no impacto volta a
                        // oscilar alguns graus.
                        rotationZ = giro.value + pancada * 3.5f
                        // O golpe vem de cima a direita, entao o alvo
                        // recua para baixo e para a esquerda. A mola do
                        // baque passa do zero, e e' isso que faz o
                        // recuo virar tremor em vez de empurrao.
                        val recuo = pancada * 7.dp.toPx()
                        translationX = -recuo
                        // Sobe ao entrar; a distancia diminui junto com a
                        // escala, entao o alvo chega ao lugar e para.
                        translationY = recuo + (1f - abriu) * 30.dp.toPx()
                    },
                )
            }

            // Curto de proposito: a caixa do halo tem AREA_HALO de lado e
            // ja' deixa um vao entre o alvo e o que vem embaixo. Somar um
            // espaco cheio aqui soltava o nome da marca.
            Spacer(Modifier.height(8.dp))

            NomeMarca(
                tamanho = 30.sp,
                mostrarAssinatura = true,
                alinhamento = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = opacidadeApoio
                    // Sobe por baixo do alvo, no lugar de so' aparecer.
                    translationY = (1f - opacidadeApoio) * 18.dp.toPx()
                },
            )

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