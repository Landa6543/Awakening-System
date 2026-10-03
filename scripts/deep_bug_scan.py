import os
import re

def deep_scan():
    src_dir = os.path.join('src', 'main', 'java', 'com', 'soybe', 'awakeningsystem')
    
    findings = []

    for root, dirs, files in os.walk(src_dir):
        for f in files:
            if not f.endswith('.java'): continue
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8', errors='ignore') as fp:
                lines = fp.readlines()

            for idx, line in enumerate(lines, 1):
                # 1. Unchecked split index: .split(...)[1] or [0]
                if re.search(r'\.split\([^)]+\)\[[1-9]\]', line):
                    findings.append((f, idx, "UNCHECKED_SPLIT", line.strip()))

                # 2. list.get(0) without isEmpty check nearby
                if '.get(0)' in line and 'isEmpty()' not in line and 'size() > 0' not in line and '!empty' not in line.lower():
                    # check 3 lines before for isEmpty
                    prev_lines = "".join(lines[max(0, idx-4):idx])
                    if 'isEmpty' not in prev_lines and 'size()' not in prev_lines and 'size >' not in prev_lines:
                        findings.append((f, idx, "UNCHECKED_GET_0", line.strip()))

                # 3. Possible division by zero: / total, / max, / count, / length, / size
                div_match = re.search(r'/\s*([a-zA-Z0-9_]+(\(\))?)\s*[\);,]', line)
                if div_match:
                    divisor = div_match.group(1)
                    if divisor in ['total', 'max', 'count', 'size()', 'length', 'duration', 'maxExp', 'maxHealth', 'cost', 'maxCharge']:
                        findings.append((f, idx, "POSSIBLE_DIV_ZERO", f"Division by {divisor}: {line.strip()}"))

                # 4. Inconsistent NBT read/write: e.g. putString vs getInt
                # 5. Missing player null check in Screen or Overlay
                if ('Screen' in f or 'Overlay' in f) and 'minecraft.player.' in line:
                    prev_lines = "".join(lines[max(0, idx-10):idx])
                    if 'player == null' not in prev_lines and 'player != null' not in prev_lines:
                        findings.append((f, idx, "SCREEN_NPE_RISK", line.strip()))

    print(f"Deep scan found {len(findings)} potential bug hotspots:")
    for f, idx, cat, desc in findings:
        print(f"[{cat}] {f}:{idx} -> {desc}")

if __name__ == '__main__':
    deep_scan()
