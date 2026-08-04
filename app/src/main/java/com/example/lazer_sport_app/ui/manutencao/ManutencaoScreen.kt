// MANUTENCOES -- abrir chamado e acompanhar os abertos.
//
// Espelha o model Manutencao do Django, inclusive o
// brinquedo_nao_listado / brinquedo_descricao_livre pra equipamento
// antigo ou fora do catalogo.
//
// Se /api/v1/manutencoes/ ainda nao existir, o envio cai no WhatsApp
// com a mesma mensagem formatada -- o chamado nao se perde.

package com.example.lazer_sport_app.ui.manutencao

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lazer_sport_app.data.AuthRepository
import com.example.lazer_sport_app.data.CatalogoRepository
import com.example.lazer_sport_app.data.Contato
import com.example.lazer_sport_app.data.FonteLista
import com.example.lazer_sport_app.data.ManutencaoDto
import com.example.lazer_sport_app.data.ManutencaoEnvio
import com.example.lazer_sport_app.data.ManutencaoRepository
import com.example.lazer_sport_app.data.Resultado
import com.example.lazer_sport_app.ui.components.BotaoPrincipal
import com.example.lazer_sport_app.ui.components.BotaoVidro
import com.example.lazer_sport_app.ui.components.CampoLazer
import com.example.lazer_sport_app.ui.components.EstadoCarregando
import com.example.lazer_sport_app.ui.components.EstadoVazio
import com.example.lazer_sport_app.ui.components.ItemVitrine
import com.example.lazer_sport_app.ui.components.Kicker
import com.example.lazer_sport_app.ui.components.TopoTela
import com.example.lazer_sport_app.ui.menu.fundoNoite
import com.example.lazer_sport_app.ui.menu.vidro
import com.example.lazer_sport_app.ui.menu.vidroTingido
import com.example.lazer_sport_app.ui.theme.Amarelo
import com.example.lazer_sport_app.ui.theme.AzulDardo
import com.example.lazer_sport_app.ui.theme.AzulPastel
import com.example.lazer_sport_app.ui.theme.NoiteCartao
import com.example.lazer_sport_app.ui.theme.RosaMarca
import com.example.lazer_sport_app.ui.theme.TextoForte
import com.example.lazer_sport_app.ui.theme.TextoFraco
import com.example.lazer_sport_app.ui.theme.TextoMedio
import com.example.lazer_sport_app.ui.theme.Verde
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============ ESTADO ============

data class FormularioManutencao(
    val brinquedoId: Int? = null,
    val brinquedoNome: String = "",
    val naoListado: Boolean = false,
    val descricaoLivre: String = "",
    val descricao: String = "",
    val telefone: String = "",
    val cep: String = "",
    val endereco: String = "",
    val numero: String = "",
    val complemento: String = "",
    val bairro: String = "",
    val cidade: String = "",
    val estado: String = "",
)

data class EstadoManutencao(
    val carregando: Boolean = true,
    val enviando: Boolean = false,
    val enviado: Boolean = false,
    val erro: String? = null,
    val abaFormulario: Boolean = true,
    val formulario: FormularioManutencao = FormularioManutencao(),
    val brinquedos: List<ItemVitrine> = emptyList(),
    val chamados: List<ManutencaoDto> = emptyList(),
)

@HiltViewModel
class ManutencaoViewModel @Inject constructor(
    private val repositorio: ManutencaoRepository,
    private val catalogo: CatalogoRepository,
    auth: AuthRepository,
) : ViewModel() {

    val estaLogado: StateFlow<Boolean> = auth.estaLogado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _estado = MutableStateFlow(EstadoManutencao())
    val estado: StateFlow<EstadoManutencao> = _estado.asStateFlow()

    init { carregar() }

    fun carregar() {
        viewModelScope.launch {
            _estado.update { it.copy(carregando = true) }
            val brinquedos = catalogo.listar(FonteLista.BRINQUEDOS, 1, null, 0).itens
            val chamados = repositorio.listar()
            _estado.update {
                it.copy(carregando = false, brinquedos = brinquedos, chamados = chamados)
            }
        }
    }

    fun aba(formulario: Boolean) = _estado.update { it.copy(abaFormulario = formulario) }

    fun editar(bloco: (FormularioManutencao) -> FormularioManutencao) =
        _estado.update { it.copy(formulario = bloco(it.formulario), erro = null) }

    fun enviar() {
        val f = _estado.value.formulario

        val equipamentoOk =
            (f.naoListado && f.descricaoLivre.isNotBlank()) || (!f.naoListado && f.brinquedoId != null)

        val problema = when {
            !equipamentoOk -> "Escolha o equipamento ou descreva qual é."
            f.descricao.isBlank() -> "Conte o que está acontecendo com o equipamento."
            f.telefone.isBlank() -> "Informe um telefone para contato."
            f.endereco.isBlank() || f.numero.isBlank() -> "Informe endereço e número."
            else -> null
        }

        if (problema != null) {
            _estado.update { it.copy(erro = problema) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(enviando = true, erro = null) }

            val resultado = repositorio.enviar(
                ManutencaoEnvio(
                    brinquedo = if (f.naoListado) null else f.brinquedoId,
                    brinquedoNaoListado = f.naoListado,
                    brinquedoDescricaoLivre = if (f.naoListado) f.descricaoLivre else "",
                    descricao = f.descricao,
                    telefoneContato = f.telefone,
                    cep = f.cep,
                    endereco = f.endereco,
                    numero = f.numero,
                    complemento = f.complemento,
                    bairro = f.bairro,
                    cidade = f.cidade,
                    estado = f.estado.uppercase().take(2),
                )
            )

            when (resultado) {
                is Resultado.Sucesso -> {
                    _estado.update { it.copy(enviando = false, enviado = true) }
                    carregar()
                }
                is Resultado.Erro ->
                    _estado.update { it.copy(enviando = false, erro = resultado.mensagem) }
            }
        }
    }

    fun novoChamado() = _estado.update {
        it.copy(enviado = false, formulario = FormularioManutencao(), abaFormulario = true)
    }
}

// ============ TELA ============

@Composable
fun ManutencaoScreen(
    aoVoltar: () -> Unit,
    aoEntrar: () -> Unit,
    viewModel: ManutencaoViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsState()
    val logado by viewModel.estaLogado.collectAsState()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopoTela(
                titulo = "Manutenções",
                subtitulo = "Assistência técnica Lazer & Sport",
                aoVoltar = aoVoltar,
            )
        },
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .fundoNoite()
                .padding(padding),
        ) {
            when {
                !logado -> EstadoVazio(
                    icone = Icons.Filled.Lock,
                    titulo = "Entre para abrir um chamado",
                    mensagem = "Precisamos da sua conta para vincular o chamado, " +
                            "acompanhar o status e avisar quando o técnico sair.",
                    textoAcao = "Entrar na minha conta",
                    aoAcao = aoEntrar,
                    corIcone = Amarelo,
                )

                estado.enviado -> Sucesso(
                    aoNovo = viewModel::novoChamado,
                    aoAcompanhar = {
                        viewModel.novoChamado()
                        viewModel.aba(false)
                    },
                )

                estado.carregando -> EstadoCarregando("Carregando seus chamados...")

                else -> Column(Modifier.fillMaxSize()) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Aba(
                            texto = "Abrir chamado",
                            ativa = estado.abaFormulario,
                            aoClicar = { viewModel.aba(true) },
                            modifier = Modifier.weight(1f),
                        )
                        Aba(
                            texto = "Meus chamados (${estado.chamados.size})",
                            ativa = !estado.abaFormulario,
                            aoClicar = { viewModel.aba(false) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (estado.abaFormulario) {
                        Formulario(
                            estado = estado,
                            aoEditar = viewModel::editar,
                            aoEnviar = viewModel::enviar,
                            aoWhatsapp = {
                                uriHandler.openUri(
                                    Contato.whatsapp(mensagemChamado(estado.formulario))
                                )
                            },
                        )
                    } else {
                        ListaChamados(
                            chamados = estado.chamados,
                            aoAbrirChamado = { viewModel.aba(true) },
                        )
                    }
                }
            }
        }
    }
}

// ============ PARTES ============

@Composable
private fun Aba(
    texto: String,
    ativa: Boolean,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .then(
                if (ativa) {
                    Modifier.vidroTingido(RosaMarca, raio = 15.dp, intensidade = 0.22f)
                } else {
                    Modifier.vidro(raio = 15.dp, intensidade = 0.05f)
                }
            )
            .clickable(onClick = aoClicar)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = texto,
            color = if (ativa) Color.White else TextoMedio,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Formulario(
    estado: EstadoManutencao,
    aoEditar: ((FormularioManutencao) -> FormularioManutencao) -> Unit,
    aoEnviar: () -> Unit,
    aoWhatsapp: () -> Unit,
) {
    val f = estado.formulario
    var menuAberto by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .vidro(raio = 22.dp, intensidade = 0.07f)
                .padding(18.dp),
        ) {
            Kicker("EQUIPAMENTO", AzulDardo)
            Spacer(Modifier.height(14.dp))

            if (!f.naoListado) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .vidro(raio = 14.dp, intensidade = 0.06f)
                            .clickable { menuAberto = true }
                            .padding(horizontal = 15.dp, vertical = 17.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = f.brinquedoNome.ifBlank { "Escolha o brinquedo do catálogo" },
                            color = if (f.brinquedoNome.isBlank()) TextoFraco else TextoForte,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = AzulDardo)
                    }

                    DropdownMenu(
                        expanded = menuAberto,
                        onDismissRequest = { menuAberto = false },
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(NoiteCartao),
                    ) {
                        estado.brinquedos.forEach { brinquedo ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        brinquedo.nome,
                                        color = TextoForte,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                },
                                onClick = {
                                    menuAberto = false
                                    aoEditar {
                                        it.copy(
                                            brinquedoId = brinquedo.id,
                                            brinquedoNome = brinquedo.nome,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            } else {
                CampoLazer(
                    valor = f.descricaoLivre,
                    aoMudar = { texto -> aoEditar { it.copy(descricaoLivre = texto) } },
                    rotulo = "Qual é o equipamento?",
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Não está no catálogo",
                        color = TextoForte,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Equipamento antigo, de outra marca ou sob medida",
                        color = TextoFraco,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Switch(
                    checked = f.naoListado,
                    onCheckedChange = { marcado ->
                        aoEditar {
                            it.copy(
                                naoListado = marcado,
                                brinquedoId = if (marcado) null else it.brinquedoId,
                                brinquedoNome = if (marcado) "" else it.brinquedoNome,
                            )
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AzulDardo,
                        uncheckedThumbColor = TextoFraco,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.10f),
                    ),
                )
            }
        }

        CampoLazer(
            valor = f.descricao,
            aoMudar = { texto -> aoEditar { it.copy(descricao = texto) } },
            rotulo = "O que está acontecendo?",
            linhaUnica = false,
            minhasLinhas = 4,
        )

        CampoLazer(
            valor = f.telefone,
            aoMudar = { texto -> aoEditar { it.copy(telefone = texto) } },
            rotulo = "Telefone para contato",
            opcoesTeclado = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .vidro(raio = 22.dp, intensidade = 0.07f)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Kicker("ONDE ESTÁ O EQUIPAMENTO", AzulPastel)
            Spacer(Modifier.height(2.dp))

            CampoLazer(
                valor = f.cep,
                aoMudar = { texto -> aoEditar { it.copy(cep = texto) } },
                rotulo = "CEP",
                opcoesTeclado = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            CampoLazer(
                valor = f.endereco,
                aoMudar = { texto -> aoEditar { it.copy(endereco = texto) } },
                rotulo = "Endereço",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoLazer(
                    valor = f.numero,
                    aoMudar = { texto -> aoEditar { it.copy(numero = texto) } },
                    rotulo = "Número",
                    modifier = Modifier.weight(1f),
                )
                CampoLazer(
                    valor = f.complemento,
                    aoMudar = { texto -> aoEditar { it.copy(complemento = texto) } },
                    rotulo = "Compl.",
                    modifier = Modifier.weight(1f),
                )
            }
            CampoLazer(
                valor = f.bairro,
                aoMudar = { texto -> aoEditar { it.copy(bairro = texto) } },
                rotulo = "Bairro",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoLazer(
                    valor = f.cidade,
                    aoMudar = { texto -> aoEditar { it.copy(cidade = texto) } },
                    rotulo = "Cidade",
                    modifier = Modifier.weight(2f),
                )
                CampoLazer(
                    valor = f.estado,
                    aoMudar = { texto -> aoEditar { it.copy(estado = texto.take(2)) } },
                    rotulo = "UF",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (estado.erro != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .vidroTingido(RosaMarca, raio = 16.dp, intensidade = 0.14f)
                    .padding(14.dp),
            ) {
                Text(
                    text = estado.erro,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        BotaoPrincipal(
            texto = "Enviar chamado",
            aoClicar = aoEnviar,
            cor = RosaMarca,
            carregando = estado.enviando,
            icone = Icons.Filled.Send,
        )

        BotaoVidro(
            texto = "Prefiro pelo WhatsApp",
            aoClicar = aoWhatsapp,
            icone = Icons.AutoMirrored.Filled.Chat,
        )
    }
}

@Composable
private fun ListaChamados(chamados: List<ManutencaoDto>, aoAbrirChamado: () -> Unit) {
    if (chamados.isEmpty()) {
        EstadoVazio(
            icone = Icons.Filled.Handyman,
            titulo = "Nenhum chamado aberto",
            mensagem = "Quando você solicitar uma manutenção, o andamento " +
                    "aparece aqui: pendente, em andamento e concluída.",
            textoAcao = "Abrir um chamado",
            aoAcao = aoAbrirChamado,
            corIcone = AzulDardo,
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.navigationBarsPadding(),
    ) {
        items(chamados, key = { it.id }) { chamado ->
            CartaoChamado(chamado)
        }
    }
}

@Composable
private fun CartaoChamado(chamado: ManutencaoDto) {
    val (cor, rotulo) = when (chamado.status) {
        "A" -> AzulDardo to "Em andamento"
        "C" -> Verde to "Concluída"
        "X" -> RosaMarca to "Cancelada"
        else -> Amarelo to "Pendente"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .vidro(raio = 20.dp, intensidade = 0.07f)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .vidroTingido(cor, raio = 12.dp, intensidade = 0.16f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Handyman,
                    contentDescription = null,
                    tint = cor,
                    modifier = Modifier.size(19.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = chamado.nomeEquipamento.ifBlank { "Chamado #${chamado.id}" },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextoForte,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "#${chamado.id}" +
                            (chamado.criadoEm?.take(10)?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoFraco,
                )
            }
            Box(
                modifier = Modifier
                    .vidroTingido(cor, raio = 999.dp, intensidade = 0.18f)
                    .padding(horizontal = 11.dp, vertical = 6.dp),
            ) {
                Text(
                    text = chamado.statusDisplay ?: rotulo,
                    color = cor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                )
            }
        }

        if (chamado.descricao.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = chamado.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = TextoMedio,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Sucesso(aoNovo: () -> Unit, aoAcompanhar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .vidroTingido(Verde, raio = 26.dp, intensidade = 0.16f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Verde,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.height(22.dp))
        Text(
            text = "Chamado registrado!",
            style = MaterialTheme.typography.headlineMedium,
            color = TextoForte,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Nossa equipe entra em contato pelo telefone informado " +
                    "para combinar a visita técnica.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoMedio,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        BotaoPrincipal(texto = "Ver meus chamados", aoClicar = aoAcompanhar, cor = AzulDardo)
        Spacer(Modifier.height(10.dp))
        BotaoVidro(texto = "Abrir outro chamado", aoClicar = aoNovo)
    }
}

private fun mensagemChamado(f: FormularioManutencao): String {
    val equipamento = if (f.naoListado) {
        f.descricaoLivre.ifBlank { "Equipamento não catalogado" }
    } else {
        f.brinquedoNome.ifBlank { "Equipamento do catálogo" }
    }
    return "Olá! Preciso de manutenção (enviado pelo app).\n\n" +
            "Equipamento: $equipamento\n" +
            "Problema: ${f.descricao}\n" +
            "Telefone: ${f.telefone}\n" +
            "Endereço: ${f.endereco}, ${f.numero} ${f.complemento} - " +
            "${f.bairro} ${f.cidade}/${f.estado} ${f.cep}"
}