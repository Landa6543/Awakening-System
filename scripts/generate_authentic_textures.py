import os
from PIL import Image

OUT_DIR = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\assets\awakening_system\textures\item"
os.makedirs(OUT_DIR, exist_ok=True)

def grid_to_image(grid, palette):
    """Converts a 16x16 string grid and a palette dict into a 16x16 RGBA Image."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    pix = img.load()
    for y, row in enumerate(grid):
        for x, ch in enumerate(row):
            if ch in palette:
                pix[x, y] = palette[ch]
            else:
                pix[x, y] = (0, 0, 0, 0)
    return img

# =========================================================================
# 1. PIECE TEMPLATES (16x16 String Grids)
# =========================================================================

FLOWER_GRID = [
    ".....OOOO.....", # 0
    "....OLLMMO....", # 1
    "...OLWWLLMDO..", # 2
    "...OLLMMMDDO..", # 3
    "..OOMMDDDMMOO.", # 4
    ".OLLMOCCGOMLLO", # 5
    ".OLWOCJJCGOWLMO", # 6
    ".OLMOGJJCGOMMDO", # 7
    ".OMDOGCCGOMDDO", # 8
    "..OOMMDDDMMOO.", # 9
    "...OLMMMMDDO..", # 10
    "...OLLMMMDDO..", # 11
    "....OLLMMDO...", # 12
    "....OLSSMMO...", # 13
    ".....OSSDO....", # 14
    "......OO......", # 15
]
# Pad each row to exactly 16 characters
FLOWER_GRID = [row.center(16, ".") for row in FLOWER_GRID]

PLUME_GRID = [
    "...........OO...", # 0
    "..........OLWO..", # 1
    ".........OLLMO..", # 2
    "........OLLMMO..", # 3
    ".......OLLWMDO..", # 4
    "......OLLWMMDO..", # 5
    ".....OLLWMMDDO..", # 6
    "....OLLWMMDDO...", # 7
    "...OLLWMMDDO....", # 8
    "..OLLWMMDDO.....", # 9
    ".OLMWMMDDO......", # 10
    ".OMWWMMDO.......", # 11
    "..OGGGDO........", # 12
    "...OGGDO........", # 13
    "...OCGDO........", # 14
    "....ODO.........", # 15
]
PLUME_GRID = [row.center(16, ".") for row in PLUME_GRID]

SANDS_GRID = [
    "...OOOOOOOOOO...", # 0
    "...OGGGGGGGGO...", # 1
    "...OOGLWWLGGO...", # 2
    "....OGLWWLGO....", # 3
    "...OGOMLLMOGO...", # 4
    "...OG.OLLMO.GO..", # 5
    "...OG..OWO..GO..", # 6
    "...OG..OWO..GO..", # 7
    "...OG..OWO..GO..", # 8
    "...OG.OLWMO.GO..", # 9
    "...OGOLLWMMOGO..", # 10
    "....OGLWWLGO....", # 11
    "...OOGLWWLGGO...", # 12
    "...OGGGGGGGGO...", # 13
    "...OOOOOOOOOO...", # 14
    "................", # 15
]
SANDS_GRID = [row.center(16, ".") for row in SANDS_GRID]

GOBLET_GRID = [
    "................", # 0
    "...OOOOOOOOOO...", # 1
    "..OLLWWWWLLMMO..", # 2
    "..OLWWCCCWWLMO..", # 3
    "..OMWCCCCCWMDO..", # 4
    "..OMMCCCCMMDDO..", # 5
    "...OMMDDDMDDO...", # 6
    "....OMMDDMDDO...", # 7
    ".....OMGGMD.....", # 8
    "......OGGDO.....", # 9
    "......OGGDO.....", # 10
    "......OGGDO.....", # 11
    ".....OGJJGDO....", # 12
    "...OOGGGGGGGDO..", # 13
    "...OGLWWWWLLGO..", # 14
    "...OOOOOOOOOOO..", # 15
]
GOBLET_GRID = [row.center(16, ".") for row in GOBLET_GRID]

CIRCLET_GRID = [
    "................", # 0
    ".......OO.......", # 1
    "......OLWO......", # 2
    ".OO...OLMO...OO.", # 3
    ".OLW.OLLMMO.OLWO", # 4
    ".OLMOOLLMMOOLMO.", # 5
    ".OMDOOLLMMOOMDO.", # 6
    ".OMDDOGCCGOMDDO.", # 7
    "..OMDGCJJCGMDD..", # 8
    "..OMDGCJJCGMDD..", # 9
    "...OGGGCCGGGO...", # 10
    "..OGLWWWWLLGDO..", # 11
    ".OGGLLMMMMLLGGDO", # 12
    ".OGLWWWWWWLLGGDO", # 13
    "..OGGGGGGGGGGO..", # 14
    "...OOOOOOOOOO...", # 15
]
CIRCLET_GRID = [row.center(16, ".") for row in CIRCLET_GRID]

# =========================================================================
# 2. SET PALETTES (Authentic Genshin Colors with 3-4 shade ramps & highlights)
# =========================================================================

SETS = {
    "crimson_witch": {
        "O": (38, 14, 18, 255),    # Dark obsidian outline
        "D": (135, 18, 26, 255),   # Deep burning crimson
        "M": (205, 32, 38, 255),   # Fire scarlet
        "L": (255, 88, 48, 255),   # Fiery blaze
        "W": (255, 218, 120, 255), # Core fire white-gold
        "C": (255, 155, 0, 255),   # Glowing ember center
        "J": (255, 240, 180, 255), # Intense flame core
        "G": (180, 40, 15, 255),   # Volcanic dark gold
        "S": (45, 25, 30, 255),    # Charred petal base
    },
    "gladiator": {
        "O": (45, 32, 12, 255),    # Dark antique bronze
        "D": (155, 105, 20, 255),  # Shaded bronze
        "M": (218, 165, 32, 255),  # Gladiator gold
        "L": (255, 215, 75, 255),  # Shining laurel gold
        "W": (255, 250, 205, 255), # Sun glint
        "C": (185, 25, 35, 255),   # Arena champion ruby
        "J": (255, 80, 95, 255),   # Bright ruby sparkle
        "G": (230, 180, 45, 255),  # Triumphal gold
        "S": (140, 18, 25, 255),   # Crimson victory sash
    },
    "viridescent": {
        "O": (12, 38, 24, 255),    # Deep forest shadow
        "D": (25, 110, 60, 255),   # Deep emerald
        "M": (45, 175, 95, 255),   # Lush Anemo green
        "L": (105, 225, 145, 255), # Spring breeze mint
        "W": (220, 255, 235, 255), # Pure wind gleam
        "C": (65, 210, 225, 255),  # Celestial Anemo turquoise
        "J": (185, 255, 255, 255), # Bright cyan sparkle
        "G": (195, 230, 185, 255), # Pale leaf silver
        "S": (18, 75, 42, 255),    # Hunter's dark bowstring
    },
    "maiden": {
        "O": (42, 25, 32, 255),    # Soft velvet plum outline
        "D": (185, 95, 125, 255),  # Dusty antique rose
        "M": (235, 145, 175, 255), # Sweet maiden blush
        "L": (255, 195, 215, 255), # Pastel blossom
        "W": (255, 250, 252, 255), # Pure pearl white
        "C": (245, 210, 95, 255),  # Gentle gold pendant
        "J": (255, 245, 185, 255), # Delicate gold sparkle
        "G": (240, 220, 230, 255), # White lace frill
        "S": (210, 115, 145, 255), # Rose ribbon
    },
    "bloodstained": {
        "O": (22, 25, 32, 255),    # Tarnished dark iron
        "D": (55, 62, 75, 255),    # Cold forged steel
        "M": (95, 105, 122, 255),  # Polished steel
        "L": (155, 168, 188, 255), # Blade reflection
        "W": (230, 238, 248, 255), # Cold metallic glint
        "C": (130, 20, 25, 255),   # Dried blood crimson
        "J": (205, 40, 50, 255),   # Fresh blood drop
        "G": (75, 82, 98, 255),    # Iron studding
        "S": (85, 15, 20, 255),    # Bloodstained tarnish
    },
    "berserker": {
        "O": (35, 18, 14, 255),    # Savage earth dark
        "D": (145, 30, 22, 255),   # Primal blood rage
        "M": (215, 55, 35, 255),   # Ferocious crimson
        "L": (255, 110, 70, 255),  # Fury flame
        "W": (255, 235, 215, 255), # Bone horn tip
        "C": (225, 205, 175, 255), # Ancient bone talisman
        "J": (255, 245, 230, 255), # Ivory shine
        "G": (110, 50, 25, 255),   # Rough leather strap
        "S": (85, 20, 15, 255),    # War paint
    },
    "exile": {
        "O": (20, 28, 42, 255),    # Night sky navy
        "D": (35, 75, 125, 255),   # Deep sapphire
        "M": (55, 120, 185, 255),  # Noble azure
        "L": (105, 175, 235, 255), # Faded sky blue
        "W": (225, 242, 255, 255), # Silver glint
        "C": (210, 160, 45, 255),  # Weathered brass insignia
        "J": (255, 220, 110, 255), # Antique gold gleam
        "G": (160, 125, 35, 255),  # Tarnished brass rim
        "S": (30, 55, 90, 255),    # Wanderer's cloak
    },
    "brave_heart": {
        "O": (16, 24, 48, 255),    # Valor midnight outline
        "D": (25, 70, 145, 255),   # Cobalt blue
        "M": (45, 125, 215, 255),  # Radiant hero blue
        "L": (95, 185, 255, 255),  # Brave light
        "W": (255, 255, 255, 255), # Pure diamond gleam
        "C": (245, 195, 35, 255),  # Sun crest gold
        "J": (255, 235, 120, 255), # Golden courage spark
        "G": (225, 170, 25, 255),  # Gold filigree
        "S": (195, 35, 45, 255),   # Heroic crimson ribbon
    }
}

TYPES = [
    ("flower", FLOWER_GRID),
    ("plume", PLUME_GRID),
    ("sands", SANDS_GRID),
    ("goblet", GOBLET_GRID),
    ("circlet", CIRCLET_GRID),
]

# Generate all 40 artifacts
for s_name, pal in SETS.items():
    for t_name, grid in TYPES:
        img = grid_to_image(grid, pal)
        filename = f"artifact_{s_name}_{t_name}.png"
        img.save(os.path.join(OUT_DIR, filename))
        print(f"Generated {filename}")

# =========================================================================
# 3. SPECIAL FOODS & TEYVAT ITEMS (16x16 Handcrafted Pixel Art)
# =========================================================================

# --- A) CRYSTAL CORE (16x16) ---
CRYSTAL_CORE_GRID = [
    "......OO........",
    "....OOWWWO......",
    "...OLLWWWWLO....",
    "..OLWWCCCWWL....",
    ".OLWCCJJCCWLO.O.",
    ".OLCCJJJJCCWOO..",
    "OWCCJJWWJJCCWO..",
    "OWCCJWWWWJCCWO..",
    "OWCCJJWWJJCCWO..",
    ".OLCCJJJJCCWOO..",
    ".OLWCCJJCCWLO.O.",
    "..OLWWCCCWWLO...",
    "...OLLWWWWLO....",
    "....OOWWWO......",
    "......OO........",
    "................",
]
CRYSTAL_CORE_GRID = [r.center(16, ".") for r in CRYSTAL_CORE_GRID]
CRYSTAL_CORE_PAL = {
    "O": (10, 48, 56, 255),    # Deep abyss teal outline
    "D": (25, 115, 130, 255),  # Shaded Anemo sea
    "L": (65, 205, 220, 255),  # Luminous Anemo cyan
    "W": (155, 245, 255, 255), # Translucent crystal wing
    "C": (95, 235, 245, 255),  # Luminous core body
    "J": (255, 255, 255, 255), # Pure starlight gleam
}
grid_to_image(CRYSTAL_CORE_GRID, CRYSTAL_CORE_PAL).save(os.path.join(OUT_DIR, "crystal_core.png"))
print("Generated crystal_core.png")

# --- B) CROWN OF INSIGHT (16x16) ---
CROWN_GRID = [
    "................",
    ".......WW.......",
    "......OLLWO.....",
    ".OO...OLLMO...OO",
    "OWLO.OLLMMO..OLW",
    "OLMO.OMMJDDO.OLM",
    "OMDDOOMJJDDDOMDO",
    "OMMDOGCJJCGOMMDD",
    ".OMDGCCJCCGOMDD.",
    ".OGGCCJJCCGGDO..",
    ".OGLWWWWWWLLGDO.",
    ".OGGLLMMMMLLGGDO",
    ".OGLWWWWWWLLGGDO",
    "..OGGGGGGGGGGO..",
    "...OOOOOOOOOO...",
    "................",
]
CROWN_GRID = [r.center(16, ".") for r in CROWN_GRID]
CROWN_PAL = {
    "O": (55, 38, 8, 255),     # Deep metallic gold shadow
    "D": (155, 105, 15, 255),  # Warm shaded bronze
    "M": (218, 160, 25, 255),  # Regal gold
    "L": (255, 210, 65, 255),  # Crown peak gold
    "W": (255, 250, 195, 255), # Pure sunlight gleam
    "C": (35, 85, 185, 255),   # Crown sapphire core
    "J": (85, 180, 255, 255),  # Radiant jewel highlight
    "G": (240, 185, 45, 255),  # Filigree crown trim
}
grid_to_image(CROWN_GRID, CROWN_PAL).save(os.path.join(OUT_DIR, "crown_of_insight.png"))
print("Generated crown_of_insight.png")

# --- C) SWEET MADAME (16x16) ---
SWEET_MADAME_GRID = [
    "................",
    ".......LL.......",
    "......LWWLO.....",
    ".....LWWWMDO....",
    "....OMMWWMMDO...",
    "...OMMMWWMMMD...",
    "...OMMRRMMMMDO..",
    "..OMRRRRRMMMDDO.",
    "..OMRRRRRMMMDDO.",
    "..OMMMRRMMMMDDO.",
    "...OMMDDDMDDDO..",
    "....OGGGGGGDO...",
    "..OOOPPPPPPOOO..",
    ".OPPPPLWWLPPPPO.",
    ".OPPPPPPPPPPPPO.",
    "..OOOOOOOOOOOO..",
]
SWEET_MADAME_GRID = [r.center(16, ".") for r in SWEET_MADAME_GRID]
SWEET_MADAME_PAL = {
    "O": (45, 22, 10, 255),    # Roasted crust dark border
    "D": (120, 55, 15, 255),   # Deep roasted skin
    "M": (185, 95, 25, 255),   # Golden crispy chicken
    "L": (235, 155, 55, 255),  # Succulent roast highlight
    "W": (255, 225, 130, 255), # Sizzling glaze shine
    "R": (255, 230, 95, 255),  # Sweet flower petal yellow
    "G": (55, 135, 40, 255),   # Sweet flower green garnish
    "P": (230, 232, 238, 255), # Porcelain plate white
}
grid_to_image(SWEET_MADAME_GRID, SWEET_MADAME_PAL).save(os.path.join(OUT_DIR, "sweet_madame.png"))
print("Generated sweet_madame.png")

# --- D) ALMOND TOFU (16x16) ---
ALMOND_TOFU_GRID = [
    "................",
    ".....OOOOOO.....",
    "....OLLWWLLO....",
    "...OLLWWWWLLO...",
    "..OLLWWSRWWLLO..",
    "..OLWWSSRRWWLO..",
    "..OLWWSRRRWWLO..",
    "..OMMWWSRWWMMO..",
    "..OMMMWWWWMMMO..",
    "..OMMMMMMMMMMO..",
    "...OMMMDMDMMO...",
    "....OMDDDDMO....",
    "..OOOGGGGGGOOO..",
    ".OGGGGPPPPPLLGO.",
    ".OGGGGGGGGGGGGO.",
    "..OOOOOOOOOOOO..",
]
ALMOND_TOFU_GRID = [r.center(16, ".") for r in ALMOND_TOFU_GRID]
ALMOND_TOFU_PAL = {
    "O": (25, 30, 38, 255),    # Celadon plate shadow
    "D": (175, 185, 195, 255), # Shaded almond milk tofu
    "M": (225, 232, 240, 255), # Creamy white tofu body
    "L": (248, 250, 254, 255), # Silky tofu light
    "W": (255, 255, 255, 255), # Glossy milk shine
    "S": (235, 160, 35, 255),  # Sweet osmanthus syrup amber
    "R": (210, 45, 45, 255),   # Wolfberry topping red
    "G": (45, 68, 75, 255),    # Dark jade ceramic dish
    "P": (75, 105, 115, 255),  # Ceramic dish highlight
}
grid_to_image(ALMOND_TOFU_GRID, ALMOND_TOFU_PAL).save(os.path.join(OUT_DIR, "almond_tofu.png"))
print("Generated almond_tofu.png")

print("All authentic textures generated successfully!")
