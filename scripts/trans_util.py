# -*- coding: utf-8 -*-
import requests, urllib.parse, sys, re, json, time, os
from concurrent.futures import ThreadPoolExecutor, as_completed

sys.stdout.reconfigure(encoding='utf-8')

CACHE_FILE = 'scripts/trans_cache.json'
cache = {}
if os.path.exists(CACHE_FILE):
    try:
        with open(CACHE_FILE, 'r', encoding='utf-8') as f:
            cache = json.load(f)
    except Exception:
        cache = {}

def clean_translated(text):
    # Remove space after color codes like §a -> §a
    text = re.sub(r'(§[0-9a-fk-or])\s+', r'\1', text)
    # Fix format specifier spaces: % s -> %s, % d -> %d
    text = re.sub(r'%\s+([0-9]*\.?[0-9]*[sdf])', r'%\1', text)
    return text.strip()

def translate_api(text):
    if text in cache:
        return cache[text]
    
    url = 'https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=en&tl=ru&q=' + urllib.parse.quote(text)
    for attempt in range(3):
        try:
            r = requests.get(url, timeout=6)
            if r.status_code == 200:
                res = r.json()
                if isinstance(res, list) and len(res) > 0:
                    translated = clean_translated(res[0])
                    cache[text] = translated
                    return translated
        except Exception:
            time.sleep(0.5)
    return text

print("Translator utility test ready.")
