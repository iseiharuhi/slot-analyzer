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


Update-MachineJson -FileName "list_slot_061.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りとBIG、重い分母は直撃系に整理。AT寄りに見える展開でも、まずはボーナス初当りを軸に全体を評価する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当り・BIGと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は荒れやすいため主判別優先で見る。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなく主判別の補強として使う。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱い、単体で決めない。" }
    )

Update-MachineJson -FileName "list_slot_062.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、重い分母は直撃ATに整理。シンフォギアは展開で短期ブレが大きいため、AT初当りを主軸にし、直撃は高ゲーム数時のみ効かせる。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。単体ではなく示唆系と総合判断。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱い、単体で決めない。" }
    )

Update-MachineJson -FileName "list_slot_063.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りとCZ、重い分母は直撃系に整理。うみねこ2は複数契機で数値が散りやすいため、主軸はボーナス初当りで固定する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当り・CZと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は荒れやすいため主判別優先で見る。" },
        @{ counterKey = "cz_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなく主判別の補強として使う。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱い、単体で決めない。" }
    )

Update-MachineJson -FileName "dmm_slot_001.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りとCZ、重い分母は直撃ATに整理。短期のAT偏りを追い過ぎず、まずはボーナス初当りを軸に評価する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当り・CZと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいため主判別優先で見る。" },
        @{ counterKey = "cz_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなく主判別の補強として使う。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱い、単体で決めない。" }
    )

Update-MachineJson -FileName "dmm_slot_002.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZとボーナス、重い分母は直撃ATに整理。RE:3は出玉波で短期ブレが出やすいため、AT初当りを主軸に固定する。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZ・ボーナスと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためAT初当り優先で評価。" },
        @{ counterKey = "bonus_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなく主判別の補強として使う。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱う。" }
    )

Update-MachineJson -FileName "dmm_slot_003.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZとボーナス、重い分母は直撃ATに整理。主軸はAT初当りで、CZは補助評価に留める。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZ・ボーナスと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためAT初当り優先で評価。" },
        @{ counterKey = "bonus_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。主判別の補強として使う。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱う。" }
    )

Update-MachineJson -FileName "dmm_slot_004.json" `
    -MachineNotes "均一化フェーズ対応。主判別はCZ、補助はAT初当りとボーナス、重い分母は直撃ATに整理。現行参照値ではCZが最も軸にしやすいため、主判別に据える。" `
    -RefConfigs @(
        @{ counterKey = "cz_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当り・ボーナスと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。単体ではなくCZ主軸で判断。" },
        @{ counterKey = "bonus_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなくCZ主軸で判断。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱う。" }
    )

Update-MachineJson -FileName "dmm_slot_005.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はAT初当りとBIGに整理。スーパーリオエース2はREGを軸にし、AT初当りは補助評価に留める。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は荒れやすいためREG主軸で見る。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" },
        @{ counterKey = "cz_count"; weight = 0.5; minSampleSize = 3000; note = "補助判別。CZは全体傾向の確認に使う。" }
    )

Update-MachineJson -FileName "dmm_slot_006.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。BT機でもREG主軸で全体の見やすさを優先する。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" }
    )

Update-MachineJson -FileName "dmm_slot_007.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZとボーナス、重い分母は直撃ATに整理。荒波機でもAT初当りを主軸に固定して均一性を優先する。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZ・ボーナスと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。単体ではなくAT初当り主軸で判断。" },
        @{ counterKey = "bonus_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。主判別の補強として使う。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱う。" }
    )

Update-MachineJson -FileName "dmm_slot_008.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。アニマルスロット ドッチはノーマルタイプとしてREG主軸で揃える。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" }
    )

Update-MachineJson -FileName "dmm_slot_009.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZとボーナス、重い分母は直撃ATに整理。主軸はAT初当りで、短期のCZ偏りを追い過ぎない。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZ・ボーナスと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためAT初当り優先で評価。" },
        @{ counterKey = "bonus_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。主判別の補強として使う。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱う。" }
    )

Update-MachineJson -FileName "dmm_slot_010.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。トリプルクラウンセブンもREG主軸で均一化する。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" }
    )

Update-MachineJson -FileName "dmm_slot_011.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZとボーナス、重い分母は直撃ATに整理。主軸はAT初当りで固定し、直撃は高サンプル時のみ効かせる。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZ・ボーナスと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためAT初当り優先で評価。" },
        @{ counterKey = "bonus_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。主判別の補強として使う。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱う。" }
    )

Update-MachineJson -FileName "dmm_slot_012.json" `
    -MachineNotes "均一化フェーズ対応。主判別はCZ、補助はAT初当りとボーナス、重い分母は直撃ATに整理。現行参照値ではCZが比較的使いやすいため主判別に据える。" `
    -RefConfigs @(
        @{ counterKey = "cz_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当り・ボーナスと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。単体ではなくCZ主軸で判断。" },
        @{ counterKey = "bonus_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなくCZ主軸で判断。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱う。" }
    )

Update-MachineJson -FileName "dmm_slot_013.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。BT機でもREG主軸で見やすさを優先する。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "dmm_slot_014.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZとボーナス、重い分母は直撃ATに整理。現行入力ではAT初当りを軸にした方が実戦で扱いやすい。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZ・ボーナスと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためAT初当り優先で評価。" },
        @{ counterKey = "bonus_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。主判別の補強として使う。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱う。" }
    )

Update-MachineJson -FileName "dmm_slot_015.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とベルに整理。スマスロ サンダーVはノーマル系としてREG主軸で揃え、ベルは高ゲーム数時の補強に使う。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "bell_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。ベルは試行不足でブレやすいため高ゲーム数で参照。" },
        @{ counterKey = "suika_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は高ゲーム数時の補強材料として使う。" },
        @{ counterKey = "big_count"; weight = 0.5; minSampleSize = 3000; note = "補助判別。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "dmm_slot_016.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りとCZ、重い分母は直撃ATに整理。海門決戦でも主軸はボーナス初当りで固定する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当り・CZと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいため主判別優先で見る。" },
        @{ counterKey = "cz_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなく主判別の補強として使う。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱う。" }
    )

Update-MachineJson -FileName "dmm_slot_017.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算と風鈴に整理。スマスロ ハナビもREG主軸で揃え、風鈴は高ゲーム数時の補強に使う。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "fuurin_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は試行不足でブレやすいため高ゲーム数で参照。" },
        @{ counterKey = "big_count"; weight = 0.5; minSampleSize = 3000; note = "補助判別。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "dmm_slot_018.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZに整理。攻殻機動隊は2軸構成で見やすさを優先し、AT初当りを主軸に固定する。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためAT初当り優先で評価。" }
    )

Update-MachineJson -FileName "dmm_slot_019.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りに整理。L範馬刃牙はボーナス初当りを主軸にしてブレを抑える。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当りと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は荒れやすいため主判別優先で見る。" }
    )

Update-MachineJson -FileName "dmm_slot_020.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りに整理。炎炎ノ消防隊2も主軸はボーナス初当りで固定し、AT側は補助とする。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当りと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は差が荒れやすいため主判別優先で見る。" }
    )

Update-MachineJson -FileName "dmm_slot_021.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とチェリーに整理。ノーマル系としてREG主軸で揃え、チェリーは高ゲーム数時の補強に使う。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "cherry_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は高ゲーム数時の補強材料として使う。" },
        @{ counterKey = "big_count"; weight = 0.5; minSampleSize = 3000; note = "補助判別。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "dmm_slot_022.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。ニューシオサイもREG主軸で全体の見やすさを優先する。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "dmm_slot_023.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。琉神-30もREG主軸で揃える。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "dmm_slot_024.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りとCZに整理。リングにかけろ1 Vはボーナス初当りを主軸にして均一性を優先する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当り・CZと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいため主判別優先で見る。" },
        @{ counterKey = "cz_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなく主判別の補強として使う。" }
    )

Update-MachineJson -FileName "dmm_slot_025.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はWラブラッシュに整理。ラブ嬢3は現行入力で確実に使える軸を優先し、無理に重い要素を増やさない。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。補助要素と合わせて総合判断。" },
        @{ counterKey = "wlove_rush_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためAT初当り優先で見る。" },
        @{ counterKey = "cz_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。存在する場合は主判別の補強として使う。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。存在する場合のみ補強材料として扱う。" }
    )

Update-MachineJson -FileName "dmm_slot_026.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りに整理。花の慶次 佐渡攻めの章はボーナス初当り主軸で全体を整える。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当りと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は荒れやすいため主判別優先で見る。" },
        @{ counterKey = "cz_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。存在する場合は主判別の補強として使う。" }
    )

Update-MachineJson -FileName "dmm_slot_027.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZに整理。スマスロモンキーターンVはAT初当り主軸で見やすさを優先する。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。存在する場合はAT初当り主軸で判断。" }
    )

Update-MachineJson -FileName "dmm_slot_028.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とベルAに整理。クランキークレストもREG主軸で均一化する。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "bell_a_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は高ゲーム数時の補強材料として使う。" },
        @{ counterKey = "big_count"; weight = 0.5; minSampleSize = 3000; note = "補助判別。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "dmm_slot_029.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はCZとAT初当りに整理。マジカルハロウィン8はREGを軸にし、AT初当りは補助評価に留める。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためREG主軸で判断。" },
        @{ counterKey = "at_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなく主判別の補強として使う。" },
        @{ counterKey = "big_count"; weight = 0.5; minSampleSize = 3000; note = "補助判別。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "dmm_slot_030.json" `
    -MachineNotes "均一化フェーズ対応。現行参照値が少ないため、無理に構造を増やさず、存在するボーナス系のみを主軸候補として丁寧に扱う。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。存在する場合はREGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。存在する場合は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。存在する場合は主判別の補強として使う。" }
    )

Update-MachineJson -FileName "dmm_slot_031.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZに整理。ケンガンアシュラはAT初当り主軸で見やすさを優先する。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためAT初当り優先で評価。" },
        @{ counterKey = "bonus_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。存在する場合は主判別の補強として使う。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。存在する場合のみ補強材料として扱う。" }
    )

Update-MachineJson -FileName "dmm_slot_032.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。ちゅらちゅらもREG主軸で揃える。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "dmm_slot_033.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算と共通ベルに整理。ひぐらし業はREG主軸で揃え、小役は高ゲーム数時の補強に使う。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "common_bell_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は高ゲーム数時の補強材料として使う。" },
        @{ counterKey = "suika_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は高ゲーム数時の補強材料として使う。" },
        @{ counterKey = "big_count"; weight = 0.5; minSampleSize = 3000; note = "補助判別。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "dmm_slot_034.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZ、重い分母はEP/SPに整理。とある魔術の禁書目録はAT初当り主軸で、EP/SPは重い分母として低weightで扱う。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためAT初当り優先で評価。" },
        @{ counterKey = "ep_sp_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱い、単体で決めない。" }
    )

Update-MachineJson -FileName "dmm_slot_035.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。JAC INバージョンもREG主軸で均一化する。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "dmm_slot_036.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はCZに整理。大工の源さん超夢源は無理に軸を増やさず、現行入力で使いやすい2軸を優先する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。単体ではなく主判別の補強として使う。" },
        @{ counterKey = "at_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。存在する場合は主判別の補強として使う。" }
    )

Update-MachineJson -FileName "dmm_slot_037.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助は弱チェリー、重い分母はソウルジェム関連に整理。まどかf-フォルテ-はボーナス初当り主軸で扱う。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。小役系と複合で判断。" },
        @{ counterKey = "weak_cherry_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は試行不足でブレやすいため高ゲーム数で参照。" },
        @{ counterKey = "soul_gem_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱い、単体で決めない。" },
        @{ counterKey = "at_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別。存在する場合は主判別の補強として使う。" },
        @{ counterKey = "cz_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別。存在する場合は主判別の補強として使う。" }
    )

Update-MachineJson -FileName "dmm_slot_038.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はCZとAT初当りに整理。エウレカHI-EVOLUTION ZERO TYPE-ARTはREG主軸で揃え、ATは補助に留める。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためREG主軸で判断。" },
        @{ counterKey = "at_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなく主判別の補強として使う。" },
        @{ counterKey = "big_count"; weight = 0.5; minSampleSize = 3000; note = "補助判別。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "dmm_slot_039.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りに整理。L エヴァンゲリオン ～未来への創造～はボーナス初当り主軸で整える。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当りと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は荒れやすいため主判別優先で見る。" },
        @{ counterKey = "cz_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。存在する場合は主判別の補強として使う。" }
    )

Update-MachineJson -FileName "dmm_slot_040.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算と共通ベルAに整理。スーハナライジング-30もREG主軸で均一化する。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "common_bell_a_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は高ゲーム数時の補強材料として使う。" },
        @{ counterKey = "big_count"; weight = 0.5; minSampleSize = 3000; note = "補助判別。BIG単体は主軸にしない。" }
    )

Write-Host "Done"
