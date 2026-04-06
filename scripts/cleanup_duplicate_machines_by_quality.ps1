param([string]$ProjectRoot=".")

$ErrorActionPreference = "Continue"

$machinesDir = Join-Path $ProjectRoot "app/src/main/assets/machines"

function NormalizeName($name) {
    if (-not $name) { return "" }

    $n = $name.ToLower()
    $n = $n.Replace(" ", "")
    $n = $n.Replace("　", "")

    return $n
}

function GetScore($json) {
    $score = 0

    if ($json.links) { $score += 10 }
    if ($json.counters) { $score += $json.counters.Count }
    if ($json.settingReferenceValues) { $score += 5 }
    if ($json.ceilingRules) { $score += 3 }

    return $score
}

$files = Get-ChildItem -Path $machinesDir -Filter *.json -File

$groups = @{}

foreach ($f in $files) {
    if ($f.Name -eq "machine_master_manifest.json") { continue }

    try {
        $raw = Get-Content -LiteralPath $f.FullName -Raw -Encoding UTF8
        $j = $raw | ConvertFrom-Json
    } catch {
        Write-Host ("SKIP invalid json: " + $f.Name)
        continue
    }

    $key = NormalizeName $j.name
    if ([string]::IsNullOrWhiteSpace($key)) {
        $key = $f.BaseName.ToLower()
    }

    if (-not $groups.ContainsKey($key)) {
        $groups[$key] = @()
    }

    $groups[$key] += [PSCustomObject]@{
        File  = $f.FullName
        Name  = $j.name
        Id    = $j.id
        Score = GetScore $j
    }
}

foreach ($k in $groups.Keys) {
    $g = @($groups[$k])

    if ($g.Count -le 1) { continue }

    $sorted = $g | Sort-Object -Property @{ Expression = { $_.Score }; Descending = $true }, @{ Expression = { $_.Id }; Descending = $false }

    $keep = $sorted[0]
    $remove = @($sorted | Select-Object -Skip 1)

    Write-Host ("KEEP: " + $keep.Name + " [" + $keep.Id + "] score=" + $keep.Score)

    foreach ($r in $remove) {
        Write-Host ("DELETE: " + $r.Name + " [" + $r.Id + "] score=" + $r.Score)

        if (Test-Path $r.File) {
            Remove-Item -LiteralPath $r.File -Force
        }
    }
}

Write-Host "DONE"