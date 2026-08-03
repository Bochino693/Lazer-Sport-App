@echo off
setlocal
cd /d "%~dp0"
title Lazer Sport - corrigir imports e compilar
echo Corrigindo o conflito de Modifier e compilando o app...
echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0corrigir_imports_e_compilar.ps1"
set "CODIGO_SAIDA=%ERRORLEVEL%"
echo.
if not "%CODIGO_SAIDA%"=="0" (
    echo A compilacao parou no erro mostrado acima.
) else (
    echo Processo concluido com sucesso.
)
echo.
pause
