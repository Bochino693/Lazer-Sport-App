package br.com.lazersport.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import br.com.lazersport.app.data.LoginSocialRepository
import br.com.lazersport.app.data.RetornoLogin
import br.com.lazersport.app.ui.navigation.NavegacaoApp
import br.com.lazersport.app.ui.theme.LazerSportTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var loginSocial: LoginSocialRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        tratarDeepLink(intent)

        setContent {
            LazerSportTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NavegacaoApp()
                }
            }
        }
    }

    /** singleTask: o retorno do navegador cai aqui, não em onCreate. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        tratarDeepLink(intent)
    }

    private fun tratarDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return

        lifecycleScope.launch {
            when (val retorno = loginSocial.processar(uri)) {
                is RetornoLogin.Sucesso -> Toast.makeText(
                    this@MainActivity,
                    "Bem-vindo, ${retorno.nome.split(" ").first()}!",
                    Toast.LENGTH_LONG,
                ).show()

                is RetornoLogin.Falha -> Toast.makeText(
                    this@MainActivity,
                    retorno.mensagem,
                    Toast.LENGTH_LONG,
                ).show()

                RetornoLogin.Ignorado -> Unit
            }
        }
    }
}