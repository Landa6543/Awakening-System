import json
import re

with open('src/main/resources/assets/awakening_system/lang/en_us.json', 'r', encoding='utf-8') as f:
    en = json.load(f)

with open('src/main/resources/assets/awakening_system/lang/es_es.json', 'r', encoding='utf-8') as f:
    es = json.load(f)

with open('src/main/java/com/soybe/awakeningsystem/SystemItems.java', 'r', encoding='utf-8') as f:
    item_code = f.read()

with open('src/main/java/com/soybe/awakeningsystem/block/SystemBlocks.java', 'r', encoding='utf-8') as f:
    block_code = f.read()

items = re.findall(r'ITEMS\.register\(\s*\"([^\"]+)\"', item_code)
blocks = re.findall(r'BLOCKS\.register\(\s*\"([^\"]+)\"', block_code)

print(f"Total items registered: {len(items)}")
print(f"Total blocks registered: {len(blocks)}")

missing_items = []
for it in items:
    k_item = f"item.awakening_system.{it}"
    k_block = f"block.awakening_system.{it}"
    if k_item not in en and k_block not in en:
        missing_items.append(it)

print(f"Items missing from en_us.json: {len(missing_items)}")
for m in missing_items:
    print(f"  Missing item: {m}")

missing_blocks = []
for b in blocks:
    k_block = f"block.awakening_system.{b}"
    if k_block not in en:
        missing_blocks.append(b)

print(f"Blocks missing from en_us.json: {len(missing_blocks)}")
for m in missing_blocks:
    print(f"  Missing block: {m}")
