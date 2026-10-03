import math
import numpy as np
from PIL import Image, ImageDraw

def generate_mahiro_espadon_textures():
    size = 128
    
    # 45-degree diagonal axis
    # Origin base near (20, 108) -> Tip near (114, 14)
    sqrt2 = math.sqrt(2.0)
    ux, uy = 1.0 / sqrt2, -1.0 / sqrt2   # along sword axis
    nx, ny = -1.0 / sqrt2, -1.0 / sqrt2  # perpendicular (positive = edge/top-left, negative = spine/bottom-right)

    def to_xy(s, w):
        base_x, base_y = 20.0, 108.0
        return base_x + s * ux + w * nx, base_y + s * uy + w * ny

    img_pure = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    img_trans = Image.new("RGBA", (size, size), (0, 0, 0, 0))

    # =========================================================
    # DETAILED ORGANIC ROSE DRAWING FUNCTION
    # Draws authentic multi-petaled roses with overlapping cupped
    # petals, dark crevices, and glowing cores.
    # =========================================================
    def render_organic_rose(img, cx, cy, radius, style="crimson", seed=0.0):
        for dy in range(-radius - 2, radius + 3):
            for dx in range(-radius - 2, radius + 3):
                dist = math.hypot(dx, dy)
                if dist > radius:
                    continue
                
                ang = (math.atan2(dy, dx) + seed) % (2.0 * math.pi)
                norm_d = dist / radius
                
                # Multi-layer petal harmonics
                l1 = math.cos(5.0 * ang)
                l2 = math.cos(5.0 * ang + 1.25)
                l3 = math.cos(3.0 * ang + dist * 0.85)
                
                crevice = False
                if norm_d > 0.62 and l1 < -0.65:
                    crevice = True
                elif 0.32 <= norm_d <= 0.72 and l2 < -0.60:
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

                if style == "silver":
                    if crevice:
                        r, g, b = 60, 65, 80
                    else:
                        base = 150 + int(75 * (1.0 - norm_d * 0.4)) + int(35 * curl)
                        r = min(255, base - 5)
                        g = min(255, base)
                        b = min(255, base + 15)
                        if dist < 2.2:
                            r, g, b = 235, 215, 180
                    if dist > radius - 1.2:
                        r, g, b = 45, 50, 62
                    img.putpixel((px, py), (r, g, b, 255))

                elif style == "engraved":
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

                elif style == "crimson":
                    if crevice:
                        r, g, b = 20, 3, 8
                    elif dist > radius - 1.2:
                        r, g, b = 40, 5, 14
                    else:
                        base_r = int(115 + 130 * (1.0 - norm_d * 0.3) + 35 * curl)
                        base_g = int(10 + 20 * (1.0 - norm_d))
                        base_b = int(22 + 25 * (1.0 - norm_d))
                        if dist < 2.5:
                            r, g, b = 255, 75, 110
                        else:
                            r = min(255, max(28, base_r))
                            g = min(255, max(4, base_g))
                            b = min(255, max(8, base_b))
                    img.putpixel((px, py), (r, g, b, 255))

    # =========================================================
    # 1. HILT & GRIP: NO CROSSGUARD!
    # Integrates directly into the flat base of the blade.
    # Pommel rose at s = 6.0, radius = 8.5
    # Grip: s = 14 to 38, width = -3.2 to +3.2
    # =========================================================

    # Pommel Roses
    pom_x, pom_y = to_xy(6.0, 0.0)
    render_organic_rose(img_pure, pom_x, pom_y, radius=9, style="silver", seed=0.4)
    render_organic_rose(img_trans, pom_x, pom_y, radius=10, style="crimson", seed=0.7)

    # Grip Body (Pure & Transformed)
    for s in np.linspace(14, 38, 150):
        for w in np.linspace(-3.2, 3.2, 35):
            norm_w = (w + 3.2) / 6.4
            # Dark charcoal textured leather wrap
            r = int(26 + 25 * norm_w)
            g = int(26 + 22 * norm_w)
            b = int(32 + 28 * norm_w)
            # Spiral ribbon wrap
            d1 = (s * 0.8 + w * 0.9) % 5.0
            if d1 < 1.2:
                r, g, b = 185, 195, 210
                if abs(w) < 1.3:
                    r, g, b = 225, 230, 245
            
            x, y = to_xy(s, w)
            ix, iy = int(round(x)), int(round(y))
            if 0 <= ix < size and 0 <= iy < size:
                img_pure.putpixel((ix, iy), (r, g, b, 255))
                # Transformed grip: preserved clean for Mahiro's hands!
                # Subtle dark violet-charcoal tint
                tr = min(255, int(r * 0.5 + 30))
                tg = int(g * 0.22)
                tb = int(b * 0.28 + 12)
                img_trans.putpixel((ix, iy), (tr, tg, tb, 255))

    # Transformed Grip:
    # 1. Roots at top (s = 14 to 20) creeping down from pommel rose
    for s_gtop in np.linspace(14, 20, 60):
        w_vine = math.sin(s_gtop * 0.8) * 2.8
        for dw in (-0.8, 0, 0.8):
            x, y = to_xy(s_gtop, w_vine + dw)
            ix, iy = int(round(x)), int(round(y))
            if 0 <= ix < size and 0 <= iy < size:
                img_trans.putpixel((ix, iy), (85, 15, 30, 255))
    # Small thorns near pommel
    for st, wt in [(15, -3.5), (17, 3.5), (19, -3.2)]:
        x, y = to_xy(st, wt)
        ix, iy = int(round(x)), int(round(y))
        if 0 <= ix < size and 0 <= iy < size:
            img_trans.putpixel((ix, iy), (220, 40, 65, 255))

    # 2. Middle grip (s = 20 to 32) is DELIBERATELY CLEAN FOR MAHIRO'S HANDS!
    # No thorns on the grip.
    # Floating briar arches protect the sides without touching the hand (w = -5.5 and w = +5.5):
    for s_flank in np.linspace(21, 31, 80):
        curve = math.sin((s_flank - 21) / 10.0 * math.pi) * 2.5
        for side in (-1, 1):
            w_flank = side * (5.5 + curve)
            x, y = to_xy(s_flank, w_flank)
            ix, iy = int(round(x)), int(round(y))
            if 0 <= ix < size and 0 <= iy < size:
                img_trans.putpixel((ix, iy), (45, 8, 16, 255))
                # thorn on outer edge
                if int(s_flank) % 4 == 0:
                    tx, ty = to_xy(s_flank, w_flank + side * 2.0)
                    tix, tiy = int(round(tx)), int(round(ty))
                    if 0 <= tix < size and 0 <= tiy < size:
                        img_trans.putpixel((tix, tiy), (200, 30, 55, 255))

    # 3. Base collar (s = 35 to 39): Roots gather before erupting onto the blade
    for s_bot in np.linspace(35, 39, 50):
        w_bot = math.cos(s_bot * 0.9) * 3.0
        for dw in (-1.0, 0, 1.0):
            x, y = to_xy(s_bot, w_bot + dw)
            ix, iy = int(round(x)), int(round(y))
            if 0 <= ix < size and 0 <= iy < size:
                img_trans.putpixel((ix, iy), (95, 18, 35, 255))

    # =========================================================
    # 2. INTEGRATED BLADE BASE & RECTANGULAR COLLAR (NO CROSSGUARD!)
    # Directly enters the flat top of the blade at s = 39.
    # Collar band across the blade: s = 39 to 43, width = -10.5 to +11.5
    # =========================================================
    for s in np.linspace(39, 43, 30):
        for w in np.linspace(-10.5, 11.5, 100):
            norm_w = (w + 10.5) / 22.0
            val = 135 + int(30 * norm_w)
            r, g, b = val - 10, val, val + 15
            # Engraved line on the collar
            if abs(s - 41) < 0.6:
                r, g, b = 60, 65, 80
            elif w < -9.5 or w > 10.5:
                r, g, b = 210, 215, 230
            x, y = to_xy(s, w)
            ix, iy = int(round(x)), int(round(y))
            if 0 <= ix < size and 0 <= iy < size:
                img_pure.putpixel((ix, iy), (r, g, b, 255))
                # Transformed collar: dark scorched steel
                tr = min(255, int(val * 0.35 + 40))
                tg = int(val * 0.20)
                tb = int(val * 0.22)
                img_trans.putpixel((ix, iy), (tr, tg, tb, 255))

    # =========================================================
    # 3. COLOSSAL BUSTER SWORD BLADE
    # s = 43 to 125
    # Width:
    # Spine (straight): w_min = -11.0
    # Cutting edge: w_max = +11.5
    # Angled tip (clip point): from s = 103, edge slopes inward
    # to meet the spine at s = 125, w = -11.0.
    # Total width: 22.5 pixels wide!
    # =========================================================
    blade_mask = np.zeros((size, size), dtype=bool)

    for s_step in np.linspace(43, 125, 750):
        w_min = -11.0 # straight spine
        
        if s_step <= 103.0:
            w_max = 11.5
        else:
            t_tip = (s_step - 103.0) / (125.0 - 103.0)
            w_max = 11.5 - t_tip * (11.5 - (-11.0))

        if w_max <= w_min:
            continue

        for w_step in np.linspace(w_min, w_max, 160):
            x, y = to_xy(s_step, w_step)
            ix, iy = int(round(x)), int(round(y))
            if 0 <= ix < size and 0 <= iy < size:
                blade_mask[ix, iy] = True
                
                w_norm = (w_step - w_min) / (w_max - w_min)
                
                # Shading:
                # Spine bevel: 0.0 to 0.15 (dark steel)
                # Flat fuller: 0.15 to 0.78 (wide reflective steel)
                # Cutting edge: 0.78 to 1.0 (bright razor edge)
                if w_norm < 0.15:
                    t = w_norm / 0.15
                    val = 55 + int(60 * t)
                    r, g, b = val - 10, val, val + 15
                elif w_norm < 0.78:
                    t = (w_norm - 0.15) / (0.78 - 0.15)
                    sheen = math.sin(s_step * 0.35 + w_step * 1.8) * 14.0
                    val = 148 + int(30 * t + sheen)
                    r, g, b = val - 12, val, val + 18
                else:
                    t = (w_norm - 0.78) / (1.0 - 0.78)
                    val = 195 + int(60 * t)
                    r, g, b = val - 4, val, val + 6

                # Outer outline
                if w_step < w_min + 0.6 or w_step > w_max - 0.6 or s_step > 124.0:
                    r, g, b = 38, 42, 52

                r = max(0, min(255, r))
                g = max(0, min(255, g))
                b = max(0, min(255, b))
                
                img_pure.putpixel((ix, iy), (r, g, b, 255))

                # Transformed base: Dark ash-steel, showing the blade clearly visible
                # underneath the garden!
                tr = max(0, min(255, int(r * 0.38 + 35)))
                tg = max(0, min(255, int(g * 0.26 + 8)))
                tb = max(0, min(255, int(b * 0.30 + 12)))
                # The exposed razor edge catches glowing crimson energy
                if w_norm > 0.88:
                    tr = min(255, tr + 125)
                    tg = int(tg * 0.35)
                    tb = int(tb * 0.35)
                img_trans.putpixel((ix, iy), (tr, tg, tb, 255))

    # =========================================================
    # 4. PURE FORM: ENGRAVED ROSES ON THE BLADE
    # Authentic, fine silver/platinum floral engraving.
    # =========================================================
    for s_curv in np.linspace(46, 108, 450):
        w1 = math.sin(s_curv * 0.18) * 4.5 + 0.5
        w2 = math.cos(s_curv * 0.18 + 0.8) * 4.0 - 0.5
        for w_v in (w1, w2):
            x, y = to_xy(s_curv, w_v)
            ix, iy = int(round(x)), int(round(y))
            if 0 <= ix < size and 0 <= iy < size and blade_mask[ix, iy]:
                orig = img_pure.getpixel((ix, iy))
                img_pure.putpixel((ix, iy), (min(255, orig[0] + 45), min(255, orig[1] + 50), min(255, orig[2] + 60), 255))

    pure_roses = [
        (49.0, 1.0, 7, 0.2),
        (61.0, -2.0, 8, 1.1),
        (74.0, 2.5, 9, 2.3),
        (87.0, -1.0, 8, 0.7),
        (99.0, 1.5, 7, 1.8),
        (110.0, -2.5, 6, 2.9),
    ]

    for s_r, w_r, rad, ang in pure_roses:
        rx, ry = to_xy(s_r, w_r)
        render_organic_rose(img_pure, rx, ry, radius=rad, style="engraved", seed=ang)

    # =========================================================
    # 5. TRANSFORMED FORM: A LIVING GARDEN CLAIMING THE SWORD!
    # Complex, organic, asymmetric, dangerous, and majestic:
    # - Tangled roots of varied thickness wrapping front & back
    # - Partially wrapping the edge, leaving cutting sections exposed
    # - Multiple large roses blooming directly from the root structure
    # - Jagged, vicious functional thorns sticking out like blades
    # - Thorny root extending past the tip!
    # =========================================================

    # 1. Subtle glowing crimson cracks / ash veins in the steel
    for s_crack in np.linspace(44, 118, 500):
        wc = math.sin(s_crack * 0.28) * 5.5 + math.cos(s_crack * 0.14) * 2.5
        x, y = to_xy(s_crack, wc)
        ix, iy = int(round(x)), int(round(y))
        for ox in range(-1, 2):
            for oy in range(-1, 2):
                px, py = ix + ox, iy + oy
                if 0 <= px < size and 0 <= py < size and blade_mask[px, py]:
                    if abs(ox) + abs(oy) == 0:
                        img_trans.putpixel((px, py), (255, 65, 90, 255))
                    else:
                        img_trans.putpixel((px, py), (170, 25, 45, 255))

    # 2. Organic Root Network: 4 Major Taproots + Secondary Tendrils
    # Wrapping around the blade (some front, some back):
    roots_defs = [
        # (freq, phase, amplitude, center_offset, base_thick)
        (0.12, 0.2, 8.5, -0.5, 2.6),   # Main spine-side taproot
        (0.14, 1.5, 8.0, 1.2, 2.4),    # Main edge-side taproot
        (0.19, 2.8, 6.5, -1.5, 1.8),   # Crossing diagonal root
        (0.24, 0.9, 7.0, 0.0, 1.5),    # Intertwining vine
        (0.31, 3.5, 5.0, 2.0, 1.2),    # Secondary thin tendril
    ]

    for freq, phase, amp, offset, base_thick in roots_defs:
        for s_r in np.linspace(40, 122, 700):
            w_root = math.sin(s_r * freq + phase) * amp + offset
            thick = base_thick + 0.6 * math.sin(s_r * 0.4)
            
            for dw in np.linspace(-thick, thick, int(thick * 6)):
                w_cur = w_root + dw
                x, y = to_xy(s_r, w_cur)
                ix, iy = int(round(x)), int(round(y))
                if 0 <= ix < size and 0 <= iy < size:
                    # Organic bark coloration: deep black-brown with crimson ridges
                    t_th = (dw + thick) / (2.0 * thick)
                    bark_r = int(22 + 40 * t_th)
                    bark_g = int(5 + 14 * t_th)
                    bark_b = int(10 + 18 * t_th)
                    if abs(dw) < 0.6: # bark highlight ridge
                        bark_r, bark_g, bark_b = 100, 20, 38
                    img_trans.putpixel((ix, iy), (bark_r, bark_g, bark_b, 255))

    # 3. Partially wrapped cutting edge:
    # Some roots loop around the cutting edge at specific points (e.g. s = 58, s = 82, s = 106)
    for s_loop in (58.0, 82.0, 106.0):
        for d_ang in np.linspace(-math.pi/2, math.pi/2, 25):
            w_loop = 11.5 + math.cos(d_ang) * 3.0
            s_l = s_loop + math.sin(d_ang) * 2.5
            x, y = to_xy(s_l, w_loop)
            ix, iy = int(round(x)), int(round(y))
            if 0 <= ix < size and 0 <= iy < size:
                img_trans.putpixel((ix, iy), (85, 16, 32, 255))

    # 4. JAGGED FUNCTIONAL THORNS & BARBS (Directly from Friend's Drawing!)
    # As the user emphasized: "Las espinas deben sobresalir de la espada y ser capaces de causar daño por sí mismas."
    # Long, lethal, organic thorns jutting out at violent angles!
    thorns = [
        # Spine side (w = -11.0): (s_base, w_base, length, angle_deg, base_w)
        (46.0, -11.0, 18.0, -65, 4.5),
        (56.0, -11.0, 26.0, -78, 6.0),
        (66.0, -11.0, 34.0, -84, 7.5),
        (78.0, -11.0, 38.0, -88, 8.5),
        (90.0, -11.0, 30.0, -78, 7.0),
        (100.0, -11.0, 24.0, -82, 6.0),
        (110.0, -11.0, 19.0, -85, 5.0),
        (118.0, -11.0, 15.0, -80, 4.5),

        # Edge side (w = +11.5):
        (44.0, 11.5, 18.0, 62, 5.0),
        (54.0, 11.5, 27.0, 72, 6.5),
        (65.0, 11.5, 35.0, 78, 7.5),
        (76.0, 11.5, 39.0, 82, 8.5),
        (88.0, 11.5, 32.0, 74, 7.5),
        (98.0, 11.5, 26.0, 68, 6.5),
        (108.0, 9.5, 21.0, 60, 5.5),
        (116.0, 3.0, 17.0, 50, 4.5),
        (123.0, -4.0, 15.0, 40, 4.0),

        # Internal surface thorns sprouting from the roots:
        (52.0, 0.0, 10.0, -30, 3.5),
        (70.0, -2.0, 12.0, 35, 4.0),
        (84.0, 1.5, 11.0, -25, 3.5),
        (95.0, -1.0, 10.0, 30, 3.5),

        # Thorny root extending past the tip!
        (124.0, -11.0, 16.0, 15, 3.8),
    ]

    for s_t, w_t, t_len, ang_deg, base_w in thorns:
        ang_rad = math.radians(ang_deg)
        tx_dir = math.sin(ang_rad) * ux + math.cos(ang_rad) * nx
        ty_dir = math.sin(ang_rad) * uy + math.cos(ang_rad) * ny
        px_dir = -ty_dir
        py_dir = tx_dir
        
        base_x, base_y = to_xy(s_t, w_t)
        
        for step in np.linspace(0, t_len, 65):
            curve = math.sin(step / t_len * 1.5) * 1.8
            curr_x = base_x + tx_dir * step + px_dir * curve
            curr_y = base_y + ty_dir * step + py_dir * curve
            t_ratio = step / t_len
            cur_w = base_w * (1.0 - t_ratio)
            
            for side in np.linspace(-cur_w, cur_w, 24):
                ix = int(round(curr_x + px_dir * side))
                iy = int(round(curr_y + py_dir * side))
                if 0 <= ix < size and 0 <= iy < size:
                    if t_ratio > 0.85: # razor-sharp glowing needle tip
                        r, g, b = 255, 55, 85
                    elif abs(side) > cur_w * 0.65: # sharp blade edge of thorn
                        r, g, b = 185, 28, 52
                    else: # black thorny obsidian core
                        r, g, b = 20, 5, 10
                    img_trans.putpixel((ix, iy), (r, g, b, 255))

    # 5. LARGE GOTHIC ROSES BLOOMING FROM THE ROOT STRUCTURE
    # As requested: "especialmente varias rosas grandes creciendo directamente de la estructura central"
    trans_roses = [
        # (s, w, radius, angle_seed)
        (49.0, 1.0, 8.5, 0.4),
        (61.0, -1.5, 10.5, 1.2),
        (74.0, 2.0, 11.5, 2.1),
        (88.0, -1.0, 10.5, 0.9),
        (100.0, 1.0, 9.5, 1.7),
        (111.0, -2.0, 7.5, 2.8),
    ]

    for s_r, w_r, rad, ang in trans_roses:
        rx, ry = to_xy(s_r, w_r)
        # First draw green-black sepals/calyx anchoring the rose to the roots
        for sep_a in np.linspace(0, 2 * math.pi, 5, endpoint=False):
            sx_dir = math.cos(sep_a + ang)
            sy_dir = math.sin(sep_a + ang)
            for sep_d in np.linspace(rad - 1.5, rad + 3.0, 10):
                six = int(round(rx + sx_dir * sep_d))
                siy = int(round(ry + sy_dir * sep_d))
                if 0 <= six < size and 0 <= siy < size:
                    img_trans.putpixel((six, siy), (35, 12, 18, 255))
        # Draw the gothic blood-red rose
        render_organic_rose(img_trans, rx, ry, radius=int(rad), style="crimson", seed=ang)

    # =========================================================
    # SAVE OUTPUT TEXTURES
    # =========================================================
    out_pure_path = "src/main/resources/assets/awakening_system/textures/item/la_doncella.png"
    out_trans_path = "src/main/resources/assets/awakening_system/textures/item/la_doncella_transformed.png"

    img_pure.save(out_pure_path)
    img_trans.save(out_trans_path)
    print(f"Updated: {out_pure_path}")
    print(f"Updated: {out_trans_path}")

    # Generate showcase comparison
    showcase = Image.new("RGBA", (size * 2 + 80, size + 40), (14, 11, 18, 255))
    showcase.paste(img_pure, (25, 20), img_pure)
    showcase.paste(img_trans, (size + 55, 20), img_trans)
    
    artifact_path = "C:/Users/soybe/.gemini/antigravity/brain/930e4101-5310-48ee-9b53-1a9777f45603/la_doncella_mahiro_showcase.png"
    showcase.save(artifact_path)
    print(f"Showcase saved to: {artifact_path}")

if __name__ == "__main__":
    generate_mahiro_espadon_textures()
