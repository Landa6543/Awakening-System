import os
import random
from PIL import Image

def generate_plum_log():
    # 1. Spirit Plum Log Side (16x16): Vertical bark striations like Cherry/Dark Oak
    # Palette: Noble deep plum bark
    bark_deep = (48, 30, 38, 255)
    bark_shadow = (68, 44, 54, 255)
    bark_mid = (92, 60, 72, 255)
    bark_hi = (118, 80, 94, 255)
    bark_lenticel = (142, 100, 114, 255)
    
    img = Image.new("RGBA", (16, 16), bark_mid)
    # Generate vertical columns with natural bark waviness
    for x in range(16):
        # Vertical bark grain
        base_col = bark_mid if x % 3 != 0 else bark_shadow
        for y in range(16):
            # Column-based noise with vertical continuity
            n = (x * 7 + y * 13 + (x ^ y)) % 11
            if x in [2, 6, 11, 15]:
                # Crevices between bark strips
                c = bark_deep if (y % 4 != 0) else bark_shadow
            elif n < 3:
                c = bark_hi
            elif n < 7:
                c = bark_mid
            elif n < 9:
                c = bark_shadow
            else:
                c = bark_lenticel if (y % 5 == 0) else bark_mid
            img.putpixel((x, y), c)
    return img

def generate_plum_log_top():
    # Top Cross-Section: Thick outer plum bark, light plum/sakura heartwood with natural growth rings
    bark_outer = (48, 30, 38, 255)
    bark_inner = (68, 44, 54, 255)
    
    wood_light = (235, 192, 202, 255)
    wood_mid = (212, 160, 172, 255)
    wood_ring = (182, 126, 138, 255)
    wood_pith = (156, 100, 112, 255)

    img = Image.new("RGBA", (16, 16), wood_mid)
    import math
    for y in range(16):
        for x in range(16):
            # Distance from center (7.5, 7.5) with slight organic distortion
            dx = x - 7.5
            dy = y - 7.5
            angle = math.atan2(dy, dx)
            dist = math.hypot(dx, dy) + 0.3 * math.sin(angle * 4)
            
            if dist >= 7.0:
                c = bark_outer
            elif dist >= 6.0:
                c = bark_inner
            elif dist <= 1.2:
                c = wood_pith # Center pith
            elif 2.8 <= dist <= 3.4 or 4.6 <= dist <= 5.2:
                c = wood_ring # Growth rings
            elif dist < 3.0:
                c = wood_light if (x + y) % 2 == 0 else wood_mid
            else:
                c = wood_mid if (x * y) % 3 == 0 else wood_light
            img.putpixel((x, y), c)
    return img

def generate_pine_log():
    # 2. Celestial Pine Log Side (16x16): Vertical pine bark plates like Spruce/Pine
    pine_deep = (44, 30, 20, 255)
    pine_shadow = (65, 46, 32, 255)
    pine_mid = (90, 66, 48, 255)
    pine_hi = (118, 88, 65, 255)
    pine_peak = (140, 108, 80, 255)

    img = Image.new("RGBA", (16, 16), pine_mid)
    for x in range(16):
        for y in range(16):
            # Vertical bark ridges
            if x in [1, 5, 9, 13]:
                c = pine_deep if y % 3 != 0 else pine_shadow
            else:
                val = (x * 11 + y * 7 + (x * 3) ^ y) % 10
                if val < 2:
                    c = pine_peak
                elif val < 5:
                    c = pine_hi
                elif val < 8:
                    c = pine_mid
                else:
                    c = pine_shadow
            img.putpixel((x, y), c)
    return img

def generate_pine_log_top():
    # Top Cross-Section: Dark spruce-like bark, warm amber-pine growth rings
    bark_outer = (44, 30, 20, 255)
    bark_inner = (65, 46, 32, 255)
    
    wood_light = (205, 165, 115, 255)
    wood_mid = (178, 136, 88, 255)
    wood_ring = (145, 105, 62, 255)
    wood_pith = (118, 82, 45, 255)

    img = Image.new("RGBA", (16, 16), wood_mid)
    import math
    for y in range(16):
        for x in range(16):
            dx = x - 7.5
            dy = y - 7.5
            angle = math.atan2(dy, dx)
            dist = math.hypot(dx, dy) + 0.25 * math.cos(angle * 5)
            
            if dist >= 7.0:
                c = bark_outer
            elif dist >= 6.0:
                c = bark_inner
            elif dist <= 1.2:
                c = wood_pith
            elif 2.8 <= dist <= 3.3 or 4.5 <= dist <= 5.1:
                c = wood_ring
            elif dist < 3.0:
                c = wood_light if (x + y) % 2 == 0 else wood_mid
            else:
                c = wood_mid if (x * 2 + y) % 3 == 0 else wood_light
            img.putpixel((x, y), c)
    return img

def main():
    base_dir = "src/main/resources/assets/awakening_system/textures/block/"
    
    plum_side = generate_plum_log()
    plum_top = generate_plum_log_top()
    pine_side = generate_pine_log()
    pine_top = generate_pine_log_top()
    
    plum_side.save(os.path.join(base_dir, "spirit_plum_log.png"))
    plum_top.save(os.path.join(base_dir, "spirit_plum_log_top.png"))
    pine_side.save(os.path.join(base_dir, "celestial_pine_log.png"))
    pine_top.save(os.path.join(base_dir, "celestial_pine_log_top.png"))
    
    print("Successfully generated vanilla-style log textures for Plum and Pine!")

if __name__ == "__main__":
    main()
