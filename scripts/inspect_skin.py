import urllib.request
import re

url = 'https://skinsmc.org/skin/229038'
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'})
with urllib.request.urlopen(req) as resp:
    html = resp.read().decode('utf-8')

for m in re.finditer(r'href="([^"]+)"', html):
    link = m.group(1)
    if 'download' in link or '.png' in link:
        print('LINK:', link)

for m in re.finditer(r'src="([^"]+)"', html):
    link = m.group(1)
    if '.png' in link:
        print('SRC:', link)
