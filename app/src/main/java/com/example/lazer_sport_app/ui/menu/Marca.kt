// app/src/main/java/com/example/lazer_sport_app/ui/menu/Marca.kt
//
// Identidade visual da Lazer & Sport num lugar so.
//
// As cores abaixo NAO foram escolhidas a olho: foram amostradas dos
// pixels da propria logo (core/static/images/logoofi.png do site).
// Por isso os icones do app combinam de verdade com a marca em vez de
// so "puxarem pro azul".
//
// IMAGENS -- copie os dois PNG para app/src/main/res/drawable/:
//   ls_simbolo.png         (o alvo com o dardo, fundo transparente)
//   ls_logo_completa.png   (logo inteira, com o letreiro)
//
// Nao renomeie: o R.drawable e gerado a partir do nome do arquivo, e
// nome com maiuscula ou hifen quebra a compilacao.

package com.example.lazer_sport_app.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lazer_sport_app.R

// ============================================================
// CORES AMOSTRADAS DA LOGO
// ============================================================

/** Rosa do alvo -- a cor de acao/destaque da marca. */
val MarcaRosa = Color(0xFFEE405E)

/** Sombra do alvo -- usada em estados pressionados e bordas. */
val MarcaRosaEscuro = Color(0xFFB43544)

/** Azul do dardo -- o acento mais vivo, otimo sobre fundo escuro. */
val MarcaAzulDardo = Color(0xFF34BAEC)

/** Azuis institucionais, herdados do CSS do site. */
val MarcaAzulProfundo = Color(0xFF004AAD)
val MarcaAzulVivo = Color(0xFF0878F9)

// ============================================================
// LOGOTIPO
// ============================================================

/**
 * Simbolo + nome, lado a lado -- o formato do cabecalho.
 *
 * @param sobreEscuro true no cabecalho azul (texto branco);
 *                    false em fundo claro.
 */
@Composable
fun LogoComNome(
    modifier: Modifier = Modifier,
    tamanhoSimbolo: Dp = 34.dp,
    sobreEscuro: Boolean = true,
    mostrarAssinatura: Boolean = false,
) {
    val corNome = if (sobreEscuro) Color.White else MarcaAzulProfundo
    val corAssinatura =
        if (sobreEscuro) Color.White.copy(alpha = 0.65f) else MarcaRosa

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
                color = corNome,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
            )
            if (mostrarAssinatura) {
                Text(
                    text = "BRINQUEDOS · CENOGRAFIA · PARQUES",
                    color = corAssinatura,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                )
            }
        }
    }
}

/** Logo inteira (com o letreiro). Usada no topo da gaveta. */
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
 * Caixinha translucida atras de um icone do cabecalho. E o que da
 * "peso" ao carrinho em vez de deixar o icone solto no azul.
 */
@Composable
fun FundoIcone(
    corFundo: Color = Color.White.copy(alpha = 0.14f),
    conteudo: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(corFundo),
        contentAlignment = Alignment.Center,
    ) {
        conteudo()
    }
}

/** Espacamento padrao entre os icones do cabecalho. */
val EspacoIcones = Arrangement.spacedBy(7.dp)

/** Padding lateral usado dentro das secoes. */
val PaddingSecao = 22.dp

@Composable
internal fun EspacadorVertical(altura: Dp) {
    Spacer(Modifier.padding(vertical = altura / 2))
}