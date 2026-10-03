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
# 1. SWEET MADAME (Authentic 16x16 Pixel Art)
# ==============================================================================
# Golden-brown roasted plump chicken on a white porcelain platter with sweet flowers
sm_pl = hex_to_rgba("#D5DFE5") # Platter rim
sm_ps = hex_to_rgba("#9AA6B2") # Platter shadow
sm_ck1 = hex_to_rgba("#5C2C10") # Chicken dark char/shadow
sm_ck2 = hex_to_rgba("#8E4718") # Deep roast
sm_ck3 = hex_to_rgba("#C26D27") # Crispy golden brown
sm_ck4 = hex_to_rgba("#E8963E") # Golden highlight
sm_ck5 = hex_to_rgba("#F7BE6D") # Crispy skin shine
sm_fl1 = hex_to_rgba("#E83865") # Sweet flower petal
sm_fl2 = hex_to_rgba("#FFAEC4") # Flower petal light
sm_fl3 = hex_to_rgba("#FFE44D") # Flower pistil gold
sm_gn1 = hex_to_rgba("#387E22") # Garnish green

sweet_madame_grid = [
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,   sm_ps, sm_pl, sm_pl, sm_pl, sm_pl,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,   sm_ps, sm_pl, sm_ck1, sm_ck2, sm_ck3, sm_ck3, sm_ck2, sm_pl,  _,    _,    _,    _,    _,    _ ],
    [ _,   sm_ps, sm_pl, sm_ck2, sm_ck3, sm_ck4, sm_ck5, sm_ck4, sm_ck3, sm_ck2, sm_pl,  _,    _,    _,    _,    _ ],
    [ _,   sm_pl, sm_ck1, sm_ck3, sm_ck4, sm_ck5, sm_ck5, sm_ck4, sm_ck4, sm_ck3, sm_ck1, sm_pl,  _,    _,    _,    _ ],
    [sm_ps, sm_pl, sm_ck2, sm_ck4, sm_ck5, sm_ck4, sm_ck3, sm_ck4, sm_ck5, sm_ck4, sm_ck2, sm_pl,  _,    _,    _,    _ ],
    [sm_pl, sm_ck1, sm_ck3, sm_ck4, sm_ck3, sm_ck2, sm_ck1, sm_ck3, sm_ck4, sm_ck3, sm_ck2, sm_ck1, sm_pl,  _,    _,    _ ],
    [sm_pl, sm_ck2, sm_ck4, sm_ck5, sm_ck4, sm_ck3, sm_ck2, sm_ck3, sm_ck5, sm_ck4, sm_ck3, sm_ck2, sm_pl,  _,    _,    _ ],
    [sm_pl, sm_ck2, sm_ck3, sm_ck4, sm_ck4, sm_ck3, sm_ck2, sm_ck2, sm_ck4, sm_ck4, sm_ck3, sm_ck2, sm_pl,  _,    _,    _ ],
    [sm_pl, sm_ck1, sm_ck2, sm_ck3, sm_ck2, sm_fl1, sm_fl2, sm_fl1, sm_ck2, sm_ck3, sm_ck2, sm_ck1, sm_pl,  _,    _,    _ ],
    [ _,   sm_pl, sm_ck1, sm_gn1, sm_fl2, sm_fl3, sm_fl2, sm_fl1, sm_gn1, sm_ck1, sm_pl, sm_pl,  _,    _,    _,    _ ],
    [ _,   sm_ps, sm_pl, sm_gn1, sm_fl1, sm_fl2, sm_fl1, sm_gn1, sm_pl, sm_pl, sm_ps,  _,    _,    _,    _,    _ ],
    [ _,    _,   sm_ps, sm_pl, sm_pl, sm_pl, sm_pl, sm_pl, sm_pl, sm_ps,  _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,   sm_ps, sm_ps, sm_ps, sm_ps, sm_ps, sm_ps,  _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ],
    [ _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _,    _ ]
]
create_16x16(sweet_madame_grid, f"{ITEM_DIR}/sweet_madame.png")

# ==============================================================================
# 2. TREE LEAF TEXTURES (100% Bespoke Minecraft Foliage with depth & negative space)
# ==============================================================================

# --- CUIHUA LEAVES (Mondstadt Apple Orchard Lush Emerald Green) ---
c_d = hex_to_rgba("#143E19") # Deep leaf cavity shadow
c_1 = hex_to_rgba("#1F5F26") # Dark foliage
c_2 = hex_to_rgba("#35883A") # Mid leaf green
c_3 = hex_to_rgba("#52B246") # Vibrant apple green
c_4 = hex_to_rgba("#7EE055") # Sunlit canopy highlight

cuihua_leaves_grid = [
    [c_1, c_2, c_3,  _,   c_2, c_3, c_4, c_3,  _,   c_1, c_2, c_3, c_4, c_3, c_2, c_1],
    [c_2, c_3, c_4, c_3, c_1, c_2, c_3, c_2, c_1, c_2, c_3, c_4, c_4, c_3, c_1,  _ ],
    [c_1, c_2, c_3, c_2, c_d, c_1, c_2, c_1, c_d, c_1, c_2, c_3, c_3, c_2, c_d, c_1],
    [ _,   c_1, c_2, c_1,  _,   c_d, c_1,  _,   c_1, c_2, c_3, c_2, c_1, c_d, c_1, c_2],
    [c_1, c_2, c_3, c_2, c_1, c_2, c_3, c_2, c_2, c_3, c_4, c_3, c_2, c_1, c_2, c_3],
    [c_2, c_3, c_4, c_3, c_2, c_3, c_4, c_3, c_1, c_2, c_3, c_2, c_1,  _,   c_1, c_2],
    [c_1, c_2, c_3, c_2, c_1, c_2, c_3, c_2, c_d, c_1, c_2, c_1, c_d, c_1, c_2, c_3],
    [c_d, c_1, c_2, c_1, c_d, c_1, c_2, c_1,  _,   c_d, c_1,  _,   c_1, c_2, c_3, c_4],
    [c_1, c_2, c_3, c_4, c_3, c_2, c_1,  _,   c_1, c_2, c_3, c_2, c_2, c_3, c_4, c_3],
    [c_2, c_3, c_4, c_4, c_3, c_1,  _,   c_1, c_2, c_3, c_4, c_3, c_1, c_2, c_3, c_2],
    [c_1, c_2, c_3, c_3, c_2, c_d, c_1, c_2, c_3, c_4, c_3, c_2, c_d, c_1, c_2, c_1],
    [ _,   c_1, c_2, c_1, c_d, c_1, c_2, c_3, c_3, c_2, c_1, c_d,  _,   c_d, c_1,  _ ],
    [c_1, c_2, c_3, c_2, c_1, c_2, c_3, c_4, c_3, c_2, c_1, c_2, c_3, c_2, c_1, c_2],
    [c_2, c_3, c_4, c_3, c_2, c_3, c_4, c_4, c_3, c_1, c_2, c_3, c_4, c_3, c_2, c_3],
    [c_1, c_2, c_3, c_2, c_1, c_2, c_3, c_3, c_2, c_d, c_1, c_2, c_3, c_2, c_1, c_2],
    [c_d, c_1, c_2, c_1, c_d, c_1, c_2, c_1, c_d,  _,   c_d, c_1, c_2, c_1, c_d, c_1]
]
create_16x16(cuihua_leaves_grid, f"{BLOCK_DIR}/cuihua_leaves.png")

# --- CUIHUA LEAVES FRUITED (With clusters of ripe hanging Sunsettias) ---
# Sunsettia fruit pixels:
f_st = hex_to_rgba("#462610") # Stem
f_s1 = hex_to_rgba("#88140D") # Fruit shadow
f_s2 = hex_to_rgba("#C62817") # Deep red-coral base
f_s3 = hex_to_rgba("#E85D1A") # Warm Sunsettia orange
f_s4 = hex_to_rgba("#FF9A26") # Sunlit orange
f_s5 = hex_to_rgba("#FFE842") # Golden shine tip

import copy
cuihua_fruited_grid = [row[:] for row in cuihua_leaves_grid]

# Place Fruit Cluster 1 (Top Left area at 2, 2)
cuihua_fruited_grid[2][2] = f_st
cuihua_fruited_grid[3][2] = f_s3
cuihua_fruited_grid[3][3] = f_s4
cuihua_fruited_grid[4][2] = f_s2
cuihua_fruited_grid[4][3] = f_s5
cuihua_fruited_grid[5][2] = f_s1
cuihua_fruited_grid[5][3] = f_s2

# Place Fruit Cluster 2 (Top Right area at 2, 11)
cuihua_fruited_grid[1][11] = f_st
cuihua_fruited_grid[2][11] = f_s4
cuihua_fruited_grid[2][12] = f_s5
cuihua_fruited_grid[3][11] = f_s3
cuihua_fruited_grid[3][12] = f_s2
cuihua_fruited_grid[4][11] = f_s1

# Place Fruit Cluster 3 (Bottom Center area at 9, 7)
cuihua_fruited_grid[8][7]  = f_st
cuihua_fruited_grid[9][6]  = f_s3
cuihua_fruited_grid[9][7]  = f_s4
cuihua_fruited_grid[10][6] = f_s2
cuihua_fruited_grid[10][7] = f_s5
cuihua_fruited_grid[11][6] = f_s1
cuihua_fruited_grid[11][7] = f_s2

# Place Fruit Cluster 4 (Bottom Right at 12, 12)
cuihua_fruited_grid[12][12] = f_st
cuihua_fruited_grid[13][12] = f_s3
cuihua_fruited_grid[13][13] = f_s4
cuihua_fruited_grid[14][12] = f_s2
cuihua_fruited_grid[14][13] = f_s5

create_16x16(cuihua_fruited_grid, f"{BLOCK_DIR}/cuihua_leaves_fruited.png")

# --- SANDBEARER LEAVES (Liyue Karst Autumn Golden Ginkgo Leaves) ---
s_d = hex_to_rgba("#5C2B06") # Deep bronze shadow
s_1 = hex_to_rgba("#8E470C") # Dark golden amber
s_2 = hex_to_rgba("#C47414") # Warm ochre
s_3 = hex_to_rgba("#E8A51D") # Harvest gold
s_4 = hex_to_rgba("#FFD447") # Radiant topaz highlight
s_5 = hex_to_rgba("#FFF192") # Pale golden leaf tip

sandbearer_leaves_grid = [
    [s_2, s_3, s_4,  _,   s_2, s_3, s_5, s_4,  _,   s_1, s_2, s_3, s_4, s_3, s_2, s_1],
    [s_3, s_4, s_5, s_3, s_1, s_2, s_4, s_3, s_1, s_2, s_3, s_4, s_5, s_4, s_2,  _ ],
    [s_1, s_2, s_3, s_2, s_d, s_1, s_2, s_1, s_d, s_1, s_2, s_3, s_4, s_3, s_1, s_2],
    [ _,   s_1, s_2, s_1,  _,   s_d, s_1,  _,   s_1, s_2, s_3, s_2, s_1, s_d, s_1, s_2],
    [s_1, s_2, s_3, s_2, s_1, s_2, s_3, s_4, s_3, s_3, s_4, s_5, s_3, s_2, s_2, s_3],
    [s_2, s_3, s_4, s_3, s_2, s_3, s_4, s_5, s_2, s_2, s_3, s_4, s_2,  _,   s_1, s_2],
    [s_1, s_2, s_3, s_2, s_1, s_2, s_3, s_3, s_d, s_1, s_2, s_2, s_d, s_1, s_2, s_3],
    [s_d, s_1, s_2, s_1, s_d, s_1, s_2, s_1,  _,   s_d, s_1,  _,   s_1, s_2, s_3, s_4],
    [s_1, s_2, s_3, s_4, s_3, s_2, s_1,  _,   s_1, s_2, s_3, s_2, s_2, s_3, s_4, s_5],
    [s_2, s_3, s_4, s_5, s_3, s_1,  _,   s_1, s_2, s_3, s_4, s_3, s_1, s_2, s_3, s_4],
    [s_1, s_2, s_3, s_4, s_2, s_d, s_1, s_2, s_3, s_4, s_5, s_3, s_d, s_1, s_2, s_1],
    [ _,   s_1, s_2, s_1, s_d, s_1, s_2, s_3, s_4, s_3, s_2, s_d,  _,   s_d, s_1,  _ ],
    [s_1, s_2, s_3, s_2, s_1, s_2, s_3, s_4, s_3, s_2, s_1, s_2, s_3, s_2, s_1, s_2],
    [s_2, s_3, s_4, s_3, s_2, s_3, s_4, s_5, s_4, s_2, s_2, s_3, s_4, s_3, s_2, s_3],
    [s_1, s_2, s_3, s_2, s_1, s_2, s_3, s_4, s_3, s_1, s_1, s_2, s_3, s_2, s_1, s_2],
    [s_d, s_1, s_2, s_1, s_d, s_1, s_2, s_1, s_d,  _,   s_d, s_1, s_2, s_1, s_d, s_1]
]
create_16x16(sandbearer_leaves_grid, f"{BLOCK_DIR}/sandbearer_leaves.png")

# --- ADHIGAMA LEAVES (Sumeru Tropical Rainforest Deep Lush Canopy) ---
a_d = hex_to_rgba("#082B1B") # Deep rainforest shadow
a_1 = hex_to_rgba("#104E2B") # Dark emerald
a_2 = hex_to_rgba("#1C7A3C") # Vibrant jungle green
a_3 = hex_to_rgba("#2EA84F") # Tropical canopy green
a_4 = hex_to_rgba("#56CE66") # Bright sunlit jungle leaf
a_5 = hex_to_rgba("#96EE85") # Glistening rainforest highlight

adhigama_leaves_grid = [
    [a_2, a_3, a_4,  _,   a_2, a_3, a_5, a_4,  _,   a_1, a_2, a_3, a_4, a_3, a_2, a_1],
    [a_3, a_4, a_5, a_3, a_1, a_2, a_3, a_2, a_1, a_2, a_3, a_4, a_5, a_4, a_2,  _ ],
    [a_1, a_2, a_3, a_2, a_d, a_1, a_2, a_1, a_d, a_1, a_2, a_3, a_4, a_3, a_1, a_2],
    [ _,   a_1, a_2, a_1,  _,   a_d, a_1,  _,   a_1, a_2, a_3, a_2, a_1, a_d, a_1, a_2],
    [a_1, a_2, a_3, a_2, a_1, a_2, a_3, a_2, a_2, a_3, a_4, a_3, a_2, a_1, a_2, a_3],
    [a_2, a_3, a_4, a_3, a_2, a_3, a_4, a_5, a_1, a_2, a_3, a_2, a_1,  _,   a_1, a_2],
    [a_1, a_2, a_3, a_2, a_1, a_2, a_3, a_2, a_d, a_1, a_2, a_1, a_d, a_1, a_2, a_3],
    [a_d, a_1, a_2, a_1, a_d, a_1, a_2, a_1,  _,   a_d, a_1,  _,   a_1, a_2, a_3, a_4],
    [a_1, a_2, a_3, a_4, a_3, a_2, a_1,  _,   a_1, a_2, a_3, a_2, a_2, a_3, a_4, a_3],
    [a_2, a_3, a_4, a_5, a_3, a_1,  _,   a_1, a_2, a_3, a_4, a_3, a_1, a_2, a_3, a_2],
    [a_1, a_2, a_3, a_3, a_2, a_d, a_1, a_2, a_3, a_4, a_3, a_2, a_d, a_1, a_2, a_1],
    [ _,   a_1, a_2, a_1, a_d, a_1, a_2, a_3, a_3, a_2, a_1, a_d,  _,   a_d, a_1,  _ ],
    [a_1, a_2, a_3, a_2, a_1, a_2, a_3, a_4, a_3, a_2, a_1, a_2, a_3, a_2, a_1, a_2],
    [a_2, a_3, a_4, a_3, a_2, a_3, a_4, a_5, a_3, a_1, a_2, a_3, a_4, a_3, a_2, a_3],
    [a_1, a_2, a_3, a_2, a_1, a_2, a_3, a_3, a_2, a_d, a_1, a_2, a_3, a_2, a_1, a_2],
    [a_d, a_1, a_2, a_1, a_d, a_1, a_2, a_1, a_d,  _,   a_d, a_1, a_2, a_1, a_d, a_1]
]
create_16x16(adhigama_leaves_grid, f"{BLOCK_DIR}/adhigama_leaves.png")

# --- MANA WOOD LEAVES (Isekai Bioluminescent Cosmic Twilight Blue & Cyan) ---
m_d = hex_to_rgba("#060E24") # Cosmic void shadow
m_1 = hex_to_rgba("#0E1F4D") # Deep navy foliage
m_2 = hex_to_rgba("#163B7A") # Midnight blue
m_3 = hex_to_rgba("#007A9E") # Glowing cyan-blue vein
m_4 = hex_to_rgba("#00CBE8") # Vivid neon cyan
m_5 = hex_to_rgba("#B8F7FF") # Ethereal starlight sparkle

mana_leaves_grid = [
    [m_1, m_2, m_3,  _,   m_2, m_3, m_4, m_3,  _,   m_1, m_2, m_3, m_4, m_3, m_2, m_1],
    [m_2, m_3, m_4, m_3, m_1, m_2, m_3, m_2, m_1, m_2, m_3, m_4, m_5, m_4, m_1,  _ ],
    [m_1, m_2, m_3, m_2, m_d, m_1, m_2, m_1, m_d, m_1, m_2, m_3, m_4, m_3, m_d, m_1],
    [ _,   m_1, m_2, m_1,  _,   m_d, m_1,  _,   m_1, m_2, m_3, m_2, m_1, m_d, m_1, m_2],
    [m_1, m_2, m_3, m_2, m_1, m_2, m_3, m_2, m_2, m_3, m_4, m_3, m_2, m_1, m_2, m_3],
    [m_2, m_3, m_4, m_3, m_2, m_3, m_4, m_5, m_1, m_2, m_3, m_2, m_1,  _,   m_1, m_2],
    [m_1, m_2, m_3, m_2, m_1, m_2, m_3, m_2, m_d, m_1, m_2, m_1, m_d, m_1, m_2, m_3],
    [m_d, m_1, m_2, m_1, m_d, m_1, m_2, m_1,  _,   m_d, m_1,  _,   m_1, m_2, m_3, m_4],
    [m_1, m_2, m_3, m_4, m_3, m_2, m_1,  _,   m_1, m_2, m_3, m_2, m_2, m_3, m_4, m_3],
    [m_2, m_3, m_4, m_5, m_3, m_1,  _,   m_1, m_2, m_3, m_4, m_3, m_1, m_2, m_3, m_2],
    [m_1, m_2, m_3, m_3, m_2, m_d, m_1, m_2, m_3, m_4, m_3, m_2, m_d, m_1, m_2, m_1],
    [ _,   m_1, m_2, m_1, m_d, m_1, m_2, m_3, m_3, m_2, m_1, m_d,  _,   m_d, m_1,  _ ],
    [m_1, m_2, m_3, m_2, m_1, m_2, m_3, m_4, m_3, m_2, m_1, m_2, m_3, m_2, m_1, m_2],
    [m_2, m_3, m_4, m_3, m_2, m_3, m_4, m_5, m_3, m_1, m_2, m_3, m_4, m_3, m_2, m_3],
    [m_1, m_2, m_3, m_2, m_1, m_2, m_3, m_3, m_2, m_d, m_1, m_2, m_3, m_2, m_1, m_2],
    [m_d, m_1, m_2, m_1, m_d, m_1, m_2, m_1, m_d,  _,   m_d, m_1, m_2, m_1, m_d, m_1]
]
create_16x16(mana_leaves_grid, f"{BLOCK_DIR}/mana_wood_leaves.png")

# --- MANA WOOD LEAVES FRUITED (With glowing celestial Mana Fruit) ---
mf_st = hex_to_rgba("#1C3870") # Stem
mf_1  = hex_to_rgba("#002875") # Deep sapphire fruit base
mf_2  = hex_to_rgba("#005CE6") # Vivid celestial azure
mf_3  = hex_to_rgba("#00D9FF") # Glowing cyan fruit body
mf_4  = hex_to_rgba("#C2FAFF") # Radiant white-cyan star core

mana_fruited_grid = [row[:] for row in mana_leaves_grid]
# Cluster 1 (2, 2)
mana_fruited_grid[2][2] = mf_st
mana_fruited_grid[3][2] = mf_2
mana_fruited_grid[3][3] = mf_3
mana_fruited_grid[4][2] = mf_1
mana_fruited_grid[4][3] = mf_4

# Cluster 2 (2, 11)
mana_fruited_grid[1][11] = mf_st
mana_fruited_grid[2][11] = mf_3
mana_fruited_grid[2][12] = mf_4
mana_fruited_grid[3][11] = mf_2
mana_fruited_grid[3][12] = mf_1

# Cluster 3 (9, 7)
mana_fruited_grid[8][7]  = mf_st
mana_fruited_grid[9][6]  = mf_2
mana_fruited_grid[9][7]  = mf_3
mana_fruited_grid[10][6] = mf_1
mana_fruited_grid[10][7] = mf_4

create_16x16(mana_fruited_grid, f"{BLOCK_DIR}/mana_wood_leaves_fruited.png")

# --- ELDORIA LEAVES (Sacred Imperial Gold & Warm Sunlit White) ---
e_d = hex_to_rgba("#614406") # Honey shadow
e_1 = hex_to_rgba("#9C6E0C") # Dark amber gold
e_2 = hex_to_rgba("#CCA018") # Sacred golden foliage
e_3 = hex_to_rgba("#F5C725") # Brilliant imperial gold
e_4 = hex_to_rgba("#FFE75C") # Luminous radiant yellow
e_5 = hex_to_rgba("#FFFDE0") # Divine diamond shine

eldoria_leaves_grid = [
    [e_2, e_3, e_4,  _,   e_2, e_3, e_5, e_4,  _,   e_1, e_2, e_3, e_4, e_3, e_2, e_1],
    [e_3, e_4, e_5, e_3, e_1, e_2, e_3, e_2, e_1, e_2, e_3, e_4, e_5, e_4, e_2,  _ ],
    [e_1, e_2, e_3, e_2, e_d, e_1, e_2, e_1, e_d, e_1, e_2, e_3, e_4, e_3, e_1, e_2],
    [ _,   e_1, e_2, e_1,  _,   e_d, e_1,  _,   e_1, e_2, e_3, e_2, e_1, e_d, e_1, e_2],
    [e_1, e_2, e_3, e_2, e_1, e_2, e_3, e_4, e_3, e_3, e_4, e_5, e_3, e_2, e_2, e_3],
    [e_2, e_3, e_4, e_3, e_2, e_3, e_4, e_5, e_2, e_2, e_3, e_4, e_2,  _,   e_1, e_2],
    [e_1, e_2, e_3, e_2, e_1, e_2, e_3, e_3, e_d, e_1, e_2, e_2, e_d, e_1, e_2, e_3],
    [e_d, e_1, e_2, e_1, e_d, e_1, e_2, e_1,  _,   e_d, e_1,  _,   e_1, e_2, e_3, e_4],
    [e_1, e_2, e_3, e_4, e_3, e_2, e_1,  _,   e_1, e_2, e_3, e_2, e_2, e_3, e_4, e_5],
    [e_2, e_3, e_4, e_5, e_3, e_1,  _,   e_1, e_2, e_3, e_4, e_3, e_1, e_2, e_3, e_4],
    [e_1, e_2, e_3, e_4, e_2, e_d, e_1, e_2, e_3, e_4, e_5, e_3, e_d, e_1, e_2, e_1],
    [ _,   e_1, e_2, e_1, e_d, e_1, e_2, e_3, e_4, e_3, e_2, e_d,  _,   e_d, e_1,  _ ],
    [e_1, e_2, e_3, e_2, e_1, e_2, e_3, e_4, e_3, e_2, e_1, e_2, e_3, e_2, e_1, e_2],
    [e_2, e_3, e_4, e_3, e_2, e_3, e_4, e_5, e_4, e_2, e_2, e_3, e_4, e_3, e_2, e_3],
    [e_1, e_2, e_3, e_2, e_1, e_2, e_3, e_4, e_3, e_1, e_1, e_2, e_3, e_2, e_1, e_2],
    [e_d, e_1, e_2, e_1, e_d, e_1, e_2, e_1, e_d,  _,   e_d, e_1, e_2, e_1, e_d, e_1]
]
create_16x16(eldoria_leaves_grid, f"{BLOCK_DIR}/eldoria_leaves.png")

# ==============================================================================
# 3. TREE BARK / LOGS & LOG TOPS (Rich 16x16 tactile texture)
# ==============================================================================

# Helper for bark generation
def make_log_texture(b_dark, b_mid1, b_mid2, b_light, b_accent=None):
    grid = []
    pattern = [
        [0, 1, 2, 1, 0, 1, 2, 3, 2, 1, 0, 1, 2, 1, 0, 1],
        [1, 2, 3, 2, 1, 2, 3, 2, 1, 0, 1, 2, 3, 2, 1, 2],
        [0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 3, 2, 1, 0, 1],
        [1, 0, 1, 2, 1, 0, 1, 2, 1, 2, 3, 2, 1, 0, 1, 2],
        [2, 1, 2, 3, 2, 1, 2, 3, 2, 1, 2, 1, 0, 1, 2, 3],
        [1, 0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 1, 2, 3, 2],
        [0, 1, 2, 1, 0, 1, 2, 3, 2, 1, 0, 1, 2, 1, 2, 1],
        [1, 2, 3, 2, 1, 2, 3, 2, 1, 2, 1, 2, 3, 2, 1, 0],
        [0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 3, 2, 1, 0, 1],
        [1, 0, 1, 2, 1, 0, 1, 2, 1, 2, 3, 2, 1, 0, 1, 2],
        [2, 1, 2, 3, 2, 1, 2, 3, 2, 1, 2, 1, 0, 1, 2, 3],
        [1, 2, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 1, 2, 3, 2],
        [0, 1, 2, 1, 0, 1, 2, 3, 2, 1, 0, 1, 2, 1, 2, 1],
        [1, 2, 3, 2, 1, 2, 3, 2, 1, 2, 1, 2, 3, 2, 1, 0],
        [0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 3, 2, 1, 0, 1],
        [1, 0, 1, 2, 1, 0, 1, 2, 1, 2, 3, 2, 1, 0, 1, 2]
    ]
    colors = [b_dark, b_mid1, b_mid2, b_light]
    for y in range(16):
        row = []
        for x in range(16):
            val = pattern[y][x]
            col = colors[val]
            if b_accent and (x == 5 or x == 11) and (y % 4 == 0 or y % 4 == 1):
                col = b_accent
            row.append(col)
        grid.append(row)
    return grid

def make_log_top_texture(bark_col, ring_outer, ring_mid, ring_inner, heartwood, mana_core=None):
    grid = []
    for y in range(16):
        row = []
        for x in range(16):
            # Distance from center (7.5, 7.5)
            dx = x - 7.5
            dy = y - 7.5
            dist = (dx*dx + dy*dy)**0.5
            if dist >= 7.2:
                col = bark_col
            elif dist >= 6.0:
                col = ring_outer
            elif dist >= 4.4:
                col = ring_mid
            elif dist >= 2.8:
                col = ring_outer if int(dist*2)%2==0 else ring_mid
            elif dist >= 1.4:
                col = ring_inner
            else:
                col = mana_core if mana_core else heartwood
            row.append(col)
        grid.append(row)
    return grid

# --- Cuihua Log (Warm hazelnut/chestnut) ---
c_log = make_log_texture(
    hex_to_rgba("#3B2213"), hex_to_rgba("#5C361E"), hex_to_rgba("#7E4D2B"), hex_to_rgba("#A2663A")
)
c_log_top = make_log_top_texture(
    hex_to_rgba("#3B2213"), hex_to_rgba("#C2915D"), hex_to_rgba("#A67543"), hex_to_rgba("#D4A772"), hex_to_rgba("#8A5A2E")
)
create_16x16(c_log, f"{BLOCK_DIR}/cuihua_log.png")
create_16x16(c_log_top, f"{BLOCK_DIR}/cuihua_log_top.png")

# --- Sandbearer Log (Mountain cedar: taupe-grey bark with warm cinnamon fissures) ---
s_log = make_log_texture(
    hex_to_rgba("#453D37"), hex_to_rgba("#685D54"), hex_to_rgba("#887B70"), hex_to_rgba("#A89A8E"),
    b_accent=hex_to_rgba("#BD6E38")
)
s_log_top = make_log_top_texture(
    hex_to_rgba("#453D37"), hex_to_rgba("#C77C40"), hex_to_rgba("#E29F5C"), hex_to_rgba("#F5C48A"), hex_to_rgba("#A15822")
)
create_16x16(s_log, f"{BLOCK_DIR}/sandbearer_log.png")
create_16x16(s_log_top, f"{BLOCK_DIR}/sandbearer_log_top.png")

# --- Adhigama Log (Tropical deep mahogany rainforest) ---
a_log = make_log_texture(
    hex_to_rgba("#2B150B"), hex_to_rgba("#4A2514"), hex_to_rgba("#6B3820"), hex_to_rgba("#8E4E30")
)
a_log_top = make_log_top_texture(
    hex_to_rgba("#2B150B"), hex_to_rgba("#7A3D22"), hex_to_rgba("#9C5230"), hex_to_rgba("#BD6E44"), hex_to_rgba("#5C2B14")
)
create_16x16(a_log, f"{BLOCK_DIR}/adhigama_log.png")
create_16x16(a_log_top, f"{BLOCK_DIR}/adhigama_log_top.png")

# --- Mana Wood Log (Obsidian-indigo with glowing cyan mana fissures) ---
m_log = make_log_texture(
    hex_to_rgba("#0C1226"), hex_to_rgba("#16203D"), hex_to_rgba("#233159"), hex_to_rgba("#35477D"),
    b_accent=hex_to_rgba("#00E5FF")
)
m_log_top = make_log_top_texture(
    hex_to_rgba("#0C1226"), hex_to_rgba("#1C2C54"), hex_to_rgba("#2B427A"), hex_to_rgba("#3E5EA6"), hex_to_rgba("#00A2C7"),
    mana_core=hex_to_rgba("#C7FFFF")
)
create_16x16(m_log, f"{BLOCK_DIR}/mana_wood_log.png")
create_16x16(m_log_top, f"{BLOCK_DIR}/mana_wood_log_top.png")

# --- Eldoria Log (Sacred gold-flecked ivory birch) ---
e_log = make_log_texture(
    hex_to_rgba("#785F26"), hex_to_rgba("#B59B55"), hex_to_rgba("#DED09C"), hex_to_rgba("#F7F2D7"),
    b_accent=hex_to_rgba("#FFD700")
)
e_log_top = make_log_top_texture(
    hex_to_rgba("#785F26"), hex_to_rgba("#D1B052"), hex_to_rgba("#E8CD78"), hex_to_rgba("#FFF0AB"), hex_to_rgba("#A37D1C")
)
create_16x16(e_log, f"{BLOCK_DIR}/eldoria_log.png")
create_16x16(e_log_top, f"{BLOCK_DIR}/eldoria_log_top.png")

# ==============================================================================
# 4. WOOD PLANKS (Distinct wooden boards with grain & nail accents)
# ==============================================================================
def make_planks(p_dark, p_mid1, p_mid2, p_light, p_nail):
    grid = []
    # 4 horizontal boards of 4 pixels each (0-3, 4-7, 8-11, 12-15)
    for y in range(16):
        row = []
        is_seam = (y % 4 == 3)
        board_idx = y // 4
        for x in range(16):
            if is_seam:
                col = p_dark
            elif y % 4 == 0:
                col = p_light
            else:
                # Add vertical joint nail/peg
                joint_x = (4 if board_idx % 2 == 0 else 12)
                if x == joint_x:
                    col = p_dark
                elif x == joint_x - 1 and y % 4 == 1:
                    col = p_nail
                elif (x + y * 3) % 5 == 0:
                    col = p_mid2
                else:
                    col = p_mid1
            row.append(col)
        grid.append(row)
    return grid

create_16x16(make_planks(
    hex_to_rgba("#52341D"), hex_to_rgba("#8A5B35"), hex_to_rgba("#A46F43"), hex_to_rgba("#C28855"), hex_to_rgba("#382110")
), f"{BLOCK_DIR}/cuihua_planks.png")

create_16x16(make_planks(
    hex_to_rgba("#6E3816"), hex_to_rgba("#A85D2A"), hex_to_rgba("#C77439"), hex_to_rgba("#E59253"), hex_to_rgba("#47220B")
), f"{BLOCK_DIR}/sandbearer_planks.png")

create_16x16(make_planks(
    hex_to_rgba("#38190C"), hex_to_rgba("#612E18"), hex_to_rgba("#7A3C20"), hex_to_rgba("#9C522E"), hex_to_rgba("#240E05")
), f"{BLOCK_DIR}/adhigama_planks.png")

create_16x16(make_planks(
    hex_to_rgba("#0C142B"), hex_to_rgba("#182A52"), hex_to_rgba("#223B73"), hex_to_rgba("#32559E"), hex_to_rgba("#00E5FF")
), f"{BLOCK_DIR}/mana_wood_planks.png")

create_16x16(make_planks(
    hex_to_rgba("#695116"), hex_to_rgba("#A8872D"), hex_to_rgba("#C7A43E"), hex_to_rgba("#E5C35E"), hex_to_rgba("#FFE77A")
), f"{BLOCK_DIR}/eldoria_planks.png")

# ==============================================================================
# 5. SAPLINGS (Handcrafted charming pixel art shoots)
# ==============================================================================
def make_sapling(leaf_d, leaf_m, leaf_l, stem_col, bud_col=None):
    grid = [[_ for _ in range(16)] for _ in range(16)]
    # Soil mound
    for x in range(5, 11):
        grid[15][x] = hex_to_rgba("#593D29")
    for x in range(6, 10):
        grid[14][x] = hex_to_rgba("#7A5438")
    
    # Stem
    grid[13][7] = stem_col
    grid[12][7] = stem_col
    grid[11][7] = stem_col
    grid[10][7] = stem_col
    grid[9][8]  = stem_col
    grid[8][8]  = stem_col

    # Leaves cluster
    leaf_coords = [
        (7, 7, leaf_l), (7, 8, leaf_l), (7, 9, leaf_m),
        (6, 6, leaf_l), (6, 7, leaf_m), (6, 8, leaf_m), (6, 9, leaf_d),
        (5, 7, leaf_l), (5, 8, leaf_m), (5, 9, leaf_d),
        (4, 7, leaf_l), (4, 8, leaf_m),
        (8, 6, leaf_d), (9, 6, leaf_m), (9, 5, leaf_l),
        (7, 10, leaf_d), (8, 10, leaf_m), (8, 11, leaf_l)
    ]
    for y, x, col in leaf_coords:
        grid[y][x] = col

    if bud_col:
        grid[5][8] = bud_col
        grid[4][8] = bud_col

    return grid

create_16x16(make_sapling(
    hex_to_rgba("#1F5F26"), hex_to_rgba("#35883A"), hex_to_rgba("#7EE055"), hex_to_rgba("#5C361E"), bud_col=hex_to_rgba("#FF9A26")
), f"{BLOCK_DIR}/cuihua_sapling.png")

create_16x16(make_sapling(
    hex_to_rgba("#8E470C"), hex_to_rgba("#C47414"), hex_to_rgba("#FFD447"), hex_to_rgba("#685D54"), bud_col=hex_to_rgba("#FFF192")
), f"{BLOCK_DIR}/sandbearer_sapling.png")

create_16x16(make_sapling(
    hex_to_rgba("#104E2B"), hex_to_rgba("#1C7A3C"), hex_to_rgba("#56CE66"), hex_to_rgba("#4A2514")
), f"{BLOCK_DIR}/adhigama_sapling.png")

create_16x16(make_sapling(
    hex_to_rgba("#0E1F4D"), hex_to_rgba("#163B7A"), hex_to_rgba("#00CBE8"), hex_to_rgba("#16203D"), bud_col=hex_to_rgba("#B8F7FF")
), f"{BLOCK_DIR}/mana_wood_sapling.png")

create_16x16(make_sapling(
    hex_to_rgba("#9C6E0C"), hex_to_rgba("#CCA018"), hex_to_rgba("#FFE75C"), hex_to_rgba("#B59B55"), bud_col=hex_to_rgba("#FFFDE0")
), f"{BLOCK_DIR}/eldoria_sapling.png")

print("COMPLETE! All 21 wood, leaf, fruited, plank and sapling textures handcrafted with 100% genuine Minecraft pixel art!")
