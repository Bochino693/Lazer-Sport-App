package com.example.lazer_sport_app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lazer_sport_app.ui.components.BotaoPrincipal
import com.example.lazer_sport_app.ui.components.BotaoVidro
import com.example.lazer_sport_app.ui.components.Kicker
import com.example.lazer_sport_app.ui.menu.PainelMarcaEntrada
import com.example.lazer_sport_app.ui.menu.fundoHero
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.RosaMarca
import com.example.lazer_sport_app.ui.theme.TextoFraco
import com.example.lazer_sport_app.ui.theme.TextoMedio

@Composable
fun BemVindoScreen(
    aoEntrar: () -> Unit,
    aoContinuarSemLogin: () -> Unit,
    aoCriarConta: () -> Unit,
    aoContinuarComGoogle: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .fundoHero(),
    ) {
        val paisagem = maxWidth > maxHeight || maxWidth >= 700.dp

        if (paisagem) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PainelMarcaEntrada(
                    modifier = Modifier.weight(1.08f),
                    altura = 300.dp,
                )

                ConteudoBemVindo(
                    aoEntrar = aoEntrar,
                    aoContinuarSemLogin = aoContinuarSemLogin,
                    aoCriarConta = aoCriarConta,
                    aoContinuarComGoogle = aoContinuarComGoogle,
                    compacto = true,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PainelMarcaEntrada(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
                    altura = 158.dp,
                )

                Spacer(Modifier.size(18.dp))

                ConteudoBemVindo(
                    aoEntrar = aoEntrar,
                    aoContinuarSemLogin = aoContinuarSemLogin,
                    aoCriarConta = aoCriarConta,
                    aoContinuarComGoogle = aoContinuarComGoogle,
                    compacto = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
                )
            }
        }
    }
}

@Composable
private fun ConteudoBemVindo(
    aoEntrar: () -> Unit,
    aoContinuarSemLogin: () -> Unit,
    aoCriarConta: () -> Unit,
    aoContinuarComGoogle: () -> Unit,
    compacto: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Kicker("DESDE 1996")

        Spacer(Modifier.size(if (compacto) 12.dp else 14.dp))

        Text(
            text = "Diversão começa aqui.",
            color = Color.White,
            style = if (compacto) {
                MaterialTheme.typography.headlineLarge
            } else {
                MaterialTheme.typography.displayMedium
            },
            textAlign = TextAlign.Center,
            lineHeight = if (compacto) 38.sp else 44.sp,
        )

        Spacer(Modifier.size(8.dp))

        Text(
            text = "Brinquedos, peças e serviços Lazer & Sport na palma da mão.",
            color = TextoMedio,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 390.dp),
        )

        Spacer(Modifier.size(if (compacto) 20.dp else 24.dp))

        // As duas ações principais ficam lado a lado.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BotaoPrincipal(
                texto = "Entrar",
                aoClicar = aoEntrar,
                cor = RosaMarca,
                icone = Icons.AutoMirrored.Rounded.Login,
                modifier = Modifier.weight(1f),
            )

            BotaoVidro(
                texto = "Criar conta",
                aoClicar = aoCriarConta,
                icone = Icons.Rounded.PersonAdd,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.size(10.dp))

        BotaoVidro(
            texto = "Continuar com Google",
            aoClicar = aoContinuarComGoogle,
            conteudoInicial = { MarcaGoogle() },
        )

        Spacer(Modifier.size(6.dp))

        TextButton(onClick = aoContinuarSemLogin) {
            Text(
                text = "Explorar sem login",
                color = AzulPastel,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = AzulPastel,
                modifier = Modifier.size(18.dp),
            )
        }

        Text(
            text = "Ao continuar, você concorda com nossos Termos e Política de Privacidade.",
            color = TextoFraco,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 380.dp),
        )

        Spacer(Modifier.size(10.dp))
    }
}

@Composable
internal fun MarcaGoogle() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFDADCE0),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "G",
            color = Color(0xFF4285F4),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
        )
    }
}