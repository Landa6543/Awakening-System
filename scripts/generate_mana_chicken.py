import os
import numpy as np
from PIL import Image

def generate_mana_chicken():
    out_dir = r"src/main/resources/assets/awakening_system/textures/item"
    os.makedirs(out_dir, exist_ok=True)

    # Load vanilla base for pixel geometry
    raw_vanilla = Image.open(r"scratch/vanilla_chicken_raw.png").convert("RGBA")
    ckd_vanilla = Image.open(r"scratch/vanilla_chicken_cooked.png").convert("RGBA")

    # 1. RAW MANA CHICKEN
    # Base silhouette of chicken, but with mana infusion:
    # Pale lavender-pink poultry meat with ethereal cyan veins and white bone.
    raw_arr = np.array(raw_vanilla)
    raw_out = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    for y in range(16):
        for x in range(16):
            r, g, b, a = raw_arr[y, x]
            if a == 0:
                continue
            
            # Check bone pixels near bottom left (x in [1, 2, 3], y in [12, 13, 14, 15])
            is_bone = (x <= 3 and y >= 11 and (r > 200 and g > 180 and b > 140))
            if is_bone:
                # Polished bone
                raw_out.putpixel((x, y), (240, 245, 255, 255))
                continue

            luma = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            
            # Mana-infused raw poultry: tender rosy lilac with soft cyan glow
            # Outline
            if r < 120 and g < 70 and b < 60:
                # Dark edge/crevice
                cr, cg, cb = 75, 45, 85
            else:
                # Flesh shading
                cr = int(140 + luma * 105)
                cg = int(125 + luma * 115)
                cb = int(175 + luma * 80)
            
            # Subtle glowing cyan mana marbling at select coordinates
            if (x == 6 and y == 7) or (x == 7 and y == 8) or (x == 10 and y == 4) or (x == 11 and y == 5):
                cr, cg, cb = 120, 240, 255
            elif (x == 5 and y == 6) or (x == 9 and y == 5):
                cr, cg, cb = 180, 220, 255

            raw_out.putpixel((x, y), (cr, cg, cb, 255))

    raw_path = os.path.join(out_dir, "mana_chicken_meat.png")
    raw_out.save(raw_path)

    # 2. COOKED MANA CHICKEN
    # Golden, savory roasted chicken drumstick with caramelized skin,
    # tender juicy meat highlight, and glowing cyan spice/aroma speckles!
    ckd_arr = np.array(ckd_vanilla)
    ckd_out = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    for y in range(16):
        for x in range(16):
            r, g, b, a = ckd_arr[y, x]
            if a == 0:
                continue

            is_bone = (x <= 3 and y >= 11 and (r > 190 and g > 170 and b > 130))
            if is_bone:
                ckd_out.putpixel((x, y), (245, 240, 230, 255))
                continue

            luma = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0

            # Outline / burnt crispy skin
            if r < 100 and g < 55:
                cr, cg, cb = 75, 30, 15
            elif luma < 0.45:
                # Deep roasted caramel skin
                cr, cg, cb = 150, 68, 25
            elif luma < 0.75:
                # Golden brown roast meat
                cr, cg, cb = 205, 115, 42
            else:
                # Sizzling golden crispy highlight
                cr, cg, cb = 245, 175, 75

            # Mana seasoning sparkles (ethereal starlight herb specks)
            if (x == 7 and y == 5) or (x == 11 and y == 6) or (x == 8 and y == 9):
                cr, cg, cb = 100, 240, 255
            elif (x == 8 and y == 6) or (x == 10 and y == 8):
                cr, cg, cb = 180, 255, 255

            ckd_out.putpixel((x, y), (cr, cg, cb, 255))

    ckd_path = os.path.join(out_dir, "cooked_mana_chicken_meat.png")
    ckd_out.save(ckd_path)

    print(f"Generated authentic Mana Chicken textures:\n  {raw_path}\n  {ckd_path}")

    # Create showcase
    scale = 8
    sc = Image.new("RGBA", (16 * scale * 2 + 30, 16 * scale + 20), (25, 28, 35, 255))
    sc.paste(raw_out.resize((16 * scale, 16 * scale), Image.NEAREST), (10, 10))
    sc.paste(ckd_out.resize((16 * scale, 16 * scale), Image.NEAREST), (16 * scale + 20, 10))
    sc.save("scratch/mana_chicken_showcase.png")
    print("Saved showcase to scratch/mana_chicken_showcase.png")

if __name__ == "__main__":
    generate_mana_chicken()
