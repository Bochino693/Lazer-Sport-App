package br.com.lazersport.app.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.lazersport.app.R

/**
 * Marca do cabeçalho compacto.
 *
 * É a logotipia completa, sem recorte e sem texto ao lado: o nome e a
 * assinatura "Brinquedos · Cenografia · Parques" já fazem parte da arte.
 * Antes aqui entrava o alvo recortado com o nome escrito ao lado, o que
 * duplicava a marca e mostrava uma versão que não é a oficial.
 *
 * @param tamanhoSimbolo altura da logotipia. A largura acompanha a
 *   proporção original da arte.
 */
@Composable
fun LogoComNome(
    modifier: Modifier = Modifier,
    tamanhoSimbolo: Dp = 34.dp,
    mostrarAssinatura: Boolean = false,
) {
    // A arte tem a assinatura embutida; o parâmetro só decide quanto de
    // altura ela recebe, para a linha miúda continuar legível quando é
    // para aparecer.
    val altura = if (mostrarAssinatura) tamanhoSimbolo * 1.25f else tamanhoSimbolo

    Image(
        painter = painterResource(R.drawable.ls_logo_completa),
        contentDescription = "Lazer & Sport Brinquedos",
        contentScale = ContentScale.Fit,
        modifier = modifier.height(altura),
    )
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