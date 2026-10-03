import os
import shutil

src_root = r"c:\Users\soybe\Desktop\Mods NeoForge\genshin_nature_workspace\build\resources\main\assets\genshin_nature"
dst_root = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\assets\awakening_system"

item_src = os.path.join(src_root, "textures", "items")
item_dst = os.path.join(dst_root, "textures", "item")
gui_dst = os.path.join(dst_root, "textures", "gui")

os.makedirs(item_dst, exist_ok=True)
os.makedirs(gui_dst, exist_ok=True)

# 1. Vision textures mapping
vision_map = {
    "anemo_vision_final.png": "vision_anemo.png",
    "geo_vision_final.png": "vision_geo.png",
    "electro_vision_final.png": "vision_electro.png",
    "dendro_vision_final.png": "vision_dendro.png",
    "hydro_vision_final.png": "vision_hydro.png",
    "pyro_vision_final.png": "vision_pyro.png",
    "cryo_vision_final.png": "vision_cryo.png",
}

for src_name, dst_name in vision_map.items():
    s = os.path.join(item_src, src_name)
    d = os.path.join(item_dst, dst_name)
    if os.path.exists(s):
        shutil.copy2(s, d)
        print(f"Copied vision: {src_name} -> {dst_name}")
    else:
        print(f"WARNING: {s} not found")

# Also copy with their original names just in case
for v in ["anemo_vision.png", "geo_vision.png", "electro_vision.png", "hydro_vision.png", "pyro_vision.png", "cryo_vision.png", "visions_menu_final.png"]:
    s = os.path.join(item_src, v)
    d = os.path.join(item_dst, v)
    if os.path.exists(s):
        shutil.copy2(s, d)
        print(f"Copied {v}")

# Copy visionsmenugui.png to gui folder
gui_src = os.path.join(src_root, "textures", "visionsmenugui.png")
if os.path.exists(gui_src):
    shutil.copy2(gui_src, os.path.join(gui_dst, "visionsmenugui.png"))
    print("Copied visionsmenugui.png")

# 2. Resins
for r in ["original_resin.png", "condensed_resin.png", "transient_resin.png"]:
    s = os.path.join(item_src, r)
    d = os.path.join(item_dst, r)
    if os.path.exists(s):
        shutil.copy2(s, d)
        print(f"Copied resin: {r}")

# 3. Slime materials
for sm in ["slime_condensate.png", "slime_secretions.png", "slime_concentrate.png"]:
    s = os.path.join(item_src, sm)
    d = os.path.join(item_dst, sm)
    if os.path.exists(s):
        shutil.copy2(s, d)
        print(f"Copied slime material: {sm}")

# 4. Masks
for m in ["damaged_mask.png", "stained_mask.png", "ominous_mask.png"]:
    s = os.path.join(item_src, m)
    d = os.path.join(item_dst, m)
    if os.path.exists(s):
        shutil.copy2(s, d)
        print(f"Copied mask: {m}")

# 5. Horns
for h in ["heavy_horn.png", "black_bronze_horn.png", "black_crystal_horn.png"]:
    s = os.path.join(item_src, h)
    d = os.path.join(item_dst, h)
    if os.path.exists(s):
        shutil.copy2(s, d)
        print(f"Copied horn: {h}")

# 6. Mora (animated texture + mcmeta!)
mora_png = os.path.join(item_src, "mora.png")
mora_mcmeta = os.path.join(item_src, "mora.png.mcmeta")
shutil.copy2(mora_png, os.path.join(item_dst, "mora.png"))
if os.path.exists(mora_mcmeta):
    shutil.copy2(mora_mcmeta, os.path.join(item_dst, "mora.png.mcmeta"))
print("Copied animated mora.png and mcmeta")
