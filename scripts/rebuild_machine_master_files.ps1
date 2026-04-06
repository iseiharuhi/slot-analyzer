param(
    [string]$ProjectRoot = "."
)

$ErrorActionPreference = "Stop"

function Load-JsonFile {
    param([string]$Path)

    $raw = Get-Content -LiteralPath $Path -Raw -Encoding UTF8
    return ($raw | ConvertFrom-Json)
}

function Save-JsonUtf8NoBom {
    param(
        [string]$Path,
        $Object,
        [int]$Depth = 100
    )

    $json = $Object | ConvertTo-Json -Depth $Depth
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $json + [Environment]::NewLine, $utf8NoBom)
}

function Get-MachineId {
    param($Json)

    if ($null -ne $Json.id -and -not [string]::IsNullOrWhiteSpace([string]$Json.id)) {
        return [string]$Json.id
    }

    if ($null -ne $Json.machine) {
        if ($null -ne $Json.machine.id -and -not [string]::IsNullOrWhiteSpace([string]$Json.machine.id)) {
            return [string]$Json.machine.id
        }
    }

    return $null
}

function Get-MachineName {
    param($Json)

    if ($null -ne $Json.name -and -not [string]::IsNullOrWhiteSpace([string]$Json.name)) {
        return [string]$Json.name
    }

    if ($null -ne $Json.machine) {
        if ($null -ne $Json.machine.name -and -not [string]::IsNullOrWhiteSpace([string]$Json.machine.name)) {
            return [string]$Json.machine.name
        }
    }

    return ""
}

$resolvedRoot = (Resolve-Path $ProjectRoot).Path
$machinesDir = Join-Path $resolvedRoot "app\src\main\assets\machines"
$masterDir = Join-Path $resolvedRoot "app\src\main\assets\master"
$detailsDir = Join-Path $masterDir "machine_details"
$manifestPath = Join-Path $machinesDir "machine_master_manifest.json"
$masterMachinesPath = Join-Path $masterDir "machines.json"

if (-not (Test-Path $machinesDir)) {
    throw ("machines directory not found: " + $machinesDir)
}

if (-not (Test-Path $masterDir)) {
    throw ("master directory not found: " + $masterDir)
}

if (-not (Test-Path $detailsDir)) {
    New-Item -ItemType Directory -Path $detailsDir | Out-Null
}

$machineFiles = Get-ChildItem -Path $machinesDir -Filter *.json -File |
    Where-Object { $_.Name -ne "machine_master_manifest.json" } |
    Sort-Object -Property Name

$manifestEntries = New-Object System.Collections.ArrayList
$masterMachines = New-Object System.Collections.ArrayList
$validIds = New-Object System.Collections.Generic.HashSet[string]

foreach ($file in $machineFiles) {
    try {
        $json = Load-JsonFile -Path $file.FullName
    }
    catch {
        Write-Host ("SKIP invalid json: " + $file.Name)
        continue
    }

    $id = Get-MachineId -Json $json
    $name = Get-MachineName -Json $json

    if ([string]::IsNullOrWhiteSpace($id)) {
        Write-Host ("SKIP missing id: " + $file.Name)
        continue
    }

    [void]$validIds.Add($id)

    [void]$manifestEntries.Add([ordered]@{
        machineId = $id
        fileName = $file.Name
        name = $name
    })

    [void]$masterMachines.Add([ordered]@{
        id = $id
        name = $name
    })
}

$manifestObject = [ordered]@{
    schemaVersion = "1"
    masterVersion = "1"
    generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
    count = $manifestEntries.Count
    machines = @($manifestEntries)
}

Save-JsonUtf8NoBom -Path $manifestPath -Object $manifestObject -Depth 10
Save-JsonUtf8NoBom -Path $masterMachinesPath -Object @($masterMachines) -Depth 10

$detailFiles = Get-ChildItem -Path $detailsDir -Filter *.json -File
foreach ($detailFile in $detailFiles) {
    $detailId = $detailFile.BaseName
    if (-not $validIds.Contains($detailId)) {
        Remove-Item -LiteralPath $detailFile.FullName -Force
        Write-Host ("DELETE detail: " + $detailFile.Name)
    }
}

Write-Host ("Manifest count: " + $manifestEntries.Count)
Write-Host ("Master count: " + $masterMachines.Count)
Write-Host "DONE"