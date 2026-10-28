import os
from PIL import Image

def create_sprite():
    return Image.new('RGBA', (32, 32), (0, 0, 0, 0))

# -------------------------------------------------------------
# 1. ALARM CLOCK (Despertador Uma Musume)
# -------------------------------------------------------------
def draw_alarm_clock():
    img = create_sprite()
    pix = img.load()

    c_out = (45, 12, 18, 255)
    c_red_hi = (245, 80, 95, 255)
    c_red_mid = (215, 35, 50, 255)
    c_red_dk = (145, 18, 30, 255)
    
    c_bell_hi = (255, 245, 150, 255)
    c_bell_mid = (235, 195, 40, 255)
    c_bell_dk = (160, 110, 15, 255)
    c_bell_out = (75, 45, 10, 255)

    c_face = (252, 250, 242, 255)
    c_face_sh = (215, 210, 200, 255)
    c_hand = (35, 35, 40, 255)

    # Brass Feet
    for pt in [(7, 27), (8, 27), (6, 28), (7, 28), (8, 28), (7, 29), (8, 29)]:
        pix[pt[0], pt[1]] = c_bell_out
    pix[7, 28] = c_bell_hi
    pix[8, 28] = c_bell_mid
    pix[8, 29] = c_bell_dk

    for pt in [(24, 27), (23, 27), (25, 28), (24, 28), (23, 28), (24, 29), (23, 29)]:
        pix[pt[0], pt[1]] = c_bell_out
    pix[24, 28] = c_bell_hi
    pix[23, 28] = c_bell_mid
    pix[23, 29] = c_bell_dk

    # Left Bell (cx=8, cy=5)
    for x in range(5, 12):
        for y in range(2, 9):
            d2 = (x - 8)**2 + (y - 5)**2
            if d2 <= 10.0:
                if d2 >= 6.5:
                    pix[x, y] = c_bell_out
                else:
                    pix[x, y] = c_bell_hi if (x <= 7 and y <= 4) else (c_bell_dk if y >= 6 else c_bell_mid)
    # Stems connecting bell to clock body
    pix[8, 8] = c_bell_out
    pix[9, 8] = c_bell_mid
    pix[9, 9] = c_bell_out

    # Right Bell (cx=23, cy=5)
    for x in range(20, 27):
        for y in range(2, 9):
            d2 = (x - 23)**2 + (y - 5)**2
            if d2 <= 10.0:
                if d2 >= 6.5:
                    pix[x, y] = c_bell_out
                else:
                    pix[x, y] = c_bell_hi if (x <= 22 and y <= 4) else (c_bell_dk if y >= 6 else c_bell_mid)
    # Stems connecting bell to clock body
    pix[23, 8] = c_bell_out
    pix[22, 8] = c_bell_mid
    pix[22, 9] = c_bell_out

    # Center Hammer
    for x in range(14, 18):
        pix[x, 5] = c_bell_out
        pix[x, 4] = c_bell_hi
    pix[15, 3] = c_bell_out
    pix[16, 3] = c_bell_out

    # Main Body Circle (center=15.5, 17.5, radius ~ 9.5)
    cx, cy, r = 15.5, 17.5, 9.5
    for x in range(4, 28):
        for y in range(6, 29):
            d2 = (x - cx)**2 + (y - cy)**2
            if d2 <= (r + 0.5)**2:
                if d2 >= (r - 0.7)**2:
                    pix[x, y] = c_out
                elif d2 >= (r - 2.4)**2:
                    if y < cy - 1 or x < cx - 1:
                        pix[x, y] = c_red_hi if (x < cx and y < cy) else c_red_mid
                    else:
                        pix[x, y] = c_red_dk
                elif d2 >= (r - 3.2)**2:
                    pix[x, y] = c_out
                else:
                    if y > cy + 2 or x > cx + 3:
                        pix[x, y] = c_face_sh
                    else:
                        pix[x, y] = c_face

    # Hour marks
    pix[15, 11] = (140, 140, 150, 255)
    pix[16, 11] = (140, 140, 150, 255)
    pix[22, 17] = (140, 140, 150, 255)
    pix[15, 23] = (140, 140, 150, 255)
    pix[9, 17] = (140, 140, 150, 255)

    # Hands (pointing to ~7:05)
    pix[14, 18] = c_hand
    pix[13, 19] = c_hand
    pix[12, 20] = c_hand

    pix[16, 16] = c_hand
    pix[17, 15] = c_hand
    pix[18, 14] = c_hand
    pix[19, 13] = c_hand

    pix[15, 17] = c_bell_mid
    pix[16, 17] = c_bell_hi

    return img

# -------------------------------------------------------------
# 2. TRAINING WHISTLE (Silbato de Entrenamiento)
# -------------------------------------------------------------
def draw_training_whistle():
    img = create_sprite()
    pix = img.load()

    c_out = (35, 40, 48, 255)
    c_hi = (255, 255, 255, 255)
    c_lt = (220, 228, 238, 255)
    c_mid = (165, 175, 190, 255)
    c_dk = (110, 120, 135, 255)
    c_hole = (25, 28, 35, 255)

    # Lanyard Loop ring attached cleanly at rear
    loop = [
        (25, 8), (26, 8), (27, 8),
        (24, 9), (28, 9),
        (24, 10), (28, 10),
        (24, 11), (28, 11),
        (25, 12), (26, 12), (27, 12)
    ]
    for pt in loop:
        pix[pt[0], pt[1]] = c_out
    pix[25, 9] = c_hi
    pix[26, 9] = c_lt
    pix[27, 9] = c_mid
    pix[25, 10] = c_lt
    pix[26, 10] = c_mid
    pix[27, 10] = c_dk
    pix[25, 11] = c_mid
    pix[26, 11] = c_dk
    pix[27, 11] = c_dk

    # Whistle Drum Cylinder (center=19.5, 16.5, r=5.5)
    cx, cy, r = 19.5, 16.5, 5.5
    for x in range(14, 26):
        for y in range(11, 23):
            d2 = (x - cx)**2 + (y - cy)**2
            if d2 <= (r + 0.5)**2:
                if d2 >= (r - 0.7)**2:
                    pix[x, y] = c_out
                else:
                    if x <= cx and y <= cy:
                        pix[x, y] = c_hi if (x <= cx - 1 and y <= cy - 1) else c_lt
                    elif y >= cy + 1:
                        pix[x, y] = c_dk
                    else:
                        pix[x, y] = c_mid

    # Sound slit hole & visible pea
    for x in range(17, 22):
        pix[x, 14] = c_hole
    pix[18, 15] = (210, 180, 75, 255)
    pix[19, 15] = (160, 125, 45, 255)

    # Mouthpiece Tube (x=4..15, y=14..22)
    for x in range(4, 16):
        for y in range(14, 23):
            is_edge = (x == 4 and 16 <= y <= 21) or (y == 15 and 4 <= x <= 15) or (y == 22 and 4 <= x <= 15)
            if is_edge:
                pix[x, y] = c_out
            elif 5 <= x <= 15 and 16 <= y <= 21:
                if y == 16:
                    pix[x, y] = c_hi
                elif y in (17, 18):
                    pix[x, y] = c_lt
                elif y in (19, 20):
                    pix[x, y] = c_mid
                else:
                    pix[x, y] = c_dk

    # Lip stop ridge at x=7
    for y in range(16, 22):
        pix[7, y] = c_lt
        pix[8, y] = c_out

    return img

# -------------------------------------------------------------
# 3. HORSESHOES (Silver & Gold)
# -------------------------------------------------------------
def draw_horseshoe(is_gold=False):
    img = create_sprite()
    pix = img.load()

    if is_gold:
        c_out = (95, 52, 10, 255)
        c_hi = (255, 250, 180, 255)
        c_lt = (250, 210, 50, 255)
        c_mid = (215, 160, 20, 255)
        c_dk = (160, 105, 10, 255)
        c_hole = (70, 35, 5, 255)
    else:
        c_out = (35, 42, 52, 255)
        c_hi = (255, 255, 255, 255)
        c_lt = (225, 235, 245, 255)
        c_mid = (175, 185, 200, 255)
        c_dk = (115, 125, 140, 255)
        c_hole = (25, 30, 40, 255)

    cx, cy = 15.5, 16.5
    r_out, r_in = 11.5, 6.0

    for x in range(3, 29):
        for y in range(6, 29):
            if y < 13 and 12 <= x <= 19:
                continue
            d2 = (x - cx)**2 + (y - cy)**2
            if d2 <= (r_out + 0.5)**2 and d2 >= (r_in - 0.5)**2:
                is_edge = (d2 >= (r_out - 0.8)**2 or d2 <= (r_in + 0.8)**2 or (y == 6 and (x <= 11 or x >= 20)))
                if is_edge:
                    pix[x, y] = c_out
                else:
                    if x < 12:
                        pix[x, y] = c_hi if x <= 7 else (c_lt if x <= 9 else c_mid)
                    elif x > 19:
                        pix[x, y] = c_dk if x >= 24 else (c_mid if x >= 22 else c_lt)
                    else:
                        pix[x, y] = c_dk if y >= 24 else (c_mid if y >= 21 else c_lt)

    # Solid rounded calks at the top tips
    for x in range(6, 12):
        pix[x, 6] = c_out
        pix[x, 7] = c_hi if x < 9 else c_lt
    for x in range(20, 26):
        pix[x, 6] = c_out
        pix[x, 7] = c_lt if x < 23 else c_dk

    # 6 Nail Holes
    for hx, hy in [(7, 11), (6, 16), (7, 21)]:
        pix[hx, hy] = c_hole
        pix[hx+1, hy] = c_hole
        pix[hx, hy+1] = c_hole
        pix[hx+1, hy+1] = c_hole
        pix[hx, hy-1] = c_hi

    for hx, hy in [(23, 11), (24, 16), (23, 21)]:
        pix[hx, hy] = c_hole
        pix[hx+1, hy] = c_hole
        pix[hx, hy+1] = c_hole
        pix[hx+1, hy+1] = c_hole
        pix[hx, hy-1] = c_lt

    if is_gold:
        pix[5, 5] = (255, 255, 255, 255)
        pix[6, 5] = (255, 245, 180, 255)
        pix[26, 5] = (255, 255, 255, 255)
        pix[25, 5] = (255, 245, 180, 255)

    return img

# -------------------------------------------------------------
# 4. ANKLE WEIGHTS (Pair of Heavy Athletic Cuffs)
# -------------------------------------------------------------
def draw_ankle_weights():
    img = create_sprite()
    pix = img.load()

    c_out = (18, 22, 28, 255)
    c_strap_hi = (70, 195, 245, 255)
    c_strap_mid = (20, 135, 195, 255)
    c_pad_hi = (95, 105, 120, 255)
    c_pad_mid = (55, 62, 75, 255)
    c_pad_dk = (35, 40, 50, 255)
    c_metal_hi = (250, 252, 255, 255)
    c_metal_mid = (190, 200, 215, 255)
    c_metal_dk = (110, 120, 135, 255)

    # Front cuff: x=4..26, y=10..24
    for x in range(4, 27):
        for y in range(10, 25):
            if (x in (4, 26) and y in (10, 24)):
                continue
            if x == 4 or x == 26 or y == 10 or y == 24:
                pix[x, y] = c_out
            else:
                pix[x, y] = c_pad_mid

    for div_x in [9, 14, 19]:
        for y in range(11, 24):
            pix[div_x, y] = c_out

    pouches = [(5, 8), (10, 13), (15, 18), (20, 25)]
    for x1, x2 in pouches:
        for x in range(x1, x2 + 1):
            pix[x, 11] = c_pad_hi
            pix[x, 12] = c_pad_hi
            pix[x, 22] = c_pad_dk
            pix[x, 23] = c_pad_dk

    # Two Horizontal Cyan Athletic Straps across pouches (y=14 and y=19)
    for x in range(4, 27):
        if pix[x, 14] != c_out:
            pix[x, 14] = c_strap_hi
        if pix[x, 15] != c_out:
            pix[x, 15] = c_strap_mid
        if pix[x, 19] != c_out:
            pix[x, 19] = c_strap_hi
        if pix[x, 20] != c_out:
            pix[x, 20] = c_strap_mid

    # Metal D-Ring Buckles on Left side
    for y in [13, 14, 15, 16]:
        pix[2, y] = c_out
        pix[3, y] = c_metal_hi if y == 13 else c_metal_mid
    pix[3, 16] = c_metal_dk

    for y in [18, 19, 20, 21]:
        pix[2, y] = c_out
        pix[3, y] = c_metal_hi if y == 18 else c_metal_mid
    pix[3, 21] = c_metal_dk

    # Flap tabs on right
    for y in [14, 15]:
        pix[27, y] = c_strap_mid
        pix[28, y] = c_strap_hi
        pix[29, y] = c_out
    for y in [19, 20]:
        pix[27, y] = c_strap_mid
        pix[28, y] = c_strap_hi
        pix[29, y] = c_out

    # Back cuff peeking from top-left (y=6..10, x=8..28)
    for x in range(8, 28):
        for y in range(6, 11):
            if pix[x, y] == (0, 0, 0, 0):
                if y == 6 or x == 27:
                    pix[x, y] = c_out
                elif y == 7:
                    pix[x, y] = c_pad_hi
                else:
                    pix[x, y] = c_pad_mid

    return img

# -------------------------------------------------------------
# 5. GACHA TICKET (Blue)
# -------------------------------------------------------------
def draw_gacha_ticket():
    img = create_sprite()
    pix = img.load()

    c_out = (12, 20, 48, 255)
    c_gold_hi = (255, 245, 160, 255)
    c_gold_mid = (235, 195, 45, 255)
    c_gold_dk = (160, 115, 20, 255)

    c_bg_hi = (60, 175, 250, 255)
    c_bg_mid = (25, 110, 215, 255)
    c_bg_dk = (15, 55, 145, 255)

    c_star_hi = (255, 255, 255, 255)
    c_star_mid = (140, 240, 255, 255)

    for x in range(3, 29):
        for y in range(8, 24):
            if (x in (3, 28) and y in (8, 23)) or (x in (4, 27) and y in (8, 23)) or (x in (3, 28) and y in (9, 22)):
                continue
            if (x in (3, 4) or x in (27, 28)) and y in (15, 16):
                continue

            is_edge = (x in (3, 28) or y in (8, 23) or
                       (x in (4, 27) and y in (9, 22)) or
                       (x in (5, 26) and y in (15, 16)))
            if is_edge:
                pix[x, y] = c_out
            else:
                is_frame = (x in (5, 26) or y in (10, 21))
                if is_frame:
                    pix[x, y] = c_gold_hi if (x < 15 and y < 15) else c_gold_mid
                else:
                    if y <= 13:
                        pix[x, y] = c_bg_hi if x <= 14 else c_bg_mid
                    elif y <= 18:
                        pix[x, y] = c_bg_mid
                    else:
                        pix[x, y] = c_bg_dk

    for nx, ny in [(3, 10), (4, 9), (5, 8), (28, 10), (27, 9), (26, 8),
                   (3, 21), (4, 22), (5, 23), (28, 21), (27, 22), (26, 23)]:
        pix[nx, ny] = c_out
    for y in [14, 17]:
        pix[3, y] = c_out
        pix[4, y] = c_out
        pix[27, y] = c_out
        pix[28, y] = c_out
    pix[5, 15] = c_out
    pix[5, 16] = c_out
    pix[26, 15] = c_out
    pix[26, 16] = c_out

    # 8-Pointed Star in Center
    for y in range(12, 20):
        pix[15, y] = c_star_hi
        pix[16, y] = c_star_hi
    for x in range(12, 20):
        pix[x, 15] = c_star_hi
        pix[x, 16] = c_star_hi

    pix[13, 13] = c_star_mid
    pix[14, 14] = c_star_hi
    pix[18, 13] = c_star_mid
    pix[17, 14] = c_star_hi
    pix[13, 18] = c_star_mid
    pix[14, 17] = c_star_hi
    pix[18, 18] = c_star_mid
    pix[17, 17] = c_star_hi

    pix[15, 15] = (255, 255, 255, 255)
    pix[16, 15] = (255, 255, 255, 255)
    pix[15, 16] = (255, 255, 255, 255)
    pix[16, 16] = (255, 255, 255, 255)

    pix[8, 12] = c_gold_hi
    pix[23, 12] = c_gold_hi
    pix[8, 19] = c_gold_hi
    pix[23, 19] = c_gold_hi

    return img

# -------------------------------------------------------------
# 6. GOLDEN GACHA TICKET (SSR)
# -------------------------------------------------------------
def draw_golden_gacha_ticket():
    img = create_sprite()
    pix = img.load()

    c_out = (85, 45, 10, 255)
    c_gold_hi = (255, 250, 180, 255)
    c_gold_lt = (255, 225, 75, 255)
    c_gold_mid = (240, 185, 30, 255)
    c_gold_dk = (185, 125, 15, 255)
    c_ruby = (230, 35, 60, 255)
    c_star_hi = (255, 255, 255, 255)

    for x in range(3, 29):
        for y in range(8, 24):
            if (x in (3, 28) and y in (8, 23)) or (x in (4, 27) and y in (8, 23)) or (x in (3, 28) and y in (9, 22)):
                continue
            if (x in (3, 4) or x in (27, 28)) and y in (15, 16):
                continue

            is_edge = (x in (3, 28) or y in (8, 23) or
                       (x in (4, 27) and y in (9, 22)) or
                       (x in (5, 26) and y in (15, 16)))
            if is_edge:
                pix[x, y] = c_out
            else:
                is_frame = (x in (5, 26) or y in (10, 21))
                if is_frame:
                    pix[x, y] = c_gold_hi if (x < 15 and y < 15) else c_gold_mid
                else:
                    if y <= 13:
                        pix[x, y] = c_gold_hi if x <= 15 else c_gold_lt
                    elif y <= 18:
                        pix[x, y] = c_gold_lt if x <= 15 else c_gold_mid
                    else:
                        pix[x, y] = c_gold_dk

    for nx, ny in [(3, 10), (4, 9), (5, 8), (28, 10), (27, 9), (26, 8),
                   (3, 21), (4, 22), (5, 23), (28, 21), (27, 22), (26, 23)]:
        pix[nx, ny] = c_out
    for y in [14, 17]:
        pix[3, y] = c_out
        pix[4, y] = c_out
        pix[27, y] = c_out
        pix[28, y] = c_out
    pix[5, 15] = c_out
    pix[5, 16] = c_out
    pix[26, 15] = c_out
    pix[26, 16] = c_out

    # Inner Gold Border Line
    for x in range(6, 26):
        pix[x, 11] = c_gold_hi if x < 15 else c_gold_lt
        pix[x, 20] = c_gold_dk
    for y in range(12, 20):
        pix[6, y] = c_gold_hi
        pix[25, y] = c_gold_dk

    # Center Radiant 8-Pointed Star
    for y in range(12, 20):
        pix[15, y] = c_star_hi
        pix[16, y] = c_star_hi
    for x in range(12, 20):
        pix[x, 15] = c_star_hi
        pix[x, 16] = c_star_hi

    pix[13, 13] = c_gold_hi
    pix[14, 14] = c_star_hi
    pix[18, 13] = c_gold_hi
    pix[17, 14] = c_star_hi
    pix[13, 18] = c_gold_hi
    pix[14, 17] = c_star_hi
    pix[18, 18] = c_gold_hi
    pix[17, 17] = c_star_hi

    # Ruby Jewel on center of star
    pix[15, 15] = c_ruby
    pix[16, 15] = (255, 120, 140, 255) # ruby highlight
    pix[15, 16] = c_ruby
    pix[16, 16] = (160, 10, 25, 255)   # ruby shadow

    # Corner filigree jewels
    pix[8, 12] = c_ruby
    pix[23, 12] = (35, 150, 240, 255)
    pix[8, 19] = (35, 150, 240, 255)
    pix[23, 19] = c_ruby

    return img

# -------------------------------------------------------------
# 7. CLOVER SPARK (Amuleto de la Suerte Uma Musume)
# -------------------------------------------------------------
def draw_clover_spark():
    img = create_sprite()
    pix = img.load()

    c_out = (85, 45, 10, 255)
    c_gold_hi = (255, 250, 180, 255)
    c_gold_mid = (240, 195, 40, 255)
    c_gold_dk = (175, 120, 15, 255)

    c_bg_dark = (12, 55, 24, 255)
    c_clover_hi = (165, 250, 145, 255)
    c_clover_mid = (65, 210, 90, 255)
    c_clover_dk = (20, 130, 45, 255)
    c_clover_out = (8, 65, 22, 255)

    cx, cy, r = 15.5, 15.5, 12.0

    for x in range(3, 29):
        for y in range(3, 29):
            d2 = (x - cx)**2 + (y - cy)**2
            if d2 <= (r + 0.5)**2:
                if d2 >= (r - 0.7)**2:
                    pix[x, y] = c_out
                elif d2 >= (r - 2.5)**2:
                    pix[x, y] = c_gold_hi if (x < cx and y < cy) else (c_gold_mid if x < cx + 3 else c_gold_dk)
                elif d2 >= (r - 3.4)**2:
                    pix[x, y] = c_out
                else:
                    pix[x, y] = c_bg_dark

    for rx, ry in [(15, 4), (16, 4), (15, 27), (16, 27), (4, 15), (4, 16), (27, 15), (27, 16),
                   (8, 8), (23, 8), (8, 23), (23, 23)]:
        pix[rx, ry] = c_gold_hi

    # 4-Leaf Clover
    leaves = [(15.5, 11.5), (15.5, 19.5), (11.5, 15.5), (19.5, 15.5)]
    for lcx, lcy in leaves:
        for x in range(int(lcx - 4), int(lcx + 5)):
            for y in range(int(lcy - 4), int(lcy + 5)):
                d2 = (x - lcx)**2 + (y - lcy)**2
                if d2 <= 9.0:
                    if d2 >= 6.5:
                        pix[x, y] = c_clover_out
                    else:
                        pix[x, y] = c_clover_hi if (x <= lcx and y <= lcy) else (c_clover_mid if y <= lcy + 1 else c_clover_dk)

    pix[15, 15] = (255, 255, 255, 255)
    pix[16, 15] = (255, 255, 255, 255)
    pix[15, 16] = (255, 255, 255, 255)
    pix[16, 16] = (255, 255, 255, 255)

    pix[15, 22] = c_clover_dk
    pix[16, 23] = c_clover_out
    pix[16, 24] = c_clover_out

    return img

# -------------------------------------------------------------
# 8. TRACK MARKER WAND (Bastón Mágico de Pista)
# -------------------------------------------------------------
def draw_track_marker_wand():
    img = create_sprite()
    pix = img.load()

    c_out = (75, 45, 10, 255)
    c_gold_hi = (255, 245, 160, 255)
    c_gold_mid = (235, 190, 40, 255)
    c_gold_dk = (165, 115, 15, 255)

    c_orb_out = (10, 65, 95, 255)
    c_orb_hi = (255, 255, 255, 255)
    c_orb_lt = (140, 245, 255, 255)
    c_orb_mid = (40, 195, 235, 255)
    c_orb_dk = (15, 125, 170, 255)

    # Diagonal Staff
    for i in range(18):
        x = 4 + i
        y = 27 - i
        pix[x, y] = c_gold_mid
        pix[x - 1, y] = c_out
        pix[x, y + 1] = c_out
        pix[x + 1, y - 1] = c_gold_hi

    pix[3, 27] = c_gold_hi
    pix[4, 28] = c_gold_dk
    pix[3, 28] = c_orb_lt

    pix[11, 20] = c_gold_hi
    pix[12, 19] = c_out
    pix[13, 18] = c_gold_hi

    # Magical Orb at head
    ox, oy, orad = 23.5, 8.5, 4.2
    for x in range(18, 29):
        for y in range(3, 14):
            d2 = (x - ox)**2 + (y - oy)**2
            if d2 <= (orad + 0.5)**2:
                if d2 >= (orad - 0.7)**2:
                    pix[x, y] = c_orb_out
                else:
                    if x <= ox and y <= oy:
                        pix[x, y] = c_orb_hi if (x <= ox - 1 and y <= oy - 1) else c_orb_lt
                    elif y >= oy + 1:
                        pix[x, y] = c_orb_dk
                    else:
                        pix[x, y] = c_orb_mid

    # Wing cradle brackets
    for pt in [(18, 12), (19, 13), (20, 13), (21, 14), (22, 13), (23, 13),
               (18, 8), (17, 7), (17, 6), (28, 12), (28, 11), (29, 10)]:
        if 0 <= pt[0] < 32 and 0 <= pt[1] < 32:
            pix[pt[0], pt[1]] = c_out
    pix[18, 7] = c_gold_hi
    pix[19, 12] = c_gold_mid
    pix[22, 12] = c_gold_hi
    pix[28, 11] = c_gold_hi

    pix[23, 4] = c_orb_lt
    pix[24, 4] = c_orb_hi
    pix[28, 8] = c_orb_hi
    pix[28, 9] = c_orb_lt
    pix[22, 7] = (255, 255, 255, 255)

    return img

# -------------------------------------------------------------
# 9. FABLE SCROLL (Pergamino Ancestral Místico)
# -------------------------------------------------------------
def draw_fable_scroll():
    img = create_sprite()
    pix = img.load()

    c_out = (55, 35, 18, 255)
    c_wood_hi = (210, 165, 90, 255)
    c_wood_mid = (160, 115, 55, 255)
    c_wood_dk = (105, 70, 30, 255)

    c_paper_hi = (255, 248, 225, 255)
    c_paper_lt = (240, 225, 185, 255)
    c_paper_mid = (215, 195, 150, 255)
    c_paper_dk = (165, 140, 95, 255)

    c_ribbon = (215, 30, 45, 255)
    c_ribbon_dk = (135, 15, 25, 255)
    c_gold = (245, 205, 50, 255)
    c_rune = (45, 160, 240, 255)

    # Rolled parchment paper body (x=8..24, y=10..22)
    for x in range(8, 25):
        for y in range(10, 23):
            if y == 10 or y == 22:
                pix[x, y] = c_out
            elif y in (11, 12):
                pix[x, y] = c_paper_hi
            elif y in (13, 14, 15, 16):
                pix[x, y] = c_paper_lt
            elif y in (17, 18, 19):
                pix[x, y] = c_paper_mid
            else:
                pix[x, y] = c_paper_dk

    # Left Rolled Spindle Cylinder (x=5..7, y=8..24)
    for y in range(8, 25):
        pix[5, y] = c_out
        pix[7, y] = c_out
        pix[6, y] = c_wood_hi if y < 14 else (c_wood_mid if y < 20 else c_wood_dk)
    # Left Golden Knobs
    pix[6, 7] = c_gold
    pix[6, 25] = c_gold

    # Right Rolled Spindle Cylinder (x=25..27, y=8..24)
    for y in range(8, 25):
        pix[25, y] = c_out
        pix[27, y] = c_out
        pix[26, y] = c_wood_hi if y < 14 else (c_wood_mid if y < 20 else c_wood_dk)
    # Right Golden Knobs
    pix[26, 7] = c_gold
    pix[26, 25] = c_gold

    # Crimson Ribbon tied vertically around center (x=15..17, y=9..23)
    for x in range(15, 18):
        for y in range(9, 24):
            if y == 9 or y == 23:
                pix[x, y] = c_out
            else:
                pix[x, y] = c_ribbon if y <= 15 else c_ribbon_dk

    # Golden Seal Medallion in center of ribbon
    pix[15, 15] = (255, 245, 160, 255)
    pix[16, 15] = c_gold
    pix[17, 15] = c_out
    pix[15, 16] = c_gold
    pix[16, 16] = (180, 130, 20, 255)
    pix[17, 16] = c_out

    # Glowing Magical Runes on visible paper
    pix[10, 13] = c_rune
    pix[11, 14] = c_rune
    pix[12, 13] = c_rune
    pix[10, 18] = c_rune
    pix[12, 17] = c_rune

    pix[20, 13] = c_rune
    pix[21, 14] = c_rune
    pix[22, 13] = c_rune
    pix[20, 17] = c_rune
    pix[22, 18] = c_rune

    return img

# -------------------------------------------------------------
# 10. GACHA CAPSULES (Standard Blue, Gold SSR, Purple SR)
# -------------------------------------------------------------
def draw_gacha_capsule(capsule_type='blue'):
    img = create_sprite()
    pix = img.load()

    c_out = (25, 30, 42, 255)
    c_white_hi = (255, 255, 255, 255)
    c_white_lt = (235, 240, 248, 255)
    c_white_mid = (195, 205, 220, 255)
    c_white_dk = (145, 155, 175, 255)

    if capsule_type == 'gold':
        c_top_hi = (255, 250, 180, 255)
        c_top_lt = (255, 225, 75, 255)
        c_top_mid = (240, 185, 30, 255)
        c_top_dk = (175, 115, 15, 255)
        c_out = (75, 45, 10, 255)
    elif capsule_type == 'purple':
        c_top_hi = (255, 185, 255, 255)
        c_top_lt = (220, 110, 250, 255)
        c_top_mid = (165, 45, 205, 255)
        c_top_dk = (110, 20, 145, 255)
        c_out = (45, 15, 65, 255)
    else: # Blue
        c_top_hi = (140, 245, 255, 255)
        c_top_lt = (60, 175, 250, 255)
        c_top_mid = (25, 110, 225, 255)
        c_top_dk = (15, 60, 155, 255)
        c_out = (15, 25, 55, 255)

    cx, cy, r = 15.5, 15.5, 9.5

    for x in range(4, 28):
        for y in range(4, 28):
            d2 = (x - cx)**2 + (y - cy)**2
            if d2 <= (r + 0.5)**2:
                if d2 >= (r - 0.7)**2:
                    pix[x, y] = c_out
                elif y == 15 or y == 16:
                    pix[x, y] = c_white_hi if x <= 14 else c_white_lt
                elif y < 15:
                    if x <= cx and y <= cy:
                        pix[x, y] = c_top_hi if (x <= cx - 2 and y <= cy - 2) else c_top_lt
                    elif y >= cy - 2:
                        pix[x, y] = c_top_mid
                    else:
                        pix[x, y] = c_top_dk
                else:
                    if x <= cx:
                        pix[x, y] = c_white_lt
                    elif y >= 22:
                        pix[x, y] = c_white_dk
                    else:
                        pix[x, y] = c_white_mid

    # Equator Latch Clasp on Left
    pix[7, 15] = c_white_hi
    pix[7, 16] = c_white_hi
    pix[8, 15] = c_out
    pix[8, 16] = c_out

    # Glossy Specular Dot
    pix[10, 8] = (255, 255, 255, 255)
    pix[11, 8] = (255, 255, 255, 255)
    pix[10, 9] = (255, 255, 255, 255)

    return img

# -------------------------------------------------------------
# 11. COMPANION CONTRACT (Pacto con Pluma de Élite)
# -------------------------------------------------------------
def draw_companion_contract():
    img = create_sprite()
    pix = img.load()

    c_out = (55, 38, 22, 255)
    c_paper_hi = (255, 250, 230, 255)
    c_paper_lt = (240, 225, 185, 255)
    c_paper_mid = (215, 190, 145, 255)
    c_paper_dk = (165, 135, 95, 255)

    c_seal = (205, 30, 45, 255)
    c_seal_hi = (255, 80, 95, 255)
    c_seal_dk = (130, 15, 25, 255)

    c_quill_hi = (255, 255, 255, 255)
    c_quill_mid = (200, 230, 255, 255)
    c_quill_dk = (120, 160, 200, 255)
    c_quill_stem = (40, 45, 55, 255)

    # Diagonal Parchment Sheet
    for x in range(5, 25):
        for y in range(7, 27):
            if x >= 21 and y <= 10:
                continue
            is_edge = (x == 5 or x == 24 or y == 7 or y == 26)
            if is_edge:
                pix[x, y] = c_out
            else:
                pix[x, y] = c_paper_hi if (x < 14 and y < 16) else (c_paper_lt if y < 22 else c_paper_mid)

    # Folded Corner Triangle
    for x in range(20, 25):
        for y in range(7, 11):
            if x + y >= 30:
                pix[x, y] = c_paper_dk if x == 20 or y == 10 else c_paper_mid
    pix[20, 7] = c_out
    pix[20, 8] = c_out
    pix[20, 9] = c_out
    pix[20, 10] = c_out
    pix[21, 10] = c_out
    pix[22, 10] = c_out
    pix[23, 10] = c_out
    pix[24, 10] = c_out

    # Text lines on parchment
    for ty in [11, 14, 17, 20]:
        for tx in range(8, 20):
            if tx % 4 != 0:
                pix[tx, ty] = (120, 100, 75, 255)

    # Red Wax Seal at bottom-left
    for x in range(7, 13):
        for y in range(20, 26):
            d2 = (x - 9.5)**2 + (y - 22.5)**2
            if d2 <= 9.0:
                if d2 >= 6.5:
                    pix[x, y] = c_seal_dk
                else:
                    pix[x, y] = c_seal_hi if x <= 9 and y <= 22 else c_seal

    # Feather Quill resting across
    for i in range(18):
        qx = 10 + i
        qy = 25 - i
        pix[qx, qy] = c_quill_stem
        if i >= 5:
            # White fluffy quill vanes
            pix[qx - 1, qy] = c_quill_hi
            pix[qx - 2, qy] = c_quill_hi
            pix[qx, qy - 1] = c_quill_mid
            pix[qx + 1, qy - 1] = c_quill_dk

    return img

# -------------------------------------------------------------
# 12. ELITE CARAT (Zanahoria Legendaria SSR)
# -------------------------------------------------------------
def draw_elite_carat():
    img = create_sprite()
    pix = img.load()

    c_out = (95, 45, 10, 255)
    c_carrot_hi = (255, 240, 140, 255)
    c_carrot_lt = (255, 185, 30, 255)
    c_carrot_mid = (245, 135, 15, 255)
    c_carrot_dk = (195, 85, 10, 255)

    c_leaf_out = (15, 60, 20, 255)
    c_leaf_hi = (140, 245, 90, 255)
    c_leaf_mid = (50, 190, 60, 255)
    c_leaf_dk = (20, 120, 35, 255)

    # Lush Green Leaves on top
    leaf_pixels = [
        # Center tuft
        ((15, 2), c_leaf_out), ((16, 2), c_leaf_out),
        ((15, 3), c_leaf_hi), ((16, 3), c_leaf_mid),
        ((14, 4), c_leaf_out), ((15, 4), c_leaf_hi), ((16, 4), c_leaf_mid), ((17, 4), c_leaf_out),
        ((14, 5), c_leaf_mid), ((15, 5), c_leaf_mid), ((16, 5), c_leaf_dk), ((17, 5), c_leaf_dk),
        # Left tuft
        ((12, 4), c_leaf_out), ((13, 4), c_leaf_hi), ((12, 5), c_leaf_out), ((13, 5), c_leaf_mid),
        # Right tuft
        ((19, 4), c_leaf_out), ((18, 4), c_leaf_hi), ((19, 5), c_leaf_out), ((18, 5), c_leaf_dk),
        # Base stem
        ((15, 6), c_leaf_dk), ((16, 6), c_leaf_dk), ((15, 7), c_leaf_dk), ((16, 7), c_leaf_dk)
    ]
    for pt, col in leaf_pixels:
        pix[pt[0], pt[1]] = col

    # Carrot Body
    widths = [
        (8, 12, 19), (9, 11, 20), (10, 11, 20), (11, 11, 20),
        (12, 11, 20), (13, 11, 20), (14, 12, 19), (15, 12, 19),
        (16, 12, 19), (17, 12, 19), (18, 13, 18), (19, 13, 18),
        (20, 13, 18), (21, 13, 18), (22, 14, 17), (23, 14, 17),
        (24, 14, 17), (25, 14, 17), (26, 15, 16), (27, 15, 16),
        (28, 15, 16)
    ]
    for y, x1, x2 in widths:
        for x in range(x1, x2 + 1):
            if x == x1 or x == x2:
                pix[x, y] = c_out
            else:
                if x <= x1 + 2:
                    pix[x, y] = c_carrot_hi if (y % 4 in (1, 2)) else c_carrot_lt
                elif x >= x2 - 2:
                    pix[x, y] = c_carrot_dk
                else:
                    pix[x, y] = c_carrot_mid

    for ry in [12, 16, 20, 24]:
        pix[13, ry] = c_out
        pix[14, ry] = c_out
        pix[15, ry] = c_out

    # Golden Sparkles
    for sx, sy in [(8, 12), (23, 11), (7, 20), (24, 19), (10, 26), (21, 25)]:
        pix[sx, sy] = (255, 245, 160, 255)

    return img

# -------------------------------------------------------------
# MAIN GENERATOR & SAVE
# -------------------------------------------------------------
target_dir = r'src/main/resources/assets/awakening_system/textures/item'
os.makedirs(target_dir, exist_ok=True)

generators = {
    'alarm_clock.png': draw_alarm_clock,
    'training_whistle.png': draw_training_whistle,
    'silver_horseshoe.png': lambda: draw_horseshoe(False),
    'gold_horseshoe.png': lambda: draw_horseshoe(True),
    'ankle_weights.png': draw_ankle_weights,
    'gacha_ticket.png': draw_gacha_ticket,
    'golden_gacha_ticket.png': draw_golden_gacha_ticket,
    'clover_spark.png': draw_clover_spark,
    'track_marker_wand.png': draw_track_marker_wand,
    'fable_scroll.png': draw_fable_scroll,
    'gacha_capsule.png': lambda: draw_gacha_capsule('blue'),
    'gacha_capsule_gold.png': lambda: draw_gacha_capsule('gold'),
    'gacha_capsule_purple.png': lambda: draw_gacha_capsule('purple'),
    'elite_carat.png': draw_elite_carat
}

for name, gen in generators.items():
    sprite = gen()
    path = os.path.join(target_dir, name)
    sprite.save(path)
    alphas = set(p[3] for p in sprite.getdata())
    print(f'Saved {name}: size={sprite.size}, alphas={alphas}')

print('All 15 pixel art textures generated and saved!')
