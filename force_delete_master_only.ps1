param(
    [string]$ProjectRoot = "."
)

$BaseDir = Join-Path $ProjectRoot "app/src/main/assets/master/machine_details"

if (-not (Test-Path $BaseDir)) {
    Write-Host "Base directory not found"
    exit
}

$files = Get-ChildItem $BaseDir -Filter *.json

$deleted = 0

foreach ($file in $files) {
    # machines側に存在しないものだけ削除
    $machinePath = Join-Path $ProjectRoot "app/src/main/assets/machines/$($file.Name)"

    if (-not (Test-Path $machinePath)) {
        Remove-Item $file.FullName -Force
        Write-Host "Deleted: $($file.Name)"
        $deleted++
    }
}

Write-Host ""
Write-Host "Deleted total: $deleted"