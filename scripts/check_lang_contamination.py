import json

with open('src/main/resources/assets/awakening_system/lang/en_us.json', 'r', encoding='utf-8') as f:
    en = json.load(f)

with open('src/main/resources/assets/awakening_system/lang/es_es.json', 'r', encoding='utf-8') as f:
    es = json.load(f)

# Common Spanish words that indicate text was accidentally left in Spanish in en_us
spanish_words = [' de ', ' del ', ' la ', ' las ', ' los ', ' para ', ' con ', ' por ', ' una ', ' uno ', ' este ', ' esta ', ' daño ', ' vida ', ' nivel ', ' mejora ', ' objeto ', ' enemigo ', ' habilidad ', ' ranura ']

spanish_in_en = {}
for k, v in en.items():
    v_lower = ' ' + v.lower() + ' '
    if any(sw in v_lower for sw in spanish_words):
        if not k.endswith('.origin') and 'la_doncella' not in k and 'coup de grâce' not in v.lower():
            spanish_in_en[k] = v

english_words = [' of ', ' the ', ' and ', ' with ', ' damage ', ' level ', ' when ', ' increases ', ' health ', ' attack ', ' critical ', ' skill ', ' slot ', ' weapon ', ' right click ', ' shift ']

english_in_es = {}
for k, v in es.items():
    v_lower = ' ' + v.lower() + ' '
    if any(ew in v_lower for ew in english_words):
        if not k.endswith('.origin') and not k.startswith('item.awakening_system.music_disc_') and 'la_doncella' not in k:
            english_in_es[k] = v

with open('scratch/contamination_report.txt', 'w', encoding='utf-8') as f:
    f.write(f"=== Spanish in en_us ({len(spanish_in_en)}) ===\n")
    for k, v in spanish_in_en.items():
        f.write(f"{k} = {v}\n")
    f.write(f"\n=== English in es_es ({len(english_in_es)}) ===\n")
    for k, v in english_in_es.items():
        f.write(f"{k} = {v}\n")

print(f"Report written! Spanish in en_us: {len(spanish_in_en)}, English in es_es: {len(english_in_es)}")
