# precision_priority_batch_2026-04-01-c

今回の差分は、精度優先で8機種を更新したバッチです。manifest は未変更です。

## 更新対象
- list_slot_040 / Lルパン三世 大航海者の秘宝
- list_slot_006 / スマスロ バイオハザード RE:2
- list_slot_005 / L転生したらスライムだった件
- list_slot_014 / L東京喰種
- list_slot_034 / パチスロ盾の勇者の成り上がり
- list_slot_036 / バイオハザード ヴィレッジ
- list_slot_037 / パチスロ ダンジョンに出会いを求めるのは間違っているだろうか2
- list_slot_038 / Re:ゼロから始める異世界生活 season2

## 主な修正方針
- AT/ART初当りを主軸に寄せてウェイトを再整理
- 重い分母の直撃要素は低ウェイト補助として採用、またはメモ用途へ後退
- UIで扱いにくい項目は notes 側へ退避
- 東京喰種は DMM/一撃URL、AT初当り、AT直撃、天井を追加して draft→partial へ引き上げ
- ダンまち2は ART初当りカウンタと参照値を追加

## 反映対象
- app/src/main/assets/machines/*.json
- app/src/main/assets/master/machine_details/*.json
- app/src/main/assets/master/machines.json

## 反映後
- assets 更新後はアプリ削除またはデータ削除を実施
