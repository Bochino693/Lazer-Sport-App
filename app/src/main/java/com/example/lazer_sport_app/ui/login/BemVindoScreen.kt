package com.example.lazer_sport_app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lazer_sport_app.ui.components.BotaoPrincipal
import com.example.lazer_sport_app.ui.components.BotaoVidro
import com.example.lazer_sport_app.ui.components.Kicker
import com.example.lazer_sport_app.ui.menu.PainelMarcaEntrada
import com.example.lazer_sport_app.ui.menu.fundoHero
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.LazerSportTheme
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .fundoHero(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 24.dp,
                    vertical = 20.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(8.dp))

                PainelMarcaEntrada(
                    modifier = Modifier.fillMaxWidth(),
                    altura = 210.dp,
                )

                Spacer(Modifier.height(20.dp))

                Kicker("DESDE 1996")

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Diversão\ncomeça aqui.",
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Brinquedos, peças e serviços Lazer & Sport na palma da mão.",
                    color = TextoMedio,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 340.dp),
                )

                Spacer(Modifier.height(34.dp))

                BotaoPrincipal(
                    texto = "Entrar",
                    aoClicar = aoEntrar,
                    cor = RosaMarca,
                    icone = Icons.AutoMirrored.Rounded.Login,
                )

                Spacer(Modifier.height(12.dp))

                BotaoVidro(
                    texto = "Continuar com Google",
                    aoClicar = aoContinuarComGoogle,
                    conteudoInicial = {
                        MarcaGoogle()
                    },
                )

                Spacer(Modifier.height(12.dp))

                BotaoVidro(
                    texto = "Criar uma conta",
                    aoClicar = aoCriarConta,
                    icone = Icons.Rounded.PersonAdd,
                )

                Spacer(Modifier.height(14.dp))

                TextButton(
                    onClick = aoContinuarSemLogin,
                ) {
                    Text(
                        text = "Continuar sem login",
                        color = AzulPastel,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(Modifier.width(6.dp))

                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = AzulPastel,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Ao continuar, você concorda com nossos Termos e " +
                            "Política de Privacidade.",
                    color = TextoFraco,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 340.dp),
                )

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/**
 * Símbolo temporário do botão do Google.
 *
 * Caso você já tenha criado ic_google.xml e GoogleIcon.kt,
 * substitua MarcaGoogle() por IconeGoogle().
 */
@Composable
private fun MarcaGoogle() {
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

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun BemVindoPreview() {
    LazerSportTheme {
        BemVindoScreen(
            aoEntrar = {},
            aoContinuarSemLogin = {},
            aoCriarConta = {},
            aoContinuarComGoogle = {},
        )
    }
}