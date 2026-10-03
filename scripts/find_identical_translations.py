import json
import re

with open('src/main/resources/assets/awakening_system/lang/en_us.json', 'r', encoding='utf-8') as f:
    en = json.load(f)

with open('src/main/resources/assets/awakening_system/lang/es_es.json', 'r', encoding='utf-8') as f:
    es = json.load(f)

# Items where en_us == es_es (could be untranslated!)
identical_items = []
for k in en:
    if k.startswith('item.awakening_system.') or k.startswith('block.awakening_system.'):
        # ignore proper nouns, numbers, codes
        v_en = en[k]
        v_es = es.get(k, '')
        if v_en == v_es:
            identical_items.append((k, v_en))

print(f"Total items/blocks identical in en_us and es_es: {len(identical_items)}")
for k, v in identical_items[:40]:
    print(f"  {k}: {v}")

