# Démarre les 8 services du TP4 puis ouvre le front-end Commandes.
$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

# JDK 17 (Spring Boot 3). On privilégie les JDK connus sur ce poste.
$javaCandidates = @(
    "$env:USERPROFILE\.jdks\ms-17.0.20.1\bin\java.exe",
    "$env:USERPROFILE\.jdks\jbr-21.0.11\bin\java.exe",
    "C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
)
$java = $javaCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $java) { $java = "java" }

function Wait-Port([int]$Port, [int]$TimeoutSeconds = 60) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $client = New-Object System.Net.Sockets.TcpClient
            $client.Connect("127.0.0.1", $Port)
            $client.Close()
            return $true
        } catch {
            Start-Sleep -Milliseconds 500
        }
    }
    Write-Warning "Le port $Port n'est pas prêt après $TimeoutSeconds s."
    return $false
}

$services = @(
    @{ Name = "ConfigService";    Dir = "ConfigService";    Jar = "configservice-0.0.1-SNAPSHOT.jar";    Port = 8888; Wait = 60 },
    @{ Name = "DiscoveryService"; Dir = "DiscoveryService"; Jar = "discoveryservice-0.0.1-SNAPSHOT.jar"; Port = 8761; Wait = 60 },
    @{ Name = "ProductService";   Dir = "ProductService";   Jar = "productservice-0.0.1-SNAPSHOT.jar";   Port = 8081; Wait = 60 },
    @{ Name = "OrderService";     Dir = "OrderService";     Jar = "orderservice-0.0.1-SNAPSHOT.jar";     Port = 8084; Wait = 90 },
    @{ Name = "BffService";       Dir = "BffService";       Jar = "bffservice-0.0.1-SNAPSHOT.jar";       Port = 8085; Wait = 60 },
    @{ Name = "ProductFrontend";  Dir = "ProductFrontend";  Jar = "productfrontend-0.0.1-SNAPSHOT.jar";  Port = 8082; Wait = 45 },
    @{ Name = "OrderFrontend";    Dir = "OrderFrontend";    Jar = "orderfrontend-0.0.1-SNAPSHOT.jar";    Port = 8083; Wait = 45 },
    @{ Name = "ProxyService";     Dir = "ProxyService";     Jar = "proxyservice-0.0.1-SNAPSHOT.jar";     Port = 8080; Wait = 45 }
)

foreach ($service in $services) {
    $dir = Join-Path $root $service.Dir
    $jar = Join-Path $dir ("target\" + $service.Jar)
    if (-not (Test-Path $jar)) {
        throw "Jar introuvable : $jar`nLancez d'abord : mvn -f build-all-pom.xml package -DskipTests"
    }

    Write-Host ("Démarrage de {0} (port {1})..." -f $service.Name, $service.Port)
    # Working directory = racine du projet : ConfigService y trouve son dossier myConfig
    Start-Process -FilePath $java -ArgumentList @("-jar", "`"$jar`"") -WorkingDirectory $root -WindowStyle Minimized
    Wait-Port $service.Port $service.Wait | Out-Null
}

Write-Host ""
Write-Host "Services démarrés."
Write-Host "Front-end commandes : http://localhost:8080/app/orders/commandes"
Write-Host "GraphiQL            : http://localhost:8080/graphiql"
Start-Process "http://localhost:8080/app/orders/commandes"
