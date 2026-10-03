import urllib.request
import re

for term in ["megumi", "fushiguro", "emiya", "archer", "fate", "firefly", "sam", "arlecchino", "harlequin"]:
    url = f"https://skinsmc.org/search/{term}"
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'})
    try:
        with urllib.request.urlopen(req) as resp:
            html = resp.read().decode('utf-8')
            matches = re.findall(r'/skin/(\d+)', html)
            print(f"Term '{term}': {len(matches)} skins found")
    except Exception as e:
        print(f"Term '{term}': {e}")
