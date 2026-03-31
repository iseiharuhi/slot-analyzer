# precision_geass_fix_and_next5_2026-04-01

今回の差分では、既存JSONを壊さず次の修正を実施。

## 1. コードギアス名称整理
- `list_slot_003` を DMM掲載の正式名 `スマスロ コードギアス反逆のルルーシュ／復活のルルーシュ` に修正
- `list_slot_004` はスマスロ重複を避けるため `パチスロコードギアス 反逆のルルーシュ3 C.C.&Kallen ver.` の整理枠へ変更
- `list_slot_003` に 1000G+α 天井を追加

## 2. partial化した機種
- list_slot_027: パチスロ アクエリオン ALL STARS
- list_slot_028: パチスロ鉄拳5
- list_slot_029: パチスロ幼女戦記
- list_slot_030: スマスロ シャーマンキング
- list_slot_031: パチスロ 新鬼武者2

## 3. 実装方針
- 重い分母でも公開比較値がある項目は採用
- 既存キーは保持、未登録の ceilingRules のみ追加
- machine_master_manifest.json は未変更
- master/machines.json は対象7機種の name/status/checksum のみ更新

## 4. 反映時の注意
- パッケージ階層を保ったまま上書き
- assets 更新後はアプリ削除またはデータ削除が必要
