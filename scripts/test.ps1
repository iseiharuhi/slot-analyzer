param(
    [string]$ProjectRoot = "C:\Users\iseih\OneDrive\Desktop\SlotSettingAnalyzerCompleteV2"
)

function Normalize-Name {
    param(
        [string]$Name
    )

    if ([string]::IsNullOrWhiteSpace($Name)) {
        return ""
    }

    $n = $Name.Trim().ToLowerInvariant()

    $n = $n -replace "[\s縲�]", ""
    $n = $n -replace "[~\-_\(\)\[\]/\\\.]", ""
    $n = $n -replace "[繝ｻ:�咯", ""

    $n = $n -replace "^繧ｹ繝槭せ繝ｭ", ""
    $n = $n -replace "^l繝代メ繧ｹ繝ｭ", ""
    $n = $n -replace "^l繧ｹ繝ｭ繝��ヨ", ""
    $n = $n -replace "^l繧ｹ繝ｭ", ""
    $n = $n -replace "^l", ""
    $n = $n -replace "^繝代メ繧ｹ繝ｭ", ""
    $n = $n -replace "^slot", ""

    return $n
}

function Get-Bigrams {
    param(
        [string]$Text
    )

    $result = New-Object System.Collections.Generic.List[string]

    if ([string]::IsNullOrWhiteSpace($Text)) {
        return $result
    }

    if ($Text.Length -eq 1) {
        $result.Add($Text)
        return $result
    }

    for ($i = 0; $i -lt ($Text.Length - 1); $i++) {
        $result.Add($Text.Substring($i, 2))
    }

    return $result
}

function Get-JaccardSimilarity {
    param(
        [string]$A,
        [string]$B
    )

    if ([string]::IsNullOrWhiteSpace($A) -or [string]::IsNullOrWhiteSpace($B)) {
        return 0.0
    }

    $aSet = New-Object 'System.Collections.Generic.HashSet[string]'
    $bSet = New-Object 'System.Collections.Generic.HashSet[string]'

    foreach ($x in (Get-Bigrams -Text $A)) { [void]$aSet.Add($x) }
    foreach ($x in (Get-Bigrams -Text $B)) { [void]$bSet.Add($x) }

    if ($aSet.Count -eq 0 -or $bSet.Count -eq 0) {
        return 0.0
    }

    $intersection = 0
    foreach ($x in $aSet) {
        if ($bSet.Contains($x)) {
            $intersection++
        }
    }

    $union = $aSet.Count + $bSet.Count - $intersection
    if ($union -le 0) {
        return 0.0
    }

    return [double]$intersection / [double]$union
}

$MachinesDir = Join-Path $ProjectRoot "app/src/main/assets/machines"
$ReportPath = Join-Path $ProjectRoot "machine_audit_report_v4.txt"

$files = Get-ChildItem -Path $MachinesDir -Filter *.json | Sort-Object Name
$rows = @()

foreach ($file in $files) {
    if ($file.Name -eq "machine_master_manifest.json") {
        continue
    }

    $raw = $null
    $json = $null
    $broken = $false
    $errorMessage = ""
    $name = ""
    $id = ""
    $structure = "unknown"
    $score = 0
    $normalizedName = ""

    try {
        $raw = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
        $json = $raw | ConvertFrom-Json -ErrorAction Stop
    }
    catch {
        $broken = $true
        $errorMessage = $_.Exception.Message
    }

    if (-not $broken -and $null -ne $json) {
        if ($null -ne $json.machine) {
            $structure = "nested"
            $data = $json.machine
        }
        else {
            $structure = "flat"
            $data = $json
        }

        if ($null -ne $data) {
            $id = [string]$data.id
            $name = [string]$data.name
            $normalizedName = Normalize-Name -Name $name
        }

        if ($id -and $name) { $score += 10 }
        if ($null -ne $json.counterDefinitions -and $json.counterDefinitions.Count -gt 0) { $score += 20 }
        if ($null -ne $json.settingReferenceValues -and $json.settingReferenceValues.Count -gt 0) { $score += 30 }
        if ($null -ne $json.ceilingRules) { $score += 20 }
        if (($null -ne $data.dmmUrl -and $data.dmmUrl -ne "") -or ($null -ne $data.ichigekiUrl -and $data.ichigekiUrl -ne "")) { $score += 10 }
        if ($null -ne $data.notes -and $data.notes -ne "") { $score += 10 }
    }

    $rows += [PSCustomObject]@{
        File           = $file.Name
        Broken         = $broken
        Structure      = $structure
        Id             = $id
        Name           = $name
        NormalizedName = $normalizedName
        Score          = $score
        ErrorMessage   = $errorMessage
    }
}

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("=== MACHINE AUDIT V4 ===")
$lines.Add("files: $($rows.Count)")
$lines.Add("")

$valid = $rows | Where-Object { $_.Broken -eq $false }
$invalid = $rows | Where-Object { $_.Broken -eq $true }

$lines.Add("[VALID COUNT] $($valid.Count)")
$lines.Add("[BROKEN COUNT] $($invalid.Count)")
$lines.Add("")

$lines.Add("[BROKEN FILES]")
if ($invalid.Count -eq 0) {
    $lines.Add("none")
}
else {
    foreach ($r in $invalid) {
        $lines.Add("- $($r.File)")
        $lines.Add("  error: $($r.ErrorMessage)")
    }
}
$lines.Add("")

$lines.Add("[EXACT DUPLICATE NAME GROUPS]")
$exactDupGroups = $valid |
    Where-Object { -not [string]::IsNullOrWhiteSpace($_.Name) } |
    Group-Object Name |
    Where-Object { $_.Count -gt 1 }

if ($exactDupGroups.Count -eq 0) {
    $lines.Add("none")
}
else {
    foreach ($g in $exactDupGroups) {
        $lines.Add("----------------------------------------")
        $lines.Add("name: $($g.Name)")
        $lines.Add("count: $($g.Count)")
        foreach ($item in ($g.Group | Sort-Object @{ Expression = "Score"; Descending = $true }, @{ Expression = "File"; Descending = $false })) {
            $lines.Add("- file: $($item.File)")
            $lines.Add("  id: $($item.Id)")
            $lines.Add("  score: $($item.Score)")
        }
    }
}
$lines.Add("")

$lines.Add("[NORMALIZED DUPLICATE NAME GROUPS]")
$normalizedDupGroups = $valid |
    Where-Object { -not [string]::IsNullOrWhiteSpace($_.NormalizedName) } |
    Group-Object NormalizedName |
    Where-Object { $_.Count -gt 1 } |
    Sort-Object Name

if ($normalizedDupGroups.Count -eq 0) {
    $lines.Add("none")
}
else {
    foreach ($g in $normalizedDupGroups) {
        $lines.Add("----------------------------------------")
        $lines.Add("normalized: $($g.Name)")
        $lines.Add("count: $($g.Count)")
        foreach ($item in ($g.Group | Sort-Object @{ Expression = "Score"; Descending = $true }, @{ Expression = "File"; Descending = $false })) {
            $lines.Add("- file: $($item.File)")
            $lines.Add("  name: $($item.Name)")
            $lines.Add("  id: $($item.Id)")
            $lines.Add("  score: $($item.Score)")
        }
    }
}
$lines.Add("")

$lines.Add("[SIMILAR NAME CANDIDATES]")
$similarPairs = New-Object System.Collections.Generic.List[object]

for ($i = 0; $i -lt $valid.Count; $i++) {
    for ($j = $i + 1; $j -lt $valid.Count; $j++) {
        $a = $valid[$i]
        $b = $valid[$j]

        if ([string]::IsNullOrWhiteSpace($a.NormalizedName) -or [string]::IsNullOrWhiteSpace($b.NormalizedName)) {
            continue
        }

        if ($a.NormalizedName -eq $b.NormalizedName) {
            continue
        }

        $sim = Get-JaccardSimilarity -A $a.NormalizedName -B $b.NormalizedName

        if ($sim -ge 0.55) {
            $similarPairs.Add([PSCustomObject]@{
                Similarity = [Math]::Round($sim, 3)
                FileA = $a.File
                NameA = $a.Name
                FileB = $b.File
                NameB = $b.Name
            })
        }
    }
}

$sortedPairs = $similarPairs | Sort-Object `
    @{ Expression = "Similarity"; Descending = $true }, `
    @{ Expression = "FileA"; Descending = $false }, `
    @{ Expression = "FileB"; Descending = $false }

if ($sortedPairs.Count -eq 0) {
    $lines.Add("none")
}
else {
    foreach ($p in $sortedPairs) {
        $lines.Add("----------------------------------------")
        $lines.Add("similarity: $($p.Similarity)")
        $lines.Add("- A: $($p.FileA)")
        $lines.Add("  name: $($p.NameA)")
        $lines.Add("- B: $($p.FileB)")
        $lines.Add("  name: $($p.NameB)")
    }
}

[System.IO.File]::WriteAllLines($ReportPath, $lines, [System.Text.Encoding]::UTF8)
Write-Host "done: $ReportPath"