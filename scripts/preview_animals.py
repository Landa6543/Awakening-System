# -*- coding: utf-8 -*-
from PIL import Image

animals = ["mana_cow.png", "mana_pig.png", "mana_sheep.png", "mana_sheep_fur.png", "mana_chicken.png"]
showcase = Image.new("RGBA", (64 * 8, 32 * 8 * len(animals)), (20, 24, 35, 255))

for i, name in enumerate(animals):
    im = Image.open(f"src/main/resources/assets/awakening_system/textures/entity/animal/{name}")
    scaled = im.resize((64 * 8, 32 * 8), Image.NEAREST)
    showcase.paste(scaled, (0, i * 32 * 8), scaled)

showcase.save("scratch/mana_animals_showcase.png")
print("Saved scratch/mana_animals_showcase.png")
