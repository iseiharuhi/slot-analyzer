# precision_batch_2026-03-31_next6b

対象:
- EVANGELION FESTIVAL
- Sアクエリオン ALL STARS
- 鉄拳5
- パチスロ幼女戦記
- スマスロ シャーマンキング
- 新鬼武者2

方針:
- 既存JSONの counterDefinitions を維持
- ceilingRules は空からの追加のみ（既存破壊なし）
- settingReferenceValues は公開比較値のうち、総ゲーム数分母の実戦入力に落としやすい軸だけ採用
- 状態別・モード別・終了画面系は notes に留め、数値化を見送り

採用概要:
- EVANGELION FESTIVAL: CZ+AT初当り合算 + 7周期天井
- アクエリオン ALL STARS: AT初当り + CZトータル + 1000G天井 + CZスルー天井
- 鉄拳5: ボーナス合算 + 750G天井
- 幼女戦記: BONUS合算 + AT初当り + 512G天井
- シャーマンキング: 通常時初当り + AT初当り + 800G天井 + ATスルー天井
- 新鬼武者2: AT初当り + 直撃AT + BZ間/BZスルー天井

反映時メモ:
- パッケージ階層そのままで上書き
- assets更新後はアプリ削除 or データ削除
- machine_master_manifest.json は未変更
