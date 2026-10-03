import os
from PIL import Image, ImageDraw

ITEM_DIR = "src/main/resources/assets/awakening_system/textures/item"
BLOCK_DIR = "src/main/resources/assets/awakening_system/textures/block"
ENTITY_DIR = "src/main/resources/assets/awakening_system/textures/entity"

os.makedirs(ITEM_DIR, exist_ok=True)
os.makedirs(BLOCK_DIR, exist_ok=True)
os.makedirs(ENTITY_DIR, exist_ok=True)

# Helper to create an image from pixel string grid
def create_from_grid(grid, palette, size=(16, 16)):
    img = Image.new("RGBA", size, (0, 0, 0, 0))
    for y, row in enumerate(grid):
        for x, char in enumerate(row):
            if char in palette and palette[char] is not None:
                img.putpixel((x, y), palette[char])
    return img

# =========================================================================
# 1. MANA MEATS
# =========================================================================

def gen_mana_beef():
    # Ribeye steak: deep purple-crimson meat, white-cyan crystal bone, brilliant cyan mana marbling
    palette = {
        ' ': None,
        'C': (35, 12, 45, 255),    # Dark seared crust
        'M': (120, 25, 80, 255),   # Raw magical meat
        'D': (155, 35, 100, 255),  # Lighter meat
        'F': (80, 15, 60, 255),    # Shaded meat
        'B': (215, 245, 255, 255), # Crystal bone
        'b': (150, 205, 240, 255), # Bone shadow
        'V': (0, 235, 255, 255),   # Bright cyan mana vein
        'v': (45, 170, 240, 255),  # Secondary mana glow
        'S': (210, 255, 255, 230), # Radiant sparkle
    }
    grid = [
        "    CCCCCCC     ",
        "  CCMMDDDDMCC   ",
        " CMFVMDDDDMDMC  ",
        "CMFVVMDDDMDDMMC ",
        "CMFVMDVMDDDDMMC ",
        "CMMMDVVVMDDDbBC ",
        "CMMDDDVMDDDDbbC ",
        " CMMMDDMDDDDbBC ",
        " CMMMDDMDDDDbBC ",
        "  CMMMDVMDDDbbC ",
        "  CMMMDVVMDDbBC ",
        "   CMMMDVMMbbC  ",
        "    CMMMMMMC    ",
        "     CCCCCC     ",
        "   S            ",
        "            S   "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(ITEM_DIR, "mana_beef.png"))
    print("Generated mana_beef.png")

def gen_mana_porkchop():
    # Porkchop cutlet with bone handle, tender indigo/magenta meat, glistening azure veins
    palette = {
        ' ': None,
        'C': (40, 20, 50, 255),
        'M': (135, 45, 110, 255),
        'D': (175, 65, 135, 255),
        'F': (95, 30, 80, 255),
        'B': (220, 250, 255, 255),
        'b': (140, 200, 235, 255),
        'V': (30, 240, 255, 255),
        'v': (20, 175, 245, 255),
        'S': (200, 255, 255, 220)
    }
    grid = [
        "    CCCCC       ",
        "  CCMDDDMCC     ",
        " CMDDDDDDMMCC   ",
        "CMFVDDDDDDMMMC  ",
        "CMFVVDDDDDDMMC  ",
        "CMFVMDVDDDMMMC  ",
        " CMFMDVVMDMMMC  ",
        " CMFMDDVMMMMC   ",
        "  CFMMDVMMMC    ",
        "  CFMMMDVMMC    ",
        "   CFFMMMMMC    ",
        "    CCbBCCCC    ",
        "     CbBC       ",
        "      CbBC      ",
        "       CC       ",
        "                "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(ITEM_DIR, "mana_porkchop.png"))
    print("Generated mana_porkchop.png")

def gen_mana_mutton():
    # Mutton cutlet: slender bone, ruby-amethyst flesh, wrapped with turquoise mana sinew
    palette = {
        ' ': None,
        'C': (45, 15, 40, 255),
        'M': (115, 25, 75, 255),
        'D': (150, 40, 95, 255),
        'F': (80, 18, 55, 255),
        'B': (210, 245, 255, 255),
        'b': (135, 195, 230, 255),
        'V': (0, 245, 240, 255),
        'v': (25, 185, 225, 255),
        'S': (220, 255, 255, 240)
    }
    grid = [
        "      CCCCC     ",
        "    CCMMDDMCC   ",
        "   CMDDDDDDMMC  ",
        "  CMFVDDDDDDMMC ",
        "  CMFVVDDDDMDMC ",
        "  CMFVMDVMDDDMC ",
        "   CMFMDVVMDMMC ",
        "   CFFMDDVMDMMC ",
        "    CCFFMDVMMC  ",
        "     CCFFMMMC   ",
        "      CbBCCCC   ",
        "      CbBC      ",
        "       CbBC     ",
        "        CbBC    ",
        "         CC     ",
        "                "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(ITEM_DIR, "mana_mutton.png"))
    print("Generated mana_mutton.png")

def gen_mana_chicken_meat():
    # Plump chicken drumstick with crystal knuckle bone and glowing cyan-blue magical meat
    palette = {
        ' ': None,
        'C': (30, 35, 65, 255),
        'M': (60, 100, 185, 255),
        'D': (85, 145, 235, 255),
        'F': (40, 70, 140, 255),
        'B': (225, 245, 255, 255),
        'b': (155, 210, 245, 255),
        'V': (0, 255, 255, 255),
        'v': (50, 195, 255, 255),
        'S': (230, 255, 255, 240)
    }
    grid = [
        "      CCCC      ",
        "    CCDDDDCC    ",
        "   CDDDDDDDMCC  ",
        "  CDDSDDDDDDMCC ",
        "  CDDVVDDDDDDMC ",
        "  CDDVMDVDDDDMC ",
        "  CMFVMDVVMDDMC ",
        "   CMFMDDVMDMMC ",
        "    CMFFMDVMMC  ",
        "     CCFFMMC    ",
        "      CbBC      ",
        "     CbBbBC     ",
        "    CbBBbBC     ",
        "     CbBbBC     ",
        "      CCCC      ",
        "                "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(ITEM_DIR, "mana_chicken_meat.png"))
    print("Generated mana_chicken_meat.png")

# =========================================================================
# 2. GENSHIN DROPS & MONSTER MATERIALS
# =========================================================================

def gen_slime_condensate():
    # Authentic Genshin 1★ Slime Condensate: pure translucent turquoise droplet
    palette = {
        ' ': None,
        'O': (15, 115, 145, 255),   # Outer outline
        'D': (25, 160, 185, 230),   # Darker body
        'T': (45, 205, 225, 235),   # Main turquoise
        'L': (95, 240, 250, 245),   # Light inner glow
        'H': (225, 255, 255, 255),  # Specular highlight
    }
    grid = [
        "       O        ",
        "      OTO       ",
        "     OTLTO      ",
        "     OTLTTO     ",
        "    OTLLTTTO    ",
        "    OTHLTTTTO   ",
        "   OTHLLTTTTO   ",
        "   OTLLLTTTTDO  ",
        "  OTLLLLTTTTTDO ",
        "  OTLLLLTTTTTDO ",
        "  OTTTTTTTTTTDO ",
        "  ODTTTTTTTTDDO ",
        "   ODDTTTTTDDO  ",
        "    ODDDDDDDO   ",
        "      OOOOO     ",
        "                "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(ITEM_DIR, "slime_condensate.png"))
    print("Generated slime_condensate.png")

def gen_slime_secretions():
    # Authentic Genshin 2★ Slime Secretions: viscous splashing puddle with bubbles
    palette = {
        ' ': None,
        'O': (15, 100, 130, 255),
        'D': (25, 150, 180, 230),
        'T': (40, 200, 220, 235),
        'L': (85, 235, 245, 245),
        'H': (220, 255, 255, 255),
        'B': (130, 245, 255, 230),  # Inner bubble
    }
    grid = [
        "      O    O    ",
        "     OTO  OTO   ",
        "     OTLDOODTO  ",
        "   OOOTLLTTTDDO ",
        "  OTTTTLLTTTTDTO",
        " OTHTLLBBTTTTTTO",
        "OTTHLTTBLTTTTTTO",
        "OTLLLTTBBTTHTDTO",
        "OTLLLLTTTTTLTDDO",
        "ODTLLLLTTTTLLDDO",
        " ODDTTTLLTTLTDDO",
        "  ODDTTTTTTTTDO ",
        "  OODDDTTTTDDO  ",
        "   OOODDDDDDO   ",
        "     OOOOOO     ",
        "                "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(ITEM_DIR, "slime_secretions.png"))
    print("Generated slime_secretions.png")

def gen_slime_concentrate():
    # Authentic Genshin 3★ Slime Concentrate: pure radiant orb with core and light facets
    palette = {
        ' ': None,
        'O': (10, 95, 130, 255),
        'D': (20, 145, 180, 240),
        'T': (40, 210, 235, 250),
        'L': (90, 240, 250, 255),
        'N': (190, 255, 255, 255), # Intense nucleus
        'H': (255, 255, 255, 255), # Core white
    }
    grid = [
        "     OOOOO      ",
        "   OOTTTTTOO    ",
        "  OTTTLLLTTTTO  ",
        " OTTLLNNLLTTTTO ",
        " OTHLNHHNLLTTDO ",
        "OTHLNNHHNNLTTDDO",
        "OTHLLNHHNLLTTDDO",
        "OTLLLNNNNLLTTDDO",
        "OTTTLLLLLLTTTDDO",
        "OTTTTTTTTTTTTDDO",
        " OTTTTTTTTTTDDO ",
        " OTTTTTTTTTDDO  ",
        "  ODDTTTTTTDDO  ",
        "   OODDDDDDOO   ",
        "     OOOOOO     ",
        "                "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(ITEM_DIR, "slime_concentrate.png"))
    print("Generated slime_concentrate.png")

def gen_condensed_resin():
    # Authentic Genshin 4★ Condensed Resin: golden faceted teardrop enclosing a cyan crystal butterfly
    palette = {
        ' ': None,
        'A': (140, 75, 10, 255),    # Dark amber rim
        'O': (195, 115, 15, 255),   # Orange amber
        'Y': (245, 175, 30, 240),   # Golden amber
        'L': (255, 215, 65, 245),   # Bright amber body
        'H': (255, 250, 180, 255),  # Golden highlight
        'B': (0, 220, 245, 255),    # Butterfly wing teal
        'b': (140, 255, 255, 255),  # Butterfly inner glow
        'W': (240, 255, 255, 255),  # Butterfly nucleus
    }
    grid = [
        "       A        ",
        "      AOA       ",
        "     AOYOA      ",
        "     AOYLOA     ",
        "    AOYLLLOA    ",
        "    AOHLYLLOA   ",
        "   AOHLLBLLLYA  ",
        "   AOHLbBbLLLYA ",
        "  AOYLLBbWbLLLYA",
        "  AOYLLLbBbLLLYA",
        "  AOYLLLBBBLLLYA",
        "  AOYLLLLLLLLYA ",
        "   AOOYYYYYYOA  ",
        "    AAOOOOOAA   ",
        "      AAAAA     ",
        "                "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(ITEM_DIR, "condensed_resin.png"))
    print("Generated condensed_resin.png")

def gen_damaged_mask():
    # Authentic Genshin 1★ Damaged Mask: Hilichurl porcelain mask with red tribal warpaint, broken right edge
    palette = {
        ' ': None,
        'O': (140, 135, 125, 255),  # Porcelain border
        'W': (245, 242, 235, 255),  # White porcelain
        'S': (190, 185, 175, 255),  # Shaded porcelain
        'R': (215, 35, 35, 255),    # Crimson warpaint
        'r': (155, 20, 20, 255),    # Dark red
        'E': (25, 20, 25, 255),     # Eyehole dark void
        'X': (70, 65, 60, 255),     # Broken fracture edge
    }
    grid = [
        "    OOOOO       ",
        "  OOWWRRWOO     ",
        " OWWWRRRWWWO    ",
        "OWWWRRRRRWWWO   ",
        "OWWRRRERRRWWX   ",
        "OWWRRREERRWWX   ",
        "OWWRRREERRWX    ",
        "OWWWRREERRWX    ",
        "OWWWWWERRWWX    ",
        "OWWWWWERRWWO    ",
        " OWWWWWWWWOO    ",
        "  OWWWWWWOO     ",
        "   OWWWWOO      ",
        "    OOOOO       ",
        "                ",
        "                "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(ITEM_DIR, "damaged_mask.png"))
    print("Generated damaged_mask.png")

def gen_stained_mask():
    # Authentic Genshin 2★ Stained Mask: full mask with symmetrical red arches and battle dirt
    palette = {
        ' ': None,
        'O': (130, 125, 115, 255),
        'W': (242, 238, 230, 255),
        'S': (180, 175, 160, 255),
        'D': (145, 130, 110, 255),  # Earth stain
        'R': (215, 30, 30, 255),    # Red warpaint
        'r': (150, 20, 20, 255),
        'E': (25, 20, 25, 255),     # Eye slit
    }
    grid = [
        "    OOOOOOO     ",
        "  OOWWRRRWWOO   ",
        " OWWWWRRRWWWWO  ",
        "OWWWWRRRRRWWWWO ",
        "OWWRREEEERRWWWO ",
        "OWWRREEEERRWWWO ",
        "OWWRREEEERRWWDO ",
        "OWDWRREERRRWSDO ",
        "OWDWRREERRWSSDO ",
        "OWDWDWWWWDDSSDO ",
        "OWDWDWWWWDDSSDO ",
        " OWWDDWWDDSSDO  ",
        "  OWWDDDDSSDO   ",
        "   OOWWSSDOO    ",
        "     OOOOOO     ",
        "                "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(ITEM_DIR, "stained_mask.png"))
    print("Generated stained_mask.png")

def gen_ominous_mask():
    # Authentic Genshin 3★ Ominous Mask: Menacing mask with TWO large curved horns and fierce markings
    palette = {
        ' ': None,
        'H': (45, 40, 48, 255),     # Black horn
        'h': (75, 70, 80, 255),     # Horn highlight
        'O': (125, 120, 110, 255),
        'W': (240, 235, 225, 255),
        'S': (175, 170, 160, 255),
        'R': (225, 20, 20, 255),    # Vivid red warpaint
        'r': (140, 10, 10, 255),
        'E': (15, 10, 15, 255),     # Dark eye socket
        'e': (180, 20, 20, 255),    # Glowing eye spark
    }
    grid = [
        "H             H ",
        "Hh           hH ",
        " Hh  OOOOO  hH  ",
        " HhOOWRRRWWOhH  ",
        "  HOWWRRRWWSOH  ",
        "  OWWWRRRWWWSO  ",
        " OWWRReeeERRWSO ",
        " OWWRREEEERRWSO ",
        " OWWRREEEERRWSO ",
        " OWWWRRRRRRWWSO ",
        " OWWWWERRREWWSO ",
        "  OWWWWEEEWWSO  ",
        "   OWWWWWWWSO   ",
        "    OOWWSSOO    ",
        "      OOOO      ",
        "                "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(ITEM_DIR, "ominous_mask.png"))
    print("Generated ominous_mask.png")

def gen_heavy_horn():
    # Authentic Genshin 2★ Heavy Horn: curved Mitachurl horn with bone rings and bronze band
    palette = {
        ' ': None,
        'O': (75, 65, 50, 255),     # Dark ridge outline
        'W': (230, 220, 195, 255),  # Ivory bone
        'w': (200, 190, 160, 255),  # Shaded bone
        'D': (145, 135, 105, 255),  # Deep horn groove
        'G': (190, 130, 45, 255),   # Bronze tribal ring
        'g': (130, 85, 25, 255),    # Bronze shadow
        'T': (85, 70, 55, 255),     # Sharp horn tip
    }
    grid = [
        "             TT ",
        "            TOwT",
        "           TOwWT",
        "          TOwWWT",
        "         TOwWWOT",
        "        TOwWWOT ",
        "       TOwDWWOT ",
        "      TOwDWWOT  ",
        "     TOwDWWOT   ",
        "    TOgGWGWOT   ",
        "   TOgGGWGWOT   ",
        "  TOgGGWGWOT    ",
        " TOwDWWWWWOT    ",
        " TOwDWWWWWOT    ",
        "  OOWWWWWWOO    ",
        "    OOOOOO      "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(ITEM_DIR, "heavy_horn.png"))
    print("Generated heavy_horn.png")

# =========================================================================
# 3. TEYVAT SAPLINGS (100% Transparent Background, Lush Pixel Art)
# =========================================================================

def gen_cuihua_sapling():
    # Rounded fruiting sapling with orange Cuihua fruit bud on a slender woody stem
    palette = {
        ' ': None,
        'S': (85, 55, 30, 255),     # Soil mound
        's': (120, 80, 45, 255),
        'W': (105, 70, 35, 255),    # Wood stem
        'w': (75, 50, 25, 255),
        'L': (70, 160, 40, 255),    # Leaf main
        'l': (40, 105, 25, 255),    # Leaf dark
        'H': (110, 205, 55, 255),   # Leaf highlight
        'F': (255, 140, 30, 255),   # Cuihua orange fruit
        'f': (210, 95, 15, 255),    # Fruit shadow
    }
    grid = [
        "       HH       ",
        "      HLLH      ",
        "     HLLFfl     ",
        "    HLLLFfll    ",
        "    HLLLLlll    ",
        "   HLLLLlllll   ",
        "   HLLLLlllll   ",
        "    HLLLLlll    ",
        "     llWWll     ",
        "      lWWl      ",
        "       WW       ",
        "       WW       ",
        "      sWWs      ",
        "     ssWWss     ",
        "    SSSSSSSS    ",
        "                "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(BLOCK_DIR, "cuihua_sapling.png"))
    print("Generated cuihua_sapling.png")

def gen_adhigama_sapling():
    # Sturdy rainforest sapling with buttress rootlets and layered deep olive-emerald leaves
    palette = {
        ' ': None,
        'S': (65, 50, 30, 255),
        's': (95, 75, 45, 255),
        'W': (80, 75, 45, 255),
        'w': (55, 50, 30, 255),
        'L': (35, 135, 65, 255),
        'l': (20, 85, 40, 255),
        'H': (75, 195, 95, 255),
    }
    grid = [
        "      HHH       ",
        "     HLLLH      ",
        "    HLLLLLH     ",
        "   HLLLLLLLH    ",
        "  HLLLLLLLLLH   ",
        "  HLLLLllllLH   ",
        "   HLLlwWllH    ",
        "    lllWWlll    ",
        "     HlWWlH     ",
        "    HLLWWLLl    ",
        "    HLLWWLLl    ",
        "     llWWll     ",
        "      wWWw      ",
        "     wwWWww     ",
        "    SSSSSSSS    ",
        "                "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(BLOCK_DIR, "adhigama_sapling.png"))
    print("Generated adhigama_sapling.png")

def gen_sandbearer_sapling():
    # Mountain pine bonsai sapling with horizontal fan-shaped golden-amber foliage tiers
    palette = {
        ' ': None,
        'S': (90, 75, 50, 255),
        's': (130, 110, 75, 255),
        'W': (115, 95, 65, 255),
        'w': (80, 65, 45, 255),
        'L': (230, 145, 30, 255),
        'l': (165, 90, 15, 255),
        'H': (255, 195, 65, 255),
    }
    grid = [
        "       HH       ",
        "     HHLLHH     ",
        "    HLLLLLLl    ",
        "     lllWWl     ",
        "       lWWl     ",
        "    HHH lW HHH  ",
        "  HHLLLllWLLLLl ",
        " HLLLLLLWWLLLLl ",
        "  llllllWWlllll ",
        "        WW      ",
        "        WW      ",
        "       wWW      ",
        "      swWWs     ",
        "     sssWWss    ",
        "    SSSSSSSS    ",
        "                "
    ]
    img = create_from_grid(grid, palette)
    img.save(os.path.join(BLOCK_DIR, "sandbearer_sapling.png"))
    print("Generated sandbearer_sapling.png")

# =========================================================================
# 4. WOOD LOG TOPS & LEAVES (Detailed Rings and Cutout Gaps)
# =========================================================================

def gen_detailed_log_top(name, bark_dark, bark_light, wood_bg, ring_dark, ring_light):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    d = ImageDraw.Draw(img)
    # 1. Fill base wood
    d.rectangle([0, 0, 15, 15], fill=wood_bg)
    # 2. Outer bark ring (2 pixels thick on perimeter)
    for y in range(16):
        for x in range(16):
            if x in [0, 1, 14, 15] or y in [0, 1, 14, 15]:
                c = bark_dark if (x * 3 + y * 7) % 5 in [0, 1] else bark_light
                img.putpixel((x, y), c)
    # 3. Concentric annual growth rings
    rings = [
        (3, 3, 12, 12),
        (5, 5, 10, 10),
    ]
    for r in rings:
        d.ellipse(r, outline=ring_dark)
    # Inner heartwood
    img.putpixel((7, 7), ring_dark)
    img.putpixel((8, 7), ring_light)
    img.putpixel((7, 8), ring_light)
    img.putpixel((8, 8), ring_dark)
    # Wood grain flecks
    for p in [(4, 6), (11, 8), (6, 11), (9, 4), (5, 9), (10, 6)]:
        img.putpixel(p, ring_dark)

    img.save(os.path.join(BLOCK_DIR, f"{name}_log_top.png"))
    print(f"Generated {name}_log_top.png")

def gen_minecraft_leaves(name, base_rgb, dark_rgb, light_rgb, accent_rgb=None):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    import random
    rng = random.Random(42 + hash(name))
    for y in range(16):
        for x in range(16):
            # 18% transparent cutout gaps for authentic Minecraft leaf look
            if rng.random() < 0.18:
                continue
            r = rng.random()
            if accent_rgb and r < 0.08:
                c = accent_rgb
            elif r < 0.45:
                c = base_rgb
            elif r < 0.75:
                c = dark_rgb
            else:
                c = light_rgb
            img.putpixel((x, y), c)
    img.save(os.path.join(BLOCK_DIR, f"{name}_leaves.png"))
    print(f"Generated {name}_leaves.png")

# =========================================================================
# 5. WYVERN SADDLE ENTITY TEXTURE (256x256 Overlay)
# =========================================================================

def gen_wyvern_saddle_entity():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Saddle seat on back of torso (UV x: 20 to 32, y: 4 to 16)
    # In 256x256 map, that's scaled by 4! (80 to 128, 16 to 64)
    # 1. Main leather saddle pad
    d.rectangle([82, 20, 126, 60], fill=(75, 40, 20, 255), outline=(45, 25, 12, 255), width=2)
    # 2. Golden rim and filigree
    d.rectangle([86, 24, 122, 56], outline=(230, 185, 45, 255), width=2)
    # 3. Inner velvet cushion
    d.rectangle([90, 28, 118, 52], fill=(130, 20, 30, 255))
    # 4. Azure dragon gem in center
    d.ellipse([100, 36, 108, 44], fill=(0, 235, 255, 255), outline=(255, 255, 255, 255))

    # Girth straps wrapping torso sides (UV x: 6 to 14, y: 20 to 30 -> 24 to 56, 80 to 120)
    d.rectangle([28, 85, 48, 115], fill=(65, 35, 18, 255), outline=(35, 20, 10, 255))
    d.rectangle([34, 95, 42, 105], fill=(225, 180, 40, 255)) # Golden buckle

    # Other side (UV x: 38 to 46, y: 20 to 30 -> 152 to 184, 80 to 120)
    d.rectangle([156, 85, 176, 115], fill=(65, 35, 18, 255), outline=(35, 20, 10, 255))
    d.rectangle([162, 95, 170, 105], fill=(225, 180, 40, 255)) # Golden buckle

    img.save(os.path.join(ENTITY_DIR, "wyvern_saddle.png"))
    print("Generated wyvern_saddle.png (256x256 entity overlay)")

def main():
    print("--- Generating Mana Meats ---")
    gen_mana_beef()
    gen_mana_porkchop()
    gen_mana_mutton()
    gen_mana_chicken_meat()

    print("--- Generating Genshin Drops ---")
    gen_slime_condensate()
    gen_slime_secretions()
    gen_slime_concentrate()
    gen_condensed_resin()
    gen_damaged_mask()
    gen_stained_mask()
    gen_ominous_mask()
    gen_heavy_horn()

    print("--- Generating Saplings ---")
    gen_cuihua_sapling()
    gen_adhigama_sapling()
    gen_sandbearer_sapling()

    print("--- Generating Wood Tops & Leaves ---")
    gen_detailed_log_top("cuihua", (85, 55, 25, 255), (170, 125, 70, 255), (210, 165, 105, 255), (160, 115, 60, 255), (230, 185, 125, 255))
    gen_detailed_log_top("adhigama", (50, 48, 30, 255), (120, 110, 75, 255), (160, 150, 110, 255), (110, 100, 65, 255), (180, 170, 130, 255))
    gen_detailed_log_top("sandbearer", (110, 90, 60, 255), (180, 155, 110, 255), (225, 200, 155, 255), (165, 140, 95, 255), (245, 220, 175, 255))

    gen_minecraft_leaves("cuihua", (65, 145, 40, 255), (35, 95, 25, 255), (115, 195, 60, 255), (255, 140, 40, 255))
    gen_minecraft_leaves("adhigama", (35, 120, 65, 255), (20, 75, 40, 255), (65, 175, 95, 255))
    gen_minecraft_leaves("sandbearer", (225, 140, 30, 255), (160, 90, 15, 255), (255, 185, 60, 255))

    print("--- Generating Wyvern Saddle Overlay ---")
    gen_wyvern_saddle_entity()

    print("All textures generated successfully!")

if __name__ == "__main__":
    main()
