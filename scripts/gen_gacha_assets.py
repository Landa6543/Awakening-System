import os
from PIL import Image, ImageDraw

TEX_DIR = "src/main/resources/assets/awakening_system/textures/item"
ENTITY_TEX_DIR = "src/main/resources/assets/awakening_system/textures/entity"
MODELS_DIR = "src/main/resources/assets/awakening_system/models/item"

os.makedirs(TEX_DIR, exist_ok=True)
os.makedirs(ENTITY_TEX_DIR, exist_ok=True)
os.makedirs(MODELS_DIR, exist_ok=True)

# 1. Primogem (16x16)
img_primo = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
# Prismatic 4-point diamond star
for y in range(16):
    for x in range(16):
        dx = abs(x - 7.5)
        dy = abs(y - 7.5)
        # Star equation
        if (dx * dy <= 2.8) and (dx + dy <= 7.0):
            # Gradient: center cyan/white, tips purple/pink
            dist = dx + dy
            if dist < 2.0:
                img_primo.putpixel((x, y), (255, 255, 255, 255))
            elif dist < 4.0:
                img_primo.putpixel((x, y), (120, 230, 255, 255))
            elif dist < 5.5:
                img_primo.putpixel((x, y), (190, 130, 255, 255))
            else:
                img_primo.putpixel((x, y), (240, 90, 210, 255))
        elif (dx * dy <= 4.2) and (dx + dy <= 7.8):
            img_primo.putpixel((x, y), (80, 40, 140, 200))
img_primo.save(os.path.join(TEX_DIR, "primogem.png"))

# 2. Intertwined Fate (16x16)
img_inter = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
draw = ImageDraw.Draw(img_inter)
# Cosmic sphere with rainbow orbit
draw.ellipse([3, 3, 12, 12], fill=(220, 100, 180, 255), outline=(255, 215, 0, 255))
draw.ellipse([5, 5, 10, 10], fill=(130, 210, 255, 255))
draw.point((7, 7), fill=(255, 255, 255, 255))
draw.point((8, 7), fill=(255, 255, 255, 255))
# Golden star points
draw.point((1, 7), fill=(255, 230, 100, 255))
draw.point((14, 7), fill=(255, 230, 100, 255))
draw.point((7, 1), fill=(255, 230, 100, 255))
draw.point((7, 14), fill=(255, 230, 100, 255))
img_inter.save(os.path.join(TEX_DIR, "intertwined_fate.png"))

# 3. Acquaint Fate (16x16)
img_acq = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
draw = ImageDraw.Draw(img_acq)
draw.ellipse([3, 3, 12, 12], fill=(70, 150, 240, 255), outline=(180, 220, 255, 255))
draw.ellipse([5, 5, 10, 10], fill=(200, 240, 255, 255))
draw.point((7, 7), fill=(255, 255, 255, 255))
draw.point((8, 7), fill=(255, 255, 255, 255))
draw.point((2, 7), fill=(180, 230, 255, 255))
draw.point((13, 7), fill=(180, 230, 255, 255))
draw.point((7, 2), fill=(180, 230, 255, 255))
draw.point((7, 13), fill=(180, 230, 255, 255))
img_acq.save(os.path.join(TEX_DIR, "acquaint_fate.png"))

# 4. Staff of Homa (16x16)
img_homa = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
# Crimson shaft diagonally
for i in range(16):
    img_homa.putpixel((i, 15 - i), (160, 20, 30, 255))
# Golden collars
img_homa.putpixel((4, 11), (230, 180, 50, 255))
img_homa.putpixel((11, 4), (230, 180, 50, 255))
# Fiery trident spear head at top right
img_homa.putpixel((13, 1), (255, 80, 40, 255))
img_homa.putpixel((14, 2), (255, 120, 50, 255))
img_homa.putpixel((15, 0), (255, 220, 120, 255))
img_homa.putpixel((12, 0), (220, 30, 30, 255))
img_homa.putpixel((15, 3), (220, 30, 30, 255))
img_homa.putpixel((12, 2), (255, 160, 60, 255))
img_homa.putpixel((13, 3), (255, 160, 60, 255))
# Pommel
img_homa.putpixel((0, 15), (230, 180, 50, 255))
img_homa.putpixel((1, 14), (200, 30, 30, 255))
img_homa.save(os.path.join(TEX_DIR, "staff_of_homa.png"))

# 5. Light Cone: Night on the Milky Way (16x16)
img_lc = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
draw = ImageDraw.Draw(img_lc)
# Slanted rectangular card frame
draw.polygon([(2, 1), (13, 1), (13, 14), (2, 14)], fill=(15, 10, 35, 255), outline=(230, 190, 70, 255))
# Galaxy nebula inside
draw.rectangle([4, 3, 11, 12], fill=(45, 20, 75, 255))
# Star streak
draw.line([(4, 11), (10, 4)], fill=(255, 220, 140, 255), width=1)
draw.point((7, 7), fill=(255, 255, 255, 255))
draw.point((9, 5), fill=(255, 90, 120, 255))
img_lc.save(os.path.join(TEX_DIR, "light_cone_milky_way.png"))

# 6. Command Spell (16x16)
img_cs = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
draw = ImageDraw.Draw(img_cs)
# Red runic wings and center sword
draw.line([(7, 2), (7, 13)], fill=(220, 20, 40, 255), width=2)
draw.line([(4, 5), (10, 5)], fill=(220, 20, 40, 255), width=2)
# Wing curves
draw.arc([2, 4, 13, 12], start=180, end=0, fill=(255, 50, 70, 255), width=1)
draw.point((3, 3), fill=(255, 80, 100, 255))
draw.point((12, 3), fill=(255, 80, 100, 255))
draw.point((7, 1), fill=(255, 180, 190, 255))
img_cs.save(os.path.join(TEX_DIR, "command_spell.png"))

# 7-10. Visions (Pyro, Electro, Cryo, Anemo)
def make_vision(filename, bg_color, gem_color, symbol_color):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Circular medallion frame with wings
    draw.ellipse([3, 3, 12, 12], fill=bg_color, outline=(230, 200, 80, 255))
    draw.ellipse([5, 5, 10, 10], fill=gem_color)
    draw.point((7, 7), fill=symbol_color)
    draw.point((8, 7), fill=symbol_color)
    draw.point((7, 8), fill=symbol_color)
    draw.point((1, 7), fill=(230, 200, 80, 255))
    draw.point((14, 7), fill=(230, 200, 80, 255))
    draw.point((7, 1), fill=(230, 200, 80, 255))
    img.save(os.path.join(TEX_DIR, filename))

make_vision("vision_pyro.png", (140, 30, 20, 255), (240, 80, 30, 255), (255, 230, 120, 255))
make_vision("vision_electro.png", (70, 20, 110, 255), (170, 60, 240, 255), (255, 200, 255, 255))
make_vision("vision_cryo.png", (30, 80, 130, 255), (90, 190, 245, 255), (230, 255, 255, 255))
make_vision("vision_anemo.png", (20, 100, 70, 255), (60, 230, 170, 255), (210, 255, 230, 255))

# 11. Paimon Entity Texture (64x32)
img_paimon = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
draw = ImageDraw.Draw(img_paimon)
# Head (0,0 to 28,14): White hair
draw.rectangle([0, 0, 28, 14], fill=(245, 245, 250, 255))
# Face front (8, 7 to 15, 13): Soft pale skin
draw.rectangle([8, 7, 15, 13], fill=(255, 232, 218, 255))
# Eyes: Deep indigo anime eyes
draw.point((9, 10), fill=(30, 40, 90, 255))
draw.point((14, 10), fill=(30, 40, 90, 255))
draw.point((9, 9), fill=(90, 130, 230, 255))
draw.point((14, 9), fill=(90, 130, 230, 255))
# Blush
draw.point((8, 11), fill=(255, 180, 180, 255))
draw.point((15, 11), fill=(255, 180, 180, 255))
# Tiara / Crown (28,0 to 60,8): Dark midnight blue with gold trim
draw.rectangle([28, 0, 56, 4], fill=(20, 25, 45, 255))
draw.line([(28, 4), (56, 4)], fill=(235, 195, 75, 255))
draw.point((42, 2), fill=(255, 220, 100, 255))

# Torso (0,14 to 20,24): White jumpsuit with gold accents
draw.rectangle([0, 14, 20, 24], fill=(250, 250, 255, 255))
draw.line([(0, 18), (20, 18)], fill=(230, 190, 70, 255))
draw.rectangle([8, 15, 12, 17], fill=(30, 35, 60, 255))

# Cape (24,14 to 38,24): Dark starry blue
draw.rectangle([24, 14, 38, 24], fill=(15, 20, 40, 255))
# Star dots on cape
draw.point((26, 16), fill=(255, 220, 100, 255))
draw.point((32, 19), fill=(255, 255, 255, 255))
draw.point((36, 16), fill=(255, 220, 100, 255))
draw.point((29, 22), fill=(255, 220, 100, 255))

# Arms (40,14 to 56,24)
draw.rectangle([40, 14, 48, 22], fill=(250, 250, 255, 255))
draw.rectangle([40, 22, 48, 24], fill=(255, 232, 218, 255)) # Hands
draw.rectangle([48, 14, 56, 22], fill=(250, 250, 255, 255))
draw.rectangle([48, 22, 56, 24], fill=(255, 232, 218, 255))

# Legs (0,24 to 16,32): White boots with gold toes
draw.rectangle([0, 24, 16, 30], fill=(240, 240, 248, 255))
draw.rectangle([0, 30, 16, 32], fill=(230, 190, 70, 255))

img_paimon.save(os.path.join(ENTITY_TEX_DIR, "paimon.png"))

print("All textures generated successfully!")
