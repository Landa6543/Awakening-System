import json

with open('src/main/resources/assets/awakening_system/lang/es_es.json', 'r', encoding='utf-8') as f:
    es = json.load(f)
with open('src/main/resources/assets/awakening_system/lang/en_us.json', 'r', encoding='utf-8') as f:
    en = json.load(f)

missing_es = []
missing_en = []

for fn in ['src/main/java/com/soybe/awakeningsystem/SellDefinitions.java', 'src/main/java/com/soybe/awakeningsystem/ShopDefinitions.java']:
    with open(fn, 'r', encoding='utf-8') as f:
        for line in f:
            if 'new SellEntry(' in line or 'new Entry(' in line:
                parts = line.split(',')
                if len(parts) >= 5:
                    k = parts[4].strip().replace('"', '').strip()
                    if k.startswith('item.awakening_system.') or k.startswith('block.awakening_system.') or k.startswith('awakening_system.'):
                        if k not in es and k not in missing_es:
                            missing_es.append(k)
                        if k not in en and k not in missing_en:
                            missing_en.append(k)

print(f"Missing in es_es: {len(missing_es)}")
for k in missing_es:
    print(f"  es: {k}")

print(f"Missing in en_us: {len(missing_en)}")
for k in missing_en:
    print(f"  en: {k}")
