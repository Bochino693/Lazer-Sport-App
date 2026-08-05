package com.example.lazer_sport_app.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lazer_sport_app.data.AuthRepository
import com.example.lazer_sport_app.data.Resultado
import com.example.lazer_sport_app.ui.components.BotaoPrincipal
import com.example.lazer_sport_app.ui.components.BotaoVidro
import com.example.lazer_sport_app.ui.components.CampoLazer
import com.example.lazer_sport_app.ui.components.Kicker
import com.example.lazer_sport_app.ui.menu.PainelMarcaEntrada
import com.example.lazer_sport_app.ui.menu.fundoHero
import com.example.lazer_sport_app.ui.menu.vidro
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.RaioCard
import com.example.lazer_sport_app.ui.theme.RosaMarca
import com.example.lazer_sport_app.ui.theme.TextoForte
import com.example.lazer_sport_app.ui.theme.TextoMedio
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val login: String = "",
    val senha: String = "",
    val carregando: Boolean = false,
    val erro: String? = null,
    val sucesso: Boolean = false,
) {
    val podeEnviar: Boolean
        get() =
            login.isNotBlank() &&
                    senha.isNotBlank() &&
                    !carregando
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repositorio: AuthRepository,
) : ViewModel() {

    private val _estado =
        MutableStateFlow(LoginUiState())

    val estado: StateFlow<LoginUiState> =
        _estado.asStateFlow()

    fun aoMudarLogin(valor: String) {
        _estado.update {
            it.copy(
                login = valor,
                erro = null,
            )
        }
    }

    fun aoMudarSenha(valor: String) {
        _estado.update {
            it.copy(
                senha = valor,
                erro = null,
            )
        }
    }

    fun entrar() {
        val atual = _estado.value

        if (!atual.podeEnviar) return

        viewModelScope.launch {
            _estado.update {
                it.copy(
                    carregando = true,
                    erro = null,
                )
            }

            when (
                val resultado = repositorio.entrar(
                    atual.login,
                    atual.senha,
                )
            ) {
                is Resultado.Sucesso -> {
                    _estado.update {
                        it.copy(
                            carregando = false,
                            sucesso = true,
                        )
                    }
                }

                is Resultado.Erro -> {
                    _estado.update {
                        it.copy(
                            carregando = false,
                            erro = resultado.mensagem,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    aoEntrar: () -> Unit,
    aoCriarConta: () -> Unit,
    aoEntrarSemConta: () -> Unit,
    aoContinuarComGoogle: () -> Unit,
    aoVoltar: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsState()

    var senhaVisivel by remember {
        mutableStateOf(false)
    }

    val uriHandler = LocalUriHandler.current

    LaunchedEffect(estado.sucesso) {
        if (estado.sucesso) {
            aoEntrar()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .fundoHero(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = aoVoltar,
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextoForte,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            PainelMarcaEntrada(
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                ),
                altura = 168.dp,
            )

            Spacer(Modifier.height(18.dp))

            Kicker("ÁREA DO CLIENTE")

            Spacer(Modifier.height(30.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .vidro(
                        raio = RaioCard + 6.dp,
                        intensidade = 0.07f,
                    )
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Entrar na sua conta",
                    style =
                        MaterialTheme.typography.titleLarge,
                    color = TextoForte,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Acompanhe pedidos, orçamentos e manutenções.",
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(22.dp))

                CampoLazer(
                    valor = estado.login,
                    aoMudar = viewModel::aoMudarLogin,
                    rotulo = "E-mail ou usuário",
                    icone = Icons.Filled.Person,
                    erro = estado.erro != null,
                    opcoesTeclado = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )

                Spacer(Modifier.height(14.dp))

                CampoLazer(
                    valor = estado.senha,
                    aoMudar = viewModel::aoMudarSenha,
                    rotulo = "Senha",
                    icone = Icons.Filled.Lock,
                    erro = estado.erro != null,
                    transformacao = if (senhaVisivel) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    iconeFinal = {
                        IconButton(
                            onClick = {
                                senhaVisivel = !senhaVisivel
                            },
                        ) {
                            Icon(
                                imageVector = if (senhaVisivel) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription =
                                    if (senhaVisivel) {
                                        "Ocultar senha"
                                    } else {
                                        "Mostrar senha"
                                    },
                            )
                        }
                    },
                    opcoesTeclado = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    acoesTeclado = KeyboardActions(
                        onDone = {
                            viewModel.entrar()
                        },
                    ),
                )

                AnimatedVisibility(
                    visible = estado.erro != null,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(
                                color = RosaMarca.copy(
                                    alpha = 0.14f,
                                ),
                                shape =
                                    RoundedCornerShape(12.dp),
                            )
                            .padding(12.dp),
                        verticalAlignment =
                            Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector =
                                Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = RosaMarca,
                            modifier = Modifier.size(18.dp),
                        )

                        Spacer(Modifier.width(9.dp))

                        Text(
                            text = estado.erro.orEmpty(),
                            color = RosaMarca,
                            style =
                                MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                BotaoPrincipal(
                    texto = "ENTRAR",
                    aoClicar = viewModel::entrar,
                    habilitado = estado.podeEnviar,
                    carregando = estado.carregando,
                    cor = RosaMarca,
                )

                Spacer(Modifier.height(10.dp))

                BotaoVidro(
                    texto = "Continuar com Google",
                    aoClicar = aoContinuarComGoogle,
                    conteudoInicial = { MarcaGoogle() },
                )

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        uriHandler.openUri(
                            "https://www.lazersport.com.br/accounts/password/reset/",
                        )
                    },
                ) {
                    Text(
                        text = "Esqueci minha senha",
                        color = TextoMedio,
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            TextButton(
                onClick = aoCriarConta,
            ) {
                Text(
                    text = "Ainda não tenho conta",
                    color = AzulPastel,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            TextButton(
                onClick = aoEntrarSemConta,
            ) {
                Text(
                    text = "Ver catálogo sem entrar",
                    color = TextoMedio,
                )
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}