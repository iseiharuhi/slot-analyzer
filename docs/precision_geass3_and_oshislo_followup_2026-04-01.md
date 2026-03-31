# Precision continuation report (2026-04-01)

対象:
- list_slot_004 パチスロコードギアス 反逆のルルーシュ3 C.C.&Kallen ver.
- dmm_slot_030 推しスロ アイドルVer.

実施内容:
- 現在の情報源zipと既存JSONを確認し、既存キー構造を維持したまま更新
- list_slot_004 は draft → partial へ引き上げ
- dmm_slot_030 は天井・基本情報を補完したが、比較値未確定のため draft 維持
- master/machines.json と master/machine_details を同期
- machine_master_manifest.json は未変更

採用方針メモ:
- コードギアス3 C.C.&Kallen ver. は、一撃の設定差ページから「ボーナス合算」「通常時の共通ベル+赤同色BB」「RB後AT中の無限移行C.C.揃い」を採用
- AT終了画面・サミートロフィーは定性示唆として強いが、総ゲーム数分母ベースの比較値へ落としにくいため notes 管理
- 推しスロ アイドルVer. は DMM/一撃で導入日・メーカー・150G天井を確認できた一方、総ゲーム数分母で使える全設定比較値テーブルを安全に確認できなかったため、settingReferenceValues は未実装のまま保持

現時点の残件:
- 推しスロ アイドルVer. のみ draft 継続
