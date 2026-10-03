import glob
import re
import os

java_files = glob.glob('src/main/java/**/*.java', recursive=True)
res_pngs = set()
for root, dirs, files in os.walk('src/main/resources'):
    for f in files:
        if f.endswith('.png'):
            rel = os.path.relpath(os.path.join(root, f), 'src/main/resources/assets/awakening_system').replace('\\', '/')
            res_pngs.add(rel)

print(f"Total resource PNGs: {len(res_pngs)}")

# Match ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "path")
tex_pattern = re.compile(r'ResourceLocation\.(?:fromNamespaceAndPath|tryBuild|create)\(\s*(?:AwakeningSystemMod\.MOD_ID|"awakening_system")\s*,\s*"([^"]+)"\)')

missing = []
found = 0
for jf in java_files:
    with open(jf, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
    for m in tex_pattern.finditer(content):
        path = m.group(1)
        if path.startswith('textures/'):
            p = path
            if not p.endswith('.png'):
                p += '.png'
            if p not in res_pngs:
                missing.append((os.path.basename(jf), path))
            else:
                found += 1

print(f"Found valid texture refs: {found}")
print(f"Missing texture refs: {len(missing)}")
for jf, path in missing:
    print(f"  MISSING in {jf}: {path}")
