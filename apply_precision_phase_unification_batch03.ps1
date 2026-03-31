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
    return $text | ConvertFrom-Json -Depth 100
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

function New-RefValue {
    param(
        [int]$SettingNo,
        [double]$DenominatorValue,
        [double]$Weight,
        [int]$MinSampleSize,
        [string]$Note
    )
    return [PSCustomObject]@{
        settingNo = $SettingNo
        denominatorValue = $DenominatorValue
        weight = $Weight
        minSampleSize = $MinSampleSize
        note = $Note
    }
}

function New-RefEntry {
    param(
        [string]$CounterKey,
        [object[]]$Values
    )
    return [PSCustomObject]@{
        counterKey = $CounterKey
        values = $Values
    }
}

function Update-MachineJson {
    param(
        [string]$FileName,
        [string]$MachineNotes,
        [object[]]$SettingReferenceValues
    )

    $path = Join-Path $machinesDir $FileName
    if (-not (Test-Path $path)) {
        Write-Host "SKIP (not found): $FileName"
        return
    }

    $data = Read-JsonFile -Path $path

    if ($null -ne $data.machine) {
        $data.machine.notes = $MachineNotes
    }

    $data.settingReferenceValues = $SettingReferenceValues

    $data | Write-JsonUtf8 -Path $path
    Write-Host "Updated: $FileName"
}

if (-not (Test-Path $machinesDir)) {
    Write-Host "machinesフォルダが見つかりません: $machinesDir"
    exit 1
}

Update-MachineJson -FileName "valvrave.json" `
    -MachineNotes "均一化フェーズ対応。主判別は革命ボーナス初当り、補助はCZ、重い分母は直撃ATで統一。ボーナス初当りを最優先に、CZは補助、直撃ATは高サンプル時のみ使用。AT回数単体は推測の主軸にしない。" `
    -SettingReferenceValues @(
        (New-RefEntry -CounterKey "bonus_count" -Values @(
            (New-RefValue 1 519.0 1.0 5 "主判別。2000G以上で評価したい。単体ではなくCZと合わせて判断。"),
            (New-RefValue 2 516.0 1.0 5 "主判別。2000G以上で評価したい。単体ではなくCZと合わせて判断。"),
            (New-RefValue 3 514.0 1.0 5 "主判別。2000G以上で評価したい。単体ではなくCZと合わせて判断。"),
            (New-RefValue 4 507.0 1.0 5 "主判別。2000G以上で評価したい。単体ではなくCZと合わせて判断。"),
            (New-RefValue 5 499.0 1.0 5 "主判別。2000G以上で評価したい。単体ではなくCZと合わせて判断。"),
            (New-RefValue 6 490.0 1.0 5 "主判別。2000G以上で評価したい。単体ではなくCZと合わせて判断。")
        )),
        (New-RefEntry -CounterKey "cz_count" -Values @(
            (New-RefValue 1 265.0 0.7 8 "補助判別。序盤はブレやすいのでボーナス初当り優先で見る。"),
            (New-RefValue 2 252.0 0.7 8 "補助判別。序盤はブレやすいのでボーナス初当り優先で見る。"),
            (New-RefValue 3 240.0 0.7 8 "補助判別。序盤はブレやすいのでボーナス初当り優先で見る。"),
            (New-RefValue 4 226.0 0.7 8 "補助判別。序盤はブレやすいのでボーナス初当り優先で見る。"),
            (New-RefValue 5 214.0 0.7 8 "補助判別。序盤はブレやすいのでボーナス初当り優先で見る。"),
            (New-RefValue 6 202.0 0.7 8 "補助判別。序盤はブレやすいのでボーナス初当り優先で見る。")
        )),
        (New-RefEntry -CounterKey "direct_hit_count" -Values @(
            (New-RefValue 1 16384.0 0.25 1 "重い分母。1回だけで決めず、高ゲーム数時の補強材料として扱う。"),
            (New-RefValue 2 12288.0 0.25 1 "重い分母。1回だけで決めず、高ゲーム数時の補強材料として扱う。"),
            (New-RefValue 3 8192.0 0.25 1 "重い分母。1回だけで決めず、高ゲーム数時の補強材料として扱う。"),
            (New-RefValue 4 6553.6 0.25 1 "重い分母。1回だけで決めず、高ゲーム数時の補強材料として扱う。"),
            (New-RefValue 5 5461.3 0.25 1 "重い分母。1回だけで決めず、高ゲーム数時の補強材料として扱う。"),
            (New-RefValue 6 4096.0 0.25 1 "重い分母。1回だけで決めず、高ゲーム数時の補強材料として扱う。")
        ))
    )

Update-MachineJson -FileName "list_slot_002.json" `
    -MachineNotes "均一化フェーズ対応。主判別は初当り(ボーナス+AT)、補助は乙女アタック、重い分母は通常時AT直撃で統一。強カワループなど特殊状態の影響を受ける直撃は過信せず、初当りとCZを主軸に見る。" `
    -SettingReferenceValues @(
        (New-RefEntry -CounterKey "bonus_count" -Values @(
            (New-RefValue 1 272.2 1.0 5 "主判別。2000G以上で見たい。単体ではなくCZと合わせて判断。"),
            (New-RefValue 2 267.3 1.0 5 "主判別。2000G以上で見たい。単体ではなくCZと合わせて判断。"),
            (New-RefValue 3 255.3 1.0 5 "主判別。2000G以上で見たい。単体ではなくCZと合わせて判断。"),
            (New-RefValue 4 238.2 1.0 5 "主判別。2000G以上で見たい。単体ではなくCZと合わせて判断。"),
            (New-RefValue 5 223.2 1.0 5 "主判別。2000G以上で見たい。単体ではなくCZと合わせて判断。"),
            (New-RefValue 6 217.1 1.0 5 "主判別。2000G以上で見たい。単体ではなくCZと合わせて判断。")
        )),
        (New-RefEntry -CounterKey "cz_count" -Values @(
            (New-RefValue 1 467.6 0.7 4 "補助判別。序盤はサンプル不足になりやすい。"),
            (New-RefValue 2 452.2 0.7 4 "補助判別。序盤はサンプル不足になりやすい。"),
            (New-RefValue 3 434.7 0.7 4 "補助判別。序盤はサンプル不足になりやすい。"),
            (New-RefValue 4 410.9 0.7 4 "補助判別。序盤はサンプル不足になりやすい。"),
            (New-RefValue 5 394.9 0.7 4 "補助判別。序盤はサンプル不足になりやすい。"),
            (New-RefValue 6 381.5 0.7 4 "補助判別。序盤はサンプル不足になりやすい。")
        )),
        (New-RefEntry -CounterKey "direct_hit_count" -Values @(
            (New-RefValue 1 10922.7 0.25 1 "重い分母。特殊状態を除外して参考程度に扱う。"),
            (New-RefValue 2 9362.3 0.25 1 "重い分母。特殊状態を除外して参考程度に扱う。"),
            (New-RefValue 3 8192.0 0.25 1 "重い分母。特殊状態を除外して参考程度に扱う。"),
            (New-RefValue 4 6553.6 0.25 1 "重い分母。特殊状態を除外して参考程度に扱う。"),
            (New-RefValue 5 5957.8 0.25 1 "重い分母。特殊状態を除外して参考程度に扱う。"),
            (New-RefValue 6 5461.3 0.25 1 "重い分母。特殊状態を除外して参考程度に扱う。")
        ))
    )

Update-MachineJson -FileName "list_slot_003.json" `
    -MachineNotes "均一化フェーズ対応。主判別は通常時ボーナス初当り、補助はAT初当り、重い分母はAT直撃で統一。CZや通常Cはメモ要素として別管理し、現行UIでは入力しやすい3軸に絞って評価する。" `
    -SettingReferenceValues @(
        (New-RefEntry -CounterKey "bonus_count" -Values @(
            (New-RefValue 1 282.9 1.0 5 "主判別。2000G以上で見たい。AT初当りとセットで判断。"),
            (New-RefValue 2 278.1 1.0 5 "主判別。2000G以上で見たい。AT初当りとセットで判断。"),
            (New-RefValue 3 260.6 1.0 5 "主判別。2000G以上で見たい。AT初当りとセットで判断。"),
            (New-RefValue 4 244.1 1.0 5 "主判別。2000G以上で見たい。AT初当りとセットで判断。"),
            (New-RefValue 5 229.2 1.0 5 "主判別。2000G以上で見たい。AT初当りとセットで判断。"),
            (New-RefValue 6 217.5 1.0 5 "主判別。2000G以上で見たい。AT初当りとセットで判断。")
        )),
        (New-RefEntry -CounterKey "at_count" -Values @(
            (New-RefValue 1 439.5 0.7 4 "補助判別。序盤は差が荒れやすいのでボーナス初当り優先。"),
            (New-RefValue 2 432.1 0.7 4 "補助判別。序盤は差が荒れやすいのでボーナス初当り優先。"),
            (New-RefValue 3 403.3 0.7 4 "補助判別。序盤は差が荒れやすいのでボーナス初当り優先。"),
            (New-RefValue 4 355.9 0.7 4 "補助判別。序盤は差が荒れやすいのでボーナス初当り優先。"),
            (New-RefValue 5 320.3 0.7 4 "補助判別。序盤は差が荒れやすいのでボーナス初当り優先。"),
            (New-RefValue 6 296.1 0.7 4 "補助判別。序盤は差が荒れやすいのでボーナス初当り優先。")
        )),
        (New-RefEntry -CounterKey "direct_hit_count" -Values @(
            (New-RefValue 1 6573.7 0.25 1 "重い分母。1回だけで決めず、高ゲーム数時の後押し要素。"),
            (New-RefValue 2 5904.7 0.25 1 "重い分母。1回だけで決めず、高ゲーム数時の後押し要素。"),
            (New-RefValue 3 4228.5 0.25 1 "重い分母。1回だけで決めず、高ゲーム数時の後押し要素。"),
            (New-RefValue 4 3124.5 0.25 1 "重い分母。1回だけで決めず、高ゲーム数時の後押し要素。"),
            (New-RefValue 5 2386.6 0.25 1 "重い分母。1回だけで決めず、高ゲーム数時の後押し要素。"),
            (New-RefValue 6 1991.5 0.25 1 "重い分母。1回だけで決めず、高ゲーム数時の後押し要素。")
        ))
    )

Update-MachineJson -FileName "list_slot_004.json" `
    -MachineNotes "均一化フェーズ対応。主判別はボーナス合算、補助はRB後AT中の無限移行C.C.揃い、重い分母は共通ベル+赤同色BBで統一。どちらも重い要素のため、実戦ではまずボーナス合算を主軸に見る。" `
    -SettingReferenceValues @(
        (New-RefEntry -CounterKey "bonus_count" -Values @(
            (New-RefValue 1 198.0 1.0 8 "主判別。十分な試行で見たい。単体ではなく他要素と合わせて判断。"),
            (New-RefValue 2 193.9 1.0 8 "主判別。十分な試行で見たい。単体ではなく他要素と合わせて判断。"),
            (New-RefValue 3 184.6 1.0 8 "主判別。十分な試行で見たい。単体ではなく他要素と合わせて判断。"),
            (New-RefValue 4 169.8 1.0 8 "主判別。十分な試行で見たい。単体ではなく他要素と合わせて判断。"),
            (New-RefValue 5 157.9 1.0 8 "主判別。十分な試行で見たい。単体ではなく他要素と合わせて判断。"),
            (New-RefValue 6 148.9 1.0 8 "主判別。十分な試行で見たい。単体ではなく他要素と合わせて判断。")
        )),
        (New-RefEntry -CounterKey "at_count" -Values @(
            (New-RefValue 1 993.0 0.6 1 "補助判別。出現自体が重いため、確認できたら参考に留める。"),
            (New-RefValue 2 993.0 0.6 1 "補助判別。出現自体が重いため、確認できたら参考に留める。"),
            (New-RefValue 3 963.8 0.6 1 "補助判別。出現自体が重いため、確認できたら参考に留める。"),
            (New-RefValue 4 595.8 0.6 1 "補助判別。出現自体が重いため、確認できたら参考に留める。"),
            (New-RefValue 5 425.6 0.6 1 "補助判別。出現自体が重いため、確認できたら参考に留める。"),
            (New-RefValue 6 331.0 0.6 1 "補助判別。出現自体が重いため、確認できたら参考に留める。")
        )),
        (New-RefEntry -CounterKey "direct_hit_count" -Values @(
            (New-RefValue 1 16384.0 0.25 1 "重い分母。1回だけで決めず、長時間実戦の補強材料。"),
            (New-RefValue 2 16384.0 0.25 1 "重い分母。1回だけで決めず、長時間実戦の補強材料。"),
            (New-RefValue 3 10922.7 0.25 1 "重い分母。1回だけで決めず、長時間実戦の補強材料。"),
            (New-RefValue 4 8192.0 0.25 1 "重い分母。1回だけで決めず、長時間実戦の補強材料。"),
            (New-RefValue 5 5461.3 0.25 1 "重い分母。1回だけで決めず、長時間実戦の補強材料。"),
            (New-RefValue 6 4096.0 0.25 1 "重い分母。1回だけで決めず、長時間実戦の補強材料。")
        ))
    )

Update-MachineJson -FileName "list_slot_005.json" `
    -MachineNotes "均一化フェーズ対応。主判別はAT初当り、補助は夜の蝶、重い分母は直撃/特殊契機で統一。設定2・4・5・6の段階機なので、存在しない設定値はそのまま作らず実在設定のみ評価する。" `
    -SettingReferenceValues @(
        (New-RefEntry -CounterKey "at_count" -Values @(
            (New-RefValue 1 370.0 1.0 4 "主判別。2000G以上で見たい。補助要素と合わせて判断。"),
            (New-RefValue 2 355.0 1.0 4 "主判別。2000G以上で見たい。補助要素と合わせて判断。"),
            (New-RefValue 4 296.0 1.0 4 "主判別。2000G以上で見たい。補助要素と合わせて判断。"),
            (New-RefValue 5 262.0 1.0 4 "主判別。2000G以上で見たい。補助要素と合わせて判断。"),
            (New-RefValue 6 245.0 1.0 4 "主判別。2000G以上で見たい。補助要素と合わせて判断。")
        )),
        (New-RefEntry -CounterKey "bonus_count" -Values @(
            (New-RefValue 1 248.0 0.7 6 "補助判別。序盤はブレやすいのでAT初当り優先。"),
            (New-RefValue 2 236.0 0.7 6 "補助判別。序盤はブレやすいのでAT初当り優先。"),
            (New-RefValue 4 230.0 0.7 6 "補助判別。序盤はブレやすいのでAT初当り優先。"),
            (New-RefValue 5 229.0 0.7 6 "補助判別。序盤はブレやすいのでAT初当り優先。"),
            (New-RefValue 6 226.0 0.7 6 "補助判別。序盤はブレやすいのでAT初当り優先。")
        )),
        (New-RefEntry -CounterKey "direct_hit_count" -Values @(
            (New-RefValue 1 65536.0 0.25 1 "重い分母。確認できても補強材料として扱う。"),
            (New-RefValue 2 32768.0 0.25 1 "重い分母。確認できても補強材料として扱う。"),
            (New-RefValue 4 16384.0 0.25 1 "重い分母。確認できても補強材料として扱う。"),
            (New-RefValue 5 8192.0 0.25 1 "重い分母。確認できても補強材料として扱う。"),
            (New-RefValue 6 4096.0 0.25 1 "重い分母。確認できても補強材料として扱う。")
        ))
    )

Write-Host "Done"
