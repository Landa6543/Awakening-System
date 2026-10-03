import os, re, sys

# Set stdout to utf-8
sys.stdout.reconfigure(encoding='utf-8')

java_dir = 'src/main/java'
pattern = re.compile(r'notify\w*\((.*?)\);', re.DOTALL)

print("--- NOTIFY CALLS WITH LITERALS OR STRINGS ---")
for root, _, files in os.walk(java_dir):
    for f in files:
        if f.endswith('.java'):
            p = os.path.join(root, f)
            with open(p, 'r', encoding='utf-8', errors='ignore') as jf:
                content = jf.read()
            for m in pattern.finditer(content):
                call = m.group(0).replace('\n', ' ')
                if 'literal:' in call or '"§' in call:
                    print(f'{f}: {call[:160]}')
