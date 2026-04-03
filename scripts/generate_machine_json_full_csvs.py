#!/usr/bin/env python3
import os, json, csv, glob, collections

PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
machines_dir = os.path.join(PROJECT_ROOT, 'app', 'src', 'main', 'assets', 'machines')
master_dir = os.path.join(PROJECT_ROOT, 'app', 'src', 'main', 'assets', 'master')
details_dir = os.path.join(master_dir, 'machine_details')
reports_dir = os.path.join(PROJECT_ROOT, 'reports')
os.makedirs(reports_dir, exist_ok=True)

with open(os.path.join(machines_dir, 'machine_master_manifest.json'), encoding='utf-8-sig') as f:
    manifest = json.load(f)
manifest_by_file = {x.get('fileName'): x for x in manifest.get('machines', [])}
manifest_by_id = {x.get('machineId'): x for x in manifest.get('machines', [])}
with open(os.path.join(master_dir, 'machines.json'), encoding='utf-8-sig') as f:
    master = json.load(f)
master_by_file = {x.get('fileName'): x for x in master.get('machines', [])}
master_by_id = {x.get('machineId'): x for x in master.get('machines', [])}
detail_files = {os.path.basename(p) for p in glob.glob(os.path.join(details_dir, '*.json'))}

def safe_join(vals, sep='|'):
    vals = [str(v) for v in vals if v not in (None, '', [], {})]
    return sep.join(vals)

machine_files = [p for p in sorted(glob.glob(os.path.join(machines_dir, '*.json'))) if os.path.basename(p) != 'machine_master_manifest.json']
id_to_files = collections.defaultdict(list)
name_to_files = collections.defaultdict(list)
cache = {}
for path in machine_files:
    fn = os.path.basename(path)
    data = json.load(open(path, encoding='utf-8-sig'))
    cache[fn] = data
    mid = data.get('id') or data.get('machine', {}).get('id')
    name = data.get('name') or data.get('machine', {}).get('name')
    id_to_files[mid].append(fn)
    name_to_files[name].append(fn)

rows = []; element_rows=[]; counter_rows=[]; ceiling_rows=[]; dup_rows=[]
for path in machine_files:
    fn = os.path.basename(path); data = cache[fn]
    if 'machine' in data:
        schema_type='legacy_full'; machine=data.get('machine',{}); mid=machine.get('id'); name=machine.get('name'); manufacturer=machine.get('manufacturer'); mtype=machine.get('type'); release_date=machine.get('releaseDate'); is_active=machine.get('isActive'); machine_notes=machine.get('notes'); dmm_url=machine.get('dmmUrl'); ichigeki_url=machine.get('ichigekiUrl'); counters=data.get('counterDefinitions',[]) or []; elements=data.get('settingReferenceValues',[]) or []; ceilings=data.get('ceilingRules',[]) or []; site_links=[]; top_notes=''
    elif 'inferenceElements' in data:
        schema_type='modern_inference'; mid=data.get('id'); name=data.get('name'); manufacturer=''; mtype=''; release_date=''; is_active=''; machine_notes=''; dmm_url=''; ichigeki_url=''; counters=data.get('counters',[]) or []; elements=data.get('inferenceElements',[]) or []; ceilings=data.get('ceilingRules',[]) or data.get('ceiling',{}).get('items',[]) or []; site_links=data.get('siteLinks',[]) or []; top_notes=data.get('notes','')
        for s in site_links:
            n=(s.get('name') or s.get('type') or '').lower(); u=s.get('url') or ''
            if 'dmm' in n and not dmm_url: dmm_url=u
            if ('一撃' in (s.get('name') or '') or 'ichigeki' in n or '1geki' in u) and not ichigeki_url: ichigeki_url=u
    elif 'settingReferenceValues' in data:
        schema_type='simple_reference'; mid=data.get('id'); name=data.get('name'); manufacturer=''; mtype=''; release_date=''; is_active=''; machine_notes=''; dmm_url=''; ichigeki_url=''; counters=data.get('counters',[]) or []; elements=data.get('settingReferenceValues',[]) or []; ceilings=data.get('ceilingRules',[]) or []; site_links=data.get('siteLinks',[]) or []; top_notes=data.get('notes','')
    else:
        schema_type='placeholder_or_other'; mid=data.get('id'); name=data.get('name'); manufacturer=''; mtype=''; release_date=''; is_active=''; machine_notes=''; dmm_url=''; ichigeki_url=''; counters=[]; elements=[]; ceilings=[]; site_links=[]; top_notes=data.get('notes','')

    manifest_entry = manifest_by_file.get(fn) or manifest_by_id.get(mid) or {}
    master_entry = master_by_file.get(fn) or master_by_id.get(mid) or {}
    details_exists = fn in detail_files

    counter_keys=[]; counter_labels=[]; default_visible=[]
    for c in counters:
        key=c.get('key'); label=c.get('displayName') or c.get('label')
        if key: counter_keys.append(key)
        if label: counter_labels.append(label)
        if c.get('isDefaultVisible') is True: default_visible.append(key or label)
        counter_rows.append({'file_name':fn,'machine_id':mid,'machine_name':name,'schema_type':schema_type,'counter_key':key,'counter_label':label,'category':c.get('category'),'sort_order':c.get('sortOrder'),'unit':c.get('unit'),'input_type':c.get('inputType'),'is_enabled':c.get('isEnabled'),'is_default_visible':c.get('isDefaultVisible'),'supports_minus':c.get('supportsMinus'),'notes':c.get('notes')})

    element_keys=[]; all_settings=set(); weights=[]; mins=[]; notes_count=0; ceiling_types=[]; ceiling_keys=[]; ceiling_limits=[]
    for idx, el in enumerate(elements, start=1):
        key=el.get('counterKey') or el.get('key'); label=el.get('label') or el.get('displayName'); vals=el.get('values'); settings=[]; weights_here=[]; mins_here=[]; notes_here=[]; value_count=0; values_preview=''
        if isinstance(vals, list):
            if vals and isinstance(vals[0], dict):
                settings=[v.get('settingNo') for v in vals if isinstance(v, dict)]
                weights_here=[v.get('weight') for v in vals if isinstance(v, dict) and v.get('weight') is not None]
                mins_here=[v.get('minSampleSize') for v in vals if isinstance(v, dict) and v.get('minSampleSize') is not None]
                notes_here=[v.get('note') for v in vals if isinstance(v, dict) and v.get('note')]
                denoms=[v.get('denominatorValue') for v in vals if isinstance(v, dict)]
                values_preview=safe_join(denoms[:6]); value_count=len(vals)
            else:
                settings=list(range(1, len(vals)+1)); weights_here=[el.get('weight')] if el.get('weight') is not None else []; mins_here=[el.get('minSampleSize')] if el.get('minSampleSize') is not None else []; notes_here=[el.get('notes') or el.get('note')] if (el.get('notes') or el.get('note')) else []; values_preview=safe_join(vals[:6]); value_count=len(vals)
        elif isinstance(vals, dict):
            settings=sorted(vals.keys(), key=lambda x: str(x)); weights_here=[el.get('weight')] if el.get('weight') is not None else []; mins_here=[el.get('minSampleSize')] if el.get('minSampleSize') is not None else []; notes_here=[el.get('notes') or el.get('note')] if (el.get('notes') or el.get('note')) else []; values_preview=safe_join([vals.get(k) for k in settings[:6]]); value_count=len(vals)
        all_settings.update(str(s) for s in settings if s not in (None,'')); weights.extend([w for w in weights_here if w is not None]); mins.extend([m for m in mins_here if m is not None]);
        if notes_here: notes_count += 1
        if key: element_keys.append(key)
        element_rows.append({'file_name':fn,'machine_id':mid,'machine_name':name,'schema_type':schema_type,'element_index':idx,'element_key':key,'element_label':label,'settings_present':safe_join(settings),'settings_count':len(settings),'values_count':value_count,'weight_values':safe_join(weights_here),'min_sample_sizes':safe_join(mins_here),'has_note':bool(notes_here),'values_preview':values_preview})

    for idx, ce in enumerate(ceilings, start=1):
        ctype=ce.get('ceilingType') or ce.get('type') or ce.get('category'); ckey=ce.get('ruleKey') or ce.get('key') or ce.get('name'); climit=ce.get('limitValue') or ce.get('value') or ce.get('games')
        if ctype: ceiling_types.append(ctype)
        if ckey: ceiling_keys.append(ckey)
        if climit not in (None,''): ceiling_limits.append(climit)
        ceiling_rows.append({'file_name':fn,'machine_id':mid,'machine_name':name,'schema_type':schema_type,'ceiling_index':idx,'rule_key':ckey,'display_name':ce.get('displayName') or ce.get('label') or ce.get('name'),'ceiling_type':ctype,'limit_value':climit,'unit':ce.get('unit'),'reset_on_hit':ce.get('resetOnHit'),'requires_reset_flag':ce.get('requiresResetFlag'),'is_enabled':ce.get('isEnabled'),'description':ce.get('description') or ce.get('notes')})

    site_link_names=[s.get('name') or s.get('type') for s in site_links]
    site_link_urls=[s.get('url') for s in site_links]
    rows.append({'file_name':fn,'machine_id':mid,'machine_name':name,'schema_type':schema_type,'top_level_keys':safe_join(sorted(list(data.keys()))),'manifest_registered': fn in manifest_by_file or mid in manifest_by_id,'manifest_machine_id': manifest_entry.get('machineId',''),'manifest_file_name': manifest_entry.get('fileName',''),'master_registered': fn in master_by_file or mid in master_by_id,'master_machine_id': master_entry.get('machineId',''),'master_machine_name': master_entry.get('machineName',''),'details_exists': details_exists,'details_file_name': fn if details_exists else '','file_name_matches_id': (fn[:-5] == mid) if mid else False,'legacy_id_kind': 'list_slot' if str(mid).startswith('list_slot_') else ('dmm_slot' if str(mid).startswith('dmm_slot_') else ''),'duplicate_name_count': len(name_to_files.get(name, [])),'duplicate_name_files': safe_join(name_to_files.get(name, [])),'duplicate_id_count': len(id_to_files.get(mid, [])),'duplicate_id_files': safe_join(id_to_files.get(mid, [])),'manufacturer': manufacturer,'machine_type': mtype,'release_date': release_date,'is_active': is_active,'machine_notes_present': bool(machine_notes),'machine_notes': machine_notes,'top_notes_present': bool(top_notes),'top_notes': top_notes,'dmm_url': dmm_url,'ichigeki_url': ichigeki_url,'site_link_count': len(site_links),'site_link_names': safe_join(site_link_names),'site_link_urls': safe_join(site_link_urls),'counter_count': len(counters),'counter_keys': safe_join(counter_keys),'counter_labels': safe_join(counter_labels),'default_visible_counter_keys': safe_join(default_visible),'setting_element_count': len(elements),'setting_element_keys': safe_join(element_keys),'settings_present_union': safe_join(sorted(all_settings, key=lambda x:(len(x),x))),'weights_unique': safe_join(sorted(set(weights))),'min_sample_sizes_unique': safe_join(sorted(set(mins))),'element_notes_present_count': notes_count,'ceiling_count': len(ceilings),'ceiling_keys': safe_join(ceiling_keys),'ceiling_types': safe_join(ceiling_types),'ceiling_limit_values': safe_join(ceiling_limits),'has_ceiling': len(ceilings) > 0})

for name, files in sorted(name_to_files.items()):
    if name and len(files) > 1:
        ids=[]
        for fn in files:
            data=cache[fn]
            ids.append(data.get('id') or data.get('machine',{}).get('id'))
        dup_rows.append({'machine_name':name,'duplicate_count':len(files),'file_names':safe_join(files),'machine_ids':safe_join(ids)})


def write_csv(path, rows):
    if not rows:
        return
    with open(path, 'w', encoding='utf-8-sig', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        writer.writeheader(); writer.writerows(rows)

write_csv(os.path.join(reports_dir, 'machine_json_inventory_full.csv'), rows)
write_csv(os.path.join(reports_dir, 'machine_json_elements_full.csv'), element_rows)
write_csv(os.path.join(reports_dir, 'machine_json_counters_full.csv'), counter_rows)
write_csv(os.path.join(reports_dir, 'machine_json_ceiling_rules_full.csv'), ceiling_rows)
write_csv(os.path.join(reports_dir, 'machine_json_duplicate_names.csv'), dup_rows)
print('done')
