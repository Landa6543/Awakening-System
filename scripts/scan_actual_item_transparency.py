import os
import json
from PIL import Image

models_dir = r'src/main/resources/assets/awakening_system/models/item'
tex_dir = r'src/main/resources/assets/awakening_system/textures'
item_tex_dir = r'src/main/resources/assets/awakening_system/textures/item'

referenced_textures = set()

for f in os.listdir(models_dir):
    if f.endswith('.json'):
        with open(os.path.join(models_dir, f), 'r', encoding='utf-8') as fp:
            try:
                data = json.load(fp)
                textures = data.get('textures', {})
                for k, v in textures.items():
                    if isinstance(v, str) and v.startswith('awakening_system:'):
                        rel_path = v[len('awakening_system:'):]
                        referenced_textures.add(rel_path)
            except Exception as e:
                pass

print(f"Total referenced textures in item models: {len(referenced_textures)}")

translucent_item_models = []

for rel in sorted(referenced_textures):
    # Check if file exists
    full_path = os.path.join(tex_dir, rel.replace('/', os.sep) + '.png')
    if os.path.exists(full_path):
        im = Image.open(full_path).convert('RGBA')
        trans_count = sum(1 for p in im.getdata() if 0 < p[3] < 255)
        if trans_count > 0:
            translucent_item_models.append((rel, full_path, trans_count, im.size))
    else:
        # maybe .png was included in path
        full_path2 = os.path.join(tex_dir, rel.replace('/', os.sep))
        if os.path.exists(full_path2):
            im = Image.open(full_path2).convert('RGBA')
            trans_count = sum(1 for p in im.getdata() if 0 < p[3] < 255)
            if trans_count > 0:
                translucent_item_models.append((rel, full_path2, trans_count, im.size))

print(f"\nItem models using textures with semi-transparent pixels: {len(translucent_item_models)}")
for rel, fp, count, sz in translucent_item_models:
    print(f"{rel:45} | count={count:5} | size={sz}")
