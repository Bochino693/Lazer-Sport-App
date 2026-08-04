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
import androidx.compose.foundation.layout.width
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

@Composable
fun Modifier.tocavel(
    aoClicar: () -> Unit,
    raio: Dp = 16.dp,
    corRealce: Color = AzulDardo,
    habilitado: Boolean = true,
    escalaMinima: Float = 0.97f,
): Modifier {
    val interacao = remember {
        MutableInteractionSource()
    }

    val pressionado by interacao.collectIsPressedAsState()
    val sobrevoado by interacao.collectIsHoveredAsState()

    val ativo = pressionado || sobrevoado

    val escala by animateFloatAsState(
        targetValue = if (pressionado) {
            escalaMinima
        } else {
            1f
        },
        animationSpec = tween(
            durationMillis = 120,
            easing = FastOutSlowInEasing,
        ),
        label = "escala",
    )

    val brilho by animateFloatAsState(
        targetValue = when {
            pressionado -> 0.22f
            sobrevoado -> 0.13f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 160),
        label = "brilho",
    )

    val borda by animateFloatAsState(
        targetValue = if (ativo) {
            0.42f
        } else {
            0.10f
        },
        animationSpec = tween(durationMillis = 160),
        label = "borda",
    )

    val formato = RoundedCornerShape(raio)

    return this
        .scale(escala)
        .clip(formato)
        .background(
            corRealce.copy(alpha = brilho)
        )
        .border(
            width = 1.dp,
            color = corRealce.copy(alpha = borda),
            shape = formato,
        )
        .clickable(
            interactionSource = interacao,
            indication = null,
            enabled = habilitado,
            onClick = aoClicar,
        )
}

@Composable
fun Modifier.brilhoCarregando(
    raio: Dp = 16.dp,
): Modifier {
    val transicao = rememberInfiniteTransition(
        label = "shimmer",
    )

    val deslocamento by transicao.animateFloat(
        initialValue = -900f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1300,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "deslocamento",
    )

    return this
        .clip(
            RoundedCornerShape(raio)
        )
        .background(
            Color.White.copy(alpha = 0.05f)
        )
        .background(
            Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.10f),
                    Color.Transparent,
                ),
                start = Offset(
                    x = deslocamento,
                    y = 0f,
                ),
                end = Offset(
                    x = deslocamento + 320f,
                    y = 320f,
                ),
            )
        )
}

@Composable
fun Esqueleto(
    modifier: Modifier = Modifier,
    altura: Dp = 16.dp,
    raio: Dp = 8.dp,
    largura: Dp? = null,
) {
    Box(
        modifier = modifier
            .then(
                if (largura != null) {
                    Modifier.width(largura)
                } else {
                    Modifier.fillMaxWidth()
                }
            )
            .height(altura)
            .brilhoCarregando(raio),
    )
}

@Composable
fun EsqueletoCartao(
    largura: Dp = 180.dp,
    altura: Dp = 292.dp,
) {
    Box(
        modifier = Modifier
            .width(largura)
            .height(altura)
            .brilhoCarregando(RaioCard),
    )
}

@Composable
fun EsqueletoLargo(
    largura: Dp = 280.dp,
    altura: Dp = 180.dp,
) {
    Box(
        modifier = Modifier
            .width(largura)
            .height(altura)
            .brilhoCarregando(RaioCard),
    )
}