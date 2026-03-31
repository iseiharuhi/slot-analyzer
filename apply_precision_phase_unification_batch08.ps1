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

# ジャグラー系
Update-MachineJson -FileName "my_juggler_v.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はぶどうとBIGに整理。ジャグラー系はREGを最優先にし、ぶどうは高ゲーム数時の補強、BIGは補助評価に留める。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を主軸に2500G以上で評価したい。" },
        @{ counterKey = "grape_count"; weight = 0.7; minSampleSize = 4000; note = "補助判別①。ぶどうは試行不足でブレやすいため高ゲーム数で参照。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は荒れやすいのでREG主軸で総合判断。" }
    )

Update-MachineJson -FileName "im_juggler_ex.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はぶどうとBIGに整理。アイム系はREGを主軸にし、ぶどうを高ゲーム数時の補強、BIGは補助に留める。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を主軸に2500G以上で評価したい。" },
        @{ counterKey = "grape_count"; weight = 0.7; minSampleSize = 4000; note = "補助判別①。ぶどうは高ゲーム数時の補強材料として使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" }
    )

Update-MachineJson -FileName "funky_juggler_2.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はぶどうとBIGに整理。ファンキー系もREGを主軸とし、ぶどうは高ゲーム数時の補強に使う。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を主軸に2500G以上で評価したい。" },
        @{ counterKey = "grape_count"; weight = 0.7; minSampleSize = 4000; note = "補助判別①。ぶどうは試行不足でブレやすいため高ゲーム数で参照。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は荒れやすいのでREG主軸で総合判断。" }
    )

Update-MachineJson -FileName "happy_juggler_v3.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はぶどうとBIGに整理。ハッピー系はREGを最優先にし、ぶどうとBIGは補助として扱う。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を主軸に2500G以上で評価したい。" },
        @{ counterKey = "grape_count"; weight = 0.7; minSampleSize = 4000; note = "補助判別①。ぶどうは高ゲーム数時の補強材料として使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" }
    )

Update-MachineJson -FileName "gogo_juggler_3.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はぶどうとBIGに整理。ゴージャグ系もREGを主軸にして、ぶどうは高ゲーム数時の補強とする。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を主軸に2500G以上で評価したい。" },
        @{ counterKey = "grape_count"; weight = 0.7; minSampleSize = 4000; note = "補助判別①。ぶどうは高ゲーム数時の補強材料として使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" }
    )

# ノーマルタイプ
Update-MachineJson -FileName "list_slot_042.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。ニューパル系はREGと合算を主軸にし、BIG単体は補助評価に留める。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は荒れやすいためREG主軸で判断。" }
    )

Update-MachineJson -FileName "list_slot_043.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。DX3もREG主軸で揃え、合算で全体傾向を補強する。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" }
    )

Update-MachineJson -FileName "list_slot_044.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とベルに整理。ハナビ系はREGとベルを高ゲーム数で確認しつつ、合算で全体傾向を見る。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "bell_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。ベルは試行不足でブレやすいため高ゲーム数で参照。" },
        @{ counterKey = "big_count"; weight = 0.5; minSampleSize = 3000; note = "補助判別。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "list_slot_045.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算・ベル・VSゲームハズレに整理。重い補助要素は低weightで残し、REG主軸を崩さない構成にする。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "bell_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。ベルは高ゲーム数時の補強材料。" },
        @{ counterKey = "vs_game_miss_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱い、単体では決めない。" },
        @{ counterKey = "big_count"; weight = 0.5; minSampleSize = 3000; note = "補助判別。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "list_slot_046.json" `
    -MachineNotes "均一化フェーズ対応。主判別はBIGに固定。現行参照値ではBIGのみのため、無理に軸を増やさず、1軸を丁寧に扱う構成で品質を揃える。" `
    -RefConfigs @(
        @{ counterKey = "big_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で評価したい。単体ではなく示唆系と総合判断。" }
    )

Update-MachineJson -FileName "list_slot_047.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算と羽・チェリー系に整理。小役系は高ゲーム数時の補強に留め、主軸はREGで揃える。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "feather_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は試行不足でブレやすいため高ゲーム数で参照。" },
        @{ counterKey = "three_coin_role_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は試行不足でブレやすいため高ゲーム数で参照。" },
        @{ counterKey = "cherry_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は試行不足でブレやすいため高ゲーム数で参照。" },
        @{ counterKey = "big_count"; weight = 0.5; minSampleSize = 3000; note = "補助判別。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "list_slot_048.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。ディスクアップ2は波でBIG先行になりやすいため、REG主軸で見やすさを優先する。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は荒れやすいためREG主軸で判断。" }
    )

Update-MachineJson -FileName "list_slot_049.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算と小役に整理。ディスクアップULTRAREMIXもREG主軸で揃え、小役は高ゲーム数時の補強として扱う。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "three_coin_role_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は高ゲーム数時の補強材料として使う。" },
        @{ counterKey = "suika_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は高ゲーム数時の補強材料として使う。" },
        @{ counterKey = "big_count"; weight = 0.5; minSampleSize = 3000; note = "補助判別。BIG単体は主軸にしない。" }
    )

Update-MachineJson -FileName "list_slot_050.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。ファミスタもノーマルタイプとしてREG主軸で揃え、合算で全体傾向を確認する。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" }
    )

# 沖スロ・ハナハナ系
Update-MachineJson -FileName "list_slot_051.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。沖ドキ！GOLDは荒波寄りだが、現行入力ではREGと合算を中心に見る方針で揃える。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REGを軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は荒れやすいためREG主軸で判断。" }
    )

Update-MachineJson -FileName "list_slot_052.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス合算に固定。現行参照値では合算のみのため、無理に軸を増やさず、1軸を丁寧に扱う構成で品質を揃える。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で評価したい。単体ではなく示唆系と総合判断。" }
    )

Update-MachineJson -FileName "list_slot_053.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス合算に固定。アンコール版も現行参照値では合算のみのため、1軸を丁寧に扱う構成で揃える。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で評価したい。単体ではなく示唆系と総合判断。" }
    )

Update-MachineJson -FileName "list_slot_054.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス合算に固定。チバリヨ2プラスも現行参照値では合算中心のため、無理に補助軸を増やさず高品質な1軸運用を維持する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で評価したい。単体ではなく示唆系と総合判断。" }
    )

Update-MachineJson -FileName "list_slot_055.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。ハナハナ系はREGを主軸にし、合算で全体傾向、BIGは補助評価に留める。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は荒れやすいためREG主軸で判断。" }
    )

Update-MachineJson -FileName "list_slot_056.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。スマート沖スロ版ニューキングハナハナもREG主軸で揃える。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" }
    )

Update-MachineJson -FileName "list_slot_057.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。ニューキングハナハナ-30もREG主軸で統一する。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" }
    )

Update-MachineJson -FileName "list_slot_058.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。ドラゴンハナハナもREG主軸で統一し、合算で全体傾向を補強する。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" }
    )

Update-MachineJson -FileName "list_slot_059.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。キングハナハナもREG主軸で揃え、BIGは補助に留める。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" }
    )

Update-MachineJson -FileName "list_slot_060.json" `
    -MachineNotes "均一化フェーズ対応。主判別はREG、補助はボーナス合算とBIGに整理。ホウオウ天翔もREG主軸で統一し、合算を補助に使う。" `
    -RefConfigs @(
        @{ counterKey = "reg_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。REG先行を軸に2500G以上で評価したい。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。合算は全体傾向の確認に使う。" },
        @{ counterKey = "big_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。BIG単体は過信せずREG主軸で判断。" }
    )

Write-Host "Done"
