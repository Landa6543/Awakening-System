import os
from PIL import Image

tex_dir = r'src/main/resources/assets/awakening_system/textures'
all_pngs = []
for root, dirs, files in os.walk(tex_dir):
    for f in files:
        if f.endswith('.png'):
            all_pngs.append(os.path.join(root, f))

print(f"Total pngs: {len(all_pngs)}")

# Look for files with 'card', 'skill', 'ability', 'constellation', 'badge', 'emblem'
for p in all_pngs:
    fn = os.path.basename(p).lower()
    rel = os.path.relpath(p, tex_dir)
    if any(k in fn for k in ['card', 'skill', 'ability', 'passive', 'active', 'crest', 'badge', 'seal', 'glyph', 'icon']):
        print(rel)
