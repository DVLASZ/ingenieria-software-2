# Detiene los servicios que levantó levantar.ps1 y, con -Rabbit, también el contenedor de RabbitMQ.
param([switch]$Rabbit)

$raiz = Split-Path -Parent $PSScriptRoot
Set-Location $raiz

if (Test-Path logs\pids) {
    foreach ($linea in Get-Content logs\pids) {
        $nombre, $id = $linea -split ':'
        if ($id) {
            try { Stop-Process -Id ([int]$id) -Force -ErrorAction Stop; Write-Host "  $nombre detenido" } catch { }
        }
    }
    Remove-Item logs\pids
}
if ($Rabbit) { docker compose down }
