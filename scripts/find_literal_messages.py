import os, re, sys

sys.stdout.reconfigure(encoding='utf-8')

java_dir = 'src/main/java'

# Search for any notify or displayClientMessage with hardcoded strings in entity/, world/, or managers
patterns = [
    re.compile(r'notify\w*\((.*?)\);', re.DOTALL),
    re.compile(r'displayClientMessage\((.*?)\);', re.DOTALL),
    re.compile(r'sendSystemMessage\((.*?)\);', re.DOTALL)
]

for root, _, files in os.walk(java_dir):
    for f in files:
        if f.endswith('.java'):
            p = os.path.join(root, f)
            with open(p, 'r', encoding='utf-8', errors='ignore') as jf:
                content = jf.read()
            for pat in patterns:
                for m in pat.finditer(content):
                    call = m.group(0).replace('\n', ' ')
                    if 'literal:' in call or 'Component.literal' in call:
                        # filter out purely punctuation or numbers
                        if any(c.isalpha() for c in call):
                            print(f'{f}: {call[:150]}')
