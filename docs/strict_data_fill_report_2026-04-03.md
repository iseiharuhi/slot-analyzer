# strict data fill report (2026-04-03)

対象:
- list_slot_064 スマスロ 北斗の拳 転生の章2
- list_slot_065 Lパチスロ 革命機ヴァルヴレイヴ2

実施内容:
- 天井情報を ceilingRules に追加
- 総ゲーム数分母で安全に運用できる settingReferenceValues を追加
- master/machines.json, master/version.json, machine_details を更新
- 近似名称ではなくメーカー正式名称の完全一致で別機種として維持

採用方針:
- 北斗転生2は AT初当り確率のみ公開比較値を採用
- ヴァルヴレイヴ2は 初当り(革命ボーナス+AT直撃)合算のみ採用
- BAR揃い時AT直撃率のような「総ゲーム数分母でない指標」はメモ用カウンタに留めた
- リセット短縮は ceilingRules の description に統合し、入力欄の重複を避けた
