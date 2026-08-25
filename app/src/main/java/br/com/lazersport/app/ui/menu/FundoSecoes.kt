// Todos os fundos do app. Degrade + brilhos radiais + grade de 42px
// desenhados em drawBehind: sem View extra, sem imagem no APK.
//
// FICA EM ui.menu DE PROPOSITO. Mover para ui.theme exigiria apagar
// este arquivo na mao, e um arquivo esquecido aqui cria nomes
// duplicados no pacote -- foi o que quebrou o build da vez passada.
// Quando o app estiver estavel: Refactor -> Move do Studio, que
// reescreve todos os imports sozinho.

package br.com.lazersport.app.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.lazersport.app.ui.theme.AzulDardo
import br.com.lazersport.app.ui.theme.RaioCard

// Os brilhos sao compostos em Screen (ver brilho() la embaixo), entao
// entram na cor cheia: quem controla a forca e' a opacidade passada em
// cada fundo, e nao um tom ja' rebaixado aqui.
private val BrilhoCiano = Color(0xFF00E5FF)
private val BrilhoMagenta = Color(0xFFFF2D6F)
private val BrilhoVioleta = Color(0xFF8B5CF6)

private val PassoGrade = 42.dp
private val CorGrade = Color(0xFF8CC8FF).copy(alpha = 0.10f)

// ============ FUNDOS DE TELA ============

/** Fundo padrao de qualquer tela interna. Use no Box raiz. */
fun Modifier.fundoNoite(): Modifier = drawBehind {
    drawRect(
        Brush.linearGradient(
            colors = listOf(Color(0xFF050A22), Color(0xFF0C1444), Color(0xFF14208A)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
        )
    )
    brilho(BrilhoCiano, 0.34f, 0.06f, 0.06f)
    brilho(BrilhoMagenta, 0.30f, 0.94f, 0.88f)
    brilho(BrilhoVioleta, 0.22f, 0.52f, 0.46f)
    grade()
}

/** Hero e cabecalhos: o azul mais aberto da home. */
fun Modifier.fundoHero(): Modifier = drawBehind {
    drawRect(
        Brush.linearGradient(
            colors = listOf(Color(0xFF050A22), Color(0xFF101C6B), Color(0xFF2A2FC4)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
        )
    )
    brilho(BrilhoMagenta, 0.62f, 0.88f, 0.06f)
    brilho(BrilhoCiano, 0.58f, 0.04f, 0.94f)
    brilho(BrilhoVioleta, 0.45f, 0.50f, 0.44f)
    grade()
}

/** Secao escura de contraste -- quase preta, sangra ate as bordas. */
fun Modifier.fundoSecaoEscura(): Modifier = drawBehind {
    drawRect(
        Brush.linearGradient(
            colors = listOf(Color(0xFF080E30), Color(0xFF03050F), Color(0xFF0A1046)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
        )
    )
    brilho(BrilhoCiano, 0.26f, 0.06f, 0.08f)
    brilho(BrilhoMagenta, 0.22f, 0.95f, 0.84f)
    grade()
}

/**
 * Secao de respiro em azul-aco. Substitui a antiga "secao clara", que
 * era um azul quase branco -- mesmo papel na leitura, sem clarear.
 */
fun Modifier.fundoSecaoAzul(): Modifier = drawBehind {
    drawRect(
        Brush.linearGradient(
            colors = listOf(Color(0xFF0D1A5E), Color(0xFF1B2A9E), Color(0xFF0A1246)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
        )
    )
    brilho(AzulDardo, 0.44f, 0.10f, 0.10f)
    brilho(BrilhoMagenta, 0.30f, 0.92f, 0.90f)
    grade()
}

// ============ FAIXAS SOLIDAS (chamada pra acao) ============

fun Modifier.fundoFaixaAzul(): Modifier = background(
    Brush.linearGradient(listOf(Color(0xFF2A2FC4), Color(0xFF00E5FF)))
)

fun Modifier.fundoFaixaRosa(): Modifier = background(
    Brush.linearGradient(listOf(Color(0xFFFF2D6F), Color(0xFFFF7A29)))
)

// ============ VIDRO (substituiu o cartao branco) ============

/**
 * Cartao de vidro.
 *
 * O veu e a borda sao azuis, e nao brancos. Branco translucido sobre um
 * fundo colorido dessatura o que esta' embaixo: era o que transformava
 * cada cartao numa mancha cinza no meio da tela.
 */
fun Modifier.vidro(
    raio: Dp = RaioCard,
    intensidade: Float = 0.14f,
    corBorda: Color = Color(0xFFA0D2FF).copy(alpha = 0.30f),
): Modifier = this
    .clip(RoundedCornerShape(raio))
    .background(Color(0xFF5A96FF).copy(alpha = intensidade))
    .border(1.dp, corBorda, RoundedCornerShape(raio))

fun Modifier.vidroTingido(
    cor: Color,
    raio: Dp = RaioCard,
    intensidade: Float = 0.15f,
): Modifier = this
    .clip(RoundedCornerShape(raio))
    .background(cor.copy(alpha = intensidade))
    .border(1.dp, cor.copy(alpha = 0.35f), RoundedCornerShape(raio))

// ============ PRIMITIVAS ============

/** Brilho radial. x/y sao fracoes 0..1, igual ao `circle at 88% 12%`. */
/**
 * Mancha de luz colorida num canto da tela.
 *
 * BlendMode.Screen e' o ponto inteiro desta funcao. Composta do jeito
 * normal (SrcOver), uma cor saturada a 20% sobre azul escuro nao fica
 * "um azul levemente rosado" -- fica cinza-arroxeado, porque a mistura
 * puxa os dois para o meio. Screen soma luz: o fundo escuro quase nao
 * resiste e o matiz aparece na propria cor.
 *
 * E' por isso que aqui as intensidades sao altas (0.3 a 0.6) e mesmo
 * assim a tela nao "estoura": Screen sobre preto rende a cor, sobre
 * claro rende quase nada.
 */
private fun DrawScope.brilho(cor: Color, intensidade: Float, x: Float, y: Float) {
    drawRect(
        Brush.radialGradient(
            colors = listOf(cor.copy(alpha = intensidade), Color.Transparent),
            center = Offset(size.width * x, size.height * y),
            radius = size.maxDimension * 0.72f,
        ),
        blendMode = BlendMode.Screen,
    )
}

/** Grade de papel milimetrado esmaecendo pra baixo (o mask-image do CSS). */
private fun DrawScope.grade(cor: Color = CorGrade) {
    val passo = PassoGrade.toPx()
    if (passo <= 0f) return

    val alturaFade = size.height * 0.88f

    val pincelVertical = Brush.verticalGradient(
        colors = listOf(cor, Color.Transparent),
        startY = 0f,
        endY = alturaFade,
    )
    var x = passo
    while (x < size.width) {
        drawLine(
            brush = pincelVertical,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f,
        )
        x += passo
    }

    var y = passo
    while (y < size.height) {
        val fracao = (1f - y / alturaFade).coerceIn(0f, 1f)
        if (fracao > 0f) {
            drawLine(
                color = cor.copy(alpha = cor.alpha * fracao),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
        }
        y += passo
    }
}