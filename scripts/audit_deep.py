import os
import re
import json
import sys

sys.stdout.reconfigure(encoding='utf-8')

def run_audit():
    lang_dir = os.path.join('src', 'main', 'resources', 'assets', 'awakening_system', 'lang')
    langs = {}
    for f in os.listdir(lang_dir):
        if f.endswith('.json'):
            path = os.path.join(lang_dir, f)
            with open(path, 'r', encoding='utf-8') as fp:
                langs[f] = json.load(fp)

    en_keys = langs.get('en_us.json', {})
    es_keys = langs.get('es_es.json', {})

    report = []
    report.append("=== 1. LANGUAGE FILE PARITY CHECK ===")
    all_keys = set()
    for k_map in langs.values():
        all_keys.update(k_map.keys())

    for name, k_map in sorted(langs.items()):
        missing = sorted(list(all_keys - set(k_map.keys())))
        if missing:
            report.append(f"[{name}] Missing {len(missing)} keys:")
            for m in missing:
                report.append(f"    - {m}")
        else:
            report.append(f"[{name}] OK (all {len(all_keys)} keys present)")

    report.append("\n=== 2. CODE vs LANG CHECK ===")
    # Look for literal translatable keys with exact string or concatenation
    exact_translatable_re = re.compile(r'Component\.translatable\(\s*"([a-zA-Z0-9_\.\-]+)"\s*[,|\)]')
    prefix_translatable_re = re.compile(r'Component\.translatable\(\s*"([a-zA-Z0-9_\.\-]+)"\s*\+')

    exact_missing = set()
    used_exact = 0
    for root, dirs, files in os.walk(os.path.join('src', 'main', 'java')):
        for file in files:
            if file.endswith('.java'):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8', errors='ignore') as fp:
                    content = fp.read()
                    for m in exact_translatable_re.findall(content):
                        used_exact += 1
                        # Filter standard vanilla keys
                        if m.startswith('item.minecraft.') or m.startswith('block.minecraft.') or m.startswith('key.') or m.startswith('gui.') or m.startswith('options.'):
                            continue
                        if m not in en_keys:
                            exact_missing.add((m, file))

    report.append(f"Exact translatable keys checked: {used_exact}")
    report.append(f"Missing exact keys in en_us.json: {len(exact_missing)}")
    for m, file in sorted(exact_missing):
        report.append(f"    - [MISSING] {m} (used in {file})")

    report.append("\n=== 3. DYNAMIC TRANSLATION PREFIXES CHECK ===")
    # Check classes, ranks, skills, stats, tabs
    prefix_found = set()
    for root, dirs, files in os.walk(os.path.join('src', 'main', 'java')):
        for file in files:
            if file.endswith('.java'):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8', errors='ignore') as fp:
                    content = fp.read()
                    for m in prefix_translatable_re.findall(content):
                        prefix_found.add(m)

    for pref in sorted(prefix_found):
        # find matching keys in en_keys
        matches = [k for k in en_keys if k.startswith(pref)]
        report.append(f"Prefix '{pref}': {len(matches)} keys defined in en_us.json")
        if len(matches) == 0:
            report.append(f"    WARNING: Zero keys defined starting with prefix '{pref}'!")

    report.append("\n=== 4. HARDCODED USER-FACING STRINGS (SCREENS, COMMANDS, MESSAGES) ===")
    literal_re = re.compile(r'Component\.literal\(\s*"([^"]+)"\s*\)')
    hardcoded_user_facing = []

    # Check screens, overlays, commands
    target_dirs = [
        os.path.join('src', 'main', 'java', 'com', 'soybe', 'awakeningsystem')
    ]
    for root, dirs, files in os.walk(target_dirs[0]):
        # skip entities / models / gen scripts unless user facing
        for file in files:
            if not file.endswith('.java'): continue
            # focus on screens, overlays, commands, event messages
            is_ui = any(word in file for word in ['Screen', 'Overlay', 'Command', 'Notification', 'Guidebook', 'Handler'])
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8', errors='ignore') as fp:
                lines = fp.readlines()
                for line_no, line in enumerate(lines, 1):
                    # check for literal components
                    for match in literal_re.findall(line):
                        clean = re.sub(r'§[0-9a-fk-or]', '', match).strip()
                        # ignore symbols, numbers, punctuation, short technical codes
                        if len(clean) > 4 and any(c.isalpha() for c in clean):
                            if ' ' in clean or re.search(r'[A-Z][a-z]', clean):
                                # check if it looks like human text
                                if not clean.startswith('http') and not clean.startswith('awakening_system:') and not clean in ['SYSTEM', 'NeoForge', 'DEBUG']:
                                    hardcoded_user_facing.append((file, line_no, clean))

    report.append(f"Found {len(hardcoded_user_facing)} potential hardcoded text strings in UI/Commands/Handlers:")
    for file, line_no, text in hardcoded_user_facing:
        report.append(f"    [{file}:{line_no}] \"{text}\"")

    out_report_path = os.path.join('scripts', 'audit_report.txt')
    with open(out_report_path, 'w', encoding='utf-8') as fp:
        fp.write('\n'.join(report))

    print(f"Report written to {out_report_path} with {len(report)} lines.")

if __name__ == '__main__':
    run_audit()
