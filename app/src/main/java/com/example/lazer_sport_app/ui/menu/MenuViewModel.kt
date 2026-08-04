package com.example.lazer_sport_app.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lazer_sport_app.data.CarrinhoRepository
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
    val avisoOffline: String? = null,
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val repositorio: CatalogoRepository,
    private val carrinho: CarrinhoRepository,
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoMenu())
    val estado: StateFlow<EstadoMenu> = _estado.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _estado.update {
                it.copy(
                    carregando = true,
                    avisoOffline = null,
                )
            }

            val resultado = runCatching {
                repositorio.carregarMenu()
            }

            val conteudoRecebido = resultado.getOrDefault(ConteudoMenu())

            val semConteudo =
                conteudoRecebido.categorias.isEmpty() &&
                        conteudoRecebido.promocoes.isEmpty() &&
                        conteudoRecebido.destaques.isEmpty() &&
                        conteudoRecebido.pecas.isEmpty() &&
                        conteudoRecebido.combos.isEmpty() &&
                        conteudoRecebido.estabelecimentos.isEmpty() &&
                        conteudoRecebido.eventos.isEmpty()

            _estado.update {
                it.copy(
                    carregando = false,
                    conteudo = if (semConteudo) {
                        dadosDemonstracao()
                    } else {
                        conteudoRecebido
                    },
                    avisoOffline = if (semConteudo) {
                        "Não foi possível atualizar o catálogo. " +
                                "Mostrando conteúdo temporário."
                    } else {
                        null
                    },
                )
            }
        }
    }

    fun adicionarAoCarrinho(item: ItemVitrine) {
        if (item.demonstracao) return
        if (!item.disponivelParaCompra) return
        if (item.preco.isNullOrBlank()) return

        viewModelScope.launch {
            carrinho.adicionar(item)
        }
    }
}

private fun dadosDemonstracao(): ConteudoMenu {
    fun criarItens(
        prefixo: String,
        quantidade: Int,
        base: Int,
    ): List<ItemVitrine> {
        return (1..quantidade).map { indice ->
            ItemVitrine(
                id = base + indice,
                nome = "$prefixo $indice",
                demonstracao = true,
            )
        }
    }

    return ConteudoMenu(
        categorias = listOf(
            "Arcades",
            "Mesas",
            "Simuladores",
            "Kids",
            "Competitivos",
            "Parques",
        ).mapIndexed { indice, nome ->
            CategoriaVitrine(
                id = indice + 1,
                nome = nome,
            )
        },
        promocoes = criarItens(
            prefixo = "Promoção",
            quantidade = 5,
            base = 100,
        ),
        destaques = criarItens(
            prefixo = "Brinquedo",
            quantidade = 6,
            base = 200,
        ),
        pecas = criarItens(
            prefixo = "Peça de reposição",
            quantidade = 5,
            base = 300,
        ),
        combos = criarItens(
            prefixo = "Combo",
            quantidade = 4,
            base = 400,
        ),
        estabelecimentos = criarItens(
            prefixo = "Estabelecimento parceiro",
            quantidade = 4,
            base = 500,
        ),
        eventos = criarItens(
            prefixo = "Evento realizado",
            quantidade = 4,
            base = 600,
        ),
    )
}