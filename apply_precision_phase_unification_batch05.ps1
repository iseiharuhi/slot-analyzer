$ErrorActionPreference = "Stop"

$baseDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$machinesDir = Join-Path $baseDir "app\src\main\assets\machines"

function Backup-File {
    param([string]$FilePath)
    if (Test-Path $FilePath) {
        Copy-Item $FilePath "$FilePath.bak" -Force
    }
}

function Read-JsonFile {
    param([string]$Path)
    $text = [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
    return $text | ConvertFrom-Json
}

function Write-JsonUtf8 {
    param(
        [string]$Path,
        [Parameter(ValueFromPipeline = $true)]$Data
    )
    Backup-File -FilePath $Path
    $json = $Data | ConvertTo-Json -Depth 100
    [System.IO.File]::WriteAllText($Path, $json, [System.Text.UTF8Encoding]::new($false))
}

function Update-RefValues {
    param(
        [object[]]$Values,
        [double]$Weight,
        [int]$MinSampleSize,
        [string]$Note
    )

    $updated = @()
    foreach ($v in $Values) {
        $updated += [PSCustomObject]@{
            settingNo = $v.settingNo
            denominatorValue = $v.denominatorValue
            weight = $Weight
            minSampleSize = $MinSampleSize
            note = $Note
        }
    }
    return $updated
}

function New-RefEntryFromExisting {
    param(
        [object[]]$ExistingRefs,
        [string]$CounterKey,
        [double]$Weight,
        [int]$MinSampleSize,
        [string]$Note
    )

    $src = $ExistingRefs | Where-Object { $_.counterKey -eq $CounterKey } | Select-Object -First 1
    if ($null -eq $src) {
        return $null
    }

    return [PSCustomObject]@{
        counterKey = $CounterKey
        values = Update-RefValues -Values $src.values -Weight $Weight -MinSampleSize $MinSampleSize -Note $Note
    }
}

function Update-MachineJson {
    param(
        [string]$FileName,
        [string]$MachineNotes,
        [object[]]$RefConfigs
    )

    $path = Join-Path $machinesDir $FileName
    if (-not (Test-Path $path)) {
        Write-Host "SKIP (not found): $FileName"
        return
    }

    $data = Read-JsonFile -Path $path
    $existingRefs = @($data.settingReferenceValues)

    if ($null -ne $data.machine) {
        $data.machine.notes = $MachineNotes
    }

    $newRefs = @()
    foreach ($cfg in $RefConfigs) {
        $entry = New-RefEntryFromExisting `
            -ExistingRefs $existingRefs `
            -CounterKey $cfg.counterKey `
            -Weight $cfg.weight `
            -MinSampleSize $cfg.minSampleSize `
            -Note $cfg.note

        if ($null -ne $entry) {
            $newRefs += $entry
        }
    }

    if ($newRefs.Count -gt 0) {
        $data.settingReferenceValues = $newRefs
        $data | Write-JsonUtf8 -Path $path
        Write-Host "Updated: $FileName"
    }
    else {
        Write-Host "SKIP (no refs matched): $FileName"
    }
}

if (-not (Test-Path $machinesDir)) {
    Write-Host "machinesフォルダが見つかりません: $machinesDir"
    exit 1
}

Update-MachineJson -FileName "list_slot_015.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りと小役に整理。弱チェリー・弱スイカは分母が重くなりやすいため主軸にせず、高ゲーム数時の補強材料として扱う。直撃系は重い分母として低weightで保持する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当りと合わせて総合判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は荒れやすいためボーナス初当り優先で見る。" },
        @{ counterKey = "weak_cherry_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は試行不足でブレやすいので高ゲーム数で参照。" },
        @{ counterKey = "weak_suika_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は試行不足でブレやすいので高ゲーム数で参照。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱い、単体では決めない。" }
    )

Update-MachineJson -FileName "list_slot_016.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はCZ、重い分母は直撃系に整理。CZは序盤の偏りが出やすいため補助に留め、直撃は高サンプル時のみ効かせる。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZと合わせて総合判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためボーナス初当り優先で見る。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。サンプル不足時は判別に使用しないこと。" }
    )

Update-MachineJson -FileName "list_slot_017.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZに整理。現行入力では2軸構成だが、主軸と補助の優先度を明確化してブレを抑える。CZ単体では決めず、AT初当りを優先して見る。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZと複合で判断する。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤はブレやすいためAT初当り優先で評価。" }
    )

Update-MachineJson -FileName "list_slot_018.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZに整理。現行UIでは入力しやすい2軸を優先採用し、CZは補助評価に留める。主軸はAT初当りで、序盤のCZ偏りを過信しない。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZと合わせて総合判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。CZは序盤に偏りやすいため過信しない。" }
    )

Update-MachineJson -FileName "list_slot_019.json" `
    -MachineNotes "均一化フェーズ対応。主判別はCZ、補助はボーナスとAT初当り、重い分母は直撃系に整理。現行参照値の中ではCZが最も軸にしやすいため主軸に据えるが、ボーナス・ATと複合で判断する。" `
    -RefConfigs @(
        @{ counterKey = "cz_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。ボーナス・ATと合わせて判断。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。単体ではなくCZ主軸で総合判断。" },
        @{ counterKey = "at_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。序盤は荒れやすいので過信しない。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱う。" }
    )

Write-Host "Done"
