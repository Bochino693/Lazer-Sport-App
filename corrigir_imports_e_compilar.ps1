param()

$ErrorActionPreference = "Stop"
$raiz = (Get-Location).Path

if (-not (Test-Path (Join-Path $raiz "settings.gradle.kts"))) {
    throw "Execute este script na raiz do projeto, onde fica settings.gradle.kts."
}

$pastaFontes = Join-Path $raiz "app/src/main/java"
$gradlew = Join-Path $raiz "gradlew.bat"

if (-not (Test-Path $pastaFontes)) {
    throw "A pasta app/src/main/java nao foi encontrada."
}

if (-not (Test-Path $gradlew)) {
    throw "gradlew.bat nao foi encontrado na raiz do projeto."
}

if (-not $env:JAVA_HOME) {
    $jbrAndroidStudio = Join-Path $env:ProgramFiles "Android/Android Studio/jbr"
    if (Test-Path $jbrAndroidStudio) {
        $env:JAVA_HOME = $jbrAndroidStudio
        $env:Path = "$env:JAVA_HOME/bin;$env:Path"
    }
}

$dataBackup = Get-Date -Format "yyyyMMdd-HHmmss"
$pastaBackup = Join-Path $raiz "_backup_imports_$dataBackup"
$utf8SemBom = New-Object System.Text.UTF8Encoding($false)
$fontesKotlin = Get-ChildItem $pastaFontes -Recurse -Filter "*.kt" -File
$fontesCorrigidas = 0

Write-Host "Analisando imports de todos os arquivos Kotlin..." -ForegroundColor Cyan

foreach ($fonteKotlin in $fontesKotlin) {
    $conteudoOriginal = [System.IO.File]::ReadAllText($fonteKotlin.FullName)
    $conteudoCorrigido = $conteudoOriginal

    # Estes imports sao colocados por autoimportacao do Android Studio e
    # entram em conflito com Modifier e Surface do Jetpack Compose.
    $conteudoCorrigido = $conteudoCorrigido -replace '(?m)^\s*import java\.lang\.reflect\.Modifier\s*\r?\n', ''
    $conteudoCorrigido = $conteudoCorrigido -replace '(?m)^\s*import android\.view\.Surface\s*\r?\n', ''

    # Se houver dois imports com o mesmo nome simples, preserva somente
    # o Modifier correto do Jetpack Compose, independentemente da origem
    # do segundo autoimport.
    $importsModifier = [regex]::Matches(
        $conteudoCorrigido,
        '(?m)^[ \t]*import[ \t]+[^\r\n]+\.Modifier[ \t]*\r?\n'
    )
    if ($importsModifier.Count -gt 1) {
        $conteudoCorrigido = [regex]::Replace(
            $conteudoCorrigido,
            '(?m)^[ \t]*import[ \t]+[^\r\n]+\.Modifier[ \t]*\r?\n',
            ''
        )
        $conteudoCorrigido = [regex]::Replace(
            $conteudoCorrigido,
            '(?m)^(package[ \t]+[^\r\n]+\r?\n)',
            ('$1' + [Environment]::NewLine + 'import androidx.compose.ui.Modifier' + [Environment]::NewLine),
            1
        )
    }

    if ($conteudoCorrigido -ne $conteudoOriginal) {
        $relativo = $fonteKotlin.FullName.Substring($raiz.Length).TrimStart('\', '/')
        $destinoBackup = Join-Path $pastaBackup $relativo
        $pastaDestino = Split-Path -Parent $destinoBackup
        New-Item -ItemType Directory -Path $pastaDestino -Force | Out-Null
        Copy-Item $fonteKotlin.FullName $destinoBackup -Force

        [System.IO.File]::WriteAllText($fonteKotlin.FullName, $conteudoCorrigido, $utf8SemBom)
        $fontesCorrigidas++
        Write-Host "Corrigido: $relativo" -ForegroundColor Yellow
    }
}

$conflitosRestantes = @()
foreach ($fonteKotlin in $fontesKotlin) {
    $importsModifier = @(Select-String -Path $fonteKotlin.FullName -Pattern '^[ \t]*import[ \t]+[^\r\n]+\.Modifier[ \t]*$')
    if ($importsModifier.Count -gt 1) {
        $relativo = $fonteKotlin.FullName.Substring($raiz.Length).TrimStart('\', '/')
        $conflitosRestantes += $relativo
    }
}

if ($conflitosRestantes.Count -gt 0) {
    throw "Ainda existem dois imports chamados Modifier em: $($conflitosRestantes -join ', '). Deixe apenas import androidx.compose.ui.Modifier."
}

if ($fontesCorrigidas -gt 0) {
    Write-Host "Backup dos arquivos alterados: $pastaBackup" -ForegroundColor Cyan
} else {
    Write-Host "Nenhum import incorreto foi encontrado. O codigo ja estava limpo." -ForegroundColor Green
}

Write-Host "Compilando com as dependencias que ja estao no computador..." -ForegroundColor Cyan
& $gradlew :app:assembleDebug --stacktrace

if ($LASTEXITCODE -ne 0) {
    throw "A limpeza dos imports terminou, mas apareceu outro erro de compilacao. Envie somente o primeiro bloco iniciado por 'e:' ou 'Caused by'."
}

$apk = Join-Path $raiz "app/build/outputs/apk/debug/app-debug.apk"
if (Test-Path $apk) {
    Write-Host "SUCESSO! APK pronto em: $apk" -ForegroundColor Green
} else {
    Write-Host "Build concluido. Confirme o APK em app/build/outputs/apk/debug/." -ForegroundColor Yellow
}
