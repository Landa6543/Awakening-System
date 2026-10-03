from PIL import Image, ImageDraw
import os

target_dir = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\assets\awakening_system\textures\entity"
os.makedirs(target_dir, exist_ok=True)

# 1. Lunar Shadow (64x64) - Astral silver-cyan ethereal phantom
img_shadow = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
draw_s = ImageDraw.Draw(img_shadow)

# Fill base skin with celestial gradient & glowing runes
# Head: 0,0 to 32,16
for y in range(16):
    for x in range(32):
        c = (20 + y*2, 35 + y*4, 70 + y*6, 230)
        draw_s.point((x, y), fill=c)
# Head eyes (cyan glow)
draw_s.rectangle([10, 8, 11, 10], fill=(130, 240, 255, 255))
draw_s.rectangle([14, 8, 15, 10], fill=(130, 240, 255, 255))

# Torso (16,16 to 40,32)
for y in range(16, 32):
    for x in range(16, 40):
        c = (15 + (x%3)*10, 30 + (y%4)*8, 65 + (x%5)*12, 235)
        draw_s.point((x, y), fill=c)
# Starlight core on chest
draw_s.rectangle([22, 20, 25, 23], fill=(160, 230, 255, 255))
draw_s.point((23, 21), fill=(255, 255, 255, 255))
draw_s.point((24, 22), fill=(255, 255, 255, 255))

# Arms (40,16 to 56,32 and 32,48 to 48,64)
for y in range(16, 32):
    for x in range(40, 56):
        draw_s.point((x, y), fill=(20, 40, 80, 220))
for y in range(48, 64):
    for x in range(32, 48):
        draw_s.point((x, y), fill=(20, 40, 80, 220))

# Legs (0,16 to 16,32 and 16,48 to 32,64)
for y in range(16, 32):
    for x in range(0, 16):
        draw_s.point((x, y), fill=(15, 25, 60, 230))
for y in range(48, 64):
    for x in range(16, 32):
        draw_s.point((x, y), fill=(15, 25, 60, 230))

# Outer celestial veil / armor layer (32,0 to 64,16 and 0,32 to 64,48)
for y in range(0, 16):
    for x in range(32, 64):
        if (x + y) % 3 == 0:
            draw_s.point((x, y), fill=(180, 225, 255, 200))
        elif (x * y) % 5 == 0:
            draw_s.point((x, y), fill=(100, 180, 240, 180))

shadow_path = os.path.join(target_dir, "lunar_shadow.png")
img_shadow.save(shadow_path)
print("Saved:", shadow_path)

# 2. Lunar Guardian (64x64) - Canon's heavy cosmic silent sentinel
img_guard = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
draw_g = ImageDraw.Draw(img_guard)

# Heavy dark obsidian-amethyst plate
for y in range(16):
    for x in range(32):
        c = (25 + y*2, 10 + y, 45 + y*4, 255)
        draw_g.point((x, y), fill=c)

# Dark Purple slit visor
draw_g.rectangle([9, 7, 16, 9], fill=(220, 130, 255, 255))
draw_g.rectangle([11, 8, 14, 8], fill=(255, 255, 255, 255))

# Torso (16,16 to 40,32)
for y in range(16, 32):
    for x in range(16, 40):
        c = (30 + (x%2)*8, 15 + (y%3)*5, 55 + (x*y)%20, 255)
        draw_g.point((x, y), fill=c)
# Canon's New Moon Rune
draw_g.rectangle([22, 19, 25, 24], fill=(180, 70, 240, 255))
draw_g.point((23, 21), fill=(255, 220, 255, 255))

# Arms & Pauldrons
for y in range(16, 32):
    for x in range(40, 56):
        draw_g.point((x, y), fill=(35, 18, 60, 255))
for y in range(48, 64):
    for x in range(32, 48):
        draw_g.point((x, y), fill=(35, 18, 60, 255))

# Heavy greaves
for y in range(16, 32):
    for x in range(0, 16):
        draw_g.point((x, y), fill=(20, 12, 40, 255))
for y in range(48, 64):
    for x in range(16, 32):
        draw_g.point((x, y), fill=(20, 12, 40, 255))

# Armor layer details (helmet horns, shoulder pads)
for y in range(0, 16):
    for x in range(32, 64):
        if (x + y) % 2 == 0:
            draw_g.point((x, y), fill=(80, 40, 130, 240))
        elif (x * y) % 3 == 0:
            draw_g.point((x, y), fill=(160, 90, 230, 240))

guard_path = os.path.join(target_dir, "lunar_guardian.png")
img_guard.save(guard_path)
print("Saved:", guard_path)
