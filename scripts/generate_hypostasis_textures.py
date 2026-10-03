import os
from PIL import Image, ImageDraw

out_dir = 'src/main/resources/assets/awakening_system/textures/entity/hypostasis'
os.makedirs(out_dir, exist_ok=True)

# 1. ANEMO HYPOSTASIS (64x64)
# Core: (0, 0) to (32, 16) for 8x8x8 box
# Cube: (32, 0) to (64, 16) for 6x6x6 box
anemo_base = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
anemo_glow = Image.new('RGBA', (64, 64), (0, 0, 0, 0))

draw_ab = ImageDraw.Draw(anemo_base)
draw_ag = ImageDraw.Draw(anemo_glow)

# Core colors: Deep teal, bright cyan, neon mint
# Draw core base (texOffs 0, 0: box 8x8x8 -> width = 8*2+8*2=32, height = 8+8=16)
for y in range(16):
    for x in range(32):
        # Checker / faceted crystal pattern
        diag = (x + y) % 4
        if diag == 0:
            c = (40, 160, 160, 255)
        elif diag == 1:
            c = (60, 210, 200, 255)
        elif diag == 2:
            c = (90, 245, 230, 255)
        else:
            c = (30, 130, 130, 255)
        
        # Center rune highlight
        if 2 <= (x % 8) <= 5 and 2 <= (y % 8) <= 5:
            c = (180, 255, 245, 255)
            draw_ag.point((x, y), fill=(180, 255, 245, 255))
        else:
            draw_ag.point((x, y), fill=(60, 210, 200, 200))
        draw_ab.point((x, y), fill=c)

# Cube plating (texOffs 32, 0: box 6x6x6 -> width = 6*4=24, height = 6*2=12)
# Basalt / ancient dark stone casing with cyan inlays
for y in range(12):
    for x in range(32, 56):
        # Border of cube: dark basalt
        lx = (x - 32) % 6
        ly = y % 6
        is_border = (lx == 0 or lx == 5 or ly == 0 or ly == 5)
        is_circuit = (lx == 2 and ly == 2) or (lx == 3 and ly == 3)
        
        if is_border:
            c = (25, 30, 35, 255)
        elif is_circuit:
            c = (80, 250, 230, 255)
            draw_ag.point((x, y), fill=(80, 250, 230, 255))
        else:
            shade = 45 + ((x + y * 2) % 15)
            c = (shade, shade + 5, shade + 10, 255)
            
        draw_ab.point((x, y), fill=c)

anemo_base.save(os.path.join(out_dir, 'anemo_hypostasis.png'))
anemo_glow.save(os.path.join(out_dir, 'anemo_hypostasis_glow.png'))
print("Saved Anemo Hypostasis textures!")

# 2. ELECTRO HYPOSTASIS (64x64)
electro_base = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
electro_glow = Image.new('RGBA', (64, 64), (0, 0, 0, 0))

draw_eb = ImageDraw.Draw(electro_base)
draw_eg = ImageDraw.Draw(electro_glow)

# Core: Deep violet, electric purple, neon lavender
for y in range(16):
    for x in range(32):
        diag = (x + y * 2) % 4
        if diag == 0:
            c = (110, 30, 160, 255)
        elif diag == 1:
            c = (165, 50, 225, 255)
        elif diag == 2:
            c = (215, 100, 255, 255)
        else:
            c = (80, 20, 120, 255)
        
        # Center rune highlight
        if 2 <= (x % 8) <= 5 and 2 <= (y % 8) <= 5:
            c = (245, 200, 255, 255)
            draw_eg.point((x, y), fill=(245, 200, 255, 255))
        else:
            draw_eg.point((x, y), fill=(165, 50, 225, 200))
        draw_eb.point((x, y), fill=c)

# Cube plating (basalt with violet lightning circuits)
for y in range(12):
    for x in range(32, 56):
        lx = (x - 32) % 6
        ly = y % 6
        is_border = (lx == 0 or lx == 5 or ly == 0 or ly == 5)
        is_circuit = (lx == 2 and ly == 2) or (lx == 3 and ly == 3) or (lx == 1 and ly == 4)
        
        if is_border:
            c = (28, 22, 35, 255)
        elif is_circuit:
            c = (210, 80, 255, 255)
            draw_eg.point((x, y), fill=(210, 80, 255, 255))
        else:
            shade = 42 + ((x * 2 + y) % 14)
            c = (shade + 8, shade, shade + 15, 255)
            
        draw_eb.point((x, y), fill=c)

electro_base.save(os.path.join(out_dir, 'electro_hypostasis.png'))
electro_glow.save(os.path.join(out_dir, 'electro_hypostasis_glow.png'))
print("Saved Electro Hypostasis textures!")

# 3. ANEMO ORB (32x32)
anemo_orb = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
draw_ao = ImageDraw.Draw(anemo_orb)
for y in range(32):
    for x in range(32):
        dist = ((x - 16)**2 + (y - 16)**2)**0.5
        if dist <= 12:
            factor = (12 - dist) / 12
            r = int(60 + 180 * factor)
            g = int(210 + 45 * factor)
            b = int(200 + 55 * factor)
            a = int(220 * factor + 35)
            draw_ao.point((x, y), fill=(r, g, b, min(255, a)))

anemo_orb.save(os.path.join(out_dir, 'anemo_orb.png'))
print("Saved Anemo Orb texture!")

# 4. ELECTRO PRISM (32x32)
electro_prism = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
draw_ep = ImageDraw.Draw(electro_prism)
for y in range(32):
    for x in range(32):
        dx = abs(x - 16)
        dy = abs(y - 16)
        # Diamond / crystal shape
        if dx + dy <= 14:
            factor = (14 - (dx + dy)) / 14
            r = int(140 + 100 * factor)
            g = int(40 + 160 * factor)
            b = int(210 + 45 * factor)
            draw_ep.point((x, y), fill=(r, g, b, 255))
        elif dx + dy == 15:
            draw_ep.point((x, y), fill=(255, 180, 255, 255)) # Glowing edge

electro_prism.save(os.path.join(out_dir, 'electro_prism.png'))
print("Saved Electro Prism texture!")
