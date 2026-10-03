import os
import math
from PIL import Image, ImageDraw

def create_empty_canvas():
    return Image.new("RGBA", (32, 32), (0, 0, 0, 0))

def put_px(img, x, y, color):
    if 0 <= x < 32 and 0 <= y < 32:
        img.putpixel((int(x), int(y)), color)

def blend(c1, c2, factor):
    """Linear interpolation between two RGBA colors"""
    r = int(c1[0] + (c2[0] - c1[0]) * factor)
    g = int(c1[1] + (c2[1] - c1[1]) * factor)
    b = int(c1[2] + (c2[2] - c1[2]) * factor)
    a = int(c1[3] + (c2[3] - c1[3]) * factor)
    return (r, g, b, a)

def draw_diagonal_shaft(img, start_xy, end_xy, palette, width=3):
    """Draws a sturdy 3-4px thick diagonal shaft from start to end with shadow, mid, and highlight"""
    x0, y0 = start_xy
    x1, y1 = end_xy
    steps = int(max(abs(x1 - x0), abs(y1 - y0)) * 2.5)
    for s in range(steps + 1):
        t = s / steps
        cx = x0 + (x1 - x0) * t
        cy = y0 + (y1 - y0) * t
        
        # Solid 3-pixel thickness perpendicular to diagonal (dx = -dy direction)
        # Highlight edge (top-left)
        put_px(img, cx - 1, cy, palette['hi'])
        put_px(img, cx, cy - 1, palette['hi'])
        # Center core
        put_px(img, cx, cy, palette['mid'])
        # Shadow edge (bottom-right)
        put_px(img, cx + 1, cy, palette['shadow'])
        put_px(img, cx, cy + 1, palette['shadow'])
        
        # Outer dark outlines
        put_px(img, cx - 1, cy - 1, palette['dark'])
        put_px(img, cx + 1, cy + 1, palette['dark'])

def draw_bolster(img, x, y, palette, size=2):
    """Draws an ornate collar/bolster ring around (x, y)"""
    for dx in range(-size, size + 1):
        for dy in range(-size, size + 1):
            if abs(dx) + abs(dy) <= size + 1:
                if dx + dy < 0:
                    put_px(img, x + dx, y + dy, palette['hi'])
                elif dx + dy == 0:
                    put_px(img, x + dx, y + dy, palette['mid'])
                else:
                    put_px(img, x + dx, y + dy, palette['shadow'])

# --- 1. OVERWORLD DUNGEON KEY (Antique Bronze & Ancient Emerald) ---
def gen_overworld_dungeon_key():
    img = create_empty_canvas()
    
    # Palette
    bronze_dark = (32, 24, 18, 255)
    bronze_shadow = (84, 58, 38, 255)
    bronze_mid = (168, 122, 68, 255)
    bronze_hi = (228, 184, 112, 255)
    bronze_shine = (255, 235, 175, 255)
    
    gem_dark = (12, 55, 28, 255)
    gem_mid = (26, 148, 70, 255)
    gem_hi = (60, 220, 115, 255)
    gem_shine = (190, 255, 215, 255)

    # Bow: Ornate cross-clover medallion centered at (8, 8)
    bow_radius = 5.5
    for y in range(2, 15):
        for x in range(2, 15):
            dist = math.hypot(x - 8, y - 8)
            # Four decorative corner lobes
            lobe_dist = min(
                math.hypot(x - 8, y - 4),
                math.hypot(x - 8, y - 12),
                math.hypot(x - 4, y - 8),
                math.hypot(x - 12, y - 8)
            )
            if dist <= 5.8 or lobe_dist <= 2.8:
                if dist < 2.2:
                    # Central Emerald Gem
                    if x == 7 and y == 7:
                        put_px(img, x, y, gem_shine)
                    elif x <= 8 and y <= 8:
                        put_px(img, x, y, gem_hi)
                    else:
                        put_px(img, x, y, gem_mid)
                elif dist < 3.2:
                    put_px(img, x, y, bronze_dark) # gem bezel
                elif dist > 5.2 and lobe_dist > 2.4:
                    put_px(img, x, y, bronze_dark) # outer ring outline
                elif x < y:
                    put_px(img, x, y, bronze_hi if (x + y) % 2 == 0 else bronze_shine) # top-left highlight
                else:
                    put_px(img, x, y, bronze_shadow if dist > 4.5 else bronze_mid)

    # Shaft: from (12, 12) to (24, 24)
    shaft_palette = {
        'hi': bronze_hi,
        'mid': bronze_mid,
        'shadow': bronze_shadow,
        'dark': bronze_dark
    }
    draw_diagonal_shaft(img, (11, 11), (23, 23), shaft_palette)
    
    # Ornate collar ring at (12, 12)
    for dx in [-1, 0, 1]:
        for dy in [-1, 0, 1]:
            if dx == -dy:
                put_px(img, 12 + dx, 12 + dy, bronze_shine)
                put_px(img, 12 + dx + 1, 12 + dy, bronze_shadow)

    # Teeth / Wards: at (21, 25) to (27, 23)
    # Ancient dungeon bit with multi-ward cutouts
    wards = [
        # Ward 1 (shorter)
        (20, 22), (21, 23), (22, 24),
        (21, 21), (22, 22), (23, 23),
        # Main bit body
        (22, 20), (23, 21), (24, 22),
        (23, 19), (24, 20), (25, 21),
        (24, 18), (25, 19), (26, 20),
        # Teeth cuts
        (25, 17), (26, 18), # tooth 1
        (27, 19), (28, 20), # tooth 2
        (26, 22), (27, 21), # tooth tip
        (24, 24), (25, 25), (26, 24) # key tip cap
    ]
    for wx, wy in wards:
        put_px(img, wx, wy, bronze_mid)
        put_px(img, wx + 1, wy, bronze_dark)
        put_px(img, wx - 1, wy, bronze_hi)
        put_px(img, wx, wy + 1, bronze_shadow)
    
    # Gem shine specular glint
    put_px(img, 6, 6, (255, 255, 255, 220))
    put_px(img, 7, 7, gem_shine)
    
    return img

# --- 2. NETHER FLAME KEY (Obsidian, Blazing Magma & Demon Horns) ---
def gen_nether_flame_key():
    img = create_empty_canvas()
    
    netherite_dark = (24, 18, 22, 255)
    netherite_shadow = (48, 38, 44, 255)
    netherite_mid = (82, 68, 76, 255)
    netherite_hi = (130, 110, 120, 255)
    
    fire_deep = (180, 20, 10, 255)
    fire_mid = (255, 80, 10, 255)
    fire_hi = (255, 190, 20, 255)
    fire_core = (255, 255, 180, 255)

    # Horned demonic skull bow around (8, 8)
    for y in range(1, 15):
        for x in range(1, 15):
            dist = math.hypot(x - 8, y - 8)
            # Horn curve: extending to (3, 2) and (13, 2)
            horn_left = math.hypot(x - 4, y - 3)
            horn_right = math.hypot(x - 12, y - 3)
            
            if dist <= 5.5 or (y <= 5 and (horn_left <= 2.5 or horn_right <= 2.5)):
                if dist < 2.5:
                    # Pulsing Magma Core
                    if x == 8 and y == 8:
                        put_px(img, x, y, fire_core)
                    elif dist < 1.6:
                        put_px(img, x, y, fire_hi)
                    else:
                        put_px(img, x, y, fire_mid)
                elif dist < 3.2:
                    put_px(img, x, y, fire_deep) # molten rim
                elif dist > 4.8 and horn_left > 1.8 and horn_right > 1.8:
                    put_px(img, x, y, netherite_dark) # outer frame
                elif y < x:
                    put_px(img, x, y, netherite_hi)
                else:
                    put_px(img, x, y, netherite_shadow)

    # Fiery ember aura around core
    for ax, ay, c in [(7, 5, fire_mid), (9, 5, fire_hi), (5, 8, fire_deep), (11, 8, fire_mid), (8, 11, fire_deep)]:
        put_px(img, ax, ay, c)

    # Netherite Shaft with molten vein
    shaft_pal = {'hi': netherite_hi, 'mid': netherite_mid, 'shadow': netherite_shadow, 'dark': netherite_dark}
    draw_diagonal_shaft(img, (11, 11), (23, 23), shaft_pal)
    # Molten vein in the middle of shaft
    for t in [13, 15, 17, 19, 21]:
        put_px(img, t, t, fire_mid if t % 2 == 0 else fire_hi)
        put_px(img, t + 1, t, fire_deep)

    # Serrated Flame Wards
    flame_teeth = [
        (22, 19), (23, 18), (24, 17), # Top flame fang
        (24, 19), (25, 18), (26, 17), # Second fang
        (23, 21), (24, 20), (25, 19),
        (24, 22), (25, 21), (26, 20),
        (25, 23), (26, 22), (27, 21), # Sharp bit point
        (24, 24), (25, 25), (26, 26)  # Molten tip
    ]
    for fx, fy in flame_teeth:
        put_px(img, fx, fy, fire_hi)
        put_px(img, fx + 1, fy, fire_mid)
        put_px(img, fx, fy + 1, fire_deep)
        put_px(img, fx - 1, fy - 1, netherite_dark)

    # Small embers drifting
    put_px(img, 3, 1, fire_hi)
    put_px(img, 13, 1, fire_hi)
    put_px(img, 15, 6, fire_mid)
    put_px(img, 27, 15, fire_mid)
    
    return img

# --- 3. END VOID KEY (Purpur, Cosmic Void, Eye of Ender) ---
def gen_end_void_key():
    img = create_empty_canvas()
    
    purpur_dark = (22, 10, 34, 255)
    purpur_shadow = (54, 26, 78, 255)
    purpur_mid = (112, 58, 158, 255)
    purpur_hi = (182, 112, 238, 255)
    purpur_shine = (235, 190, 255, 255)
    
    ender_teal_dark = (6, 38, 38, 255)
    ender_teal_mid = (18, 120, 110, 255)
    ender_teal_bright = (48, 220, 190, 255)
    ender_core = (200, 255, 245, 255)

    # Star-shaped cosmic void bow
    for y in range(1, 15):
        for x in range(1, 15):
            dist = math.hypot(x - 8, y - 8)
            # 4 diagonal cosmic crystal spikes
            diag_dist = min(
                math.hypot(x - 3, y - 3),
                math.hypot(x - 13, y - 3),
                math.hypot(x - 3, y - 13),
                math.hypot(x - 13, y - 13)
            )
            if dist <= 5.4 or diag_dist <= 2.2:
                if dist < 2.4:
                    # Eye of Ender pupil & iris
                    if x == 8 and (y == 7 or y == 8):
                        put_px(img, x, y, (12, 10, 18, 255)) # vertical slit pupil
                    elif dist < 1.6:
                        put_px(img, x, y, ender_teal_bright)
                    else:
                        put_px(img, x, y, ender_teal_mid)
                elif dist < 3.2:
                    put_px(img, x, y, ender_teal_dark) # iris boundary
                elif dist > 4.6 and diag_dist > 1.8:
                    put_px(img, x, y, purpur_dark)
                elif x < y:
                    put_px(img, x, y, purpur_hi if dist > 3.8 else purpur_shine)
                else:
                    put_px(img, x, y, purpur_shadow)

    # Eye specular glint
    put_px(img, 7, 7, ender_core)

    # Void crystal shaft
    void_pal = {'hi': purpur_hi, 'mid': purpur_mid, 'shadow': purpur_shadow, 'dark': purpur_dark}
    draw_diagonal_shaft(img, (11, 11), (23, 23), void_pal)
    # Starlight in shaft
    for st in [14, 18, 21]:
        put_px(img, st, st, ender_teal_bright)

    # Void fractal wards (geometric crystal teeth)
    end_teeth = [
        (21, 19), (22, 18), (23, 17), (24, 16),
        (23, 20), (24, 19), (25, 18),
        (24, 21), (25, 20), (26, 19), (27, 18),
        (24, 23), (25, 22), (26, 21),
        (24, 24), (25, 25), (26, 26), (27, 27) # Cosmic tip
    ]
    for ex, ey in end_teeth:
        put_px(img, ex, ey, purpur_hi)
        put_px(img, ex + 1, ey, purpur_mid)
        put_px(img, ex, ey + 1, purpur_dark)
        put_px(img, ex - 1, ey - 1, purpur_shine)

    # Cosmic floating particles
    put_px(img, 2, 2, ender_teal_bright)
    put_px(img, 14, 2, ender_teal_bright)
    put_px(img, 2, 14, ender_teal_bright)
    put_px(img, 28, 17, purpur_shine)

    return img

# --- 4. TEYVAT CELESTIAL SHRINE KEY (Gilded Wings, Celestial Star Diamond) ---
def gen_teyvat_shrine_key():
    img = create_empty_canvas()
    
    gold_dark = (60, 42, 12, 255)
    gold_shadow = (140, 95, 26, 255)
    gold_mid = (220, 165, 45, 255)
    gold_hi = (255, 218, 100, 255)
    gold_shine = (255, 250, 195, 255)
    
    star_dark = (30, 80, 130, 255)
    star_mid = (70, 170, 240, 255)
    star_hi = (160, 230, 255, 255)
    star_core = (255, 255, 255, 255)

    # Celestial Genshin Crown & Winged Arch at (8, 8)
    for y in range(1, 15):
        for x in range(1, 15):
            dist = math.hypot(x - 8, y - 8)
            # Archon Wing arcs: flare out to top-left and bottom-left
            wing_dist = min(math.hypot(x - 4, y - 5), math.hypot(x - 5, y - 11), math.hypot(x - 11, y - 4))
            if dist <= 5.8 or wing_dist <= 2.6:
                # 4-pointed Primogem diamond star in the center
                dx = abs(x - 8)
                dy = abs(y - 8)
                is_star = (dx + dy <= 3) or (dx <= 1 and dy <= 4) or (dy <= 1 and dx <= 4)
                if is_star:
                    if dx == 0 and dy == 0:
                        put_px(img, x, y, star_core)
                    elif dx + dy <= 2:
                        put_px(img, x, y, star_hi)
                    else:
                        put_px(img, x, y, star_mid)
                elif dist < 3.8:
                    put_px(img, x, y, gold_dark) # star mounting
                elif dist > 5.2 and wing_dist > 2.0:
                    put_px(img, x, y, gold_dark)
                elif x <= y:
                    put_px(img, x, y, gold_hi if (x+y)%2 == 0 else gold_shine)
                else:
                    put_px(img, x, y, gold_shadow if dist > 4.6 else gold_mid)

    # Fluted golden shaft with celestial silver inlays
    draw_diagonal_shaft(img, (11, 11), (23, 23), {'hi': gold_shine, 'mid': gold_mid, 'shadow': gold_shadow, 'dark': gold_dark})
    for c in [13, 17, 21]:
        put_px(img, c, c, star_hi)

    # Shrine of depths teeth (Gilded arch portal shape)
    shrine_wards = [
        (21, 19), (22, 18), (23, 17), (24, 16), (25, 15),
        (23, 20), (24, 19), (25, 18), (26, 17),
        (24, 21), (25, 20), (26, 19),
        (23, 22), (24, 23), (25, 22), (26, 21), (27, 20),
        (25, 24), (26, 23), (27, 22),
        (24, 24), (25, 25), (26, 26), (27, 27)
    ]
    for sx, sy in shrine_wards:
        put_px(img, sx, sy, gold_mid)
        put_px(img, sx - 1, sy, gold_hi)
        put_px(img, sx, sy - 1, gold_shine)
        put_px(img, sx + 1, sy, gold_shadow)
        put_px(img, sx, sy + 1, gold_dark)

    # Celestial star glints
    put_px(img, 7, 7, star_core)
    put_px(img, 8, 8, star_core)
    put_px(img, 1, 4, gold_shine)
    put_px(img, 11, 2, gold_shine)

    return img

# --- 5. ISEKAI MANA KEY (Arcane Silver, Faceted Cyan Mana Crystal) ---
def gen_isekai_mana_key():
    img = create_empty_canvas()
    
    silver_dark = (28, 36, 48, 255)
    silver_shadow = (70, 85, 105, 255)
    silver_mid = (140, 165, 190, 255)
    silver_hi = (210, 230, 245, 255)
    silver_shine = (255, 255, 255, 255)
    
    mana_dark = (0, 75, 115, 255)
    mana_mid = (0, 170, 230, 255)
    mana_bright = (50, 225, 255, 255)
    mana_core = (220, 250, 255, 255)

    # Arcane ring bow with orbiting mana shards
    for y in range(1, 15):
        for x in range(1, 15):
            dist = math.hypot(x - 8, y - 8)
            # Floating crystal orbit nodes
            node_dist = min(
                math.hypot(x - 3, y - 7),
                math.hypot(x - 13, y - 7),
                math.hypot(x - 7, y - 3),
                math.hypot(x - 7, y - 13)
            )
            if dist <= 5.6 or node_dist <= 2.0:
                if dist < 2.6:
                    # Faceted cyan mana crystal
                    if x <= 8 and y <= 8:
                        put_px(img, x, y, mana_core if x == 7 and y == 7 else mana_bright)
                    else:
                        put_px(img, x, y, mana_mid if dist < 2.0 else mana_dark)
                elif dist < 3.4:
                    put_px(img, x, y, silver_dark)
                elif dist > 5.0 and node_dist > 1.6:
                    put_px(img, x, y, silver_dark)
                elif x <= y:
                    put_px(img, x, y, silver_shine if dist > 4.2 else silver_hi)
                else:
                    put_px(img, x, y, silver_shadow)

    # Arcane Silver Shaft with glowing mana runes
    draw_diagonal_shaft(img, (11, 11), (23, 23), {'hi': silver_hi, 'mid': silver_mid, 'shadow': silver_shadow, 'dark': silver_dark})
    for r in [13, 16, 19, 22]:
        put_px(img, r, r, mana_bright)
        put_px(img, r - 1, r, mana_core)

    # Arcane Geometric Wards
    mana_teeth = [
        (21, 18), (22, 17), (23, 16),
        (22, 19), (23, 18), (24, 17),
        (24, 20), (25, 19), (26, 18),
        (23, 22), (24, 21), (25, 20),
        (25, 22), (26, 21), (27, 20),
        (24, 24), (25, 25), (26, 26)
    ]
    for mx, my in mana_teeth:
        put_px(img, mx, my, mana_bright)
        put_px(img, mx + 1, my, mana_mid)
        put_px(img, mx - 1, my, silver_shine)
        put_px(img, mx, my + 1, silver_shadow)
        put_px(img, mx, my - 1, silver_hi)

    # Pulsing mana spark particles
    put_px(img, 2, 7, mana_bright)
    put_px(img, 13, 7, mana_bright)
    put_px(img, 7, 2, mana_bright)
    put_px(img, 27, 18, mana_core)

    return img

# --- 6. MURIM DAO KEY (Imperial Jade, Yin-Yang Taiji, Gold & Crimson Silk Tassel) ---
def gen_murim_dao_key():
    img = create_empty_canvas()
    
    # White Mutton-fat Jade & Emerald Jade
    jade_white = (242, 248, 242, 255)
    jade_shadow = (175, 195, 180, 255)
    jade_dark = (60, 80, 70, 255)
    
    jade_green_hi = (110, 230, 160, 255)
    jade_green_mid = (45, 160, 95, 255)
    jade_green_dark = (18, 75, 42, 255)
    
    gold_hi = (255, 220, 110, 255)
    gold_mid = (210, 160, 40, 255)
    gold_dark = (110, 75, 20, 255)
    
    silk_crimson = (220, 30, 45, 255)
    silk_shadow = (130, 15, 25, 255)
    silk_knot = (255, 180, 40, 255)

    # Bow: Circular Jade Ring with Taiji Yin-Yang emblem in center
    for y in range(2, 15):
        for x in range(2, 15):
            dist = math.hypot(x - 8, y - 8)
            if dist <= 5.8:
                if dist < 2.8:
                    # Yin-Yang core (Black/Dark Jade & White Jade)
                    # S-curve dividing line:
                    is_yin = (x < 8 and y <= 8) or (y > 8 and math.hypot(x - 8, y - 9.5) < 1.4) or (x <= 7 and y > 8 and math.hypot(x - 8, y - 6.5) > 1.4)
                    # Dot eyes
                    if x == 8 and y == 6: # white eye in black
                        put_px(img, x, y, jade_white)
                    elif x == 8 and y == 10: # black eye in white
                        put_px(img, x, y, jade_dark)
                    elif is_yin:
                        put_px(img, x, y, jade_dark)
                    else:
                        put_px(img, x, y, jade_white)
                elif dist < 3.5:
                    put_px(img, x, y, gold_mid) # gold bezel
                elif dist > 5.2:
                    put_px(img, x, y, jade_dark) # outer ring border
                elif x <= y:
                    put_px(img, x, y, jade_green_hi if dist > 4.2 else jade_white)
                else:
                    put_px(img, x, y, jade_green_mid)

    # Crimson Silk Tassel hanging from bottom-left of bow (around (4, 12) down to (1, 19))
    tassel_pixels = [
        (4, 11, gold_hi), (3, 12, silk_knot), (4, 12, silk_knot), # Golden mystic knot
        (3, 13, silk_crimson), (4, 13, silk_crimson),
        (2, 14, silk_crimson), (3, 14, silk_shadow), (4, 14, silk_crimson),
        (2, 15, silk_crimson), (3, 15, silk_crimson), (4, 15, silk_shadow),
        (1, 16, silk_crimson), (2, 16, silk_crimson), (3, 16, silk_crimson),
        (1, 17, silk_shadow), (2, 17, silk_crimson),
        (1, 18, silk_crimson)
    ]
    for tx, ty, tc in tassel_pixels:
        put_px(img, tx, ty, tc)

    # Fluted Jade Shaft wrapped in Gold Dragon Filigree
    draw_diagonal_shaft(img, (11, 11), (23, 23), {'hi': jade_green_hi, 'mid': jade_green_mid, 'shadow': jade_green_dark, 'dark': jade_dark})
    # Gold dragon ribbon spiraling the shaft
    for sp in [(12, 11), (13, 13), (16, 15), (17, 17), (20, 19), (21, 21)]:
        put_px(img, sp[0], sp[1], gold_hi)
        put_px(img, sp[0] + 1, sp[1], gold_mid)

    # Ancient Seal-Script Jade Wards
    murim_teeth = [
        (21, 19), (22, 18), (23, 17),
        (22, 21), (23, 20), (24, 19),
        (24, 17), (25, 16),
        (24, 21), (25, 20), (26, 19),
        (26, 17), (27, 16),
        (25, 23), (26, 22), (27, 21),
        (24, 24), (25, 25), (26, 26)
    ]
    for mx, my in murim_teeth:
        put_px(img, mx, my, jade_green_hi)
        put_px(img, mx + 1, my, jade_green_mid)
        put_px(img, mx, my + 1, gold_mid)
        put_px(img, mx - 1, my - 1, jade_white)

    return img

# --- 7. MASTER DUNGEON KEY (dungeon_key.png) ---
def gen_master_dungeon_key():
    # Antique Ornate Brass & Forged Iron with Skull / Keyhole emblem
    img = create_empty_canvas()
    
    iron_dark = (28, 28, 32, 255)
    iron_shadow = (55, 55, 65, 255)
    iron_mid = (110, 115, 130, 255)
    iron_hi = (180, 185, 200, 255)
    iron_shine = (240, 245, 255, 255)
    
    brass_mid = (195, 150, 45, 255)
    brass_hi = (245, 205, 90, 255)

    # Large Trefoil / Gothic Arch Bow
    for y in range(2, 15):
        for x in range(2, 15):
            dist = math.hypot(x - 8, y - 8)
            # Trefoil 3-lobed pattern
            lobe_dist = min(math.hypot(x - 5, y - 5), math.hypot(x - 11, y - 5), math.hypot(x - 8, y - 11))
            if dist <= 5.8 or lobe_dist <= 2.8:
                if dist < 2.0:
                    put_px(img, x, y, (0, 0, 0, 0)) # hollow keyhole center!
                elif dist < 3.2:
                    put_px(img, x, y, brass_hi if x <= y else brass_mid) # brass bezel
                elif dist > 5.2 and lobe_dist > 2.4:
                    put_px(img, x, y, iron_dark)
                elif x <= y:
                    put_px(img, x, y, iron_shine if dist > 4.5 else iron_hi)
                else:
                    put_px(img, x, y, iron_shadow if dist > 4.5 else iron_mid)

    # Shaft with brass collar
    draw_diagonal_shaft(img, (11, 11), (23, 23), {'hi': iron_hi, 'mid': iron_mid, 'shadow': iron_shadow, 'dark': iron_dark})
    put_px(img, 12, 11, brass_hi)
    put_px(img, 11, 12, brass_mid)
    put_px(img, 22, 21, brass_hi)
    put_px(img, 21, 22, brass_mid)

    # Heavy Gothic Stepped Wards
    dungeon_wards = [
        (21, 19), (22, 18), (23, 17), (24, 16),
        (22, 21), (23, 20), (24, 19), (25, 18),
        (24, 21), (25, 20), (26, 19),
        (25, 23), (26, 22), (27, 21), (28, 20),
        (24, 24), (25, 25), (26, 26)
    ]
    for dx, dy in dungeon_wards:
        put_px(img, dx, dy, iron_mid)
        put_px(img, dx - 1, dy, iron_hi)
        put_px(img, dx, dy - 1, iron_shine)
        put_px(img, dx + 1, dy, iron_dark)
        put_px(img, dx, dy + 1, iron_shadow)

    return img

# --- 8. JADE ANCESTRAL KEY (jade_ancestral_key.png) ---
def gen_jade_ancestral_key():
    img = create_empty_canvas()
    
    jade_light = (160, 245, 195, 255)
    jade_mid = (65, 185, 120, 255)
    jade_dark = (25, 95, 55, 255)
    jade_outline = (12, 45, 25, 255)
    gold = (245, 200, 75, 255)
    gold_dark = (120, 85, 20, 255)

    # Bi-disc circular bow with carved dragon crest
    for y in range(2, 15):
        for x in range(2, 15):
            dist = math.hypot(x - 8, y - 8)
            if dist <= 5.8:
                if dist < 2.0:
                    put_px(img, x, y, gold) # Golden center core
                elif dist < 3.0:
                    put_px(img, x, y, gold_dark)
                elif dist > 5.2:
                    put_px(img, x, y, jade_outline)
                elif x <= y:
                    put_px(img, x, y, jade_light)
                else:
                    put_px(img, x, y, jade_mid if dist < 4.5 else jade_dark)

    # Carved dragon horn at top of key
    for px, py in [(6, 2), (7, 1), (8, 1), (9, 2), (10, 3)]:
        put_px(img, px, py, gold)

    draw_diagonal_shaft(img, (11, 11), (23, 23), {'hi': jade_light, 'mid': jade_mid, 'shadow': jade_dark, 'dark': jade_outline})

    # Jade stepped teeth
    j_teeth = [
        (21, 19), (22, 18), (23, 17),
        (22, 21), (23, 20), (24, 19),
        (24, 21), (25, 20), (26, 19),
        (25, 23), (26, 22), (27, 21),
        (24, 24), (25, 25), (26, 26)
    ]
    for jx, jy in j_teeth:
        put_px(img, jx, jy, jade_mid)
        put_px(img, jx - 1, jy, jade_light)
        put_px(img, jx + 1, jy, jade_dark)
        put_px(img, jx, jy + 1, jade_outline)

    return img

# --- 9, 10, 11, 12: ELEMENTAL SHRINE OF DEPTHS KEYS (32x32 HD) ---
def gen_elemental_shrine_key(element_name, gem_colors):
    """
    element_name: 'anemo', 'geo', 'electro', 'dendro'
    gem_colors: dict with 'dark', 'mid', 'bright', 'shine'
    """
    img = create_empty_canvas()
    
    # Genshin Shrine Key Frame: Exquisite Gilded Silver / Platinum with Gold Trims
    frame_dark = (50, 45, 55, 255)
    frame_shadow = (115, 110, 125, 255)
    frame_mid = (185, 180, 195, 255)
    frame_hi = (240, 235, 250, 255)
    frame_shine = (255, 255, 255, 255)
    
    gold_trim = (245, 195, 60, 255)
    gold_dark = (120, 80, 15, 255)

    # Circular crown with arched wings
    for y in range(2, 15):
        for x in range(2, 15):
            dist = math.hypot(x - 8, y - 8)
            wing = min(math.hypot(x - 4, y - 4), math.hypot(x - 12, y - 4))
            if dist <= 5.8 or wing <= 2.8:
                if dist < 2.6:
                    # Central Elemental Gem
                    if x == 7 and y == 7:
                        put_px(img, x, y, gem_colors['shine'])
                    elif x <= 8 and y <= 8:
                        put_px(img, x, y, gem_colors['bright'])
                    else:
                        put_px(img, x, y, gem_colors['mid'] if dist < 2.2 else gem_colors['dark'])
                elif dist < 3.4:
                    put_px(img, x, y, gold_trim)
                elif dist > 5.2 and wing > 2.2:
                    put_px(img, x, y, frame_dark)
                elif x <= y:
                    put_px(img, x, y, frame_shine if dist > 4.5 else frame_hi)
                else:
                    put_px(img, x, y, frame_shadow if dist > 4.5 else frame_mid)

    # Shaft with element glowing groove
    draw_diagonal_shaft(img, (11, 11), (23, 23), {'hi': frame_hi, 'mid': frame_mid, 'shadow': frame_shadow, 'dark': frame_dark})
    for ep in [13, 16, 19, 22]:
        put_px(img, ep, ep, gem_colors['bright'])

    # Shrine Gate Wards
    shrine_wards = [
        (21, 19), (22, 18), (23, 17), (24, 16),
        (22, 21), (23, 20), (24, 19), (25, 18),
        (24, 21), (25, 20), (26, 19),
        (25, 23), (26, 22), (27, 21),
        (24, 24), (25, 25), (26, 26)
    ]
    for sx, sy in shrine_wards:
        put_px(img, sx, sy, frame_mid)
        put_px(img, sx - 1, sy, frame_hi)
        put_px(img, sx + 1, sy, frame_shadow)
        put_px(img, sx, sy + 1, frame_dark)
        put_px(img, sx, sy - 1, gold_trim)

    return img

def main():
    out_dir = "src/main/resources/assets/awakening_system/textures/item/"
    os.makedirs(out_dir, exist_ok=True)
    
    # 1. Overworld
    ow = gen_overworld_dungeon_key()
    ow.save(os.path.join(out_dir, "overworld_dungeon_key.png"))
    
    # 2. Nether
    ne = gen_nether_flame_key()
    ne.save(os.path.join(out_dir, "nether_flame_key.png"))
    
    # 3. End
    end = gen_end_void_key()
    end.save(os.path.join(out_dir, "end_void_key.png"))
    
    # 4. Teyvat
    tey = gen_teyvat_shrine_key()
    tey.save(os.path.join(out_dir, "teyvat_shrine_key.png"))
    
    # 5. Isekai
    ise = gen_isekai_mana_key()
    ise.save(os.path.join(out_dir, "isekai_mana_key.png"))
    
    # 6. Murim Dao
    mur = gen_murim_dao_key()
    mur.save(os.path.join(out_dir, "murim_dao_key.png"))
    
    # 7. Master Dungeon Key
    dun = gen_master_dungeon_key()
    dun.save(os.path.join(out_dir, "dungeon_key.png"))
    
    # 8. Jade Ancestral Key
    jad = gen_jade_ancestral_key()
    jad.save(os.path.join(out_dir, "jade_ancestral_key.png"))
    
    # 9-12 Elemental Shrine Keys
    anemo_colors = {
        'dark': (15, 85, 75, 255),
        'mid': (45, 185, 155, 255),
        'bright': (90, 245, 210, 255),
        'shine': (220, 255, 245, 255)
    }
    anemo = gen_elemental_shrine_key('anemo', anemo_colors)
    anemo.save(os.path.join(out_dir, "anemo_shrine_of_depths_key.png"))
    
    geo_colors = {
        'dark': (110, 65, 12, 255),
        'mid': (220, 150, 25, 255),
        'bright': (255, 205, 55, 255),
        'shine': (255, 250, 190, 255)
    }
    geo = gen_elemental_shrine_key('geo', geo_colors)
    geo.save(os.path.join(out_dir, "geo_shrine_of_depths_key.png"))
    
    electro_colors = {
        'dark': (65, 15, 110, 255),
        'mid': (160, 50, 225, 255),
        'bright': (215, 120, 255, 255),
        'shine': (250, 220, 255, 255)
    }
    electro = gen_elemental_shrine_key('electro', electro_colors)
    electro.save(os.path.join(out_dir, "electro_shrine_of_depths_key.png"))
    
    dendro_colors = {
        'dark': (20, 80, 25, 255),
        'mid': (50, 180, 65, 255),
        'bright': (110, 235, 120, 255),
        'shine': (225, 255, 225, 255)
    }
    dendro = gen_elemental_shrine_key('dendro', dendro_colors)
    dendro.save(os.path.join(out_dir, "dendro_shrine_of_depths_key.png"))
    
    print("Successfully generated all 12 HD keys!")

if __name__ == "__main__":
    main()
