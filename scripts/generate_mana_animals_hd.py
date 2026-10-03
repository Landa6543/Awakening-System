# -*- coding: utf-8 -*-
"""
Generator for High-Contrast, Detailed Mana Animal Textures:
- Mana Cow (Vaca de Maná): Cosmic/Aether Bovine with indigo hide, glowing cyan spots, obsidian horns, shaded hooves & muzzle.
- Mana Pig (Cerdo de Maná): Void/Amethyst Boar with dark velvet violet hide, ivory tusks, glowing runes, shaded hooves.
- Mana Sheep (Oveja de Maná): Astral Slate body + Volumetric Celestial Cloud fleece with 3D tuft shading.
- Mana Chicken (Gallina de Maná): Solar Phoenix Fowl with layered vermilion & golden feathers, ruby wattle, golden beak.
"""
from PIL import Image, ImageDraw
import os

OUT_DIR = "src/main/resources/assets/awakening_system/textures/entity/animal"
VANILLA_DIR = "scratch/vanilla_textures/assets/minecraft/textures/entity"
os.makedirs(OUT_DIR, exist_ok=True)

# -------------------------------------------------------------
# 1. MANA COW (Vaca de Maná) - 64x32
# -------------------------------------------------------------
def make_mana_cow():
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    pixels = img.load()

    # Base palette: Indigo & midnight blue fur
    c_base = (24, 34, 58, 255)
    c_dark = (16, 22, 40, 255)
    c_shadow = (11, 15, 28, 255)
    c_light = (36, 50, 84, 255)
    c_highlight = (50, 70, 114, 255)

    # Rune / Mana spot palette (Luminous Cyan)
    c_spot_dark = (0, 150, 210, 255)
    c_spot_mid = (0, 215, 255, 255)
    c_spot_glow = (140, 240, 255, 255)
    c_spot_core = (230, 255, 255, 255)

    # Horn palette (Obsidian Jade to Moonlit Silver tip)
    c_horn_base = (20, 42, 40, 255)
    c_horn_mid = (40, 85, 80, 255)
    c_horn_tip = (190, 235, 230, 255)

    # Muzzle palette (Soft lavender-slate)
    c_muz_base = (70, 65, 95, 255)
    c_muz_light = (90, 84, 120, 255)
    c_muz_dark = (48, 44, 68, 255)
    c_nostril = (25, 22, 38, 255)

    # Eye palette (Glowing Sapphire/Cyan with glint)
    c_eye_glow = (0, 230, 255, 255)
    c_eye_pupil = (0, 80, 140, 255)
    c_eye_glint = (255, 255, 255, 255)

    # Udder (Bioluminescent Amethyst / Cyan)
    c_udder_base = (85, 50, 100, 255)
    c_udder_glow = (0, 210, 240, 255)

    # Hoof palette
    c_hoof = (14, 18, 30, 255)
    c_hoof_rim = (45, 60, 95, 255)

    # --- HEAD (0..16, 0..12) ---
    for x in range(0, 16):
        for y in range(0, 12):
            # Noise & ambient occlusion shading
            v = (x * 7 + y * 13) % 4
            col = c_dark if y > 7 else (c_base if v < 2 else c_light)
            if y == 0: col = c_highlight
            pixels[x, y] = col

    # Face details (front face is at x=4..8, y=4..12)
    # Forehead mystic mana star rune
    pixels[5, 5] = c_spot_mid
    pixels[6, 5] = c_spot_core
    pixels[6, 4] = c_spot_glow
    pixels[6, 6] = c_spot_glow
    pixels[7, 5] = c_spot_mid

    # Eyes (Front face: (4,8) and (7,8) in vanilla cow)
    pixels[4, 7] = c_eye_pupil
    pixels[4, 8] = c_eye_glow
    pixels[7, 7] = c_eye_pupil
    pixels[7, 8] = c_eye_glow
    # Eye glint
    pixels[4, 7] = c_eye_glint
    pixels[7, 7] = c_eye_glint

    # Side face eye wrap
    pixels[3, 8] = c_spot_dark
    pixels[8, 8] = c_spot_dark

    # Muzzle (x=4..8, y=9..12)
    for x in range(4, 8):
        for y in range(9, 12):
            pixels[x, y] = c_muz_base if (x+y)%2 == 0 else c_muz_light
    # Nostrils
    pixels[4, 11] = c_nostril
    pixels[7, 11] = c_nostril
    pixels[5, 11] = c_muz_dark
    pixels[6, 11] = c_muz_dark

    # Horns (x=22..24, y=0..4 in vanilla cow UV)
    for x in range(22, 24):
        for y in range(0, 4):
            if y == 0:
                pixels[x, y] = c_horn_tip
            elif y == 1:
                pixels[x, y] = c_horn_mid
            else:
                pixels[x, y] = c_horn_base

    # --- BODY (18..40, 4..18) ---
    for x in range(18, 40):
        for y in range(4, 18):
            # Base shading: Top of body is lighter, bottom has shadow
            if y < 8:
                col = c_highlight if (x+y)%3 == 0 else c_base
            elif y > 14:
                col = c_shadow if y == 17 else c_dark
            else:
                col = c_base if (x+y)%2 == 0 else c_light
            pixels[x, y] = col

    # Organic Celestial Mana Spots on Body
    # Spot 1 (Left flank)
    spot1 = [(21, 6), (22, 6), (22, 7), (23, 7), (23, 8), (22, 8), (21, 7)]
    for sx, sy in spot1:
        pixels[sx, sy] = c_spot_mid
    pixels[22, 7] = c_spot_core

    # Spot 2 (Right flank / side)
    spot2 = [(32, 8), (33, 8), (34, 8), (33, 9), (34, 9), (35, 9), (33, 10)]
    for sx, sy in spot2:
        pixels[sx, sy] = c_spot_mid
    pixels[33, 9] = c_spot_glow
    pixels[34, 8] = c_spot_core

    # Spot 3 (Dorsal spine rune)
    spot3 = [(26, 4), (27, 4), (27, 5), (28, 5), (27, 6)]
    for sx, sy in spot3:
        pixels[sx, sy] = c_spot_glow
    pixels[27, 5] = c_spot_core

    # Udder (x=52..56, y=0..4 in cow UV)
    for x in range(52, 56):
        for y in range(0, 4):
            pixels[x, y] = c_udder_base
    pixels[53, 2] = c_udder_glow
    pixels[54, 2] = c_udder_glow

    # --- LEGS (0..16, 16..32) ---
    # 4 legs: (0..4), (4..8), (8..12), (12..16)
    for x in range(0, 16):
        for y in range(16, 32):
            if y >= 29:
                # Hooves
                pixels[x, y] = c_hoof_rim if y == 29 else c_hoof
            elif y < 20:
                # Upper leg muscle
                pixels[x, y] = c_base if (x+y)%2 == 0 else c_light
            else:
                # Lower leg
                pixels[x, y] = c_dark if (x+y)%2 == 0 else c_base

    # Subtle glowing ankle rune on legs
    pixels[2, 27] = c_spot_mid
    pixels[6, 26] = c_spot_mid
    pixels[10, 27] = c_spot_mid
    pixels[14, 26] = c_spot_mid

    img.save(f"{OUT_DIR}/mana_cow.png")
    print("Generated mana_cow.png (64x32)")

# -------------------------------------------------------------
# 2. MANA PIG (Cerdo de Maná) - 64x32
# -------------------------------------------------------------
def make_mana_pig():
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    pixels = img.load()

    # Base palette: Deep velvet shadow-amethyst
    c_base = (55, 30, 75, 255)
    c_dark = (36, 18, 52, 255)
    c_shadow = (22, 10, 32, 255)
    c_light = (76, 44, 102, 255)
    c_highlight = (98, 58, 130, 255)

    # Rune / Mana energy palette (Electric Violet / Magenta)
    c_rune_glow = (225, 75, 255, 255)
    c_rune_mid = (175, 25, 235, 255)
    c_rune_core = (255, 210, 255, 255)

    # Snout palette (Dark magenta velvet)
    c_snout_base = (90, 40, 100, 255)
    c_snout_light = (115, 55, 128, 255)
    c_snout_dark = (60, 25, 70, 255)
    c_nostril = (20, 8, 28, 255)

    # Tusk palette (Polished runic ivory)
    c_tusk = (255, 245, 210, 255)
    c_tusk_shadow = (200, 180, 140, 255)

    # Eye palette (Glowing amber-gold / fiery ruby)
    c_eye_amber = (255, 190, 0, 255)
    c_eye_pupil = (140, 0, 60, 255)
    c_eye_glint = (255, 255, 255, 255)

    # Hoof palette
    c_hoof = (25, 12, 36, 255)
    c_hoof_rim = (70, 35, 95, 255)

    # --- HEAD (0..16, 0..16) ---
    for x in range(0, 16):
        for y in range(0, 16):
            v = (x * 5 + y * 11) % 4
            col = c_dark if y > 10 else (c_base if v < 2 else c_light)
            if y == 0: col = c_highlight
            pixels[x, y] = col

    # Forehead arcane scar / rune
    pixels[6, 4] = c_rune_mid
    pixels[7, 4] = c_rune_core
    pixels[6, 5] = c_rune_glow
    pixels[7, 5] = c_rune_mid

    # Eyes (Vanilla pig head front is x=4..12, y=4..12; eyes at (5,7) and (10,7) roughly)
    pixels[5, 7] = c_eye_amber
    pixels[6, 7] = c_eye_pupil
    pixels[5, 6] = c_eye_glint

    pixels[9, 7] = c_eye_pupil
    pixels[10, 7] = c_eye_amber
    pixels[10, 6] = c_eye_glint

    # Ears (Sides of head x=0..4 and x=12..16)
    for ey in range(2, 6):
        pixels[2, ey] = c_dark
        pixels[13, ey] = c_dark

    # --- SNOUT (16..24, 16..20) in vanilla pig UV ---
    for x in range(16, 24):
        for y in range(16, 20):
            pixels[x, y] = c_snout_base if (x+y)%2 == 0 else c_snout_light
    # Front snout face: nostrils & tusks
    pixels[18, 18] = c_nostril
    pixels[21, 18] = c_nostril
    # Tusks curling up on bottom edges
    pixels[17, 19] = c_tusk_shadow
    pixels[17, 18] = c_tusk
    pixels[22, 19] = c_tusk_shadow
    pixels[22, 18] = c_tusk

    # --- BODY (28..56, 8..24) ---
    for x in range(28, 56):
        for y in range(8, 24):
            if y < 12:
                col = c_highlight if (x+y)%3 == 0 else c_base
            elif y > 19:
                col = c_shadow if y >= 22 else c_dark
            else:
                col = c_base if (x+y)%2 == 0 else c_light
            pixels[x, y] = col

    # Spine and flank runes (Organic, curved energy trails)
    runes = [
        (34, 10), (35, 10), (36, 11), (37, 11), (38, 12),
        (44, 13), (45, 13), (45, 14), (46, 14), (47, 15),
        (33, 16), (34, 16), (34, 17), (35, 17),
        (50, 12), (51, 12), (51, 13)
    ]
    for rx, ry in runes:
        pixels[rx, ry] = c_rune_mid
    pixels[36, 11] = c_rune_core
    pixels[45, 14] = c_rune_core
    pixels[34, 17] = c_rune_glow

    # --- LEGS (0..16, 16..32) ---
    for x in range(0, 16):
        for y in range(16, 32):
            if y >= 29:
                pixels[x, y] = c_hoof_rim if y == 29 else c_hoof
            elif y < 22:
                pixels[x, y] = c_base if (x+y)%2 == 0 else c_light
            else:
                pixels[x, y] = c_dark if (x+y)%2 == 0 else c_base

    # Subtle ankle runes
    pixels[2, 28] = c_rune_mid
    pixels[6, 28] = c_rune_mid
    pixels[10, 28] = c_rune_mid
    pixels[14, 28] = c_rune_mid

    img.save(f"{OUT_DIR}/mana_pig.png")
    print("Generated mana_pig.png (64x32)")

# -------------------------------------------------------------
# 3. MANA SHEEP (Oveja de Maná) - Base & Fur (64x32)
# -------------------------------------------------------------
def make_mana_sheep():
    # 3.1 Base Sheep (Skin / Sheared)
    base_img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    p_base = base_img.load()

    # Twilight Slate skin tones
    s_base = (38, 48, 72, 255)
    s_dark = (26, 32, 50, 255)
    s_light = (54, 68, 98, 255)
    s_shadow = (18, 22, 36, 255)
    s_hoof = (14, 16, 26, 255)

    # Eye & glowing accents
    c_eye_cyan = (0, 225, 255, 255)
    c_eye_pupil = (0, 85, 130, 255)
    c_glint = (255, 255, 255, 255)

    # Head (0..16, 0..12)
    for x in range(0, 16):
        for y in range(0, 12):
            p_base[x, y] = s_base if (x+y)%2 == 0 else s_light
            if y == 0: p_base[x, y] = s_light
            if y > 8: p_base[x, y] = s_dark

    # Eyes on sheep face (front face is x=4..8, y=4..8 roughly in vanilla)
    p_base[4, 6] = c_eye_cyan
    p_base[4, 5] = c_glint
    p_base[7, 6] = c_eye_cyan
    p_base[7, 5] = c_glint

    # Body (28..56, 8..22)
    for x in range(28, 56):
        for y in range(8, 22):
            col = s_base if (x+y)%2 == 0 else (s_light if y < 14 else s_dark)
            p_base[x, y] = col

    # Legs (0..16, 16..32)
    for x in range(0, 16):
        for y in range(16, 32):
            if y >= 29:
                p_base[x, y] = s_hoof
            else:
                p_base[x, y] = s_dark if y > 24 else (s_base if (x+y)%2 == 0 else s_light)

    base_img.save(f"{OUT_DIR}/mana_sheep.png")
    print("Generated mana_sheep.png (64x32)")

    # 3.2 Sheep Fur (Volumetric Celestial Cloud Fleece)
    fur_img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    p_fur = fur_img.load()

    # Cloud Fleece Palette: Puffy volumetric shading (highlights on tops of clumps, deep AO in seams)
    f_high = (220, 245, 255, 255)  # Starlight crest
    f_mid_high = (160, 220, 250, 255)  # Aether blue
    f_mid = (100, 185, 235, 255)  # Deep cyan sky
    f_shadow = (55, 130, 190, 255)  # Underside shadow
    f_crease = (30, 85, 140, 255)  # Ambient occlusion between wool tufts

    def fill_tufted_box(x0, y0, x1, y1):
        for x in range(x0, x1):
            for y in range(y0, y1):
                # Volumetric tuft pattern: 3x3 to 4x4 rounded clumps
                tx = (x - x0) % 4
                ty = (y - y0) % 4
                # Top edge of clump is highlight, bottom right is crease
                if ty == 0:
                    col = f_high if tx in [1, 2] else f_mid_high
                elif ty == 1:
                    col = f_mid_high if tx in [0, 3] else f_high
                elif ty == 2:
                    col = f_mid if tx in [1, 2] else f_shadow
                else:
                    col = f_crease if tx == 3 else f_shadow
                p_fur[x, y] = col

    # Head fleece: (0..16, 0..12)
    fill_tufted_box(0, 0, 16, 12)
    # Face cutout on head front so eyes can see
    for x in range(4, 8):
        for y in range(5, 9):
            p_fur[x, y] = (0, 0, 0, 0)

    # Body fleece: (28..56, 8..22)
    fill_tufted_box(28, 8, 56, 22)

    # Glowing mana star tufts scattered on body fleece
    glow_stars = [(33, 11), (39, 15), (46, 11), (51, 16)]
    for gx, gy in glow_stars:
        p_fur[gx, gy] = (255, 255, 255, 255)
        p_fur[gx+1, gy] = (180, 240, 255, 255)
        p_fur[gx, gy+1] = (180, 240, 255, 255)

    fur_img.save(f"{OUT_DIR}/mana_sheep_fur.png")
    print("Generated mana_sheep_fur.png (64x32)")

# -------------------------------------------------------------
# 4. MANA CHICKEN (Gallina de Maná) - 64x32
# -------------------------------------------------------------
def make_mana_chicken():
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    pixels = img.load()

    # Phoenix / Solar Plumage Palette
    c_plumage_dark = (138, 18, 30, 255)     # Deep crimson base
    c_plumage_mid = (195, 35, 38, 255)      # Vermilion
    c_plumage_bright = (235, 80, 35, 255)   # Flame orange
    c_feather_gold = (255, 175, 20, 255)    # Solar golden tip
    c_feather_crest = (255, 225, 85, 255)   # Gleaming highlight

    # Beak & Talons (Scaled Golden Amber)
    c_beak_light = (255, 200, 30, 255)
    c_beak_mid = (220, 150, 15, 255)
    c_beak_dark = (165, 100, 10, 255)
    c_talon_claw = (80, 45, 10, 255)

    # Wattle & Comb (Vibrant Ruby Red)
    c_comb_bright = (235, 25, 70, 255)
    c_comb_dark = (150, 10, 40, 255)
    c_comb_high = (255, 120, 160, 255)

    # Eyes (Piercing Sapphire/Cyan with glint)
    c_eye = (0, 220, 255, 255)
    c_pupil = (0, 70, 120, 255)
    c_glint = (255, 255, 255, 255)

    # --- HEAD (0..16, 0..9) ---
    for x in range(0, 16):
        for y in range(0, 9):
            v = (x * 7 + y * 13) % 4
            if y == 0:
                col = c_feather_crest
            elif y > 5:
                col = c_plumage_dark
            else:
                col = c_plumage_mid if v < 2 else c_plumage_bright
            pixels[x, y] = col

    # Eyes on head sides (x=3, y=4 and x=7, y=4 roughly in chicken)
    pixels[3, 3] = c_glint
    pixels[3, 4] = c_eye
    pixels[7, 3] = c_glint
    pixels[7, 4] = c_eye

    # --- BEAK (14..18, 0..4) ---
    for x in range(14, 18):
        for y in range(0, 4):
            pixels[x, y] = c_beak_light if y < 2 else c_beak_mid
    pixels[15, 1] = c_beak_dark  # Nostril

    # --- WATTLE & COMB (14..18, 4..8) ---
    for x in range(14, 18):
        for y in range(4, 8):
            pixels[x, y] = c_comb_bright if y < 6 else c_comb_dark
    pixels[15, 4] = c_comb_high

    # --- BODY (0..20, 9..27) ---
    for x in range(0, 20):
        for y in range(9, 27):
            # Layered plumage simulation:
            # Each 2-3 rows represents overlapping feather layers
            layer = (y - 9) // 3
            if layer % 2 == 0:
                col = c_plumage_bright if (x+y)%3 == 0 else c_plumage_mid
            else:
                col = c_plumage_dark if (x+y)%2 == 0 else c_plumage_mid
            if y == 9: col = c_feather_gold  # Golden mantle
            if y >= 25: col = c_plumage_dark  # Underside shadow
            pixels[x, y] = col

    # Golden feather tips on breast & tail
    pixels[6, 15] = c_feather_gold
    pixels[7, 15] = c_feather_crest
    pixels[8, 15] = c_feather_gold

    pixels[6, 21] = c_feather_gold
    pixels[7, 21] = c_feather_crest
    pixels[8, 21] = c_feather_gold

    # --- WINGS (24..40, 13..24) ---
    for x in range(24, 40):
        for y in range(13, 24):
            # Wing feathers have angled downward sweep
            sweep = (x + y) % 3
            if y >= 21:
                # Primary flight feather tips: golden flame!
                pixels[x, y] = c_feather_crest if x%2==0 else c_feather_gold
            elif y > 17:
                pixels[x, y] = c_plumage_bright if sweep == 0 else c_plumage_mid
            else:
                pixels[x, y] = c_plumage_mid if sweep == 0 else c_plumage_dark

    # --- LEGS & CLAWS (26..32, 0..11) ---
    for x in range(26, 32):
        for y in range(0, 11):
            if y >= 9:
                pixels[x, y] = c_talon_claw  # Sharp talons
            else:
                pixels[x, y] = c_beak_mid if (x+y)%2 == 0 else c_beak_light

    img.save(f"{OUT_DIR}/mana_chicken.png")
    print("Generated mana_chicken.png (64x32)")

if __name__ == "__main__":
    make_mana_cow()
    make_mana_pig()
    make_mana_sheep()
    make_mana_chicken()
    print("All Mana Animal textures successfully updated!")
