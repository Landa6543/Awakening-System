import os
import math
from PIL import Image, ImageDraw

target_dir = r"src/main/resources/assets/awakening_system/textures/item"

# =========================================================================
# 1. GENERATE ALL 12 PILLS CLEANLY WITH 0 TRANSLUCENT PIXELS
# =========================================================================
def draw_detailed_pill(glow_col, shell_dark, shell_mid, shell_light, swirl_col, filename):
    base = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    d = ImageDraw.Draw(base)

    cx, cy = 64, 64
    # Solid outer shell and border
    d.ellipse([cx - 40, cy - 40, cx + 40, cy + 40], fill=shell_dark, outline=(15, 15, 20, 255), width=4)
    d.ellipse([cx - 34, cy - 34, cx + 32, cy + 32], fill=shell_mid)
    d.ellipse([cx - 28, cy - 28, cx + 20, cy + 20], fill=shell_light)

    d.arc([cx - 26, cy - 24, cx + 26, cy + 24], start=30, end=210, fill=swirl_col, width=6)
    d.arc([cx - 18, cy - 16, cx + 18, cy + 16], start=210, end=390, fill=(255, 255, 255, 255), width=4)
    d.ellipse([cx - 10, cy - 10, cx + 10, cy + 10], fill=swirl_col)
    d.ellipse([cx - 4, cy - 4, cx + 4, cy + 4], fill=(255, 255, 255, 255))

    d.ellipse([cx - 20, cy - 20, cx - 8, cy - 8], fill=(255, 255, 255, 255))
    d.point([(cx + 12, cy - 14), (cx - 14, cy + 16), (cx + 18, cy + 12)], fill=(255, 255, 220, 255))

    # Sparkle points
    sparkle_pts = [(cx - 48, cy - 20), (cx + 46, cy + 18), (cx + 30, cy - 42), (cx - 32, cy + 40)]
    for sp in sparkle_pts:
        d.point(sp, fill=(255, 255, 255, 255))

    # Resize to 32x32 and strictly binarize
    small = base.resize((32, 32), Image.Resampling.LANCZOS)
    pix = small.load()
    w, h = small.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = pix[x, y]
            if a < 120:
                pix[x, y] = (0, 0, 0, 0)
            else:
                pix[x, y] = (r, g, b, 255)
    dest = os.path.join(target_dir, filename)
    small.save(dest)
    print(f"Generated clean pill: {filename}")

# Generate all pills
draw_detailed_pill((255, 100, 30), (120, 20, 10, 255), (220, 45, 20, 255), (255, 140, 60, 255), (255, 220, 90, 255), "rising_dragon_pill.png")
draw_detailed_pill((30, 220, 120), (15, 75, 40, 255), (25, 160, 85, 255), (75, 230, 145, 255), (200, 255, 230, 255), "marrow_cleansing_pill.png")
draw_detailed_pill((170, 60, 255), (60, 15, 100, 255), (115, 35, 195, 255), (185, 95, 255, 255), (240, 195, 255, 255), "soul_transformation_pill.png")
draw_detailed_pill((255, 215, 0), (130, 85, 10, 255), (218, 155, 20, 255), (255, 230, 80, 255), (255, 255, 220, 255), "realm_breakthrough_pill.png")
draw_detailed_pill((20, 120, 200), (10, 45, 85, 255), (20, 90, 160, 255), (60, 165, 240, 255), (180, 235, 255, 255), "black_tortoise_pill.png")

# Now the 7 previously translucent pills:
draw_detailed_pill((255, 50, 30), (140, 15, 25, 255), (225, 40, 35, 255), (255, 120, 80, 255), (255, 240, 160, 255), "crimson_phoenix_pill.png")
draw_detailed_pill((100, 180, 255), (45, 70, 110, 255), (140, 175, 215, 255), (210, 230, 250, 255), (255, 255, 255, 255), "white_tiger_pill.png")
draw_detailed_pill((40, 220, 100), (18, 85, 45, 255), (45, 175, 85, 255), (130, 240, 160, 255), (220, 255, 220, 255), "foundation_building_pill.png")
draw_detailed_pill((180, 100, 255), (65, 25, 115, 255), (135, 60, 215, 255), (195, 140, 255, 255), (255, 230, 255, 255), "heavenly_enlightenment_pill.png")
draw_detailed_pill((255, 215, 0), (145, 95, 12, 255), (235, 170, 30, 255), (255, 235, 100, 255), (255, 255, 220, 255), "nine_revolutions_pill.png")
draw_detailed_pill((240, 140, 255), (105, 40, 160, 255), (200, 100, 235, 255), (255, 175, 245, 255), (255, 255, 255, 255), "primordial_rebirth_pill.png")
draw_detailed_pill((160, 60, 220), (80, 20, 120, 255), (160, 50, 200, 255), (220, 130, 240, 255), (255, 220, 140, 255), "tribulation_shield_pill.png")

# =========================================================================
# 2. GENERATE ALL 5 GIFT CARDS CLEANLY (NO SUBPIXEL BLUR)
# =========================================================================
cards = [
    (5, (40, 180, 90), (20, 80, 40), (220, 255, 220)),     # Green ($5)
    (15, (60, 140, 240), (20, 50, 120), (220, 240, 255)),  # Blue ($15)
    (30, (170, 70, 240), (70, 20, 110), (245, 220, 255)),  # Purple ($30)
    (50, (240, 150, 40), (120, 60, 10), (255, 240, 210)),  # Gold/Amber ($50)
    (100, (240, 50, 80), (120, 15, 30), (255, 220, 230))    # Crimson / Diamond ($100)
]

for val, bg_bright, bg_dark, fg_color in cards:
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)

    # Clean pixel rectangle for card (no rounded anti-aliasing)
    # Border
    c_border = (220, 200, 140, 255)
    c_out = (15, 15, 20, 255)
    for x in range(3, 29):
        for y in range(7, 25):
            if (x in (3, 28) and y in (7, 24)):
                continue
            if x == 3 or x == 28 or y == 7 or y == 24:
                im.putpixel((x, y), c_out)
            elif x == 4 or x == 27 or y == 8 or y == 23:
                im.putpixel((x, y), c_border)
            else:
                # Gradient
                ratio = (y - 9) / 14.0
                r = int(bg_bright[0] * (1 - ratio) + bg_dark[0] * ratio)
                g = int(bg_bright[1] * (1 - ratio) + bg_dark[1] * ratio)
                b = int(bg_bright[2] * (1 - ratio) + bg_dark[2] * ratio)
                im.putpixel((x, y), (r, g, b, 255))

    # Gold chip
    for cx in range(6, 11):
        for cy in range(12, 16):
            im.putpixel((cx, cy), (255, 215, 0, 255))
    for cy in range(12, 16):
        im.putpixel((8, cy), (180, 140, 0, 255))

    # Star / Crest on top right
    draw.point((23, 10), fill=(255, 255, 255, 255))
    draw.point((22, 11), fill=(255, 240, 180, 255))
    draw.point((24, 11), fill=(255, 240, 180, 255))
    draw.point((23, 12), fill=(255, 255, 255, 255))

    # Dollar symbol & number
    draw.line([(14, 11), (14, 16)], fill=fg_color)
    draw.line([(13, 12), (15, 12)], fill=fg_color)
    draw.line([(13, 14), (15, 14)], fill=fg_color)

    # Value text
    if val == 5:
        draw.line([(17, 11), (20, 11)], fill=fg_color)
        draw.point((17, 12), fill=fg_color)
        draw.line([(17, 13), (20, 13)], fill=fg_color)
        draw.point((20, 14), fill=fg_color)
        draw.line([(17, 15), (20, 15)], fill=fg_color)
    elif val == 15:
        draw.line([(16, 11), (16, 15)], fill=fg_color)
        draw.line([(18, 11), (21, 11)], fill=fg_color)
        draw.point((18, 12), fill=fg_color)
        draw.line([(18, 13), (21, 13)], fill=fg_color)
        draw.point((21, 14), fill=fg_color)
        draw.line([(18, 15), (21, 15)], fill=fg_color)
    elif val == 30:
        draw.line([(16, 11), (19, 11)], fill=fg_color)
        draw.line([(16, 13), (19, 13)], fill=fg_color)
        draw.line([(16, 15), (19, 15)], fill=fg_color)
        draw.point((19, 12), fill=fg_color)
        draw.point((19, 14), fill=fg_color)
        draw.rectangle([(21, 11), (24, 15)], outline=fg_color)
    elif val == 50:
        draw.line([(16, 11), (19, 11)], fill=fg_color)
        draw.point((16, 12), fill=fg_color)
        draw.line([(16, 13), (19, 13)], fill=fg_color)
        draw.point((19, 14), fill=fg_color)
        draw.line([(16, 15), (19, 15)], fill=fg_color)
        draw.rectangle([(21, 11), (24, 15)], outline=fg_color)
    elif val == 100:
        draw.line([(15, 11), (15, 15)], fill=fg_color)
        draw.rectangle([(17, 11), (19, 15)], outline=fg_color)
        draw.rectangle([(21, 11), (23, 15)], outline=fg_color)

    # Solid subtle line
    draw.line([(5, 21), (26, 21)], fill=(255, 255, 255, 255))

    out_file = os.path.join(target_dir, f"gift_card_{val}.png")
    im.save(out_file)
    print(f"Generated clean gift card: {out_file}")

# =========================================================================
# 3. CLEAN LUNAR AUTHORITY ITEMS
# =========================================================================
def clean_lunar_aria():
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    cx, cy = 15.5, 15.5

    c_out = (25, 35, 60, 255)
    c_ring = (140, 205, 255, 255)
    c_moon_hi = (255, 255, 255, 255)
    c_moon_mid = (225, 240, 255, 255)
    c_crater = (180, 210, 240, 255)

    for x in range(4, 28):
        for y in range(4, 28):
            d2 = (x - cx)**2 + (y - cy)**2
            if d2 <= 11.5**2:
                if d2 >= 10.0**2:
                    im.putpixel((x, y), c_out)
                elif d2 >= 8.5**2:
                    im.putpixel((x, y), c_ring)
                elif d2 >= 7.0**2:
                    im.putpixel((x, y), c_out)
                else:
                    im.putpixel((x, y), c_moon_hi if (x < cx and y < cy) else c_moon_mid)

    # Craters
    im.putpixel((12, 13), c_crater)
    im.putpixel((13, 13), c_crater)
    im.putpixel((19, 17), c_crater)
    im.putpixel((20, 18), c_crater)

    # Radiant star in center
    for i in range(-3, 4):
        im.putpixel((int(cx + i), int(cy)), (140, 215, 255, 255))
        im.putpixel((int(cx), int(cy + i)), (140, 215, 255, 255))
    im.putpixel((int(cx), int(cy)), (255, 255, 255, 255))

    # Wings filigree
    for dx in [-12, -11, 11, 12]:
        im.putpixel((int(cx + dx), int(cy - 2)), (210, 240, 255, 255))
        im.putpixel((int(cx + dx), int(cy - 1)), c_out)

    im.save(os.path.join(target_dir, "lunar_authority_aria.png"))
    print("Saved clean lunar_authority_aria.png")

def clean_lunar_sonnet():
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    cx, cy = 15.5, 15.5
    c_out = (60, 30, 80, 255)
    c_gold = (255, 225, 120, 255)
    c_lavender = (210, 150, 245, 255)

    for x in range(4, 28):
        for y in range(4, 28):
            d_out = (x - cx)**2 + (y - cy)**2
            d_in = (x - (cx + 4.5))**2 + (y - cy)**2
            if d_out <= 11.5**2 and d_in >= 8.0**2:
                if d_out >= 10.0**2 or d_in <= 9.2**2:
                    im.putpixel((x, y), c_out)
                else:
                    im.putpixel((x, y), c_gold if x < cx else c_lavender)

    # Harp strings
    for y in range(11, 21, 3):
        for x in range(11, 17):
            if im.getpixel((x, y))[3] == 0:
                im.putpixel((x, y), (255, 255, 240, 255))

    im.save(os.path.join(target_dir, "lunar_authority_sonnet.png"))
    print("Saved clean lunar_authority_sonnet.png")

def clean_lunar_canon():
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    cx, cy = 15.5, 15.5
    c_out = (35, 15, 60, 255)
    c_ring = (190, 110, 255, 255)
    c_obsidian = (18, 10, 30, 255)
    c_core = (255, 210, 255, 255)

    for x in range(4, 28):
        for y in range(4, 28):
            d2 = (x - cx)**2 + (y - cy)**2
            if d2 <= 11.5**2:
                if d2 >= 10.0**2:
                    im.putpixel((x, y), c_out)
                elif d2 >= 8.5**2:
                    im.putpixel((x, y), c_ring)
                elif d2 >= 7.0**2:
                    im.putpixel((x, y), c_out)
                else:
                    im.putpixel((x, y), c_obsidian)

    # Cosmic Diamond eye in center
    for dx in range(-4, 5):
        for dy in range(-4, 5):
            if abs(dx) + abs(dy) <= 4:
                im.putpixel((int(cx + dx), int(cy + dy)), (170, 90, 255, 255))
            if abs(dx) + abs(dy) <= 2:
                im.putpixel((int(cx + dx), int(cy + dy)), c_core)
    im.putpixel((int(cx), int(cy)), (255, 255, 255, 255))

    im.save(os.path.join(target_dir, "lunar_authority_canon.png"))
    print("Saved clean lunar_authority_canon.png")

clean_lunar_aria()
clean_lunar_sonnet()
clean_lunar_canon()

# =========================================================================
# 4. CLEAN HUNTER BOOKS (REMOVE DROP SHADOW AND STRICTLY BINARIZE)
# =========================================================================
for bname in ['hunter_guide.png', 'hunter_bestiary.png']:
    bp = os.path.join(target_dir, bname)
    im = Image.open(bp).convert('RGBA')
    pix = im.load()
    w, h = im.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = pix[x, y]
            # Drop shadow removal
            if y > 100 and a < 180 and (r < 60 and g < 60 and b < 60):
                pix[x, y] = (0, 0, 0, 0)
            elif a < 128:
                pix[x, y] = (0, 0, 0, 0)
            else:
                pix[x, y] = (r, g, b, 255)
    im.save(bp)
    print(f"Cleaned book: {bname}")

# =========================================================================
# 5. STRICT BINARIZATION FOR ANY REMAINING ITEM MODEL TEXTURES
# =========================================================================
remaining = [
    'astral_dragon_crescent.png', 'astral_guandao.png', 'jade_feijian.png',
    'mana_fruit.png', 'absolution.png', 'subway_door.png', 'soul_solid.png',
    'celestial_pine_nut.png', 'eldoria_fruit.png', 'mana_milk_bucket.png',
    'vortex_vanquisher.png', 'bow_full_charge.png'
]

for rname in remaining:
    rp = os.path.join(target_dir, rname)
    if os.path.exists(rp):
        im = Image.open(rp).convert('RGBA')
        pix = im.load()
        w, h = im.size
        for y in range(h):
            for x in range(w):
                r, g, b, a = pix[x, y]
                if a < 128:
                    pix[x, y] = (0, 0, 0, 0)
                else:
                    pix[x, y] = (r, g, b, 255)
        im.save(rp)
        print(f"Binarized alpha for {rname}")

print("Master item texture cleaning completed!")
