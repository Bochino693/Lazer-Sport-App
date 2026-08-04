// Camada de comércio: carrinho local persistido, manutenções e pedidos.
//
// POR QUE CARRINHO LOCAL: a API v1 hoje expõe só auth/catálogo. O
// carrinho vive no DataStore e sobrevive a fechar o app; quando o
// Django ganhar /carrinho/, basta o sincronizar() abaixo funcionar --
// nenhuma tela muda.
//
// Interface Retrofit separada (ApiComercio) de propósito: não mexe no
// Network.kt que já está funcionando, e reaproveita o mesmo Retrofit.

package com.example.lazer_sport_app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.lazer_sport_app.ui.components.ItemVitrine
import com.example.lazer_sport_app.ui.components.TipoItemVitrine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URLEncoder

// ============ 1. CONTATO DA EMPRESA ============

object Contato {
    const val WHATSAPP = "5511960563135"
    const val TELEFONE_EXIBICAO = "+55 11 96056-3135"
    const val EMAIL = "comercial@lazersport.com.br"
    const val ENDERECO =
        "Rua São Roque de Minas, 104 — Jardim Peri, São Paulo — SP, CEP 02679-110"
    const val SITE = "https://www.lazersport.com.br/"

    const val MAPA =
        "https://www.google.com/maps/dir/?api=1&destination=" +
                "Rua%20S%C3%A3o%20Roque%20de%20Minas%2C%20104%20-%20Jardim%20Peri%2C" +
                "%20S%C3%A3o%20Paulo%20-%20SP%2C%2002679-110"

    /** wa.me não gosta de "+" como espaço; troco por %20. */
    fun whatsapp(mensagem: String): String {
        val texto = URLEncoder.encode(mensagem, "UTF-8").replace("+", "%20")
        return "https://wa.me/$WHATSAPP?text=$texto"
    }

    fun site(caminho: String): String = SITE + caminho.trimStart('/')
}

// ============ 2. DTOs ============

@Serializable
data class ItemCarrinhoEnvio(
    val tipo: String,
    @SerialName("item_id") val itemId: Int,
    val quantidade: Int,
)

@Serializable
data class CarrinhoEnvio(
    val itens: List<ItemCarrinhoEnvio>,
    @SerialName("tipo_envio") val tipoEnvio: String = "frete",
    val cupom: String? = null,
)

@Serializable
data class ManutencaoEnvio(
    val brinquedo: Int? = null,
    @SerialName("brinquedo_nao_listado") val brinquedoNaoListado: Boolean = false,
    @SerialName("brinquedo_descricao_livre") val brinquedoDescricaoLivre: String = "",
    val descricao: String = "",
    @SerialName("telefone_contato") val telefoneContato: String = "",
    val cep: String = "",
    val endereco: String = "",
    val numero: String = "",
    val complemento: String = "",
    val bairro: String = "",
    val cidade: String = "",
    val estado: String = "",
)

@Serializable
data class ManutencaoDto(
    val id: Int = 0,
    @SerialName("nome_equipamento") val nomeEquipamento: String = "",
    val descricao: String = "",
    val status: String = "P",
    @SerialName("status_display") val statusDisplay: String? = null,
    @SerialName("criado_em") val criadoEm: String? = null,
    val cidade: String? = null,
    val estado: String? = null,
)

@Serializable
data class ItemPedidoDto(
    val id: Int = 0,
    @SerialName("nome_item") val nomeItem: String = "",
    @SerialName("tipo_item") val tipoItem: String = "",
    val quantidade: Int = 1,
    val subtotal: String? = null,
)

@Serializable
data class PedidoDto(
    val id: Int = 0,
    val status: String = "",
    @SerialName("status_display") val statusDisplay: String? = null,
    @SerialName("tipo_envio") val tipoEnvio: String? = null,
    @SerialName("total_final") val totalFinal: String? = null,
    @SerialName("criado_em") val criadoEm: String? = null,
    val itens: List<ItemPedidoDto> = emptyList(),
)

// ============ 3. RETROFIT ============

interface ApiComercio {

    @GET("manutencoes/")
    suspend fun manutencoes(@Query("page") pagina: Int = 1): Paginado<ManutencaoDto>

    @POST("manutencoes/")
    suspend fun criarManutencao(@Body corpo: ManutencaoEnvio): ManutencaoDto

    @GET("pedidos/")
    suspend fun pedidos(@Query("page") pagina: Int = 1): Paginado<PedidoDto>

    @POST("carrinho/sincronizar/")
    suspend fun sincronizarCarrinho(@Body corpo: CarrinhoEnvio)
}

@Module
@InstallIn(SingletonComponent::class)
object ComercioModule {

    @Provides
    @Singleton
    fun fornecerApiComercio(retrofit: Retrofit): ApiComercio =
        retrofit.create(ApiComercio::class.java)
}

// ============ 4. MODELO LOCAL DO CARRINHO ============

@Serializable
data class ItemCarrinho(
    val id: Int,
    val nome: String,
    val tipo: String = "brinquedo",
    @SerialName("preco_unitario") val precoUnitario: Double = 0.0,
    val quantidade: Int = 1,
    @SerialName("imagem_url") val imagemUrl: String? = null,
) {
    val chave: String get() = "$tipo:$id"
    val subtotal: Double get() = precoUnitario * quantidade
    val precoFormatado: String get() = formatarReal(precoUnitario)
    val subtotalFormatado: String get() = formatarReal(subtotal)
}

data class EstadoCarrinho(
    val itens: List<ItemCarrinho> = emptyList(),
    val tipoEnvio: String = "frete",
    val cupom: String = "",
) {
    val quantidade: Int get() = itens.sumOf { it.quantidade }
    val totalBruto: Double get() = itens.sumOf { it.subtotal }
    val vazio: Boolean get() = itens.isEmpty()
    val totalFormatado: String get() = formatarReal(totalBruto)
}

// ============ 5. REPOSITORIO DO CARRINHO ============

private val Context.dataStoreCarrinho by preferencesDataStore(name = "carrinho_local")

@Singleton
class CarrinhoRepository @Inject constructor(
    @ApplicationContext private val contexto: Context,
    private val api: ApiComercio,
) {
    private val jsonLocal = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val CHAVE_ITENS = stringPreferencesKey("itens")
    private val CHAVE_ENVIO = stringPreferencesKey("tipo_envio")
    private val CHAVE_CUPOM = stringPreferencesKey("cupom")

    val estado: Flow<EstadoCarrinho> = contexto.dataStoreCarrinho.data.map { prefs ->
        EstadoCarrinho(
            itens = ler(prefs[CHAVE_ITENS]),
            tipoEnvio = prefs[CHAVE_ENVIO] ?: "frete",
            cupom = prefs[CHAVE_CUPOM] ?: "",
        )
    }

    val quantidade: Flow<Int> = estado.map { it.quantidade }

    suspend fun adicionar(item: ItemVitrine, quantidade: Int = 1) {
        val tipo = item.tipo.slug()
        val chave = "$tipo:${item.id}"
        gravar { atuais ->
            if (atuais.any { it.chave == chave }) {
                atuais.map {
                    if (it.chave == chave) it.copy(quantidade = it.quantidade + quantidade) else it
                }
            } else {
                atuais + ItemCarrinho(
                    id = item.id,
                    nome = item.nome,
                    tipo = tipo,
                    precoUnitario = precoParaDouble(item.preco),
                    quantidade = quantidade,
                    imagemUrl = item.imagemUrl,
                )
            }
        }
    }

    suspend fun definirQuantidade(chave: String, novaQuantidade: Int) {
        if (novaQuantidade <= 0) {
            remover(chave)
            return
        }
        gravar { atuais ->
            atuais.map { if (it.chave == chave) it.copy(quantidade = novaQuantidade) else it }
        }
    }

    suspend fun remover(chave: String) {
        gravar { atuais -> atuais.filterNot { it.chave == chave } }
    }

    suspend fun limpar() {
        contexto.dataStoreCarrinho.edit { it.remove(CHAVE_ITENS) }
    }

    suspend fun definirTipoEnvio(tipo: String) {
        contexto.dataStoreCarrinho.edit { it[CHAVE_ENVIO] = tipo }
    }

    suspend fun definirCupom(cupom: String) {
        contexto.dataStoreCarrinho.edit { it[CHAVE_CUPOM] = cupom }
    }

    /** Só funciona quando o Django ganhar /carrinho/sincronizar/. */
    suspend fun sincronizar(atual: EstadoCarrinho): Boolean = runCatching {
        api.sincronizarCarrinho(
            CarrinhoEnvio(
                itens = atual.itens.map {
                    ItemCarrinhoEnvio(it.tipo, it.id, it.quantidade)
                },
                tipoEnvio = atual.tipoEnvio,
                cupom = atual.cupom.takeIf { it.isNotBlank() },
            )
        )
        true
    }.getOrDefault(false)

    private fun ler(bruto: String?): List<ItemCarrinho> =
        runCatching { bruto?.let { jsonLocal.decodeFromString<List<ItemCarrinho>>(it) } }
            .getOrNull() ?: emptyList()

    private suspend fun gravar(bloco: (List<ItemCarrinho>) -> List<ItemCarrinho>) {
        contexto.dataStoreCarrinho.edit { prefs ->
            prefs[CHAVE_ITENS] = jsonLocal.encodeToString(bloco(ler(prefs[CHAVE_ITENS])))
        }
    }
}

// ============ 6. MANUTENCOES ============

@Singleton
class ManutencaoRepository @Inject constructor(
    private val api: ApiComercio,
) {
    suspend fun listar(): List<ManutencaoDto> =
        runCatching { api.manutencoes().results }.getOrDefault(emptyList())

    suspend fun enviar(corpo: ManutencaoEnvio): Resultado<ManutencaoDto> = try {
        Resultado.Sucesso(api.criarManutencao(corpo))
    } catch (e: Exception) {
        Resultado.Erro(
            when {
                e is retrofit2.HttpException && e.code() == 401 ->
                    "Entre na sua conta para abrir um chamado."
                e is retrofit2.HttpException && e.code() == 404 ->
                    "O envio pelo app ainda não está liberado. " +
                            "Use o WhatsApp abaixo que a gente já registra."
                e is retrofit2.HttpException && e.code() == 400 ->
                    "Confira os campos preenchidos."
                e is java.net.UnknownHostException || e is java.net.ConnectException ->
                    "Sem conexão. Verifique sua internet."
                else -> "Não foi possível enviar agora. Tente pelo WhatsApp."
            }
        )
    }
}

// ============ 7. PEDIDOS ============

@Singleton
class PedidosRepository @Inject constructor(
    private val api: ApiComercio,
) {
    suspend fun listar(): List<PedidoDto> =
        runCatching { api.pedidos().results }.getOrDefault(emptyList())
}

// ============ 8. UTILITARIOS ============

private fun TipoItemVitrine.slug(): String = when (this) {
    TipoItemVitrine.BRINQUEDO -> "brinquedo"
    TipoItemVitrine.PECA -> "pecas"
    TipoItemVitrine.COMBO -> "combo"
    TipoItemVitrine.PROMOCAO -> "promocao"
    TipoItemVitrine.ESTABELECIMENTO -> "estabelecimento"
    TipoItemVitrine.EVENTO -> "evento"
}

/** "R$ 1.250,00" -> 1250.0. Aceita também "1250.00" cru do DRF. */
internal fun precoParaDouble(texto: String?): Double {
    if (texto.isNullOrBlank()) return 0.0
    val limpo = texto.replace(Regex("[^0-9,.]"), "")
    if (limpo.isBlank()) return 0.0
    val normalizado =
        if (limpo.contains(",")) limpo.replace(".", "").replace(",", ".") else limpo
    return normalizado.toDoubleOrNull() ?: 0.0
}

internal fun formatarReal(valor: Double): String =
    formatarReal(
        BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).toPlainString()
    )