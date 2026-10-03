import urllib.request
import re
import os
import base64
from PIL import Image

def get_html(url):
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'})
    with urllib.request.urlopen(req) as resp:
        return resp.read().decode('utf-8')

def download_skin_for_query(query, out_name):
    url = f"https://skinsmc.org/search/{query}"
    print(f"Fetching skins for query '{query}' -> {url}")
    html = get_html(url)
    skin_ids = re.findall(r'/skin/(\d+)', html)
    if not skin_ids:
        print(f"No skins found for '{query}'")
        return False
    print(f"Found {len(skin_ids)} skins for {query}. Trying first: {skin_ids[0]}")
    skin_page = f"https://skinsmc.org/skin/{skin_ids[0]}"
    page_html = get_html(skin_page)
    
    encoded_urls = re.findall(r'/headrender/([a-zA-Z0-9+/=]+)/', page_html)
    if not encoded_urls:
        print("No headrender encoded URL found!")
        return False
    
    skin_s3_url = base64.b64decode(encoded_urls[0]).decode('utf-8')
    print(f"Decoded S3 URL: {skin_s3_url}")
    
    dest_path = os.path.join("src/main/resources/assets/awakening_system/textures/entity/companion", f"{out_name}.png")
    req = urllib.request.Request(skin_s3_url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as resp:
        data = resp.read()
    with open(dest_path, 'wb') as f:
        f.write(data)
    
    im = Image.open(dest_path)
    print(f"SUCCESS: Saved {out_name}.png ({im.size}, {im.mode})")
    return True

queries = [
    ("firefly", "firefly"),
    ("megumi", "megumi"),
    ("emiya", "archer")
]

for q, out in queries:
    download_skin_for_query(q, out)

