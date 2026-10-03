import os
import numpy as np
from PIL import Image
import math

out_dir = "src/main/resources/assets/awakening_system/textures/item"
os.makedirs(out_dir, exist_ok=True)

size = 128
ox, oy = 16.0, 112.0
sqrt2 = math.sqrt(2.0)
ux, uy = 1.0 / sqrt2, -1.0 / sqrt2
nx, ny = -1.0 / sqrt2, -1.0 / sqrt2

def to_xy(s, w):
    return ox + s * ux + w * nx, oy + s * uy + w * ny

def add_pixel_outline(img, outline_color):
    arr = np.array(img)
    alpha = arr[:, :, 3]
    out_arr = arr.copy()
    h, w = alpha.shape
    for y in range(h):
        for x in range(w):
            if alpha[y, x] == 0:
                has_solid = False
                for dy, dx in [(-1,0), (1,0), (0,-1), (0,1)]:
                    ny_p, nx_p = y + dy, x + dx
                    if 0 <= ny_p < h and 0 <= nx_p < w and alpha[ny_p, nx_p] > 180:
                        has_solid = True
                        break
                if has_solid:
                    out_arr[y, x] = outline_color
    return Image.fromarray(out_arr)

# ==============================================================
# 1. ELUCIDATOR (Kirito's Slender Pitch-Black Demon Blade) - 128x128
# ==============================================================
elu = Image.new('RGBA', (size, size), (0, 0, 0, 0))

# Pommel (s=0..12)
for s in np.linspace(0, 4, 30):
    w_max = 2.6 * math.sin((s / 4.0) * math.pi * 0.5)
    for w in np.linspace(-w_max, w_max, 25):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            norm_w = w / (w_max + 1e-5)
            c = int(210 + 40 * (1.0 - norm_w * norm_w))
            elu.putpixel((ipx, ipy), (c - 8, c, min(255, c + 15), 255))

for s in np.linspace(4, 11, 40):
    for w in np.linspace(-2.6, 2.6, 35):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            norm_w = abs(w) / 2.6
            base = int(28 + 32 * (1.0 - norm_w * norm_w))
            elu.putpixel((ipx, ipy), (base, base + 2, base + 6, 255))

for s in np.linspace(11, 13, 20):
    for w in np.linspace(-3.0, 3.0, 35):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            c = int(190 + 60 * (1.0 - abs(w)/3.0))
            elu.putpixel((ipx, ipy), (c - 10, c, c + 12, 255))

# Grip (s=13..41) - Slender grip
for s in np.linspace(13, 41, 200):
    for w in np.linspace(-2.2, 2.2, 35):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            dist_ring = min(abs(s - 20), abs(s - 27), abs(s - 34))
            if dist_ring < 0.85:
                norm_w = abs(w) / 2.2
                c = int(200 + 50 * (1.0 - norm_w))
                elu.putpixel((ipx, ipy), (c - 8, c, c + 15, 255))
            elif dist_ring < 1.3:
                elu.putpixel((ipx, ipy), (14, 15, 18, 255))
            else:
                norm_w = abs(w) / 2.2
                base = int(22 + 18 * (1.0 - norm_w * norm_w))
                elu.putpixel((ipx, ipy), (base, base + 2, base + 5, 255))

# Crossguard (s=40..48)
for s in np.linspace(40, 50, 60):
    for w in np.linspace(-14, -4, 60):
        dist = math.hypot(s - 45.0, w - (-9.0))
        if 2.2 <= dist <= 4.5:
            px, py = to_xy(s, w)
            ipx, ipy = int(round(px)), int(round(py))
            if 0 <= ipx < size and 0 <= ipy < size:
                if dist > 3.8 or dist < 2.8:
                    c = 220
                    elu.putpixel((ipx, ipy), (c - 5, c, c + 10, 255))
                else:
                    elu.putpixel((ipx, ipy), (50, 56, 68, 255))

for s in np.linspace(41.5, 47.5, 45):
    for w in np.linspace(-5.2, 5.2, 60):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            if abs(w) > 4.6 or s < 42.1 or s > 46.9:
                elu.putpixel((ipx, ipy), (195, 205, 220, 255))
            else:
                elu.putpixel((ipx, ipy), (22, 25, 32, 255))

for s in np.linspace(43.0, 46.0, 30):
    for w in np.linspace(4.8, 15.5, 60):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            if w > 14.0:
                elu.putpixel((ipx, ipy), (225, 235, 250, 255))
            elif abs(s - 44.5) > 1.0:
                elu.putpixel((ipx, ipy), (145, 155, 172, 255))
            else:
                elu.putpixel((ipx, ipy), (32, 35, 45, 255))

# Blade (s=48..142) - Slender Blade
for s in np.linspace(48, 142, 750):
    if s < 54:
        w_max = 3.6
    elif s < 122:
        w_max = 4.0
    elif s < 133:
        t = (s - 122) / 11.0
        w_max = 4.0 + 0.6 * t
    else:
        t = (s - 133) / 9.0
        w_max = 4.6 * (1.0 - t)

    if w_max < 0.2: continue

    for w in np.linspace(-w_max, w_max, int(w_max * 20 + 12)):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if not (0 <= ipx < size and 0 <= ipy < size): continue

        dist_edge = w_max - abs(w)

        if dist_edge < 1.1:
            edge_t = dist_edge / 1.1
            c = int(250 - 25 * edge_t)
            col = (c - 5, c, min(255, c + 8), 255)
        elif dist_edge < 1.8:
            col = (68, 78, 96, 255)
        else:
            norm_w = abs(w) / w_max
            if abs(w) < 0.9 and s < 126:
                c = int(48 + 24 * (1.0 - abs(w)/0.9))
                col = (c, c + 3, c + 8, 255)
            else:
                base = int(16 + 12 * (1.0 - norm_w))
                col = (base, base + 2, base + 5, 255)

        elu.putpixel((ipx, ipy), col)

elu = add_pixel_outline(elu, (10, 11, 14, 255))
elu.save(os.path.join(out_dir, "elucidator.png"))
print("Generated slender elucidator.png")

# ==============================================================
# 2. DARK REPULSER (Kirito's Slender Crystallite Longsword) - 128x128
# ==============================================================
rep = Image.new('RGBA', (size, size), (0, 0, 0, 0))

for s in np.linspace(0, 12, 100):
    if s < 6:
        w_max = 0.8 + 3.6 * (s / 6.0)
    else:
        w_max = 4.4 * (1.0 - (s - 6.0) / 7.0) + 2.2 * ((s - 6.0) / 7.0)

    for w in np.linspace(-w_max, w_max, int(w_max * 14 + 10)):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if not (0 <= ipx < size and 0 <= ipy < size): continue

        norm_w = w / (w_max + 1e-5)
        if norm_w > 0:
            t = 1.0 - norm_w
            col = (int(145 + 100 * t), int(240 + 15 * t), int(225 + 30 * t), 255)
        else:
            t = 1.0 + norm_w
            col = (int(25 + 65 * t), int(135 + 75 * t), int(125 + 70 * t), 255)
        rep.putpixel((ipx, ipy), col)

for s in np.linspace(12, 40, 200):
    for w in np.linspace(-2.3, 2.3, 35):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if not (0 <= ipx < size and 0 <= ipy < size): continue

        wrap1 = abs((s * 1.4 + w * 2.5) % 8.0 - 4.0)
        wrap2 = abs((s * 1.4 - w * 2.5 + 2.0) % 8.0 - 4.0)
        is_wrap = wrap1 < 0.85 or wrap2 < 0.85
        if is_wrap:
            rep.putpixel((ipx, ipy), (238, 255, 250, 255))
        else:
            t = 1.0 - (abs(w) / 2.3)**2
            rep.putpixel((ipx, ipy), (int(22 + 35 * t), int(110 + 50 * t), int(100 + 45 * t), 255))

for w in np.linspace(-16, 16, 180):
    norm_w = abs(w) / 16.0
    s_center = 43.5 + 6.5 * (norm_w ** 1.35)
    s_thick = 3.8 * (1.0 - norm_w * 0.65)
    for s in np.linspace(s_center - s_thick, s_center + s_thick, 45):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if not (0 <= ipx < size and 0 <= ipy < size): continue

        if s > s_center:
            col = (115, 240, 218, 255)
        else:
            col = (30, 135, 120, 255)
        rep.putpixel((ipx, ipy), col)

for s in np.linspace(42.0, 47.0, 50):
    for w in np.linspace(-3.6, 3.6, 50):
        d_val = abs(s - 44.5) / 2.5 + abs(w) / 3.6
        if d_val <= 1.0:
            px, py = to_xy(s, w)
            ipx, ipy = int(round(px)), int(round(py))
            if 0 <= ipx < size and 0 <= ipy < size:
                if d_val > 0.85:
                    col = (18, 75, 65, 255)
                else:
                    glow = 1.0 - d_val
                    col = (int(175 + 80 * glow), 255, int(235 + 20 * glow), 255)
                rep.putpixel((ipx, ipy), col)

for s in np.linspace(48, 142, 750):
    if s < 56:
        t = (s - 48) / 8.0
        w_max = 4.2 + 1.6 * math.sin(t * math.pi * 0.5)
    elif s < 125:
        t = (s - 56) / 69.0
        w_max = 5.8 - 1.0 * t
    else:
        t = (s - 125) / 17.0
        w_max = 4.8 * (1.0 - t)

    if w_max < 0.2: continue

    for w in np.linspace(-w_max, w_max, int(w_max * 20 + 12)):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if not (0 <= ipx < size and 0 <= ipy < size): continue

        dist_edge = w_max - abs(w)

        if dist_edge < 1.0:
            col = (220, 255, 250, 255)
        elif dist_edge < 1.8:
            col = (115, 235, 212, 255)
        else:
            ridge_dist = abs(w)
            if ridge_dist < 0.9:
                r_val = 1.0 - ridge_dist / 0.9
                col = (int(190 + 65 * r_val), 255, int(240 + 15 * r_val), 255)
            elif w > 0:
                t = (w - 0.9) / (w_max - 1.8 + 1e-5)
                r = int(70 + 50 * (1.0 - t))
                g = int(205 + 40 * (1.0 - t))
                b = int(185 + 40 * (1.0 - t))
                col = (r, g, b, 255)
            else:
                t = (-w - 0.9) / (w_max - 1.8 + 1e-5)
                r = int(22 + 40 * (1.0 - t))
                g = int(135 + 50 * (1.0 - t))
                b = int(125 + 45 * (1.0 - t))
                col = (r, g, b, 255)

        rep.putpixel((ipx, ipy), col)

rep = add_pixel_outline(rep, (8, 48, 40, 255))
rep.save(os.path.join(out_dir, "dark_repulser.png"))
print("Generated slender dark_repulser.png")

# ==============================================================
# 3. LAMBENT LIGHT (Asuna's Slender Needle Rapier) - 128x128
# ==============================================================
lam = Image.new('RGBA', (size, size), (0, 0, 0, 0))

for s in np.linspace(0, 4, 30):
    w_max = 1.6 * math.sin((s / 4.0) * math.pi * 0.5)
    for w in np.linspace(-w_max, w_max, 20):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            lam.putpixel((ipx, ipy), (255, 218, 25, 255))

for s in np.linspace(4, 11, 50):
    for w in np.linspace(-3.5, 3.5, 50):
        dist = math.hypot(s - 7.5, w)
        if dist <= 3.0:
            px, py = to_xy(s, w)
            ipx, ipy = int(round(px)), int(round(py))
            if not (0 <= ipx < size and 0 <= ipy < size): continue
            if dist < 1.3:
                lam.putpixel((ipx, ipy), (45, 220, 195, 255))
            elif dist > 2.5:
                lam.putpixel((ipx, ipy), (175, 125, 15, 255))
            else:
                lam.putpixel((ipx, ipy), (255, 222, 65, 255))

for s in np.linspace(11, 13, 20):
    for w in np.linspace(-2.4, 2.4, 25):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            lam.putpixel((ipx, ipy), (235, 180, 10, 255))

for s in np.linspace(13, 40, 220):
    for w in np.linspace(-1.9, 1.9, 30):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if not (0 <= ipx < size and 0 <= ipy < size): continue
        is_wire = (s * 1.6 - w * 2.2) % 5.5 < 1.1
        if is_wire:
            lam.putpixel((ipx, ipy), (252, 210, 25, 255))
        else:
            norm_w = w / 1.9
            c = int(244 + 11 * (1.0 - norm_w * norm_w))
            lam.putpixel((ipx, ipy), (c, c, min(255, c + 3), 255))

for s in np.linspace(40, 50, 75):
    for w in np.linspace(-10, 10, 85):
        ellipse = ((s - 45.0) / 4.5)**2 + (w / 9.8)**2
        if ellipse <= 1.0:
            px, py = to_xy(s, w)
            ipx, ipy = int(round(px)), int(round(py))
            if not (0 <= ipx < size and 0 <= ipy < size): continue
            is_rim = ellipse > 0.74
            ang = math.atan2(w, (s - 45.0) * 2.5)
            is_spoke = abs(math.sin(ang * 4.0)) < 0.18
            if is_rim or is_spoke:
                if is_rim and ellipse > 0.86:
                    lam.putpixel((ipx, ipy), (175, 125, 15, 255))
                else:
                    lam.putpixel((ipx, ipy), (255, 225, 75, 255))
            else:
                d_center = math.sqrt(ellipse)
                r = int(12 + 25 * (1.0 - d_center))
                g = int(155 + 55 * (1.0 - d_center))
                b = int(145 + 50 * (1.0 - d_center))
                lam.putpixel((ipx, ipy), (r, g, b, 255))

for t_bow in np.linspace(0.0, 1.0, 140):
    s_b = (1.0 - t_bow)**2 * 44.0 + 2.0 * (1.0 - t_bow) * t_bow * 26.0 + t_bow**2 * 11.0
    w_b = (1.0 - t_bow)**2 * (-9.2) + 2.0 * (1.0 - t_bow) * t_bow * (-7.5) + t_bow**2 * (-3.0)
    for dw in np.linspace(-0.7, 0.7, 5):
        px, py = to_xy(s_b, w_b + dw)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            lam.putpixel((ipx, ipy), (255, 215, 20, 255))

for s in np.linspace(50, 144, 800):
    if s < 56:
        w_max = 2.4 - 0.4 * ((s - 50) / 6.0)
    elif s < 132:
        t = (s - 56) / 76.0
        w_max = 2.0 - 0.8 * t
    else:
        t = (s - 132) / 12.0
        w_max = 1.2 * (1.0 - t)

    if w_max < 0.2: continue

    for w in np.linspace(-w_max, w_max, int(w_max * 24 + 10)):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if not (0 <= ipx < size and 0 <= ipy < size): continue

        dist_edge = w_max - abs(w)

        if dist_edge < 0.6:
            col = (255, 255, 255, 255)
        elif abs(w) < 0.45:
            col = (255, 255, 255, 255)
        elif w > 0:
            t = (w - 0.45) / (w_max - 0.45 + 1e-5)
            c = int(228 - 35 * t)
            col = (c, c + 3, c + 8, 255)
        else:
            t = (-w - 0.45) / (w_max - 0.45 + 1e-5)
            c = int(160 - 45 * t)
            col = (c, c + 5, c + 15, 255)
        lam.putpixel((ipx, ipy), col)

lam = add_pixel_outline(lam, (15, 30, 45, 255))
lam.save(os.path.join(out_dir, "lambent_light.png"))
print("Generated slender lambent_light.png")

print("All slender SAO textures successfully updated!")
