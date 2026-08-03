// app/src/main/java/com/example/lazer_sport_app/data/Network.kt
//
// Camada de rede inteira num arquivo só, pra você conseguir colar e
// rodar. Quando crescer, quebre nas pastas data/remote, data/local
// e di -- a estrutura já está pensada pra isso.
//
// O que tem aqui:
//   1. DTOs        -- espelham o JSON que o Django manda
//   2. ApiService  -- interface Retrofit
//   3. TokenStore  -- guarda o token no DataStore (sobrevive a reinício)
//   4. AuthInterceptor -- injeta "Authorization: Token xxx" sozinho
//   5. NetworkModule   -- monta tudo via Hilt
//   6. AuthRepository  -- o que a tela usa

package com.example.lazer_sport_app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.lazer_sport_app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
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

// ============================================================
// 1. DTOs -- os nomes batem com o JSON do serializer Django
// ============================================================

@Serializable
data class LoginRequest(
    val login: String,
    val senha: String,
)

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
    val username: String,
    val email: String,
    @SerialName("nome_completo") val nomeCompleto: String = "",
    val telefone: String = "",
    @SerialName("is_staff") val isStaff: Boolean = false,
)

@Serializable
data class AuthResponse(
    val token: String,
    val usuario: UsuarioDto,
)

@Serializable
data class CategoriaDto(
    val id: Int,
    val nome: String? = null,
    val imagem: String? = null,
)

@Serializable
data class BrinquedoDto(
    val id: Int,
    val nome: String,
    val valor: String? = null,
    val avaliacao: String? = null,
    val imagem: String? = null,
    @SerialName("exibir_na_loja") val exibirNaLoja: Boolean = true,
)

/** O DRF pagina assim: { count, next, previous, results: [...] } */
@Serializable
data class Paginado<T>(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<T> = emptyList(),
)

// ============================================================
// 2. INTERFACE RETROFIT
// ============================================================

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
    suspend fun brinquedo(@Path("id") id: Int): BrinquedoDto
}

// ============================================================
// 3. TOKEN STORE
// ============================================================

private val Context.dataStore by preferencesDataStore(name = "lazer_sport")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val contexto: Context,
) {
    private val CHAVE_TOKEN = stringPreferencesKey("token")
    private val CHAVE_NOME = stringPreferencesKey("nome")

    val token: Flow<String?> = contexto.dataStore.data
        .map { it[CHAVE_TOKEN] }

    val nome: Flow<String?> = contexto.dataStore.data
        .map { it[CHAVE_NOME] }

    suspend fun salvar(token: String, nome: String) {
        contexto.dataStore.edit {
            it[CHAVE_TOKEN] = token
            it[CHAVE_NOME] = nome
        }
    }

    suspend fun limpar() {
        contexto.dataStore.edit { it.clear() }
    }

    /** Usado pelo interceptor, que roda fora de corrotina. */
    fun tokenAgora(): String? = runBlocking { token.first() }
}

// ============================================================
// 4. INTERCEPTOR
// ============================================================

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.tokenAgora()
        val original = chain.request()

        val requisicao = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder()
                // Formato exigido pelo TokenAuthentication do DRF.
                // "Token", não "Bearer" -- errar isso dá 401 silencioso.
                .header("Authorization", "Token $token")
                .build()
        }

        return chain.proceed(requisicao)
    }
}

// ============================================================
// 5. MÓDULO HILT
// ============================================================

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun fornecerJson(): Json = Json {
        ignoreUnknownKeys = true   // API pode ganhar campos sem quebrar o app
        coerceInputValues = true   // null vira o valor padrão do data class
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun fornecerOkHttp(
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun fornecerRetrofit(
        cliente: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(cliente)
        .addConverterFactory(
            json.asConverterFactory("application/json".toMediaType())
        )
        .build()

    @Provides
    @Singleton
    fun fornecerApi(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}

// ============================================================
// 6. REPOSITÓRIO
// ============================================================

/** Resultado de qualquer operação de rede, sem exception vazando pra UI. */
sealed interface Resultado<out T> {
    data class Sucesso<T>(val dados: T) : Resultado<T>
    data class Erro(val mensagem: String) : Resultado<Nothing>
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) {
    val estaLogado: Flow<Boolean> = tokenStore.token.map { !it.isNullOrBlank() }
    val nomeUsuario: Flow<String?> = tokenStore.nome

    suspend fun entrar(login: String, senha: String): Resultado<UsuarioDto> =
        try {
            val resposta = api.login(LoginRequest(login.trim(), senha))
            tokenStore.salvar(
                resposta.token,
                resposta.usuario.nomeCompleto.ifBlank { resposta.usuario.username },
            )
            Resultado.Sucesso(resposta.usuario)
        } catch (e: Exception) {
            Resultado.Erro(traduzirErro(e))
        }

    suspend fun registrar(
        nome: String,
        email: String,
        telefone: String,
        senha: String,
    ): Resultado<UsuarioDto> = try {
        val resposta = api.registrar(
            RegistroRequest(nome.trim(), email.trim(), telefone.trim(), senha)
        )
        tokenStore.salvar(resposta.token, resposta.usuario.nomeCompleto)
        Resultado.Sucesso(resposta.usuario)
    } catch (e: Exception) {
        Resultado.Erro(traduzirErro(e))
    }

    suspend fun sair() {
        runCatching { api.logout() }   // se falhar, limpa local mesmo assim
        tokenStore.limpar()
    }

    /** Mensagem legível em português no lugar de stack trace. */
    private fun traduzirErro(e: Exception): String = when {
        e is retrofit2.HttpException && e.code() == 401 ->
            "Login ou senha inválidos."
        e is retrofit2.HttpException && e.code() == 400 ->
            "Confira os dados informados."
        e is retrofit2.HttpException && e.code() >= 500 ->
            "O servidor está fora do ar. Tente em instantes."
        e is java.net.UnknownHostException || e is java.net.ConnectException ->
            "Sem conexão. Verifique sua internet."
        e is java.net.SocketTimeoutException ->
            "A conexão demorou demais. Tente de novo."
        else -> "Algo deu errado. Tente novamente."
    }
}