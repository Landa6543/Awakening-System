import os
from PIL import Image

ITEM_DIR = "src/main/resources/assets/awakening_system/textures/item"
BLOCK_DIR = "src/main/resources/assets/awakening_system/textures/block"
os.makedirs(ITEM_DIR, exist_ok=True)
os.makedirs(BLOCK_DIR, exist_ok=True)

def hex_to_rgba(h, a=255):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)

_ = (0, 0, 0, 0)

def create_16x16(grid, out_path):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if y < len(grid) and x < len(grid[y]):
                img.putpixel((x, y), grid[y][x])
    img.save(out_path)
    print(f"Generated {os.path.basename(out_path)}")

# ==============================================================================
# 1. FOODS (MANA MEATS & FRUITS) - 100% AUTHENTIC MINECRAFT PIXEL ART
# ==============================================================================

# --- RAW MANA BEEF ---
# Classic T-Bone / Ribeye steak with glowing cyan mana marbling
b_ol = hex_to_rgba("#380610") # Dark meat outline
b_m1 = hex_to_rgba("#6B1120") # Dark shadow meat
b_m2 = hex_to_rgba("#9C1A2E") # Deep ruby meat
b_m3 = hex_to_rgba("#C92942") # Mid crimson meat
b_m4 = hex_to_rgba("#EB4660") # Highlight meat
b_ft = hex_to_rgba("#F3E2D8") # Marbled fat edge
b_fs = hex_to_rgba("#D4B5A5") # Fat shadow
b_v1 = hex_to_rgba("#0090B8") # Deep cyan mana vein
b_v2 = hex_to_rgba("#30D8F8") # Bright cyan mana vein
b_v3 = hex_to_rgba("#E0FBFF") # Glowing white-cyan mana core

raw_beef_grid = [
    [ _,    _,    _,   b_ol, b_ol, b_ol, b_ol, b_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   b_ol, b_ft, b_ft, b_ft, b_ft, b_fs, b_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,   b_ol, b_ft, b_m4, b_m3, b_v2, b_v3, b_m3, b_ft, b_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,   b_ol, b_ft, b_m3, b_v2, b_v3, b_v2, b_m2, b_m3, b_ft, b_ol,  _,    _,    _,    _,    _ ],
    [b_ol, b_ft, b_m3, b_v3, b_v2, b_m2, b_m2, b_m2, b_m2, b_fs, b_ol,  _,    _,    _,    _,    _ ],
    [b_ol, b_fs, b_m3, b_v2, b_m2, b_ft, b_fs, b_m1, b_m2, b_m3, b_ol,  _,    _,    _,    _,    _ ],
    [b_ol, b_fs, b_m2, b_m2, b_ft, b_ft, b_fs, b_v2, b_v3, b_m2, b_ol,  _,    _,    _,    _,    _ ],
    [b_ol, b_m2, b_m2, b_m1, b_fs, b_fs, b_v2, b_v3, b_m2, b_m3, b_ol,  _,    _,    _,    _,    _ ],
    [b_ol, b_m2, b_m1, b_m1, b_m1, b_v2, b_v3, b_m2, b_m3, b_m4, b_ol,  _,    _,    _,    _,    _ ],
    [b_ol, b_m1, b_m1, b_v2, b_v3, b_v2, b_m2, b_m3, b_m3, b_m2, b_ol,  _,    _,    _,    _,    _ ],
    [ _,   b_ol, b_m1, b_v3, b_v2, b_m2, b_m2, b_m3, b_m2, b_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,   b_ol, b_fs, b_m2, b_m2, b_m3, b_m2, b_m1, b_m1, b_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,   b_ol, b_ft, b_fs, b_m2, b_m1, b_ol, b_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   b_ol, b_ol, b_fs, b_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,   b_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- COOKED MANA BEEF ---
# Seared steak with grill char marks and simmering cyan mana glaze
cb_ol = hex_to_rgba("#220A05") # Charred outline
cb_c1 = hex_to_rgba("#45180C") # Dark grill char
cb_c2 = hex_to_rgba("#6E2813") # Roasted crust
cb_c3 = hex_to_rgba("#9C3C1B") # Golden brown
cb_c4 = hex_to_rgba("#C95524") # Warm highlight sear
cb_m1 = hex_to_rgba("#881F1F") # Juicy tender pink-red
cb_v1 = hex_to_rgba("#0090B8") # Deep cyan mana juice
cb_v2 = hex_to_rgba("#35DCFA") # Sizzling bright cyan mana juice
cb_v3 = hex_to_rgba("#D5F8FF") # Simmering mana steam glint

cooked_beef_grid = [
    [ _,    _,    _,   cb_ol, cb_ol, cb_ol, cb_ol, cb_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   cb_ol, cb_c3, cb_c4, cb_c4, cb_c3, cb_c2, cb_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,   cb_ol, cb_c3, cb_c1, cb_c2, cb_c3, cb_v2, cb_v3, cb_c3, cb_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,   cb_ol, cb_c4, cb_c1, cb_m1, cb_c2, cb_c1, cb_c2, cb_c4, cb_c3, cb_ol,  _,    _,    _,    _,    _ ],
    [cb_ol, cb_c3, cb_c1, cb_c2, cb_m1, cb_c1, cb_c2, cb_v2, cb_v3, cb_c2, cb_ol,  _,    _,    _,    _,    _ ],
    [cb_ol, cb_c2, cb_c1, cb_c3, cb_c1, cb_c2, cb_c3, cb_c1, cb_c2, cb_c3, cb_ol,  _,    _,    _,    _,    _ ],
    [cb_ol, cb_c2, cb_c2, cb_c1, cb_c2, cb_c3, cb_c1, cb_c2, cb_m1, cb_c2, cb_ol,  _,    _,    _,    _,    _ ],
    [cb_ol, cb_c3, cb_c1, cb_c2, cb_c3, cb_c1, cb_c2, cb_v2, cb_c2, cb_c3, cb_ol,  _,    _,    _,    _,    _ ],
    [cb_ol, cb_c2, cb_c1, cb_c2, cb_c1, cb_c2, cb_v2, cb_v3, cb_c3, cb_c4, cb_ol,  _,    _,    _,    _,    _ ],
    [cb_ol, cb_c2, cb_c1, cb_v2, cb_v3, cb_c2, cb_c1, cb_c2, cb_c3, cb_c2, cb_ol,  _,    _,    _,    _,    _ ],
    [ _,   cb_ol, cb_c2, cb_v2, cb_c2, cb_c1, cb_c2, cb_c3, cb_c2, cb_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,   cb_ol, cb_c2, cb_c2, cb_c2, cb_c3, cb_c2, cb_c1, cb_c1, cb_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,   cb_ol, cb_c3, cb_c2, cb_c2, cb_c1, cb_ol, cb_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   cb_ol, cb_ol, cb_c2, cb_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,   cb_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- RAW MANA CHICKEN ---
# Tender poultry drumstick with mana runes and clean bone
ck_ol = hex_to_rgba("#4A2814")
ck_m1 = hex_to_rgba("#C27A54") # Shade
ck_m2 = hex_to_rgba("#E0966C") # Mid
ck_m3 = hex_to_rgba("#F5B48C") # Highlight
ck_bn = hex_to_rgba("#F8F5EE") # Bone
ck_bs = hex_to_rgba("#C8BAAA") # Bone shadow

raw_chicken_grid = [
    [ _,    _,    _,    _,    _,   ck_ol, ck_ol, ck_ol, ck_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,   ck_ol, ck_m3, ck_m3, ck_m2, ck_m2, ck_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   ck_ol, ck_m3, b_v3,  ck_m3, ck_m2, ck_m2, ck_m1, ck_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,   ck_ol, ck_m3, ck_m3, b_v2,  b_v3,  ck_m2, ck_m2, ck_m1, ck_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,   ck_ol, ck_m3, ck_m2, ck_m2, b_v2,  b_v3,  ck_m1, ck_m1, ck_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,   ck_ol, ck_m2, ck_m2, ck_m2, ck_m2, b_v2,  ck_m1, ck_m1, ck_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,   ck_ol, ck_m2, ck_m2, ck_m1, ck_m1, ck_m1, ck_m1, ck_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   ck_ol, ck_m2, ck_m1, ck_m1, ck_m1, ck_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,   ck_ol, ck_m1, ck_m1, ck_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,   ck_ol, ck_bn, ck_bn, ck_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   ck_ol, ck_bn, ck_bn, ck_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   ck_ol, ck_bn, ck_bn, ck_bs, ck_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   ck_ol, ck_bn, ck_bn, ck_bs, ck_bs, ck_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   ck_ol, ck_bs, ck_ol, ck_bs, ck_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   ck_ol,  _,   ck_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- COOKED MANA CHICKEN ---
# Golden roasted crispy drumstick with herb seasoning
cc_ol = hex_to_rgba("#381908")
cc_m1 = hex_to_rgba("#7A3E16") # Deep roast
cc_m2 = hex_to_rgba("#AC5E22") # Rich roast
cc_m3 = hex_to_rgba("#D98534") # Golden crispy
cc_m4 = hex_to_rgba("#F5B054") # Crackling highlight
cc_hb = hex_to_rgba("#3E8024") # Herb fleck

cooked_chicken_grid = [
    [ _,    _,    _,    _,    _,   cc_ol, cc_ol, cc_ol, cc_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,   cc_ol, cc_m4, cc_m4, cc_m3, cc_m3, cc_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   cc_ol, cc_m4, cc_m4, cc_m3, cc_hb, cc_m3, cc_m2, cc_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,   cc_ol, cc_m4, cc_m3, b_v3,  cc_m3, cc_m2, cc_m2, cc_m1, cc_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,   cc_ol, cc_m3, cc_m3, b_v2,  b_v3,  cc_m2, cc_hb, cc_m1, cc_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,   cc_ol, cc_m3, cc_m2, cc_m2, b_v2,  cc_m1, cc_m1, cc_m1, cc_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,   cc_ol, cc_m2, cc_m2, cc_m1, cc_m1, cc_m1, cc_m1, cc_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   cc_ol, cc_m2, cc_m1, cc_m1, cc_m1, cc_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,   cc_ol, cc_m1, cc_m1, cc_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,   cc_ol, ck_bn, ck_bn, cc_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   cc_ol, ck_bn, ck_bn, cc_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   cc_ol, ck_bn, ck_bn, ck_bs, cc_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   cc_ol, ck_bn, ck_bn, ck_bs, ck_bs, cc_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   cc_ol, ck_bs, cc_ol, ck_bs, cc_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   cc_ol,  _,   cc_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- RAW MANA PORKCHOP ---
pk_ol = hex_to_rgba("#421820")
pk_m1 = hex_to_rgba("#9C4252") # Shade
pk_m2 = hex_to_rgba("#C86072") # Rosy pork
pk_m3 = hex_to_rgba("#E88295") # Tender mid
pk_m4 = hex_to_rgba("#FAA8B8") # Highlight
pk_ft = hex_to_rgba("#FFF0F4") # White pork fat

raw_pork_grid = [
    [ _,    _,   pk_ol, pk_ol, pk_ol, pk_ol, pk_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   pk_ol, pk_ft, pk_ft, pk_ft, pk_ft, pk_ft, pk_ol, pk_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [pk_ol, pk_ft, pk_m4, pk_m3, pk_v2 if 'pk_v2' in locals() else b_v2, b_v3, pk_m3, pk_ft, pk_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [pk_ol, pk_ft, pk_m3, b_v2,  b_v3,  pk_m2, pk_m3, pk_ft, pk_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [pk_ol, pk_m3, pk_m2, b_v3,  b_v2,  pk_m2, pk_m2, pk_ft, pk_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [pk_ol, pk_m3, pk_m2, pk_m2, pk_m1, pk_m1, pk_m2, pk_ft, pk_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [pk_ol, pk_m2, pk_m1, b_v2,  b_v3,  pk_m1, pk_m2, pk_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [pk_ol, pk_m2, pk_m1, b_v3,  b_v2,  pk_m2, pk_m3, pk_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   pk_ol, pk_m1, pk_m1, pk_m2, pk_m3, pk_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   pk_ol, pk_ft, pk_m2, pk_m2, pk_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   pk_ol, pk_ol, pk_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- COOKED MANA PORKCHOP ---
cp_ol = hex_to_rgba("#2C1204")
cp_m1 = hex_to_rgba("#683412")
cp_m2 = hex_to_rgba("#9C5220")
cp_m3 = hex_to_rgba("#C87532")
cp_m4 = hex_to_rgba("#F0A552")
cp_ft = hex_to_rgba("#FDE4C2")

cooked_pork_grid = [
    [ _,    _,   cp_ol, cp_ol, cp_ol, cp_ol, cp_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   cp_ol, cp_ft, cp_ft, cp_ft, cp_ft, cp_ft, cp_ol, cp_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [cp_ol, cp_ft, cp_m4, cp_m3, b_v2,  b_v3,  cp_m3, cp_ft, cp_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [cp_ol, cp_ft, cp_m3, cp_m1, cp_m2, cp_m2, cp_m3, cp_ft, cp_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [cp_ol, cp_m3, cp_m2, cp_m1, b_v2,  b_v3,  cp_m2, cp_ft, cp_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [cp_ol, cp_m3, cp_m2, cp_m2, cp_m1, cp_m1, cp_m2, cp_ft, cp_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [cp_ol, cp_m2, cp_m1, b_v2,  b_v3,  cp_m1, cp_m2, cp_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [cp_ol, cp_m2, cp_m1, b_v3,  b_v2,  cp_m2, cp_m3, cp_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   cp_ol, cp_m1, cp_m1, cp_m2, cp_m3, cp_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   cp_ol, cp_ft, cp_m2, cp_m2, cp_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   cp_ol, cp_ol, cp_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- RAW & COOKED MUTTON ---
mt_ol = hex_to_rgba("#380A18")
mt_m1 = hex_to_rgba("#74182E")
mt_m2 = hex_to_rgba("#A52945")
mt_m3 = hex_to_rgba("#CF3E60")
mt_m4 = hex_to_rgba("#F26688")

raw_mutton_grid = [
    [ _,    _,    _,   mt_ol, mt_ol, mt_ol, mt_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   mt_ol, b_ft,  b_ft,  b_ft,  b_fs, mt_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   mt_ol, b_ft,  mt_m4, mt_m3, b_v2,  b_v3,  mt_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   mt_ol, b_ft,  mt_m3, b_v2,  b_v3,  mt_m2, mt_m3, mt_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [mt_ol, b_ft,  mt_m3, b_v3,  b_v2,  mt_m2, mt_m2, mt_m3, mt_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [mt_ol, b_fs,  mt_m2, b_v2,  mt_m2, mt_m1, mt_m1, mt_m2, mt_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [mt_ol, mt_m2, mt_m2, mt_m1, mt_m1, b_v2,  b_v3,  mt_m2, mt_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [mt_ol, mt_m2, mt_m1, mt_m1, b_v2,  b_v3,  mt_m2, mt_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   mt_ol, mt_m1, mt_m1, mt_m2, mt_m2, mt_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   mt_ol, b_fs,  mt_m2, mt_ol, mt_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   mt_ol, mt_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

cm_ol = hex_to_rgba("#250D06")
cm_m1 = hex_to_rgba("#5B2312")
cm_m2 = hex_to_rgba("#883B1F")
cm_m3 = hex_to_rgba("#BA592E")
cm_m4 = hex_to_rgba("#E58045")

cooked_mutton_grid = [
    [ _,    _,    _,   cm_ol, cm_ol, cm_ol, cm_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   cm_ol, cp_ft, cp_ft, cp_ft, cp_ft, cm_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   cm_ol, cp_ft, cm_m4, cm_m3, b_v2,  b_v3,  cm_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   cm_ol, cp_ft, cm_m3, b_v2,  b_v3,  cm_m2, cm_m3, cm_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [cm_ol, cp_ft, cm_m3, b_v3,  b_v2,  cm_m2, cm_m2, cm_m3, cm_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [cm_ol, cp_ft, cm_m2, b_v2,  cm_m2, cm_m1, cm_m1, cm_m2, cm_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [cm_ol, cm_m2, cm_m2, cm_m1, cm_m1, b_v2,  b_v3,  cm_m2, cm_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [cm_ol, cm_m2, cm_m1, cm_m1, b_v2,  b_v3,  cm_m2, cm_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   cm_ol, cm_m1, cm_m1, cm_m2, cm_m2, cm_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   cm_ol, cp_ft, cm_m2, cm_ol, cm_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   cm_ol, cm_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- SUNSETTIA (FRUTA DEL OCASO - GENSHIN) ---
sn_ol = hex_to_rgba("#5C1204") # Outline
sn_d1 = hex_to_rgba("#A22010") # Deep crimson bottom
sn_d2 = hex_to_rgba("#DC421A") # Rich coral-orange
sn_d3 = hex_to_rgba("#F57022") # Radiant peach-orange
sn_d4 = hex_to_rgba("#FAA838") # Warm golden blush
sn_hi = hex_to_rgba("#FFE885") # Sunny highlight
sn_st = hex_to_rgba("#422810") # Stem
sn_lf = hex_to_rgba("#4E9E28") # Green leaf
sn_lh = hex_to_rgba("#88D645") # Leaf highlight

sunsettia_grid = [
    [ _,    _,    _,    _,    _,   sn_st,  _,   sn_lh, sn_lf,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,   sn_st, sn_lf, sn_lf,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   sn_ol, sn_ol, sn_ol, sn_ol, sn_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   sn_ol, sn_hi, sn_d4, sn_d4, sn_d3, sn_d2, sn_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,   sn_ol, sn_hi, sn_hi, sn_d4, sn_d3, sn_d3, sn_d2, sn_d2, sn_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,   sn_ol, sn_d4, sn_d4, sn_d3, sn_d3, sn_d2, sn_d2, sn_d1, sn_ol,  _,    _,    _,    _,    _,    _ ],
    [sn_ol, sn_d4, sn_d4, sn_d3, sn_d3, sn_d2, sn_d2, sn_d1, sn_d1, sn_d1, sn_ol,  _,    _,    _,    _,    _ ],
    [sn_ol, sn_d4, sn_d3, sn_d3, sn_d2, sn_d2, sn_d1, sn_d1, sn_d1, sn_d1, sn_ol,  _,    _,    _,    _,    _ ],
    [sn_ol, sn_d3, sn_d3, sn_d2, sn_d2, sn_d1, sn_d1, sn_d1, sn_ol, sn_ol, sn_ol,  _,    _,    _,    _,    _ ],
    [sn_ol, sn_d3, sn_d2, sn_d2, sn_d1, sn_d1, sn_d1, sn_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   sn_ol, sn_d2, sn_d1, sn_d1, sn_d1, sn_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   sn_ol, sn_d1, sn_d1, sn_d1, sn_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   sn_ol, sn_ol, sn_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- MANA FRUIT (FRUTA DE MANÁ) ---
mf_ol = hex_to_rgba("#041838")
mf_d1 = hex_to_rgba("#0E3875")
mf_d2 = hex_to_rgba("#1C65B5")
mf_d3 = hex_to_rgba("#2EA2E8")
mf_d4 = hex_to_rgba("#68D6FF")
mf_hi = hex_to_rgba("#E2F8FF")

mana_fruit_grid = [
    [ _,    _,    _,    _,    _,   sn_st,  _,   mf_d4, mf_d3,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,   sn_st, mf_d3, mf_d3,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   mf_ol, mf_ol, mf_ol, mf_ol, mf_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   mf_ol, mf_hi, mf_d4, mf_d4, mf_d3, mf_d2, mf_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,   mf_ol, mf_hi, mf_hi, mf_d4, mf_d3, mf_d3, mf_d2, mf_d2, mf_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,   mf_ol, mf_d4, mf_d4, mf_d3, mf_d3, mf_d2, mf_d2, mf_d1, mf_ol,  _,    _,    _,    _,    _,    _ ],
    [mf_ol, mf_d4, mf_d4, mf_d3, mf_d3, mf_d2, mf_d2, mf_d1, mf_d1, mf_d1, mf_ol,  _,    _,    _,    _,    _ ],
    [mf_ol, mf_d4, mf_d3, mf_d3, mf_d2, mf_d2, mf_d1, mf_d1, mf_d1, mf_d1, mf_ol,  _,    _,    _,    _,    _ ],
    [mf_ol, mf_d3, mf_d3, mf_d2, mf_d2, mf_d1, mf_d1, mf_d1, mf_ol, mf_ol, mf_ol,  _,    _,    _,    _,    _ ],
    [mf_ol, mf_d3, mf_d2, mf_d2, mf_d1, mf_d1, mf_d1, mf_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   mf_ol, mf_d2, mf_d1, mf_d1, mf_d1, mf_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   mf_ol, mf_d1, mf_d1, mf_d1, mf_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   mf_ol, mf_ol, mf_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

create_16x16(raw_beef_grid, f"{ITEM_DIR}/mana_beef.png")
create_16x16(cooked_beef_grid, f"{ITEM_DIR}/cooked_mana_beef.png")
create_16x16(raw_chicken_grid, f"{ITEM_DIR}/mana_chicken_meat.png")
create_16x16(cooked_chicken_grid, f"{ITEM_DIR}/cooked_mana_chicken_meat.png")
create_16x16(raw_pork_grid, f"{ITEM_DIR}/mana_porkchop.png")
create_16x16(cooked_pork_grid, f"{ITEM_DIR}/cooked_mana_porkchop.png")
create_16x16(raw_mutton_grid, f"{ITEM_DIR}/mana_mutton.png")
create_16x16(cooked_mutton_grid, f"{ITEM_DIR}/cooked_mana_mutton.png")
create_16x16(sunsettia_grid, f"{ITEM_DIR}/sunsettia.png")
create_16x16(mana_fruit_grid, f"{ITEM_DIR}/mana_fruit.png")

# ==============================================================================
# 2. GENSHIN DROPS - 100% AUTHENTIC MINECRAFT PIXEL ART
# ==============================================================================

# --- SLIME CONDENSATE (Condensado de Slime) ---
sc_ol = hex_to_rgba("#083548") # Dark teal outline
sc_b1 = hex_to_rgba("#14627E") # Dark shadow cyan
sc_b2 = hex_to_rgba("#209BB5") # Mid cyan
sc_b3 = hex_to_rgba("#42D2EB") # Bright turquoise
sc_b4 = hex_to_rgba("#88EDF8") # Specular cyan
sc_hi = hex_to_rgba("#FFFFFF") # Pure white gleam

slime_condensate_grid = [
    [ _,    _,    _,    _,    _,    _,   sc_ol, sc_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,   sc_ol, sc_b3, sc_b3, sc_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,   sc_ol, sc_b4, sc_b3, sc_b3, sc_b2, sc_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   sc_ol, sc_hi, sc_b4, sc_b3, sc_b2, sc_b2, sc_b1, sc_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,   sc_ol, sc_hi, sc_hi, sc_b4, sc_b3, sc_b3, sc_b2, sc_b1, sc_ol,  _,    _,    _,    _,    _ ],
    [ _,   sc_ol, sc_b4, sc_hi, sc_b4, sc_b3, sc_b3, sc_b2, sc_b2, sc_b1, sc_b1, sc_ol,  _,    _,    _,    _ ],
    [ _,   sc_ol, sc_b4, sc_b3, sc_b3, sc_b3, sc_b4, sc_b3, sc_b2, sc_b1, sc_b1, sc_ol,  _,    _,    _,    _ ],
    [sc_ol, sc_b3, sc_b3, sc_b3, sc_b3, sc_b4, sc_hi, sc_b3, sc_b2, sc_b1, sc_b1, sc_b1, sc_ol,  _,    _,    _ ],
    [sc_ol, sc_b3, sc_b3, sc_b3, sc_b3, sc_b3, sc_b3, sc_b2, sc_b2, sc_b1, sc_b1, sc_b1, sc_ol,  _,    _,    _ ],
    [sc_ol, sc_b2, sc_b3, sc_b3, sc_b2, sc_b2, sc_b2, sc_b2, sc_b1, sc_b1, sc_b1, sc_b1, sc_ol,  _,    _,    _ ],
    [sc_ol, sc_b2, sc_b2, sc_b2, sc_b2, sc_b1, sc_b1, sc_b1, sc_b1, sc_b1, sc_b1, sc_b1, sc_ol,  _,    _,    _ ],
    [ _,   sc_ol, sc_b2, sc_b2, sc_b1, sc_b1, sc_b1, sc_b1, sc_b1, sc_b1, sc_b1, sc_ol,  _,    _,    _,    _ ],
    [ _,   sc_ol, sc_b1, sc_b1, sc_b1, sc_b1, sc_b1, sc_b1, sc_b1, sc_b1, sc_b1, sc_ol,  _,    _,    _,    _ ],
    [ _,    _,   sc_ol, sc_ol, sc_b1, sc_b1, sc_b1, sc_b1, sc_b1, sc_ol, sc_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,   sc_ol, sc_ol, sc_ol, sc_ol, sc_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- SLIME SECRETIONS (Secreciones de Slime) ---
ss_ol = hex_to_rgba("#042535")
ss_b1 = hex_to_rgba("#0C4E68")
ss_b2 = hex_to_rgba("#157E9E")
ss_b3 = hex_to_rgba("#2AB8D8")
ss_b4 = hex_to_rgba("#6CE4F8")
ss_hi = hex_to_rgba("#FFFFFF")

slime_secretions_grid = [
    [ _,    _,    _,    _,    _,    _,   ss_ol, ss_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,   ss_ol, ss_b4, ss_b3, ss_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,   ss_ol, ss_b4, ss_b3, ss_b2, ss_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   ss_ol, ss_hi, ss_b3, ss_b2, ss_b1, ss_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   ss_ol, ss_hi, ss_b4, ss_b3, ss_b2, ss_b1, ss_b1, ss_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,   ss_ol, ss_b4, sc_hi, ss_b3, ss_b3, ss_b2, ss_b2, ss_b1, ss_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,   ss_ol, ss_b3, ss_b3, ss_b3, ss_b4, ss_b3, ss_b2, ss_b1, ss_ol,  _,    _,    _,    _,    _,    _ ],
    [ss_ol, ss_b3, ss_b3, ss_b3, ss_b4, ss_hi, ss_b3, ss_b2, ss_b1, ss_b1, ss_ol,  _,    _,    _,    _,    _ ],
    [ss_ol, ss_b3, ss_b3, ss_b3, ss_b3, ss_b3, ss_b2, ss_b2, ss_b1, ss_b1, ss_ol,  _,    _,    _,    _,    _ ],
    [ss_ol, ss_b2, ss_b3, ss_b3, ss_b2, ss_b2, ss_b1, ss_b1, ss_b1, ss_b1, ss_ol,  _,    _,    _,    _,    _ ],
    [ _,   ss_ol, ss_b2, ss_b2, ss_b1, ss_b1, ss_b1, ss_b1, ss_b1, ss_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,   ss_ol, ss_b1, ss_b1, ss_b1, ss_b1, ss_b1, ss_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   ss_ol, ss_b1, ss_b1, ss_b1, ss_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,   ss_ol, ss_b2, ss_b1, ss_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,   ss_ol, ss_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- SLIME CONCENTRATE (Concentrado de Slime) ---
sz_ol = hex_to_rgba("#021528")
sz_b1 = hex_to_rgba("#063255")
sz_b2 = hex_to_rgba("#0F5E94")
sz_b3 = hex_to_rgba("#1C91CF")
sz_b4 = hex_to_rgba("#42C8FA")
sz_sp = hex_to_rgba("#BCEEFF") # Radiant aura
sz_hi = hex_to_rgba("#FFFFFF")

slime_concentrate_grid = [
    [ _,    _,    _,    _,    _,    _,   sz_sp,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,   sz_ol, sz_ol, sz_ol, sz_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   sz_sp, sz_ol, sz_b4, sz_b4, sz_b3, sz_ol, sz_sp,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   sz_ol, sz_b4, sz_hi, sz_b3, sz_b2, sz_b2, sz_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,   sz_ol, sz_b4, sz_hi, sz_b4, sz_b3, sz_b2, sz_b1, sz_b1, sz_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,   sz_ol, sz_b3, sz_b4, sz_sp, sz_hi, sz_b3, sz_b2, sz_b1, sz_ol,  _,    _,    _,    _,    _,    _ ],
    [sz_sp, sz_ol, sz_b3, sz_b3, sz_hi, sz_hi, sz_b3, sz_b2, sz_b1, sz_ol, sz_sp,  _,    _,    _,    _,    _ ],
    [ _,   sz_ol, sz_b3, sz_b3, sz_sp, sz_hi, sz_b3, sz_b1, sz_b1, sz_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,   sz_ol, sz_b2, sz_b3, sz_b3, sz_b3, sz_b2, sz_b1, sz_b1, sz_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,   sz_ol, sz_b2, sz_b2, sz_b1, sz_b1, sz_b1, sz_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   sz_sp, sz_ol, sz_b1, sz_b1, sz_b1, sz_ol, sz_sp,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,   sz_ol, sz_ol, sz_ol, sz_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,   sz_sp,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- CONDENSED RESIN (Resina Concentrada) ---
# Iconic butterfly-shaped golden-honey crystal resin
cr_ol = hex_to_rgba("#452402") # Amber edge
cr_c1 = hex_to_rgba("#7E4405") # Honey shadow
cr_c2 = hex_to_rgba("#B86C0A") # Golden amber
cr_c3 = hex_to_rgba("#E89B18") # Radiant gold
cr_c4 = hex_to_rgba("#F8C838") # Sunburst yellow
cr_hi = hex_to_rgba("#FFF6B0") # Bright core crystal

condensed_resin_grid = [
    [ _,   cr_ol, cr_ol,  _,    _,    _,    _,    _,    _,    _,   cr_ol, cr_ol,  _,    _,    _,    _ ],
    [cr_ol, cr_c4, cr_c3, cr_ol,  _,    _,   cr_ol, cr_ol,  _,   cr_ol, cr_c3, cr_c4, cr_ol,  _,    _,    _ ],
    [cr_ol, cr_hi, cr_c4, cr_c3, cr_ol, cr_ol, cr_c4, cr_c4, cr_ol, cr_ol, cr_c3, cr_c4, cr_ol,  _,    _,    _ ],
    [cr_ol, cr_c4, cr_c3, cr_c2, cr_c3, cr_c4, cr_hi, cr_hi, cr_c4, cr_c3, cr_c2, cr_c3, cr_ol,  _,    _,    _ ],
    [ _,   cr_ol, cr_c3, cr_c2, cr_c4, cr_hi, cr_hi, cr_hi, cr_hi, cr_c4, cr_c2, cr_c3, cr_ol,  _,    _,    _ ],
    [ _,   cr_ol, cr_c3, cr_c2, cr_c3, cr_c4, cr_hi, cr_hi, cr_c4, cr_c3, cr_c2, cr_c3, cr_ol,  _,    _,    _ ],
    [ _,    _,   cr_ol, cr_c2, cr_c1, cr_c3, cr_c4, cr_c4, cr_c3, cr_c1, cr_c2, cr_ol,  _,    _,    _,    _ ],
    [ _,    _,   cr_ol, cr_c3, cr_c2, cr_ol, cr_c3, cr_c3, cr_ol, cr_c2, cr_c3, cr_ol,  _,    _,    _,    _ ],
    [ _,   cr_ol, cr_c4, cr_c3, cr_c2, cr_ol, cr_c2, cr_c2, cr_ol, cr_c2, cr_c3, cr_c4, cr_ol,  _,    _,    _ ],
    [cr_ol, cr_c3, cr_c2, cr_c1, cr_ol,  _,   cr_c1, cr_c1,  _,   cr_ol, cr_c1, cr_c2, cr_c3, cr_ol,  _,    _ ],
    [cr_ol, cr_c2, cr_c1, cr_ol,  _,    _,   cr_ol, cr_ol,  _,    _,   cr_ol, cr_c1, cr_c2, cr_ol,  _,    _ ],
    [ _,   cr_ol, cr_ol,  _,    _,    _,    _,    _,    _,    _,    _,   cr_ol, cr_ol,  _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- DAMAGED MASK (Máscara Dañada - Hilichurl) ---
mk_ol = hex_to_rgba("#221D16") # Dark outline
mk_b1 = hex_to_rgba("#7A7062") # Dark bone
mk_b2 = hex_to_rgba("#ABA08E") # Mid bone
mk_b3 = hex_to_rgba("#D5CABE") # Light bone
mk_b4 = hex_to_rgba("#EFE8DE") # White bone
mk_ey = hex_to_rgba("#181410") # Black eye sockets

damaged_mask_grid = [
    [ _,    _,   mk_ol, mk_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b2, mk_b3, mk_ol,  _,    _,    _,    _,    _,   mk_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,   mk_ol, mk_b3, mk_b2, mk_ol, mk_ol, mk_ol, mk_ol, mk_ol, mk_b2, mk_ol,  _,    _,    _,    _ ],
    [ _,    _,   mk_ol, mk_b4, mk_b3, mk_b3, mk_b3, mk_b3, mk_b3, mk_b2, mk_ol,  _,    _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b4, mk_b4, mk_b4, mk_b3, mk_b3, mk_b3, mk_b3, mk_b2, mk_b2, mk_ol,  _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b4, mk_ey, mk_ey, mk_b4, mk_b3, mk_b3, mk_ey, mk_ey, mk_b2, mk_ol,  _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b4, mk_ey, mk_ey, mk_b3, mk_b3, mk_b2, mk_ey, mk_ey, mk_b2, mk_ol,  _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b4, mk_b4, mk_b4, mk_b3, mk_ol, mk_b2, mk_b2, mk_b2, mk_b1, mk_ol,  _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b3, mk_b3, mk_b3, mk_ol, mk_b2, mk_b2, mk_b2, mk_b1, mk_b1, mk_ol,  _,    _,    _,    _ ],
    [ _,    _,   mk_ol, mk_b3, mk_b2, mk_b2, mk_ol, mk_b1, mk_b1, mk_b1, mk_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,   mk_ol, mk_b2, mk_b2, mk_b1, mk_b1, mk_ol, mk_b1, mk_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   mk_ol, mk_ol, mk_ol, mk_ol,  _,   mk_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- STAINED MASK (Máscara Sucia - Hilichurl con pintura de guerra) ---
sm_pt = hex_to_rgba("#A21818") # Crimson warpaint
sm_ph = hex_to_rgba("#D82E2E") # Bright warpaint

stained_mask_grid = [
    [ _,    _,   mk_ol, mk_ol,  _,    _,    _,    _,    _,    _,   mk_ol, mk_ol,  _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b2, mk_b3, mk_ol,  _,    _,    _,    _,   mk_ol, mk_b3, mk_b2, mk_ol,  _,    _,    _ ],
    [ _,    _,   mk_ol, mk_b3, mk_b2, mk_ol, mk_ol, mk_ol, mk_ol, mk_b2, mk_b3, mk_ol,  _,    _,    _,    _ ],
    [ _,    _,   mk_ol, mk_b4, mk_b3, sm_ph, sm_pt, sm_ph, mk_b3, mk_b3, mk_b2, mk_ol,  _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b4, sm_ph, mk_b4, sm_pt, sm_pt, sm_pt, mk_b4, sm_ph, mk_b2, mk_ol,  _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b4, mk_ey, mk_ey, sm_pt, mk_b3, sm_pt, mk_ey, mk_ey, mk_b2, mk_ol,  _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b4, mk_ey, mk_ey, sm_pt, mk_b3, sm_pt, mk_ey, mk_ey, mk_b2, mk_ol,  _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b4, sm_ph, mk_b4, mk_b3, mk_b3, mk_b2, mk_b4, sm_ph, mk_b1, mk_ol,  _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b3, sm_pt, mk_b3, mk_b2, mk_b2, mk_b2, mk_b3, sm_pt, mk_b1, mk_ol,  _,    _,    _,    _ ],
    [ _,    _,   mk_ol, mk_b3, sm_pt, mk_b2, mk_b2, mk_b1, sm_pt, mk_b2, mk_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,   mk_ol, mk_b2, mk_b2, mk_b1, mk_b1, mk_b1, mk_b1, mk_b1, mk_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,    _,   mk_ol, mk_ol, mk_ol, mk_ol, mk_ol, mk_ol, mk_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- OMINOUS MASK (Máscara Amenazante - Chieftain con cuernos y ojos brillantes) ---
om_h1 = hex_to_rgba("#14101E") # Horn shadow
om_h2 = hex_to_rgba("#2C223C") # Horn dark
om_h3 = hex_to_rgba("#48365E") # Horn mid
om_gd = hex_to_rgba("#E8A820") # Gold band
om_re = hex_to_rgba("#FF2555") # Glowing red eye slits

ominous_mask_grid = [
    [mk_ol, mk_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _,   mk_ol, mk_ol,  _,    _ ],
    [om_h2, om_h3, mk_ol,  _,    _,    _,    _,    _,    _,    _,   mk_ol, om_h3, om_h2, mk_ol,  _,    _ ],
    [ _,   om_h2, om_gd, mk_ol, mk_ol, mk_ol, mk_ol, mk_ol, mk_ol, mk_ol, om_gd, om_h2,  _,    _,    _,    _ ],
    [ _,   om_h1, om_h2, mk_b4, mk_b3, om_re, om_re, mk_b3, mk_b3, om_h2, om_h1,  _,    _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b4, mk_b4, om_re, om_re, om_re, om_re, mk_b4, mk_b3, mk_ol,  _,    _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b4, mk_ey, mk_ey, om_re, om_re, mk_ey, mk_ey, mk_b2, mk_ol,  _,    _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b4, mk_ey, mk_ey, mk_b3, mk_b2, mk_ey, mk_ey, mk_b2, mk_ol,  _,    _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b3, om_re, mk_b4, mk_b3, mk_b2, mk_b3, om_re, mk_b1, mk_ol,  _,    _,    _,    _,    _ ],
    [ _,   mk_ol, mk_b3, om_re, mk_b3, mk_b2, mk_b2, mk_b2, om_re, mk_b1, mk_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,   mk_ol, mk_b2, om_re, mk_b1, mk_b1, om_re, mk_b2, mk_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,   mk_ol, mk_b2, mk_b1, mk_b1, mk_b1, mk_b1, mk_b1, mk_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   mk_ol, mk_ol, mk_ol, mk_ol, mk_ol, mk_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

# --- HEAVY HORN (Cuerno Pesado - Mitachurl) ---
hn_ol = hex_to_rgba("#1E1610")
hn_h1 = hex_to_rgba("#3C2E22")
hn_h2 = hex_to_rgba("#5E4834")
hn_h3 = hex_to_rgba("#85684C")
hn_h4 = hex_to_rgba("#AD8C68")
hn_gd = hex_to_rgba("#E8A820") # Golden ring bands
hn_gh = hex_to_rgba("#FFE050")

heavy_horn_grid = [
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,   hn_ol, hn_ol,  _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,   hn_ol, hn_h4, hn_h3, hn_ol,  _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,   hn_ol, hn_h4, hn_h4, hn_h3, hn_ol,  _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,   hn_ol, hn_h4, hn_h3, hn_h3, hn_h2, hn_ol,  _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,   hn_ol, hn_gd, hn_gh, hn_gd, hn_h2, hn_ol,  _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,   hn_ol, hn_h4, hn_gd, hn_gd, hn_h3, hn_h2, hn_ol,  _,    _,    _,    _ ],
    [ _,    _,    _,    _,   hn_ol, hn_h4, hn_h3, hn_h3, hn_h2, hn_h1, hn_ol,  _,    _,    _,    _,    _ ],
    [ _,    _,    _,   hn_ol, hn_h4, hn_h3, hn_h3, hn_h2, hn_h1, hn_ol,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,   hn_ol, hn_gd, hn_gh, hn_gd, hn_h2, hn_h1, hn_ol,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,   hn_ol, hn_h4, hn_gd, hn_gd, hn_h2, hn_h1, hn_ol,  _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   hn_ol, hn_h3, hn_h3, hn_h2, hn_h1, hn_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [hn_ol, hn_h3, hn_h2, hn_h1, hn_h1, hn_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [hn_ol, hn_h2, hn_h1, hn_h1, hn_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [hn_ol, hn_h1, hn_ol, hn_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,   hn_ol,  _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]

create_16x16(slime_condensate_grid, f"{ITEM_DIR}/slime_condensate.png")
create_16x16(slime_secretions_grid, f"{ITEM_DIR}/slime_secretions.png")
create_16x16(slime_concentrate_grid, f"{ITEM_DIR}/slime_concentrate.png")
create_16x16(condensed_resin_grid, f"{ITEM_DIR}/condensed_resin.png")
create_16x16(damaged_mask_grid, f"{ITEM_DIR}/damaged_mask.png")
create_16x16(stained_mask_grid, f"{ITEM_DIR}/stained_mask.png")
create_16x16(ominous_mask_grid, f"{ITEM_DIR}/ominous_mask.png")
create_16x16(heavy_horn_grid, f"{ITEM_DIR}/heavy_horn.png")

print("All foods & Genshin drops generated in 100% authentic Minecraft pixel art!")
