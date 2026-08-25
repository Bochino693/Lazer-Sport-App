// Tema unico da marca -- escuro sempre.
//
// POR QUE MORREU O ESQUEMA CLARO: o app inteiro desenha sobre azul-noite.
// Com um lightColorScheme de background branco, o Material pintava de
// branco tudo que nao tivesse fundo proprio: ModalDrawerSheet,
// NavigationBar, Card, Surface. A "branquidao" vinha daqui, nao das telas.

package br.com.lazersport.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ============ PALETA (fonte unica de cor do app) ============
//
// POR QUE MUDOU: a paleta antiga era azul-quase-preto com brilhos rosa e
// azul pintados por cima a 6-25% de opacidade. Cor saturada em opacidade
// baixa sobre fundo escuro nao fica discreta -- fica cinza. O app inteiro
// vinha lavado, empoeirado, e a queixa de "opaco" e "morto" era isso:
// nenhuma cor chegava perto da propria saturacao em lugar nenhum.
//
// A troca tem duas partes, e a segunda importa mais que a primeira:
//
//   1. matizes de fato vivos, com o azul abrindo em violeta e ciano;
//   2. os brilhos passam a ser compostos em BlendMode.Screen, em
//      FundoSecoes.kt. Screen soma luz em vez de misturar tinta: o mesmo
//      ciano que virava cinza-azulado agora acende. Trocar so' os matizes
//      sem trocar a composicao teria dado outro tom de poeira.
//
// O vermelho da marca continua sendo o acento quente, e o azul continua
// sendo a base -- e' a logo que manda, e ela e' azul e vermelha.

// -- fundos ------------------------------------------------------------
// A rampa vai de indigo quase preto ate um azul eletrico de verdade. E'
// essa amplitude que da profundidade: antes os tres tons ficavam todos na
// mesma faixa escura e a tela parecia chapada.
val NoiteTopo = Color(0xFF050A22)
val NoiteMeio = Color(0xFF101C6B)
val NoiteBase = Color(0xFF2A2FC4)

// Superficies. Puxadas para o azul, e nao para o cinza: cartao cinza em
// cima de fundo colorido e' o que mais envelhece uma tela.
val NoiteCartao = Color(0xFF0E1638)
val NoiteCampo = Color(0xFF18225C)

// -- azuis e ciano ------------------------------------------------------
val AzulProfundo = Color(0xFF2A2FC4)
val AzulVivo = Color(0xFF3D6BFF)
/** O ciano de acento. E' ele que carrega a energia da paleta. */
val AzulDardo = Color(0xFF00E5FF)
val AzulPastel = Color(0xFFA9C8FF)

// -- quentes ------------------------------------------------------------
/** Vermelho da marca, agora na saturacao cheia. Acento e alerta. */
val RosaMarca = Color(0xFFFF2D6F)
val RosaEscuro = Color(0xFFC41E58)
/** Fecha o degrade dos botoes principais: vermelho abrindo em laranja. */
val LaranjaQuente = Color(0xFFFF7A29)
/** Violeta que liga o azul ao vermelho nos brilhos de fundo. */
val VioletaNeon = Color(0xFF8B5CF6)
val Amarelo = Color(0xFFFFCC33)
val Verde = Color(0xFF22C55E)

// -- texto e bordas -----------------------------------------------------
val TextoForte = Color(0xFFFFFFFF)
val TextoMedio = Color(0xFFB9CEF0)
val TextoFraco = Color(0xFF7E97C4)
val BordaSuave = Color(0xFF2E45A0)

// ============ RAIOS ============
val RaioSecao = 28.dp
val RaioCard = 22.dp
val RaioBotao = 16.dp
val RaioCampo = 14.dp

private val EsquemaLazer = darkColorScheme(
    primary = AzulVivo,
    onPrimary = Color.White,
    primaryContainer = AzulProfundo,
    onPrimaryContainer = Color.White,

    secondary = AzulDardo,
    onSecondary = Color(0xFF00202B),
    secondaryContainer = NoiteCampo,
    onSecondaryContainer = TextoForte,

    tertiary = RosaMarca,
    onTertiary = Color.White,

    background = NoiteTopo,
    onBackground = TextoForte,

    surface = NoiteCartao,
    onSurface = TextoForte,
    surfaceVariant = NoiteCampo,
    onSurfaceVariant = TextoMedio,

    error = RosaMarca,
    onError = Color.White,

    outline = BordaSuave,
    outlineVariant = Color(0xFF1E2C6E),

    scrim = Color(0xFF02040E),
)

// O site usa Manrope. Pra igualar: .ttf em res/font/ e trocar
// FontFamily.Default por FontFamily(Font(R.font.manrope_bold), ...).
val TipografiaLazer = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.3).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        letterSpacing = 0.3.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp,
    ),
)

@Composable
fun LazerSportTheme(content: @Composable () -> Unit) {
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val janela = (view.context as Activity).window
            // window.statusBarColor virou no-op na API 35. Com
            // enableEdgeToEdge() na MainActivity so falta garantir
            // icone claro sobre o fundo escuro.
            WindowCompat.getInsetsController(janela, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = EsquemaLazer,
        typography = TipografiaLazer,
        content = content,
    )
}