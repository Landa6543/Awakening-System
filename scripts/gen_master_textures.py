import math, random, os
from PIL import Image, ImageDraw

ITEM_DIR = 'src/main/resources/assets/awakening_system/textures/item'
BLOCK_DIR = 'src/main/resources/assets/awakening_system/textures/block'
os.makedirs(ITEM_DIR, exist_ok=True)
os.makedirs(BLOCK_DIR, exist_ok=True)

# Helper: clamp color
def clamp(v):
    return max(0, min(255, int(v)))

def lerp_c(c1, c2, t):
    return tuple(clamp(c1[i] + (c2[i] - c1[i]) * t) for i in range(len(c1)))

# ==============================================================================
# 1. MANA MEATS (32x32 High Quality Pixel Art)
# ==============================================================================

def gen_mana_beef():
    # Thick prime ribeye steak with crystalline bone and glowing cyan mana marbling
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Meat polygon base
    meat_pts = [
        (8, 6), (16, 4), (24, 6), (28, 12), (29, 20),
        (25, 26), (17, 28), (10, 27), (6, 21), (5, 13)
    ]
    # Draw meat base with gradient & shading
    for y in range(4, 29):
        for x in range(5, 30):
            # check distance to meat center (17, 16)
            dx = (x - 17) / 11.5
            dy = (y - 16) / 11.0
            dist = dx*dx + dy*dy
            if dist <= 1.0:
                # Base meat color: deep rich crimson-ruby
                # Shading: top-left lighter, bottom-right darker
                shade = 1.0 - (x * 0.015 + y * 0.02)
                r = clamp(170 * shade + 35)
                g = clamp(25 * shade + 10)
                b = clamp(40 * shade + 20)
                
                # Mana marbling veins (flowing curves)
                v1 = math.sin(x * 0.65 + y * 0.45) * math.cos(y * 0.5 - x * 0.2)
                v2 = math.sin((x + y) * 0.8)
                if abs(v1) < 0.18 or abs(v2) < 0.12:
                    # Luminous cyan mana fat vein
                    r, g, b = (40, 225, 245)
                    if abs(v1) < 0.07:
                        r, g, b = (190, 250, 255) # Pure bright mana core
                elif dist > 0.82:
                    # Crisp seared fat rind
                    r = clamp(210 * shade + 30)
                    g = clamp(175 * shade + 20)
                    b = clamp(130 * shade + 20)

                img.putpixel((x, y), (r, g, b, 255))

    # Dark crisp outline
    for y in range(3, 30):
        for x in range(4, 31):
            if img.getpixel((x, y))[3] > 0:
                # check neighbors
                for nx, ny in [(x-1,y), (x+1,y), (x,y-1), (x,y+1)]:
                    if nx < 0 or nx >= 32 or ny < 0 or ny >= 32 or img.getpixel((nx, ny))[3] == 0:
                        img.putpixel((x, y), (55, 12, 18, 255))
                        break

    # Crystalline Bone at top-left
    bone_pts = [(7, 7), (10, 5), (12, 8), (9, 11), (6, 9)]
    d.polygon(bone_pts, fill=(210, 245, 255, 255), outline=(130, 190, 220, 255))
    img.putpixel((9, 8), (255, 255, 255, 255)) # Bone highlight
    img.putpixel((8, 7), (240, 255, 255, 255))

    # Glowing mana highlights
    d.point([(14, 12), (18, 17), (21, 21), (13, 20)], fill=(220, 255, 255, 255))
    img.save(f'{ITEM_DIR}/mana_beef.png')

def gen_cooked_mana_beef():
    # Flame-grilled thick mana steak with sear marks and glowing cyan juices
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    for y in range(4, 29):
        for x in range(5, 30):
            dx = (x - 17) / 11.5
            dy = (y - 16) / 11.0
            dist = dx*dx + dy*dy
            if dist <= 1.0:
                # Cooked rich mahogany steak
                shade = 1.0 - (x * 0.018 + y * 0.02)
                r = clamp(110 * shade + 35)
                g = clamp(45 * shade + 15)
                b = clamp(30 * shade + 15)

                # Grill marks (diagonal charred stripes)
                if abs((x + y) % 7 - 3) < 1.0:
                    r, g, b = (45, 18, 12)

                # Radiant mana juices oozing through cracks
                v1 = math.sin(x * 0.65 + y * 0.45) * math.cos(y * 0.5 - x * 0.2)
                if abs(v1) < 0.15:
                    r, g, b = (30, 210, 235)
                    if abs(v1) < 0.05:
                        r, g, b = (170, 245, 255)

                img.putpixel((x, y), (r, g, b, 255))

    # Dark charred crust outline
    for y in range(3, 30):
        for x in range(4, 31):
            if img.getpixel((x, y))[3] > 0:
                for nx, ny in [(x-1,y), (x+1,y), (x,y-1), (x,y+1)]:
                    if nx < 0 or nx >= 32 or ny < 0 or ny >= 32 or img.getpixel((nx, ny))[3] == 0:
                        img.putpixel((x, y), (35, 12, 10, 255))
                        break

    # Crystal bone cooked
    d.polygon([(7, 7), (10, 5), (12, 8), (9, 11), (6, 9)], fill=(200, 230, 240, 255), outline=(90, 140, 160, 255))
    img.putpixel((9, 8), (250, 255, 255, 255))
    img.save(f'{ITEM_DIR}/cooked_mana_beef.png')

def gen_mana_porkchop():
    # Tender pork chop with pink-rose meat, white rind, and cyan mana veins
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    for y in range(5, 28):
        for x in range(6, 28):
            dx = (x - 17) / 10.0
            dy = (y - 16) / 10.5
            dist = dx*dx + dy*dy
            if dist <= 1.0:
                shade = 1.0 - (x * 0.015 + y * 0.02)
                # Tender rosy pork
                r = clamp(235 * shade + 15)
                g = clamp(140 * shade + 20)
                b = clamp(155 * shade + 25)

                # Outer fat rind
                if dx < -0.4:
                    r = clamp(245 * shade + 10)
                    g = clamp(230 * shade + 20)
                    b = clamp(215 * shade + 20)

                # Ethereal mana swirl
                v = math.sin(x * 0.7 - y * 0.5)
                if abs(v) < 0.18:
                    r, g, b = (50, 230, 250)
                    if abs(v) < 0.06:
                        r, g, b = (200, 255, 255)

                img.putpixel((x, y), (r, g, b, 255))

    for y in range(4, 29):
        for x in range(5, 29):
            if img.getpixel((x, y))[3] > 0:
                for nx, ny in [(x-1,y), (x+1,y), (x,y-1), (x,y+1)]:
                    if nx < 0 or nx >= 32 or ny < 0 or ny >= 32 or img.getpixel((nx, ny))[3] == 0:
                        img.putpixel((x, y), (70, 30, 40, 255))
                        break

    # Crystalline T-bone handle
    d.polygon([(18, 5), (22, 4), (24, 7), (20, 8)], fill=(220, 250, 255, 255), outline=(120, 180, 210, 255))
    img.save(f'{ITEM_DIR}/mana_porkchop.png')

def gen_mana_mutton():
    # Curved lamb rack chop with dark burgundy meat and crystal bone handle
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    for y in range(8, 28):
        for x in range(7, 26):
            dx = (x - 16) / 8.5
            dy = (y - 18) / 9.5
            dist = dx*dx + dy*dy
            if dist <= 1.0:
                shade = 1.0 - (x * 0.015 + y * 0.02)
                # Rich burgundy-red mutton
                r = clamp(190 * shade + 20)
                g = clamp(40 * shade + 15)
                b = clamp(55 * shade + 20)

                # Mana threads
                v = math.sin(x * 0.8 + y * 0.6)
                if abs(v) < 0.2:
                    r, g, b = (45, 220, 245)
                    if abs(v) < 0.07:
                        r, g, b = (195, 250, 255)

                img.putpixel((x, y), (r, g, b, 255))

    for y in range(7, 29):
        for x in range(6, 27):
            if img.getpixel((x, y))[3] > 0:
                for nx, ny in [(x-1,y), (x+1,y), (x,y-1), (x,y+1)]:
                    if nx < 0 or nx >= 32 or ny < 0 or ny >= 32 or img.getpixel((nx, ny))[3] == 0:
                        img.putpixel((x, y), (60, 15, 22, 255))
                        break

    # Long crystal rib bone extending upwards
    d.polygon([(11, 12), (9, 4), (12, 3), (14, 10)], fill=(215, 245, 255, 255), outline=(110, 170, 200, 255))
    img.putpixel((10, 5), (255, 255, 255, 255))
    img.save(f'{ITEM_DIR}/mana_mutton.png')

def gen_mana_chicken():
    # Plump poultry drumstick with mana glaze and crystal bone nub
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    for y in range(6, 24):
        for x in range(9, 27):
            dx = (x - 18) / 8.0
            dy = (y - 14) / 7.5
            dist = dx*dx + dy*dy
            if dist <= 1.0:
                shade = 1.0 - (x * 0.015 + y * 0.02)
                # Golden-tan tender poultry
                r = clamp(225 * shade + 20)
                g = clamp(170 * shade + 15)
                b = clamp(120 * shade + 20)

                # Mana glaze dripping
                v = math.sin(x * 0.75 - y * 0.6)
                if abs(v) < 0.2:
                    r, g, b = (55, 230, 250)
                    if abs(v) < 0.07:
                        r, g, b = (205, 255, 255)

                img.putpixel((x, y), (r, g, b, 255))

    for y in range(5, 25):
        for x in range(8, 28):
            if img.getpixel((x, y))[3] > 0:
                for nx, ny in [(x-1,y), (x+1,y), (x,y-1), (x,y+1)]:
                    if nx < 0 or nx >= 32 or ny < 0 or ny >= 32 or img.getpixel((nx, ny))[3] == 0:
                        img.putpixel((x, y), (75, 45, 20, 255))
                        break

    # Crystal bone knob at bottom-left
    d.polygon([(9, 21), (6, 25), (8, 28), (12, 24)], fill=(220, 245, 255, 255), outline=(120, 180, 210, 255))
    img.save(f'{ITEM_DIR}/mana_chicken_meat.png')

# ==============================================================================
# 2. AUTHENTIC GENSHIN IMPACT MONSTER DROPS (32x32)
# ==============================================================================

def gen_slime_condensate():
    # Canonical Genshin: teardrop cyan droplet with pure translucent core and light reflection
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    for y in range(5, 28):
        for x in range(7, 25):
            # Teardrop formula: wider at bottom, tapered at top
            factor = (y - 5) / 23.0
            hw = 9.0 * math.sqrt(max(0.0, factor)) * (1.1 - 0.4 * factor)
            dx = abs(x - 16)
            if dx <= hw:
                t = dx / max(0.1, hw)
                # Outer cyan rim -> inner radiant aquamarine
                r = clamp(20 + 40 * (1.0 - t))
                g = clamp(170 + 75 * (1.0 - t))
                b = clamp(210 + 45 * (1.0 - t))
                a = clamp(200 + 55 * (1.0 - t))
                img.putpixel((x, y), (r, g, b, a))

    # Crisp outer outline
    for y in range(4, 29):
        for x in range(6, 26):
            if img.getpixel((x, y))[3] > 0:
                for nx, ny in [(x-1,y), (x+1,y), (x,y-1), (x,y+1)]:
                    if nx < 0 or nx >= 32 or ny < 0 or ny >= 32 or img.getpixel((nx, ny))[3] == 0:
                        img.putpixel((x, y), (10, 110, 150, 255))
                        break

    # White specular glare highlight on top-left curve
    d.ellipse([(12, 10), (15, 14)], fill=(255, 255, 255, 240))
    d.ellipse([(14, 16), (16, 18)], fill=(220, 255, 255, 190))
    img.save(f'{ITEM_DIR}/slime_condensate.png')

def gen_slime_secretions():
    # Canonical Genshin: viscous puddle with little bubbles and glossy sheen
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Multi-lobed puddle
    centers = [(12, 18, 7), (20, 17, 8), (16, 22, 6), (10, 14, 5), (22, 12, 5)]
    for y in range(6, 29):
        for x in range(4, 29):
            min_dist = 999.0
            for cx, cy, cr in centers:
                dist = math.sqrt((x - cx)**2 + (y - cy)**2) / cr
                if dist < min_dist:
                    min_dist = dist
            if min_dist <= 1.0:
                t = min_dist
                r = clamp(15 + 45 * (1.0 - t))
                g = clamp(190 + 55 * (1.0 - t))
                b = clamp(180 + 65 * (1.0 - t))
                img.putpixel((x, y), (r, g, b, 235))

    # Darker outline
    for y in range(5, 30):
        for x in range(3, 30):
            if img.getpixel((x, y))[3] > 0:
                for nx, ny in [(x-1,y), (x+1,y), (x,y-1), (x,y+1)]:
                    if nx < 0 or nx >= 32 or ny < 0 or ny >= 32 or img.getpixel((nx, ny))[3] == 0:
                        img.putpixel((x, y), (8, 120, 130, 255))
                        break

    # Floating slime bubbles
    d.ellipse([(14, 14), (18, 18)], outline=(220, 255, 255, 255), fill=(70, 235, 240, 200))
    d.ellipse([(11, 18), (13, 20)], fill=(240, 255, 255, 220))
    d.ellipse([(21, 15), (23, 17)], fill=(240, 255, 255, 220))
    img.save(f'{ITEM_DIR}/slime_secretions.png')

def gen_slime_concentrate():
    # Canonical Genshin: brilliant celestial concentrate sphere with radiant rings
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    cx, cy = 16, 16
    for y in range(4, 28):
        for x in range(4, 28):
            dist = math.sqrt((x - cx)**2 + (y - cy)**2)
            if dist <= 11.0:
                t = dist / 11.0
                r = clamp(30 + 190 * (1.0 - t**1.5))
                g = clamp(210 + 45 * (1.0 - t))
                b = clamp(255)
                a = clamp(255 * (1.0 - 0.2 * t))
                img.putpixel((x, y), (r, g, b, a))

    # Dark blue-cyan perimeter outline
    for y in range(3, 29):
        for x in range(3, 29):
            if img.getpixel((x, y))[3] > 0:
                for nx, ny in [(x-1,y), (x+1,y), (x,y-1), (x,y+1)]:
                    if nx < 0 or nx >= 32 or ny < 0 or ny >= 32 or img.getpixel((nx, ny))[3] == 0:
                        img.putpixel((x, y), (15, 80, 140, 255))
                        break

    # Radiating golden-white star core
    d.line([(16, 8), (16, 24)], fill=(255, 255, 255, 255), width=1)
    d.line([(8, 16), (24, 16)], fill=(255, 255, 255, 255), width=1)
    d.ellipse([(14, 14), (18, 18)], fill=(255, 255, 255, 255))
    img.save(f'{ITEM_DIR}/slime_concentrate.png')

def gen_condensed_resin():
    # Canonical Genshin: golden-amber cubic crystal encasing a glowing cyan Geo Crystalfly
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Amber crystal facets
    # Hexagonal / rhomboid crystal shape
    poly = [(16, 4), (26, 9), (26, 23), (16, 28), (6, 23), (6, 9)]
    d.polygon(poly, fill=(245, 175, 40, 220), outline=(210, 130, 20, 255))

    # Inner facets
    d.line([(16, 4), (16, 28)], fill=(255, 215, 80, 240), width=1)
    d.line([(16, 16), (26, 9)], fill=(255, 225, 100, 240), width=1)
    d.line([(16, 16), (6, 9)], fill=(255, 200, 70, 240), width=1)
    d.line([(16, 16), (26, 23)], fill=(200, 120, 20, 240), width=1)
    d.line([(16, 16), (6, 23)], fill=(180, 110, 15, 240), width=1)

    # Shading top facet bright, bottom facet dark
    for y in range(5, 16):
        for x in range(7, 26):
            if img.getpixel((x, y))[3] > 0 and y < 16:
                c = img.getpixel((x, y))
                img.putpixel((x, y), (clamp(c[0]+25), clamp(c[1]+25), clamp(c[2]+15), c[3]))

    # Geo Crystalfly inside (Cyan glowing butterfly)
    # Wings
    d.polygon([(16, 16), (10, 11), (12, 16), (14, 17)], fill=(60, 240, 255, 255))
    d.polygon([(16, 16), (22, 11), (20, 16), (18, 17)], fill=(60, 240, 255, 255))
    d.polygon([(16, 17), (12, 21), (15, 20)], fill=(40, 210, 240, 255))
    d.polygon([(16, 17), (20, 21), (17, 20)], fill=(40, 210, 240, 255))
    # Body & head
    d.line([(16, 13), (16, 19)], fill=(220, 255, 255, 255), width=1)
    d.point([(15, 12), (17, 12)], fill=(255, 255, 255, 255)) # Antennae tips
    img.save(f'{ITEM_DIR}/condensed_resin.png')

def gen_damaged_mask():
    # Canonical Genshin: white curved porcelain mask with curved red eye-slit and crack/break
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Mask oval
    mask_poly = [
        (16, 5), (23, 8), (26, 15), (24, 22), (19, 27),
        (13, 27), (8, 22), (6, 15), (9, 8)
    ]
    d.polygon(mask_poly, fill=(235, 235, 230, 255), outline=(90, 85, 80, 255))

    # Inner soft shading (porcelain depth)
    for y in range(6, 27):
        for x in range(7, 26):
            if img.getpixel((x, y))[3] > 0:
                dist = math.sqrt((x - 16)**2 + (y - 16)**2)
                shade = clamp(245 - dist * 3.5)
                img.putpixel((x, y), (shade, shade, clamp(shade - 5), 255))

    # Red tribal markings (curved eye slashes)
    d.line([(11, 13), (13, 17)], fill=(195, 30, 30, 255), width=2)
    d.line([(21, 13), (19, 17)], fill=(195, 30, 30, 255), width=2)
    d.line([(14, 21), (18, 21)], fill=(195, 30, 30, 255), width=1)

    # Crack and broken chip at top-left
    d.line([(9, 7), (12, 11), (11, 14)], fill=(60, 55, 50, 255), width=1)
    # Chip out corner
    for p in [(7, 7), (8, 7), (7, 8), (8, 8), (9, 8)]:
        img.putpixel(p, (0, 0, 0, 0))

    img.save(f'{ITEM_DIR}/damaged_mask.png')

def gen_stained_mask():
    # Canonical Genshin: weathered Hilichurl mask with orange/brown stain and war markings
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    mask_poly = [
        (16, 5), (23, 8), (26, 15), (24, 22), (19, 27),
        (13, 27), (8, 22), (6, 15), (9, 8)
    ]
    d.polygon(mask_poly, fill=(215, 205, 190, 255), outline=(75, 65, 55, 255))

    # Stained weathered texture
    for y in range(6, 27):
        for x in range(7, 26):
            if img.getpixel((x, y))[3] > 0:
                # Ochre / mud staining on bottom and edges
                stain = 1.0 - (y / 27.0) * 0.3
                r = clamp(210 * stain + (x % 3) * 5)
                g = clamp(190 * stain + (y % 3) * 4)
                b = clamp(165 * stain)
                img.putpixel((x, y), (r, g, b, 255))

    # Intricate war markings (curved red eyes + orange tribal horns on brow)
    d.line([(10, 13), (13, 17)], fill=(180, 25, 25, 255), width=2)
    d.line([(22, 13), (19, 17)], fill=(180, 25, 25, 255), width=2)
    d.polygon([(14, 8), (16, 6), (18, 8), (16, 10)], fill=(210, 110, 20, 255))
    d.line([(13, 22), (19, 22)], fill=(180, 25, 25, 255), width=1)
    img.save(f'{ITEM_DIR}/stained_mask.png')

def gen_ominous_mask():
    # Canonical Genshin: Mitachurl chieftain mask with dual curved black horns and crimson runes
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Horns first (behind mask)
    # Left horn curving outward and up
    horn_l = [(9, 12), (5, 9), (3, 4), (6, 3), (9, 7)]
    d.polygon(horn_l, fill=(65, 55, 55, 255), outline=(30, 25, 25, 255))
    # Right horn
    horn_r = [(23, 12), (27, 9), (29, 4), (26, 3), (23, 7)]
    d.polygon(horn_r, fill=(65, 55, 55, 255), outline=(30, 25, 25, 255))

    # Mask oval
    mask_poly = [
        (16, 7), (23, 10), (25, 17), (23, 24), (19, 28),
        (13, 28), (9, 24), (7, 17), (9, 10)
    ]
    d.polygon(mask_poly, fill=(240, 235, 225, 255), outline=(60, 50, 45, 255))

    # Shading
    for y in range(8, 28):
        for x in range(8, 25):
            if img.getpixel((x, y))[3] > 0 and (x, y) not in horn_l and (x, y) not in horn_r:
                dist = math.sqrt((x - 16)**2 + (y - 17)**2)
                shade = clamp(240 - dist * 4.0)
                img.putpixel((x, y), (shade, clamp(shade - 5), clamp(shade - 10), 255))

    # Fierce ominous glowing crimson war patterns
    d.line([(10, 14), (13, 19)], fill=(225, 20, 20, 255), width=2)
    d.line([(22, 14), (19, 19)], fill=(225, 20, 20, 255), width=2)
    d.polygon([(16, 10), (13, 14), (19, 14)], fill=(210, 25, 25, 255))
    d.line([(12, 23), (16, 25), (20, 23)], fill=(225, 20, 20, 255), width=2)

    # Horn ridges
    d.line([(4, 6), (7, 8)], fill=(120, 100, 95, 255), width=1)
    d.line([(28, 6), (25, 8)], fill=(120, 100, 95, 255), width=1)
    img.save(f'{ITEM_DIR}/ominous_mask.png')

def gen_heavy_horn():
    # Canonical Genshin: curved ivory Mitachurl horn banded with engraved bronze ring
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Curved horn body: starts wide at bottom-right, curves to sharp tip at top-left
    horn_pts = [
        (26, 26), (23, 28), (17, 26), (12, 20), (8, 14),
        (5, 7), (7, 5), (12, 8), (18, 14), (24, 20)
    ]
    d.polygon(horn_pts, fill=(235, 225, 205, 255), outline=(70, 60, 45, 255))

    # Horn shading & texture ridges
    for y in range(5, 29):
        for x in range(5, 27):
            if img.getpixel((x, y))[3] > 0:
                shade = 1.0 - ((x + y) * 0.012)
                r = clamp(235 * shade)
                g = clamp(225 * shade)
                b = clamp(205 * shade)
                # Ridges
                if (x + y) % 4 == 0:
                    r = clamp(r - 25)
                    g = clamp(g - 25)
                    b = clamp(b - 25)
                img.putpixel((x, y), (r, g, b, 255))

    # Bronze / copper tribal band in the middle
    d.polygon([(14, 15), (17, 13), (21, 17), (18, 20)], fill=(185, 115, 45, 255), outline=(95, 55, 15, 255))
    # Engraved rune on band
    d.point([(17, 16), (18, 15)], fill=(255, 215, 130, 255))
    img.save(f'{ITEM_DIR}/heavy_horn.png')

# ==============================================================================
# 3. SAPLINGS (32x32 on 100% Transparent Background)
# ==============================================================================

def gen_sapling(filename, stem_c, leaf_primary, leaf_secondary, fruit_c=None, glow_c=None):
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Trunk / stem (curving upwards from 16, 29 to 16, 14)
    stem_pts = [(16, 29), (15, 24), (16, 18), (15, 13)]
    for i in range(len(stem_pts) - 1):
        d.line([stem_pts[i], stem_pts[i+1]], fill=stem_c, width=2)

    # Branches
    d.line([(16, 20), (11, 16)], fill=stem_c, width=1)
    d.line([(16, 18), (21, 15)], fill=stem_c, width=1)
    d.line([(15, 14), (13, 10)], fill=stem_c, width=1)
    d.line([(15, 14), (19, 10)], fill=stem_c, width=1)

    # Leaf clusters (multiple overlapping leafy spheres)
    clusters = [
        (11, 15, 4), (21, 14, 4), (16, 10, 5), (12, 9, 3), (20, 9, 3), (16, 6, 3)
    ]
    for cx, cy, r in clusters:
        for y in range(cy - r, cy + r + 1):
            for x in range(cx - r, cx + r + 1):
                if (x - cx)**2 + (y - cy)**2 <= r**2:
                    t = (y - (cy - r)) / max(1, 2 * r) # 0 at top, 1 at bottom
                    col = lerp_c(leaf_primary, leaf_secondary, t)
                    img.putpixel((x, y), (col[0], col[1], col[2], 255))

    # Add dark outline to leaf clusters for crisp Minecraft style
    for y in range(1, 31):
        for x in range(1, 31):
            if img.getpixel((x, y))[3] > 0:
                for nx, ny in [(x-1,y), (x+1,y), (x,y-1), (x,y+1)]:
                    if img.getpixel((nx, ny))[3] == 0:
                        c = img.getpixel((x, y))
                        dark = (clamp(c[0]*0.5), clamp(c[1]*0.5), clamp(c[2]*0.5), 255)
                        img.putpixel((x, y), dark)
                        break

    # Fruits or flowers if specified
    if fruit_c:
        d.ellipse([(9, 15), (11, 17)], fill=fruit_c)
        d.ellipse([(20, 13), (22, 15)], fill=fruit_c)
        d.ellipse([(15, 8), (17, 10)], fill=fruit_c)

    # Bioluminescent glow specs if specified
    if glow_c:
        d.point([(10, 14), (21, 13), (16, 7), (13, 8), (19, 8)], fill=glow_c)

    # Roots at bottom
    d.line([(16, 29), (12, 31)], fill=stem_c, width=1)
    d.line([(16, 29), (20, 31)], fill=stem_c, width=1)

    img.save(filename)

# ==============================================================================
# 4. LOGS & LOG TOPS (Detailed rings, cambium, textured bark rim)
# ==============================================================================

def gen_log_top(filename, bark_c, sapwood_c, heartwood_c, ring_c):
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 255))
    d = ImageDraw.Draw(img)

    cx, cy = 7.5, 7.5
    for y in range(16):
        for x in range(16):
            dist = math.sqrt((x - cx)**2 + (y - cy)**2)
            if dist > 6.8:
                # Rugged textured bark perimeter
                noise = ((x * 17 + y * 31) % 5) * 6
                r = clamp(bark_c[0] + noise - 12)
                g = clamp(bark_c[1] + noise - 12)
                b = clamp(bark_c[2] + noise - 12)
            elif dist > 5.5:
                # Cambium / sapwood boundary
                r, g, b = sapwood_c
            else:
                # Concentric annual growth rings
                ring_phase = math.sin(dist * 3.8)
                if ring_phase > 0.4:
                    r, g, b = ring_c
                elif dist <= 1.8:
                    # Heartwood center
                    r, g, b = heartwood_c
                else:
                    t = dist / 5.5
                    r, g, b = lerp_c(heartwood_c, sapwood_c, t)
                # Subtle wood noise
                noise = ((x * 13 + y * 7) % 3) * 4 - 4
                r = clamp(r + noise)
                g = clamp(g + noise)
                b = clamp(b + noise)
            img.putpixel((x, y), (r, g, b, 255))
    img.save(filename)

def gen_log_side(filename, bark_primary, bark_secondary, fissure_c):
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 255))
    for y in range(16):
        for x in range(16):
            # Vertical bark texture with fissures
            fissure = math.sin(x * 1.5 + (y * 0.3))
            noise = ((x * 23 + y * 37) % 7) * 4
            if fissure < -0.65:
                r, g, b = fissure_c
            elif fissure < 0.2:
                r, g, b = bark_secondary
            else:
                r, g, b = bark_primary
            r = clamp(r + noise - 10)
            g = clamp(g + noise - 10)
            b = clamp(b + noise - 10)
            img.putpixel((x, y), (r, g, b, 255))
    img.save(filename)

# ==============================================================================
# 5. LEAVES (Minecraft style: distinct clusters, cutout gaps)
# ==============================================================================

def gen_leaves(filename, c_light, c_mid, c_dark, gap_ratio=0.18):
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            # Deterministic pseudo-random based on coords
            h = (x * 73 + y * 137 + (x ^ y) * 23) % 100
            if h < gap_ratio * 100:
                # Transparent cutout gap
                img.putpixel((x, y), (0, 0, 0, 0))
            else:
                # Cluster shading: top-edges lighter, bottom-edges darker
                cluster = ((x // 4) + (y // 4)) % 2
                in_cluster_y = y % 4
                if in_cluster_y == 0:
                    c = c_light
                elif in_cluster_y == 3:
                    c = c_dark
                else:
                    c = c_mid
                # subtle noise
                noise = (h % 9) * 4 - 16
                r = clamp(c[0] + noise)
                g = clamp(c[1] + noise)
                b = clamp(c[2] + noise)
                img.putpixel((x, y), (r, g, b, 255))
    img.save(filename)

# ==============================================================================
# 6. PLANKS (Wood grain and joints)
# ==============================================================================

def gen_planks(filename, p_light, p_mid, p_dark, nail_c):
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 255))
    d = ImageDraw.Draw(img)
    # 4 horizontal plank strips (height 4 each)
    for p in range(4):
        y_start = p * 4
        # Staggered vertical joints
        joint_x = 8 if (p % 2 == 0) else 12
        for dy in range(4):
            y = y_start + dy
            for x in range(16):
                if dy == 3:
                    # Bottom groove shadow of plank
                    c = p_dark
                elif dy == 0:
                    # Top highlight edge of plank
                    c = p_light
                elif x == joint_x:
                    # Vertical plank joint
                    c = p_dark
                else:
                    grain = ((x * 11 + y * 3) % 4) * 5
                    c = (clamp(p_mid[0] + grain), clamp(p_mid[1] + grain), clamp(p_mid[2] + grain))
                img.putpixel((x, y), (c[0], c[1], c[2], 255))
        # Nail dots
        nail1 = (joint_x - 1, y_start + 1)
        nail2 = (joint_x + 1, y_start + 1)
        if 0 <= nail1[0] < 16: img.putpixel(nail1, nail_c)
        if 0 <= nail2[0] < 16: img.putpixel(nail2, nail_c)
    img.save(filename)

# ==============================================================================
# RUN ALL GENERATIONS
# ==============================================================================

print('Generating Mana Meats...')
gen_mana_beef()
gen_cooked_mana_beef()
gen_mana_porkchop()
gen_mana_mutton()
gen_mana_chicken()

print('Generating Authentic Genshin Drops...')
gen_slime_condensate()
gen_slime_secretions()
gen_slime_concentrate()
gen_condensed_resin()
gen_damaged_mask()
gen_stained_mask()
gen_ominous_mask()
gen_heavy_horn()

print('Generating Teyvat & Isekai Saplings...')
# Cuihua: warm brown stem, rich emerald leaves, red-orange fruit buds
gen_sapling(f'{BLOCK_DIR}/cuihua_sapling.png', (115, 75, 40, 255), (120, 205, 55), (45, 125, 30), fruit_c=(235, 75, 35, 255))
# Adhigama: deep mahogany stem, lush rainforest green leaves
gen_sapling(f'{BLOCK_DIR}/adhigama_sapling.png', (85, 45, 25, 255), (75, 185, 60), (25, 95, 35))
# Sandbearer: grey-tan trunk, golden-amber autumn pagoda leaves
gen_sapling(f'{BLOCK_DIR}/sandbearer_sapling.png', (130, 115, 90, 255), (245, 195, 45), (180, 115, 20))
# Mana Wood: dark indigo-cyan stem, glowing cyan-azure leaves with starry buds
gen_sapling(f'{BLOCK_DIR}/mana_wood_sapling.png', (45, 50, 95, 255), (60, 235, 255), (25, 110, 200), glow_c=(210, 255, 255, 255))
# Eldoria: golden-tan stem, radiant golden-amber leaves with golden sparkle
gen_sapling(f'{BLOCK_DIR}/eldoria_sapling.png', (185, 145, 70, 255), (255, 220, 65), (200, 140, 25), glow_c=(255, 255, 200, 255))

print('Generating Teyvat & Isekai Logs & Log Tops...')
# Cuihua
gen_log_top(f'{BLOCK_DIR}/cuihua_log_top.png', bark_c=(95, 65, 35), sapwood_c=(200, 160, 105), heartwood_c=(150, 95, 45), ring_c=(175, 125, 70))
gen_log_side(f'{BLOCK_DIR}/cuihua_log.png', bark_primary=(115, 80, 45), bark_secondary=(85, 55, 30), fissure_c=(55, 35, 18))
# Adhigama
gen_log_top(f'{BLOCK_DIR}/adhigama_log_top.png', bark_c=(75, 45, 25), sapwood_c=(185, 130, 95), heartwood_c=(125, 65, 40), ring_c=(150, 85, 55))
gen_log_side(f'{BLOCK_DIR}/adhigama_log.png', bark_primary=(90, 55, 32), bark_secondary=(65, 38, 20), fissure_c=(40, 22, 12))
# Sandbearer
gen_log_top(f'{BLOCK_DIR}/sandbearer_log_top.png', bark_c=(110, 100, 85), sapwood_c=(220, 185, 110), heartwood_c=(180, 110, 45), ring_c=(195, 140, 70))
gen_log_side(f'{BLOCK_DIR}/sandbearer_log.png', bark_primary=(125, 115, 95), bark_secondary=(95, 85, 70), fissure_c=(65, 55, 45))
# Mana Wood
gen_log_top(f'{BLOCK_DIR}/mana_wood_log_top.png', bark_c=(35, 40, 75), sapwood_c=(70, 130, 190), heartwood_c=(30, 220, 245), ring_c=(50, 160, 215))
gen_log_side(f'{BLOCK_DIR}/mana_wood_log.png', bark_primary=(40, 48, 88), bark_secondary=(28, 33, 62), fissure_c=(20, 190, 220))
# Eldoria
gen_log_top(f'{BLOCK_DIR}/eldoria_log_top.png', bark_c=(145, 110, 55), sapwood_c=(245, 215, 130), heartwood_c=(225, 165, 45), ring_c=(200, 135, 35))
gen_log_side(f'{BLOCK_DIR}/eldoria_log.png', bark_primary=(170, 130, 65), bark_secondary=(135, 100, 48), fissure_c=(95, 68, 30))

print('Generating Teyvat & Isekai Leaves...')
gen_leaves(f'{BLOCK_DIR}/cuihua_leaves.png', (135, 215, 60), (80, 165, 40), (45, 110, 25))
gen_leaves(f'{BLOCK_DIR}/adhigama_leaves.png', (85, 195, 70), (45, 145, 50), (25, 95, 30))
gen_leaves(f'{BLOCK_DIR}/sandbearer_leaves.png', (250, 205, 55), (210, 145, 30), (150, 90, 20))
gen_leaves(f'{BLOCK_DIR}/mana_wood_leaves.png', (80, 240, 255), (40, 160, 230), (25, 90, 175))
gen_leaves(f'{BLOCK_DIR}/eldoria_leaves.png', (255, 230, 80), (230, 175, 40), (175, 115, 25))

print('Generating Isekai Planks...')
gen_planks(f'{BLOCK_DIR}/mana_wood_planks.png', (85, 135, 185), (55, 95, 145), (35, 65, 105), (20, 200, 230, 255))
gen_planks(f'{BLOCK_DIR}/eldoria_planks.png', (250, 215, 125), (220, 175, 85), (170, 125, 50), (120, 80, 30, 255))

print('ALL TEXTURES GENERATED SUCCESSFULLY!')
