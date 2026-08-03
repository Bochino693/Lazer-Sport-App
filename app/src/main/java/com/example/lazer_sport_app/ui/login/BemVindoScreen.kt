package com.example.lazer_sport_app.ui.login

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lazer_sport_app.R
import com.example.lazer_sport_app.ui.theme.AzulClaro
import com.example.lazer_sport_app.ui.theme.AzulEscuro
import com.example.lazer_sport_app.ui.theme.LazerSportTheme
import com.example.lazer_sport_app.ui.theme.NoiteProfunda
import com.example.lazer_sport_app.ui.theme.RaioBotao
import com.example.lazer_sport_app.ui.theme.Vermelho

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
            .background(
                Brush.verticalGradient(
                    0.0f to NoiteProfunda,
                    0.52f to AzulEscuro,
                    1.0f to AzulClaro,
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SeloMarca()

                Spacer(Modifier.height(14.dp))

                Image(
                    painter = painterResource(R.drawable.ilustracao_abertura),
                    contentDescription = "Ilustração de diversão e jogos",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Diversão começa aqui.",
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Explore brinquedos, peças e serviços Lazer & Sport de um jeito simples.",
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 390.dp),
                )

                Spacer(Modifier.height(26.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 18.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Como você quer continuar?",
                            color = NoiteProfunda,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(18.dp))

                        Button(
                            onClick = aoEntrar,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(RaioBotao),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Vermelho,
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(Icons.Rounded.Login, contentDescription = null)
                            Spacer(Modifier.width(10.dp))
                            Text("Entrar", style = MaterialTheme.typography.labelLarge)
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = aoContinuarComGoogle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(RaioBotao),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NoiteProfunda,
                            ),
                        ) {
                            GoogleMark()
                            Spacer(Modifier.width(10.dp))
                            Text("Continuar com Google")
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = aoCriarConta,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(RaioBotao),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AzulEscuro,
                            ),
                        ) {
                            Icon(Icons.Rounded.PersonAdd, contentDescription = null)
                            Spacer(Modifier.width(10.dp))
                            Text("Criar uma conta")
                        }

                        Spacer(Modifier.height(6.dp))

                        TextButton(onClick = aoContinuarSemLogin) {
                            Text(
                                text = "Continuar sem login",
                                color = NoiteProfunda.copy(alpha = 0.78f),
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = NoiteProfunda.copy(alpha = 0.78f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Ao continuar, você concorda com nossos Termos e Política de Privacidade.",
                    color = Color.White.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 360.dp),
                )
            }
        }
    }
}

@Composable
private fun SeloMarca() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Vermelho),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "L&S",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Spacer(Modifier.width(11.dp))
        Column {
            Text(
                text = "LAZER & SPORT",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                letterSpacing = 1.2.sp,
            )
            Text(
                text = "DESDE 1996",
                color = Color.White.copy(alpha = 0.64f),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun GoogleMark() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(1.dp, Color(0xFFDADCE0), CircleShape)
            .background(Color.White),
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

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun BemVindoPreview() {
    LazerSportTheme(escuro = false) {
        BemVindoScreen(
            aoEntrar = {},
            aoContinuarSemLogin = {},
            aoCriarConta = {},
            aoContinuarComGoogle = {},
        )
    }
}