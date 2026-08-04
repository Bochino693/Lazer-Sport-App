// Tema unico da marca -- escuro sempre.
//
// POR QUE MORREU O ESQUEMA CLARO: o app inteiro desenha sobre azul-noite.
// Com um lightColorScheme de background branco, o Material pintava de
// branco tudo que nao tivesse fundo proprio: ModalDrawerSheet,
// NavigationBar, Card, Surface. A "branquidao" vinha daqui, nao das telas.

package com.example.lazer_sport_app.ui.theme

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
// Azuis institucionais do CSS do site; rosa e azul-dardo amostrados
// dos pixels da logo (logoofi.png).

val NoiteTopo = Color(0xFF060E1C)
val NoiteMeio = Color(0xFF0A1A33)
val NoiteBase = Color(0xFF07254D)
val NoiteCartao = Color(0xFF0C1B31)
val NoiteCampo = Color(0xFF12294A)

val AzulProfundo = Color(0xFF004AAD)
val AzulVivo = Color(0xFF0878F9)
val AzulDardo = Color(0xFF34BAEC)
val AzulPastel = Color(0xFF91C2FF)

val RosaMarca = Color(0xFFEE405E)
val RosaEscuro = Color(0xFFB43544)
val Amarelo = Color(0xFFFFC53D)
val Verde = Color(0xFF16A34A)

val TextoForte = Color(0xFFEAF1FB)
val TextoMedio = Color(0xFFA9BBD4)
val TextoFraco = Color(0xFF7489A8)
val BordaSuave = Color(0xFF25406B)

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
    onSecondary = Color(0xFF00243D),
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
    outlineVariant = Color(0xFF1A2E4E),

    scrim = Color(0xFF03070F),
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