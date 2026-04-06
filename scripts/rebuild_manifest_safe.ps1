param(
    [string]$ProjectRoot = "C:\Users\iseih\OneDrive\Desktop\SlotSettingAnalyzerCompleteV2",
    [switch]$RemoveSelectedDuplicates
)

$MachinesDir = Join-Path $ProjectRoot "app/src/main/assets/machines"
$ManifestPath = Join-Path $MachinesDir "machine_master_manifest.json"

$deleteTargets = @(
    "dmm_slot_016.json",
    "lovejo3.json",
    "kengan.json",
    "list_slot_038.json",
    "list_slot_056.json",
    "list_slot_057.json",
    "idolmaster.json"
)

function Get-FileSha256 {
    param([string]$Path)

    $sha = [System.Security.Cryptography.SHA256]::Create()
    try {
        $stream = [System.IO.File]::OpenRead($Path)
        try {
            $hashBytes = $sha.ComputeHash($stream)
        }
        finally {
            $stream.Dispose()
        }
        return ([System.BitConverter]::ToString($hashBytes)).Replace("-", "").ToLowerInvariant()
    }
    finally {
        $sha.Dispose()
    }
}

function Read-MachineJson {
    param([string]$Path)

    try {
        $raw = [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
        return $raw | ConvertFrom-Json -ErrorAction Stop
    }
    catch {
        Write-Warning "JSON read failed: $Path"
        return $null
    }
}

if (-not (Test-Path $MachinesDir)) {
    throw "Machines directory not found: $MachinesDir"
}

if ($RemoveSelectedDuplicates) {
    Write-Host "=== REMOVE SELECTED DUPLICATES ==="
    foreach ($name in $deleteTargets) {
        $path = Join-Path $MachinesDir $name
        if (Test-Path $path) {
            Remove-Item $path -Force
            Write-Host "Deleted: $name"
        } else {
            Write-Host "Skip (not found): $name"
        }
    }
    Write-Host ""
}

Write-Host "=== REBUILD MANIFEST ==="

$machineItems = New-Object System.Collections.Generic.List[object]

Get-ChildItem -Path $MachinesDir -Filter *.json |
    Where-Object { $_.Name -ne "machine_master_manifest.json" } |
    Sort-Object Name |
    ForEach-Object {
        $json = Read-MachineJson -Path $_.FullName
        if ($null -eq $json) { return }

        $machine = if ($null -ne $json.machine) { $json.machine } else { $json }

        $machineId = [string]$machine.id
        if ([string]::IsNullOrWhiteSpace($machineId)) {
            Write-Warning "Skipped due to missing machine.id: $($_.Name)"
            return
        }

        $checksum = Get-FileSha256 -Path $_.FullName

        $machineItems.Add([PSCustomObject]@{
            machineId = $machineId
            fileName  = $_.Name
            checksum  = $checksum
        })
    }

$masterVersion = "rebuilt-" + (Get-Date).ToString("yyyy-MM-dd_HH-mm-ss")

$manifestObject = [ordered]@{
    schemaVersion = 1
    masterVersion = $masterVersion
    machines      = @($machineItems)
}

$jsonText = $manifestObject | ConvertTo-Json -Depth 6

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($ManifestPath, $jsonText, $utf8NoBom)

Write-Host "Manifest rebuilt successfully:"
Write-Host $ManifestPath
Write-Host "machineCount: $($machineItems.Count)"
Write-Host "masterVersion: $masterVersion"
