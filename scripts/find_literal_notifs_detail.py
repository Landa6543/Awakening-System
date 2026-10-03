import os, re, sys

sys.stdout.reconfigure(encoding='utf-8')

java_dir = 'src/main/java'

pattern = re.compile(r'notify\w*\((.*?)\);', re.DOTALL)

for root, _, files in os.walk(java_dir):
    for f in files:
        if f.endswith('.java'):
            p = os.path.join(root, f)
            with open(p, 'r', encoding='utf-8', errors='ignore') as jf:
                content = jf.read()
            for m in pattern.finditer(content):
                call = m.group(0).replace('\n', ' ')
                # If call contains literal: or quoted Spanish
                if 'literal:' in call or ('"' in call and not any(k in call for k in ['awakening_system.', 'minecraft:'])):
                    print(f'{f}: {call}')
