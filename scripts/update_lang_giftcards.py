import json
import collections

entries = {
    "item.awakening_system.gift_card_5": "Tarjeta de Regalo ($5 USD)",
    "item.awakening_system.gift_card_15": "Tarjeta de Regalo ($15 USD)",
    "item.awakening_system.gift_card_30": "Tarjeta de Regalo ($30 USD)",
    "item.awakening_system.gift_card_50": "Tarjeta de Regalo ($50 USD)",
    "item.awakening_system.gift_card_100": "Tarjeta de Regalo ($100 USD)",
}

for lang in ["es_es.json", "en_us.json"]:
    path = f"src/main/resources/assets/awakening_system/lang/{lang}"
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f, object_pairs_hook=collections.OrderedDict)
    for k, v in entries.items():
        data[k] = v
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
    print(f"Updated {lang}")
