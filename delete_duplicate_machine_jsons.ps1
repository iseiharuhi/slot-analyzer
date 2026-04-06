param(
    [string]$ProjectRoot = "."
)

$ErrorActionPreference = "Stop"

$targets = @(
    "app/src/main/assets/machines/chibariyo2_plus.json",
    "app/src/main/assets/machines/dragon_hanahana_30.json",
    "app/src/main/assets/machines/king_hanahana_30.json",
    "app/src/main/assets/machines/oki_doki_duo.json",
    "app/src/main/assets/machines/oki_doki_gold.json",
    "app/src/main/assets/machines/l_hokuto.json",
    "app/src/main/assets/machines/code_geass_cc_kallen.json",
    "app/src/main/assets/machines/code_geass_fukkatsu.json",
    "app/src/main/assets/machines/lupin.json"
)

Write-Host "=== machine duplicate cleanup delete script ==="
Write-Host "ProjectRoot: $ProjectRoot"
Write-Host ""

foreach ($relativePath in $targets) {
    $fullPath = Join-Path $ProjectRoot $relativePath

    if (Test-Path $fullPath) {
        Remove-Item $fullPath -Force
        Write-Host "[DELETED] $relativePath"
    }
    else {
        Write-Host "[SKIPPED] Not found: $relativePath"
    }
}

Write-Host ""
Write-Host "Done."
