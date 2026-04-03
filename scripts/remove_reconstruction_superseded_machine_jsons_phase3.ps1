param(
    [switch]$WhatIf
)

$ErrorActionPreference = "Stop"

$targets = @(
    "app/src/main/assets/machines/code_geass_cc_kallen.json"
    "app/src/main/assets/machines/eureka_zero.json"
    "app/src/main/assets/machines/ghost_shell.json"
    "app/src/main/assets/machines/kabaneri_kaimon.json"
    "app/src/main/assets/machines/kengan.json"
    "app/src/main/assets/machines/lovejo3.json"
    "app/src/main/assets/machines/madoka_forte.json"
    "app/src/main/assets/machines/monkey_turn_v.json"
    "app/src/main/assets/machines/ring_v.json"
    "app/src/main/assets/machines/toaru.json"
    "app/src/main/assets/machines/lupin.json"
)

$projectRoot = Get-Location

Write-Host "Project root: $projectRoot"
Write-Host ""

foreach ($relativePath in $targets) {
    $fullPath = Join-Path $projectRoot $relativePath
    if (Test-Path $fullPath) {
        if ($WhatIf) {
            Write-Host "[WhatIf] Would remove: $relativePath"
        } else {
            Remove-Item $fullPath -Force
            Write-Host "Removed: $relativePath"
        }
    } else {
        Write-Host "Skip (not found): $relativePath"
    }
}

Write-Host ""
if ($WhatIf) {
    Write-Host "Dry run complete."
} else {
    Write-Host "Removal complete."
}
