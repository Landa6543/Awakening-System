import math
from PIL import Image, ImageDraw

def create_aria_texture():
    # 32x32 Full Moon Celestial Medallion (Aria)
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    cx, cy = 16, 16

    # Celestial outer aura
    for r in range(14, 8, -1):
        alpha = int(35 + (15 - r) * 20)
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(160, 225, 255, alpha))

    # Wings / celestial filigree
    wing_color = (210, 240, 255, 240)
    shadow_wing = (100, 160, 210, 220)
    for dx in [-12, -11, -10, -9, 9, 10, 11, 12]:
        draw.point((cx + dx, cy + int(abs(dx)*0.4) - 4), fill=wing_color)
        draw.point((cx + dx, cy + int(abs(dx)*0.4) - 3), fill=shadow_wing)

    # Silver full moon base
    draw.ellipse([cx - 9, cy - 9, cx + 9, cy + 9], fill=(240, 250, 255, 255), outline=(130, 200, 240, 255))
    draw.ellipse([cx - 7, cy - 7, cx + 7, cy + 7], fill=(255, 255, 255, 255))

    # Lunar crater nuances & star center
    draw.ellipse([cx - 4, cy - 5, cx - 1, cy - 2], fill=(220, 235, 250, 200))
    draw.ellipse([cx + 2, cy + 1, cx + 5, cy + 4], fill=(220, 235, 250, 200))

    # 4-pointed radiant star in center
    star_col = (140, 215, 255, 255)
    core_col = (255, 255, 255, 255)
    for i in range(-3, 4):
        draw.point((cx + i, cy), fill=star_col)
        draw.point((cx, cy + i), fill=star_col)
    draw.point((cx, cy), fill=core_col)

    im.save("src/main/resources/assets/awakening_system/textures/item/lunar_authority_aria.png")
    print("Aria texture saved")

def create_sonnet_texture():
    # 32x32 Crescent Moon Harp / Talisman (Sonnet)
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    cx, cy = 16, 16

    # Lavender & golden aura
    for r in range(14, 7, -1):
        alpha = int(30 + (15 - r) * 18)
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(230, 190, 255, alpha))

    # Golden-lavender crescent moon shape
    # Outer circle
    draw.ellipse([cx - 10, cy - 10, cx + 10, cy + 10], fill=(255, 235, 170, 255), outline=(220, 160, 240, 255))
    # Cutout with offset ellipse to create sharp crescent
    for x in range(32):
        for y in range(32):
            dx = x - (cx + 4)
            dy = y - cy
            if dx*dx + dy*dy < 8.2*8.2:
                im.putpixel((x, y), (0, 0, 0, 0))

    # Harp strings of celestial light across the crescent hollow
    for y in range(cy - 6, cy + 7, 3):
        for x in range(cx - 5, cx + 2):
            if im.getpixel((x, y))[3] == 0:
                im.putpixel((x, y), (255, 255, 230, 200))

    # Star gem on tip
    draw.point((cx - 3, cy - 10), fill=(255, 255, 255, 255))
    draw.point((cx - 3, cy + 10), fill=(255, 255, 255, 255))
    draw.ellipse([cx - 8, cy - 3, cx - 4, cy + 1], fill=(255, 255, 255, 255), outline=(210, 130, 255, 255))

    im.save("src/main/resources/assets/awakening_system/textures/item/lunar_authority_sonnet.png")
    print("Sonnet texture saved")

def create_canon_texture():
    # 32x32 New Moon Cosmic Void / Amethyst Talisman (Canon)
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    cx, cy = 16, 16

    # Deep violet & indigo cosmic aura
    for r in range(14, 8, -1):
        alpha = int(35 + (15 - r) * 22)
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(130, 60, 220, alpha))

    # Dark eclipse rim
    draw.ellipse([cx - 9, cy - 9, cx + 9, cy + 9], fill=(20, 10, 40, 255), outline=(180, 100, 255, 255))
    # Obsidian inner core
    draw.ellipse([cx - 7, cy - 7, cx + 7, cy + 7], fill=(10, 5, 25, 255))

    # Amethyst glowing eclipse edge ring
    for angle in range(0, 360, 15):
        rad = math.radians(angle)
        r = 7.5
        x = int(cx + r * math.cos(rad))
        y = int(cy + r * math.sin(rad))
        alpha = int(180 + 75 * math.sin(rad * 2))
        draw.point((x, y), fill=(210, 140, 255, alpha))

    # Floating cosmic eye / diamond rune at center
    draw.polygon([(cx, cy - 5), (cx + 4, cy), (cx, cy + 5), (cx - 4, cy)], fill=(170, 90, 255, 255))
    draw.polygon([(cx, cy - 3), (cx + 2, cy), (cx, cy + 3), (cx - 2, cy)], fill=(255, 210, 255, 255))
    draw.point((cx, cy), fill=(255, 255, 255, 255))

    im.save("src/main/resources/assets/awakening_system/textures/item/lunar_authority_canon.png")
    print("Canon texture saved")

def create_guide_texture():
    # 32x32 Sacred Trilunar Chronicle Book / Scroll (Trilunar Guide)
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)

    # Book cover in celestial midnight blue and silver-gold trim
    draw.rectangle([5, 4, 26, 28], fill=(30, 25, 60, 255), outline=(215, 175, 95, 255))
    draw.rectangle([7, 6, 24, 26], fill=(45, 38, 90, 255))

    # Spine on left
    draw.rectangle([5, 4, 8, 28], fill=(20, 15, 45, 255), outline=(215, 175, 95, 255))
    # Spine gold bands
    for y in [7, 12, 17, 22, 26]:
        draw.line([(5, y), (8, y)], fill=(255, 220, 130, 255))

    # Cover art: Three Moon Phases (Full, Crescent, New)
    # Aria (Full moon)
    draw.ellipse([14, 9, 18, 13], fill=(240, 250, 255, 255), outline=(150, 210, 255, 255))
    # Sonnet (Crescent moon)
    draw.ellipse([11, 17, 15, 21], fill=(255, 225, 150, 255))
    draw.ellipse([13, 17, 16, 21], fill=(45, 38, 90, 255))
    # Canon (Eclipse moon)
    draw.ellipse([17, 17, 21, 21], fill=(20, 12, 40, 255), outline=(190, 120, 255, 255))

    # Bookmark ribbon hanging down
    draw.rectangle([15, 27, 17, 30], fill=(220, 140, 255, 255))
    draw.polygon([(15, 30), (16, 28), (17, 30)], fill=(0, 0, 0, 0))

    # Corner gold brackets
    draw.point((9, 6), fill=(255, 230, 140, 255))
    draw.point((23, 6), fill=(255, 230, 140, 255))
    draw.point((9, 25), fill=(255, 230, 140, 255))
    draw.point((23, 25), fill=(255, 230, 140, 255))

    im.save("src/main/resources/assets/awakening_system/textures/item/trilunar_guide.png")
    print("Guide texture saved")

def create_altar_texture():
    # 32x32 Altar Block / Icon
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)

    # Base steps
    draw.rectangle([2, 26, 29, 31], fill=(40, 42, 55, 255), outline=(90, 95, 120, 255))
    draw.rectangle([4, 22, 27, 26], fill=(70, 75, 95, 255), outline=(120, 130, 160, 255))

    # Pedestal body
    draw.rectangle([8, 12, 23, 22], fill=(215, 225, 235, 255), outline=(140, 160, 180, 255))
    draw.rectangle([10, 14, 21, 20], fill=(235, 245, 255, 255))

    # Capital top
    draw.rectangle([4, 8, 27, 12], fill=(180, 195, 215, 255), outline=(240, 245, 255, 255))

    # Floating celestial orb with 3 orbiting moon sparks
    cx, cy = 16, 5
    for r in range(5, 1, -1):
        alpha = int(40 + (6 - r) * 35)
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(200, 160, 255, alpha))
    draw.ellipse([cx - 2, cy - 2, cx + 2, cy + 2], fill=(255, 255, 255, 255))

    # 3 Moon runes on pedestal front
    # Full (Aria)
    draw.ellipse([11, 16, 13, 18], fill=(160, 220, 255, 255))
    # Crescent (Sonnet)
    draw.point((15, 16), fill=(255, 230, 150, 255))
    draw.point((15, 17), fill=(255, 230, 150, 255))
    draw.point((16, 17), fill=(255, 230, 150, 255))
    # New (Canon)
    draw.ellipse([18, 16, 20, 18], fill=(30, 15, 50, 255), outline=(190, 120, 255, 255))

    im.save("src/main/resources/assets/awakening_system/textures/item/trilunar_altar.png")
    im.save("src/main/resources/assets/awakening_system/textures/block/trilunar_altar.png")
    print("Altar textures saved")

if __name__ == "__main__":
    create_aria_texture()
    create_sonnet_texture()
    create_canon_texture()
    create_guide_texture()
    create_altar_texture()
