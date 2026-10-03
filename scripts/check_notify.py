import os
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

notify_re = re.compile(r'AwakeningSystemMod\.notify(All)?\(\s*[^,]+,\s*"([^"]+)"\s*,\s*"([^"]+)"')

print("--- AUDITING NOTIFY CALLS ---")
for root, dirs, files in os.walk(os.path.join('src', 'main', 'java')):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8', errors='ignore') as fp:
                for idx, line in enumerate(fp, 1):
                    for m in notify_re.finditer(line):
                        title_arg = m.group(2)
                        msg_arg = m.group(3)
                        if not title_arg.startswith('awakening_system.') and not title_arg.startswith('literal:'):
                            print(f"[INVALID NOTIFY TITLE] {f}:{idx} -> '{title_arg}'")
                        if not msg_arg.startswith('awakening_system.') and not msg_arg.startswith('literal:'):
                            print(f"[INVALID NOTIFY MSG] {f}:{idx} -> '{msg_arg}'")
