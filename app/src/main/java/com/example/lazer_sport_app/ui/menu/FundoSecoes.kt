// app/src/main/java/com/example/lazer_sport_app/ui/menu/FundoSecoes.kt
//
// Reproduz em Compose os fundos das secoes do lazersport.com.br.
//
// No site, cada bloco tem TRES camadas empilhadas:
//   1. um degrade linear diagonal (a base)
//   2. dois brilhos radiais -- azul num canto, vermelho no oposto
//   3. uma grade de 42px tipo papel milimetrado, esmaecendo pra baixo
//
// Aqui isso vira `Modifier.drawBehind`, que desenha atras do conteudo
// sem criar nenhuma View extra. Nao usa imagem: e tudo vetorial, entao
// escala em qualquer tela sem pesar no APK.
//
// Valores copiados do <style id="ls-home-sections-theme"> e do
// .ls-home-hero do home.html -- se mudar o site, mude aqui tambem.

package com.example.lazer_sport_app.ui.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================
// PALETA DAS SECOES (hex direto do CSS do site)
// ============================================================

private val AzulBrilho = Color(0xFF0878F9)
private val VermelhoBrilho = Color(0xFFE63946)

// Hero: o azul mais aberto da home
private val HeroTopo = Color(0xFF071426)
private val HeroMeio = Color(0xFF0A2447)
private val HeroBase = Color(0xFF063D83)

// Secao escura (categorias, estabelecimentos, eventos)
private val EscuroTopo = Color(0xFF0A1728)
private val EscuroMeio = Color(0xFF050B14)
private val EscuroBase = Color(0xFF07111F)

// Secao clara (promocoes, brinquedos, pecas, combos)
private val ClaroTopo = Color(0xFFEEF5FD)
private val ClaroMeio = Color(0xFFE7EFF9)
private val ClaroBase = Color(0xFFF7FAFF)

/** Cor do texto que fica legivel sobre a secao clara. */
val TituloSobreClaro = Color(0xFF102B4C)
val SubtituloSobreClaro = Color(0xFF60758F)

private val GradeSobreClaro = Color(0xFF004AAD).copy(alpha = 0.035f)
private val GradeSobreEscuro = Color(0xFF78A5E0).copy(alpha = 0.055f)

private val PassoGrade = 42.dp

// ============================================================
// MODIFICADORES
// ============================================================

/**
 * Fundo do hero: azul profundo abrindo pro azul da marca, com brilho
 * vermelho em cima a direita e azul embaixo a esquerda.
 */
fun Modifier.fundoHero(): Modifier = drawBehind {
    drawRect(
        Brush.linearGradient(
            colors = listOf(HeroTopo, HeroMeio, HeroBase),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
        )
    )
    brilho(VermelhoBrilho, 0.25f, 0.88f, 0.12f)
    brilho(AzulBrilho, 0.35f, 0.08f, 0.94f)
    grade(GradeSobreEscuro)
}

/**
 * Fundo das secoes escuras de contraste. No site sao "categorias",
 * "estabelecimentos" e "eventos" -- os blocos que quebram o branco.
 */
fun Modifier.fundoSecaoEscura(): Modifier = drawBehind {
    drawRect(
        Brush.linearGradient(
            colors = listOf(EscuroTopo, EscuroMeio, EscuroBase),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
        )
    )
    brilho(AzulBrilho, 0.20f, 0.06f, 0.08f)
    brilho(VermelhoBrilho, 0.14f, 0.95f, 0.82f)
    grade(GradeSobreEscuro)
}

/**
 * Fundo das secoes claras -- o cartao azulado com grade.
 *
 * @Composable porque respeita o tema escuro do aparelho: no modo noturno
 * o azul-clarinho do site cegaria, entao cai pro fundo escuro.
 */
@Composable
fun Modifier.fundoSecaoClara(): Modifier {
    val escuro = isSystemInDarkTheme()
    return if (escuro) {
        fundoSecaoEscura()
    } else {
        drawBehind {
            drawRect(
                Brush.linearGradient(
                    colors = listOf(ClaroTopo, ClaroMeio, ClaroBase),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                )
            )
            brilho(AzulBrilho, 0.13f, 0.07f, 0.07f)
            brilho(VermelhoBrilho, 0.07f, 0.94f, 0.88f)
            grade(GradeSobreClaro)
        }
    }
}

/** Cor de titulo que funciona sobre `fundoSecaoClara` nos dois temas. */
@Composable
fun corTituloSecaoClara(): Color =
    if (isSystemInDarkTheme()) Color(0xFFE6EDF7) else TituloSobreClaro

@Composable
fun corSubtituloSecaoClara(): Color =
    if (isSystemInDarkTheme()) Color(0xFFA9BBD4) else SubtituloSobreClaro

// ============================================================
// PRIMITIVAS DE DESENHO
// ============================================================

/**
 * Brilho radial suave. `x` e `y` sao fracoes do tamanho (0..1), igual
 * ao `circle at 88% 12%` do CSS.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.brilho(
    cor: Color,
    intensidade: Float,
    x: Float,
    y: Float,
) {
    drawRect(
        Brush.radialGradient(
            colors = listOf(cor.copy(alpha = intensidade), Color.Transparent),
            center = Offset(size.width * x, size.height * y),
            radius = size.maxDimension * 0.85f,
        )
    )
}

/**
 * Grade de papel milimetrado. As linhas nascem opacas no topo e somem
 * a 88% da altura -- e o `mask-image` do CSS, feito na mao porque o
 * Compose nao tem mascara de brush pronta.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.grade(cor: Color) {
    val passo = PassoGrade.toPx()
    if (passo <= 0f) return

    val alturaFade = size.height * 0.88f

    // Verticais: o degrade cuida do esmaecimento sozinho.
    val pincelVertical = Brush.verticalGradient(
        colors = listOf(cor, Color.Transparent),
        startY = 0f,
        endY = alturaFade,
    )
    var x = passo
    while (x < size.width) {
        drawLine(
            brush = pincelVertical,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f,
        )
        x += passo
    }

    // Horizontais: cada linha tem alpha proprio conforme a altura.
    var y = passo
    while (y < size.height) {
        val fracao = (1f - y / alturaFade).coerceIn(0f, 1f)
        if (fracao > 0f) {
            drawLine(
                color = cor.copy(alpha = cor.alpha * fracao),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
        }
        y += passo
    }
}

/** Raio dos cartoes de secao -- 30px no site. */
val RaioSecao: Dp = 30.dp