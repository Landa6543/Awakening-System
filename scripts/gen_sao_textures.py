import numpy as np
from PIL import Image
import math
import os

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
# 1. ELUCIDATOR (Kirito's Pitch-Black Demon Blade) - 128x128
# ==============================================================
elu = Image.new('RGBA', (size, size), (0, 0, 0, 0))

# Pommel (s=0..12)
# Silver spherical tip
for s in np.linspace(0, 4, 30):
    w_max = 3.6 * math.sin((s / 4.0) * math.pi * 0.5)
    for w in np.linspace(-w_max, w_max, 25):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            norm_w = w / (w_max + 1e-5)
            c = int(210 + 40 * (1.0 - norm_w * norm_w))
            elu.putpixel((ipx, ipy), (c - 8, c, min(255, c + 15), 255))

# Dark steel pommel cylinder (s=4..11)
for s in np.linspace(4, 11, 40):
    for w in np.linspace(-3.8, 3.8, 35):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            norm_w = abs(w) / 3.8
            base = int(28 + 32 * (1.0 - norm_w * norm_w))
            elu.putpixel((ipx, ipy), (base, base + 2, base + 6, 255))

# Silver spacer ring (s=11..13)
for s in np.linspace(11, 13, 20):
    for w in np.linspace(-4.2, 4.2, 35):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            norm_w = abs(w) / 4.2
            c = int(185 + 65 * (1.0 - norm_w))
            elu.putpixel((ipx, ipy), (c - 10, c, c + 12, 255))

# Grip (s=13..41)
for s in np.linspace(13, 41, 200):
    for w in np.linspace(-3.2, 3.2, 40):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            dist_ring = min(abs(s - 20), abs(s - 27), abs(s - 34))
            if dist_ring < 0.85:
                norm_w = abs(w) / 3.2
                c = int(195 + 55 * (1.0 - norm_w))
                elu.putpixel((ipx, ipy), (c - 8, c, c + 15, 255))
            elif dist_ring < 1.3:
                elu.putpixel((ipx, ipy), (14, 15, 18, 255))
            else:
                norm_w = abs(w) / 3.2
                base = int(22 + 18 * (1.0 - norm_w * norm_w))
                elu.putpixel((ipx, ipy), (base, base + 2, base + 5, 255))

# Crossguard (s=41..49)
# Right ring (w < 0) - circle at s=45.5, w=-11.5, radius=5.8
for s in np.linspace(39, 52, 70):
    for w in np.linspace(-18, -5, 70):
        dist = math.hypot(s - 45.5, w - (-11.5))
        if 2.8 <= dist <= 5.8:
            px, py = to_xy(s, w)
            ipx, ipy = int(round(px)), int(round(py))
            if 0 <= ipx < size and 0 <= ipy < size:
                if dist > 4.9 or dist < 3.5:
                    c = 215
                    elu.putpixel((ipx, ipy), (c - 5, c, c + 10, 255))
                else:
                    elu.putpixel((ipx, ipy), (50, 56, 68, 255))

# Center block
for s in np.linspace(41, 48.5, 55):
    for w in np.linspace(-7.5, 7.5, 75):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            if abs(w) > 6.6 or s < 41.8 or s > 47.7:
                elu.putpixel((ipx, ipy), (190, 200, 215, 255))
            else:
                elu.putpixel((ipx, ipy), (22, 25, 32, 255))

# Left crossbar (w > 0)
for s in np.linspace(42.8, 46.5, 35):
    for w in np.linspace(6.5, 19.5, 70):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            if w > 17.5:
                elu.putpixel((ipx, ipy), (220, 230, 245, 255))
            elif abs(s - 44.65) > 1.2:
                elu.putpixel((ipx, ipy), (145, 155, 172, 255))
            else:
                elu.putpixel((ipx, ipy), (32, 35, 45, 255))

# Blade (s=48..142)
for s in np.linspace(48, 142, 700):
    if s < 55:
        w_max = 5.2
    elif s < 120:
        w_max = 6.2
    elif s < 132:
        t = (s - 120) / 12.0
        w_max = 6.2 + 0.9 * t
    else:
        t = (s - 132) / 10.0
        w_max = 7.1 * (1.0 - t)

    if w_max < 0.2: continue

    for w in np.linspace(-w_max, w_max, int(w_max * 16 + 12)):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if not (0 <= ipx < size and 0 <= ipy < size): continue

        dist_edge = w_max - abs(w)

        if dist_edge < 1.3:
            edge_t = dist_edge / 1.3
            c = int(250 - 30 * edge_t)
            col = (c - 5, c, min(255, c + 8), 255)
        elif dist_edge < 2.2:
            col = (68, 78, 96, 255)
        else:
            norm_w = abs(w) / w_max
            if abs(w) < 1.1 and s < 126:
                c = int(48 + 24 * (1.0 - abs(w)/1.1))
                col = (c, c + 3, c + 8, 255)
            else:
                base = int(16 + 12 * (1.0 - norm_w))
                col = (base, base + 2, base + 5, 255)

        elu.putpixel((ipx, ipy), col)

elu = add_pixel_outline(elu, (10, 11, 14, 255))
elu.save(os.path.join(out_dir, "elucidator.png"))
print("Generated master elucidator.png")

# ==============================================================
# 2. DARK REPULSER (Kirito's Crystallite Dragon Longsword) - 128x128
# ==============================================================
rep = Image.new('RGBA', (size, size), (0, 0, 0, 0))

# Pommel (s=0..12)
for s in np.linspace(0, 12, 120):
    if s < 6:
        w_max = 1.0 + 4.8 * (s / 6.0)
    else:
        w_max = 5.8 * (1.0 - (s - 6.0) / 7.0) + 2.8 * ((s - 6.0) / 7.0)

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

# Grip (s=12..40)
for s in np.linspace(12, 40, 220):
    for w in np.linspace(-3.2, 3.2, 40):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if not (0 <= ipx < size and 0 <= ipy < size): continue

        wrap1 = abs((s * 1.4 + w * 2.2) % 8.0 - 4.0)
        wrap2 = abs((s * 1.4 - w * 2.2 + 2.0) % 8.0 - 4.0)
        is_wrap = wrap1 < 0.9 or wrap2 < 0.9
        if is_wrap:
            rep.putpixel((ipx, ipy), (238, 255, 250, 255))
        else:
            t = 1.0 - (abs(w) / 3.2)**2
            rep.putpixel((ipx, ipy), (int(22 + 35 * t), int(110 + 50 * t), int(100 + 45 * t), 255))

# Crossguard (s=38..52)
for w in np.linspace(-22, 22, 200):
    norm_w = abs(w) / 22.0
    s_center = 43.5 + 8.2 * (norm_w ** 1.35)
    s_thick = 4.4 * (1.0 - norm_w * 0.7)
    for s in np.linspace(s_center - s_thick, s_center + s_thick, 50):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if not (0 <= ipx < size and 0 <= ipy < size): continue

        if s > s_center:
            col = (115, 240, 218, 255)
        else:
            col = (30, 135, 120, 255)
        rep.putpixel((ipx, ipy), col)

# Central Diamond Core Gem (s=42..47, w=-4.5..4.5)
for s in np.linspace(41.5, 47.5, 60):
    for w in np.linspace(-4.5, 4.5, 60):
        d_val = abs(s - 44.5) / 3.0 + abs(w) / 4.5
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

# Blade (s=48..142)
for s in np.linspace(48, 142, 700):
    if s < 58:
        t = (s - 48) / 10.0
        w_max = 6.2 + 2.5 * math.sin(t * math.pi * 0.5)
    elif s < 125:
        t = (s - 58) / 67.0
        w_max = 8.7 - 1.5 * t
    else:
        t = (s - 125) / 17.0
        w_max = 7.2 * (1.0 - t)

    if w_max < 0.2: continue

    for w in np.linspace(-w_max, w_max, int(w_max * 18 + 12)):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if not (0 <= ipx < size and 0 <= ipy < size): continue

        dist_edge = w_max - abs(w)

        if dist_edge < 1.2:
            col = (220, 255, 250, 255)
        elif dist_edge < 2.2:
            col = (115, 235, 212, 255)
        else:
            ridge_dist = abs(w)
            if ridge_dist < 1.2:
                r_val = 1.0 - ridge_dist / 1.2
                col = (int(190 + 65 * r_val), 255, int(240 + 15 * r_val), 255)
            elif w > 0:
                t = (w - 1.2) / (w_max - 3.2 + 1e-5)
                r = int(70 + 50 * (1.0 - t))
                g = int(205 + 40 * (1.0 - t))
                b = int(185 + 40 * (1.0 - t))
                col = (r, g, b, 255)
            else:
                t = (-w - 1.2) / (w_max - 3.2 + 1e-5)
                r = int(22 + 40 * (1.0 - t))
                g = int(135 + 50 * (1.0 - t))
                b = int(125 + 45 * (1.0 - t))
                col = (r, g, b, 255)

        rep.putpixel((ipx, ipy), col)

rep = add_pixel_outline(rep, (8, 48, 40, 255))
rep.save(os.path.join(out_dir, "dark_repulser.png"))
print("Generated master dark_repulser.png")

# ==============================================================
# 3. LAMBENT LIGHT (Asuna's Masterwork Rapier) - 128x128
# ==============================================================
lam = Image.new('RGBA', (size, size), (0, 0, 0, 0))

# Pommel (s=0..12)
for s in np.linspace(0, 4, 30):
    w_max = 2.0 * math.sin((s / 4.0) * math.pi * 0.5)
    for w in np.linspace(-w_max, w_max, 20):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            lam.putpixel((ipx, ipy), (255, 218, 25, 255))

for s in np.linspace(4, 11, 55):
    for w in np.linspace(-4.5, 4.5, 55):
        dist = math.hypot(s - 7.5, w)
        if dist <= 3.8:
            px, py = to_xy(s, w)
            ipx, ipy = int(round(px)), int(round(py))
            if not (0 <= ipx < size and 0 <= ipy < size): continue
            if dist < 1.7:
                lam.putpixel((ipx, ipy), (45, 220, 195, 255))
            elif dist > 3.2:
                lam.putpixel((ipx, ipy), (175, 125, 15, 255))
            else:
                lam.putpixel((ipx, ipy), (255, 222, 65, 255))

for s in np.linspace(11, 13, 20):
    for w in np.linspace(-3.2, 3.2, 30):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            lam.putpixel((ipx, ipy), (235, 180, 10, 255))

# Grip (s=13..40)
for s in np.linspace(13, 40, 220):
    for w in np.linspace(-2.7, 2.7, 35):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if not (0 <= ipx < size and 0 <= ipy < size): continue
        is_wire = (s * 1.6 - w * 2.0) % 6.0 < 1.15
        if is_wire:
            lam.putpixel((ipx, ipy), (252, 210, 25, 255))
        else:
            norm_w = w / 2.7
            c = int(244 + 11 * (1.0 - norm_w * norm_w))
            lam.putpixel((ipx, ipy), (c, c, min(255, c + 3), 255))

# Cup-Guard (Swept Hilt Bell) (s=39..51)
for s in np.linspace(39, 51, 85):
    for w in np.linspace(-14, 14, 110):
        ellipse = ((s - 45.0) / 5.2)**2 + (w / 13.0)**2
        if ellipse <= 1.0:
            px, py = to_xy(s, w)
            ipx, ipy = int(round(px)), int(round(py))
            if not (0 <= ipx < size and 0 <= ipy < size): continue
            is_rim = ellipse > 0.76
            ang = math.atan2(w, (s - 45.0) * 2.5)
            is_spoke = abs(math.sin(ang * 4.0)) < 0.18
            if is_rim or is_spoke:
                if is_rim and ellipse > 0.88:
                    lam.putpixel((ipx, ipy), (175, 125, 15, 255))
                else:
                    lam.putpixel((ipx, ipy), (255, 225, 75, 255))
            else:
                d_center = math.sqrt(ellipse)
                r = int(12 + 25 * (1.0 - d_center))
                g = int(155 + 55 * (1.0 - d_center))
                b = int(145 + 50 * (1.0 - d_center))
                lam.putpixel((ipx, ipy), (r, g, b, 255))

# Golden Knuckleguard Bow
for t_bow in np.linspace(0.0, 1.0, 140):
    s_b = (1.0 - t_bow)**2 * 44.0 + 2.0 * (1.0 - t_bow) * t_bow * 26.0 + t_bow**2 * 11.0
    w_b = (1.0 - t_bow)**2 * (-12.0) + 2.0 * (1.0 - t_bow) * t_bow * (-10.0) + t_bow**2 * (-3.6)
    for dw in np.linspace(-0.8, 0.8, 5):
        px, py = to_xy(s_b, w_b + dw)
        ipx, ipy = int(round(px)), int(round(py))
        if 0 <= ipx < size and 0 <= ipy < size:
            lam.putpixel((ipx, ipy), (255, 215, 20, 255))

# Blade (s=50..144)
for s in np.linspace(50, 144, 750):
    if s < 56:
        w_max = 3.6 - 0.6 * ((s - 50) / 6.0)
    elif s < 132:
        t = (s - 56) / 76.0
        w_max = 3.0 - 1.2 * t
    else:
        t = (s - 132) / 12.0
        w_max = 1.8 * (1.0 - t)

    if w_max < 0.2: continue

    for w in np.linspace(-w_max, w_max, int(w_max * 20 + 12)):
        px, py = to_xy(s, w)
        ipx, ipy = int(round(px)), int(round(py))
        if not (0 <= ipx < size and 0 <= ipy < size): continue

        dist_edge = w_max - abs(w)

        if dist_edge < 0.8:
            col = (255, 255, 255, 255)
        elif abs(w) < 0.6:
            col = (255, 255, 255, 255)
        elif w > 0:
            t = (w - 0.6) / (w_max - 0.6 + 1e-5)
            c = int(228 - 35 * t)
            col = (c, c + 3, c + 8, 255)
        else:
            t = (-w - 0.6) / (w_max - 0.6 + 1e-5)
            c = int(160 - 45 * t)
            col = (c, c + 5, c + 15, 255)
        lam.putpixel((ipx, ipy), col)

lam = add_pixel_outline(lam, (15, 30, 45, 255))
lam.save(os.path.join(out_dir, "lambent_light.png"))
print("Generated master lambent_light.png")

print("All SAO master textures successfully updated!")
