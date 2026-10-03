import json
import collections

sounds_json_path = "src/main/resources/assets/awakening_system/sounds.json"

with open(sounds_json_path, "r", encoding="utf-8") as f:
    data = json.load(f, object_pairs_hook=collections.OrderedDict)

characters = [
    'furina', 'raiden_shogun', 'hu_tao', 'zhongli', 'nahida', 'kazuha', 'noelle',
    'gojo_satoru', 'sung_jinwoo', 'cha_haein', 'diluc', 'jean', 'bennett',
    'xiao', 'arlecchino', 'acheron', 'kafka', 'firefly', 'ellen_joe',
    'igris', 'beru', 'sukuna', 'yuji_itadori', 'megumi',
    'artoria_pendragon', 'gilgamesh', 'archer', 'rengoku', 'tanjiro', 'nezuko'
]

added_count = 0
for c in characters:
    for act in ['summon', 'skill', 'ambient', 'hurt']:
        key = f"companion_{c}_{act}"
        if key not in data:
            data[key] = {
                "category": "voice",
                "subtitle": f"subtitles.awakening_system.{key}",
                "sounds": [
                    f"awakening_system:companion/{key}"
                ]
            }
            added_count += 1
        else:
            # Ensure category is "voice"
            data[key]["category"] = "voice"

with open(sounds_json_path, "w", encoding="utf-8") as f:
    json.dump(data, f, indent=2, ensure_ascii=False)

print(f"Updated sounds.json! Added {added_count} companion sound events. Total keys: {len(data)}")
