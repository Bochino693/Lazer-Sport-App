package com.example.lazer_sport_app.ui.menu

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lazer_sport_app.R
import com.example.lazer_sport_app.ui.theme.AzulPastel

/**
 * Símbolo e nome usados no cabeçalho compacto do aplicativo.
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
        Image(
            painter = painterResource(R.drawable.ls_simbolo),
            contentDescription = "Logo Lazer & Sport",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(tamanhoSimbolo),
        )

        Spacer(Modifier.width(10.dp))

        Column {
            Text(
                text = "LAZER & SPORT",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
            )

            if (mostrarAssinatura) {
                Text(
                    text = "BRINQUEDOS · CENOGRAFIA · PARQUES",
                    color = AzulPastel.copy(alpha = 0.85f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                )
            }
        }
    }
}

/**
 * Exibe a imagem completa da Lazer & Sport sem nenhum corte.
 */
@Composable
fun LogoCompleta(
    modifier: Modifier = Modifier,
    largura: Dp = 190.dp,
) {
    Image(
        painter = painterResource(R.drawable.ls_logo_completa),
        contentDescription = "Lazer & Sport Brinquedos",
        contentScale = ContentScale.Fit,
        modifier = modifier.width(largura),
    )
}

/**
 * Painel utilizado nas telas de boas-vindas e login.
 *
 * ContentScale.Fit garante que alvo, dardo, nome e assinatura
 * apareçam completamente, sem cortes.
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
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF06162C),
                        Color(0xFF0756B5),
                        Color(0xFFB62843),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = formato,
            )
            .padding(
                horizontal = 24.dp,
                vertical = 18.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(
                R.drawable.ls_logo_completa,
            ),
            contentDescription = "Lazer & Sport Brinquedos",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Fundo translúcido usado atrás dos ícones do cabeçalho.
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

/**
 * Espaçamento padrão entre os ícones do cabeçalho.
 */
val EspacoIcones = Arrangement.spacedBy(7.dp)