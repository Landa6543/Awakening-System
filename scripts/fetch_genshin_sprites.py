import urllib.request, re, os
from PIL import Image

ITEM_DIR = 'src/main/resources/assets/awakening_system/textures/item'
os.makedirs(ITEM_DIR, exist_ok=True)
os.makedirs('scratch', exist_ok=True)

items = [
    ('slime_condensate', 'Slime_Condensate'),
    ('slime_secretions', 'Slime_Secretions'),
    ('slime_concentrate', 'Slime_Concentrate'),
    ('condensed_resin', 'Condensed_Resin'),
    ('damaged_mask', 'Damaged_Mask'),
    ('stained_mask', 'Stained_Mask'),
    ('ominous_mask', 'Ominous_Mask'),
    ('heavy_horn', 'Heavy_Horn'),
]

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Referer': 'https://genshin-impact.fandom.com/'
}

for item_name, page_name in items:
    wiki_url = f'https://genshin-impact.fandom.com/wiki/{page_name}'
    req = urllib.request.Request(wiki_url, headers=headers)
    try:
        with urllib.request.urlopen(req) as resp:
            html = resp.read().decode('utf-8', errors='ignore')
        # Find Item_*.png in json-ld or og:image
        m = re.search(r'\"(https://static\.wikia\.nocookie\.net/gensin-impact/images/[^\"]+Item_[^\"]+)\"', html)
        if not m:
            m = re.search(r'<meta property=\"og:image\" content=\"([^\"]+)\"', html)
        if m:
            img_url = m.group(1).replace(r'\u0026', '&')
            print(f'Fetching {item_name} from: {img_url}')
            img_req = urllib.request.Request(img_url, headers=headers)
            with urllib.request.urlopen(img_req) as img_resp:
                raw_data = img_resp.read()
            raw_path = f'scratch/{item_name}_raw.png'
            with open(raw_path, 'wb') as f:
                f.write(raw_data)
            
            # Now process with PIL: keep high resolution (e.g. 64x64 or 32x32 crisp with alpha)
            im = Image.open(raw_path).convert('RGBA')
            # Resize cleanly to 32x32 and 64x64
            target_path = f'{ITEM_DIR}/{item_name}.png'
            # 64x64 or 32x32? Let's check size: if we make it 32x32 or 48x48:
            im_resized = im.resize((32, 32), Image.Resampling.LANCZOS)
            im_resized.save(target_path)
            print(f'-> Successfully generated canonical {item_name}.png (32x32) from official Genshin sprite!')
        else:
            print(f'No image URL found in html for {page_name}')
    except Exception as e:
        print(f'Error fetching {page_name}: {e}')

print('Genshin drop sprite download & processing complete!')
