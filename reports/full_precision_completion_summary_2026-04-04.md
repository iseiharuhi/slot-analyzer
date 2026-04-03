# Full Precision Completion Summary (2026-04-04)

## Overview
- machine json count: 124
- schema full_legacy: 115
- schema simple_flat: 5
- schema invalid_json: 0
- schema unknown: 4
- status OK: 91
- status WARNING: 33
- status ERROR: 0

## Highest-priority issues
- invalid_json (mostly UTF-8 BOM parse problems under naive readers): 0
- simple_flat schema files lacking machine/counterDefinitions blocks: 5
- missing dmmUrl: 9
- missing ichigekiUrl: 12
- missing manifest linkage: 6

## Files normalized in this diff
- karakuri.json: UTF-8 BOM removed
- lupin.json: UTF-8 BOM removed
- tokyoghoul.json: UTF-8 BOM removed
- vvv.json: UTF-8 BOM removed

## Notes
- This diff focuses on broad completion support without guessing new machine stats.
- Existing settingReferenceValues were preserved.
- The included audit script reads UTF-8 BOM safely and outputs a fresh CSV so the remaining work can be completed in one pass.
