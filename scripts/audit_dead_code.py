import os
import re
import json
import sys

sys.stdout.reconfigure(encoding='utf-8')

PROJECT_ROOT = os.path.abspath('.')
JAVA_ROOT = os.path.join(PROJECT_ROOT, 'src', 'main', 'java')
RES_ROOT = os.path.join(PROJECT_ROOT, 'src', 'main', 'resources')

def run_fast_audit():
    print("=== 1. JUNK ARTIFACTS IN PROJECT ROOT ===", flush=True)
    root_files = os.listdir(PROJECT_ROOT)
    junk_classes = [f for f in root_files if f.endswith('.class')]
    print(f"Loose compiled .class files in root: {junk_classes}")

    print("\n=== 2. READING ALL PROJECT TOKENS ===", flush=True)
    all_java_files = {} # cname -> (path, text, token_set)
    all_other_tokens = set()

    for root, dirs, files in os.walk(JAVA_ROOT):
        for f in files:
            if f.endswith('.java'):
                cname = f[:-5]
                path = os.path.join(root, f)
                with open(path, 'r', encoding='utf-8', errors='ignore') as fp:
                    text = fp.read()
                    tokens = set(re.findall(r'[A-Za-z0-9_]+', text))
                    all_java_files[cname] = (path, text, tokens)

    for root, dirs, files in os.walk(RES_ROOT):
        for f in files:
            if f.endswith('.json') or f.endswith('.toml'):
                path = os.path.join(root, f)
                try:
                    with open(path, 'r', encoding='utf-8', errors='ignore') as fp:
                        all_other_tokens.update(re.findall(r'[A-Za-z0-9_]+', fp.read()))
                except:
                    pass

    print(f"Total Java source files: {len(all_java_files)}", flush=True)

    print("\n=== 3. ORPHANED / UNREFERENCED CLASSES ===", flush=True)
    orphaned_classes = []
    for cname, (path, text, tokens) in all_java_files.items():
        if cname in ['AwakeningSystemMod', 'AwakeningSystemClient']:
            continue
        # Check if cname is in other files' tokens
        found = False
        if cname in all_other_tokens:
            found = True
        else:
            for other_cname, (opath, otext, otokens) in all_java_files.items():
                if other_cname != cname and cname in otokens:
                    found = True
                    break
        if not found:
            orphaned_classes.append((cname, os.path.relpath(path, PROJECT_ROOT)))

    print(f"Potential orphaned classes ({len(orphaned_classes)}):", flush=True)
    for cname, rpath in orphaned_classes:
        print(f"  - {cname} -> {rpath}")

    print("\n=== 4. UNUSED PRIVATE METHODS ===", flush=True)
    unused_privates = []
    method_decl_re = re.compile(r'private\s+(?:static\s+)?(?:final\s+)?[\w\<\>\[\],\s]+\s+([a-zA-Z0-9_]+)\s*\(')
    for cname, (path, text, tokens) in all_java_files.items():
        for m in method_decl_re.finditer(text):
            mname = m.group(1)
            if mname in [cname, 'equals', 'hashCode', 'toString', 'init', 'read', 'write', 'lambda']:
                continue
            # count occurrences of mname in text
            count = len(re.findall(r'\b' + re.escape(mname) + r'\b', text))
            if count <= 1:
                unused_privates.append((cname, mname, os.path.relpath(path, PROJECT_ROOT)))

    print(f"Potential unused private methods ({len(unused_privates)}):", flush=True)
    for cname, mname, rpath in unused_privates:
        print(f"  - {cname}.{mname}() in {rpath}")

    print("\n=== 5. SOUND ASSETS AUDIT ===", flush=True)
    sounds_json_path = os.path.join(RES_ROOT, 'assets', 'awakening_system', 'sounds.json')
    sounds_dir = os.path.join(RES_ROOT, 'assets', 'awakening_system', 'sounds')

    if os.path.exists(sounds_json_path):
        with open(sounds_json_path, 'r', encoding='utf-8') as fp:
            sounds_data = json.load(fp)

        declared_files = set()
        for s_event, s_info in sounds_data.items():
            for s_entry in s_info.get('sounds', []):
                s_name = s_entry if isinstance(s_entry, str) else s_entry.get('name', '')
                if ':' in s_name:
                    s_name = s_name.split(':')[1]
                declared_files.add(s_name + '.ogg')

        actual_sound_files = set(f for f in os.listdir(sounds_dir) if f.endswith('.ogg'))
        unreferenced_sounds = actual_sound_files - declared_files
        missing_sounds = declared_files - actual_sound_files

        print(f"Dead sound files on disk (not in sounds.json): {len(unreferenced_sounds)}")
        for u in sorted(unreferenced_sounds):
            print(f"  - [DEAD SOUND] {u}")
        print(f"Missing sound files declared in sounds.json: {len(missing_sounds)}")
        for m in sorted(missing_sounds):
            print(f"  - [MISSING SOUND] {m}")

    print("\n=== 6. ENTITY ATTRIBUTES REGISTRATION AUDIT ===", flush=True)
    mod_entities_path = os.path.join(JAVA_ROOT, 'com', 'soybe', 'awakeningsystem', 'entity', 'ModEntities.java')
    with open(mod_entities_path, 'r', encoding='utf-8') as fp:
        mod_entities_code = fp.read()

    reg_entities = set(re.findall(r'ENTITY_TYPES\.register\(\s*"([^"]+)"', mod_entities_code))
    attr_registered = set(re.findall(r'event\.put\(\s*([A-Za-z0-9_]+)\.get\(\)', mod_entities_code))
    # match field names like IGRIS_BOSS to entity name igris_boss
    field_to_name = {}
    for m in re.finditer(r'DeferredHolder<EntityType<\?>, EntityType<([^>]+)>>\s+([A-Z0-9_]+)\s*=\s*ENTITY_TYPES\.register\(\s*"([^"]+)"', mod_entities_code):
        field_to_name[m.group(2)] = (m.group(1), m.group(3))

    for field, (etype, ename) in field_to_name.items():
        # If it's a living entity (Mob, Monster, PathfinderMob, Animal, Boss)
        is_projectile_or_vehicle = any(w in etype for w in ['Arrow', 'Projectile', 'Seat', 'Coin', 'Totem'])
        if not is_projectile_or_vehicle:
            if field not in attr_registered:
                print(f"  - [POTENTIAL CRASH BUG] Living entity '{ename}' ({field}) has NO attributes registered in registerAttributes!")

    print("\n=== AUDIT COMPLETE ===", flush=True)

if __name__ == '__main__':
    run_fast_audit()
