param(
    [string]$ProjectRoot = "C:\Users\iseih\OneDrive\Desktop\SlotSettingAnalyzerCompleteV2"
)

$MachinesDir = Join-Path $ProjectRoot "app/src/main/assets/machines"
$ManifestPath = Join-Path $MachinesDir "machine_master_manifest.json"

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

$machineItems = New-Object System.Collections.Generic.List[object]

Get-ChildItem -Path $MachinesDir -Filter *.json |
    Where-Object { $_.Name -ne "machine_master_manifest.json" } |
    Sort-Object Name |
    ForEach-Object {
        $json = Read-MachineJson -Path $_.FullName
        if ($null -eq $json) { return }

        $machine = if ($null -ne $json.machine) { $json.machine } else { $json }

        $id = [string]$machine.id
        $name = [string]$machine.name
        $releaseDate = [string]$machine.releaseDate
        $isActive = if ($null -ne $machine.isActive) { [bool]$machine.isActive } else { $true }

        if ([string]::IsNullOrWhiteSpace($id) -or [string]::IsNullOrWhiteSpace($name)) {
            Write-Warning "Skipped due to missing id/name: $($_.Name)"
            return
        }

        $machineItems.Add([PSCustomObject]@{
            machineId   = $id
            name        = $name
            fileName    = $_.Name
            releaseDate = $releaseDate
            isActive    = $isActive
        })
    }

$manifestObject = [ordered]@{
    version      = (Get-Date).ToString("yyyy-MM-dd_HH-mm-ss")
    machineCount = $machineItems.Count
    machines     = @($machineItems)
}

$jsonText = $manifestObject | ConvertTo-Json -Depth 6

# UTF-8 BOM縺ｪ縺�
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($ManifestPath, $jsonText, $utf8NoBom)

Write-Host "Manifest rebuilt successfully:"
Write-Host $ManifestPath
Write-Host "machineCount: $($machineItems.Count)"