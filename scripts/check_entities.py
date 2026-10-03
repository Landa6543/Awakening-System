# -*- coding: utf-8 -*-
import re, sys
sys.stdout.reconfigure(encoding='utf-8')

content = open('src/main/java/com/soybe/awakeningsystem/entity/ModEntities.java', 'r', encoding='utf-8').read()
registered_entities = re.findall(r'ENTITY_TYPES\.register\("([^"]+)"', content)
print('Total registered entities in ModEntities:', len(registered_entities))
for e in registered_entities:
    print(' -', e)

# Now check SystemItems for spawn eggs
items_content = open('src/main/java/com/soybe/awakeningsystem/SystemItems.java', 'r', encoding='utf-8').read()
spawn_eggs = re.findall(r'"([^"]+_spawn_egg)"', items_content)
print('\nTotal spawn eggs in SystemItems:', len(spawn_eggs))
for egg in spawn_eggs:
    print(' *', egg)

missing_eggs = []
for e in registered_entities:
    # Some entities are projectiles or bosses or effects, let's check
    expected = e + '_spawn_egg'
    if expected not in spawn_eggs:
        missing_eggs.append(e)

print('\nEntities without a spawn egg:', len(missing_eggs))
for m in missing_eggs:
    print(' !', m)
