import os
import re

java_files = []
for root, dirs, files in os.walk('src/main/java'):
    for f in files:
        if f.endswith('.java'):
            java_files.append(os.path.join(root, f))

out_lines = []
out_lines.append(f"Total java files: {len(java_files)}\n")

notify_matches = []

for fpath in java_files:
    with open(fpath, 'r', encoding='utf-8', errors='ignore') as f:
        lines = f.readlines()
    
    for i, line in enumerate(lines):
        if 'AwakeningSystemMod.notify' in line:
            full_stmt = line
            idx = i
            while ';' not in full_stmt and idx + 1 < len(lines):
                idx += 1
                full_stmt += " " + lines[idx]
            
            quotes = re.findall(r'"([^"\\]*(?:\\.[^"\\]*)*)"', full_stmt)
            for q in quotes:
                if not q.startswith("awakening_system.") and not q.startswith("literal:") and not q.startswith("%") and len(q.strip()) > 1:
                    if q not in [",", ":::", " ", "", "\n"]:
                        notify_matches.append((fpath, i + 1, full_stmt.strip(), q))

out_lines.append(f"Found {len(notify_matches)} hardcoded notification strings:\n")
seen = set()
for fpath, lnum, stmt, q in notify_matches:
    key = (fpath, lnum, q)
    if key not in seen:
        seen.add(key)
        out_lines.append(f"{fpath}:{lnum} -> quote: '{q}'\n")

with open('scripts/notify_audit.txt', 'w', encoding='utf-8') as out:
    out.writelines(out_lines)

print("Wrote scripts/notify_audit.txt successfully!")
