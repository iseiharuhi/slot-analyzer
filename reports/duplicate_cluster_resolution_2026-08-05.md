# Duplicate cluster resolution (2026-08-05)

Wave1のURL反映作業に着手する前に、既存194件に対してdmmUrl/ichigekiUrlの完全一致チェックと
正規化名の広域スイープを実施したところ、`l_ring_ni_kakero.json`(既に解決済み)に加えて
新たに3クラスタの重複が見つかった。「情報量が多い方を残す」ではなく、**ネットの公開データと
実際に数値を突き合わせて**判断した。

## クラスタA: エウレカセブン HI-EVOLUTION ZERO TYPE-ART
- `dmm_slot_038.json` と `eureka_zero.json` が同一機種(dmmUrl/ichigekiUrl完全一致)
- 1geki.jpの公開データ(初当り合算: 設定1=1/159.7 〜 設定6=1/133.2)と `dmm_slot_038.json` の
  `at_count` 参照値が**完全一致**。ボーナス合算(1/195.6等)も `big_count`+`reg_count` の調和平均で再現できた。
- `eureka_zero.json` の値(220/200/180/160/140/120など丸め値、notesなし)は公開データと一致せず、根拠不明の推測値と判断。
- **結論**: `dmm_slot_038.json` を採用、`eureka_zero.json` を削除。あわせてメーカー(TAIYO ELEC)・発売日(2023-11-06)を補完。

## クラスタB: 劇場版魔法少女まどか☆マギカ f-フォルテ-
- `dmm_slot_037.json` / `madoka_forte.json` / `madoka_magica_f.json` の3ファイルが同一機種
- 1geki.jpの公開データ(ボーナス初当り: 設定1=1/251.2、設定6=1/187.5)と `dmm_slot_037.json` の
  `bonus_count` 参照値が**完全一致**。メーカー(メーシー)・発売日(2023-11-06)も公開情報と一致。
- `madoka_forte.json` は汎用カウンター(cz/at)のみで公開データと不一致。`madoka_magica_f.json` は空シェル。
- **結論**: `dmm_slot_037.json` を採用、`madoka_forte.json` と `madoka_magica_f.json` を削除。

## クラスタC: コードギアス 反逆のルルーシュ／復活のルルーシュ
- `list_slot_003.json`(メーカー・発売日・詳細notes・URL完備)と `l_code_geass.json`(空シェル)が同一機種
- **結論**: `list_slot_003.json` を採用、`l_code_geass.json` を削除。

## 反映内容
各クラスタで不採用となった4ファイルを、以下3箇所全てから削除して整合性を確保:
- `app/src/main/assets/machines/*.json`
- `app/src/main/assets/master/machine_details/*.json`
- `app/src/main/assets/master/machines.json` の該当エントリ

機種ファイル総数: **194 → 190**

## 検証
再監査の結果、190/190件が有効なJSON、破損0件を確認。
