// Interações e carregamento: press/hover, brilho de foco e esqueleto
// animado (o "efeito ajax" -- o bloco cinza que pulsa enquanto a rede
// não voltou, em vez da tela pular do vazio pro cheio).

package com.example.lazer_sport_app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Linha/cartão clicável com resposta física: encolhe ao apertar,
 * acende no hover (mouse, TV, DeX) e mantém o realce enquanto o dedo
 * está em cima. Substitui o ripple do Material, que brigava com os
 * ícones coloridos da gaveta.
 */
@Composable
fun Modifier.tocavel(
    aoClicar: () -> Unit,
    raio: Dp = 16.dp,
    corRealce: Color = AzulDardo,
    habilitado: Boolean = true,
    escalaMinima: Float = 0.97f,
): Modifier {
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val sobrevoado by interacao.collectIsHoveredAsState()

    val ativo = pressionado || sobrevoado

    val escala by animateFloatAsState(
        targetValue = if (pressionado) escalaMinima else 1f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "escala",
    )

    val brilho by animateFloatAsState(
        targetValue = when {
            pressionado -> 0.22f
            sobrevoado -> 0.13f
            else -> 0f
        },
        animationSpec = tween(160),
        label = "brilho",
    )

    val borda by animateFloatAsState(
        targetValue = if (ativo) 0.42f else 0.10f,
        animationSpec = tween(160),
        label = "borda",
    )

    val formato = RoundedCornerShape(raio)

    return this
        .scale(escala)
        .clip(formato)
        .background(corRealce.copy(alpha = brilho))
        .border(1.dp, corRealce.copy(alpha = borda), formato)
        .clickable(
            interactionSource = interacao,
            indication = null,
            enabled = habilitado,
            onClick = aoClicar,
        )
}

/** Varredura diagonal contínua -- a "luz" que atravessa o esqueleto. */
@Composable
fun Modifier.brilhoCarregando(raio: Dp = 16.dp): Modifier {
    val transicao = rememberInfiniteTransition(label = "shimmer")
    val deslocamento by transicao.animateFloat(
        initialValue = -900f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "deslocamento",
    )

    return this
        .clip(RoundedCornerShape(raio))
        .background(Color.White.copy(alpha = 0.05f))
        .background(
            Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.10f),
                    Color.Transparent,
                ),
                start = Offset(deslocamento, 0f),
                end = Offset(deslocamento + 320f, 320f),
            )
        )
}

/** Retângulo pulsando no lugar de um texto ou imagem. */
@Composable
fun Esqueleto(
    modifier: Modifier = Modifier,
    altura: Dp = 16.dp,
    raio: Dp = 8.dp,
    largura: Dp? = null,
) {
    Box(
        modifier = modifier
            .then(if (largura != null) Modifier.width(largura) else Modifier.fillMaxWidth())
            .height(altura)
            .brilhoCarregando(raio),
    )
}

/** Cartão-fantasma no formato do CartaoItem. */
@Composable
fun EsqueletoCartao(largura: Dp = 180.dp, altura: Dp = 292.dp) {
    Box(
        modifier = Modifier
            .width(largura)
            .height(altura)
            .brilhoCarregando(RaioCard),
    )
}

/** Cartão-fantasma no formato do CartaoLargo. */
@Composable
fun EsqueletoLargo(largura: Dp = 280.dp, altura: Dp = 180.dp) {
    Box(
        modifier = Modifier
            .width(largura)
            .height(altura)
            .brilhoCarregando(RaioCard),
    )
}

private fun Modifier.width(valor: Dp): Modifier =
    this.then(androidx.compose.foundation.layout.Modifier.width(valor))