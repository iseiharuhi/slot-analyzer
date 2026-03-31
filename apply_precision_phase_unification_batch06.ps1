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

Update-MachineJson -FileName "list_slot_020.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りとCZに整理。レヴューや上位状態の影響で短期ブレが出やすいため、まずはボーナス初当りを最優先に見る。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT・CZと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は荒れやすいためボーナス初当り優先。" },
        @{ counterKey = "cz_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなく主判別の補強として使う。" }
    )

Update-MachineJson -FileName "list_slot_021.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りとCZに整理。モードや特殊当選の影響でAT側に偏りが出るため、主軸はボーナス初当りで固定する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT・CZと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいため主判別優先で見る。" },
        @{ counterKey = "cz_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。主判別の補強として扱い、単体で決めない。" }
    )

Update-MachineJson -FileName "list_slot_022.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りとCZに整理。トリガーの重なりでAT寄りに見える場面があるため、まずはボーナス初当りを軸に全体を整える。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT・CZと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は荒れやすいためボーナス初当り優先。" },
        @{ counterKey = "cz_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。主判別の補強材料として使う。" }
    )

Update-MachineJson -FileName "list_slot_023.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当りに固定。現行参照値では主軸に使いやすいのがボーナスのみのため、無理に補助軸を増やさず、確実に使える1軸を高品質に維持する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。単体ではなく示唆系と総合判断。" }
    )

Update-MachineJson -FileName "list_slot_024.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はCZに整理。特化当選や波でAT結果が揺れやすいため、現行入力ではボーナス初当りを主軸にして安定度を優先する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいため主判別優先で見る。" }
    )

Update-MachineJson -FileName "list_slot_025.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りに整理。高確や状態差でAT側が荒れやすいため、炎炎はボーナス初当りを軸に実戦向けの見やすさを優先する。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当りと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は差が荒れやすいため主判別優先で見る。" }
    )

Update-MachineJson -FileName "list_slot_026.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はCZとAT初当りに整理。複数トリガーで数値が散りやすいため、軸はボーナス初当りで固定し、CZとATは補助に留める。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZ・ATと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいため主判別優先で見る。" },
        @{ counterKey = "at_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなく主判別の補強として使う。" }
    )

Update-MachineJson -FileName "list_slot_027.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZに整理。2軸機種として扱い、無理に重い要素を足さず、実戦で使える初当りバランスの見やすさを優先する。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいためAT初当り優先で評価。" }
    )

Update-MachineJson -FileName "list_slot_028.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りに整理。上位移行や連鎖の影響でAT回数が偏るため、まずはボーナス初当りを軸にしてブレを抑える。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当りと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は偏りやすいため主判別優先で見る。" }
    )

Update-MachineJson -FileName "list_slot_029.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス初当り、補助はAT初当りと共通9ベルに整理。共通9ベルは分母が重いため小役補助として扱い、ボーナス初当りを主軸に据える。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。AT初当りと複合で判断。" },
        @{ counterKey = "at_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤は荒れやすいため主判別優先で見る。" },
        @{ counterKey = "common_9bell_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は試行不足でブレやすいため高ゲーム数で参照。" }
    )

Write-Host "Done"
