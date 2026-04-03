# パッケージ再読込サマリー（2026-04-04）

## 1. 監査対象
- パッケージ: `SlotSettingAnalyzerCompleteV2.zip`
- 機種リスト: `機種リスト.txt`

## 2. 主要件数
- manifest 登録機種数: **115**
- `app/src/main/assets/machines` 内 JSON 数（manifest本体除く）: **130**
- `.json.bak` 数: **105**
- `app/src/main/assets/master/machine_details` 数: **115**
- manifest 未登録 JSON 数: **15**
- manifest 内 `full_machine` 数: **95**
- manifest 内 `legacy_simple` 数: **20**

## 3. 機種リスト照合
- 機種リスト掲載数: **94**
- exact 一致: **74**
- 表記差分込み一致: **79**
- non-manifest にのみ存在: **3**
- 現パッケージ内で未確認: **12**

## 4. 重点所見
- 現パッケージは **manifest 115件 / machine_details 115件で件数整合**。
- 一方、`assets/machines` には **manifest未登録JSON 15件** が残存。
- manifest 正本は **full_machine 95件 / legacy_simple 20件** の混在。
- 機種リスト基準では、**正式名称・表記差分** による不一致が残る。
- `code_geass_fukkatsu.json` / `monkey_turn_v.json` / `toaru.json` などは **manifest外の補助JSON** として残っている。

## 5. 更新ファイル
- `reports/package_refresh_manifest_inventory_2026-04-04.csv`
- `reports/package_refresh_non_manifest_audit_2026-04-04.csv`
- `reports/package_refresh_machine_list_alignment_2026-04-04.csv`
- `docs/SlotSettingAnalyzer_詳細設計書_v1_0_2026-04-04.docx`
- `docs/design/機種マスタJSON設計書_v1_0_2026-04-04.docx`
