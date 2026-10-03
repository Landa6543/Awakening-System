import os, re, sys

sys.stdout.reconfigure(encoding='utf-8')

entity_dir = 'src/main/java/com/soybe/awakeningsystem/entity'

patterns = [
    re.compile(r'notify\w*\((.*?)\);', re.DOTALL),
    re.compile(r'displayClientMessage\((.*?)\);', re.DOTALL),
    re.compile(r'sendSystemMessage\((.*?)\);', re.DOTALL)
]

for root, _, files in os.walk(entity_dir):
    for f in files:
        if f.endswith('.java'):
            p = os.path.join(root, f)
            with open(p, 'r', encoding='utf-8', errors='ignore') as jf:
                content = jf.read()
            for pat in patterns:
                for m in pat.finditer(content):
                    call = m.group(0).replace('\n', ' ')
                    print(f'{f}: {call}')
