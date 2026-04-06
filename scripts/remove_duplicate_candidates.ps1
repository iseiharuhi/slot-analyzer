param(
    [string]$ProjectRoot = "C:\Users\iseih\OneDrive\Desktop\SlotSettingAnalyzerCompleteV2",
    [switch]$Execute
)

$MachinesDir = Join-Path $ProjectRoot "app/src/main/assets/machines"

$deleteTargets = @(
    "karakuri.json",
    "dmm_slot_034.json",
    "dmm_slot_027.json",
    "dmm_slot_024.json",
    "vvv.json",
    "tokyoghoul.json",
    "list_slot_061.json"
)

Write-Host "=== Duplicate cleanup candidate script ==="
Write-Host "ProjectRoot : $ProjectRoot"
Write-Host "MachinesDir  : $MachinesDir"
Write-Host ""

$found = @()
$missing = @()

foreach ($name in $deleteTargets) {
    $path = Join-Path $MachinesDir $name
    if (Test-Path $path) {
        $found += $path
    }
    else {
        $missing += $path
    }
}

Write-Host "[FOUND FILES]"
if ($found.Count -eq 0) {
    Write-Host "none"
}
else {
    foreach ($path in $found) {
        Write-Host $path
    }
}

Write-Host ""
Write-Host "[MISSING FILES]"
if ($missing.Count -eq 0) {
    Write-Host "none"
}
else {
    foreach ($path in $missing) {
        Write-Host $path
    }
}

Write-Host ""
if (-not $Execute) {
    Write-Host "Dry run only. No files were deleted."
    Write-Host "To execute deletion, run with -Execute"
    exit 0
}

Write-Host "Executing deletion..."
foreach ($path in $found) {
    Remove-Item $path -Force
    Write-Host "Deleted: $path"
}

Write-Host ""
Write-Host "Done."