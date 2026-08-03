// app/src/main/java/com/example/lazer_sport_app/ui/menu/MenuViewModel.kt
//
// Busca o conteudo real da API e entrega pro MenuScreen.
//
// TRES DECISOES QUE IMPORTAM AQUI:
//
// 1) CARREGAMENTO PARALELO. Categorias, brinquedos e pecas sao tres
//    chamadas independentes. Em sequencia seriam ~3x o tempo; com
//    async/await elas saem juntas.
//
// 2) FALHA PARCIAL NAO DERRUBA A TELA. Se `pecas` falhar mas
//    `brinquedos` responder, o cliente ve os brinquedos. Cada secao
//    tem seu proprio try -- por isso `runCatching` item a item, nunca
//    um try gigante em volta de tudo.
//
// 3) FALLBACK VISIVEL. Com a API fora do ar, a tela abre com os dados
//    de demonstracao e uma mensagem discreta em vez de tela branca.
//    Some assim que o backend responder.
//
// As secoes "promocoes" e "combos" ainda nao tem endpoint no Django.
// Por enquanto derivo promocoes dos brinquedos e deixo combos vazio --
// a secao simplesmente nao aparece (o MenuScreen ja testa isEmpty).

package com.example.lazer_sport_app.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lazer_sport_app.data.ApiService
import com.example.lazer_sport_app.data.BrinquedoDto
import com.example.lazer_sport_app.data.CategoriaDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.getOrDefault

// ============================================================
// ESTADO DA TELA
// ============================================================

data class EstadoMenu(
    val carregando: Boolean = true,
    val conteudo: ConteudoMenu = ConteudoMenu(),
    /** Preenchido quando NADA veio da rede -- a tela mostra demo. */
    val avisoOffline: String? = null,
)

// ============================================================
// REPOSITORIO
// ============================================================

@Singleton
class CatalogoRepository @Inject constructor(
    private val api: ApiService,
) {

    /**
     * Dispara as chamadas em paralelo. Cada uma falha por conta
     * propria: o que responder, aparece.
     */
    suspend fun carregarMenu(): ConteudoMenu = coroutineScope {

        val categoriasAsync = async {
            runCatching { api.categorias() }.getOrDefault(emptyList())
        }
        val brinquedosAsync = async {
            runCatching { api.brinquedos(pagina = 1).results }
                .getOrDefault(emptyList())
        }
        val pecasAsync = async {
            runCatching { api.pecas(pagina = 1).results }
                .getOrDefault(emptyList())
        }

        val categorias = categoriasAsync.await()
        val brinquedos = brinquedosAsync.await()
        val pecas = pecasAsync.await()

        // So entra na vitrine o que o Django marcou pra exibir.
        val visiveis = brinquedos.filter { it.exibirNaLoja }

        ConteudoMenu(
            categorias = categorias.map { it.paraVitrine() },
            // Sem endpoint de promocoes ainda: uso os mais bem avaliados
            // como destaque secundario. Troque quando a API existir.
            promocoes = visiveis
                .sortedByDescending { it.avaliacao?.replace(",", ".")?.toFloatOrNull() ?: 0f }
                .take(8)
                .map { it.paraVitrine(selo = "DESTAQUE") },
            destaques = visiveis.take(12).map { it.paraVitrine() },
            pecas = pecas.map { it.paraVitrine() },
            combos = emptyList(),
            estabelecimentos = emptyList(),
            eventos = emptyList(),
        )
    }
}

// ---- Conversores DTO -> modelo de tela ----------------------
// Ficam aqui de proposito: se o serializer do Django mudar, muda um
// lugar so e o Compose nem fica sabendo.

private fun CategoriaDto.paraVitrine() = CategoriaVitrine(
    id = id,
    nome = nome.orEmpty().ifBlank { "Categoria" },
    imagemUrl = imagem,
)

private fun BrinquedoDto.paraVitrine(selo: String? = null) = ItemVitrine(
    id = id,
    nome = nome,
    preco = valor?.let { formatarReal(it) },
    imagemUrl = imagem,
    selo = selo,
    avaliacao = avaliacao?.takeIf { it.isNotBlank() },
)

/** O DRF manda DecimalField como string ("1250.00"). */
private fun formatarReal(bruto: String): String {
    val numero = bruto.replace(",", ".").toDoubleOrNull() ?: return bruto
    val inteiro = numero.toLong()
    val centavos = ((numero - inteiro) * 100).toInt()
    val comPontos = inteiro.toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
    return "R$ %s,%02d".format(comPontos, centavos)
}

// ============================================================
// VIEWMODEL
// ============================================================

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val repositorio: CatalogoRepository,
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoMenu())
    val estado: StateFlow<EstadoMenu> = _estado.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _estado.update { it.copy(carregando = true, avisoOffline = null) }

            val vindo = runCatching { repositorio.carregarMenu() }
                .getOrDefault(ConteudoMenu())

            val vazio = vindo.categorias.isEmpty() && vindo.destaques.isEmpty()

            _estado.update {
                it.copy(
                    carregando = false,
                    conteudo = if (vazio) dadosDemo() else vindo,
                    avisoOffline = if (vazio) {
                        "Não foi possível carregar o catálogo agora. " +
                                "Mostrando conteúdo de exemplo."
                    } else {
                        null
                    },
                )
            }
        }
    }
}