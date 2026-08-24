// app/src/main/java/br/com/lazersport/app/LazerSportApp.kt
//
// Ponto de partida do Hilt. Esta classe não faz nada visível, mas sem
// ela nenhum @Inject funciona -- o app crasha ao abrir com
// "Hilt Activity must be attached to an @HiltAndroidApp Application".
//
// O AndroidManifest precisa apontar pra cá com:
//     android:name=".LazerSportApp"
//
// ATENÇÃO: apague o arquivo antigo LAzerSportApp.kt (com "A"
// maiúsculo). Ter os dois deixa o manifest apontando pro errado.

package br.com.lazersport.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LazerSportApp : Application()