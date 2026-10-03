import urllib.request, urllib.parse, re

headers = {"User-Agent": "Mozilla/5.0"}
def search(q):
    url = f"https://www.myinstants.com/en/search/?name={urllib.parse.quote(q)}"
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=10) as r:
            html = r.read().decode('utf-8', errors='ignore')
            mp3s = list(set(re.findall(r'/media/sounds/[^"\'\s]+\.mp3', html)))
            return mp3s
    except Exception as e:
        print(f"Error {q}: {e}")
        return []

for q in ['sukuna', 'itadori', 'megumi', 'excalibur', 'gilgamesh', 'archer fate', 'rengoku', 'tanjiro', 'nezuko', 'arise', 'beru']:
    res = search(q)
    print(f"\n{q}: {len(res)} matches")
    for p in res[:4]:
        print(f"   https://www.myinstants.com{p}")
