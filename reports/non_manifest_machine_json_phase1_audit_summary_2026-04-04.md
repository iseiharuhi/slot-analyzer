# Non-manifest machine JSON phase1 audit (2026-04-04)

## Purpose
Current `assets/machines/` still contains JSON files that are **not referenced by `machine_master_manifest.json`**.
This audit classifies those files into:
- 削除対象
- 再構成候補

## Summary
- non-manifest JSON count: 15
- 削除対象: 4
- 再構成候補: 11

## 削除対象 (phase1)
These are placeholder / old alias files that should be removed with the bulk delete script:
- `karakuri.json` → keep `karakuri_circus.json`
- `lupin.json` → keep `list_slot_040.json`
- `tokyoghoul.json` → keep `tokyo_ghoul.json`
- `vvv.json` → keep `valvrave.json`

## 再構成候補
These are non-manifest files worth reviewing when the project enters the precision-strengthening phase. They should **not** replace current manifest IDs directly. Instead, reuse their contents only if they improve the existing canonical JSON.
- `code_geass_cc_kallen.json` ↔ current canonical `list_slot_012.json`
- `code_geass_fukkatsu.json` ↔ current canonical `list_slot_013.json`
- `eureka_zero.json` ↔ current canonical `list_slot_028.json`
- `ghost_shell.json` ↔ current canonical `list_slot_027.json`
- `kabaneri_kaimon.json` ↔ current canonical `list_slot_063.json`
- `kengan.json` ↔ current canonical `list_slot_026.json`
- `lovejo3.json` ↔ current canonical `list_slot_029.json`
- `madoka_forte.json` ↔ current canonical `list_slot_010.json`
- `monkey_turn_v.json` ↔ current canonical `dmm_slot_027.json`
- `ring_v.json` ↔ current canonical `dmm_slot_024.json`
- `toaru.json` ↔ current canonical `list_slot_030.json`

## Notes
- This phase does **not** change IDs.
- This phase does **not** rewrite manifest/master yet.
- This phase only isolates obvious delete targets and reconstruct candidates.
