from pathlib import Path
import json, csv

base = Path("app/src/main/assets/machines")
rows = []
for p in sorted(base.glob("*.json")):
    data = json.loads(p.read_text(encoding="utf-8-sig"))
    schema = "unknown"
    idv = name = ""
    counters = refs = ceilings = 0
    if isinstance(data, dict):
        if "machine" in data and "counterDefinitions" in data:
            schema = "full_machine"
            idv = data.get("machine", {}).get("id", "")
            name = data.get("machine", {}).get("name", "")
            counters = len(data.get("counterDefinitions") or [])
            refs = len(data.get("settingReferenceValues") or [])
            ceilings = len(data.get("ceilingRules") or [])
        elif "settingReferenceValues" in data:
            schema = "legacy_simple"
            idv = data.get("id", "")
            name = data.get("name", "")
            refs = len(data.get("settingReferenceValues") or [])
        elif p.name == "machine_master_manifest.json":
            schema = "manifest"
    rows.append({
        "file": p.name,
        "schema": schema,
        "id": idv,
        "name": name,
        "counter_count": counters,
        "reference_count": refs,
        "ceiling_rule_count": ceilings,
    })

out = Path("reports") / "full_machine_inventory_regenerated.csv"
out.parent.mkdir(parents=True, exist_ok=True)
with out.open("w", encoding="utf-8-sig", newline="") as f:
    w = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
    w.writeheader()
    w.writerows(rows)
print(out)
