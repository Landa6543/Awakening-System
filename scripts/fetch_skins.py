import urllib.request
import re
import os
from PIL import Image

def get_html(url):
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'})
    with urllib.request.urlopen(req) as resp:
        return resp.read().decode('utf-8')

def download_file(url, out_path):
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'})
    with urllib.request.urlopen(req) as resp:
        data = resp.read()
    with open(out_path, 'wb') as f:
        f.write(data)

targets = {
    "xiao": "https://skinsmc.org/search/xiao+genshin",
    "arlecchino": "https://skinsmc.org/search/arlecchino+genshin",
    "firefly": "https://skinsmc.org/search/firefly+honkai",
    "megumi": "https://skinsmc.org/search/megumi+fushiguro",
    "archer": "https://skinsmc.org/search/archer+fate+stay+night"
}

dest_dir = "src/main/resources/assets/awakening_system/textures/entity/companion"

for name, search_url in targets.items():
    print(f"Searching skin for {name}...")
    try:
        html = get_html(search_url)
        skin_ids = re.findall(r'/skin/(\d+)', html)
        if not skin_ids:
            print(f"No skins found for {name}")
            continue
        first_id = skin_ids[0]
        skin_page = f"https://skinsmc.org/skin/{first_id}"
        page_html = get_html(skin_page)
        
        # Look for png or download link
        # SkinsMC direct skin images are usually https://skinsmc.org/assets/skins/... or /download/
        png_matches = [m for m in re.findall(r'href="([^"]*download[^"]*)"', page_html)]
        if not png_matches:
            png_matches = [m for m in re.findall(r'src="([^"]*skins[^"]*\.png)"', page_html)]
        
        target_path = os.path.join(dest_dir, f"{name}.png")
        if png_matches:
            dl_url = png_matches[0]
            if dl_url.startswith('/'):
                dl_url = "https://skinsmc.org" + dl_url
            print(f"Downloading {name} from {dl_url}...")
            download_file(dl_url, target_path)
            # Verify image
            im = Image.open(target_path)
            print(f"Saved {name}.png with size {im.size} mode {im.mode}")
        else:
            print(f"Could not find download URL for {name} in {skin_page}")
    except Exception as e:
        print(f"Error for {name}: {e}")
