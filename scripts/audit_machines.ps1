# Machine data audit script.
# Usage: powershell -ExecutionPolicy Bypass -File .\scripts\audit_machines.ps1
# Scans app/src/main/assets/machines/*.json and reports:
#   - broken/unparseable JSON
#   - duplicate machine id / exact name / normalized-name collisions
#   - missing or bare (top-page-only) dmmUrl / ichigekiUrl
#   - generic placeholder-style filenames (dmm_slot_NNN, list_slot_NNN) that should be renamed
#   - partial/draft machines still needing precision follow-up
# Writes reports/machine_audit_report_<date>.md and prints a one-line summary.

$ErrorActionPreference = 'Stop'
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
$dir = Join-Path $repoRoot 'app\src\main\assets\machines'
$files = Get-ChildItem $dir -Filter *.json | Where-Object { $_.Name -ne 'machine_master_manifest.json' } | Sort-Object Name

$records = @()
$broken = @()

foreach ($f in $files) {
    try {
        $json = Get-Content $f.FullName -Raw -Encoding UTF8 | ConvertFrom-Json
    } catch {
        $broken += [pscustomobject]@{ file = $f.Name; error = $_.Exception.Message }
        continue
    }
    $m = $json.machine
    if (-not $m) {
        $broken += [pscustomobject]@{ file = $f.Name; error = 'no "machine" object' }
        continue
    }
    $notes = [string]$m.notes
    $status = 'unknown'
    if ($notes -match '^(?i)partial') { $status = 'partial' }
    elseif ($notes -match '^(?i)draft') { $status = 'draft' }
    elseif ($notes) { $status = 'other' }
    else { $status = 'empty' }

    $records += [pscustomobject]@{
        file          = $f.Name
        id            = $m.id
        name          = $m.name
        isActive      = $m.isActive
        status        = $status
        dmmUrl        = $m.dmmUrl
        ichigekiUrl   = $m.ichigekiUrl
        hasCeiling    = [bool]($json.ceilingRules -and $json.ceilingRules.Count -gt 0)
        hasRefValues  = [bool]($json.settingReferenceValues -and $json.settingReferenceValues.Count -gt 0)
    }

    # keep filename and internal id in sync — a mismatch here silently breaks
    # anything that looks machines up by id (sessions, history, etc.)
    if ($m.id -ne $f.BaseName) {
        $broken += [pscustomobject]@{ file = $f.Name; error = "filename/id mismatch: internal id is `"$($m.id)`"" }
    }
}

# normalize helper: strip common decorations for duplicate detection
function Normalize-Name($name) {
    if (-not $name) { return '' }
    $n = $name
    $n = $n -replace '^[LＬ]', ''
    $n = $n -replace '(?i)スマスロ', ''
    $n = $n -replace '(?i)パチスロ', ''
    $n = $n -replace '(?i)スロット', ''
    $n = $n -replace '[\s　]', ''
    $n = $n -replace '[〜～]', ''
    $n = $n -replace '[:：]', ''
    $n = $n.ToLowerInvariant()
    return $n
}

$byExact = $records | Group-Object name | Where-Object { $_.Count -gt 1 }
$byNorm = $records | Group-Object { Normalize-Name $_.name } | Where-Object { $_.Count -gt 1 -and $_.Name -ne '' }

$idDupes = $records | Group-Object id | Where-Object { $_.Count -gt 1 }

$missingDmm = $records | Where-Object { -not $_.dmmUrl -or $_.dmmUrl -eq '' }
$missingIchigeki = $records | Where-Object { -not $_.ichigekiUrl -or $_.ichigekiUrl -eq '' }
$bareDmm = $records | Where-Object { $_.dmmUrl -match '^https?://p-town\.dmm\.com/?$' }
$bareIchigeki = $records | Where-Object { $_.ichigekiUrl -match '^https?://1geki\.jp/?$' }

$statusCounts = $records | Group-Object status | Select-Object Name, Count

# generic/placeholder-style filename check (dmm_slot_NNN, list_slot_NNN, etc.)
# NOTE for future machine additions: do NOT name new files dmm_slot_NNN / list_slot_NNN
# style placeholders. Use a descriptive romanized snake_case slug that matches how the
# rest of the catalog is named (e.g. "tensei_slime", "hokuto_tensei_2"), and make sure
# the internal "id" field matches the filename exactly. Before adding a new machine,
# check it isn't already covered under a different slug (see duplicate sections above).
$genericPattern = '^(dmm|list)_slot_\d+$'
$genericNamed = $records | Where-Object { $_.id -match $genericPattern }

$dateStamp = Get-Date -Format 'yyyy-MM-dd'
$outPath = Join-Path $repoRoot "reports\machine_audit_report_$dateStamp.md"

$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine("# Machine Audit Report ($dateStamp)")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("対象: app/src/main/assets/machines/*.json (manifest除く)")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("- files scanned: $($files.Count)")
[void]$sb.AppendLine("- valid: $($records.Count)")
[void]$sb.AppendLine("- broken: $($broken.Count)")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("## Status breakdown")
foreach ($s in $statusCounts) { [void]$sb.AppendLine("- $($s.Name): $($s.Count)") }
[void]$sb.AppendLine("")
[void]$sb.AppendLine("## Broken files / filename-id mismatches")
if ($broken.Count -eq 0) { [void]$sb.AppendLine("none") }
else { foreach ($b in $broken) { [void]$sb.AppendLine("- $($b.file): $($b.error)") } }
[void]$sb.AppendLine("")
[void]$sb.AppendLine("## Duplicate machine id")
if ($idDupes.Count -eq 0) { [void]$sb.AppendLine("none") }
else { foreach ($g in $idDupes) { [void]$sb.AppendLine("- id=$($g.Name): $(($g.Group | ForEach-Object { $_.file }) -join ', ')") } }
[void]$sb.AppendLine("")
[void]$sb.AppendLine("## Exact duplicate names")
if ($byExact.Count -eq 0) { [void]$sb.AppendLine("none") }
else { foreach ($g in $byExact) { [void]$sb.AppendLine("- name=`"$($g.Name)`": $(($g.Group | ForEach-Object { $_.file }) -join ', ')") } }
[void]$sb.AppendLine("")
[void]$sb.AppendLine("## Normalized duplicate names (verify manually — this heuristic has false positives)")
if ($byNorm.Count -eq 0) { [void]$sb.AppendLine("none") }
else {
    foreach ($g in $byNorm) {
        [void]$sb.AppendLine("- normalized=`"$($g.Name)`":")
        foreach ($r in $g.Group) { [void]$sb.AppendLine("  - $($r.file) : $($r.name)") }
    }
}
[void]$sb.AppendLine("")
[void]$sb.AppendLine("## Missing / bare URLs")
[void]$sb.AppendLine("- missing dmmUrl: $($missingDmm.Count)")
foreach ($r in $missingDmm) { [void]$sb.AppendLine("  - $($r.file)") }
[void]$sb.AppendLine("- missing ichigekiUrl: $($missingIchigeki.Count)")
foreach ($r in $missingIchigeki) { [void]$sb.AppendLine("  - $($r.file)") }
[void]$sb.AppendLine("- bare (top page) dmmUrl: $($bareDmm.Count)")
foreach ($r in $bareDmm) { [void]$sb.AppendLine("  - $($r.file)") }
[void]$sb.AppendLine("- bare (top page) ichigekiUrl: $($bareIchigeki.Count)")
foreach ($r in $bareIchigeki) { [void]$sb.AppendLine("  - $($r.file)") }
[void]$sb.AppendLine("")
[void]$sb.AppendLine("## Generic/placeholder-style filenames (should be renamed to descriptive slugs)")
if ($genericNamed.Count -eq 0) { [void]$sb.AppendLine("none") }
else { foreach ($r in $genericNamed) { [void]$sb.AppendLine("- $($r.file) : $($r.name)") } }
[void]$sb.AppendLine("")
[void]$sb.AppendLine("## Partial / draft machines (need precision follow-up)")
$needsWork = $records | Where-Object { $_.status -eq 'partial' -or $_.status -eq 'draft' } | Sort-Object file
foreach ($r in $needsWork) {
    [void]$sb.AppendLine("- $($r.file) [$($r.status)] $($r.name) (ceiling=$($r.hasCeiling), refValues=$($r.hasRefValues))")
}

$sb.ToString() | Out-File -FilePath $outPath -Encoding utf8
Write-Output "WROTE: $outPath"
Write-Output "files=$($files.Count) valid=$($records.Count) broken=$($broken.Count) exactDupe=$($byExact.Count) normDupe=$($byNorm.Count) idDupe=$($idDupes.Count) missingDmm=$($missingDmm.Count) missingIchigeki=$($missingIchigeki.Count) bareDmm=$($bareDmm.Count) bareIchigeki=$($bareIchigeki.Count) genericNamed=$($genericNamed.Count)"
