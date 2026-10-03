import math
from PIL import Image, ImageDraw

def generate_food_textures():
    size = 32

    # 1. ADEPTUS TEMPTATION (Tentación de Adeptus)
    # Ornate royal ceramic pot with golden broth, abalone, shiitake, and steam
    img_adeptus = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d_adeptus = ImageDraw.Draw(img_adeptus)

    # Pot body (lower bowl)
    # Oval bottom: x 6..25, y 18..28
    for y in range(18, 29):
        w = int(math.sqrt(max(0, 1.0 - ((y - 23.5) / 5.5) ** 2)) * 10)
        for x in range(16 - w, 16 + w + 1):
            # Rich royal celadon / bronze ceramic
            shade = (y - 18) / 10.0
            r = int(180 - shade * 60)
            g = int(140 - shade * 50)
            b = int(70 - shade * 30)
            # Gold rim and ornate filigree
            if y == 18 or y == 28 or abs(x - 16) == w:
                r, g, b = 240, 200, 80
            elif y == 23 and abs(x - 16) % 3 == 0:
                r, g, b = 255, 220, 120
            img_adeptus.putpixel((x, y), (r, g, b, 255))

    # Steaming golden broth inside the pot: y 14..18
    for y in range(14, 19):
        w = int(math.sqrt(max(0, 1.0 - ((y - 16.0) / 3.0) ** 2)) * 9)
        for x in range(16 - w, 16 + w + 1):
            # Golden amber broth
            img_adeptus.putpixel((x, y), (235, 175, 45, 255))

    # Ingredients floating:
    # Abalone slice (glistening golden-tan):
    for dx in range(-3, 3):
        for dy in range(-2, 2):
            if dx*dx + dy*dy <= 7:
                img_adeptus.putpixel((13 + dx, 15 + dy), (250, 215, 140, 255))
    # Shiitake mushroom cap (rich dark umber with star cut):
    for dx in range(-3, 3):
        for dy in range(-2, 2):
            if dx*dx + dy*dy <= 7:
                col = (110, 60, 30) if abs(dx) != abs(dy) else (220, 190, 140)
                img_adeptus.putpixel((19 + dx, 15 + dy), (*col, 255))
    # Green scallion / adeptus herb garnish:
    img_adeptus.putpixel((16, 14), (80, 200, 90, 255))
    img_adeptus.putpixel((15, 13), (60, 170, 70, 255))
    img_adeptus.putpixel((17, 13), (90, 220, 100, 255))

    # Steam wisps floating up:
    steam_coords = [
        (13, 10), (12, 8), (14, 6), (13, 4),
        (16, 11), (17, 9), (16, 7), (18, 5), (17, 3),
        (20, 10), (21, 8), (19, 6), (20, 4)
    ]
    for sx, sy in steam_coords:
        img_adeptus.putpixel((sx, sy), (245, 240, 230, 180))

    img_adeptus.save("src/main/resources/assets/awakening_system/textures/item/adeptus_temptation.png")

    # 2. SWEET MADAME (Pollo Dulce)
    # Roasted whole golden chicken with crisp glaze and sweet flower petals
    img_chicken = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    # White ceramic plate
    for y in range(21, 29):
        w = int(math.sqrt(max(0, 1.0 - ((y - 25.0) / 4.0) ** 2)) * 12)
        for x in range(16 - w, 16 + w + 1):
            val = 220 + (y - 21) * 3
            edge = (abs(x - 16) >= w - 1 or y >= 27)
            col = (180, 190, 205) if edge else (val, val, val + 5)
            img_chicken.putpixel((x, y), (*col, 255))

    # Roast chicken body (golden-brown roasted drumsticks and breast)
    for y in range(12, 23):
        w = int(math.sqrt(max(0, 1.0 - ((y - 17.5) / 5.5) ** 2)) * 9)
        for x in range(16 - w, 16 + w + 1):
            t_rad = math.hypot(x - 16, y - 16) / 9.0
            # Honey roasted caramel skin
            if t_rad < 0.4:
                r, g, b = 250, 190, 70 # Crispy golden highlight
            elif t_rad < 0.75:
                r, g, b = 205, 120, 35 # Roasted amber
            else:
                r, g, b = 145, 70, 20  # Deep brown seared crust
            img_chicken.putpixel((x, y), (r, g, b, 255))

    # Drumstick bones with white paper frills / bone ends
    img_chicken.putpixel((8, 14), (245, 240, 230, 255))
    img_chicken.putpixel((7, 13), (255, 250, 240, 255))
    img_chicken.putpixel((6, 12), (245, 235, 220, 255))

    # Sweet Flower petals garnish (bright yellow & white flowers)
    for fx, fy in [(12, 19), (20, 18), (15, 22)]:
        img_chicken.putpixel((fx, fy), (255, 235, 80, 255))
        img_chicken.putpixel((fx+1, fy), (255, 255, 150, 255))
        img_chicken.putpixel((fx, fy+1), (100, 200, 90, 255)) # green leaf

    img_chicken.save("src/main/resources/assets/awakening_system/textures/item/sweet_madame.png")

    # 3. ALMOND TOFU (Tofu de Almendras)
    # Porcelain dish with pristine white diamond tofu cubes in golden sweet syrup
    img_tofu = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    # Elegant blue-and-white porcelain dish
    for y in range(18, 29):
        w = int(math.sqrt(max(0, 1.0 - ((y - 23.5) / 5.5) ** 2)) * 12)
        for x in range(16 - w, 16 + w + 1):
            edge = (abs(x - 16) >= w - 1 or y == 18 or y == 28)
            col = (50, 100, 180) if edge else (235, 240, 250)
            img_tofu.putpixel((x, y), (*col, 255))

    # Sweet osmanthus syrup pool
    for y in range(17, 24):
        w = int(math.sqrt(max(0, 1.0 - ((y - 20.5) / 3.5) ** 2)) * 9)
        for x in range(16 - w, 16 + w + 1):
            img_tofu.putpixel((x, y), (245, 195, 75, 255))

    # 3 Silky Almond Tofu cubes (isometric diamond cut, milky ivory white)
    def draw_tofu_cube(cx, cy):
        # Top face (pure bright white)
        for dx in range(-2, 3):
            for dy in range(-1, 2):
                if abs(dx) + abs(dy) * 2 <= 3:
                    img_tofu.putpixel((cx + dx, cy + dy), (255, 255, 252, 255))
        # Left face (soft cream shadow)
        for dy in range(2, 5):
            for dx in range(-2, 1):
                img_tofu.putpixel((cx + dx, cy + dy), (235, 238, 245, 255))
        # Right face (light shade)
        for dy in range(2, 5):
            for dx in range(1, 3):
                img_tofu.putpixel((cx + dx, cy + dy), (245, 248, 252, 255))

    draw_tofu_cube(12, 15)
    draw_tofu_cube(20, 15)
    draw_tofu_cube(16, 12)

    # Sweet Osmanthus flower blossom on top
    img_tofu.putpixel((16, 11), (255, 220, 60, 255))
    img_tofu.putpixel((15, 11), (255, 180, 40, 255))
    img_tofu.putpixel((17, 11), (255, 180, 40, 255))
    img_tofu.putpixel((16, 10), (255, 80, 60, 255)) # goji berry

    img_tofu.save("src/main/resources/assets/awakening_system/textures/item/almond_tofu.png")

    # 4. SUNSETTIA (Fruta del Ocaso)
    # Plump, juicy tropical fruit with smooth gradient from sunset red/pink to orange/yellow
    img_sunset = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    for y in range(7, 28):
        norm_y = (y - 7) / 21.0
        # Pear/teardrop shape: narrower at top, plump at bottom
        w = int((4.0 + 8.5 * math.sin(norm_y * math.pi * 0.85)))
        for x in range(16 - w, 16 + w + 1):
            norm_x = (x - (16 - w)) / max(1, (2 * w))
            dist_c = math.hypot(norm_x - 0.4, norm_y - 0.4)
            # Radiant sunset color gradient:
            # Top: Golden Honey Yellow -> Mid: Mango Orange -> Bottom: Crimson Sunset Rose
            if norm_y < 0.25:
                r, g, b = 255, 225, 70
            elif norm_y < 0.55:
                t = (norm_y - 0.25) / 0.3
                r = int(255)
                g = int(225 - 90 * t)
                b = int(70 - 30 * t)
            else:
                t = (norm_y - 0.55) / 0.45
                r = int(255 - 40 * t)
                g = int(135 - 90 * t)
                b = int(40 + 40 * t)
            
            # Juicy highlight
            if dist_c < 0.22:
                r = min(255, r + 45)
                g = min(255, g + 40)
                b = min(255, b + 40)
            
            # Subtle edge shading
            if abs(x - 16) >= w - 1 or y == 27:
                r = max(0, r - 50)
                g = max(0, g - 40)
                b = max(0, b - 20)

            img_sunset.putpixel((x, y), (r, g, b, 255))

    # Green leafy stem
    stem = [(16, 6), (16, 5), (17, 4), (18, 4)]
    for sx, sy in stem:
        img_sunset.putpixel((sx, sy), (90, 160, 45, 255))
    # Leaf 1 (left)
    for lx, ly in [(14, 5), (13, 5), (12, 6), (13, 6)]:
        img_sunset.putpixel((lx, ly), (120, 205, 60, 255))
    # Leaf 2 (right)
    for lx, ly in [(18, 5), (19, 5), (20, 6)]:
        img_sunset.putpixel((lx, ly), (105, 185, 50, 255))

    img_sunset.save("src/main/resources/assets/awakening_system/textures/item/sunsettia.png")

    # 5. MANA FRUIT (Fruta de Maná)
    # Luminescent celestial fruit with crystalline facets and sparkling azure energy
    img_mana = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    for y in range(7, 28):
        norm_y = (y - 7) / 21.0
        w = int((4.5 + 8.0 * math.sin(norm_y * math.pi * 0.88)))
        for x in range(16 - w, 16 + w + 1):
            norm_x = (x - (16 - w)) / max(1, (2 * w))
            # Crystalline facets
            facet = (math.sin(x * 1.2) * math.cos(y * 1.2)) * 30.0
            
            # Mystical azure-cyan & violet gradient
            t = norm_y
            r = int(50 + 70 * t + facet * 0.5)
            g = int(180 - 60 * t + facet)
            b = int(255)
            
            # Core inner radiance
            if math.hypot(x - 16, y - 16) < 6.0:
                r = min(255, r + 90)
                g = min(255, g + 60)
                b = 255
            
            r = max(0, min(255, r))
            g = max(0, min(255, g))
            b = max(0, min(255, b))
            img_mana.putpixel((x, y), (r, g, b, 255))

    # Glowing ethereal mana leaves
    for lx, ly in [(15, 6), (14, 5), (13, 4), (16, 5), (17, 5), (18, 4), (19, 3)]:
        img_mana.putpixel((lx, ly), (140, 250, 255, 255))

    # Sparkle motes
    img_mana.putpixel((10, 12), (255, 255, 255, 255))
    img_mana.putpixel((21, 20), (220, 245, 255, 255))
    img_mana.putpixel((13, 23), (200, 240, 255, 255))

    img_mana.save("src/main/resources/assets/awakening_system/textures/item/mana_fruit.png")

    # 6. MANA CHICKEN MEAT & COOKED MANA CHICKEN MEAT
    # Raw:
    img_raw = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    for y in range(10, 26):
        w = int(math.sin((y - 10) / 16.0 * math.pi) * 8.0)
        for x in range(16 - w, 16 + w + 1):
            norm_d = math.hypot(x - 16, y - 18) / 8.0
            r = int(220 - 40 * norm_d)
            g = int(150 + 60 * (1.0 - norm_d))
            b = int(200 + 55 * (1.0 - norm_d)) # cyan/violet mana sheen
            img_raw.putpixel((x, y), (r, g, b, 255))
    # Bone:
    for by in range(6, 12):
        img_raw.putpixel((16, by), (245, 245, 250, 255))
        img_raw.putpixel((17, by), (230, 235, 245, 255))
    img_raw.save("src/main/resources/assets/awakening_system/textures/item/mana_chicken_meat.png")

    # Cooked:
    img_cooked = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    for y in range(10, 26):
        w = int(math.sin((y - 10) / 16.0 * math.pi) * 8.0)
        for x in range(16 - w, 16 + w + 1):
            norm_d = math.hypot(x - 16, y - 18) / 8.0
            r = int(205 - 60 * norm_d)
            g = int(115 - 40 * norm_d)
            b = int(50 + 130 * (1.0 - norm_d)) # roasted gold with blue mana infusion
            img_cooked.putpixel((x, y), (r, g, b, 255))
    for by in range(6, 12):
        img_cooked.putpixel((16, by), (245, 245, 240, 255))
        img_cooked.putpixel((17, by), (220, 215, 205, 255))
    img_cooked.save("src/main/resources/assets/awakening_system/textures/item/cooked_mana_chicken_meat.png")

    print("All custom food textures successfully generated!")

if __name__ == "__main__":
    generate_food_textures()
