import os
import re

java_files = []
for root, dirs, files in os.walk('src/main/java'):
    for f in files:
        if f.endswith('.java'):
            java_files.append(os.path.join(root, f))

out_lines = []
out_lines.append(f"Total java files: {len(java_files)}\n")

actionbar_matches = []

for fpath in java_files:
    with open(fpath, 'r', encoding='utf-8', errors='ignore') as f:
        lines = f.readlines()
    
    for i, line in enumerate(lines):
        if 'displayClientMessage' in line:
            full_stmt = line
            idx = i
            while ';' not in full_stmt and idx + 1 < len(lines):
                idx += 1
                full_stmt += " " + lines[idx]
            
            if 'Component.literal(' in full_stmt:
                # check for non-empty text
                quotes = re.findall(r'Component\.literal\(\s*"([^"\\]*(?:\\.[^"\\]*)*)"\s*\)', full_stmt)
                for q in quotes:
                    # filter out pure styling or numbers or empty
                    if len(q.strip()) > 1 and not re.match(r'^[§0-9a-fk-or\s\[\]:\(\)\-+/]+$', q):
                        actionbar_matches.append((fpath, i + 1, q))

out_lines.append(f"Found {len(actionbar_matches)} hardcoded displayClientMessage strings:\n")
seen = set()
for fpath, lnum, q in actionbar_matches:
    key = (fpath, lnum, q)
    if key not in seen:
        seen.add(key)
        out_lines.append(f"{fpath}:{lnum} -> '{q}'\n")

with open('scripts/actionbar_audit.txt', 'w', encoding='utf-8') as out:
    out.writelines(out_lines)

print(f"Done! Found {len(seen)} unique hardcoded action bar messages.")
