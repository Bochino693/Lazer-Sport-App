// Identidade visual num lugar so. Continua em ui.menu pelo mesmo
// motivo do FundoSecoes.kt.
//
// IMAGENS ja presentes em res/drawable/:
//   ls_simbolo.png, ls_logo_completa.png
// Nao renomeie: R.drawable e gerado do nome do arquivo.

package com.example.lazer_sport_app.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lazer_sport_app.R
import com.example.lazer_sport_app.ui.theme.AzulPastel

/** Simbolo + nome lado a lado -- formato do cabecalho. */
@Composable
fun LogoComNome(
    modifier: Modifier = Modifier,
    tamanhoSimbolo: Dp = 34.dp,
    mostrarAssinatura: Boolean = false,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
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

/** Logo inteira com letreiro. Topo da gaveta e telas de entrada. */
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

/** Caixinha translucida atras de um icone do cabecalho. */
@Composable
fun FundoIcone(
    corFundo: Color = Color.White.copy(alpha = 0.14f),
    tamanho: Dp = 40.dp,
    conteudo: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(tamanho)
            .vidroTingido(corFundo, raio = 13.dp, intensidade = 1f),
        contentAlignment = Alignment.Center,
    ) {
        conteudo()
    }
}

/** Espacamento padrao entre icones do cabecalho. */
val EspacoIcones = Arrangement.spacedBy(7.dp)