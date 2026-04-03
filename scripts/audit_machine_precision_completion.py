import csv
import json
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MACHINES_DIR = ROOT / "app" / "src" / "main" / "assets" / "machines"
MANIFEST_PATH = MACHINES_DIR / "machine_master_manifest.json"
OUT_CSV = ROOT / "reports" / "full_precision_completion_audit_regenerated.csv"

manifest = json.loads(MANIFEST_PATH.read_text(encoding="utf-8-sig"))
manifest_map = {m.get("id", ""): m for m in manifest.get("machines", [])}
manifest_files = {m.get("fileName", "") for m in manifest.get("machines", [])}
rows = []

for fn in sorted(os.listdir(MACHINES_DIR)):
    if not fn.endswith('.json') or fn.endswith('.json.bak') or fn == 'machine_master_manifest.json':
        continue
    path = MACHINES_DIR / fn
    raw = path.read_bytes()
    bom = raw.startswith(b'ï»¿')
    try:
        data = json.loads(raw.decode('utf-8-sig'))
    except Exception as e:
        rows.append({
            'file': fn,
            'id': '',
            'name': '',
            'schema': 'invalid_json',
            'bom': bom,
            'manifest_registered': fn in manifest_files,
            'counter_count': '',
            'reference_count': '',
            'settings': '',
            'has_dmm': '',
            'has_ichigeki': '',
            'has_ceiling': '',
            'missing_fields': 'invalid_json',
            'status': 'ERROR',
            'note': str(e),
        })
        continue

    schema = 'unknown'
    machine_id = ''
    name = ''
    counter_count = 0
    ref_count = 0
    settings = ''
    has_dmm = False
    has_ichigeki = False
    has_ceiling = False
    missing = []

    if 'machine' in data and 'counterDefinitions' in data and 'settingReferenceValues' in data:
        schema = 'full_legacy'
        machine = data['machine']
        machine_id = machine.get('id', '')
        name = machine.get('name', '')
        counter_count = len(data.get('counterDefinitions', []))
        ref_count = len(data.get('settingReferenceValues', []))
        st = set()
        for ref in data.get('settingReferenceValues', []):
            for value in ref.get('values', []):
                if isinstance(value, dict) and 'settingNo' in value:
                    st.add(value['settingNo'])
        settings = ','.join(map(str, sorted(st)))
        has_dmm = bool(machine.get('dmmUrl'))
        has_ichigeki = bool(machine.get('ichigekiUrl'))
        has_ceiling = bool(data.get('ceilingRules'))
        if not has_dmm:
            missing.append('dmmUrl')
        if not has_ichigeki:
            missing.append('ichigekiUrl')
        if counter_count == 0:
            missing.append('counterDefinitions')
        if ref_count == 0:
            missing.append('settingReferenceValues')
    elif 'id' in data and 'name' in data and 'settingReferenceValues' in data:
        schema = 'simple_flat'
        machine_id = data.get('id', '')
        name = data.get('name', '')
        ref_count = len(data.get('settingReferenceValues', []))
        st = set()
        for ref in data.get('settingReferenceValues', []):
            values = ref.get('values', [])
            if isinstance(values, list):
                for i, _ in enumerate(values, start=1):
                    st.add(i)
        settings = ','.join(map(str, sorted(st)))
        missing.extend(['machine', 'counterDefinitions'])
    else:
        missing.append('unrecognized_schema')

    manifest_registered = fn in manifest_files or (machine_id in manifest_map)
    if not manifest_registered:
        missing.append('manifest')

    rows.append({
        'file': fn,
        'id': machine_id,
        'name': name,
        'schema': schema,
        'bom': bom,
        'manifest_registered': manifest_registered,
        'counter_count': counter_count,
        'reference_count': ref_count,
        'settings': settings,
        'has_dmm': has_dmm,
        'has_ichigeki': has_ichigeki,
        'has_ceiling': has_ceiling,
        'missing_fields': '|'.join(missing),
        'status': 'OK' if not missing else 'WARNING',
        'note': '',
    })

OUT_CSV.parent.mkdir(parents=True, exist_ok=True)
with OUT_CSV.open('w', encoding='utf-8', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
    writer.writeheader()
    writer.writerows(rows)

print(f'generated: {OUT_CSV}')
