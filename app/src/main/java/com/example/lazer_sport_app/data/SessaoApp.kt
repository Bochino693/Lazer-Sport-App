// Estado da API + login social por deep link + validações.
//
// POR QUE EXISTE: o app precisava adivinhar, por 404, o que já estava
// no ar. Agora ele pergunta uma vez em /status/ e sabe exatamente
// quais seções existem -- as que não existem somem da interface em vez
// de aparecerem vazias sem explicação.

package com.example.lazer_sport_app.data

import android.net.Uri
import com.example.lazer_sport_app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import retrofit2.Retrofit
import retrofit2.http.GET

// ============ 1. DTO E ENDPOINT ============

@Serializable
data class StatusDto(
    val ok: Boolean = false,
    val versao: String = "0",
    val recursos: List<String> = emptyList(),
)

interface ApiStatus {
    @GET("status/")
    suspend fun status(): StatusDto
}

@Module
@InstallIn(SingletonComponent::class)
object StatusModule {

    @Provides
    @Singleton
    fun fornecerApiStatus(retrofit: Retrofit): ApiStatus =
        retrofit.create(ApiStatus::class.java)
}

// ============ 2. ESTADO ============

enum class SaudeApi {
    VERIFICANDO,

    /** Respondeu e tem tudo que o app usa. */
    COMPLETA,

    /** Respondeu, mas faltam seções. O app esconde o que não existe. */
    PARCIAL,

    /** Não respondeu. Modo offline com aviso honesto. */
    FORA,
}

data class EstadoApi(
    val saude: SaudeApi = SaudeApi.VERIFICANDO,
    val versao: String = "",
    val recursos: Set<String> = emptySet(),
    val detalhe: String = "",
) {
    fun tem(recurso: String): Boolean =
        saude == SaudeApi.FORA || recursos.isEmpty() || recurso in recursos

    val podeLogar: Boolean get() = saude != SaudeApi.FORA && tem("auth")
    val temGoogle: Boolean get() = saude != SaudeApi.FORA && tem("auth_google")
    val temManutencoes: Boolean get() = saude != SaudeApi.FORA && tem("manutencoes")
    val temPedidos: Boolean get() = saude != SaudeApi.FORA && tem("pedidos")
}

private val RECURSOS_ESPERADOS = setOf(
    "auth", "categorias", "brinquedos", "pecas",
    "estabelecimentos", "eventos", "combos", "promocoes",
    "manutencoes", "pedidos",
)

// ============ 3. REPOSITÓRIO ============

@Singleton
class StatusRepository @Inject constructor(
    private val api: ApiStatus,
) {
    private val _estado = MutableStateFlow(EstadoApi())
    val estado: StateFlow<EstadoApi> = _estado.asStateFlow()

    suspend fun verificar(): EstadoApi {
        _estado.value = EstadoApi(saude = SaudeApi.VERIFICANDO)

        val novo = runCatching { api.status() }.fold(
            onSuccess = { s ->
                val faltando = RECURSOS_ESPERADOS - s.recursos.toSet()
                EstadoApi(
                    saude = if (faltando.isEmpty()) SaudeApi.COMPLETA else SaudeApi.PARCIAL,
                    versao = s.versao,
                    recursos = s.recursos.toSet(),
                    detalhe = if (faltando.isEmpty()) {
                        "Catálogo sincronizado"
                    } else {
                        "Algumas seções ainda estão sendo publicadas"
                    },
                )
            },
            onFailure = { erro ->
                EstadoApi(
                    saude = SaudeApi.FORA,
                    detalhe = when (erro) {
                        is java.net.UnknownHostException,
                        is java.net.ConnectException,
                            -> "Sem conexão com o servidor"
                        is java.net.SocketTimeoutException -> "O servidor demorou a responder"
                        else -> "Não conseguimos falar com o servidor"
                    },
                )
            },
        )

        _estado.value = novo
        return novo
    }
}

// ============ 4. LOGIN SOCIAL POR DEEP LINK ============

/** Resultado do retorno do navegador. */
sealed interface RetornoLogin {
    data class Sucesso(val nome: String) : RetornoLogin
    data class Falha(val mensagem: String) : RetornoLogin
    data object Ignorado : RetornoLogin
}

@Singleton
class LoginSocialRepository @Inject constructor(
    private val tokenStore: TokenStore,
) {
    /** URL que o app abre no navegador. */
    fun urlEntrada(provedor: String): String {
        val base = BuildConfig.BASE_URL.trimEnd('/')
        return "$base/auth/app/entrar/?provedor=$provedor"
    }

    /**
     * Recebe lazersport://auth?token=...&nome=...&email=...
     * ou lazersport://auth?erro=...
     */
    suspend fun processar(uri: Uri?): RetornoLogin {
        if (uri == null || uri.scheme != "lazersport" || uri.host != "auth") {
            return RetornoLogin.Ignorado
        }

        uri.getQueryParameter("erro")?.let { erro ->
            return RetornoLogin.Falha(
                when (erro) {
                    "nao_autenticado" -> "O login foi cancelado antes de terminar."
                    "conta_desativada" -> "Esta conta está desativada. Fale com a gente."
                    "provedor_invalido" -> "Esse tipo de login não está disponível."
                    else -> "Não foi possível concluir o login."
                }
            )
        }

        val token = uri.getQueryParameter("token")?.trim().orEmpty()
        if (token.isBlank()) {
            return RetornoLogin.Falha("O servidor não devolveu o acesso. Tente de novo.")
        }

        val nome = uri.getQueryParameter("nome")?.trim().orEmpty()
        val email = uri.getQueryParameter("email")?.trim().orEmpty()

        tokenStore.salvar(token, nome.ifBlank { "Cliente" }, email)
        return RetornoLogin.Sucesso(nome.ifBlank { "Cliente" })
    }
}