@echo off
setlocal
cd /d "%~dp0"

echo ============================================================
echo  CORRECAO DO APP LAZER ^& SPORT
echo ============================================================
echo.

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0aplicar_correcao_windows.ps1"

echo.
echo Pressione qualquer tecla para fechar esta janela.
pause >nul
endlocal
