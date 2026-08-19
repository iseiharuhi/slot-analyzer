# Metadata fix pass (2026-08-05)

Wave1/Wave2の実在検証([wave1](machine_existence_verification_wave1_2026-08-04.md) / [wave2](machine_existence_verification_wave2_2026-08-05.md))で
見つかった誤りのうち、修正内容が明確なもの32件を実際に修正した。

## Group A: URL誤り(13件、Wave2由来)
| file | 変更内容 |
|---|---|
| dmm_slot_002.json | ichigekiUrl → `l_bhr2/`(旧作「バイオハザード7」誤りを修正) |
| dmm_slot_003.json | ichigekiUrl → `l_takt_op_destiny/`(タ行索引ページ誤りを修正) |
| dmm_slot_004.json | ichigekiUrl → `l_bigdream/`(ハ行索引ページ誤りを修正) |
| dmm_slot_005.json | ichigekiUrl → `l_sp_rioace2/`(初代「スーパーリオエース」誤りを修正) |
| funky_juggler_2.json | dmmUrl → `machines/3961`、releaseDate → 2021-10-04 |
| gogo_juggler_3.json | dmmUrl → `machines/4375`、releaseDate → 2023-07-03 |
| happy_juggler_v3.json | dmmUrl → `machines/4230`、ichigekiUrl → `s_happyjuggler_v3/`、releaseDate → 2022-10-24 |
| im_juggler_ex.json | dmmUrl → `machines/3626`、releaseDate → 2020-12-14 |
| list_slot_044.json (ハナビ) | dmmUrl → `machines/3911`(旧型2015年版の誤りを修正、新ハナビへ) |
| list_slot_055.json | dmmUrl → `machines/4701`(非スマート版誤りを修正)、releaseDate → 2025-01-20 |
| my_juggler_v.json | dmmUrl → `machines/4029`、ichigekiUrl → `s_myj5/`、releaseDate → 2021-12-06 |
| hokuto.json | ichigekiUrl → `s_sma_hokutonoken/`(404修正) |
| karakuri_circus.json | ichigekiUrl → `l_karakuri/`(404修正、続編ページと誤認しないよう注意) |

## Group B: 発売日/メーカー名の誤り(6件、Wave2由来、URL変更なし)
| file | 変更内容 |
|---|---|
| list_slot_046.json | releaseDate: 2020-02-03 → 2020-04-20 |
| list_slot_047.json | releaseDate: 2025-09-01 → 2025-07-07 |
| list_slot_062.json | releaseDate: 2024-09-02 → 2024-07-08 |
| dmm_slot_006.json | releaseDate: 2026-06-08 → 2026-05-25 |
| bandori.json | releaseDate: null → 2024-11-05 |
| dmm_slot_013.json | manufacturer: "GINZA"(中古販売店名の誤混入) → "Sammy" |

## Group C: Wave1由来の誤り(13件、URLなし機種)
| file | 変更内容 |
|---|---|
| ao_no_exorcist.json | manufacturer: サボハニ→OLYMPIA、releaseDate: 2020-08-03→2021-03-08 |
| akame_ga_kill.json | manufacturer: サミー→NANASHOW、releaseDate: 2020-03-02→2020-11-09 |
| aldnoah_zero.json | manufacturer: ユニバーサルブロス→ELECO |
| drifters.json | releaseDate: 2022-08-08→2022-02-07 |
| no_game_no_life.json | releaseDate: 2021-04-05→2021-06-14 |
| rakuen_tsuihou.json | releaseDate: 2021-06-07→2021-09-06 |
| warau_salesman_zesshou.json | releaseDate: 2025-01-20→2020-10-05(約4.5年のズレを修正) |
| yoshimune_3.json | releaseDate: 2021-10-04→2020-10-05 |
| railgun.json | manufacturer: JFJ→藤商事 |
| tiger_bunny_sp.json | releaseDate: 2022-01-11→2022-03-07 |
| warau_salesman_4.json | releaseDate: 2022-12-05→2022-10-03 |
| godzilla_vs_eva.json | name: 「LゴジラVSエヴァンゲリオン」→「Lゴジラ対エヴァンゲリオン」(正式表記)、manufacturer: null→ビスティ、releaseDate: null→2024-02-05 |
| prism_nana.json | name: 「Lプリズムナナ」→「プリズムナナ」(誤ったL接頭辞を削除) |
| frame_arms_girls.json | name: 「フレイムアームガールズ」→「フレームアームズ・ガール」(表記ゆれ修正) |

## Group D: 保留していた3件を追加調査のうえ修正(2026-08-05 追記)
| file | 変更内容 |
|---|---|
| black_lagoon_4.json | releaseDate: 2023-02-06(パチンコ版等の誤値)→2020-07-06(スロット版の実際の導入日)、ichigekiUrl: null→`https://1geki.jp/slot/s_blacklagoon4/` |
| eva_tamashii_no_kyoumei.json | name: 「エヴァンゲリオン魂の共鳴」→「新世紀エヴァンゲリオン 魂の共鳴」(正式名称)、manufacturer: null→ビスティ、releaseDate: null→2022-01-24、dmmUrl: null→`https://p-town.dmm.com/machines/4060` |
| ring_ni_kakero_1.json | manufacturer: null→銀座、releaseDate: null→2007-03-05(2007年オリジナル版と判明) |

### 重複ファイルの発見と解消
調査の過程で `l_ring_ni_kakero.json` と `ring_v.json` が**同一機種(Lリングにかけろ1V、2023-12-04、DMM機種ID 4460)を指す重複ファイル**であることが判明した。
`ring_v.json` の方がdmmUrl・ichigekiUrl・参照値まで揃っていたため、ユーザー確認のうえ `l_ring_ni_kakero.json` を削除して一本化した。
削除時、以下3箇所全てから該当エントリを除去し整合性を取った:
- `app/src/main/assets/machines/l_ring_ni_kakero.json`(削除)
- `app/src/main/assets/master/machine_details/l_ring_ni_kakero.json`(削除)
- `app/src/main/assets/master/machines.json` の該当エントリ(削除)

この結果、機種ファイル総数は **195 → 194** になった。

## 副次的に判明した事項(今回は対応せず、別タスク候補)
`app/src/main/assets/master/machine_details/` 配下に `assets/machines/` と同じ196件分の機種詳細JSONが並行して存在している。
コード上は `LocalAssetMachineMasterSource`(`assets/machines/`を直接読む)が実際に使われており、`master/machine_details/`は`MasterApi`(リモート同期用、現状ネットワーク経由でしか呼ばれない構成)向けのデータで、アプリの実データとしては使われていない可能性が高い。
ただし今回の重複のように、この並行ストアが `assets/machines/` と食い違っている(またはズレていく)リスクがあるため、後日この2ストアの整合性チェックか、不要なら削除を検討した方がよい。

## 検証
修正後、`app/src/main/assets/machines/*.json` 195件を再監査。JSON破損0件、有効195件を確認済み。

## 未着手(次のステップ候補)
- Wave1で見つかったURL(59機種分、各エージェント報告に記載)をdmmUrl/ichigekiUrlへ反映
- partial/draft 58機種の精度データ(参照値・天井ルール)の作成
- 上記「意図的に見送った項目」3件のユーザー判断待ち
