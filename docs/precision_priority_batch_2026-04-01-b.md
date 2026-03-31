# precision_priority_batch_2026-04-01-b

対象:
- Lからくりサーカス
- Lゴブリンスレイヤー
- L戦国乙女4 戦乱に閃く炯眼の軍師
- スマスロ コードギアス反逆のルルーシュ／復活のルルーシュ
- パチスロ 炎炎ノ消防隊

## 変更方針
- 既存JSONを破壊せず、weight / minSampleSize / notes / ceilingRules を精度フェーズ向けに再調整
- machine_details の status / capturedDate / リンクを更新
- master/machines.json の checksum と status を更新
- machine_master_manifest.json は未変更

## 機種別要点
### Lからくりサーカス
- 主判別をAT初当り、補助をCZ初当りへ整理
- 1200G天井 / CZ5回目 / AT間2500G を明確化
- status を verified へ更新

### Lゴブリンスレイヤー
- 共通ベルを最重要主軸化
- 通常時600G/1000G/1500Gのゲーム数天井を追加
- DMM直リンク、メーカー、導入日を補完
- status を verified へ更新

### L戦国乙女4 戦乱に閃く炯眼の軍師
- 初当りボーナス + CZ補助 + 通常時AT直撃の3軸整理
- 通常時799G天井、ボーナス7回目エピボ天井を追加
- DMM直リンクを補完
- status を verified へ更新

### スマスロ コードギアス反逆のルルーシュ／復活のルルーシュ
- DMMの推測まとめを notes に反映
- 既存3軸（ボーナス/AT/直撃）の重みを精度寄りに再調整
- status は partial 維持

### パチスロ 炎炎ノ消防隊
- 通常時ボーナス初当りを主判別、炎炎激闘初当りを補助へ整理
- 通常時850G+α / 設定変更後650G+α / 同一有利区間3000Gボーナスを反映
- status は partial 維持
