// app/src/main/java/com/example/lazer_sport_app/ui/login/LoginScreen.kt
//
// Tela de login + ViewModel no mesmo arquivo (são acoplados; separe
// quando crescer).
//
// Decisões de design:
//   - Fundo em degradê azul da marca, cartão branco flutuando por cima.
//     Dá profundidade sem precisar de imagem pesada.
//   - O logo entra como texto estilizado por enquanto. Quando quiser a
//     marca de verdade, jogue o logoo.webp que otimizei em res/drawable
//     e troque o Text pelo Image (comentário marcado abaixo).
//   - Botão desabilitado enquanto os campos estão vazios: o cliente não
//     descobre o erro só depois de clicar.
//   - Um campo só pra "e-mail ou usuário", porque a API aceita os dois.

package com.example.lazer_sport_app.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lazer_sport_app.data.AuthRepository
import com.example.lazer_sport_app.data.Resultado
import com.example.lazer_sport_app.ui.theme.AzulClaro
import com.example.lazer_sport_app.ui.theme.AzulEscuro
import com.example.lazer_sport_app.ui.theme.NoiteProfunda
import com.example.lazer_sport_app.ui.theme.RaioBotao
import com.example.lazer_sport_app.ui.theme.RaioCampo
import com.example.lazer_sport_app.ui.theme.Vermelho
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============================================================
// ESTADO E VIEWMODEL
// ============================================================

data class LoginUiState(
    val login: String = "",
    val senha: String = "",
    val carregando: Boolean = false,
    val erro: String? = null,
    val sucesso: Boolean = false,
) {
    val podeEnviar: Boolean
        get() = login.isNotBlank() && senha.length >= 4 && !carregando
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repositorio: AuthRepository,
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun aoMudarLogin(valor: String) =
        _estado.update { it.copy(login = valor, erro = null) }

    fun aoMudarSenha(valor: String) =
        _estado.update { it.copy(senha = valor, erro = null) }

    fun entrar() {
        val atual = _estado.value
        if (!atual.podeEnviar) return

        viewModelScope.launch {
            _estado.update { it.copy(carregando = true, erro = null) }

            when (val r = repositorio.entrar(atual.login, atual.senha)) {
                is Resultado.Sucesso ->
                    _estado.update { it.copy(carregando = false, sucesso = true) }
                is Resultado.Erro ->
                    _estado.update { it.copy(carregando = false, erro = r.mensagem) }
            }
        }
    }
}

// ============================================================
// TELA
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    aoEntrar: () -> Unit,
    aoCriarConta: () -> Unit,
    aoEntrarSemConta: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsState()
    var senhaVisivel by remember { mutableStateOf(false) }

    // Quando o login dá certo, navega pro menu
    LaunchedEffect(estado.sucesso) {
        if (estado.sucesso) aoEntrar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AzulEscuro, AzulClaro, NoiteProfunda),
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(96.dp))

            // ---------- MARCA ----------
            // Pra usar o logo real: jogue logoo.webp em res/drawable e troque
            // este bloco por:
            //   Image(painterResource(R.drawable.logoo), null, Modifier.height(84.dp))
            Text(
                text = "LAZER & SPORT",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Brinquedos que fazem a diferença",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(40.dp))

            // ---------- CARTÃO ----------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Entrar na sua conta",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = estado.login,
                        onValueChange = viewModel::aoMudarLogin,
                        label = { Text("E-mail ou usuário") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Filled.Person, contentDescription = null)
                        },
                        shape = RoundedCornerShape(RaioCampo),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        isError = estado.erro != null,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = estado.senha,
                        onValueChange = viewModel::aoMudarSenha,
                        label = { Text("Senha") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                Icon(
                                    imageVector = if (senhaVisivel)
                                        Icons.Filled.VisibilityOff
                                    else
                                        Icons.Filled.Visibility,
                                    contentDescription = if (senhaVisivel)
                                        "Ocultar senha" else "Mostrar senha",
                                )
                            }
                        },
                        visualTransformation = if (senhaVisivel)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        shape = RoundedCornerShape(RaioCampo),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.entrar() }
                        ),
                        isError = estado.erro != null,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // ---------- ERRO ----------
                    AnimatedVisibility(visible = estado.erro != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .background(
                                    Vermelho.copy(alpha = 0.10f),
                                    RoundedCornerShape(12.dp),
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = estado.erro.orEmpty(),
                                color = Vermelho,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    Button(
                        onClick = viewModel::entrar,
                        enabled = estado.podeEnviar,
                        shape = RoundedCornerShape(RaioBotao),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Vermelho,
                            contentColor = Color.White,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                    ) {
                        if (estado.carregando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("ENTRAR", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    TextButton(onClick = aoCriarConta) {
                        Text("Ainda não tenho conta")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            TextButton(onClick = aoEntrarSemConta) {
                Text(
                    "Ver catálogo sem entrar",
                    color = Color.White.copy(alpha = 0.9f),
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}