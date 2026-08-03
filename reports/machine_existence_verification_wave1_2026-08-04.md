# Machine existence verification — Wave 1 (2026-08-04)

## 背景
既存の機種JSON(196件)は全て前任のAIセッション(通称チャッピー)が生成したもので、内容の信頼性が担保されていない。
最初のステップとして、`dmmUrl`・`ichigekiUrl` が両方とも未設定(=既存の裏付けが一切ない)59機種を対象に、
Web調査で実在確認を行った。59機種を3バッチに分割し、並列の調査エージェントで検証。

## 結果サマリ
- 対象: 59機種(dmmUrl/ichigekiUrl 両方null)
- REAL_CONFIRMED: 58
- REAL_LIKELY: 1 (`frame_arms_girls.json` — 正式名称は「フレームアームズ・ガール」、登録名「フレイムアームガールズ」は表記ゆれ。同一機種の可能性大)
- NOT_FOUND_SUSPICIOUS(捏造疑い): 0
- AMBIGUOUS: 0(ただし note 参照。`ring_ni_kakero_1.json` は3バージョン中どれか特定不能)

**結論: このグループに捏造機種は見つからなかった。** 機種名そのものは信頼できる。

## 見つかった副次的な問題(メタデータの誤り)
機種の実在性とは別に、以下のフィールドが実際と食い違っていることが判明した。今後の修正候補:

| file | 問題 |
|---|---|
| ao_no_exorcist.json | メーカー誤り(サボハニ→正しくはOLYMPIA)、発売日誤り(2020-08-03→正しくは2021-03-08) |
| akame_ga_kill.json | メーカー誤り(サミー→正しくはNANASHOW)、発売日誤り(2020-03-02→正しくは2020-11-09) |
| aldnoah_zero.json | メーカー誤り(ユニバーサルブロス→正しくはELECO) |
| black_lagoon_4.json | 発売日が別バージョン(パチンコ版)のものになっている可能性 |
| drifters.json | 発売日誤り(2022-08-08→正しくは2022-02-07) |
| godzilla_vs_eva.json | 正式表記は「対」であり「VS」ではない、発売日誤り(→2024-02-05) |
| eva_tamashii_no_kyoumei.json | 正式タイトルには「新世紀...」の接頭辞がある |
| no_game_no_life.json | 発売日誤り(2021-04-05→正しくは2021-06-14) |
| rakuen_tsuihou.json | 発売日誤り(2021-06-07→正しくは2021-09-06) |
| railgun.json | メーカー誤り(JFJ→正しくは藤商事) |
| tiger_bunny_sp.json | 発売日誤り(2022-01-11→正しくは2022-03-07) |
| warau_salesman_4.json | 発売日誤り(2022-12-05→正しくは2022-10-03) |
| warau_salesman_zesshou.json | 発売日誤り(2025-01-20→正しくは2020-10-05、約4.5年のズレ) |
| yoshimune_3.json | 発売日誤り(2021-10-04→正しくは2020-10-05頃) |
| prism_nana.json | 誤った「L」接頭辞が付いている(正式には非L機種) |
| frame_arms_girls.json | 名称の表記ゆれ(正式名称「フレームアームズ・ガール」) |
| ring_ni_kakero_1.json | 同名シリーズが複数バージョン存在(2007/2020/2023)、どれを指すか特定不能 |

## 次のステップ候補
1. 上記59機種について、調査で見つかったURL(各エージェント報告内)を dmmUrl/ichigekiUrl に反映
2. メーカー名・発売日の誤りを修正
3. `ring_ni_kakero_1.json` はどのバージョンを指すか要確認(ユーザー判断)
4. Wave 2: dmmUrl/ichigekiUrl が既にある136機種についても、URLが実際にその機種を指しているか(名前とURL先の機種が一致しているか)の検証が未実施
