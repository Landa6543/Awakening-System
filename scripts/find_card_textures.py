import os
from PIL import Image

item_dir = r'src/main/resources/assets/awakening_system/textures/item'

# Let's search for textures that match colors or patterns of the cards
# Card 1: blue border, yellow trim, dark blue background, pink/white streak/star
# Card 2: red border, dark/black background, red slash
# Card 3: purple border, dark purple background, cyan/magenta pattern
# Card 4: orange background, cyan wings
# Card 5: yellow background, light icon
# Red symbol: red claw/weapon
# Orange circle: orange circular emblem with flame/fire

all_files = [f for f in os.listdir(item_dir) if f.endswith('.png')]

# Let's search by keywords
keywords = ['card', 'rune', 'stone', 'skill', 'ticket', 'emblem', 'badge', 'crest', 'mark', 'seal', 'blood', 'wall', 'dragon', 'step', 'ruler', 'iron']
found = []
for f in all_files:
    for kw in keywords:
        if kw in f.lower():
            found.append(f)
            break

print("Found matching keywords:", len(found))
for f in sorted(found):
    print(f)
