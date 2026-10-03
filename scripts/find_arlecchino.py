import urllib.request
import re
import base64
import os
from PIL import Image

def find_arlecchino():
    for query in ['fatui', 'harbinger']:
        url = f"https://skinsmc.org/search/{query}"
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as resp:
            html = resp.read().decode('utf-8')
        
        skin_ids = re.findall(r'/skin/(\d+)', html)
        print(f"Checking {len(skin_ids)} skins in query '{query}'...")
        for sid in skin_ids:
            page_url = f"https://skinsmc.org/skin/{sid}"
            req2 = urllib.request.Request(page_url, headers={'User-Agent': 'Mozilla/5.0'})
            with urllib.request.urlopen(req2) as resp2:
                p_html = resp2.read().decode('utf-8')
            
            title_m = re.search(r'<title>(.*?)</title>', p_html, re.IGNORECASE)
            title = title_m.group(1) if title_m else ""
            if "arlecchino" in title.lower() or "knave" in title.lower() or "peruere" in title.lower():
                print(f"FOUND MATCH in skin {sid}: {title}")
                encoded_urls = re.findall(r'/headrender/([a-zA-Z0-9+/=]+)/', p_html)
                if encoded_urls:
                    s3_url = base64.b64decode(encoded_urls[0]).decode('utf-8')
                    print("S3 URL:", s3_url)
                    req3 = urllib.request.Request(s3_url, headers={'User-Agent': 'Mozilla/5.0'})
                    with urllib.request.urlopen(req3) as resp3:
                        data = resp3.read()
                    out_path = "src/main/resources/assets/awakening_system/textures/entity/companion/arlecchino.png"
                    with open(out_path, "wb") as f:
                        f.write(data)
                    im = Image.open(out_path)
                    print("SUCCESS! Saved arlecchino.png:", im.size, im.mode)
                    return True
    return False

find_arlecchino()
