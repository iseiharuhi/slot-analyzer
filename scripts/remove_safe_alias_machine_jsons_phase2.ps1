param(
    [switch]$WhatIf
)

$ErrorActionPreference = 'Stop'

$targets = @(
    'app/src/main/assets/machines/karakuri.json',
    'app/src/main/assets/machines/tokyoghoul.json',
    'app/src/main/assets/machines/vvv.json'
)

Write-Host '=== Safe alias machine JSON cleanup (phase2) ==='
Write-Host "Project root: $(Get-Location)"

foreach ($relativePath in $targets) {
    $fullPath = Join-Path (Get-Location) $relativePath
    if (Test-Path $fullPath) {
        if ($WhatIf) {
            Write-Host "[WhatIf] Remove: $relativePath"
        }
        else {
            Remove-Item $fullPath -Force
            Write-Host "Removed: $relativePath"
        }
    }
    else {
        Write-Host "Skip (not found): $relativePath"
    }
}

Write-Host 'Done.'
