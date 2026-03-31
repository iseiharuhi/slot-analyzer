# precision_batch_more6_2026-03-31

## 対象
- dmm_slot_026 / L花の慶次～佐渡攻めの章〜
- list_slot_010 / Lベルセルク無双
- list_slot_012 / L南国育ち
- list_slot_013 / スマスロ痛いのは嫌なので防御力に極振りしたいと思います。
- list_slot_015 / Lアズールレーン THE ANIMATION
- list_slot_016 / Lダーリン・イン・ザ・フランキス

## 方針
- 既存JSONをベースに差分更新
- ceilingRules はそのまま保持
- 信頼できる公開値のみ反映
- 条件付き確率を総ゲーム数分母へ無理に落とし込まない

## 反映内容
- 機種メタ情報（メーカー / 導入日 / リンク）補完
- draft → partial 引き上げ: list_slot_010 / list_slot_012 / list_slot_013 / list_slot_015 / list_slot_016
- machine_details の status / direct URL を同期

## 注意
- assets 更新後はアプリ削除 or データ削除
- machine_master_manifest.json は未変更
- 条件付き抽選しか確認できない要素は notes に留め、settingReferenceValues へは入れていない
