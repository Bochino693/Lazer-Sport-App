// Todos os fundos do app. Degrade + brilhos radiais + grade de 42px
// desenhados em drawBehind: sem View extra, sem imagem no APK.
//
// FICA EM ui.menu DE PROPOSITO. Mover para ui.theme exigiria apagar
// este arquivo na mao, e um arquivo esquecido aqui cria nomes
// duplicados no pacote -- foi o que quebrou o build da vez passada.
// Quando o app estiver estavel: Refactor -> Move do Studio, que
// reescreve todos os imports sozinho.

package com.example.lazer_sport_app.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lazer_sport_app.ui.theme.AzulDardo
import com.example.lazer_sport_app.ui.theme.RaioCard

private val BrilhoAzul = Color(0xFF0878F9)
private val BrilhoRosa = Color(0xFFEE405E)

private val PassoGrade = 42.dp
private val CorGrade = Color(0xFF78A5E0).copy(alpha = 0.055f)

// ============ FUNDOS DE TELA ============

/** Fundo padrao de qualquer tela interna. Use no Box raiz. */
fun Modifier.fundoNoite(): Modifier = drawBehind {
    drawRect(
        Brush.linearGradient(
            colors = listOf(Color(0xFF060E1C), Color(0xFF0A1A33), Color(0xFF081F3D)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
        )
    )
    brilho(BrilhoAzul, 0.22f, 0.08f, 0.06f)
    brilho(BrilhoRosa, 0.13f, 0.94f, 0.86f)
    grade()
}

/** Hero e cabecalhos: o azul mais aberto da home. */
fun Modifier.fundoHero(): Modifier = drawBehind {
    drawRect(
        Brush.linearGradient(
            colors = listOf(Color(0xFF071426), Color(0xFF0A2447), Color(0xFF063D83)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
        )
    )
    brilho(BrilhoRosa, 0.25f, 0.88f, 0.12f)
    brilho(BrilhoAzul, 0.35f, 0.08f, 0.94f)
    grade()
}

/** Secao escura de contraste -- quase preta, sangra ate as bordas. */
fun Modifier.fundoSecaoEscura(): Modifier = drawBehind {
    drawRect(
        Brush.linearGradient(
            colors = listOf(Color(0xFF0A1728), Color(0xFF050B14), Color(0xFF07111F)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
        )
    )
    brilho(BrilhoAzul, 0.20f, 0.06f, 0.08f)
    brilho(BrilhoRosa, 0.14f, 0.95f, 0.82f)
    grade()
}

/**
 * Secao de respiro em azul-aco. Substitui a antiga "secao clara", que
 * era um azul quase branco -- mesmo papel na leitura, sem clarear.
 */
fun Modifier.fundoSecaoAzul(): Modifier = drawBehind {
    drawRect(
        Brush.linearGradient(
            colors = listOf(Color(0xFF0B2547), Color(0xFF0E3059), Color(0xFF0A1F3E)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
        )
    )
    brilho(AzulDardo, 0.16f, 0.10f, 0.10f)
    brilho(BrilhoRosa, 0.10f, 0.92f, 0.90f)
    grade()
}

// ============ FAIXAS SOLIDAS (chamada pra acao) ============

fun Modifier.fundoFaixaAzul(): Modifier = background(
    Brush.linearGradient(listOf(Color(0xFF004AAD), Color(0xFF0878F9)))
)

fun Modifier.fundoFaixaRosa(): Modifier = background(
    Brush.linearGradient(listOf(Color(0xFFB43544), Color(0xFFEE405E)))
)

// ============ VIDRO (substituiu o cartao branco) ============

fun Modifier.vidro(
    raio: Dp = RaioCard,
    intensidade: Float = 0.06f,
    corBorda: Color = Color.White.copy(alpha = 0.12f),
): Modifier = this
    .clip(RoundedCornerShape(raio))
    .background(Color.White.copy(alpha = intensidade))
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
private fun DrawScope.brilho(cor: Color, intensidade: Float, x: Float, y: Float) {
    drawRect(
        Brush.radialGradient(
            colors = listOf(cor.copy(alpha = intensidade), Color.Transparent),
            center = Offset(size.width * x, size.height * y),
            radius = size.maxDimension * 0.85f,
        )
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