# reconstruction canonicalization phase3 (2026-04-04)

## 結論
再構成候補12件を見直した結果、**11件は manifest 側の正本が既に存在**しており、
non-manifest 側の候補JSONは削除して問題ないと判断した。

**保留は 1件のみ**
- `code_geass_fukkatsu.json`

これは現 manifest 側が `list_slot_003.json`（「スマスロ コードギアス反逆のルルーシュ／復活のルルーシュ」）という**結合タイトル**になっており、
将来的に「反逆」「復活」を別機種として分離する可能性があるため、今は削除しない。

## safe delete（11件）
- `code_geass_cc_kallen.json`
- `eureka_zero.json`
- `ghost_shell.json`
- `kabaneri_kaimon.json`
- `kengan.json`
- `lovejo3.json`
- `madoka_forte.json`
- `monkey_turn_v.json`
- `ring_v.json`
- `toaru.json`
- `lupin.json`

## hold / reconstruct later（1件）
- `code_geass_fukkatsu.json`

## 今回の判断ポイント
- full_machine の manifest 正本があるものは、placeholder / legacy_simple / notes-only rogue JSON を削除対象へ寄せた
- `monkey_turn_v.json` は simplified `settingReferenceValues` しか持たない旧形式のため、`dmm_slot_027.json` を正本採用
- `lupin.json` は再確認の結果、`list_slot_040.json` が正本として存在するため safe delete に変更
- `code_geass_fukkatsu.json` だけは、manifest の結合機種タイトル問題が残っているため保留

## 次フェーズ
1. この11件を削除
2. `code_geass_fukkatsu.json` と `list_slot_003.json` の扱いを最終決定
3. その後にノーマル → 沖スロ → AT機の精度強化へ移行
