# precision_batch_continue_2026-04-01

今回の差分では、既存JSONを壊さずに以下の3機種を更新しました。

## 対象
- list_slot_061 / 麻雀物語
- list_slot_062 / 戦姫絶唱シンフォギア 正義の歌
- list_slot_063 / Lパチスロうみねこのなく頃に2

## 更新方針
- 既存の counterDefinitions / settingReferenceValues / ceilingRules をベースに追記
- 分母が重くても公開比較値がある項目は採用
- 既存の他キーや階層は壊さない
- machine_master_manifest.json は未変更

## 反映内容
### 麻雀物語
- メーカー、導入日、DMM URL を補完
- 通常時AT直撃率を direct_hit_count に追加

### 戦姫絶唱シンフォギア 正義の歌
- DMM URL と導入日を補完
- 最終決戦確率を direct_hit_count に追加

### Lパチスロうみねこのなく頃に2
- draft から partial に引き上げ
- ボーナス合算確率を bonus_count に反映
- REG中斜め青7揃い確率を direct_hit_count に反映
- CZ間200G天井を ceilingRules に追加
- メーカー、導入日、外部リンクを補完

## 反映時の注意
- パッケージ階層そのままで上書き
- assets 更新後はアプリ削除またはデータ削除が必要
