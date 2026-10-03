import urllib.request, json, os, urllib.parse

sound_dir = r"src/main/resources/assets/awakening_system/sounds/companion"
os.makedirs(sound_dir, exist_ok=True)

targets = [
    {
        "id": "xiao",
        "wiki": "genshin-impact",
        "prefix": "VO_JA_Xiao",
        "summon_kw": "join_party",
        "skill_kw": "elemental_burst",
        "ambient_kw": "about",
        "hurt_kw": "damage"
    },
    {
        "id": "arlecchino",
        "wiki": "genshin-impact",
        "prefix": "VO_JA_Arlecchino",
        "summon_kw": "join_party",
        "skill_kw": "elemental_burst",
        "ambient_kw": "about",
        "hurt_kw": "damage"
    },
    {
        "id": "acheron",
        "wiki": "honkai-star-rail",
        "prefix": "VO_JA_Acheron",
        "summon_kw": "battle_begins",
        "skill_kw": "turn_atk",
        "ambient_kw": "basic_atk",
        "hurt_kw": "hit_by_light"
    },
    {
        "id": "kafka",
        "wiki": "honkai-star-rail",
        "prefix": "VO_JA_Kafka",
        "summon_kw": "battle_begins",
        "skill_kw": "turn_atk",
        "ambient_kw": "basic_atk",
        "hurt_kw": "hit_by_light"
    },
    {
        "id": "firefly",
        "wiki": "honkai-star-rail",
        "prefix": "VO_JA_Firefly",
        "summon_kw": "battle_begins",
        "skill_kw": "turn_atk",
        "ambient_kw": "basic_atk",
        "hurt_kw": "hit_by_light"
    }
]

def search_files(wiki, prefix):
    url = f"https://{wiki}.fandom.com/api.php?action=query&list=allimages&aiprefix={urllib.parse.quote(prefix)}&ailimit=100&format=json"
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
    try:
        with urllib.request.urlopen(req, timeout=12) as r:
            data = json.loads(r.read().decode('utf-8'))
            return data.get('query', {}).get('allimages', [])
    except Exception as e:
        print(f"Error fetching {wiki} {prefix}: {e}")
        return []

def download_audio(url, wiki, path):
    req = urllib.request.Request(url, headers={
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
        "Referer": f"https://{wiki}.fandom.com/"
    })
    with urllib.request.urlopen(req, timeout=15) as r:
        content = r.read()
        with open(path, 'wb') as f:
            f.write(content)
        print(f"  -> Saved {os.path.basename(path)} ({len(content)} bytes)")

for t in targets:
    cid = t["id"]
    wiki = t["wiki"]
    print(f"\nProcessing {cid} from {wiki}...")
    files = search_files(wiki, t["prefix"])
    print(f"  Found {len(files)} files.")
    
    summon_file = None
    skill_file = None
    ambient_file = None
    hurt_file = None
    
    for item in files:
        name = item['name'].lower()
        if not summon_file and (t['summon_kw'] in name or 'hello' in name or 'begins' in name):
            summon_file = item
        elif not skill_file and (t['skill_kw'] in name or 'burst' in name or 'skill' in name):
            skill_file = item
        elif not ambient_file and (t['ambient_kw'] in name or 'chat' in name):
            ambient_file = item
        elif not hurt_file and (t['hurt_kw'] in name or 'down' in name):
            hurt_file = item
            
    if not summon_file and len(files) > 0: summon_file = files[0]
    if not skill_file and len(files) > 1: skill_file = files[1]
    if not ambient_file and len(files) > 2: ambient_file = files[2]
    if not hurt_file and len(files) > 3: hurt_file = files[3]

    for role, f in [('summon', summon_file), ('skill', skill_file), ('ambient', ambient_file), ('hurt', hurt_file)]:
        if f:
            out_name = f"companion_{cid}_{role}.ogg"
            out_path = os.path.join(sound_dir, out_name)
            try:
                download_audio(f['url'], wiki, out_path)
            except Exception as e:
                print(f"  Error downloading {role} for {cid}: {e}")
