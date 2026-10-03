import os
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

msg_re = re.compile(r'displayClientMessage\(\s*Component\.literal\(\s*"([^"]+)"\s*\)')

print("--- AUDITING DISPLAY CLIENT MESSAGES ---")
for root, dirs, files in os.walk(os.path.join('src', 'main', 'java')):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8', errors='ignore') as fp:
                for idx, line in enumerate(fp, 1):
                    for m in msg_re.finditer(line):
                        text = m.group(1)
                        clean = re.sub(r'§[0-9a-fk-or]', '', text).strip()
                        if len(clean) > 4 and any(c.isalpha() for c in clean):
                            print(f"{f}:{idx} -> \"{clean}\"")
