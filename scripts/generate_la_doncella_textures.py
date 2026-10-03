import math
import numpy as np
from PIL import Image, ImageDraw

def create_la_doncella_textures():
    size = 128
    
    # Diagonal axis: 45 degrees
    # Base hilt near (22, 106) -> Blade tip near (112, 16)
    sqrt2 = math.sqrt(2.0)
    ux, uy = 1.0 / sqrt2, -1.0 / sqrt2   # along sword axis
    nx, ny = -1.0 / sqrt2, -1.0 / sqrt2  # perpendicular (positive = edge/top-left, negative = spine/bottom-right)

    def to_xy(s, w):
        base_x, base_y = 22.0, 106.0
        return base_x + s * ux + w * nx, base_y + s * uy + w * ny

    img_pure = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    img_trans = Image.new("RGBA", (size, size), (0, 0, 0, 0))

    # =========================================================
    # DETAILED ROSE DRAWING FUNCTION
    # =========================================================
    def render_detailed_rose(img, cx, cy, radius, mode="silver", seed=0):
        for dy in range(-radius - 2, radius + 3):
            for dx in range(-radius - 2, radius + 3):
                dist = math.hypot(dx, dy)
                if dist > radius:
                    continue
                
                ang = (math.atan2(dy, dx) + seed) % (2.0 * math.pi)
                norm_d = dist / radius
                
                l1 = math.cos(5.0 * ang)
                l2 = math.cos(5.0 * ang + 1.25)
                l3 = math.cos(3.0 * ang + dist * 0.85)
                
                crevice = False
                if norm_d > 0.62 and l1 < -0.65:
                    crevice = True
                elif 0.32 <= norm_d <= 0.72 and l2 < -0.6:
                    crevice = True
                elif 0.12 <= norm_d < 0.42 and l3 < -0.55:
                    crevice = True

                curl = 0.0
                if norm_d > 0.65 and l1 > 0.45:
                    curl = (l1 - 0.45) * 2.0
                elif 0.35 <= norm_d <= 0.65 and l2 > 0.35:
                    curl = (l2 - 0.35) * 1.8
                elif norm_d < 0.35 and l3 > 0.3:
                    curl = (l3 - 0.3) * 1.5

                px = int(round(cx + dx))
                py = int(round(cy + dy))
                if not (0 <= px < size and 0 <= py < size):
                    continue

                if mode == "silver":
                    if crevice:
                        r, g, b = 65, 70, 85
                    else:
                        base = 155 + int(70 * (1.0 - norm_d * 0.4)) + int(35 * curl)
                        r = min(255, base - 5)
                        g = min(255, base)
                        b = min(255, base + 15)
                        if dist < 2.2:
                            r, g, b = 235, 215, 185
                    if dist > radius - 1.2:
                        r, g, b = 48, 52, 65
                    img.putpixel((px, py), (r, g, b, 255))

                elif mode == "engraved":
                    orig = img.getpixel((px, py))
                    if orig[3] > 0:
                        if crevice:
                            r = max(20, orig[0] - 45)
                            g = max(20, orig[1] - 45)
                            b = max(25, orig[2] - 40)
                        else:
                            emboss = int(50 * curl + 22 * (1.0 - norm_d))
                            r = min(255, orig[0] + emboss)
                            g = min(255, orig[1] + emboss + 6)
                            b = min(255, orig[2] + emboss + 15)
                        img.putpixel((px, py), (r, g, b, 255))

                elif mode == "crimson":
                    if crevice:
                        r, g, b = 18, 3, 8
                    elif dist > radius - 1.2:
                        r, g, b = 35, 4, 12
                    else:
                        base_r = int(110 + 130 * (1.0 - norm_d * 0.3) + 35 * curl)
                        base_g = int(12 + 22 * (1.0 - norm_d))
                        base_b = int(24 + 28 * (1.0 - norm_d))
                        if dist < 2.5:
                            r, g, b = 255, 80, 115
                        else:
                            r = min(255, max(28, base_r))
                            g = min(255, max(4, base_g))
                            b = min(255, max(8, base_b))
                    img.putpixel((px, py), (r, g, b, 255))

    # =========================================================
    # 1. HILT & GRIP & POMMEL
    # =========================================================
    for s in np.linspace(13, 33, 120):
        for w in np.linspace(-3.2, 3.2, 30):
            norm_w = (w + 3.2) / 6.4
            r = int(28 + 25 * norm_w)
            g = int(28 + 22 * norm_w)
            b = int(35 + 30 * norm_w)
            d1 = (s * 0.75 + w * 0.9) % 5.0
            d2 = (s * 0.75 - w * 0.9) % 5.0
            if d1 < 1.1 or d2 < 1.1:
                r, g, b = 190, 200, 215
                if abs(w) < 1.4:
                    r, g, b = 230, 235, 250
            x, y = to_xy(s, w)
            ix, iy = int(round(x)), int(round(y))
            if 0 <= ix < size and 0 <= iy < size:
                img_pure.putpixel((ix, iy), (r, g, b, 255))
                tr = min(255, int(r * 0.45 + 40))
                tg = int(g * 0.2)
                tb = int(b * 0.25 + 10)
                img_trans.putpixel((ix, iy), (tr, tg, tb, 255))

    # Guard: Wide rectangular bolster
    for s in np.linspace(33, 38, 40):
        for w in np.linspace(-11.5, 11.5, 90):
            t_w = abs(w) / 11.5
            t_s = (s - 33) / 5.0
            val = 145 - int(35 * t_w) + int(50 * t_s)
            r, g, b = val - 8, val, val + 15
            if abs(w) > 10.0 or s < 34.0 or s > 37.0:
                r, g, b = 215, 220, 235
            elif abs(abs(w) - 7.5) < 0.6:
                r, g, b = 60, 65, 80

            x, y = to_xy(s, w)
            ix, iy = int(round(x)), int(round(y))
            if 0 <= ix < size and 0 <= iy < size:
                img_pure.putpixel((ix, iy), (r, g, b, 255))
                tr = min(255, int(val * 0.35 + 40))
                tg = int(val * 0.18)
                tb = int(val * 0.2)
                if abs(w) > 9.5 or s < 34.2 or s > 36.8:
                    tr, tg, tb = 220, 35, 65
                img_trans.putpixel((ix, iy), (tr, tg, tb, 255))

    # Pommel Rose
    pom_x, pom_y = to_xy(5.5, 0.0)
    render_detailed_rose(img_pure, pom_x, pom_y, radius=8, mode="silver", seed=0.5)
    render_detailed_rose(img_trans, pom_x, pom_y, radius=9, mode="crimson", seed=0.8)

    # =========================================================
    # 2. COLOSSAL BUSTER SWORD BLADE
    # =========================================================
    blade_mask = np.zeros((size, size), dtype=bool)

    for s_step in np.linspace(38, 125, 700):
        w_min = -9.5 # spine (straight)
        
        if s_step <= 104.0:
            w_max = 12.0
        else:
            t_tip = (s_step - 104.0) / (125.0 - 104.0)
            w_max = 12.0 - t_tip * (12.0 - (-9.5))

        if w_max <= w_min:
            continue

        for w_step in np.linspace(w_min, w_max, 150):
            x, y = to_xy(s_step, w_step)
            ix, iy = int(round(x)), int(round(y))
            if 0 <= ix < size and 0 <= iy < size:
                blade_mask[ix, iy] = True
                
                w_norm = (w_step - w_min) / (w_max - w_min)
                
                if w_norm < 0.16:
                    t = w_norm / 0.16
                    val = 55 + int(65 * t)
                    r, g, b = val - 10, val, val + 16
                elif w_norm < 0.78:
                    t = (w_norm - 0.16) / (0.78 - 0.16)
                    sheen = math.sin(s_step * 0.35 + w_step * 1.8) * 14.0
                    val = 150 + int(30 * t + sheen)
                    r, g, b = val - 12, val, val + 18
                else:
                    t = (w_norm - 0.78) / (1.0 - 0.78)
                    val = 195 + int(60 * t)
                    r, g, b = val - 4, val, val + 6

                if w_step < w_min + 0.6 or w_step > w_max - 0.6 or s_step > 124.0:
                    r, g, b = 38, 42, 52

                r = max(0, min(255, r))
                g = max(0, min(255, g))
                b = max(0, min(255, b))
                
                img_pure.putpixel((ix, iy), (r, g, b, 255))

                # Transformed base blade: dark ash steel
                tr = max(0, min(255, int(r * 0.40 + 35)))
                tg = max(0, min(255, int(g * 0.28 + 8)))
                tb = max(0, min(255, int(b * 0.32 + 12)))
                if w_norm > 0.85: # glowing crimson sharp edge
                    tr = min(255, tr + 120)
                    tg = int(tg * 0.35)
                    tb = int(tb * 0.35)
                img_trans.putpixel((ix, iy), (tr, tg, tb, 255))

    # =========================================================
    # 3. PURE FORM: ENGRAVED ROSES ON THE BLADE
    # =========================================================
    for s_curv in np.linspace(42, 108, 400):
        w1 = math.sin(s_curv * 0.18) * 4.5 + 0.5
        w2 = math.cos(s_curv * 0.18 + 0.8) * 4.0 - 0.5
        for w_v in (w1, w2):
            x, y = to_xy(s_curv, w_v)
            ix, iy = int(round(x)), int(round(y))
            if 0 <= ix < size and 0 <= iy < size and blade_mask[ix, iy]:
                orig = img_pure.getpixel((ix, iy))
                img_pure.putpixel((ix, iy), (min(255, orig[0] + 45), min(255, orig[1] + 50), min(255, orig[2] + 60), 255))

    pure_roses = [
        (47.0, 1.0, 7, 0.2),
        (58.0, -2.0, 8, 1.1),
        (70.0, 2.5, 9, 2.3),
        (83.0, -1.0, 8, 0.7),
        (96.0, 1.5, 7, 1.8),
        (108.0, -2.5, 6, 2.9),
    ]

    for s_r, w_r, rad, ang in pure_roses:
        rx, ry = to_xy(s_r, w_r)
        render_detailed_rose(img_pure, rx, ry, radius=rad, mode="engraved", seed=ang)

    # =========================================================
    # 4. TRANSFORMED FORM: BRIARS, JAGGED THORNS & BLOOD ROSES
    # Thick thorny roots and dramatic jutting thorn spikes!
    # =========================================================

    # Fissures / ash cracks
    for s_crack in np.linspace(40, 120, 500):
        wc = math.sin(s_crack * 0.3) * 5.0 + math.cos(s_crack * 0.12) * 2.0
        x, y = to_xy(s_crack, wc)
        ix, iy = int(round(x)), int(round(y))
        for ox in range(-1, 2):
            for oy in range(-1, 2):
                px, py = ix + ox, iy + oy
                if 0 <= px < size and 0 <= py < size and blade_mask[px, py]:
                    if abs(ox) + abs(oy) == 0:
                        img_trans.putpixel((px, py), (255, 75, 100, 255))
                    else:
                        img_trans.putpixel((px, py), (180, 28, 48, 255))

    # Thick bramble roots wrapping the blade
    for s_root in np.linspace(35, 120, 700):
        w_b1 = math.sin(s_root * 0.14) * 8.5 - 0.5
        w_b2 = math.cos(s_root * 0.14 + 1.3) * 8.0 + 1.0
        w_b3 = math.sin(s_root * 0.20 + 2.2) * 6.5 - 1.2
        
        for w_curr in (w_b1, w_b2, w_b3):
            thick = 2.4 + 0.8 * math.sin(s_root * 0.32)
            for dw in np.linspace(-thick, thick, 14):
                x, y = to_xy(s_root, w_curr + dw)
                ix, iy = int(round(x)), int(round(y))
                if 0 <= ix < size and 0 <= iy < size:
                    t = (dw + thick) / (2.0 * thick)
                    bark_r = int(18 + 35 * t)
                    bark_g = int(4 + 12 * t)
                    bark_b = int(8 + 16 * t)
                    if abs(dw) < 0.7:
                        bark_r, bark_g, bark_b = 95, 18, 35
                    img_trans.putpixel((ix, iy), (bark_r, bark_g, bark_b, 255))

    # MASSIVE JAGGED THORNS / SPINES (Directly matched to friend's drawing!)
    # On the drawing:
    # Right side (spine): prominent, giant thorn spikes sweeping out and back!
    # Left side (edge): vicious thorns jutting outward!
    thorns = [
        # Spine side (w = -9.5): (s_base, w_base, length, angle_degrees, base_width)
        (45.0, -9.5, 16.0, -65, 4.5),
        (54.0, -9.5, 24.0, -78, 6.0),
        (65.0, -9.5, 32.0, -84, 7.5),
        (76.0, -9.5, 36.0, -88, 8.0),
        (88.0, -9.5, 28.0, -78, 6.5),
        (98.0, -9.5, 22.0, -82, 5.5),
        (108.0, -9.5, 18.0, -85, 5.0),
        (116.0, -9.5, 14.0, -80, 4.0),

        # Edge side (w = +12.0):
        (42.0, 12.0, 17.0, 62, 5.0),
        (52.0, 12.0, 26.0, 72, 6.5),
        (63.0, 12.0, 34.0, 78, 7.5),
        (74.0, 12.0, 38.0, 82, 8.5),
        (85.0, 12.0, 30.0, 74, 7.0),
        (95.0, 12.0, 25.0, 68, 6.0),
        (105.0, 10.0, 20.0, 60, 5.0),
        (114.0, 3.0, 16.0, 50, 4.5),
        (122.0, -4.0, 14.0, 40, 4.0),

        # Guard & ricasso spikes
        (33.0, -11.5, 12.0, -45, 4.0),
        (33.0, 11.5, 12.0, 45, 4.0),
        (38.0, -8.5, 14.0, -60, 4.5),
        (38.0, 10.5, 14.0, 60, 4.5),
    ]

    for s_t, w_t, t_len, ang_deg, base_w in thorns:
        ang_rad = math.radians(ang_deg)
        tx_dir = math.sin(ang_rad) * ux + math.cos(ang_rad) * nx
        ty_dir = math.sin(ang_rad) * uy + math.cos(ang_rad) * ny
        px_dir = -ty_dir
        py_dir = tx_dir
        
        base_x, base_y = to_xy(s_t, w_t)
        
        for step in np.linspace(0, t_len, 60):
            # Thorn curvature
            curve = math.sin(step / t_len * 1.5) * 1.5
            curr_x = base_x + tx_dir * step + px_dir * curve
            curr_y = base_y + ty_dir * step + py_dir * curve
            t_ratio = step / t_len
            cur_w = base_w * (1.0 - t_ratio)
            
            for side in np.linspace(-cur_w, cur_w, 24):
                ix = int(round(curr_x + px_dir * side))
                iy = int(round(curr_y + py_dir * side))
                if 0 <= ix < size and 0 <= iy < size:
                    if t_ratio > 0.85:
                        r, g, b = 255, 55, 85
                    elif abs(side) > cur_w * 0.65:
                        r, g, b = 185, 28, 52
                    else:
                        r, g, b = 20, 5, 10
                    img_trans.putpixel((ix, iy), (r, g, b, 255))

    # Blood-red blooming roses along the brambles
    trans_roses = [
        (48.0, 1.0, 8, 0.4),
        (60.0, -1.5, 10, 1.2),
        (73.0, 2.0, 11, 2.1),
        (86.0, -1.0, 10, 0.9),
        (99.0, 1.0, 9, 1.7),
        (110.0, -2.0, 7, 2.8),
    ]

    for s_r, w_r, rad, ang in trans_roses:
        rx, ry = to_xy(s_r, w_r)
        render_detailed_rose(img_trans, rx, ry, radius=rad, mode="crimson", seed=ang)

    # Save final textures
    out_pure_path = "src/main/resources/assets/awakening_system/textures/item/la_doncella.png"
    out_trans_path = "src/main/resources/assets/awakening_system/textures/item/la_doncella_transformed.png"

    img_pure.save(out_pure_path)
    img_trans.save(out_trans_path)
    print(f"Updated: {out_pure_path}")
    print(f"Updated: {out_trans_path}")

    # Generate enhanced showcase image
    showcase = Image.new("RGBA", (size * 2 + 80, size + 40), (14, 11, 18, 255))
    showcase.paste(img_pure, (25, 20), img_pure)
    showcase.paste(img_trans, (size + 55, 20), img_trans)
    
    artifact_path = "C:/Users/soybe/.gemini/antigravity/brain/930e4101-5310-48ee-9b53-1a9777f45603/la_doncella_showcase.png"
    showcase.save(artifact_path)
    print(f"Showcase saved to: {artifact_path}")

if __name__ == "__main__":
    create_la_doncella_textures()
