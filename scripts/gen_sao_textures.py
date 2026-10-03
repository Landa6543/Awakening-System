import os
from PIL import Image

out_dir = "src/main/resources/assets/awakening_system/textures/item"
os.makedirs(out_dir, exist_ok=True)

# 1. Elucidator (32x32) - Kirito's pitch-black sword with pure silver lining
img_elu = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

# Blade runs diagonally from bottom-left (3, 28) towards top-right (28, 3)
for i in range(25):
    bx = 4 + i
    by = 27 - i
    # Center blade (pure dark obsidian/carbon)
    img_elu.putpixel((bx, by), (26, 26, 32, 255))
    # Blade highlights (silver edge)
    if bx + 1 < 32 and by - 1 >= 0:
        img_elu.putpixel((bx + 1, by), (45, 52, 60, 255))
    if bx - 1 >= 0 and by + 1 < 32:
        img_elu.putpixel((bx, by + 1), (18, 18, 22, 255))
    # Outer silver sheen along tip and edges
    if i > 5 and i < 24:
        img_elu.putpixel((bx, by - 1), (210, 220, 235, 230))

# Tip (point)
img_elu.putpixel((29, 2), (240, 245, 255, 255))
img_elu.putpixel((28, 2), (200, 210, 225, 255))
img_elu.putpixel((29, 3), (200, 210, 225, 255))

# Crossguard at (8, 23)
for gx in range(-2, 3):
    img_elu.putpixel((8 + gx, 23 + gx), (180, 190, 205, 255))
    img_elu.putpixel((8 + gx - 1, 23 + gx + 1), (120, 130, 145, 255))

# Hilt & Pommel
img_elu.putpixel((6, 25), (40, 40, 45, 255))
img_elu.putpixel((5, 26), (30, 30, 35, 255))
img_elu.putpixel((4, 27), (20, 20, 25, 255))
img_elu.putpixel((3, 28), (190, 200, 215, 255)) # Pommel silver ring

img_elu.save(os.path.join(out_dir, "elucidator.png"))
print("Generated elucidator.png")


# 2. Dark Repulser (32x32) - Kirito's crystalline teal/emerald sword
img_rep = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

# Blade
for i in range(25):
    bx = 4 + i
    by = 27 - i
    # Crystalline turquoise/aquamarine core
    img_rep.putpixel((bx, by), (64, 212, 180, 255))
    if bx + 1 < 32 and by - 1 >= 0:
        img_rep.putpixel((bx + 1, by), (130, 245, 220, 255)) # bright crystal facet
    if bx - 1 >= 0 and by + 1 < 32:
        img_rep.putpixel((bx, by + 1), (32, 150, 130, 255)) # shadow facet
    if i > 6 and i < 24:
        img_rep.putpixel((bx, by - 1), (200, 255, 245, 240)) # glint

# Tip
img_rep.putpixel((29, 2), (230, 255, 250, 255))
img_rep.putpixel((28, 2), (160, 245, 225, 255))
img_rep.putpixel((29, 3), (160, 245, 225, 255))

# Crossguard cross wings
for gx in range(-3, 4):
    img_rep.putpixel((8 + gx, 23 + gx), (70, 185, 160, 255))
    img_rep.putpixel((8 + gx - 1, 23 + gx + 1), (40, 130, 110, 255))
img_rep.putpixel((9, 21), (220, 255, 240, 255))
img_rep.putpixel((6, 24), (220, 255, 240, 255))

# Hilt & Pommel
img_rep.putpixel((6, 25), (45, 90, 80, 255))
img_rep.putpixel((5, 26), (35, 75, 65, 255))
img_rep.putpixel((4, 27), (30, 65, 55, 255))
img_rep.putpixel((3, 28), (140, 230, 210, 255)) # Pommel crystal

img_rep.save(os.path.join(out_dir, "dark_repulser.png"))
print("Generated dark_repulser.png")


# 3. Lambent Light (32x32) - Asuna's elegant rapier
img_lam = Image.new("RGBA", (32, 32), (0, 0, 0, 0))

# Ultra fine rapier needle blade
for i in range(25):
    bx = 4 + i
    by = 27 - i
    # Silver-white fine needle
    img_lam.putpixel((bx, by), (235, 240, 250, 255))
    if bx + 1 < 32:
        img_lam.putpixel((bx + 1, by), (180, 195, 215, 255))
    if i > 8 and i < 24:
        img_lam.putpixel((bx, by - 1), (255, 255, 255, 255)) # extreme shine

# Tip
img_lam.putpixel((29, 2), (255, 255, 255, 255))
img_lam.putpixel((28, 2), (220, 230, 245, 255))

# Bell cup-guard (swept rapier cup)
guard_pixels = [
    (8, 22), (9, 22), (10, 22),
    (7, 23), (8, 23), (9, 23),
    (6, 24), (7, 24), (8, 24)
]
for gp in guard_pixels:
    img_lam.putpixel(gp, (120, 225, 215, 255)) # Cyan cup
img_lam.putpixel((9, 21), (255, 215, 0, 255)) # Gold rim
img_lam.putpixel((7, 25), (255, 215, 0, 255))

# Handle & Pommel
img_lam.putpixel((6, 25), (240, 240, 245, 255))
img_lam.putpixel((5, 26), (230, 230, 235, 255))
img_lam.putpixel((4, 27), (255, 215, 0, 255)) # Gold pommel
img_lam.putpixel((3, 28), (255, 235, 120, 255))

img_lam.save(os.path.join(out_dir, "lambent_light.png"))
print("Generated lambent_light.png")
