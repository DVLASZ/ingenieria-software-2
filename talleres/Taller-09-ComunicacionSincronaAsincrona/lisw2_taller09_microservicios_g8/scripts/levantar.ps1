# Compila (si hace falta) y levanta los nueve servicios con `java -jar`.
#   .\scripts\levantar.ps1                levanta RabbitMQ con Docker (si no responde) y los servicios
#   .\scripts\levantar.ps1 -SinDocker     usa el RabbitMQ que ya esté en localhost:5672 (instalado en el equipo)
# Los registros quedan en logs\<servicio>.log y los identificadores de proceso en logs\pids.
param([switch]$SinDocker)

$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
Set-Location $raiz
New-Item -ItemType Directory -Force logs | Out-Null

$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' } else { 'java' }
$servicios = [ordered]@{
    'user-service' = 8081; 'validation-service' = 8082; 'lifecycle-service' = 8084; 'question-service' = 8083
    'review-service' = 8085; 'notification-service' = 8086; 'history-service' = 8087
    'simulation-service' = 8088; 'tracking-service' = 8089
}

function Test-Puerto($puerto) {
    try { $c = New-Object Net.Sockets.TcpClient; $c.Connect('localhost', $puerto); $c.Close(); return $true } catch { return $false }
}

if (-not (Test-Puerto 5672)) {
    if ($SinDocker) { Write-Host 'No hay un RabbitMQ en localhost:5672. Inícialo y vuelve a ejecutar.'; exit 1 }
    Write-Host 'Levantando RabbitMQ con Docker...'
    docker compose up -d
    # el puerto se abre antes de que RabbitMQ esté listo: se espera a que Docker lo marque como saludable
    for ($i = 0; $i -lt 60; $i++) {
        if ((docker inspect -f '{{.State.Health.Status}}' saberpro-rabbitmq 2>$null) -eq 'healthy') { break }
        Start-Sleep 2
    }
    if ((docker inspect -f '{{.State.Health.Status}}' saberpro-rabbitmq 2>$null) -ne 'healthy') { Write-Host 'RabbitMQ no quedó saludable'; exit 1 }
}
Write-Host 'RabbitMQ listo (consola: http://localhost:15672, guest / guest)'

if (-not (Get-ChildItem -Path . -Recurse -Filter '*-SNAPSHOT.jar' -ErrorAction SilentlyContinue | Where-Object { $_.FullName -match 'target' })) {
    Write-Host 'Compilando...'
    mvn -q -DskipTests package
}

$pids = @()
foreach ($nombre in $servicios.Keys) {
    $puerto = $servicios[$nombre]
    $jar = (Get-ChildItem "$nombre\target\*-SNAPSHOT.jar" | Select-Object -First 1).FullName
    $env:SERVER_PORT = "$puerto"
    $p = Start-Process -FilePath $java -ArgumentList '-jar', "`"$jar`"" -RedirectStandardOutput "logs\$nombre.log" `
        -RedirectStandardError "logs\$nombre.err.log" -PassThru -WindowStyle Hidden
    $pids += "${nombre}:$($p.Id)"
    Write-Host "  $nombre arrancando en el puerto $puerto"
}
Remove-Item Env:SERVER_PORT -ErrorAction SilentlyContinue
$pids | Set-Content logs\pids

Write-Host 'Esperando a que respondan...'
foreach ($nombre in $servicios.Keys) {
    $puerto = $servicios[$nombre]
    $listo = $false
    for ($i = 0; $i -lt 60 -and -not $listo; $i++) {
        if (Test-Puerto $puerto) { $listo = $true } else { Start-Sleep 2 }
    }
    if ($listo) { Write-Host "  $nombre listo" } else { Write-Host "  $nombre NO respondió (revisa logs\$nombre.log)" }
}
Write-Host 'Todo en marcha. Para detenerlo: .\scripts\detener.ps1'
