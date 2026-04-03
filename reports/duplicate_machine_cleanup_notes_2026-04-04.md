# Duplicate machine cleanup notes

This diff zip contains:
- patched `machine_master_manifest.json`
- patched `master/machines.json`
- patched `master/version.json`
- bulk delete scripts for obsolete duplicate machine JSON files
- a CSV listing each keep/delete decision

## Keep / delete policy
Keep the canonical named JSON file and delete the older duplicate file when:
- the old file uses a legacy `list_slot_*` ID
- the old file uses a legacy `dmm_slot_*` ID but a canonical named file already exists
- the old file is an older alias (`enen.json`) and a better canonical file (`fire_force.json`) exists

## Delete targets count
- legacy duplicate files: 20
- extra old alias files: 1
- total delete targets: 21

## Important
Apply the patched manifest/master files **together with** running one of the delete scripts.
If you delete files without applying the manifest/master patch, some machines could disappear from the list.
