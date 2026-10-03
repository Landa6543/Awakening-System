import os
from PIL import Image

def blend(c1, c2, factor):
    return tuple(int(c1[i] + (c2[i] - c1[i]) * factor) for i in range(4))

def create_abyssal_armor():
    # Load original layer 1 & 2 to preserve exact geometry/masks
    l1_path = "src/main/resources/assets/awakening_system/textures/models/armor/abyssal_layer_1.png"
    l2_path = "src/main/resources/assets/awakening_system/textures/models/armor/abyssal_layer_2.png"
    
    l1 = Image.open(l1_path).convert("RGBA")
    l2 = Image.open(l2_path).convert("RGBA")
    
    # Custom color remapping:
    # We turn the flat dark blue wash into high-contrast obsidian plate armor with gold trims and cyan runes
    obsidian_deep = (18, 18, 26, 255)
    obsidian_shadow = (28, 30, 42, 255)
    obsidian_mid = (48, 52, 72, 255)
    obsidian_hi = (78, 86, 116, 255)
    obsidian_shine = (120, 134, 172, 255)
    
    gold_dark = (110, 80, 20, 255)
    gold_mid = (210, 165, 45, 255)
    gold_hi = (255, 220, 95, 255)
    
    rune_cyan = (0, 245, 255, 255)
    rune_purple = (180, 60, 255, 255)
    
    def process_layer(img, is_l1=True):
        w, h = img.size
        out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        for y in range(h):
            for x in range(w):
                p = img.getpixel((x, y))
                if p[3] == 0:
                    continue
                # Calculate brightness
                lum = (p[0] * 0.299 + p[1] * 0.587 + p[2] * 0.114) / 255.0
                
                # Base obsidian shading
                if lum < 0.15:
                    c = obsidian_deep
                elif lum < 0.30:
                    c = obsidian_shadow
                elif lum < 0.50:
                    c = obsidian_mid
                elif lum < 0.70:
                    c = obsidian_hi
                else:
                    c = obsidian_shine
                    
                # Add Gold Trim along key edges
                # For Helmet (y <= 16): visor crest and crown edge
                if is_l1 and y <= 15:
                    if y == 6 or y == 7 or (x in [7, 8, 9] and y in [2, 3]):
                        c = gold_hi if lum > 0.4 else gold_mid
                    elif y == 9 and x in [4, 5, 11, 12]:
                        c = rune_cyan # Glowing eye slit runes!
                
                # For Chestplate front (x between 19 and 29, y between 19 and 31)
                if is_l1 and 19 <= x <= 28 and 19 <= y <= 31:
                    # Gold collar / trim at neck
                    if y == 19 or y == 20:
                        c = gold_hi if lum > 0.4 else gold_mid
                    # Abyssal runic symbol in center of chest (x=23, 24, y=23, 24, 25)
                    elif (x in [23, 24] and 22 <= y <= 26) or (y == 24 and 21 <= x <= 26):
                        c = rune_cyan
                    elif (x in [21, 26] and y in [22, 26]):
                        c = rune_purple
                    # Gold belt at waist
                    elif y == 31:
                        c = gold_mid
                
                # For Shoulders / Pauldrons (x between 40 and 55, y between 16 and 31)
                if is_l1 and 40 <= x <= 55 and 16 <= y <= 31:
                    if y in [16, 17]:
                        c = gold_hi if lum > 0.4 else gold_mid
                    elif y == 20 and x in [43, 44, 51, 52]:
                        c = rune_cyan
                
                # For Leggings (layer 2)
                if not is_l1:
                    # Gold knee guards and waist trim
                    if y in [18, 19] and (x in [1, 2, 6, 7, 9, 10, 14, 15]):
                        c = gold_mid
                    elif y in [24, 25] and (x in [2, 3, 10, 11]):
                        c = rune_cyan # Knee rune
                
                out.putpixel((x, y), c)
        return out

    new_l1 = process_layer(l1, is_l1=True)
    new_l2 = process_layer(l2, is_l1=False)
    
    new_l1.save(l1_path)
    new_l2.save(l2_path)
    print("Saved abyssal armor layers 1 and 2")

def create_abyssal_armor_icons():
    # Icons: 32x32 HD for helmet, chestplate, leggings, boots
    icon_dir = "src/main/resources/assets/awakening_system/textures/item/"
    
    obsidian_deep = (16, 16, 24, 255)
    obsidian_shadow = (30, 32, 46, 255)
    obsidian_mid = (54, 58, 80, 255)
    obsidian_hi = (88, 96, 128, 255)
    obsidian_shine = (140, 152, 190, 255)
    
    gold_dark = (110, 80, 20, 255)
    gold_mid = (215, 170, 45, 255)
    gold_hi = (255, 225, 95, 255)
    gold_shine = (255, 250, 185, 255)
    
    rune_cyan = (0, 245, 255, 255)
    rune_purple = (185, 65, 255, 255)
    rune_white = (225, 255, 255, 255)

    def draw_helmet():
        img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        # Ornate Horned Abyssal Greathelm
        for y in range(4, 28):
            for x in range(4, 28):
                # Crown dome
                dx = abs(x - 15.5)
                dy = abs(y - 14)
                if (dx <= 8 and 8 <= y <= 24) or (dy <= 6 and dx <= 10):
                    # Shading
                    if x < 15:
                        c = obsidian_hi if x < 10 else obsidian_shine
                    else:
                        c = obsidian_shadow if x > 20 else obsidian_mid
                    
                    # Gold crest trim
                    if y in [8, 9] or x in [7, 24]:
                        c = gold_hi if x <= 15 else gold_mid
                    
                    # Visor T-slit (glowing abyssal cyan)
                    if (y == 16 and 9 <= x <= 22) or (x in [15, 16] and 16 <= y <= 22):
                        c = rune_white if x in [15, 16] and y == 16 else rune_cyan
                    elif (y == 15 or y == 17) and 9 <= x <= 22:
                        c = obsidian_deep # Visor rim
                        
                    img.putpixel((x, y), c)
                    
                # Curved Abyssal horns
                if (y <= 12 and (x in [5, 6, 7] or x in [24, 25, 26])):
                    if y >= 6 and (x == 5 or x == 26):
                        img.putpixel((x, y), obsidian_deep)
                    elif y >= 4:
                        img.putpixel((x, y), gold_hi if x < 16 else gold_mid)
                        
        return img

    def draw_chestplate():
        img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        for y in range(4, 28):
            for x in range(4, 28):
                # Neck opening
                if y <= 7 and 12 <= x <= 19:
                    continue
                # Shoulders & body
                if (y <= 12 and (4 <= x <= 12 or 19 <= x <= 27)) or (8 <= y <= 27 and 8 <= x <= 23):
                    # Base metal
                    if x < 16:
                        c = obsidian_hi if x < 12 else obsidian_shine
                    else:
                        c = obsidian_shadow if x > 20 else obsidian_mid
                    
                    # Gold Pauldron trim & collar
                    if y in [5, 6] or y in [26, 27] or x in [8, 23]:
                        c = gold_hi if x <= 16 else gold_mid
                    
                    # Glowing Abyssal Star Sigil in chest
                    dx = abs(x - 15.5)
                    dy = abs(y - 16.5)
                    if (dx + dy <= 3) or (dx == 0 and dy <= 4) or (dy == 0 and dx <= 4):
                        c = rune_white if dx + dy <= 1 else rune_cyan
                    elif dx + dy == 4:
                        c = rune_purple
                        
                    img.putpixel((x, y), c)
        return img

    def draw_leggings():
        img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        for y in range(4, 28):
            for x in range(6, 26):
                # Waistbelt
                if 5 <= y <= 10 and 8 <= x <= 23:
                    c = gold_hi if y in [6, 7] and x <= 16 else (gold_mid if y in [6, 7] else obsidian_mid)
                    if x in [15, 16] and y in [7, 8]:
                        c = rune_cyan # Belt buckle gem
                    img.putpixel((x, y), c)
                # Two legs
                elif 10 <= y <= 27:
                    # Leg split
                    if 14 <= x <= 17 and y >= 12:
                        continue
                    if (8 <= x <= 14) or (17 <= x <= 23):
                        c = obsidian_hi if x in [8, 9, 17, 18] else (obsidian_shadow if x in [13, 14, 22, 23] else obsidian_mid)
                        # Knee plates with gold & cyan runes
                        if 17 <= y <= 19 and (x in [10, 11, 19, 20]):
                            c = rune_cyan if y == 18 else gold_hi
                        elif y in [26, 27]:
                            c = gold_mid
                        img.putpixel((x, y), c)
        return img

    def draw_boots():
        img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        for y in range(12, 28):
            for x in range(5, 27):
                # Left boot: x between 7 and 14, right boot: 17 and 24
                if (7 <= x <= 14 and y <= 23) or (5 <= x <= 14 and y >= 24) or \
                   (17 <= x <= 24 and y <= 23) or (17 <= x <= 26 and y >= 24):
                    c = obsidian_hi if x in [7, 8, 17, 18] else (obsidian_shadow if x in [13, 14, 23, 24] else obsidian_mid)
                    # Gold cuffs at top
                    if y in [13, 14]:
                        c = gold_hi if x in [7, 8, 17, 18] else gold_mid
                    # Gold toe caps
                    if y in [25, 26] and (x in [5, 6, 25, 26]):
                        c = gold_hi
                    # Shin cyan rune
                    if y in [18, 19] and (x in [10, 11, 20, 21]):
                        c = rune_cyan
                    img.putpixel((x, y), c)
        return img

    h = draw_helmet()
    c = draw_chestplate()
    l = draw_leggings()
    b = draw_boots()
    
    h.save(os.path.join(icon_dir, "abyssal_helmet.png"))
    c.save(os.path.join(icon_dir, "abyssal_chestplate.png"))
    l.save(os.path.join(icon_dir, "abyssal_leggings.png"))
    b.save(os.path.join(icon_dir, "abyssal_boots.png"))
    print("Saved 4 HD abyssal armor item icons")

def create_abyssal_blocks():
    block_dir = "src/main/resources/assets/awakening_system/textures/block/"
    
    deepslate_base = (34, 34, 40, 255)
    deepslate_dark = (22, 22, 26, 255)
    deepslate_hi = (55, 55, 65, 255)
    
    obsidian_base = (24, 24, 34, 255)
    obsidian_hi = (45, 48, 68, 255)
    obsidian_shadow = (18, 18, 26, 255)
    obsidian_dark = (14, 14, 20, 255)
    
    gold_accent = (220, 170, 50, 255)
    gold_hi = (255, 225, 100, 255)
    
    crystal_cyan = (0, 240, 255, 255)
    crystal_purple = (170, 50, 240, 255)
    crystal_core = (235, 255, 255, 255)

    # 1. Abyssal Ore (16x16)
    ore = Image.new("RGBA", (16, 16), deepslate_base)
    # Deepslate texture noise
    for y in range(16):
        for x in range(16):
            if (x + y * 3) % 4 == 0:
                ore.putpixel((x, y), deepslate_dark)
            elif (x * 2 + y) % 5 == 0:
                ore.putpixel((x, y), deepslate_hi)
                
    # Rich glowing abyssal crystal clusters embedded in the rock
    gem_clusters = [
        # Cluster 1 (center-left)
        (3, 4, crystal_purple), (4, 4, crystal_cyan), (5, 4, crystal_core), (4, 5, crystal_purple),
        (5, 5, crystal_cyan), (6, 5, crystal_core), (5, 6, crystal_purple),
        # Cluster 2 (bottom-right)
        (10, 10, crystal_cyan), (11, 10, crystal_core), (12, 10, crystal_purple),
        (10, 11, crystal_purple), (11, 11, crystal_cyan), (12, 11, crystal_core),
        (11, 12, crystal_cyan),
        # Small shards
        (9, 3, crystal_cyan), (10, 4, crystal_core), (2, 11, crystal_cyan), (3, 12, crystal_purple)
    ]
    for gx, gy, gc in gem_clusters:
        ore.putpixel((gx, gy), gc)
        
    ore.save(os.path.join(block_dir, "abyssal_ore.png"))

    # 2. Abyssal Block (16x16)
    # Chiseled Dark Obsidian with Inlaid Gold Frame and Radiant Abyssal Star Core
    blk = Image.new("RGBA", (16, 16), obsidian_base)
    for y in range(16):
        for x in range(16):
            # Outer beveled border
            if x == 0 or y == 0:
                blk.putpixel((x, y), obsidian_hi)
            elif x == 15 or y == 15:
                blk.putpixel((x, y), obsidian_dark)
            elif x == 1 or y == 1:
                blk.putpixel((x, y), gold_accent)
            elif x == 14 or y == 14:
                blk.putpixel((x, y), gold_accent)
            else:
                blk.putpixel((x, y), obsidian_base)
                
    # Center Carved Sigil
    sigil = [
        (7, 4, gold_hi), (8, 4, gold_hi),
        (7, 11, gold_hi), (8, 11, gold_hi),
        (4, 7, gold_hi), (4, 8, gold_hi),
        (11, 7, gold_hi), (11, 8, gold_hi),
        (6, 6, crystal_purple), (9, 6, crystal_purple),
        (6, 9, crystal_purple), (9, 9, crystal_purple),
        (7, 7, crystal_cyan), (8, 7, crystal_core),
        (7, 8, crystal_core), (8, 8, crystal_cyan)
    ]
    for sx, sy, sc in sigil:
        blk.putpixel((sx, sy), sc)
        
    blk.save(os.path.join(block_dir, "abyssal_block.png"))

    # 3. Abyssal Furnace Textures
    # Front Off, Front On, Side, Top
    side = Image.new("RGBA", (16, 16), obsidian_base)
    for y in range(16):
        for x in range(16):
            if x == 0 or y == 0:
                side.putpixel((x, y), obsidian_hi)
            elif x == 15 or y == 15:
                side.putpixel((x, y), obsidian_dark)
            elif (x + y * 2) % 3 == 0:
                side.putpixel((x, y), obsidian_shadow)
            else:
                side.putpixel((x, y), obsidian_base)
    side.save(os.path.join(block_dir, "abyssal_furnace_side.png"))
    side.save(os.path.join(block_dir, "abyssal_furnace_top.png"))

    # Front Off
    front_off = side.copy()
    # Draw furnace door and vents
    for y in range(7, 13):
        for x in range(4, 12):
            front_off.putpixel((x, y), (12, 12, 16, 255))
    for x in range(5, 11):
        front_off.putpixel((x, 7), gold_accent)
        front_off.putpixel((x, 12), gold_accent)
    front_off.save(os.path.join(block_dir, "abyssal_furnace_front.png"))

    # Front On (glowing eerie violet-cyan flame inside)
    front_on = front_off.copy()
    fire_pixels = [
        (5, 11, crystal_purple), (6, 11, crystal_cyan), (7, 11, crystal_core),
        (8, 11, crystal_core), (9, 11, crystal_cyan), (10, 11, crystal_purple),
        (6, 10, crystal_purple), (7, 10, crystal_cyan), (8, 10, crystal_core), (9, 10, crystal_cyan),
        (7, 9, crystal_purple), (8, 9, crystal_cyan),
        (7, 8, crystal_core)
    ]
    for fx, fy, fc in fire_pixels:
        front_on.putpixel((fx, fy), fc)
    front_on.save(os.path.join(block_dir, "abyssal_furnace_front_on.png"))

    print("Saved all abyssal block textures")

if __name__ == "__main__":
    create_abyssal_armor()
    create_abyssal_armor_icons()
    create_abyssal_blocks()
