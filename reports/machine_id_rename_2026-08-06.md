# Machine file/id rename pass (2026-08-06)

## 背景
`dmm_slot_001`〜`dmm_slot_040`(34件)・`list_slot_003`〜`list_slot_063`(40件)、計74件が
中身と無関係な連番ファイル名のままだった(例: `dmm_slot_037.json` が実は「まどか☆マギカ f-フォルテ-」)。
実機で確認したところ、これらのIDに紐づく実践セッションはテスト用データのみだったため、
`id`フィールドも含めて完全にリネームした。

## 実施内容
1. 74件を中身のわかる説明的なsnake_caseスラッグに一括リネーム(対応表は本レポート末尾)
2. `app/src/main/assets/machines/*.json` と `app/src/main/assets/master/machine_details/*.json` の
   両方で、ファイル名と内部の`"id"`フィールドを同時に変更
3. `app/src/main/assets/master/machines.json` 内の該当74エントリの`id`も同期

## 途中で見つかった副次的な問題
- リネームスクリプトの正規表現が一部ファイル(ダブルスペース区切りでフォーマットされていた
  `list_slot_003`/`004`/`040`)にマッチせず、ファイル名だけ変わって内部`id`が旧IDのまま残る不具合が発生。
  検証パスで発見し、3件とも手動修正。
- `master/machines.json` に元から存在した壊れたスタブエントリ(`name`が`id`と同じ文字列になっている
  プレースホルダー)3件(`king_hanahana_30`, `oki_doki_duo`, `oki_doki_gold`)が、リネーム後のIDと衝突。
  スタブ側を削除して解消。
- `master/machines.json` にはこれ以外にも、`assets/machines/`に対応ファイルが存在しない孤立エントリが
  複数見つかった(`dmm_slot_024`, `dmm_slot_016`, `dmm_slot_018`, `dmm_slot_027`, `dmm_slot_034`,
  `list_slot_038`, `list_slot_056`, `list_slot_057`, `list_slot_061` 等)。これは既にチップ化済みの
  「master/machine_details 並行ストア監査」タスクのスコープなので、今回は触っていない。

## 検証
- 全190ファイルで「ファイル名 = 内部id」の完全一致を確認(0件の不一致)
- `master/machine_details/` も同様に0件の不一致
- `master/machines.json` の重複id 0件
- 監査スクリプト再実行: 190/190件有効、破損0件、genericNamed(連番プレースホルダー名)0件

## 今後の運用
- `scripts/audit_machines.ps1` をリポジトリに追加した。今後の一括編集後はこれを実行して確認する。
- 命名規則・重複チェックの手順を `CLAUDE.md` に明文化した(今後機種が増えても同じ問題が起きないように)。

## リネーム対応表(74件)
| 旧ID | 新ID | 機種名 |
|---|---|---|
| dmm_slot_001 | birdie_wing | スマスロ BIRDIE WING -Golf Girls' Story- |
| dmm_slot_002 | biohazard_re3 | スマスロ バイオハザードRE:3 |
| dmm_slot_003 | takt_op_destiny | Ｌタクトオーパス デスティニー |
| dmm_slot_004 | big_dream_golden_pusher | スマスロ ビッグドリーム THE GOLDEN PUSHER |
| dmm_slot_005 | super_rio_ace_2 | スマスロスーパーリオエース2 |
| dmm_slot_006 | galfy | LB スロット GALFY |
| dmm_slot_007 | million_god_kamigami | スマスロ ミリオンゴッド-神々の軌跡- |
| dmm_slot_008 | animal_slot_docchi | アニマルスロット ドッチ |
| dmm_slot_009 | shinuchi_yoshimune | 真打 吉宗 |
| dmm_slot_010 | triple_crown_seven | LBトリプルクラウンセブン |
| dmm_slot_011 | jormungand | スマスロヨルムンガンド |
| dmm_slot_012 | kyokou_suiri | L虚構推理 |
| dmm_slot_013 | isekai_quartet_bt | A-SLOT+ 異世界かるてっと BT |
| dmm_slot_014 | akudama_drive | Lアクダマドライブ |
| dmm_slot_015 | thunder_v | スマスロ サンダーV |
| dmm_slot_017 | l_hanabi | スマスロ ハナビ |
| dmm_slot_020 | fire_force_2 | Lパチスロ 炎炎ノ消防隊2 |
| dmm_slot_021 | miku_idomu_challenge_2 | ミクちゃんとイドムンのミラクルチャレンジ2 |
| dmm_slot_022 | new_shiosai | ニューシオサイ |
| dmm_slot_023 | ryujin_30_suika | パチスロ琉神−30 スイカバージョン |
| dmm_slot_025 | lovejo_3 | Lラブ嬢3〜Wご指名はいかがですか？〜 |
| dmm_slot_026 | keiji_sado | L花の慶次～佐渡攻めの章〜 |
| dmm_slot_028 | cranky_crest | クランキークレスト |
| dmm_slot_029 | magical_halloween_8 | マジカルハロウィン８ |
| dmm_slot_030 | oshi_slo_idol | 推しスロ アイドルVer. |
| dmm_slot_031 | kengan_ashura | パチスロ ケンガンアシュラ |
| dmm_slot_032 | chura_chura | Sちゅらちゅら |
| dmm_slot_033 | higurashi_gou | L ひぐらしのなく頃に 業 |
| dmm_slot_035 | digislo_jac_in | デジスロ JAC INバージョン |
| dmm_slot_036 | daiku_gensan_mugen | L大工の源さん超夢源 |
| dmm_slot_037 | madoka_forte | スマスロ劇場版 魔法少女まどか☆マギカ...f-フォルテ- |
| dmm_slot_038 | eureka_zero | パチスロ交響詩篇エウレカセブン HI-EVOLUTION ZERO TYPE-ART |
| dmm_slot_039 | eva_mirai_souzou | L エヴァンゲリオン ～未来への創造～ |
| dmm_slot_040 | suhana_rising_30 | スーハナライジング-30 |
| list_slot_003 | code_geass_revival | スマスロ コードギアス 反逆のルルーシュ／復活のルルーシュ |
| list_slot_004 | code_geass_3_cc_kallen | パチスロ コードギアス 反逆のルルーシュ3 C.C.\&Kallen ver. |
| list_slot_009 | mahjong_fight_club_kakusei | L麻雀格闘倶楽部 覚醒 |
| list_slot_010 | berserk_musou | Lベルセルク無双 |
| list_slot_012 | nangoku_sodachi | L南国育ち |
| list_slot_013 | bofuri | スマスロ痛いのは嫌なので防御力に極振りしたいと思います。 |
| list_slot_026 | evangelion_festival | EVANGELION FESTIVAL |
| list_slot_027 | aquarion_all_stars | パチスロ アクエリオン ALL STARS |
| list_slot_028 | tekken_5 | パチスロ鉄拳5 |
| list_slot_029 | youjo_senki | パチスロ幼女戦記 |
| list_slot_030 | shaman_king | スマスロ シャーマンキング |
| list_slot_031 | shin_onimusha_2 | パチスロ 新鬼武者2 |
| list_slot_032 | fairy_tail_2 | FAIRY TAIL2 |
| list_slot_033 | persona_5 | ペルソナ5 |
| list_slot_034 | tate_no_yuusha | パチスロ盾の勇者の成り上がり |
| list_slot_035 | boowy | S BOØWY |
| list_slot_036 | biohazard_village | バイオハザード ヴィレッジ |
| list_slot_037 | danmachi_2 | パチスロ ダンジョンに出会いを求めるのは間違っているだろうか2 |
| list_slot_039 | konosuba | この素晴らしい世界に祝福を！ |
| list_slot_040 | lupin_treasure_voyager | Lルパン三世 大航海者の秘宝 |
| list_slot_041 | komonchama_ten | 黄門ちゃま天 |
| list_slot_042 | new_pulsar_sp4 | ニューパルサーSP4 |
| list_slot_043 | new_pulsar_dx3 | ニューパルサーDX3 |
| list_slot_044 | shin_hanabi | ハナビ(新ハナビ) |
| list_slot_045 | versus_rexse | バーサスリヴァイズ |
| list_slot_046 | thunder_v_lightning | サンダーVライトニング |
| list_slot_047 | alex_bright | アレックス ブライト |
| list_slot_048 | disc_up_2 | ディスクアップ2 |
| list_slot_049 | disc_up_ultraremix | A-SLOT+ ディスクアップ ULTRAREMIX |
| list_slot_050 | famista_kaidou | ファミスタ回胴版!! |
| list_slot_051 | oki_doki_gold | 沖ドキ！GOLD |
| list_slot_052 | oki_doki_duo | 沖ドキ！DUO |
| list_slot_053 | oki_doki_duo_encore | スマート沖スロ 沖ドキ！DUO-30 アンコール |
| list_slot_054 | chibariyo_2_plus | チバリヨ2プラス |
| list_slot_055 | star_hanahana | スマート沖スロ スターハナハナ |
| list_slot_058 | dragon_hanahana_senko_30 | ドラゴンハナハナ～閃光～ 30 |
| list_slot_059 | king_hanahana_30 | キングハナハナ-30 |
| list_slot_060 | hanahana_houou_tensho_30 | ハナハナホウオウ～天翔～-30 |
| list_slot_062 | symphogear | 戦姫絶唱シンフォギア 正義の歌 |
| list_slot_063 | umineko_2 | Lパチスロうみねこのなく頃に2 |
