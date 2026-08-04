// Busca o conteudo da home e entrega pro MenuScreen.
//
// O CatalogoRepository saiu daqui e foi pro data/Network.kt: agora
// catalogo, pecas, eventos e estabelecimentos usam o mesmo repositorio,
// e nao fazia sentido ele morar dentro do ViewModel de uma tela.
//
// FALLBACK VISIVEL: com a API fora do ar a tela abre com dados de
// demonstracao e um aviso, em vez de tela vazia sem explicacao.

package com.example.lazer_sport_app.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lazer_sport_app.data.CatalogoRepository
import com.example.lazer_sport_app.ui.components.CategoriaVitrine
import com.example.lazer_sport_app.ui.components.ConteudoMenu
import com.example.lazer_sport_app.ui.components.ItemVitrine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoMenu(
    val carregando: Boolean = true,
    val conteudo: ConteudoMenu = ConteudoMenu(),
    /** Preenchido quando NADA veio da rede -- a tela mostra demo. */
    val avisoOffline: String? = null,
)

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

/** Conteudo de demonstracao usado quando a API nao responde. */
fun dadosDemo(): ConteudoMenu {
    fun itens(prefixo: String, quantidade: Int, base: Int) =
        (1..quantidade).map { indice ->
            ItemVitrine(
                id = base + indice,
                nome = "$prefixo $indice",
                preco = "R$ ${(indice * 137) + 290},00",
                avaliacao = "4,${5 + (indice % 5)}",
                selo = if (indice == 1) "NOVO" else null,
            )
        }

    return ConteudoMenu(
        categorias = listOf(
            "Infláveis", "Arcades", "Mesas", "Simuladores", "Kids", "Radicais",
        ).mapIndexed { indice, nome -> CategoriaVitrine(indice + 1, nome) },
        promocoes = itens("Promoção", 5, 100),
        destaques = itens("Brinquedo", 6, 200),
        pecas = itens("Peça", 5, 300),
        combos = itens("Combo", 4, 400),
        estabelecimentos = (1..4).map {
            ItemVitrine(500 + it, "Estabelecimento parceiro $it")
        },
        eventos = (1..4).map { ItemVitrine(600 + it, "Evento realizado $it") },
    )
}