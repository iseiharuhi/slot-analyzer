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

Update-MachineJson -FileName "list_slot_007.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はボーナス合算、重い分母は直撃系を補強材料として扱う方針に整理。銭形DASHや示唆系は重要だが、現行入力ではまずAT初当りを主軸に見る。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。単体ではなく他要素と併用推奨。" },
        @{ counterKey = "bonus_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤はブレやすいためAT初当りと合わせて判断。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱い、単体で決めない。" }
    )

Update-MachineJson -FileName "list_slot_008.json" `
    -MachineNotes "均一化フェーズ対応。主判別はストライクボーナス初当り、重い分母は通常時直撃を補強材料として扱う構造に整理。直撃は設定差が大きいが分母が重いため、長時間実戦でのみ効かせる。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で評価したい。単体ではなく示唆系と合わせて判断。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。刺さると強いが、サンプル不足時は判別に使用しないこと。" }
    )

Update-MachineJson -FileName "list_slot_009.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助は10枚ベルに整理。小役は分母が重くなりやすいため、AT初当りより優先しない。直撃や示唆系はnotes管理で残す。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。単体ではなく他要素と併用推奨。" },
        @{ counterKey = "bell_count"; weight = 0.6; minSampleSize = 4000; note = "補助判別②。小役は試行不足だと荒れやすいので高ゲーム数で参照。" }
    )

Update-MachineJson -FileName "list_slot_012.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス合算、重い分母は初代モード突入を補強材料として扱う構造に整理。モードや楽曲変化は重要だが、まずは合算を最優先で見る。" `
    -RefConfigs @(
        @{ counterKey = "bonus_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。単体ではなく他要素と併用推奨。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。確認できても補強材料として扱い、単体で決めない。" }
    )

Update-MachineJson -FileName "list_slot_014.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助はCZとボーナス、重い分母はAT直撃に整理。カードや月山招待状などの示唆は強いが、現行UIでは入力しやすい4軸を優先して評価する。" `
    -RefConfigs @(
        @{ counterKey = "at_count"; weight = 1.0; minSampleSize = 2500; note = "主判別。2500G以上で信頼度上昇。CZやボーナスと複合で判断。" },
        @{ counterKey = "cz_count"; weight = 0.7; minSampleSize = 3000; note = "補助判別①。序盤はブレやすいためAT初当り優先で見る。" },
        @{ counterKey = "bonus_count"; weight = 0.6; minSampleSize = 3000; note = "補助判別②。単体ではなくCZ・AT初当りと組み合わせて判断。" },
        @{ counterKey = "direct_hit_count"; weight = 0.25; minSampleSize = 12000; note = "重い分母。高サンプル時の補強材料として扱う。" }
    )

Write-Host "Done"
