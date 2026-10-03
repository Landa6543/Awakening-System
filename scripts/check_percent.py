import json
import re
import os
import sys

sys.stdout.reconfigure(encoding='utf-8')

lang_dir = os.path.join('src', 'main', 'resources', 'assets', 'awakening_system', 'lang')
for fname in sorted(os.listdir(lang_dir)):
    if not fname.endswith('.json'): continue
    path = os.path.join(lang_dir, fname)
    with open(path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    bad_percent = []
    for k, v in data.items():
        if not isinstance(v, str): continue
        for m in re.finditer(r'%', v):
            idx = m.start()
            sub = v[idx:idx+6]
            # valid: %%, %s, %d, %f, %1$s, %2$d, etc.
            if not re.match(r'%(%|[0-9]+(\$[0-9]*)?[sdf]|[sdf])', sub):
                bad_percent.append((k, v, sub))

    print(f"[{fname}] Invalid % found: {len(bad_percent)}")
    if bad_percent:
        for k, v, sub in bad_percent[:10]:
            print(f"   {k}: {v} -> near '{sub}'")
        if len(bad_percent) > 10:
            print(f"   ... and {len(bad_percent)-10} more")
