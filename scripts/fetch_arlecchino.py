import urllib.request
import re
from PIL import Image

# Search on planetminecraft
url = "https://www.planetminecraft.com/skins/?keywords=arlecchino"
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'})
try:
    with urllib.request.urlopen(req) as resp:
        html = resp.read().decode('utf-8')
    skin_urls = re.findall(r'href="(/skin/[^"]+)"', html)
    print("Found PMC skin URLs:", len(skin_urls))
    if skin_urls:
        print("First skin:", skin_urls[0])
        # View skin page
        skin_page_url = "https://www.planetminecraft.com" + skin_urls[0]
        req2 = urllib.request.Request(skin_page_url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'})
        with urllib.request.urlopen(req2) as resp2:
            page_html = resp2.read().decode('utf-8')
        
        # Look for download link or skin texture url
        # PlanetMinecraft has /download/skin/... or static.planetminecraft.com
        dl_matches = re.findall(r'href="(/skin/[^"]+/download/)"', page_html)
        print("Download matches:", dl_matches)
        if dl_matches:
            dl_url = "https://www.planetminecraft.com" + dl_matches[0]
            req_dl = urllib.request.Request(dl_url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', 'Referer': skin_page_url})
            with urllib.request.urlopen(req_dl) as dl_resp:
                data = dl_resp.read()
            with open("src/main/resources/assets/awakening_system/textures/entity/companion/arlecchino.png", "wb") as f:
                f.write(data)
            im = Image.open("src/main/resources/assets/awakening_system/textures/entity/companion/arlecchino.png")
            print("SUCCESS! Arlecchino skin saved:", im.size, im.mode)
except Exception as e:
    print("Error:", e)
