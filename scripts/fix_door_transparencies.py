import os
from PIL import Image

def fix_doors():
    base_dir = "src/main/resources/assets/awakening_system/textures/block/"
    
    # 1. Bamboo Door Top: Fill round window with shoji rice paper & bamboo lattice
    bamboo_top_path = os.path.join(base_dir, "bamboo_door_top.png")
    if os.path.exists(bamboo_top_path):
        im = Image.open(bamboo_top_path).convert("RGBA")
        shoji_bg = (244, 238, 220, 255)
        shoji_shadow = (226, 218, 196, 255)
        bamboo_slat = (120, 155, 75, 255)
        
        for y in range(im.height):
            for x in range(im.width):
                p = im.getpixel((x, y))
                if p[3] < 200:
                    # Circular window is roughly around x=6..9, y=4..7
                    if x == 7 or y == 5:
                        im.putpixel((x, y), bamboo_slat) # Bamboo divider cross
                    elif x < 7 and y < 5:
                        im.putpixel((x, y), shoji_bg)
                    else:
                        im.putpixel((x, y), shoji_shadow)
        im.save(bamboo_top_path)
        print("Fixed bamboo_door_top.png")

    # 2. Bamboo Trapdoor: Fill cutouts with woven bamboo
    bamboo_trap_path = os.path.join(base_dir, "bamboo_trapdoor.png")
    if os.path.exists(bamboo_trap_path):
        im = Image.open(bamboo_trap_path).convert("RGBA")
        weave_1 = (162, 198, 109, 255)
        weave_2 = (131, 165, 77, 255)
        for y in range(im.height):
            for x in range(im.width):
                p = im.getpixel((x, y))
                if p[3] < 200:
                    im.putpixel((x, y), weave_1 if (x + y) % 2 == 0 else weave_2)
        im.save(bamboo_trap_path)
        print("Fixed bamboo_trapdoor.png")

    # 3. Sandbearer Door Top & Bottom: Fill empty slits with Shoji paper & golden wood lattice
    shoji_cream = (248, 242, 228, 255)
    shoji_shade = (230, 222, 204, 255)
    wood_divider = (184, 146, 75, 255)
    wood_shade = (138, 101, 42, 255)
    
    for fname in ["sandbearer_door_top.png", "sandbearer_door_bottom.png"]:
        path = os.path.join(base_dir, fname)
        if os.path.exists(path):
            im = Image.open(path).convert("RGBA")
            for y in range(im.height):
                for x in range(im.width):
                    p = im.getpixel((x, y))
                    if p[3] < 200:
                        # Add fine vertical or horizontal paper lattice
                        if x in [7, 8] or y in [5, 11]:
                            im.putpixel((x, y), wood_divider if x == 7 or y == 5 else wood_shade)
                        elif (x + y) % 2 == 0:
                            im.putpixel((x, y), shoji_cream)
                        else:
                            im.putpixel((x, y), shoji_shade)
            im.save(path)
            print(f"Fixed {fname}")

    # 4. Sandbearer Trapdoor
    sb_trap_path = os.path.join(base_dir, "sandbearer_trapdoor.png")
    if os.path.exists(sb_trap_path):
        im = Image.open(sb_trap_path).convert("RGBA")
        for y in range(im.height):
            for x in range(im.width):
                p = im.getpixel((x, y))
                if p[3] < 200:
                    if x % 3 == 0:
                        im.putpixel((x, y), wood_divider)
                    else:
                        im.putpixel((x, y), shoji_cream if y % 2 == 0 else shoji_shade)
        im.save(sb_trap_path)
        print("Fixed sandbearer_trapdoor.png")

    # 5. Cuihua Door Top & Trapdoor: Frosted warm glass panes
    glass_hi = (245, 240, 230, 255)
    glass_mid = (220, 212, 198, 255)
    glass_shadow = (185, 175, 160, 255)
    glass_glint = (255, 255, 255, 255)
    
    for fname in ["cuihua_door_top.png", "cuihua_trapdoor.png"]:
        path = os.path.join(base_dir, fname)
        if os.path.exists(path):
            im = Image.open(path).convert("RGBA")
            for y in range(im.height):
                for x in range(im.width):
                    p = im.getpixel((x, y))
                    if p[3] < 200:
                        # Diagonal glint
                        if (x + y) in [8, 14]:
                            im.putpixel((x, y), glass_glint)
                        elif x < 8 and y < 8:
                            im.putpixel((x, y), glass_hi)
                        elif (x + y) % 2 == 0:
                            im.putpixel((x, y), glass_mid)
                        else:
                            im.putpixel((x, y), glass_shadow)
            im.save(path)
            print(f"Fixed {fname}")

    # 6. Fir Door Top: Warm rustic window behind iron bars
    fir_top_path = os.path.join(base_dir, "fir_door_top.png")
    if os.path.exists(fir_top_path):
        im = Image.open(fir_top_path).convert("RGBA")
        for y in range(im.height):
            for x in range(im.width):
                p = im.getpixel((x, y))
                if p[3] < 200:
                    im.putpixel((x, y), (120, 130, 145, 255) if (x + y) % 2 == 0 else (95, 105, 120, 255))
        im.save(fir_top_path)
        print("Fixed fir_door_top.png")

    # 7. Otogi Trapdoor
    otogi_trap_path = os.path.join(base_dir, "otogi_trapdoor.png")
    if os.path.exists(otogi_trap_path):
        im = Image.open(otogi_trap_path).convert("RGBA")
        for y in range(im.height):
            for x in range(im.width):
                p = im.getpixel((x, y))
                if p[3] < 200:
                    im.putpixel((x, y), (68, 56, 46, 255) if (x + y) % 2 == 0 else (52, 42, 34, 255))
        im.save(otogi_trap_path)
        print("Fixed otogi_trapdoor.png")

if __name__ == "__main__":
    fix_doors()
