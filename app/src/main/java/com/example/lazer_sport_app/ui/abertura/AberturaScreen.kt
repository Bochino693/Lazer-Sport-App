// TELA DE ABERTURA -- logo, pulso da marca e o estado real da API.
//
// Não é enfeite: é aqui que o app pergunta /status/ e decide o que vai
// existir na sessão. Sem essa consulta o cliente descobria que a API
// estava fora só depois de três telas vazias.

package com.example.lazer_sport_app.ui.abertura

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lazer_sport_app.data.EstadoApi
import com.example.lazer_sport_app.data.SaudeApi
import com.example.lazer_sport_app.data.StatusRepository
import com.example.lazer_sport_app.ui.components.BotaoPrincipal
import com.example.lazer_sport_app.ui.components.BotaoVidro
import com.example.lazer_sport_app.ui.menu.LogoCompleta
import com.example.lazer_sport_app.ui.menu.fundoHero
import com.example.lazer_sport_app.ui.menu.vidroTingido
import com.example.lazer_sport_app.ui.theme.Amarelo
import com.example.lazer_sport_app.ui.theme.AzulDardo
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.RosaMarca
import com.example.lazer_sport_app.ui.theme.TextoFraco
import com.example.lazer_sport_app.ui.theme.TextoMedio
import com.example.lazer_sport_app.ui.theme.Verde
import com.example.lazer_sport_app.ui.theme.brilhoCarregando
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AberturaViewModel @Inject constructor(
    private val repositorio: StatusRepository,
) : ViewModel() {

    val estado: StateFlow<EstadoApi> = repositorio.estado

    init { verificar() }

    fun verificar() {
        viewModelScope.launch { repositorio.verificar() }
    }
}

@Composable
fun AberturaScreen(
    aoContinuar: () -> Unit,
    viewModel: AberturaViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsState()

    // Segura meio segundo mesmo quando a resposta é instantânea: piscar
    // a marca e sumir fica pior do que não ter splash nenhum.
    LaunchedEffect(estado.saude) {
        if (estado.saude == SaudeApi.COMPLETA || estado.saude == SaudeApi.PARCIAL) {
            delay(650)
            aoContinuar()
        }
    }

    val pulso = rememberInfiniteTransition(label = "pulso")
    val escala by pulso.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "escala",
    )

    val opacidadeMarca by animateFloatAsState(
        targetValue = if (estado.saude == SaudeApi.VERIFICANDO) 1f else 0.92f,
        animationSpec = tween(500),
        label = "opacidade",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .fundoHero(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LogoCompleta(
                largura = 240.dp,
                modifier = Modifier
                    .scale(if (estado.saude == SaudeApi.VERIFICANDO) escala else 1f)
                    .alpha(opacidadeMarca),
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "BRINQUEDOS · CENOGRAFIA · PARQUES",
                color = AzulPastel.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )

            Spacer(Modifier.height(44.dp))

            when (estado.saude) {
                SaudeApi.VERIFICANDO -> Carregando()

                SaudeApi.COMPLETA -> Aviso(
                    cor = Verde,
                    icone = Icons.Filled.CheckCircle,
                    titulo = "Tudo pronto",
                    detalhe = "Catálogo sincronizado · API v${estado.versao}",
                )

                SaudeApi.PARCIAL -> Aviso(
                    cor = Amarelo,
                    icone = Icons.Filled.WarningAmber,
                    titulo = "Quase lá",
                    detalhe = "${estado.recursos.size} seções no ar · " +
                            "as demais aparecem assim que forem publicadas",
                )

                SaudeApi.FORA -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Aviso(
                        cor = RosaMarca,
                        icone = Icons.Filled.CloudOff,
                        titulo = "Servidor fora de alcance",
                        detalhe = estado.detalhe,
                    )
                    Spacer(Modifier.height(26.dp))
                    BotaoPrincipal(
                        texto = "Tentar de novo",
                        aoClicar = viewModel::verificar,
                        cor = RosaMarca,
                        icone = Icons.Filled.Sync,
                    )
                    Spacer(Modifier.height(10.dp))
                    BotaoVidro(
                        texto = "Entrar mesmo assim",
                        aoClicar = aoContinuar,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Sem conexão você navega no que já foi visto, " +
                                "mas preços e disponibilidade podem estar desatualizados.",
                        color = TextoFraco,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Text(
            text = "Lazer & Sport Brinquedos · v1.0",
            color = TextoFraco.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp),
        )
    }
}

@Composable
private fun Carregando() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(5.dp)
                .brilhoCarregando(raio = 999.dp),
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Conectando ao catálogo...",
            color = TextoMedio,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun Aviso(
    cor: Color,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    detalhe: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .vidroTingido(cor, raio = 18.dp, intensidade = 0.14f)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .vidroTingido(cor, raio = 12.dp, intensidade = 0.18f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.width(0.dp).then(Modifier.fillMaxWidth())) {
            Text(
                text = titulo,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = detalhe,
                color = TextoMedio,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
    Spacer(Modifier.height(0.dp))
}