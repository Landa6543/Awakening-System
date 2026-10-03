import os
from PIL import Image, ImageDraw

ITEM_DIR = "src/main/resources/assets/awakening_system/textures/item"
ARMOR_DIR = "src/main/resources/assets/awakening_system/textures/models/armor"

os.makedirs(ITEM_DIR, exist_ok=True)
os.makedirs(ARMOR_DIR, exist_ok=True)

# -------------------------------------------------------------
# 1. HILICHURL / MITACHURL DROPS
# -------------------------------------------------------------

def gen_heavy_horn():
    # 32x32 Heavy Horn (Mitachurl curved battle horn)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Horn curve path: from base at bottom right (24, 28) curving up and left to tip at (6, 5)
    points = [
        # (x, y, radius, color_base, color_highlight)
        (24, 27, 5, (70, 50, 35), (160, 120, 80)),   # Base root
        (22, 24, 5, (90, 70, 50), (190, 150, 100)),
        (19, 20, 4.5, (120, 95, 70), (220, 185, 130)), # Tribal ring area
        (16, 16, 4, (140, 115, 85), (235, 205, 150)),
        (13, 12, 3.2, (160, 135, 100), (245, 225, 175)),
        (10, 8, 2.5, (180, 155, 120), (255, 240, 200)),
        (7, 5, 1.5, (200, 180, 140), (255, 250, 225)),  # Sharp tip
        (5, 4, 0.8, (220, 200, 160), (255, 255, 245))
    ]
    
    # Draw horn body
    for x, y, r, c_dark, c_light in points:
        ir = int(r)
        for dx in range(-ir, ir + 1):
            for dy in range(-ir, ir + 1):
                if dx*dx + dy*dy <= r*r:
                    # light from top-left
                    factor = ((-dx - dy) / (2.0 * r) + 0.5)
                    factor = max(0.0, min(1.0, factor))
                    cr = int(c_dark[0] * (1 - factor) + c_light[0] * factor)
                    cg = int(c_dark[1] * (1 - factor) + c_light[1] * factor)
                    cb = int(c_dark[2] * (1 - factor) + c_light[2] * factor)
                    px = max(0, min(31, int(x + dx)))
                    py = max(0, min(31, int(y + dy)))
                    img.putpixel((px, py), (cr, cg, cb, 255))
                    
    # Tribal golden ring at base (19, 20)
    for dy in range(-1, 2):
        d.line([(16, 21 + dy), (23, 19 + dy)], fill=(255, 200, 40, 255), width=1)
        d.line([(18, 20 + dy), (22, 19 + dy)], fill=(255, 240, 120, 255), width=1)
        
    # Carved tribal glyphs on bone
    d.line([(13, 14), (16, 16)], fill=(80, 50, 30, 255), width=1)
    d.line([(10, 10), (12, 11)], fill=(80, 50, 30, 255), width=1)
    
    # Outline & shading
    img_16 = img.resize((16, 16), Image.Resampling.LANCZOS)
    img_16.save(os.path.join(ITEM_DIR, "heavy_horn.png"))
    print("Saved heavy_horn.png")

def gen_damaged_mask():
    # 16x16 Damaged Mask (cracked white bone mask, broken horn)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Mask base oval
    d.polygon([(4, 4), (11, 4), (13, 8), (11, 14), (4, 14), (2, 8)], fill=(230, 225, 215, 255), outline=(90, 80, 75, 255))
    
    # Left horn intact
    d.polygon([(4, 4), (2, 2), (2, 1), (3, 1), (5, 3)], fill=(50, 45, 45, 255))
    # Right horn snapped/broken
    d.rectangle([10, 3, 12, 4], fill=(70, 60, 55, 255))
    
    # Eye slits (dark hollow)
    d.rectangle([4, 8, 6, 9], fill=(20, 15, 15, 255))
    d.rectangle([9, 8, 11, 9], fill=(20, 15, 15, 255))
    
    # Cracks running across right eye
    d.line([(8, 4), (9, 7), (12, 11), (10, 13)], fill=(80, 65, 55, 255))
    # Weathering and dirt
    img.putpixel((4, 12), (180, 165, 145, 255))
    img.putpixel((10, 13), (170, 150, 130, 255))
    
    img.save(os.path.join(ITEM_DIR, "damaged_mask.png"))
    print("Saved damaged_mask.png")

def gen_stained_mask():
    # 16x16 Stained Mask (intact tribal mask with warpaint)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Mask base
    d.polygon([(4, 4), (11, 4), (13, 8), (11, 14), (4, 14), (2, 8)], fill=(240, 238, 230, 255), outline=(70, 65, 60, 255))
    
    # Both curved horns intact
    d.polygon([(4, 4), (2, 2), (1, 1), (3, 0), (5, 3)], fill=(40, 35, 35, 255))
    d.polygon([(11, 4), (13, 2), (14, 1), (12, 0), (10, 3)], fill=(40, 35, 35, 255))
    
    # Red Hilichurl warpaint chevron on forehead
    d.polygon([(7, 4), (8, 4), (10, 6), (5, 6)], fill=(210, 45, 35, 255))
    
    # Eye slits with crimson menacing pupil dots
    d.rectangle([4, 8, 6, 9], fill=(25, 20, 20, 255))
    d.rectangle([9, 8, 11, 9], fill=(25, 20, 20, 255))
    img.putpixel((5, 8), (255, 60, 40, 255))
    img.putpixel((10, 8), (255, 60, 40, 255))
    
    # Mouth slit
    d.line([(6, 12), (9, 12)], fill=(60, 55, 50, 255))
    
    img.save(os.path.join(ITEM_DIR, "stained_mask.png"))
    print("Saved stained_mask.png")

def gen_ominous_mask():
    # 16x16 Ominous Mask (regal Mitachurl chief mask, dark horns, ominous red glow)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Mask base (smooth obsidian-ivory with dark lacquer)
    d.polygon([(4, 3), (11, 3), (14, 8), (11, 15), (4, 15), (1, 8)], fill=(245, 240, 235, 255), outline=(50, 40, 45, 255))
    
    # Large menacing horns with golden banding
    d.polygon([(4, 3), (2, 1), (0, 0), (3, 0), (5, 2)], fill=(30, 25, 25, 255))
    d.polygon([(11, 3), (13, 1), (15, 0), (12, 0), (10, 2)], fill=(30, 25, 25, 255))
    d.line([(2, 1), (3, 1)], fill=(255, 210, 60, 255))
    d.line([(12, 1), (13, 1)], fill=(255, 210, 60, 255))
    
    # Crimson chief sigil
    d.line([(7, 4), (8, 4)], fill=(230, 20, 20, 255))
    d.line([(6, 5), (9, 5)], fill=(255, 40, 40, 255))
    d.line([(7, 6), (8, 6)], fill=(200, 20, 20, 255))
    
    # Piercing glowing eye slits with aura
    d.rectangle([4, 8, 6, 9], fill=(180, 0, 0, 255))
    d.rectangle([9, 8, 11, 9], fill=(180, 0, 0, 255))
    img.putpixel((5, 8), (255, 120, 80, 255))
    img.putpixel((10, 8), (255, 120, 80, 255))
    # Glow bleed
    img.putpixel((3, 8), (120, 0, 0, 180))
    img.putpixel((12, 8), (120, 0, 0, 180))
    
    # Fangs / jaw etchings
    d.line([(5, 13), (6, 14)], fill=(50, 40, 40, 255))
    d.line([(10, 13), (9, 14)], fill=(50, 40, 40, 255))
    
    img.save(os.path.join(ITEM_DIR, "ominous_mask.png"))
    print("Saved ominous_mask.png")

# -------------------------------------------------------------
# 2. SLIME DROPS & CONDENSED RESIN
# -------------------------------------------------------------

def gen_slime_condensate():
    # 16x16 Slime Condensate (crystal clear bouncy hydro droplet)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Droplet teardrop shape
    d.ellipse([3, 5, 12, 14], fill=(70, 215, 235, 230), outline=(30, 140, 180, 255))
    d.polygon([(7, 2), (8, 2), (11, 7), (4, 7)], fill=(90, 230, 245, 240))
    
    # Inner glowing core
    d.ellipse([5, 8, 10, 12], fill=(140, 250, 255, 240))
    
    # Specular shine highlight
    d.ellipse([5, 5, 7, 7], fill=(255, 255, 255, 255))
    img.putpixel((8, 4), (240, 255, 255, 255))
    
    img.save(os.path.join(ITEM_DIR, "slime_condensate.png"))
    print("Saved slime_condensate.png")

def gen_slime_secretions():
    # 16x16 Slime Secretions (thicker vial of pure glowing jelly)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Glass flask contour
    d.rectangle([6, 1, 9, 3], fill=(210, 240, 250, 220), outline=(120, 160, 180, 255))
    d.polygon([(5, 4), (10, 4), (13, 8), (13, 14), (2, 14), (2, 8)], fill=(40, 175, 215, 240), outline=(20, 100, 140, 255))
    
    # Shimmering jelly inside
    d.ellipse([4, 7, 11, 13], fill=(80, 240, 230, 245))
    d.ellipse([6, 9, 9, 11], fill=(200, 255, 250, 255))
    
    # Flask highlights
    d.line([(3, 9), (3, 13)], fill=(255, 255, 255, 220))
    img.putpixel((6, 2), (255, 255, 255, 255))
    
    img.save(os.path.join(ITEM_DIR, "slime_secretions.png"))
    print("Saved slime_secretions.png")

def gen_slime_concentrate():
    # 16x16 Slime Concentrate (pure elemental diamond crystal cluster)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Octahedral crystal shape
    d.polygon([(7, 1), (8, 1), (14, 8), (8, 15), (7, 15), (1, 8)], fill=(30, 180, 225, 250), outline=(15, 95, 150, 255))
    
    # Crystal facets
    d.polygon([(7, 1), (8, 1), (8, 8), (4, 8)], fill=(130, 245, 255, 255))
    d.polygon([(8, 1), (14, 8), (8, 8)], fill=(80, 210, 245, 240))
    d.polygon([(1, 8), (8, 8), (8, 15), (7, 15)], fill=(40, 150, 205, 250))
    d.polygon([(8, 8), (14, 8), (8, 15)], fill=(20, 115, 175, 255))
    
    # Center brilliance star
    d.line([(7, 5), (8, 11)], fill=(255, 255, 255, 255))
    d.line([(5, 8), (10, 8)], fill=(255, 255, 255, 255))
    
    img.save(os.path.join(ITEM_DIR, "slime_concentrate.png"))
    print("Saved slime_concentrate.png")

def gen_condensed_resin():
    # 16x16 Condensed Resin (amber tear drop holding crystal core butterfly)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Amber drop silhouette
    d.ellipse([2, 4, 13, 15], fill=(255, 175, 30, 235), outline=(180, 105, 10, 255))
    d.polygon([(7, 1), (8, 1), (12, 6), (3, 6)], fill=(255, 195, 50, 245))
    
    # Trapped Crystal Core butterfly (radiant cyan wings)
    # Left wing
    d.polygon([(5, 7), (7, 8), (5, 10)], fill=(120, 255, 245, 255))
    # Right wing
    d.polygon([(10, 7), (8, 8), (10, 10)], fill=(120, 255, 245, 255))
    # Butterfly core glow
    img.putpixel((7, 8), (255, 255, 255, 255))
    img.putpixel((8, 8), (255, 255, 255, 255))
    
    # Amber highlight & luster
    d.line([(3, 5), (5, 3)], fill=(255, 240, 160, 255))
    img.putpixel((4, 13), (255, 220, 100, 200))
    
    img.save(os.path.join(ITEM_DIR, "condensed_resin.png"))
    print("Saved condensed_resin.png")

# -------------------------------------------------------------
# 3. MANA MEATS & MANA MILK BUCKET
# -------------------------------------------------------------

def gen_mana_beef():
    # 16x16 Mana Beef (ribeye steak with glowing blue mana marbling)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Steak outline
    d.polygon([(3, 5), (10, 2), (14, 6), (13, 12), (7, 14), (2, 10)], fill=(175, 45, 60, 255), outline=(85, 20, 30, 255))
    
    # Bone on side
    d.ellipse([10, 4, 13, 7], fill=(245, 240, 225, 255), outline=(130, 120, 105, 255))
    
    # Mana-infused fat marbling (glowing cyan/azure lines)
    d.line([(5, 6), (7, 8), (6, 11)], fill=(90, 230, 255, 255), width=1)
    d.line([(8, 4), (10, 8), (9, 12)], fill=(70, 200, 255, 255), width=1)
    img.putpixel((7, 8), (255, 255, 255, 255))
    img.putpixel((10, 8), (220, 255, 255, 255))
    
    img.save(os.path.join(ITEM_DIR, "mana_beef.png"))
    print("Saved mana_beef.png")

def gen_mana_porkchop():
    # 16x16 Mana Porkchop (tender cut with ethereal glow)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    d.polygon([(2, 6), (7, 3), (13, 5), (14, 11), (9, 13), (3, 11)], fill=(225, 115, 125, 255), outline=(115, 45, 55, 255))
    d.ellipse([11, 4, 13, 6], fill=(245, 240, 230, 255))
    
    # Glowing mana ribbons
    d.line([(4, 7), (8, 8), (6, 11)], fill=(110, 240, 255, 255), width=1)
    d.line([(8, 5), (11, 8)], fill=(70, 210, 255, 255), width=1)
    img.putpixel((8, 8), (255, 255, 255, 255))
    
    img.save(os.path.join(ITEM_DIR, "mana_porkchop.png"))
    print("Saved mana_porkchop.png")

def gen_mana_mutton():
    # 16x16 Mana Mutton (chop cutlet with glowing mana veins)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    d.polygon([(3, 4), (8, 2), (13, 7), (12, 13), (5, 12)], fill=(160, 50, 65, 255), outline=(75, 20, 30, 255))
    d.ellipse([1, 3, 4, 6], fill=(240, 235, 220, 255))
    
    # Mana veins
    d.line([(5, 6), (9, 7), (7, 10)], fill=(80, 235, 255, 255), width=1)
    d.line([(9, 7), (11, 10)], fill=(130, 245, 255, 255), width=1)
    img.putpixel((9, 7), (255, 255, 255, 255))
    
    img.save(os.path.join(ITEM_DIR, "mana_mutton.png"))
    print("Saved mana_mutton.png")

def gen_mana_chicken():
    # 16x16 Mana Chicken Drumstick
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Drumstick meat
    d.ellipse([5, 3, 14, 11], fill=(215, 135, 80, 255), outline=(115, 65, 35, 255))
    # Bone
    d.rectangle([2, 9, 6, 12], fill=(245, 240, 225, 255), outline=(130, 120, 105, 255))
    d.ellipse([1, 10, 4, 13], fill=(245, 240, 225, 255))
    
    # Mana glow marbling
    d.line([(7, 5), (11, 7), (9, 9)], fill=(90, 240, 255, 255), width=1)
    img.putpixel((11, 7), (255, 255, 255, 255))
    
    img.save(os.path.join(ITEM_DIR, "mana_chicken_meat.png"))
    print("Saved mana_chicken_meat.png")

def gen_mana_milk():
    # 16x16 Mana Milk Bucket
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Bucket rim & body
    d.polygon([(3, 4), (12, 4), (10, 14), (5, 14)], fill=(160, 170, 180, 255), outline=(80, 85, 95, 255))
    # Bucket handle
    d.line([(2, 5), (4, 1), (11, 1), (13, 5)], fill=(100, 110, 120, 255), width=1)
    
    # Glowing Mana Milk (white with cyan radiant gradient and bubbles)
    d.ellipse([4, 4, 11, 8], fill=(210, 250, 255, 255))
    d.ellipse([5, 5, 10, 7], fill=(255, 255, 255, 255))
    img.putpixel((6, 5), (100, 230, 255, 255))
    img.putpixel((9, 6), (120, 245, 255, 255))
    
    # Starlight glint on bucket
    img.putpixel((4, 8), (255, 255, 255, 240))
    img.putpixel((4, 9), (255, 255, 255, 240))
    
    img.save(os.path.join(ITEM_DIR, "mana_milk_bucket.png"))
    print("Saved mana_milk_bucket.png")

# -------------------------------------------------------------
# 4. WYVERN DRAGON FLIGHT HARNESS
# -------------------------------------------------------------

def gen_wyvern_saddle():
    # 32x32 Wyvern Saddle (dragon flight harness with dragon scales, gold runes, horned cantle)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Dark dragon scale leather base
    d.polygon([(6, 12), (25, 12), (27, 24), (4, 24)], fill=(35, 30, 40, 255), outline=(15, 12, 18, 255))
    
    # Crimson velvet seat cushion
    d.ellipse([9, 13, 22, 21], fill=(165, 25, 35, 255), outline=(95, 15, 20, 255))
    d.ellipse([11, 14, 20, 19], fill=(210, 45, 55, 255))
    
    # Horned pommel & cantle (dragon bone spikes)
    d.polygon([(7, 10), (9, 13), (6, 14)], fill=(225, 215, 195, 255), outline=(120, 110, 95, 255))
    d.polygon([(24, 10), (22, 13), (25, 14)], fill=(225, 215, 195, 255), outline=(120, 110, 95, 255))
    
    # Golden reinforced harness straps & buckles
    d.line([(5, 16), (26, 16)], fill=(255, 210, 50, 255), width=1)
    d.line([(5, 21), (26, 21)], fill=(255, 210, 50, 255), width=1)
    
    # Stirrups hanging down
    d.line([(10, 22), (10, 29)], fill=(60, 55, 65, 255), width=1)
    d.rectangle([8, 28, 12, 30], outline=(255, 210, 50, 255), fill=(40, 35, 45, 255))
    
    d.line([(21, 22), (21, 29)], fill=(60, 55, 65, 255), width=1)
    d.rectangle([19, 28, 23, 30], outline=(255, 210, 50, 255), fill=(40, 35, 45, 255))
    
    # Glowing dragon crest buckle in center
    d.rectangle([14, 15, 17, 18], fill=(255, 235, 110, 255), outline=(175, 125, 20, 255))
    img.putpixel((15, 16), (255, 60, 40, 255))
    
    img.save(os.path.join(ITEM_DIR, "wyvern_saddle.png"))
    print("Saved wyvern_saddle.png")

# -------------------------------------------------------------
# 5. RUNES OVERHAUL
# -------------------------------------------------------------

def gen_runes():
    runes = [
        ("rune_stone_bloodthirst.png", (220, 30, 45), (255, 100, 120), "skull"),
        ("rune_stone_dragon_eyes.png", (255, 170, 20), (255, 240, 90), "eye"),
        ("rune_stone_fourth_wall.png", (40, 220, 230), (180, 255, 255), "crack"),
        ("rune_stone_iron_body.png", (80, 160, 240), (200, 230, 255), "shield"),
        ("rune_stone_rulers_authority.png", (170, 50, 240), (235, 150, 255), "monarch"),
        ("rune_stone_shadow_step.png", (110, 30, 190), (180, 110, 255), "shadow")
    ]
    
    for filename, col_dark, col_light, style in runes:
        img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        
        # Carved Ancient Tablet Base (Hexagonal slate)
        tablet = [(8, 4), (23, 4), (28, 12), (28, 21), (23, 28), (8, 28), (3, 21), (3, 12)]
        d.polygon(tablet, fill=(35, 32, 42, 255), outline=(65, 60, 75, 255))
        d.polygon([(p[0]+1, p[1]+1) for p in tablet[:4]] + [(p[0]-1, p[1]-1) for p in tablet[4:]], outline=(20, 18, 25, 255))
        
        # Tablet stone texture
        for y in range(6, 26):
            for x in range(6, 26):
                if (x + y*3) % 7 == 0:
                    img.putpixel((x, y), (48, 45, 58, 255))
                    
        # Glowing Rune Glyphs
        if style == "skull":
            d.ellipse([11, 9, 20, 17], outline=col_dark, fill=(20, 15, 22, 255))
            d.line([(13, 13), (14, 13)], fill=col_light, width=1)
            d.line([(17, 13), (18, 13)], fill=col_light, width=1)
            d.line([(14, 18), (17, 18)], fill=col_dark, width=2)
            d.line([(15, 19), (16, 23)], fill=col_light, width=1)
        elif style == "eye":
            d.polygon([(8, 16), (15, 10), (23, 16), (15, 22)], outline=col_dark, fill=(20, 15, 22, 255))
            d.ellipse([13, 13, 17, 19], fill=col_dark)
            d.line([(15, 11), (15, 21)], fill=col_light, width=1)
            img.putpixel((15, 16), (255, 255, 255, 255))
        elif style == "crack":
            d.line([(15, 6), (15, 25)], fill=col_dark, width=2)
            d.line([(15, 6), (15, 25)], fill=col_light, width=1)
            d.line([(15, 12), (9, 15)], fill=col_light, width=1)
            d.line([(15, 18), (22, 14)], fill=col_light, width=1)
            d.line([(15, 20), (10, 23)], fill=col_dark, width=1)
        elif style == "shield":
            d.polygon([(11, 9), (20, 9), (21, 16), (15, 23), (10, 16)], outline=col_dark, fill=(20, 15, 22, 255))
            d.line([(15, 11), (15, 21)], fill=col_light, width=1)
            d.line([(12, 15), (19, 15)], fill=col_light, width=1)
        elif style == "monarch":
            # Crown / wings
            d.polygon([(10, 11), (12, 19), (15, 14), (19, 19), (21, 11)], outline=col_dark)
            d.ellipse([13, 14, 18, 19], fill=col_light)
            d.line([(15, 7), (15, 12)], fill=col_light, width=2)
            img.putpixel((15, 16), (255, 255, 255, 255))
        elif style == "shadow":
            d.polygon([(15, 7), (11, 22), (15, 19), (20, 22)], fill=col_dark)
            d.line([(15, 7), (15, 24)], fill=col_light, width=1)
            d.line([(12, 16), (19, 16)], fill=col_light, width=1)
            
        path = os.path.join(ITEM_DIR, filename)
        img.save(path)
        print("Saved", filename)

# -------------------------------------------------------------
# 6. MORA ARMOR OVERHAUL (REX LAPIS / LIYUE ARCHON REGALIA)
# -------------------------------------------------------------

def gen_mora_armor():
    # mora_layer_1 (64x32: Helmet, Chestplate, Boots, Shoulders)
    # mora_layer_2 (64x32: Leggings)
    
    # Layer 1
    l1 = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    d1 = ImageDraw.Draw(l1)
    
    # Helmet area (Head: 0,0 to 32,16)
    # Top crown & face plate
    d1.rectangle([0, 0, 32, 16], fill=(235, 185, 45, 255))
    # Ornate Geo filigree trim on helmet
    d1.rectangle([0, 8, 31, 9], fill=(160, 110, 15, 255))
    d1.rectangle([8, 8, 15, 15], fill=(250, 215, 80, 255)) # Faceplate
    d1.rectangle([9, 10, 11, 11], fill=(20, 20, 25, 255)) # Left eye
    d1.rectangle([13, 10, 15, 11], fill=(20, 20, 25, 255)) # Right eye
    # Rex Lapis dragon horn crest atop helmet
    d1.line([(3, 2), (6, 0)], fill=(255, 240, 140, 255), width=1)
    d1.line([(28, 2), (25, 0)], fill=(255, 240, 140, 255), width=1)
    
    # Chestplate & Arms area (16, 16 to 40, 32)
    # Chestplate (20, 16 to 36, 32)
    d1.rectangle([16, 16, 36, 32], fill=(230, 175, 40, 255))
    # Dragon scale pectoral plate
    d1.polygon([(20, 20), (28, 20), (24, 27)], fill=(255, 220, 85, 255), outline=(150, 100, 10, 255))
    # Geo Archon square emblem in chest
    d1.rectangle([23, 21, 25, 23], fill=(255, 255, 190, 255), outline=(180, 125, 15, 255))
    # Imperial silk dark trim
    d1.rectangle([16, 30, 36, 31], fill=(70, 50, 25, 255))
    
    # Boots area (0, 16 to 16, 32 and 40, 16 to 56, 32)
    d1.rectangle([0, 26, 16, 31], fill=(235, 180, 45, 255))
    d1.rectangle([40, 26, 56, 31], fill=(235, 180, 45, 255))
    # Gold boot kneecap guards
    d1.rectangle([4, 24, 7, 26], fill=(255, 230, 110, 255))
    d1.rectangle([44, 24, 47, 26], fill=(255, 230, 110, 255))
    
    l1.save(os.path.join(ARMOR_DIR, "mora_layer_1.png"))
    print("Saved mora_layer_1.png")
    
    # Layer 2 (Leggings)
    l2 = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    d2 = ImageDraw.Draw(l2)
    # Waist and legs
    d2.rectangle([16, 16, 36, 21], fill=(235, 185, 45, 255)) # Belt
    d2.rectangle([22, 17, 26, 20], fill=(255, 240, 130, 255), outline=(150, 105, 15, 255)) # Buckle
    # Thigh plates & shins
    d2.rectangle([0, 16, 16, 30], fill=(215, 165, 35, 255))
    d2.rectangle([40, 16, 56, 30], fill=(215, 165, 35, 255))
    # Geo golden filigree knee accents
    d2.rectangle([4, 22, 8, 25], fill=(255, 225, 95, 255))
    d2.rectangle([44, 22, 48, 25], fill=(255, 225, 95, 255))
    
    l2.save(os.path.join(ARMOR_DIR, "mora_layer_2.png"))
    print("Saved mora_layer_2.png")
    
    # Armor icons
    def make_icon(shape_type, out_name):
        ico = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        di = ImageDraw.Draw(ico)
        if shape_type == "helmet":
            di.polygon([(4, 2), (11, 2), (13, 6), (13, 13), (2, 13), (2, 6)], fill=(245, 195, 55, 255), outline=(150, 100, 15, 255))
            di.rectangle([5, 7, 10, 9], fill=(20, 20, 25, 255))
            di.rectangle([7, 3, 8, 5], fill=(255, 245, 160, 255))
        elif shape_type == "chest":
            di.polygon([(3, 2), (6, 2), (8, 4), (10, 2), (13, 2), (15, 5), (13, 14), (3, 14), (1, 5)], fill=(245, 195, 55, 255), outline=(150, 100, 15, 255))
            di.polygon([(6, 6), (10, 6), (8, 10)], fill=(255, 235, 110, 255))
            di.rectangle([7, 7, 9, 8], fill=(255, 255, 200, 255))
        elif shape_type == "legs":
            di.polygon([(3, 2), (12, 2), (13, 7), (10, 14), (8, 14), (8, 7), (7, 7), (7, 14), (5, 14), (2, 7)], fill=(235, 185, 45, 255), outline=(150, 100, 15, 255))
            di.rectangle([6, 3, 9, 4], fill=(255, 240, 130, 255))
        elif shape_type == "boots":
            di.polygon([(2, 4), (6, 4), (6, 10), (9, 10), (9, 14), (1, 14), (1, 10), (2, 10)], fill=(245, 195, 55, 255), outline=(150, 100, 15, 255))
            di.polygon([(9, 4), (13, 4), (13, 10), (14, 10), (14, 14), (6, 14), (6, 10), (9, 10)], fill=(245, 195, 55, 255), outline=(150, 100, 15, 255))
        ico.save(os.path.join(ITEM_DIR, out_name))
        print("Saved", out_name)
        
    make_icon("helmet", "mora_helmet.png")
    make_icon("chest", "mora_chestplate.png")
    make_icon("legs", "mora_leggings.png")
    make_icon("boots", "mora_boots.png")

gen_heavy_horn()
gen_damaged_mask()
gen_stained_mask()
gen_ominous_mask()
gen_slime_condensate()
gen_slime_secretions()
gen_slime_concentrate()
gen_condensed_resin()
gen_mana_beef()
gen_mana_porkchop()
gen_mana_mutton()
gen_mana_chicken()
gen_mana_milk()
gen_wyvern_saddle()
gen_runes()
gen_mora_armor()
