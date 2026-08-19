# Machine existence & URL verification — Wave 2 (2026-08-05)

## 背景
Wave 1([machine_existence_verification_wave1_2026-08-04.md](machine_existence_verification_wave1_2026-08-04.md))で
「dmmUrl/ichigekiUrl が両方とも未設定」の59機種を検証し、全て実在を確認した。
Wave 2 では残る136機種(dmmUrl/ichigekiUrl のいずれかが既に設定済み)について、
**そのURLが本当にその機種を指しているか**を検証した。136件を7バッチに分割し、順次Web調査エージェントで検証。

## 結果サマリ(136機種)
- MATCH_CONFIRMED(URLが正しく一致): 123
- MISMATCH(URLが別の機種を指していた): 9
- BROKEN_LINK(リンク切れ or 索引ページ): 4
- UNVERIFIABLE: 0

**結論: 136機種全て実在する本物の機種。捏造は0件。** 問題はURLの誤り(13件、全体の約9.6%)に限られる。

Wave 1(59件)+ Wave 2(136件)を合わせた **195機種全てが実在確認済み、捏造ゼロ**。

## MISMATCH(URLが別機種を指していた) — 9件
| file | 問題 | 修正案 |
|---|---|---|
| dmm_slot_002.json | ichigekiUrl が旧作「バイオハザード7」を指す | `https://1geki.jp/slot/l_bhr2/` |
| dmm_slot_005.json | ichigekiUrl が初代「スーパーリオエース」(2ではない)を指す | `https://1geki.jp/slot/l_sp_rioace2/` |
| funky_juggler_2.json | dmmUrl が無関係な「P真・一騎当千」を指す | `https://p-town.dmm.com/machines/3961` |
| gogo_juggler_3.json | dmmUrl が無関係な「e東京喰種」を指す | `https://p-town.dmm.com/machines/4375` |
| happy_juggler_v3.json | dmmUrl が無関係な「Pモンスターハンターライズ」を指す、ichigekiUrlは404 | dmm: `https://p-town.dmm.com/machines/4230` / ichigeki: `https://1geki.jp/slot/s_happyjuggler_v3/` |
| im_juggler_ex.json | dmmUrl が無関係な「PA戦国乙女レジェンドバトル」を指す | `https://p-town.dmm.com/machines/3626` |
| list_slot_044.json (ハナビ) | dmmUrl が旧型2015年版「ハナビ」を指す(ichigekiUrl・発売日は正しい新ハナビを指しており、ファイル内で不整合) | `https://p-town.dmm.com/machines/3911` |
| list_slot_055.json | dmmUrl が非スマート版「スターハナハナ-30」を指す | `https://p-town.dmm.com/machines/4701` |
| my_juggler_v.json | dmmUrl が無関係なパチンコ機、ichigekiUrlは404 | dmm: `https://p-town.dmm.com/machines/4029` / ichigeki: `https://1geki.jp/slot/s_myj5/` |

## BROKEN_LINK — 4件
| file | 問題 | 修正案 |
|---|---|---|
| dmm_slot_003.json | ichigekiUrl が「タ行」索引ページ(機種ページでない) | `https://1geki.jp/slot/l_takt_op_destiny/` |
| dmm_slot_004.json | ichigekiUrl が「ハ行」索引ページ | `https://1geki.jp/slot/l_bigdream/` |
| hokuto.json | ichigekiUrl が404 | `https://1geki.jp/slot/s_sma_hokutonoken/` |
| karakuri_circus.json | ichigekiUrl が404(からくりサーカス2ではなく初代のURLを使うこと) | `https://1geki.jp/slot/l_karakuri/` |

## 重要な系統的パターン: ジャグラー系列
`funky_juggler_2` / `gogo_juggler_3` / `happy_juggler_v3` / `im_juggler_ex` / `my_juggler_v` の **5機種全て**で
dmmUrlが完全に無関係な他機種(パチンコ機など)を指しており、かつ発売日が同一の
**「2025-01-01」という不自然なプレースホルダー値**になっていた(`my_juggler_v` は2021-12-06が正しい)。
これは個別の誤りではなく、ジャグラー系列(定番機種扱いのため個別データ収集が甘くなった可能性)に対する
**チャッピーの生成ロジックが系統的に破綻していたことを示す**。ハナハナ系列など他の定番シリーズも同様のリスクがあるため要注意。

## 発売日の副次的な誤り(URL問題とは別、note経由で判明)
| file | 記録値 | 実際 |
|---|---|---|
| list_slot_046.json | 2020-02-03 | 2020-04-20 |
| list_slot_047.json | 2025-09-01 | 2025-07-07 |
| list_slot_062.json | 2024-09-02 | 2024-07-08 |
| dmm_slot_006.json | 2026-06-08 | 2026-05-25 |
| bandori.json | null | 2024-11-05 |
| dmm_slot_013.json | manufacturer: "GINZA"(中古販売店名) | 正しくはSammy |
| dmm_slot_021/022/023/028/029/032/033/035/036/037/038/039/040 等 | manufacturer/releaseDate が null | Web調査で埋められる値あり(各バッチ結果参照) |

## 次のステップ候補
1. 上記13件のURL誤りを修正
2. ジャグラー系列5機種の発売日・メーカー名を正しい値に修正
3. その他の発売日/メーカー名の誤りを順次修正
4. Wave1で見つかった17件の誤り(既存レポート参照)と合わせて、まとめて一括修正パスを実施
