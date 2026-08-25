package br.com.lazersport.app.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.lazersport.app.R
import br.com.lazersport.app.ui.theme.RosaMarca
import br.com.lazersport.app.ui.theme.TextoForte
import br.com.lazersport.app.ui.theme.TextoMedio

// A MARCA NA TELA
//
// A logotipia oficial (ls_logo_completa) e' um PNG largo: alvo, dardo,
// nome e assinatura, tudo dentro de uma pilula branca de contorno preto.
// Ela foi desenhada para papel e para o site claro. Jogada por cima do
// azul-noite do app, a pilula vira um bloco branco no meio da tela, e
// como a arte inteira precisa caber na largura disponivel, o alvo -- que
// e' a parte que identifica a marca -- encolhe ate' virar um selo.
//
// Aqui a marca e' remontada em duas pecas separadas:
//
//   SIMBOLO  alvo e dardo em vetor (ls_marca_alvo / ls_marca_dardo), os
//            mesmos arquivos que desenham o icone do launcher. Como sao
//            duas pecas, o dardo pode entrar voando na tela de abertura;
//   NOME     "Lazer & Sport" escrito em texto, na cor do tema, sem
//            pilula e sem contorno.
//
// Separado assim, o simbolo ocupa o espaco todo que tiver e o nome
// acompanha o tema em vez de arrastar um fundo branco consigo.

/** Direcao do eixo do dardo, do alvo para a cauda: 45 graus, para cima
 *  e para a direita. Sai de 1/raiz(2) -- e' o mesmo -45 do desenho. */
private const val EIXO = 0.70710678f

/**
 * Alvo com o dardo cravado. E' o mesmo desenho do icone do app.
 *
 * @param progressoDardo 0 deixa o dardo fora da tela, no prolongamento do
 *   proprio eixo; 1 crava a ponta no centro do alvo. So' a abertura anima
 *   isso -- em todo o resto do app o dardo ja' chega cravado.
 * @param distanciaEntrada de quao longe o dardo parte, em multiplos do
 *   tamanho do simbolo. 1.6 basta para ele comecar fora de qualquer
 *   tela em que o simbolo caiba.
 */
@Composable
fun SimboloMarca(
    modifier: Modifier = Modifier,
    tamanho: Dp = 56.dp,
    progressoDardo: Float = 1f,
    distanciaEntrada: Float = 1.6f,
) {
    // O tamanho vem antes do modifier do chamador de proposito: quem
    // chama pode trocar por fillMaxSize e deixar o simbolo ocupar o
    // espaco que tiver, em vez de ficar preso a um lado fixo.
    Box(modifier = Modifier.size(tamanho).then(modifier)) {
        Image(
            painter = painterResource(R.drawable.ls_marca_alvo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )

        Image(
            painter = painterResource(R.drawable.ls_marca_dardo),
            // O alvo ja' descreve a marca; o dardo e' a outra metade do
            // mesmo desenho e nao merece um anuncio proprio no leitor de
            // tela.
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val falta = (1f - progressoDardo).coerceIn(0f, 1f)
                    val percurso = size.width * distanciaEntrada * falta
                    translationX = percurso * EIXO
                    translationY = -percurso * EIXO
                    // Some cedo na volta: um dardo meio transparente
                    // parado no ar entrega que e' um desenho deslizando.
                    alpha = (progressoDardo * 3f).coerceIn(0f, 1f)
                },
        )
    }
}

/**
 * "Lazer & Sport" em texto, com a assinatura opcional embaixo.
 *
 * Os pontos que separam a assinatura saem no vermelho da marca, como na
 * arte impressa.
 */
@Composable
fun NomeMarca(
    modifier: Modifier = Modifier,
    tamanho: TextUnit = 22.sp,
    cor: Color = TextoForte,
    mostrarAssinatura: Boolean = false,
    alinhamento: TextAlign = TextAlign.Start,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alinhamento == TextAlign.Center) {
            Alignment.CenterHorizontally
        } else {
            Alignment.Start
        },
    ) {
        Text(
            text = "Lazer & Sport",
            color = cor,
            fontSize = tamanho,
            fontWeight = FontWeight.Bold,
            // Levemente apertado: no corpo do texto o padrao e' folgado
            // demais para um nome que precisa ler como uma peca so'.
            letterSpacing = (-0.6).sp,
            maxLines = 1,
            textAlign = alinhamento,
        )

        if (mostrarAssinatura) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = buildAnnotatedString {
                    val ponto = SpanStyle(
                        color = RosaMarca,
                        fontWeight = FontWeight.Black,
                    )
                    append("Brinquedos")
                    withStyle(ponto) { append("  ·  ") }
                    append("Cenografia")
                    withStyle(ponto) { append("  ·  ") }
                    append("Parques")
                },
                color = TextoMedio,
                fontSize = tamanho * 0.36f,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.4.sp,
                maxLines = 1,
                textAlign = alinhamento,
            )
        }
    }
}

/**
 * Marca do cabecalho: simbolo e nome lado a lado.
 *
 * @param tamanhoSimbolo lado do alvo. O nome acompanha.
 */
@Composable
fun LogoComNome(
    modifier: Modifier = Modifier,
    tamanhoSimbolo: Dp = 34.dp,
    mostrarAssinatura: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SimboloMarca(tamanho = tamanhoSimbolo)
        Spacer(Modifier.width(tamanhoSimbolo * 0.28f))
        NomeMarca(
            tamanho = (tamanhoSimbolo.value * 0.52f).sp,
            mostrarAssinatura = mostrarAssinatura,
        )
    }
}

/**
 * Marca empilhada, para quando ha' altura sobrando: simbolo grande em
 * cima, nome e assinatura embaixo.
 */
@Composable
fun LogoCompleta(
    modifier: Modifier = Modifier,
    tamanhoSimbolo: Dp = 132.dp,
    progressoDardo: Float = 1f,
    mostrarAssinatura: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SimboloMarca(
            tamanho = tamanhoSimbolo,
            progressoDardo = progressoDardo,
        )
        Spacer(Modifier.height(tamanhoSimbolo * 0.14f))
        NomeMarca(
            tamanho = (tamanhoSimbolo.value * 0.23f).sp,
            mostrarAssinatura = mostrarAssinatura,
            alinhamento = TextAlign.Center,
        )
    }
}

/**
 * Painel das telas de boas-vindas e login.
 *
 * O gradiente continua, porque e' ele que separa o painel do fundo da
 * tela; o que saiu de dentro foi a logotipia em PNG, que punha uma
 * pilula branca no meio do azul.
 */
@Composable
fun PainelMarcaEntrada(
    modifier: Modifier = Modifier,
    altura: Dp = 190.dp,
) {
    val formato = RoundedCornerShape(30.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(altura)
            .clip(formato)
            .fundoMarca()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = formato,
            )
            .padding(horizontal = 24.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        // O simbolo ocupa a altura util do painel, com folga para o nome
        // embaixo. Fora de proporcao ele encostaria na borda em telas
        // baixas, que e' onde o painel entra com 158dp.
        LogoCompleta(tamanhoSimbolo = altura * 0.46f)
    }
}

/**
 * Fundo translucido usado atras dos icones do cabecalho.
 */
@Composable
fun FundoIcone(
    corFundo: Color = Color.White.copy(alpha = 0.14f),
    tamanho: Dp = 40.dp,
    conteudo: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(tamanho)
            .vidroTingido(
                cor = corFundo,
                raio = 13.dp,
                intensidade = 1f,
            ),
        contentAlignment = Alignment.Center,
    ) {
        conteudo()
    }
}

/** Gradiente da marca: indigo abrindo em azul eletrico e no vermelho. */
private fun Modifier.fundoMarca(): Modifier = this.then(
    Modifier.background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF0A1046),
                Color(0xFF2A2FC4),
                Color(0xFFFF2D6F),
            ),
        ),
    ),
)

/**
 * Espacamento padrao entre os icones do cabecalho.
 */
val EspacoIcones = Arrangement.spacedBy(7.dp)
