param(
    [string]$ProjectRoot = "C:\Users\iseih\OneDrive\Desktop\SlotSettingAnalyzerCompleteV2",
    [switch]$Execute,
    [switch]$RebuildManifest
)

$MachinesDir = Join-Path $ProjectRoot "app/src/main/assets/machines"
$ManifestPath = Join-Path $MachinesDir "machine_master_manifest.json"

$targets = @(
    "dmm_slot_016.json",
    "lovejo3.json",
    "kengan.json",
    "list_slot_038.json",
    "list_slot_056.json",
    "list_slot_057.json",
    "idolmaster.json"
)

function Read-MachineJson {
    param(
        [string]$Path
    )

    try {
        $raw = [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
        return $raw | ConvertFrom-Json -ErrorAction Stop
    }
    catch {
        Write-Warning "JSON read failed: $Path"
        return $null
    }
}

function Get-MachineInfo {
    param(
        [string]$Path
    )

    $json = Read-MachineJson -Path $Path
    if ($null -eq $json) {
        return $null
    }

    $machine = if ($null -ne $json.machine) { $json.machine } else { $json }

    return [PSCustomObject]@{
        fileName     = [System.IO.Path]::GetFileName($Path)
        id           = [string]$machine.id
        name         = [string]$machine.name
        releaseDate  = [string]$machine.releaseDate
        isActive     = if ($null -ne $machine.isActive) { [bool]$machine.isActive } else { $true }
    }
}

function Rebuild-Manifest {
    param(
        [string]$MachinesDir,
        [string]$ManifestPath
    )

    Write-Host ""
    Write-Host "=== REBUILD MANIFEST ==="

    $files = Get-ChildItem -Path $MachinesDir -Filter *.json |
        Where-Object { $_.Name -ne "machine_master_manifest.json" } |
        Sort-Object Name

    $items = New-Object System.Collections.Generic.List[object]

    foreach ($file in $files) {
        $info = Get-MachineInfo -Path $file.FullName
        if ($null -eq $info) {
            Write-Warning "Skipped from manifest: $($file.Name)"
            continue
        }

        if ([string]::IsNullOrWhiteSpace($info.id) -or [string]::IsNullOrWhiteSpace($info.name)) {
            Write-Warning "Skipped from manifest due to missing id/name: $($file.Name)"
            continue
        }

        $items.Add([PSCustomObject]@{
            id          = $info.id
            name        = $info.name
            fileName    = $info.fileName
            releaseDate = $info.releaseDate
            isActive    = $info.isActive
        })
    }

    $manifestObject = [PSCustomObject]@{
        version      = (Get-Date).ToString("yyyy-MM-dd_HH-mm-ss")
        machineCount = $items.Count
        machines     = @($items)
    }

    $json = $manifestObject | ConvertTo-Json -Depth 6
    [System.IO.File]::WriteAllText($ManifestPath, $json, [System.Text.Encoding]::UTF8)

    Write-Host "Manifest rebuilt:"
    Write-Host $ManifestPath
    Write-Host "machineCount: $($items.Count)"
}

Write-Host "=== DELETE TARGET CHECK ==="
Write-Host "ProjectRoot : $ProjectRoot"
Write-Host "MachinesDir  : $MachinesDir"
Write-Host "ManifestPath : $ManifestPath"
Write-Host ""

$found = @()
$missing = @()

foreach ($t in $targets) {
    $path = Join-Path $MachinesDir $t
    if (Test-Path $path) {
        $info = Get-MachineInfo -Path $path
        if ($null -ne $info) {
            Write-Host ("FOUND: {0} | id={1} | name={2}" -f $t, $info.id, $info.name)
        }
        else {
            Write-Host "FOUND: $t | id/name read failed"
        }
        $found += $path
    }
    else {
        Write-Host "NOT FOUND: $t"
        $missing += $path
    }
}

if (-not $Execute) {
    Write-Host ""
    Write-Host "Dry run only."
    Write-Host "Delete only:"
    Write-Host 'powershell -ExecutionPolicy Bypass -File .\scripts\remove_selected_duplicates.ps1 -Execute'
    Write-Host ""
    Write-Host "Delete + rebuild manifest:"
    Write-Host 'powershell -ExecutionPolicy Bypass -File .\scripts\remove_selected_duplicates.ps1 -Execute -RebuildManifest'
    exit 0
}

Write-Host ""
Write-Host "=== DELETING ==="

foreach ($f in $found) {
    Remove-Item $f -Force
    Write-Host "Deleted: $f"
}

Write-Host "Delete complete."

if ($RebuildManifest) {
    Rebuild-Manifest -MachinesDir $MachinesDir -ManifestPath $ManifestPath
}
else {
    Write-Host ""
    Write-Host "Manifest was not rebuilt."
    Write-Host "Run again with -RebuildManifest if needed."
}