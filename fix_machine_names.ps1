param(
    [string]$ProjectRoot = "."
)

$ErrorActionPreference = "Stop"

Write-Host "=== machine name fix script ==="
Write-Host "ProjectRoot: $ProjectRoot"
Write-Host ""

$targets = @(
    @{
        path = "app/src/main/assets/machines/list_slot_004.json"
        newName = "パチスロ コードギアス 反逆のルルーシュ3 C.C.&Kallen ver."
    },
    @{
        path = "app/src/main/assets/machines/list_slot_003.json"
        newName = "スマスロ コードギアス 反逆のルルーシュ／復活のルルーシュ"
    },
    @{
        path = "app/src/main/assets/machines/list_slot_040.json"
        newName = "Lルパン三世 大航海者の秘宝"
    }
)

foreach ($t in $targets) {
    $fullPath = Join-Path $ProjectRoot $t.path

    if (Test-Path $fullPath) {
        $json = Get-Content $fullPath -Raw -Encoding UTF8 | ConvertFrom-Json

        $oldName = $json.machine.name
        $json.machine.name = $t.newName

        $json | ConvertTo-Json -Depth 100 | Out-File $fullPath -Encoding utf8

        Write-Host "[UPDATED] $($t.path)"
        Write-Host "  $oldName -> $($t.newName)"
    }
    else {
        Write-Host "[SKIPPED] Not found: $($t.path)"
    }
}

Write-Host ""
Write-Host "Done."
