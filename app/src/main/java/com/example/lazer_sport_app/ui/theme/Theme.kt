// app/src/main/java/com/example/lazer_sport_app/ui/theme/Theme.kt
//
// Paleta puxada direto do base.html do site, pra o app não parecer
// outra marca:
//   --azul-escuro  #004AAD   (primária)
//   --azul-claro   #007BFF
//   --vermelho     #E63946   (ação / destaque)
//   --amarelo      #FFD700   (avaliação, selos)
//   fundo escuro   #0D1B2E
//
// Substitui os arquivos Color.kt / Theme.kt / Type.kt que o Android
// Studio gerou -- pode apagar os três e deixar só este.

package com.example.lazer_sport_app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ============================================================
// CORES DA MARCA
// ============================================================
val AzulEscuro = Color(0xFF004AAD)
val AzulClaro = Color(0xFF007BFF)
val Vermelho = Color(0xFFE63946)
val VermelhoEscuro = Color(0xFFC9152D)
val Amarelo = Color(0xFFFFD700)
val NoiteProfunda = Color(0xFF0D1B2E)
val NoiteSuave = Color(0xFF1B3659)
val CinzaClaro = Color(0xFFF4F6FA)
val Verde = Color(0xFF2ECC71)

private val EsquemaClaro = lightColorScheme(
    primary = AzulEscuro,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001A41),

    secondary = AzulClaro,
    onSecondary = Color.White,

    tertiary = Vermelho,
    onTertiary = Color.White,

    background = Color.White,
    onBackground = NoiteProfunda,

    surface = Color.White,
    onSurface = NoiteProfunda,
    surfaceVariant = CinzaClaro,
    onSurfaceVariant = Color(0xFF5A6B85),

    error = Vermelho,
    onError = Color.White,

    outline = Color(0xFFD0D8E4),
)

private val EsquemaEscuro = darkColorScheme(
    primary = Color(0xFF7FA8FF),
    onPrimary = Color(0xFF002B6F),
    primaryContainer = NoiteSuave,
    onPrimaryContainer = Color(0xFFD6E4FF),

    secondary = AzulClaro,
    onSecondary = Color.White,

    tertiary = Color(0xFFFF8A8A),
    onTertiary = Color(0xFF5F0011),

    background = NoiteProfunda,
    onBackground = Color(0xFFE6EDF7),

    surface = Color(0xFF122238),
    onSurface = Color(0xFFE6EDF7),
    surfaceVariant = NoiteSuave,
    onSurfaceVariant = Color(0xFFA9BBD4),

    error = Color(0xFFFF6B6B),
    onError = Color(0xFF5F0011),

    outline = Color(0xFF33486A),
)

// ============================================================
// TIPOGRAFIA
// ============================================================
// O site usa Manrope. Pra usar a mesma no app, baixe os .ttf em
// fonts.google.com/specimen/Manrope, jogue em res/font/ e troque
// FontFamily.Default por FontFamily(Font(R.font.manrope_bold), ...).
// Com a fonte do sistema já fica bom -- é só um passo opcional depois.

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
        fontSize = 26.sp,
        lineHeight = 32.sp,
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

// Raio padrão dos cantos -- usado nos cards e botões
val RaioCard = 20.dp
val RaioBotao = 16.dp
val RaioCampo = 14.dp

@Composable
fun LazerSportTheme(
    escuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val esquema = if (escuro) EsquemaEscuro else EsquemaClaro
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val janela = (view.context as Activity).window
            // Barra de status transparente: o degradê da tela de login
            // sobe até o topo, fica bem mais bonito.
            janela.statusBarColor = Color.Transparent.value.toInt()
            WindowCompat
                .getInsetsController(janela, view)
                .isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = esquema,
        typography = TipografiaLazer,
        content = content,
    )
}