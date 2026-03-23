# CHANGELOG v0.4 (設計書更新版)

## 今回の反映内容
- sessionId ベースの画面遷移へ設計書を更新
- SessionInput / Inference / HistoryList / HistoryDetail の実装仕様を反映
- play_sessions の状態管理項目 (is_finished / is_current / ended_at / last_inference_summary / last_confidence_label) を設計書へ反映
- 推測結果スナップショット保存の流れを追記
- 実戦終了と履歴からの再開フローを追記
- assets JSON + Seeder + masterVersion による機種マスタ投入仕様を反映
- Git へ反映するためのブランチ運用例を追加

## 現在の実装上のポイント
- 機種選択では「新規実戦開始」と「続きから再開」を分離
- 実戦入力は機種ごとに必要な項目だけ表示
- 推測結果では設定1-6バーと設定帯バーを併用
- 履歴一覧は終了済みセッションを中心に表示
- 履歴詳細からセッション再開が可能

## 推奨コミットメッセージ
docs: refresh design spec and revision list
