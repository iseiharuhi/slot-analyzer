param(
    [string]$ProjectRoot = "C:\Users\iseih\OneDrive\Desktop\SlotSettingAnalyzerCompleteV2"
)

$MachinesDir = Join-Path $ProjectRoot "app/src/main/assets/machines"
$ManifestPath = Join-Path $MachinesDir "machine_master_manifest.json"

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

if ($machineItems.Count -eq 0) {
    throw "No machine items were collected. Manifest was not written."
}

$manifestObject = [ordered]@{
    schemaVersion = 1
    masterVersion = (Get-Date).ToString("yyyy-MM-dd_HH-mm-ss")
    machines      = @($machineItems)
}

$jsonText = $manifestObject | ConvertTo-Json -Depth 6
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($ManifestPath, $jsonText, $utf8NoBom)

Write-Host "Manifest rebuilt successfully:"
Write-Host $ManifestPath
Write-Host "machineCount: $($machineItems.Count)"
