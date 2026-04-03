# JSONアクションプラン Phase2（2026-04-04）

## 今回の結論

### すぐ削除してよい旧別名JSON
- karakuri.json → karakuri_circus.json に統一
- tokyoghoul.json → tokyo_ghoul.json に統一
- vvv.json → valvrave.json に統一

### まだ削除してはいけない再構成候補
- lupin.json
- code_geass_cc_kallen.json
- code_geass_fukkatsu.json
- eureka_zero.json
- ghost_shell.json
- kabaneri_kaimon.json
- kengan.json
- lovejo3.json
- madoka_forte.json
- monkey_turn_v.json
- ring_v.json
- toaru.json

## 重要メモ
- `lupin.json` は旧名っぽく見えるが、現パッケージ内に対応するmanifest正本が見当たらないため、現時点では削除不可。
- `karakuri.json` / `tokyoghoul.json` / `vvv.json` は manifest側の正本が存在するため削除して安全。
- 次フェーズは「再構成候補12件のうち、正本採用する機種から順に manifest 追加 + 精度強化」に入る。
