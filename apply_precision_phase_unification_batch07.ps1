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

Update-MachineJson -FileName "list_slot_030.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りに整理。上位状態やループでAT側が短期的に偏りやすいため、まずはボーナス初当りを軸にして実戦での見やすさを優先する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当りと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は荒れやすいため主判別優先で見る。" }
    )

Update-MachineJson -FileName "list_slot_031.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、重い分母は直撃ATに整理。鬼モードや示唆は重要だが、現行入力ではAT初当りを軸にし、直撃は高ゲーム数時の補強材料として低weightで残す。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。示唆要素と合わせて総合判断。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱い、単体で決めない。" }
    )

Update-MachineJson -FileName "list_slot_032.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZに整理。フェアリーテイル2はCZからの流れで短期ブレが出やすいため、CZ単体を追い過ぎずAT初当りを主軸に固定する。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためAT初当り優先で評価。" }
    )

Update-MachineJson -FileName "list_slot_033.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りに整理。高確や前兆の偏りでAT回数が揺れやすいため、主軸はボーナス初当りで固定して全体の均一性を優先する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当りと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は荒れやすいため主判別優先で見る。" }
    )

Update-MachineJson -FileName "list_slot_034.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当りに固定。盾の勇者は現行参照値で確実に使える軸がAT初当り中心のため、無理に補助軸を増やさず、1軸を丁寧に扱う構成で品質を揃える。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。単体ではなく示唆系と総合判断。" }
    )

Update-MachineJson -FileName "list_slot_035.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当りに固定。S BOØWYは現行入力で無理に補助項目を増やさず、AT初当りを高品質に扱う方針でブレを抑える。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。単体ではなく示唆系と総合判断。" }
    )

Update-MachineJson -FileName "list_slot_036.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りと弱チェリーに整理。弱チェリーは小役として高ゲーム数時の補強に留め、主軸はボーナス初当りで統一する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当りと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいため主判別優先で見る。" },
        @{ counterKey = "weak_cherry_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は試行不足でブレやすいため高ゲーム数で参照。" }
    )

Update-MachineJson -FileName "list_slot_037.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス合算、補助はBIG・REG・AT初当りに整理。ダンまち2はボーナス種別も見たいが、全体の均一性を優先してまず合算を主軸に置く。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。BIG・REG・ATと複合で判断。" },
        @{ counterKey = "big_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。単体ではなく合算主軸で判断。" },
        @{ counterKey = "reg_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。単体ではなく合算主軸で判断。" },
        @{ counterKey = "at_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。序盤は荒れやすいため過信しない。" }
    )

Update-MachineJson -FileName "list_slot_038.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当りに固定。現行参照値ではAT初当りが最も使いやすいため、リゼロ2は1軸を丁寧に扱う構成で均一性を確保する。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。単体ではなく示唆系と総合判断。" }
    )

Update-MachineJson -FileName "list_slot_039.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当りに固定。このすばは現行入力で確実に使える軸がAT初当り中心のため、無理に補助項目を増やさず高品質な1軸運用を維持する。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。単体ではなく示唆系と総合判断。" }
    )

Update-MachineJson -FileName "list_slot_040.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りとCZに整理。複数契機でATが伸びやすいため、主軸はボーナス初当りで固定し、CZは補助として扱う。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当り・CZと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいため主判別優先で見る。" },
        @{ counterKey = "cz_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなく主判別の補強として使う。" }
    )

Update-MachineJson -FileName "list_slot_041.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当りに固定。黄門ちゃま天は現行入力で主軸として扱いやすいのがAT初当りのため、1軸を丁寧に評価する構成で均一性を保つ。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。単体ではなく示唆系と総合判断。" }
    )

Write-Host "Done"
