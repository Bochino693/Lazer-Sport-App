package com.example.lazer_sport_app.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lazer_sport_app.data.CarrinhoRepository
import com.example.lazer_sport_app.data.CatalogoRepository
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

            runCatching {
                repositorio.carregarMenu()
            }.onSuccess { conteudoRecebido ->
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
                        conteudo = conteudoRecebido,
                        avisoOffline = if (semConteudo) {
                            "A API não retornou itens. Verifique a conexão e tente novamente."
                        } else {
                            null
                        },
                    )
                }
            }.onFailure {
                _estado.update {
                    it.copy(
                        carregando = false,
                        conteudo = ConteudoMenu(),
                        avisoOffline =
                            "Não foi possível carregar os dados reais da Lazer & Sport.",
                    )
                }
            }
        }
    }

    fun adicionarAoCarrinho(item: ItemVitrine) {
        if (!item.disponivelParaCompra) return
        if (item.preco.isNullOrBlank()) return

        viewModelScope.launch {
            carrinho.adicionar(item)
        }
    }
}
