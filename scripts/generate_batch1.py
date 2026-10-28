import os
from PIL import Image, ImageDraw

def create_sprite():
    return Image.new('RGBA', (32, 32), (0, 0, 0, 0))

def draw_alarm_clock():
    img = create_sprite()
    pixels = img.load()

    # Red Classic Twin-bell Alarm Clock
    # Colors:
    c_out = (50, 12, 18, 255)       # dark red/maroon outline
    c_red_hi = (245, 85, 95, 255)   # red highlight
    c_red_mid = (215, 35, 50, 255)  # red base
    c_red_dk = (150, 18, 30, 255)   # red shadow
    
    c_bell_hi = (255, 245, 160, 255) # brass highlight
    c_bell_mid = (235, 195, 45, 255) # brass mid
    c_bell_dk = (160, 115, 15, 255)  # brass shadow
    c_bell_out = (80, 50, 10, 255)   # brass outline

    c_face = (252, 250, 242, 255)    # clock face white
    c_face_sh = (220, 215, 205, 255) # clock face shadow
    c_hand = (35, 35, 40, 255)       # hands

    # Feet (bottom left: (8,29)-(9,30), bottom right: (22,29)-(23,30))
    for x, y in [(7, 28), (8, 29), (9, 30), (7, 29), (8, 30)]:
        pixels[x, y] = c_bell_out
    pixels[8, 29] = c_bell_mid
    pixels[7, 29] = c_bell_hi

    for x, y in [(24, 28), (23, 29), (22, 30), (24, 29), (23, 30)]:
        pixels[x, y] = c_bell_out
    pixels[23, 29] = c_bell_mid
    pixels[24, 29] = c_bell_hi

    # Bells on top
    # Left Bell (cx=8, cy=6, radius~4)
    bell_left = [
        (6, 4), (7, 3), (8, 3), (9, 4), (10, 5), (10, 6), (9, 7), (8, 7), (7, 7), (6, 6), (5, 5)
    ]
    for x, y in [(6,3),(7,2),(8,2),(9,3),(10,4),(11,5),(11,6),(10,7),(9,8),(8,8),(7,8),(6,7),(5,6),(5,5)]:
        pixels[x, y] = c_bell_out
    for x in range(6, 11):
        for y in range(3, 8):
            if (x-8)**2 + (y-5)**2 <= 8:
                pixels[x, y] = c_bell_mid
    pixels[7, 3] = c_bell_hi
    pixels[8, 4] = c_bell_hi
    pixels[9, 6] = c_bell_dk

    # Right Bell (cx=23, cy=6)
    for x, y in [(25,3),(24,2),(23,2),(22,3),(21,4),(20,5),(20,6),(21,7),(22,8),(23,8),(24,8),(25,7),(26,6),(26,5)]:
        pixels[x, y] = c_bell_out
    for x in range(21, 26):
        for y in range(3, 8):
            if (x-23)**2 + (y-5)**2 <= 8:
                pixels[x, y] = c_bell_mid
    pixels[22, 3] = c_bell_hi
    pixels[23, 4] = c_bell_hi
    pixels[24, 6] = c_bell_dk

    # Center hammer & handle (x=14..17, y=3..6)
    for x in range(14, 18):
        pixels[x, 5] = c_bell_out
        pixels[x, 4] = c_bell_hi
    pixels[15, 3] = c_bell_out
    pixels[16, 3] = c_bell_out

    # Main Body Circle (center=15.5, 18.5, radius ~ 10)
    cx, cy, r = 15.5, 18.5, 10.5
    for x in range(3, 29):
        for y in range(7, 30):
            d2 = (x - cx)**2 + (y - cy)**2
            if d2 <= (r + 0.6)**2:
                if d2 >= (r - 0.7)**2:
                    pixels[x, y] = c_out
                elif d2 >= (r - 2.5)**2:
                    # Red outer ring
                    if y < cy - 2 or x < cx - 2:
                        pixels[x, y] = c_red_hi if (x < cx and y < cy) else c_red_mid
                    else:
                        pixels[x, y] = c_red_dk
                elif d2 >= (r - 3.3)**2:
                    # Inner dial rim
                    pixels[x, y] = c_out
                else:
                    # Clock dial face
                    if y > cy + 2 or x > cx + 3:
                        pixels[x, y] = c_face_sh
                    else:
                        pixels[x, y] = c_face

    # Clock hour marks: 12, 3, 6, 9
    pixels[15, 12] = (140, 140, 150, 255)
    pixels[16, 12] = (140, 140, 150, 255)
    pixels[22, 18] = (140, 140, 150, 255)
    pixels[15, 24] = (140, 140, 150, 255)
    pixels[9, 18] = (140, 140, 150, 255)

    # Clock Hands (Center at 15, 18)
    # Hour hand pointing to 7 (down-left)
    pixels[14, 19] = c_hand
    pixels[13, 20] = c_hand
    pixels[12, 21] = c_hand

    # Minute hand pointing to 1 (up-right)
    pixels[16, 17] = c_hand
    pixels[17, 16] = c_hand
    pixels[18, 15] = c_hand
    pixels[19, 14] = c_hand

    # Center pin
    pixels[15, 18] = c_bell_mid
    pixels[16, 18] = c_bell_hi

    return img

def draw_training_whistle():
    img = create_sprite()
    pixels = img.load()

    # Chrome / Silver athletic coach whistle
    c_out = (35, 40, 48, 255)
    c_hi = (255, 255, 255, 255)
    c_lt = (220, 228, 238, 255)
    c_mid = (165, 175, 190, 255)
    c_dk = (110, 120, 135, 255)
    c_hole = (25, 28, 35, 255)

    # Whistle held diagonally / horizontally from left mouthpiece to right cylinder ball
    # Ring on far right (top-right loop for lanyard: cx=26, cy=10)
    for x, y in [(25,8),(26,8),(27,9),(28,10),(28,11),(27,12),(26,12),(25,11)]:
        pixels[x, y] = c_out
    pixels[26, 9] = c_hi
    pixels[27, 10] = c_lt
    pixels[27, 11] = c_dk

    # Cylinder Body (x=16..25, y=10..22)
    # Circular drum on right side
    cx, cy, r = 21.0, 16.5, 5.5
    for x in range(15, 27):
        for y in range(11, 23):
            d2 = (x - cx)**2 + (y - cy)**2
            if d2 <= (r + 0.5)**2:
                if d2 >= (r - 0.7)**2:
                    pixels[x, y] = c_out
                else:
                    if x < cx and y < cy:
                        pixels[x, y] = c_hi if (x <= cx - 2 and y <= cy - 2) else c_lt
                    elif y >= cy + 2:
                        pixels[x, y] = c_dk
                    else:
                        pixels[x, y] = c_mid

    # Central slit / ball window hole
    for x in range(19, 23):
        pixels[x, 14] = c_hole
    pixels[20, 15] = (200, 170, 70, 255) # pea ball visible inside!
    pixels[21, 15] = (150, 120, 40, 255)

    # Mouthpiece tube extending to bottom-left (from x=5 to 16, y=15 to 22)
    tube = [
        # top line of tube
        (5, 18), (6, 17), (7, 17), (8, 16), (9, 16), (10, 15), (11, 15), (12, 14), (13, 14), (14, 14), (15, 14),
        # bottom line of tube
        (5, 22), (6, 22), (7, 22), (8, 22), (9, 21), (10, 21), (11, 21), (12, 21), (13, 21), (14, 21), (15, 21),
        # tip / lip of mouthpiece
        (4, 19), (4, 20), (4, 21), (5, 19), (5, 20), (5, 21)
    ]
    for x, y in tube:
        pixels[x, y] = c_out

    # Fill inside mouthpiece tube
    for x in range(6, 16):
        for y in range(16, 21):
            if pixels[x, y] == (0, 0, 0, 0):
                if y == 16 or (x < 10 and y == 17):
                    pixels[x, y] = c_hi
                elif y <= 18:
                    pixels[x, y] = c_lt
                elif y == 19:
                    pixels[x, y] = c_mid
                else:
                    pixels[x, y] = c_dk

    # Lip stop ridge
    for y in range(18, 22):
        pixels[7, y] = c_lt
        pixels[8, y] = c_out

    return img

def draw_horseshoe(is_gold=False):
    img = create_sprite()
    pixels = img.load()

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

    # U-Shape Horseshoe
    # Center = (15.5, 15.5)
    # Outer radius ~ 11.5, Inner radius ~ 6.0
    # Top opening between left branch and right branch
    cx, cy = 15.5, 15.5
    r_out = 11.5
    r_in = 6.0

    for x in range(3, 29):
        for y in range(4, 28):
            # U opening at the top: if y < 14 and between x=12 and 19, open!
            if y < 12 and 12 <= x <= 19:
                continue

            d2 = (x - cx)**2 + (y - cy)**2
            if d2 <= (r_out + 0.5)**2 and d2 >= (r_in - 0.5)**2:
                # Shape outline edges
                is_edge = (d2 >= (r_out - 0.8)**2 or d2 <= (r_in + 0.8)**2 or (y <= 5 and (x <= 11 or x >= 20)))
                if is_edge:
                    pixels[x, y] = c_out
                else:
                    # Shading
                    if x < 12: # Left branch
                        if x <= 7:
                            pixels[x, y] = c_hi
                        elif x <= 9:
                            pixels[x, y] = c_lt
                        else:
                            pixels[x, y] = c_mid
                    elif x > 19: # Right branch
                        if x >= 24:
                            pixels[x, y] = c_dk
                        elif x >= 22:
                            pixels[x, y] = c_mid
                        else:
                            pixels[x, y] = c_lt
                    else: # Bottom curve
                        if y >= 24:
                            pixels[x, y] = c_dk
                        elif y >= 21:
                            pixels[x, y] = c_mid
                        else:
                            pixels[x, y] = c_lt

    # Turn the flat top ends into calks (tips of horseshoe)
    for x in range(6, 12):
        pixels[x, 5] = c_out
        pixels[x, 6] = c_hi if x < 9 else c_lt
    for x in range(20, 26):
        pixels[x, 5] = c_out
        pixels[x, 6] = c_lt if x < 23 else c_dk

    # 6 Nail Holes (3 on left, 3 on right)
    # Left holes:
    left_holes = [(7, 10), (6, 15), (7, 20)]
    for hx, hy in left_holes:
        pixels[hx, hy] = c_hole
        pixels[hx+1, hy] = c_hole
        pixels[hx, hy+1] = c_hole
        pixels[hx+1, hy+1] = c_hole
        # highlight notch
        pixels[hx, hy-1] = c_hi

    # Right holes:
    right_holes = [(23, 10), (24, 15), (23, 20)]
    for hx, hy in right_holes:
        pixels[hx, hy] = c_hole
        pixels[hx+1, hy] = c_hole
        pixels[hx, hy+1] = c_hole
        pixels[hx+1, hy+1] = c_hole
        pixels[hx, hy-1] = c_lt

    if is_gold:
        # Extra sparkle accents on gold horseshoe
        pixels[5, 4] = (255, 255, 255, 255)
        pixels[4, 5] = (255, 245, 180, 255)
        pixels[26, 4] = (255, 255, 255, 255)
        pixels[27, 5] = (255, 245, 180, 255)

    return img

def draw_ankle_weights():
    img = create_sprite()
    pixels = img.load()

    # Dark athletic training ankle weight cuff
    c_out = (18, 22, 28, 255)
    c_strap_hi = (70, 190, 235, 255)   # bright cyan athletic strap
    c_strap_mid = (25, 130, 185, 255)
    c_pad_hi = (95, 105, 120, 255)     # dark slate pad highlight
    c_pad_mid = (55, 62, 75, 255)      # dark slate pad
    c_pad_dk = (35, 40, 50, 255)       # dark slate shadow
    c_metal_hi = (250, 252, 255, 255)  # chrome D-ring
    c_metal_mid = (190, 200, 215, 255)
    c_metal_dk = (110, 120, 135, 255)

    # 4 Segmented Weight Pouches (horizontal cuff spanning x=5..26, y=10..24)
    # Background cuff
    for x in range(5, 27):
        for y in range(10, 25):
            if (x in (5, 26) and y in (10, 24)):
                continue
            if x == 5 or x == 26 or y == 10 or y == 24:
                pixels[x, y] = c_out
            else:
                pixels[x, y] = c_pad_mid

    # Segment bars (dividers between weights: x=10, x=15, x=20)
    for div_x in [10, 15, 20]:
        for y in range(11, 24):
            pixels[div_x, y] = c_out

    # Fill each pouch (4 pouches)
    pouches = [(6, 9), (11, 14), (16, 19), (21, 25)]
    for x1, x2 in pouches:
        for x in range(x1, x2 + 1):
            pixels[x, 11] = c_pad_hi
            pixels[x, 12] = c_pad_hi
            for y in range(13, 22):
                pixels[x, y] = c_pad_mid
            pixels[x, 22] = c_pad_dk
            pixels[x, 23] = c_pad_dk

    # Two Horizontal Cyan Athletic Straps across all pouches (y=14 and y=19)
    for x in range(5, 27):
        if pixels[x, 14] != c_out:
            pixels[x, 14] = c_strap_hi
        if pixels[x, 15] != c_out:
            pixels[x, 15] = c_strap_mid
        if pixels[x, 19] != c_out:
            pixels[x, 19] = c_strap_hi
        if pixels[x, 20] != c_out:
            pixels[x, 20] = c_strap_mid

    # Metal D-Ring Buckles on Left side (x=3..5, y=13..16 and y=18..21)
    for y in [13, 14, 15, 16]:
        pixels[3, y] = c_out
        pixels[4, y] = c_metal_hi if y == 13 else c_metal_mid
    pixels[4, 16] = c_metal_dk

    for y in [18, 19, 20, 21]:
        pixels[3, y] = c_out
        pixels[4, y] = c_metal_hi if y == 18 else c_metal_mid
    pixels[4, 21] = c_metal_dk

    # Velcro Tab flaps on Right side (x=27..29)
    for y in [14, 15]:
        pixels[27, y] = c_strap_mid
        pixels[28, y] = c_strap_hi
        pixels[29, y] = c_out
    for y in [19, 20]:
        pixels[27, y] = c_strap_mid
        pixels[28, y] = c_strap_hi
        pixels[29, y] = c_out

    return img

os.makedirs('build/test_sprites', exist_ok=True)
draw_alarm_clock().save('build/test_sprites/alarm_clock.png')
draw_training_whistle().save('build/test_sprites/training_whistle.png')
draw_horseshoe(False).save('build/test_sprites/silver_horseshoe.png')
draw_horseshoe(True).save('build/test_sprites/gold_horseshoe.png')
draw_ankle_weights().save('build/test_sprites/ankle_weights.png')
print('Batch 1 generated successfully!')
