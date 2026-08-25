// Campos, botoes, busca e estrutura de secao. Arquivo estava vazio.

package br.com.lazersport.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.lazersport.app.ui.menu.fundoFaixaAzul
import br.com.lazersport.app.ui.menu.fundoSecaoAzul
import br.com.lazersport.app.ui.menu.fundoSecaoEscura
import br.com.lazersport.app.ui.menu.vidro
import br.com.lazersport.app.ui.theme.AzulDardo
import br.com.lazersport.app.ui.theme.AzulPastel
import br.com.lazersport.app.ui.theme.NoiteCampo
import br.com.lazersport.app.ui.theme.RaioBotao
import br.com.lazersport.app.ui.theme.RaioCampo
import br.com.lazersport.app.ui.theme.RaioSecao
import br.com.lazersport.app.ui.theme.LaranjaQuente
import br.com.lazersport.app.ui.theme.RosaMarca
import br.com.lazersport.app.ui.theme.TextoForte
import br.com.lazersport.app.ui.theme.TextoFraco
import br.com.lazersport.app.ui.theme.TextoMedio

// ============ BUSCA ============

@Composable
fun BarraBusca(
    valor: String,
    aoMudar: (String) -> Unit,
    dica: String = "Buscar...",
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .vidro(raio = RaioBotao, intensidade = 0.08f)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = AzulDardo)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (valor.isEmpty()) {
                Text(dica, color = TextoFraco, style = MaterialTheme.typography.bodyMedium)
            }
            BasicTextField(
                value = valor,
                onValueChange = aoMudar,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextoForte),
                cursorBrush = SolidColor(AzulDardo),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            )
        }
        if (valor.isNotEmpty()) {
            IconButton(onClick = { aoMudar("") }, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Limpar busca",
                    tint = TextoFraco,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ============ CAMPO ============

/**
 * O OutlinedTextField padrao herda cores pensadas pra fundo claro --
 * sobre o azul-noite o texto some. Estas cores resolvem de uma vez.
 */
@Composable
fun CampoLazer(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    icone: ImageVector? = null,
    iconeFinal: @Composable (() -> Unit)? = null,
    erro: Boolean = false,
    linhaUnica: Boolean = true,
    minhasLinhas: Int = 1,
    transformacao: VisualTransformation = VisualTransformation.None,
    opcoesTeclado: KeyboardOptions = KeyboardOptions.Default,
    acoesTeclado: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        label = { Text(rotulo) },
        singleLine = linhaUnica,
        minLines = minhasLinhas,
        isError = erro,
        visualTransformation = transformacao,
        keyboardOptions = opcoesTeclado,
        keyboardActions = acoesTeclado,
        leadingIcon = icone?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = iconeFinal,
        shape = RoundedCornerShape(RaioCampo),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextoForte,
            unfocusedTextColor = TextoForte,
            focusedContainerColor = NoiteCampo.copy(alpha = 0.55f),
            unfocusedContainerColor = NoiteCampo.copy(alpha = 0.35f),
            errorContainerColor = NoiteCampo.copy(alpha = 0.35f),
            cursorColor = AzulDardo,
            focusedBorderColor = AzulDardo,
            unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
            errorBorderColor = RosaMarca,
            focusedLabelColor = AzulDardo,
            unfocusedLabelColor = TextoFraco,
            errorLabelColor = RosaMarca,
            focusedLeadingIconColor = AzulDardo,
            unfocusedLeadingIconColor = TextoFraco,
            focusedTrailingIconColor = AzulDardo,
            unfocusedTrailingIconColor = TextoFraco,
        ),
    )
}

// ============ BOTOES ============

/**
 * Botao principal.
 *
 * @param corFim segunda cor do degrade. Quando nao vem, o botao da marca
 *   (vermelho) abre em laranja e qualquer outra cor fica chapada. Assim o
 *   botao de acao ganha o degrade sem precisar mexer nas dezenas de
 *   chamadas espalhadas pelo app, e um botao verde de confirmar continua
 *   verde, sem virar um arco-iris.
 */
@Composable
fun BotaoPrincipal(
    texto: String,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
    cor: Color = RosaMarca,
    corFim: Color? = null,
    corTexto: Color = Color.White,
    habilitado: Boolean = true,
    carregando: Boolean = false,
    icone: ImageVector? = null,
) {
    val fim = corFim ?: if (cor == RosaMarca) LaranjaQuente else cor
    val ativo = habilitado && !carregando

    Button(
        onClick = aoClicar,
        enabled = ativo,
        shape = RoundedCornerShape(RaioBotao),
        // O preenchimento vem do degrade no modifier, entao o Button nao
        // pinta nada por baixo -- inclusive no estado desligado, senao o
        // cinza do Material apareceria por cima do degrade.
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = corTexto,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = corTexto.copy(alpha = 0.6f),
        ),
        // O degrade e' pintado no modifier, fora do padding; este aqui
        // so' impede o texto de encostar na borda quando e' comprido.
        contentPadding = PaddingValues(horizontal = 18.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(RaioBotao))
            .background(
                Brush.linearGradient(
                    listOf(
                        cor.copy(alpha = if (ativo) 1f else 0.35f),
                        fim.copy(alpha = if (ativo) 1f else 0.35f),
                    ),
                ),
            ),
    ) {
        if (carregando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = corTexto,
                strokeWidth = 2.dp,
            )
        } else {
            if (icone != null) {
                Icon(icone, contentDescription = null)
                Spacer(Modifier.width(10.dp))
            }
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Botao secundario: vidro com borda, sem preenchimento. */
@Composable
fun BotaoVidro(
    texto: String,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
    icone: ImageVector? = null,
    conteudoInicial: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .vidro(raio = RaioBotao, intensidade = 0.07f)
            .clickable(onClick = aoClicar)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        conteudoInicial?.invoke()
        if (icone != null) {
            Icon(icone, contentDescription = null, tint = TextoForte)
        }
        if (conteudoInicial != null || icone != null) Spacer(Modifier.width(10.dp))
        Text(texto, color = TextoForte, style = MaterialTheme.typography.labelLarge)
    }
}

// ============ ESTRUTURA DE SECAO ============

/** Pilula de rotulo acima do titulo -- o `.ls-section-kicker` do site. */
@Composable
fun Kicker(texto: String, cor: Color = AzulPastel) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(cor.copy(alpha = 0.14f))
            .border(1.dp, cor.copy(alpha = 0.30f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            text = texto,
            color = cor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.4.sp,
        )
    }
}

@Composable
fun CabecalhoSecao(
    kicker: String,
    titulo: String,
    subtitulo: String? = null,
    acao: String? = null,
    aoAcao: () -> Unit = {},
    corKicker: Color = AzulPastel,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = 22.dp)) {
        Kicker(kicker, corKicker)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextoForte,
                    lineHeight = 30.sp,
                )
                if (subtitulo != null) {
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoMedio,
                    )
                }
            }
            if (acao != null) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .fundoFaixaAzul()
                        .clickable(onClick = aoAcao)
                        .padding(horizontal = 15.dp, vertical = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = acao,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Bloco escuro sangrando ate as bordas. */
@Composable
fun SecaoEscura(
    kicker: String,
    titulo: String,
    subtitulo: String? = null,
    acao: String? = null,
    aoAcao: () -> Unit = {},
    corKicker: Color = AzulPastel,
    conteudo: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fundoSecaoEscura()
            .padding(vertical = 34.dp),
    ) {
        CabecalhoSecao(kicker, titulo, subtitulo, acao, aoAcao, corKicker)
        Spacer(Modifier.height(22.dp))
        conteudo()
    }
}

/** Cartao azul-aco arredondado. Substitui a antiga secao clara. */
@Composable
fun SecaoAzul(
    kicker: String,
    titulo: String,
    subtitulo: String? = null,
    acao: String? = null,
    aoAcao: () -> Unit = {},
    corKicker: Color = AzulDardo,
    conteudo: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(RaioSecao))
            .fundoSecaoAzul()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(RaioSecao))
            .padding(vertical = 30.dp),
    ) {
        CabecalhoSecao(kicker, titulo, subtitulo, acao, aoAcao, corKicker)
        Spacer(Modifier.height(20.dp))
        conteudo()
    }
}