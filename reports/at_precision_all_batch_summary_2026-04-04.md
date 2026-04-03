# AT精度一括調整 Phase11

- 更新ファイル数: 73
- 対象: AT/BT系・6.5号機AT系として現パッケージに存在するJSONを一括更新
- full_machine: 既存の分母値は維持し、weight / minSampleSize / note を保守寄りに調整
- legacy_simple: inferenceElements の weight / minSampleSize を保守寄りに調整し、低サンプル注意の notes を追加
- 構造変更なし。既存キー・ID・分母値は変更していない
- 重複/別名JSONも現パッケージ内整合のため同時更新している
