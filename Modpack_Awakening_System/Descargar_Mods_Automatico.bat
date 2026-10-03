@echo off
chcp 65001 > nul
title Awakening: Monarch's Ascension - Verificador de Modpack
echo =====================================================================
echo    Iniciando verificador de Awakening: Monarch's Ascension (1.21.1)
echo =====================================================================
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0Descargar_Mods_Automatico.ps1"
echo.
echo Presiona cualquier tecla para salir...
pause > nul
