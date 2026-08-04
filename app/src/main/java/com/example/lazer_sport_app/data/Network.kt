// Camada de rede inteira: DTOs, Retrofit, Hilt, token e repositorios.
//
// NOVIDADES desta versao:
//   - endpoints de estabelecimentos, eventos, combos e promocoes
//   - detalhe do brinquedo com descricao, voltagem e medidas
//   - CatalogoRepository migrou pra ca (saiu do MenuViewModel) porque
//     todas as telas de lista precisam dele
//   - FonteLista: uma lista generica serve seis telas
//
// TOLERANCIA A API INCOMPLETA: cada chamada e envolvida em
// runCatching. Endpoint que ainda nao existe no Django devolve lista
// vazia em vez de derrubar a tela.

package com.example.lazer_sport_app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.lazer_sport_app.BuildConfig
import com.example.lazer_sport_app.ui.components.CategoriaVitrine
import com.example.lazer_sport_app.ui.components.ConteudoMenu
import com.example.lazer_sport_app.ui.components.ItemVitrine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ============ 1. DTOs ============

@Serializable
data class LoginRequest(val login: String, val senha: String)

@Serializable
data class RegistroRequest(
    @SerialName("nome_completo") val nomeCompleto: String,
    val email: String,
    val telefone: String,
    val senha: String,
)

@Serializable
data class UsuarioDto(
    val id: Int,
    val username: String = "",
    val email: String = "",
    @SerialName("nome_completo") val nomeCompleto: String = "",
    val telefone: String = "",
    @SerialName("is_staff") val isStaff: Boolean = false,
)

@Serializable
data class AuthResponse(val token: String, val usuario: UsuarioDto)

@Serializable
data class CategoriaDto(
    val id: Int,
    val nome: String? = null,
    val imagem: String? = null,
)

@Serializable
data class BrinquedoDto(
    val id: Int,
    val nome: String = "",
    val valor: String? = null,
    val avaliacao: String? = null,
    val imagem: String? = null,
    @SerialName("exibir_na_loja") val exibirNaLoja: Boolean = true,
)

/** Detalhe: campos extras sao opcionais pra funcionar com o serializer
 *  antigo (que so manda os basicos) e com o novo. */
@Serializable
data class BrinquedoDetalheDto(
    val id: Int,
    val nome: String = "",
    val descricao: String? = null,
    val valor: String? = null,
    val avaliacao: String? = null,
    val imagem: String? = null,
    val voltz: String? = null,
    @SerialName("altura_m") val alturaM: String? = null,
    @SerialName("largura_m") val larguraM: String? = null,
    @SerialName("profundidade_m") val profundidadeM: String? = null,
    val categorias: List<CategoriaDto> = emptyList(),
)

@Serializable
data class EstabelecimentoDto(
    val id: Int,
    val nome: String = "",
    val imagem: String? = null,
)

@Serializable
data class ImagemEventoDto(val imagem: String? = null, val legenda: String? = null)

@Serializable
data class EventoDto(
    val id: Int,
    val titulo: String = "",
    val descricao: String = "",
    val imagens: List<ImagemEventoDto> = emptyList(),
)

@Serializable
data class ComboDto(
    val id: Int,
    val descricao: String = "",
    val imagem: String? = null,
    val valor: String? = null,
)

@Serializable
data class PromocaoDto(
    val id: Int,
    val descricao: String = "",
    val imagem: String? = null,
    val preco: String? = null,
    @SerialName("brinquedo_id") val brinquedoId: Int? = null,
)

/** O DRF pagina assim: { count, next, previous, results: [...] } */
@Serializable
data class Paginado<T>(
    val count: Int = 0,
    val next: String? = null,
    val previous: String? = null,
    val results: List<T> = emptyList(),
)

// ============ 2. INTERFACE RETROFIT ============

interface ApiService {

    @POST("auth/login/")
    suspend fun login(@Body corpo: LoginRequest): AuthResponse

    @POST("auth/registro/")
    suspend fun registrar(@Body corpo: RegistroRequest): AuthResponse

    @POST("auth/logout/")
    suspend fun logout()

    @GET("auth/perfil/")
    suspend fun perfil(): UsuarioDto

    @GET("categorias/")
    suspend fun categorias(): List<CategoriaDto>

    @GET("brinquedos/")
    suspend fun brinquedos(
        @Query("page") pagina: Int = 1,
        @Query("categoria") categoria: Int? = null,
        @Query("busca") busca: String? = null,
    ): Paginado<BrinquedoDto>

    @GET("brinquedos/{id}/")
    suspend fun brinquedo(@Path("id") id: Int): BrinquedoDetalheDto

    @GET("pecas/")
    suspend fun pecas(
        @Query("page") pagina: Int = 1,
        @Query("busca") busca: String? = null,
    ): Paginado<BrinquedoDto>

    @GET("estabelecimentos/")
    suspend fun estabelecimentos(
        @Query("page") pagina: Int = 1,
    ): Paginado<EstabelecimentoDto>

    @GET("eventos/")
    suspend fun eventos(@Query("page") pagina: Int = 1): Paginado<EventoDto>

    @GET("combos/")
    suspend fun combos(@Query("page") pagina: Int = 1): Paginado<ComboDto>

    @GET("promocoes/")
    suspend fun promocoes(@Query("page") pagina: Int = 1): Paginado<PromocaoDto>
}

// ============ 3. TOKEN STORE ============

private val Context.dataStore by preferencesDataStore(name = "lazer_sport")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val contexto: Context,
) {
    private val CHAVE_TOKEN = stringPreferencesKey("token")
    private val CHAVE_NOME = stringPreferencesKey("nome")
    private val CHAVE_EMAIL = stringPreferencesKey("email")
    private val CHAVE_VISITANTE = booleanPreferencesKey("visitante")

    val token: Flow<String?> = contexto.dataStore.data.map { it[CHAVE_TOKEN] }
    val nome: Flow<String?> = contexto.dataStore.data.map { it[CHAVE_NOME] }
    val email: Flow<String?> = contexto.dataStore.data.map { it[CHAVE_EMAIL] }

    /** Escolheu "continuar sem login". Sobrevive a fechar o app. */
    val visitante: Flow<Boolean> = contexto.dataStore.data
        .map { it[CHAVE_VISITANTE] ?: false }

    suspend fun salvar(token: String, nome: String, email: String) {
        contexto.dataStore.edit {
            it[CHAVE_TOKEN] = token
            it[CHAVE_NOME] = nome
            it[CHAVE_EMAIL] = email
            it[CHAVE_VISITANTE] = false
        }
    }

    suspend fun marcarVisitante() {
        contexto.dataStore.edit { it[CHAVE_VISITANTE] = true }
    }

    suspend fun limpar() {
        contexto.dataStore.edit { it.clear() }
    }

    fun tokenAgora(): String? = runBlocking { token.first() }
}

// ============ 4. INTERCEPTOR ============

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.tokenAgora()
        val original = chain.request()

        val requisicao = if (token.isNullOrBlank()) {
            original
        } else {
            // "Token", nao "Bearer" -- e o formato do TokenAuthentication
            // do DRF. Errar isso da 401 silencioso.
            original.newBuilder()
                .header("Authorization", "Token $token")
                .build()
        }

        return chain.proceed(requisicao)
    }
}

// ============ 5. MODULO HILT ============

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun fornecerJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun fornecerOkHttp(authInterceptor: AuthInterceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun fornecerRetrofit(cliente: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(cliente)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun fornecerApi(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}

// ============ 6. RESULTADO ============

sealed interface Resultado<out T> {
    data class Sucesso<T>(val dados: T) : Resultado<T>
    data class Erro(val mensagem: String) : Resultado<Nothing>
}

// ============ 7. AUTENTICACAO ============

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) {
    val estaLogado: Flow<Boolean> = tokenStore.token.map { !it.isNullOrBlank() }
    val nomeUsuario: Flow<String?> = tokenStore.nome
    val emailUsuario: Flow<String?> = tokenStore.email
    val modoVisitante: Flow<Boolean> = tokenStore.visitante

    suspend fun continuarSemLogin() = tokenStore.marcarVisitante()

    suspend fun entrar(login: String, senha: String): Resultado<UsuarioDto> =
        try {
            val r = api.login(LoginRequest(login.trim(), senha))
            tokenStore.salvar(
                r.token,
                r.usuario.nomeCompleto.ifBlank { r.usuario.username },
                r.usuario.email,
            )
            Resultado.Sucesso(r.usuario)
        } catch (e: Exception) {
            Resultado.Erro(traduzirErro(e))
        }

    suspend fun registrar(
        nome: String,
        email: String,
        telefone: String,
        senha: String,
    ): Resultado<UsuarioDto> = try {
        val r = api.registrar(
            RegistroRequest(nome.trim(), email.trim(), telefone.trim(), senha)
        )
        tokenStore.salvar(r.token, r.usuario.nomeCompleto.ifBlank { nome }, r.usuario.email)
        Resultado.Sucesso(r.usuario)
    } catch (e: Exception) {
        Resultado.Erro(traduzirErro(e))
    }

    suspend fun sair() {
        runCatching { api.logout() }   // se falhar, limpa local mesmo assim
        tokenStore.limpar()
    }

    private fun traduzirErro(e: Exception): String = when {
        e is retrofit2.HttpException && e.code() == 401 -> "Login ou senha inválidos."
        e is retrofit2.HttpException && e.code() == 400 -> "Confira os dados informados."
        e is retrofit2.HttpException && e.code() >= 500 ->
            "O servidor está fora do ar. Tente em instantes."
        e is java.net.UnknownHostException || e is java.net.ConnectException ->
            "Sem conexão. Verifique sua internet."
        e is java.net.SocketTimeoutException -> "A conexão demorou demais. Tente de novo."
        else -> "Algo deu errado. Tente novamente."
    }
}

// ============ 8. CATALOGO ============

/** Uma lista generica atende seis telas. */
enum class FonteLista(val titulo: String, val subtitulo: String, val largo: Boolean) {
    BRINQUEDOS("Brinquedos", "Catálogo completo", false),
    PECAS("Peças de Reposição", "Componentes originais", false),
    PROMOCOES("Promoções", "Condições por tempo limitado", false),
    COMBOS("Combos", "Pacotes com melhor custo-benefício", false),
    ESTABELECIMENTOS("Estabelecimentos", "Onde nossos brinquedos estão", true),
    EVENTOS("Eventos", "Um pouco do que já montamos", true),
}

data class PaginaVitrine(
    val itens: List<ItemVitrine> = emptyList(),
    val temMais: Boolean = false,
)

@Singleton
class CatalogoRepository @Inject constructor(
    private val api: ApiService,
) {

    /** Home: tudo em paralelo, cada secao falhando por conta propria. */
    suspend fun carregarMenu(): ConteudoMenu = coroutineScope {
        val categoriasA = async { runCatching { api.categorias() }.getOrDefault(emptyList()) }
        val brinquedosA = async {
            runCatching { api.brinquedos(1).results }.getOrDefault(emptyList())
        }
        val pecasA = async { runCatching { api.pecas(1).results }.getOrDefault(emptyList()) }
        val estabA = async {
            runCatching { api.estabelecimentos(1).results }.getOrDefault(emptyList())
        }
        val eventosA = async { runCatching { api.eventos(1).results }.getOrDefault(emptyList()) }
        val combosA = async { runCatching { api.combos(1).results }.getOrDefault(emptyList()) }
        val promosA = async { runCatching { api.promocoes(1).results }.getOrDefault(emptyList()) }

        val brinquedos = brinquedosA.await().filter { it.exibirNaLoja }
        val promocoesApi = promosA.await()

        ConteudoMenu(
            categorias = categoriasA.await().map { it.paraVitrine() },
            // Sem endpoint de promocoes ainda? Cai nos mais bem avaliados.
            promocoes = if (promocoesApi.isNotEmpty()) {
                promocoesApi.map { it.paraVitrine() }
            } else {
                brinquedos
                    .sortedByDescending { it.avaliacao.paraFloat() }
                    .take(8)
                    .map { it.paraVitrine(selo = "DESTAQUE") }
            },
            destaques = brinquedos.take(12).map { it.paraVitrine() },
            pecas = pecasA.await().map { it.paraVitrine() },
            combos = combosA.await().map { it.paraVitrine() },
            estabelecimentos = estabA.await().map { it.paraVitrine() },
            eventos = eventosA.await().map { it.paraVitrine() },
        )
    }

    /** Lista paginada de qualquer fonte. */
    suspend fun listar(
        fonte: FonteLista,
        pagina: Int,
        busca: String?,
        filtroCategoria: Int,
    ): PaginaVitrine = runCatching {
        val termo = busca?.trim()?.takeIf { it.isNotBlank() }

        when (fonte) {
            FonteLista.BRINQUEDOS -> {
                val p = api.brinquedos(
                    pagina = pagina,
                    categoria = filtroCategoria.takeIf { it > 0 },
                    busca = termo,
                )
                PaginaVitrine(
                    p.results.filter { it.exibirNaLoja }.map { it.paraVitrine() },
                    p.next != null,
                )
            }

            FonteLista.PECAS -> {
                val p = api.pecas(pagina = pagina, busca = termo)
                PaginaVitrine(p.results.map { it.paraVitrine() }, p.next != null)
            }

            // As de baixo ainda nao filtram no servidor: filtro na mao.
            FonteLista.PROMOCOES -> {
                val p = api.promocoes(pagina)
                PaginaVitrine(p.results.map { it.paraVitrine() }.filtrar(termo), p.next != null)
            }

            FonteLista.COMBOS -> {
                val p = api.combos(pagina)
                PaginaVitrine(p.results.map { it.paraVitrine() }.filtrar(termo), p.next != null)
            }

            FonteLista.ESTABELECIMENTOS -> {
                val p = api.estabelecimentos(pagina)
                PaginaVitrine(p.results.map { it.paraVitrine() }.filtrar(termo), p.next != null)
            }

            FonteLista.EVENTOS -> {
                val p = api.eventos(pagina)
                PaginaVitrine(p.results.map { it.paraVitrine() }.filtrar(termo), p.next != null)
            }
        }
    }.getOrElse { PaginaVitrine() }

    suspend fun categorias(): List<CategoriaVitrine> =
        runCatching { api.categorias().map { it.paraVitrine() } }.getOrDefault(emptyList())

    suspend fun detalhe(id: Int): Resultado<BrinquedoDetalheDto> = try {
        Resultado.Sucesso(api.brinquedo(id))
    } catch (e: Exception) {
        Resultado.Erro("Não foi possível carregar este item agora.")
    }
}

// ============ 9. CONVERSORES ============
// Ficam aqui de proposito: se o serializer do Django mudar, muda um
// lugar so e o Compose nem fica sabendo.

private fun List<ItemVitrine>.filtrar(termo: String?): List<ItemVitrine> =
    if (termo == null) this else filter { it.nome.contains(termo, ignoreCase = true) }

private fun String?.paraFloat(): Float =
    this?.replace(",", ".")?.toFloatOrNull() ?: 0f

internal fun CategoriaDto.paraVitrine() = CategoriaVitrine(
    id = id,
    nome = nome.orEmpty().ifBlank { "Categoria" },
    imagemUrl = imagem,
)

internal fun BrinquedoDto.paraVitrine(selo: String? = null) = ItemVitrine(
    id = id,
    nome = nome,
    preco = valor?.let { formatarReal(it) },
    imagemUrl = imagem,
    selo = selo,
    avaliacao = avaliacao?.takeIf { it.isNotBlank() },
)

internal fun EstabelecimentoDto.paraVitrine() = ItemVitrine(
    id = id,
    nome = nome,
    imagemUrl = imagem,
)

internal fun EventoDto.paraVitrine() = ItemVitrine(
    id = id,
    nome = titulo,
    imagemUrl = imagens.firstOrNull()?.imagem,
    descricao = descricao.takeIf { it.isNotBlank() },
)

internal fun ComboDto.paraVitrine() = ItemVitrine(
    id = id,
    nome = descricao,
    preco = valor?.let { formatarReal(it) },
    imagemUrl = imagem,
    selo = "COMBO",
)

internal fun PromocaoDto.paraVitrine() = ItemVitrine(
    id = brinquedoId ?: id,
    nome = descricao,
    preco = preco?.let { formatarReal(it) },
    imagemUrl = imagem,
    selo = "OFERTA",
)

/** O DRF manda DecimalField como string ("1250.00"). */
internal fun formatarReal(bruto: String): String {
    val numero = bruto.replace(",", ".").toDoubleOrNull() ?: return bruto
    val inteiro = numero.toLong()
    val centavos = ((numero - inteiro) * 100).toInt()
    val comPontos = inteiro.toString().reversed().chunked(3).joinToString(".").reversed()
    return "R$ %s,%02d".format(comPontos, centavos)
}