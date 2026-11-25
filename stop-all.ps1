# Arrête les services du TP4, qu'ils soient lancés par start-all.ps1
# (java -jar) ou par les configurations IntelliJ (classe principale).
$jarPatterns = "configservice-|discoveryservice-|productservice-|orderservice-|bffservice-|productfrontend-|orderfrontend-|proxyservice-"
$classPatterns = "isetjb\.soa\.tpmicro\.(configservice|discoveryservice|productservice|orderservice|bffservice|productfrontend|orderfrontend|proxyservice)\."

$processes = Get-CimInstance Win32_Process |
    Where-Object {
        $_.Name -eq "java.exe" -and $_.CommandLine -and
        ($_.CommandLine -match $jarPatterns -or $_.CommandLine -match $classPatterns)
    }

if (-not $processes) {
    Write-Host "Aucun service TP4 en cours d'exécution."
    return
}

foreach ($process in $processes) {
    Write-Host ("Arrêt de {0} (PID {1})..." -f $process.Name, $process.ProcessId)
    Stop-Process -Id $process.ProcessId -Force -ErrorAction SilentlyContinue
}

Write-Host "Services arrêtés."
