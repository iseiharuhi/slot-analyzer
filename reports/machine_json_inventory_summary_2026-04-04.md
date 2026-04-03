# Machine JSON inventory summary (2026-04-04)

    Source package: current uploaded `SlotSettingAnalyzerCompleteV2.zip`

    ## Counts
    - machine JSON files (excluding manifest): 151
    - manifest registrations: 115
    - master registrations: 105
    - machine details files present: 115
    - duplicate machine-name groups: 20
    - legacy ID files (`list_slot_*` / `dmm_slot_*`): 103

    ## Schema breakdown
    - modern_inference: 27
- simple_reference: 5
- legacy_full: 115
- placeholder_or_other: 4

    ## Included CSVs
    - `machine_json_inventory_full_2026-04-04.csv`
      - one row per machine JSON file
      - registration state, schema type, duplicate state, counters, setting elements, site links, and ceiling coverage
    - `machine_json_elements_full_2026-04-04.csv`
      - one row per setting/inference element
      - keys, labels, setting coverage, weights, min sample sizes, preview values
    - `machine_json_counters_full_2026-04-04.csv`
      - one row per counter definition
    - `machine_json_ceiling_rules_full_2026-04-04.csv`
      - one row per ceiling rule
    - `machine_json_duplicate_names_2026-04-04.csv`
      - duplicate machine names and the backing file/id sets

    ## Main purpose for next chat
    This export is intended as a handoff pack so the next chat can inspect:
    - what each JSON contains
    - whether counters / setting elements / links / ceiling rules are present
    - where duplicate machine names still exist
    - which files are legacy vs canonical
