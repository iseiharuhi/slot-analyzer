# 機種精度監査メモ（2026-03-31）

この監査は現行パッケージ内のJSON・master情報・設計書を読み、状態不整合の補正と notes / machine_details の正規化を行ったもの。

## 実施内容

- machines.json の status を、各機種JSONの settingReferenceValues 有無と既存 verified 指定に合わせて再判定
- machines.json の checksumSha256 を再計算
- machines.json の count を実ファイル数に合わせて補正
- 各 machine_details の status / machineName / externalLinks を現JSONに同期
- 各機種JSONの machine.notes を status に合わせて正規化

## 集計

- verified: 3機種
- partial: 63機種
- draft: 37機種

## verified
- スマスロ BIRDIE WING -Golf Girls' Story- (dmm_slot_001)
- スマスロ 甲鉄城のカバネリ 海門（うなと）決戦 (dmm_slot_016)
- スマスロモンキーターンV (dmm_slot_027)

## partial
- A-SLOT+ ディスクアップ ULTRAREMIX (list_slot_049)
- A-SLOT+ 異世界かるてっと BT (dmm_slot_013)
- L ひぐらしのなく頃に 業 (dmm_slot_033)
- L エヴァンゲリオン ～未来への創造～ (dmm_slot_039)
- L リングにかけろ1 V (dmm_slot_024)
- LB スロット GALFY (dmm_slot_006)
- LBトリプルクラウンセブン (dmm_slot_010)
- Lアクダマドライブ (dmm_slot_014)
- Lコードギアス 反逆のルルーシュ (list_slot_003)
- Lコードギアス 復活のルルーシュ (list_slot_004)
- Lゴブリンスレイヤー (list_slot_001)
- Lパチスロ 炎炎ノ消防隊2 (dmm_slot_020)
- Lラブ嬢3〜Wご指名はいかがですか？〜 (dmm_slot_025)
- L大工の源さん超夢源 (dmm_slot_036)
- L戦国乙女4 戦乱に閃く炯眼の軍師 (list_slot_002)
- L範馬刃牙 (dmm_slot_019)
- L花の慶次～佐渡攻めの章〜 (dmm_slot_026)
- L虚構推理 (dmm_slot_012)
- Sちゅらちゅら (dmm_slot_032)
- アニマルスロット ドッチ (dmm_slot_008)
- アレックス ブライト (list_slot_047)
- キングハナハナ-30 (list_slot_059)
- クランキークレスト (dmm_slot_028)
- サンダーVライトニング (list_slot_046)
- スマスロ サンダーV (dmm_slot_015)
- スマスロ ハナビ (dmm_slot_017)
- スマスロ バイオハザードRE:3 (dmm_slot_002)
- スマスロ ビッグドリーム THE GOLDEN PUSHER (dmm_slot_004)
- スマスロ ミリオンゴッド-神々の軌跡- (dmm_slot_007)
- スマスロ 攻殻機動隊 (dmm_slot_018)
- スマスロとある魔術の禁書目録 (dmm_slot_034)
- スマスロスーパーリオエース2 (dmm_slot_005)
- スマスロヨルムンガンド (dmm_slot_011)
- スマスロ劇場版 魔法少女まどか☆マギカ[前編]始まりの物語／[後編]永遠の物語f-フォルテ- (dmm_slot_037)
- スマート沖スロ スターハナハナ (list_slot_055)
- スマート沖スロ ニューキングハナハナ-30 (list_slot_056)
- スマート沖スロ 沖ドキ！DUO-30 アンコール (list_slot_053)
- スーハナライジング-30 (dmm_slot_040)
- チバリヨ2プラス (list_slot_054)
- ディスクアップ2 (list_slot_048)
- デジスロ JAC INバージョン (dmm_slot_035)
- ドラゴンハナハナ～閃光～ 30 (list_slot_058)
- ニューキングハナハナ-30 (list_slot_057)
- ニューシオサイ (dmm_slot_022)
- ニューパルサーDX3 (list_slot_043)
- ニューパルサーSP4 (list_slot_042)
- パチスロ交響詩篇エウレカセブン HI-EVOLUTION ZERO TYPE-ART (dmm_slot_038)
- ハナハナホウオウ～天翔～-30 (list_slot_060)
- ハナビ (list_slot_044)
- バーサスリヴァイズ (list_slot_045)
- パチスロ ケンガンアシュラ (dmm_slot_031)
- パチスロ 炎炎ノ消防隊 (list_slot_025)
- パチスロ琉神−30 スイカバージョン (dmm_slot_023)
- ファミスタ回胴版!! (list_slot_050)
- マジカルハロウィン８ (dmm_slot_029)
- ミクちゃんとイドムンのミラクルチャレンジ2 (dmm_slot_021)
- 戦姫絶唱シンフォギア 正義の歌 (list_slot_062)
- 押忍！番長4 (list_slot_011)
- 沖ドキ！DUO (list_slot_052)
- 沖ドキ！GOLD (list_slot_051)
- 真打 吉宗 (dmm_slot_009)
- 麻雀物語 (list_slot_061)
- Ｌタクトオーパス デスティニー (dmm_slot_003)

## draft
- EVANGELION FESTIVAL (list_slot_026)
- FAIRY TAIL2 (list_slot_032)
- Lありふれた職業で世界最強 (list_slot_022)
- Lうしおととら 白面決戦 (list_slot_018)
- Lにゃんこ大戦争 超神速 (list_slot_024)
- Lアズールレーン THE ANIMATION (list_slot_015)
- Lゴジラ (list_slot_019)
- Lストライクウィッチーズ2 (list_slot_008)
- Lダーリン・イン・ザ・フランキス (list_slot_016)
- Lパチスロ シン・エヴァンゲリオン (list_slot_023)
- Lパチスロうみねこのなく頃に2 (list_slot_063)
- Lベルセルク無双 (list_slot_010)
- Lルパン三世 大航海者の秘宝 (list_slot_040)
- L主役は銭形4 (list_slot_007)
- L仮面ライダー電王 (list_slot_021)
- L南国育ち (list_slot_012)
- L咲-Saki- 頂上決戦 (list_slot_017)
- L少女☆歌劇 レヴュースタァライト -The SLOT- (list_slot_020)
- L東京喰種 (list_slot_014)
- L転生したらスライムだった件 (list_slot_005)
- L麻雀格闘倶楽部 覚醒 (list_slot_009)
- Re:ゼロから始める異世界生活 season2 (list_slot_038)
- S BOØWY (list_slot_035)
- Sアクエリオン ALL STARS (list_slot_027)
- この素晴らしい世界に祝福を！ (list_slot_039)
- スマスロ シャーマンキング (list_slot_030)
- スマスロ バイオハザード RE:2 (list_slot_006)
- スマスロ痛いのは嫌なので防御力に極振りしたいと思います。 (list_slot_013)
- バイオハザード ヴィレッジ (list_slot_036)
- パチスロ ダンジョンに出会いを求めるのは間違っているだろうか2 (list_slot_037)
- パチスロ幼女戦記 (list_slot_029)
- パチスロ盾の勇者の成り上がり (list_slot_034)
- ペルソナ5 (list_slot_033)
- 推しスロ アイドルVer. (dmm_slot_030)
- 新鬼武者2 (list_slot_031)
- 鉄拳5 (list_slot_028)
- 黄門ちゃま天 (list_slot_041)

## 補足

- この差分は状態不整合の是正と現行データの保守しやすさ向上が目的。
- 公開解析の新規採用そのものを全機種一斉に増やしたわけではない。
- ただし、既に比較値が入っているのに draft のままだった機種は partial に補正済み。
