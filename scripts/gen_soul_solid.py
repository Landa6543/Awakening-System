from PIL import Image, ImageDraw
import os

target_dir = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\assets\awakening_system\textures\item"
os.makedirs(target_dir, exist_ok=True)

# 64x64 pixel art for Soul Solid (Cane Sword)
# Diagonal layout from bottom-left (handle) to top-right (blade tip)
size = 64
img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Handle & Cane grip: purple cane with ornate gold trim
# From (8, 55) to (20, 43)
handle_coords = []
for i in range(14):
    x = 7 + i
    y = 56 - i
    # Cane handle width 3 px
    draw.line([(x-1, y), (x+1, y)], fill=(80, 25, 110, 255))
    draw.point((x, y), fill=(120, 40, 165, 255))
    draw.point((x-1, y+1), fill=(55, 15, 80, 255))

# Gold collar / ring (tsuba) around (21, 42)
collar_points = [(20, 43), (21, 42), (22, 41), (21, 44), (22, 43), (23, 42)]
for p in collar_points:
    draw.point(p, fill=(245, 205, 50, 255))
draw.point((21, 42), fill=(255, 245, 140, 255))

# Slender Ice Steel Blade from (23, 40) to (57, 6)
for i in range(36):
    x = 22 + i
    y = 41 - i
    # Blade core
    draw.point((x, y), fill=(225, 250, 255, 255))
    # Soul Frost edge (cyan / soul ice glow)
    draw.point((x-1, y), fill=(130, 225, 255, 240))
    draw.point((x, y+1), fill=(70, 175, 245, 230))
    # Underworld cold accent
    if i % 3 == 0:
        draw.point((x+1, y-1), fill=(185, 240, 255, 255))

# Blade tip at (58, 5)
draw.point((57, 6), fill=(255, 255, 255, 255))
draw.point((58, 5), fill=(210, 250, 255, 255))

# Save
dest = os.path.join(target_dir, "soul_solid.png")
img.save(dest)
print("Saved Soul Solid texture to:", dest)
