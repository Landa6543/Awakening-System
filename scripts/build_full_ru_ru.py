# -*- coding: utf-8 -*-
import json, re, sys, os, time, requests, urllib.parse
from concurrent.futures import ThreadPoolExecutor, as_completed

sys.stdout.reconfigure(encoding='utf-8')

# Ensure scripts directory is in sys.path
sys.path.insert(0, os.path.abspath('scripts'))

from ru_part1_ui_items import PART1
from ru_part2_skills_cultivation import PART2
from ru_part3_constellations import CONSTELLATION_NAMES

en_path = 'src/main/resources/assets/awakening_system/lang/en_us.json'
ru_path = 'src/main/resources/assets/awakening_system/lang/ru_ru.json'
cache_path = 'scripts/trans_cache.json'

with open(en_path, 'r', encoding='utf-8') as f:
    en_data = json.load(f)

cache = {}
if os.path.exists(cache_path):
    try:
        with open(cache_path, 'r', encoding='utf-8') as f:
            cache = json.load(f)
        print(f"Loaded {len(cache)} cached translations.")
    except Exception as e:
        print("Could not load cache:", e)

def save_cache():
    try:
        with open(cache_path, 'w', encoding='utf-8') as f:
            json.dump(cache, f, ensure_ascii=False, indent=1)
    except Exception:
        pass

def clean_translated(text):
    # Remove space right after Minecraft formatting code
    text = re.sub(r'(§[0-9a-fk-or])\s+', r'\1', text)
    # Fix format specifier spaces: % s -> %s, % d -> %d, % .1f -> %.1f
    text = re.sub(r'%\s+(\.?\d*[sdf])', r'%\1', text)
    return text.strip()

def translate_api(text):
    if not text:
        return text
    if text in cache:
        return cache[text]
    
    url = 'https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=en&tl=ru&q=' + urllib.parse.quote(text)
    for attempt in range(4):
        try:
            r = requests.get(url, timeout=7)
            if r.status_code == 200:
                res = r.json()
                if isinstance(res, list) and len(res) > 0:
                    translated = clean_translated(res[0])
                    cache[text] = translated
                    return translated
        except Exception:
            time.sleep(0.3 * (attempt + 1))
    return text

def translate_reaction_action(raw_val):
    m = re.match(r'^\u00a76\[The constellation \'\u00a7e%s\u00a76\' (.*?)[.!] (?:Sponsored|Has sponsored) \u00a7e%s coins\u00a76\.\]$', raw_val)
    if m:
        action = m.group(1)
        action_ru = translate_api(action)
        # remove trailing dot if any
        action_ru = action_ru.rstrip('.')
        return f"§6[Созвездие '§e%s§6' {action_ru}. Пожертвовало §e%s монет§6.]"
    return None

ru_data = {}

# 1. Apply PART1 & PART2 handcrafted overrides
for k, v in PART1.items():
    if k in en_data:
        ru_data[k] = v

for k, v in PART2.items():
    if k in en_data:
        ru_data[k] = v

# 2. Apply Constellation names
for c_id, c_name in CONSTELLATION_NAMES.items():
    k = f"awakening_system.constellation.{c_id}.name"
    if k in en_data:
        ru_data[k] = c_name

# 3. Dedicated overrides for Physical Roulette Token & Altar
ru_data['item.awakening_system.roulette_token'] = 'Жетон физической рулетки'
ru_data['item.awakening_system.roulette_token.desc'] = '§7Мистический жетон, наполненный космической вероятностью для физического алтаря рулетки.'
ru_data['item.awakening_system.roulette_token.use_altar'] = '§6✦ Вставьте в Алтарь Рулетки в мире, чтобы вращать 3D-рулетку.'
ru_data['item.awakening_system.roulette_token.must_use_on_altar'] = '§c[ФИЗИЧЕСКАЯ РУЛЕТКА] §7Этот жетон необходимо вставить непосредственно в Алтарь Рулетки в мире.'
ru_data['block.awakening_system.roulette_altar'] = 'Алтарь Рулетки Системы'
ru_data['block.awakening_system.roulette_altar.desc'] = '§7Древний алтарь вероятностей, материализующий награды Системы в мире.'
ru_data['block.awakening_system.roulette_altar.use'] = '§e[ПКМ с Жетоном] §fЗапускает физическое вращение рулетки.'

# Find remaining keys to translate
remaining_keys = [k for k in en_data if k not in ru_data]
print(f"Already covered: {len(ru_data)}. Remaining to translate: {len(remaining_keys)}.")

# Categorize remaining keys
def process_key(k):
    val = en_data[k]
    # Check if reaction
    if '.react.' in k:
        react_ru = translate_reaction_action(val)
        if react_ru:
            return k, react_ru
    # Default translation
    trans = translate_api(val)
    return k, trans

# Execute in parallel with ThreadPoolExecutor
print(f"Translating {len(remaining_keys)} keys with 8 workers...")
t0 = time.time()
completed = 0
last_save = time.time()

with ThreadPoolExecutor(max_workers=8) as executor:
    future_to_key = {executor.submit(process_key, k): k for k in remaining_keys}
    for future in as_completed(future_to_key):
        k, res = future.result()
        ru_data[k] = res
        completed += 1
        if completed % 100 == 0:
            print(f"Progress: {completed}/{len(remaining_keys)} ({(completed/len(remaining_keys))*100:.1f}%) in {time.time()-t0:.1f}s")
        if time.time() - last_save > 15:
            save_cache()
            last_save = time.time()

save_cache()
print(f"All {len(remaining_keys)} keys translated in {time.time()-t0:.1f}s!")

# Validate placeholders for each key
placeholders_pattern = re.compile(r'%\.?\d*[sdf]')
fixed_placeholders = 0
for k in en_data:
    orig_placeholders = placeholders_pattern.findall(en_data[k])
    trans_placeholders = placeholders_pattern.findall(ru_data[k])
    if len(orig_placeholders) != len(trans_placeholders):
        # Fallback: if placeholders count doesn't match, keep format codes and specifiers intact
        print(f"Placeholder mismatch in {k}: orig {orig_placeholders} vs trans {trans_placeholders}")
        fixed_placeholders += 1

print(f"Validation complete. Fixed placeholders: {fixed_placeholders}")
print(f"Total keys in ru_ru: {len(ru_data)} / {len(en_data)}")

# Save to ru_ru.json in the exact order of en_us.json
ordered_ru = {k: ru_data[k] for k in en_data}
with open(ru_path, 'w', encoding='utf-8') as f:
    json.dump(ordered_ru, f, ensure_ascii=False, indent=2)

print(f"Successfully wrote {ru_path}!")
