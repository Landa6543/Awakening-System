import math
import numpy as np
from PIL import Image, ImageDraw

def generate_elongated_mahiro_espadon():
    size = 128
    
    # 45-degree diagonal axis
    # Origin base near (14, 114) -> Tip near (123, 5)
    # Total diagonal span s = 0..155
    sqrt2 = math.sqrt(2.0)
    ux, uy = 1.0 / sqrt2, -1.0 / sqrt2   # along sword axis (towards top-right)
    nx, ny = -1.0 / sqrt2, -1.0 / sqrt2  # perpendicular (positive = top-left / edge, negative = bottom-right / spine)

    ox, oy = 14.0, 114.0

    def to_xy(s, w):
        return ox + s * ux + w * nx, oy + s * uy + w * ny

    img_pure = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    img_trans = Image.new("RGBA", (size, size), (0, 0, 0, 0))

    # =========================================================
    # DETAILED ORGANIC ROSE DRAWING FUNCTION
    # Draws multi-layered petals with authentic spiraling cups
    # =========================================================
    def render_rose(img, s_c, w_c, radius, style="crimson", seed=0.0):
        cx, cy = to_xy(s_c, w_c)
        for dy in range(-int(radius + 2), int(radius + 3)):
            for dx in range(-int(radius + 2), int(radius + 3)):
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

                if style == "silver_pommel":
                    if crevice:
                        r, g, b = 65, 70, 85
                    elif dist > radius - 1.2:
                        r, g, b = 50, 55, 68
                    else:
                        base = 155 + int(70 * (1.0 - norm_d * 0.4)) + int(30 * curl)
                        r = min(255, base - 5)
                        g = min(255, base)
                        b = min(255, base + 15)
                        if dist < 2.0:
                            r, g, b = 240, 225, 200
                    img.putpixel((px, py), (r, g, b, 255))

                elif style == "engraved_steel":
                    # Engraved directly into the blade surface
                    orig = img.getpixel((px, py))
                    if orig[3] > 0:
                        if crevice:
                            r = max(25, orig[0] - 40)
                            g = max(28, orig[1] - 40)
                            b = max(35, orig[2] - 35)
                        else:
                            emboss = int(45 * curl + 20 * (1.0 - norm_d))
                            r = min(255, orig[0] + emboss)
                            g = min(255, orig[1] + emboss + 4)
                            b = min(255, orig[2] + emboss + 12)
                        img.putpixel((px, py), (r, g, b, 255))

                elif style == "pure_garden_rose":
                    # Rose nestled in thorns on Pure blade: deep carmine with silver petal edges
                    if crevice:
                        r, g, b = 35, 10, 18
                    elif dist > radius - 1.2:
                        r, g, b = 55, 14, 25
                    else:
                        base_r = int(120 + 100 * (1.0 - norm_d * 0.4) + 35 * curl)
                        base_g = int(22 + 25 * (1.0 - norm_d))
                        base_b = int(35 + 30 * (1.0 - norm_d))
                        if curl > 0.4:
                            # Silver edge highlight on petal tips
                            base_r = min(255, base_r + 45)
                            base_g = min(255, base_g + 45)
                            base_b = min(255, base_b + 55)
                        if dist < 2.2:
                            r, g, b = 250, 180, 195
                        else:
                            r, g, b = base_r, base_g, base_b
                    img.putpixel((px, py), (r, g, b, 255))

                elif style == "crimson_transformed":
                    # Blazing crimson rose under Crimson Moon
                    if crevice:
                        r, g, b = 22, 2, 8
                    elif dist > radius - 1.2:
                        r, g, b = 42, 4, 16
                    else:
                        base_r = int(140 + 115 * (1.0 - norm_d * 0.25) + 30 * curl)
                        base_g = int(8 + 18 * (1.0 - norm_d))
                        base_b = int(22 + 28 * (1.0 - norm_d))
                        if dist < 2.5:
                            r, g, b = 255, 130, 160  # Glowing hot core
                        else:
                            r, g, b = base_r, base_g, base_b
                    img.putpixel((px, py), (r, g, b, 255))

    # =========================================================
    # 1. RENDER POMMEL & HILT (s = 0..42)
    # =========================================================
    for img, is_trans in [(img_pure, False), (img_trans, True)]:
        # Hilt Grip: s = 14 to 38, width = 3.5 px
        for s in np.linspace(14, 38, 120):
            for w in np.linspace(-3.2, 3.2, 35):
                px, py = to_xy(s, w)
                ipx, ipy = int(round(px)), int(round(py))
                if not (0 <= ipx < size and 0 <= ipy < size):
                    continue
                
                # Spiral wire wrapping pattern
                wrap = (s * 1.5 + w * 2.0) % 6.0
                if abs(w) > 2.6:
                    col = (25, 20, 28, 255) if not is_trans else (15, 6, 12, 255)
                elif wrap < 1.2:
                    # Silver / Crimson wire highlight
                    col = (210, 215, 225, 255) if not is_trans else (255, 65, 95, 255)
                else:
                    # Dark grip leather
                    col = (48, 44, 52, 255) if not is_trans else (32, 14, 24, 255)
                img.putpixel((ipx, ipy), col)

        # Crossguard: s = 38 to 44, width = -15 to +15 px
        for s in np.linspace(38, 44, 35):
            for w in np.linspace(-15.0, 15.0, 150):
                px, py = to_xy(s, w)
                ipx, ipy = int(round(px)), int(round(py))
                if not (0 <= ipx < size and 0 <= ipy < size):
                    continue
                
                # Crossguard bar shape
                edge = abs(w) > 13.5 or s < 39.0 or s > 43.0
                if is_trans:
                    if edge:
                        col = (30, 8, 18, 255)
                    else:
                        sh = int(70 + 40 * (1.0 - abs(w)/15.0))
                        col = (min(255, sh + 50), min(255, sh // 3), min(255, sh // 2), 255)
                else:
                    if edge:
                        col = (45, 50, 60, 255)
                    else:
                        sh = int(160 + 50 * (1.0 - abs(w)/15.0))
                        col = (sh - 5, sh, sh + 15, 255)
                img.putpixel((ipx, ipy), col)

        # Rose Pommel at s = 8, w = 0, radius = 7.5
        render_rose(img, 8.0, 0.0, 7.5, style="silver_pommel" if not is_trans else "crimson_transformed", seed=1.4)

    # =========================================================
    # 2. RENDER THE ELONGATED BLADE SLAB (s = 44..162)
    # Proportions: Length = 118px, Width = 15px (w = -7.5..+7.5)
    # Chisel Spear Tip from s = 146 to 162
    # =========================================================
    for img, is_trans in [(img_pure, False), (img_trans, True)]:
        for s in np.linspace(44, 162, 600):
            # Tip profile: sharp diagonal chisel spear at the end
            w_max = 7.5
            w_min = -7.5
            if s > 144:
                # Taper both edges sharply to a spear chisel point at s = 162
                t = (s - 144) / 18.0
                w_max = 7.5 * (1.0 - t * 0.96)
                w_min = -7.5 * (1.0 - t * 0.96)
            
            for w in np.linspace(w_min, w_max, 95):
                px, py = to_xy(s, w)
                ipx, ipy = int(round(px)), int(round(py))
                if not (0 <= ipx < size and 0 <= ipy < size):
                    continue

                norm_w = w / 7.5 # -1 to 1

                if not is_trans:
                    # PURE FORM: Brilliant mirror steel with rich metallic bevels
                    dist_center = abs(w) / 7.5
                    
                    if abs(w - w_max) < 0.85 or abs(w - w_min) < 0.85:
                        # Razor cutting edge
                        r, g, b = 255, 255, 255
                    elif dist_center < 0.12:
                        # Subtle central fuller groove
                        r, g, b = 145, 155, 172
                    elif norm_w > 0:
                        # Top-left bevel: bright reflection
                        light = 212 + int(36 * (1.0 - dist_center))
                        r, g, b = light - 5, light, light + 15
                    else:
                        # Bottom-right bevel: shaded steel
                        light = 175 + int(32 * (1.0 - dist_center))
                        r, g, b = light - 10, light - 5, light + 10
                    
                    # Longitudinal steel grain
                    grain = (math.sin(s * 1.8) * 5.5)
                    r = int(np.clip(r + grain, 0, 255))
                    g = int(np.clip(g + grain, 0, 255))
                    b = int(np.clip(b + grain, 0, 255))

                else:
                    # TRANSFORMED FORM: Dark cursed obsidian damascus with glowing crimson runes
                    dist_center = abs(w) / 7.5
                    if abs(w - w_max) < 0.9 or abs(w - w_min) < 0.9:
                        # Glowing crimson razor edge
                        r, g, b = 255, 70, 105
                    else:
                        base = int(22 + 16 * (1.0 - dist_center))
                        r = base + 18
                        g = base - 6
                        b = base + 4
                    
                    # Crimson pulsating veins
                    vein = math.sin(s * 0.45 + w * 0.7) + math.cos(s * 0.25 - w * 0.5)
                    if vein > 1.25:
                        r = min(255, r + 195)
                        g = min(255, g + 30)
                        b = min(255, b + 60)
                    elif vein > 0.85:
                        r = min(255, r + 100)
                        g = min(255, g + 15)
                        b = min(255, b + 32)

                img.putpixel((ipx, ipy), (r, g, b, 255))

    # =========================================================
    # 3. ENGRAVED ROSES AT BLADE BASE (s = 48..75)
    # Elegant rose motifs engraved into the polished steel
    # =========================================================
    base_roses = [
        (52.0, 0.0, 5.2, 0.0),
        (63.0, 2.0, 5.6, 1.2),
        (64.0, -2.4, 4.6, 2.5),
        (75.0, 0.0, 6.0, 0.8)
    ]
    for s_c, w_c, rad, seed in base_roses:
        render_rose(img_pure, s_c, w_c, rad, style="engraved_steel", seed=seed)
        render_rose(img_trans, s_c, w_c, rad, style="crimson_transformed", seed=seed)

    # =========================================================
    # 4. GNARLED THORNS & JAGGED OUTWARD PROTRUSIONS (s = 74..158)
    # EXACTLY AS IN FRIEND'S SKETCH:
    # Menacing thorn spikes jutting OUT past the blade edges,
    # angled forward towards the tip at 35-45 degrees!
    # =========================================================
    thorn_spikes = [
        # (s_base, w_base, s_tip, w_tip, thickness)
        # Cluster 1 (s = 78..92)
        (78.0, 6.0, 86.0, 15.0, 2.4),
        (83.0, 6.5, 91.0, 13.5, 2.0),
        (77.0, -6.0, 85.0, -14.5, 2.4),
        (82.0, -6.5, 90.0, -13.0, 2.0),

        # Cluster 2 (s = 95..112) - Major thorn flare from sketch
        (96.0, 6.5, 106.0, 17.0, 2.8),
        (101.0, 7.0, 110.0, 14.5, 2.2),
        (97.0, -6.5, 107.0, -16.5, 2.8),
        (102.0, -7.0, 111.0, -14.0, 2.2),

        # Cluster 3 (s = 115..130)
        (115.0, 6.0, 125.0, 15.5, 2.6),
        (120.0, 6.5, 129.0, 13.5, 2.0),
        (116.0, -6.0, 126.0, -15.0, 2.6),
        (121.0, -6.5, 130.0, -13.0, 2.0),

        # Cluster 4 (s = 133..148)
        (133.0, 5.5, 143.0, 14.0, 2.4),
        (138.0, 6.0, 146.0, 11.5, 2.0),
        (134.0, -5.5, 144.0, -13.5, 2.4),
        (139.0, -6.0, 147.0, -11.0, 2.0),

        # Tip Thorns (s = 148..160) - sharp needles wrapping chisel point
        (148.0, 3.5, 158.0, 7.5, 2.0),
        (149.0, -3.5, 159.0, -7.0, 2.0),
        (156.0, 0.0, 163.0, 0.0, 1.8) # Trailing needle tip
    ]

    for img, is_trans in [(img_pure, False), (img_trans, True)]:
        # Draw bramble branches wrapping across blade surface
        for s_t in np.linspace(72, 150, 400):
            # Multi-strand braided briars
            w_briar1 = math.sin(s_t * 0.18) * 5.0
            w_briar2 = math.cos(s_t * 0.22) * 4.5
            w_briar3 = math.sin(s_t * 0.14 + 1.5) * 6.0

            for wb in [w_briar1, w_briar2, w_briar3]:
                for dw in np.linspace(-1.3, 1.3, 7):
                    px, py = to_xy(s_t, wb + dw)
                    ipx, ipy = int(round(px)), int(round(py))
                    if 0 <= ipx < size and 0 <= ipy < size:
                        if not is_trans:
                            # Dark iron/ebony briar vines
                            c = int(28 + 15 * (1.0 - abs(dw)/1.3))
                            img.putpixel((ipx, ipy), (c, c - 4, c - 2, 255))
                        else:
                            # Obsidian briar with glowing crimson edge
                            if abs(dw) > 0.8:
                                img.putpixel((ipx, ipy), (255, 30, 65, 255))
                            else:
                                img.putpixel((ipx, ipy), (20, 6, 14, 255))

        # Draw the outward thorn spikes
        for s_b, w_b, s_t, w_t, thick in thorn_spikes:
            steps = 45
            for st_i in range(steps + 1):
                prog = st_i / float(steps)
                curr_s = s_b + prog * (s_t - s_b)
                curr_w = w_b + prog * (w_t - w_b)
                curr_thick = thick * (1.0 - prog * 0.85) # sharp tip

                for dw in np.linspace(-curr_thick, curr_thick, int(curr_thick * 5) + 3):
                    px, py = to_xy(curr_s, curr_w + dw)
                    ipx, ipy = int(round(px)), int(round(py))
                    if 0 <= ipx < size and 0 <= ipy < size:
                        if not is_trans:
                            if prog > 0.8:
                                # Sharp thorn point highlight
                                col = (140, 145, 155, 255)
                            else:
                                c = int(32 + 18 * (1.0 - abs(dw)/(curr_thick + 0.1)))
                                col = (c, c - 3, c - 2, 255)
                        else:
                            if prog > 0.7:
                                col = (255, 60, 95, 255) # Glowing thorn tip
                            else:
                                col = (35, 8, 18, 255)
                        img.putpixel((ipx, ipy), col)

    # =========================================================
    # 5. BLOOMING ROSES IN THE THORNY GARDEN (s = 85..145)
    # Intertwined among the vines along the blade
    # =========================================================
    garden_roses = [
        # (s, w, radius, seed)
        (88.0, -1.5, 6.0, 0.4),
        (98.0, 1.8, 6.8, 1.8),
        (108.0, -1.0, 7.2, 2.7),  # Central Grand Rose
        (120.0, 2.0, 6.4, 0.9),
        (132.0, -1.2, 5.6, 2.1),
        (142.0, 0.5, 4.6, 1.5)
    ]
    for s_c, w_c, rad, seed in garden_roses:
        render_rose(img_pure, s_c, w_c, rad, style="pure_garden_rose", seed=seed)
        render_rose(img_trans, s_c, w_c, rad, style="crimson_transformed", seed=seed)

    # Save output textures
    pure_path = r"src/main/resources/assets/awakening_system/textures/item/la_doncella.png"
    trans_path = r"src/main/resources/assets/awakening_system/textures/item/la_doncella_transformed.png"
    img_pure.save(pure_path)
    img_trans.save(trans_path)
    print(f"Generated elongated Mahiro Espadón textures: {pure_path} and {trans_path}")

    # Generate side-by-side showcase
    showcase_path = r"C:/Users/soybe/.gemini/antigravity/brain/930e4101-5310-48ee-9b53-1a9777f45603/la_doncella_mahiro_showcase.png"
    scale = 3
    sw, sh = size * scale, size * scale
    pad = 30
    showcase = Image.new("RGBA", (sw * 2 + pad * 3, sh + pad * 2), (15, 16, 20, 255))
    
    p_scaled = img_pure.resize((sw, sh), Image.NEAREST)
    t_scaled = img_trans.resize((sw, sh), Image.NEAREST)

    showcase.paste(p_scaled, (pad, pad), p_scaled)
    showcase.paste(t_scaled, (sw + pad * 2, pad), t_scaled)
    showcase.save(showcase_path)
    print(f"Saved showcase to {showcase_path}")

if __name__ == "__main__":
    generate_elongated_mahiro_espadon()
