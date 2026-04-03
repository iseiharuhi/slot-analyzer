# ノーマル機 精度仕上げ Phase6

## 実施内容
- WARNING判定だったノーマル系9機種に DMMぱちタウンURL を補完
- JSON構造は変更せず、`machine.dmmUrl` のみ追加
- 既存の ichigekiUrl / カウンタ / 推測参照値は維持

## 更新対象
- ニューパルサーSP4
- ニューパルサーDX3
- ハナビ
- バーサスリヴァイズ
- サンダーVライトニング
- アレックス ブライト
- ディスクアップ2
- A-SLOT+ ディスクアップ ULTRAREMIX
- ファミスタ回胴版!!

## 期待効果
- 監査CSV上の `missing_fields = dmmUrl` を解消
- ノーマル機の WARNING 群を削減
- リンク表示の均一性を改善
