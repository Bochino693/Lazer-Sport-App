// ESTE ARQUIVO ERA UMA CLASSE VAZIA. A rota "registro" existia no grafo
// mas caia num placeholder que mandava pro site -- o app tinha um botao
// "Criar conta" que nao criava conta.
//
// Usa auth/registro/, que ja existia no ApiService, e entra logado
// direto (a API devolve token no cadastro).
//
// Telefone com mascara na digitacao: o ClientePerfil guarda telefone
// como CharField e voce nao quer 11 formatos diferentes no admin.

package com.example.lazer_sport_app.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lazer_sport_app.data.AuthRepository
import com.example.lazer_sport_app.data.Resultado
import com.example.lazer_sport_app.ui.components.BotaoPrincipal
import com.example.lazer_sport_app.ui.components.CampoLazer
import com.example.lazer_sport_app.ui.components.TopoTela
import com.example.lazer_sport_app.ui.menu.fundoNoite
import com.example.lazer_sport_app.ui.menu.vidro
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.RaioCard
import com.example.lazer_sport_app.ui.theme.RosaMarca
import com.example.lazer_sport_app.ui.theme.TextoFraco
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroUiState(
    val nome: String = "",
    val email: String = "",
    val telefone: String = "",
    val senha: String = "",
    val confirmacao: String = "",
    val carregando: Boolean = false,
    val erro: String? = null,
    val sucesso: Boolean = false,
) {
    val senhasBatem: Boolean
        get() = confirmacao.isEmpty() || senha == confirmacao

    val podeEnviar: Boolean
        get() = nome.isNotBlank() &&
                email.contains("@") &&
                telefone.count { it.isDigit() } >= 10 &&
                senha.length >= 6 &&
                senha == confirmacao &&
                !carregando
}

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val repositorio: AuthRepository,
) : ViewModel() {

    private val _estado = MutableStateFlow(RegistroUiState())
    val estado: StateFlow<RegistroUiState> = _estado.asStateFlow()

    fun aoMudarNome(v: String) = _estado.update { it.copy(nome = v, erro = null) }
    fun aoMudarEmail(v: String) = _estado.update { it.copy(email = v, erro = null) }
    fun aoMudarSenha(v: String) = _estado.update { it.copy(senha = v, erro = null) }
    fun aoMudarConfirmacao(v: String) =
        _estado.update { it.copy(confirmacao = v, erro = null) }

    fun aoMudarTelefone(v: String) =
        _estado.update { it.copy(telefone = mascararTelefone(v), erro = null) }

    fun registrar() {
        val atual = _estado.value
        if (!atual.podeEnviar) return

        viewModelScope.launch {
            _estado.update { it.copy(carregando = true, erro = null) }

            val r = repositorio.registrar(
                nome = atual.nome,
                email = atual.email,
                telefone = atual.telefone,
                senha = atual.senha,
            )

            when (r) {
                is Resultado.Sucesso ->
                    _estado.update { it.copy(carregando = false, sucesso = true) }
                is Resultado.Erro ->
                    _estado.update { it.copy(carregando = false, erro = r.mensagem) }
            }
        }
    }
}

/** (11) 91234-5678 -- aplicada a cada tecla, nunca deixa passar lixo. */
private fun mascararTelefone(bruto: String): String {
    val d = bruto.filter { it.isDigit() }.take(11)
    return when {
        d.isEmpty() -> ""
        d.length <= 2 -> "($d"
        d.length <= 6 -> "(${d.take(2)}) ${d.drop(2)}"
        d.length <= 10 -> "(${d.take(2)}) ${d.drop(2).take(4)}-${d.drop(6)}"
        else -> "(${d.take(2)}) ${d.drop(2).take(5)}-${d.drop(7)}"
    }
}

@Composable
fun RegistroScreen(
    aoConcluir: () -> Unit,
    aoVoltar: () -> Unit,
    aoJaTenhoConta: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsState()
    var senhaVisivel by remember { mutableStateOf(false) }

    LaunchedEffect(estado.sucesso) {
        if (estado.sucesso) aoConcluir()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .fundoNoite(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            TopoTela(
                titulo = "Criar conta",
                subtitulo = "Leva menos de um minuto",
                aoVoltar = aoVoltar,
            )

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .vidro(raio = RaioCard + 6.dp, intensidade = 0.07f)
                    .padding(22.dp),
            ) {
                CampoLazer(
                    valor = estado.nome,
                    aoMudar = viewModel::aoMudarNome,
                    rotulo = "Nome completo",
                    icone = Icons.Filled.Person,
                    opcoesTeclado = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                )

                Spacer(Modifier.height(14.dp))

                CampoLazer(
                    valor = estado.email,
                    aoMudar = viewModel::aoMudarEmail,
                    rotulo = "E-mail",
                    icone = Icons.Filled.AlternateEmail,
                    opcoesTeclado = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )

                Spacer(Modifier.height(14.dp))

                CampoLazer(
                    valor = estado.telefone,
                    aoMudar = viewModel::aoMudarTelefone,
                    rotulo = "Telefone / WhatsApp",
                    icone = Icons.Filled.Phone,
                    opcoesTeclado = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next,
                    ),
                )

                Spacer(Modifier.height(14.dp))

                CampoLazer(
                    valor = estado.senha,
                    aoMudar = viewModel::aoMudarSenha,
                    rotulo = "Senha (mínimo 6 caracteres)",
                    icone = Icons.Filled.Lock,
                    transformacao = if (senhaVisivel) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    iconeFinal = {
                        IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                            Icon(
                                imageVector = if (senhaVisivel) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = null,
                            )
                        }
                    },
                    opcoesTeclado = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                    ),
                )

                Spacer(Modifier.height(14.dp))

                CampoLazer(
                    valor = estado.confirmacao,
                    aoMudar = viewModel::aoMudarConfirmacao,
                    rotulo = "Confirmar senha",
                    icone = Icons.Filled.Lock,
                    erro = !estado.senhasBatem,
                    transformacao = if (senhaVisivel) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    opcoesTeclado = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                )

                if (!estado.senhasBatem) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "As senhas não conferem.",
                        color = RosaMarca,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                AnimatedVisibility(visible = estado.erro != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(
                                RosaMarca.copy(alpha = 0.14f),
                                RoundedCornerShape(12.dp),
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = RosaMarca,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(9.dp))
                        Text(
                            text = estado.erro.orEmpty(),
                            color = RosaMarca,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                BotaoPrincipal(
                    texto = "CRIAR MINHA CONTA",
                    aoClicar = viewModel::registrar,
                    habilitado = estado.podeEnviar,
                    carregando = estado.carregando,
                    cor = RosaMarca,
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(onClick = aoJaTenhoConta) {
                    Text("Já tenho conta", color = AzulPastel)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Seus dados são usados apenas para atendimento, " +
                        "orçamentos e pedidos.",
                color = TextoFraco,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
            )

            Spacer(Modifier.height(30.dp))
        }
    }
}