param(
    [string]$ProjectRoot = "."
)

$ErrorActionPreference = "Stop"

$machinesDir = Join-Path $ProjectRoot "app/src/main/assets/machines"

if (-not (Test-Path $machinesDir)) {
    Write-Host "Machines directory not found: $machinesDir"
    exit 1
}

$bakFiles = Get-ChildItem -Path $machinesDir -Filter "*.json.bak" -File

if ($bakFiles.Count -eq 0) {
    Write-Host "No .json.bak files found in: $machinesDir"
    exit 0
}

$deleted = 0
foreach ($file in $bakFiles) {
    Remove-Item -LiteralPath $file.FullName -Force
    Write-Host "Deleted: $($file.Name)"
    $deleted++
}

Write-Host ""
Write-Host "Deleted total: $deleted"
