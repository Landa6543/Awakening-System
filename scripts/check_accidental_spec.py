import json
import re
import os

lang_dir = os.path.join('src', 'main', 'resources', 'assets', 'awakening_system', 'lang')
with open(os.path.join(lang_dir, 'en_us.json'), 'r', encoding='utf-8') as f:
    en = json.load(f)

# Find all keys in code that call Component.translatable(key) WITH args vs WITHOUT args
trans_args = {}
for root, dirs, files in os.walk(os.path.join('src', 'main', 'java')):
    for file in files:
        if file.endswith('.java'):
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8', errors='ignore') as fp:
                content = fp.read()
                # Find Component.translatable("key", ...)
                for m in re.finditer(r'Component\.translatable\(\s*"([^"]+)"\s*(,\s*([^)]+))?\)', content):
                    key = m.group(1)
                    has_args = bool(m.group(2))
                    trans_args[key] = has_args

print(f"Total keys mapped from code: {len(trans_args)}")
accidental_specifiers = []
for k, has_args in trans_args.items():
    if not has_args and k in en:
        val = en[k]
        # check if val contains %s or %d or %f or %1$s
        spec = re.findall(r'%[0-9]*\$?[sdf]', val)
        if spec:
            accidental_specifiers.append((k, val, spec))

print(f"Keys without args in code BUT containing format specifiers in en_us.json: {len(accidental_specifiers)}")
for k, val, spec in accidental_specifiers:
    print(f"   {k}: {val} -> specifiers: {spec}")
