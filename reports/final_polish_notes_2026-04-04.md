# Final polish notes (2026-04-04)

Included in this diff:

1. `app/src/main/assets/machines/machine_master_manifest.json`
   - `list_slot_064` → `hokuto_tensei_2`
   - `list_slot_065` → `valvrave_2`
   - checksum updated to current file hashes
   - duplicate machineId entries removed

2. `app/src/main/assets/master/machines.json`
   - normalized the same legacy entries
   - `count` recalculated after dedupe

3. `app/src/main/assets/master/version.json`
   - updated release note and master data version

4. `scripts/remove_rogue_machine_jsons.ps1`
   - cleanup helper for four non-schema placeholder JSON files currently present in `assets/machines/`
   - target files:
     - `karakuri.json`
     - `lupin.json`
     - `tokyoghoul.json`
     - `vvv.json`

Current checksums used:
- `hokuto_tensei_2.json`: `a2af024ef2831bd843c7f7204e10cbca4e77f566177c3502f1b9d23ea9aa19f7`
- `valvrave_2.json`: `142006b72c46b4de3141bdbabeb110104e621ea8c1c67043c1bef2a9521259d6`
