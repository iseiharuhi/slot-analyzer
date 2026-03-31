# precision_batch_032_037_report_2026-04-01

今回の差分では、以下の6機種を draft から partial へ引き上げた。

- list_slot_032 FAIRY TAIL2
- list_slot_033 ペルソナ5
- list_slot_034 パチスロ盾の勇者の成り上がり
- list_slot_035 S BOØWY
- list_slot_036 バイオハザード ヴィレッジ
- list_slot_037 パチスロ ダンジョンに出会いを求めるのは間違っているだろうか2

実施内容:
- 既存JSONの ceilingRules を全件確認し、今回対象機種のみ必要な天井情報を追加
- 既存テンプレの counterDefinitions を正式名称へ置換
- 公開解析から信頼できる分母値だけを settingReferenceValues へ追加
- master/machine_details を partial に同期
- master/machines.json は対象6機種の status/checksum のみ更新（count など他の全体項目は未修正のまま維持）

採用方針:
- 主判別は 1〜3 項目
- 小役は長時間サンプル向けのもののみ採用
- 終了画面・ボイス・条件付き抽選などは notes に留め、数値化は見送り
