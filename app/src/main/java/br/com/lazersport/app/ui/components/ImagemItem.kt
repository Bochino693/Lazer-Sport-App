// Imagem de produto com a marca no lugar do vazio.
//
// O catalogo tem item sem foto, e foto que falha ao baixar. Sem tratar,
// o espaco ficava em branco ou com o icone generico do sistema -- o
// androide -- dentro de um card da Lazer & Sport. Aqui o lugar da foto
// que falta e ocupado pela propria marca: continua sendo um card da loja,
// nao um erro.

package br.com.lazersport.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.lazersport.app.R
import coil3.compose.AsyncImage

/**
 * Foto do item, com o logotipo da Lazer & Sport enquanto carrega, quando
 * a URL vem vazia e quando o download falha.
 *
 * @param recuoDaMarca folga em volta do logotipo quando ele aparece no
 *   lugar da foto. O logotipo e largo; sem recuo ele encosta na borda do
 *   card e parece cortado.
 */
@Composable
fun ImagemItem(
    url: String?,
    descricao: String?,
    modifier: Modifier = Modifier,
    escala: ContentScale = ContentScale.Fit,
    recuoDaMarca: Dp = 18.dp,
) {
    val marca = painterResource(R.drawable.ls_logo_completa)

    // Sem URL nao ha o que pedir a rede: mostra a marca direto, sem passar
    // pelo Coil e sem o piscar de um carregamento que nunca vai acontecer.
    if (url.isNullOrBlank()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Image(
                painter = marca,
                contentDescription = descricao,
                contentScale = ContentScale.Fit,
                alpha = 0.55f,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(recuoDaMarca),
            )
        }
        return
    }

    AsyncImage(
        model = url,
        contentDescription = descricao,
        contentScale = escala,
        placeholder = marca,
        error = marca,
        fallback = marca,
        modifier = modifier,
    )
}
