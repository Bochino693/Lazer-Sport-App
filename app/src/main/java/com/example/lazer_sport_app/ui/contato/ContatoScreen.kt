// CONTATO -- espelha o bloco "Venha até a Lazer & Sport" do site.

package com.example.lazer_sport_app.ui.contato

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lazer_sport_app.data.Contato
import com.example.lazer_sport_app.ui.components.BotaoPrincipal
import com.example.lazer_sport_app.ui.components.BotaoVidro
import com.example.lazer_sport_app.ui.components.Kicker
import com.example.lazer_sport_app.ui.components.TopoTela
import com.example.lazer_sport_app.ui.menu.fundoFaixaRosa
import com.example.lazer_sport_app.ui.menu.fundoNoite
import com.example.lazer_sport_app.ui.menu.vidro
import com.example.lazer_sport_app.ui.menu.vidroTingido
import com.example.lazer_sport_app.ui.theme.Amarelo
import com.example.lazer_sport_app.ui.theme.AzulDardo
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.RaioSecao
import com.example.lazer_sport_app.ui.theme.TextoForte
import com.example.lazer_sport_app.ui.theme.TextoFraco
import com.example.lazer_sport_app.ui.theme.Verde

@Composable
fun ContatoScreen(aoVoltar: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopoTela(
                titulo = "Fale com a gente",
                subtitulo = "Orçamentos, visitas e suporte",
                aoVoltar = aoVoltar,
            )
        },
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .fundoNoite()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 34.dp),
        ) {
            Spacer(Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(RaioSecao))
                    .fundoFaixaRosa()
                    .padding(horizontal = 22.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Kicker("ATENDIMENTO DIRETO", Color.White)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Chame no WhatsApp",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "É o canal mais rápido: orçamento, locação, " +
                            "peças e manutenção falam pelo mesmo número.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(20.dp))
                BotaoPrincipal(
                    texto = Contato.TELEFONE_EXIBICAO,
                    aoClicar = {
                        uriHandler.openUri(
                            Contato.whatsapp(
                                "Olá! Vim pelo app da Lazer & Sport e gostaria de mais informações."
                            )
                        )
                    },
                    cor = Color.White,
                    corTexto = Color(0xFF8E1F33),
                    icone = Icons.AutoMirrored.Filled.Chat,
                )
            }

            Spacer(Modifier.height(22.dp))

            LinhaContato(
                icone = Icons.Filled.Place,
                cor = AzulDardo,
                rotulo = "Endereço",
                valor = Contato.ENDERECO,
                aoClicar = { uriHandler.openUri(Contato.MAPA) },
            )
            Spacer(Modifier.height(10.dp))
            LinhaContato(
                icone = Icons.Filled.Phone,
                cor = Verde,
                rotulo = "Telefone / WhatsApp",
                valor = Contato.TELEFONE_EXIBICAO,
                aoClicar = { uriHandler.openUri("tel:+${Contato.WHATSAPP}") },
            )
            Spacer(Modifier.height(10.dp))
            LinhaContato(
                icone = Icons.Filled.Email,
                cor = AzulPastel,
                rotulo = "E-mail",
                valor = Contato.EMAIL,
                aoClicar = { uriHandler.openUri("mailto:${Contato.EMAIL}") },
            )
            Spacer(Modifier.height(10.dp))
            LinhaContato(
                icone = Icons.Filled.Schedule,
                cor = Amarelo,
                rotulo = "Atendimento",
                valor = "Segunda a sexta, 8h às 18h · Sábado, 8h às 12h",
                aoClicar = {},
            )
            Spacer(Modifier.height(10.dp))
            LinhaContato(
                icone = Icons.Filled.Language,
                cor = AzulDardo,
                rotulo = "Site",
                valor = "lazersport.com.br",
                aoClicar = { uriHandler.openUri(Contato.SITE) },
            )

            Spacer(Modifier.height(22.dp))

            BotaoVidro(
                texto = "Traçar rota até o showroom",
                aoClicar = { uriHandler.openUri(Contato.MAPA) },
                icone = Icons.Filled.Map,
            )
        }
    }
}

@Composable
private fun LinhaContato(
    icone: ImageVector,
    cor: Color,
    rotulo: String,
    valor: String,
    aoClicar: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .vidro(raio = 18.dp, intensidade = 0.06f)
            .clickable(onClick = aoClicar)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .vidroTingido(cor, raio = 13.dp, intensidade = 0.16f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(rotulo, style = MaterialTheme.typography.labelSmall, color = TextoFraco)
            Text(valor, style = MaterialTheme.typography.bodyMedium, color = TextoForte)
        }
    }
}