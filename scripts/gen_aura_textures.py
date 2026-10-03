import math
import os
from PIL import Image, ImageDraw

out_dir = "src/main/resources/assets/awakening_system/textures/vfx"
os.makedirs(out_dir, exist_ok=True)

# 1. aura_flame.png (256x256) - Vertical flame ribbons with seamless vertical wrapping
w, h = 256, 256
img_flame = Image.new("RGBA", (w, h), (0, 0, 0, 0))
for y in range(h):
    # vertical envelope: brighter in middle-lower, fades at top
    v_norm = y / float(h)
    v_fade = math.sin(v_norm * math.pi) ** 0.8
    for x in range(w):
        u_norm = x / float(w)
        # horizontal flame spikes (sine harmonics)
        ribbon1 = math.sin(u_norm * 8.0 * math.pi + y * 0.12)
        ribbon2 = math.cos(u_norm * 14.0 * math.pi - y * 0.18)
        ribbon3 = math.sin(u_norm * 4.0 * math.pi + y * 0.06)
        val = 0.5 + 0.25 * ribbon1 + 0.15 * ribbon2 + 0.10 * ribbon3
        val = max(0.0, min(1.0, val))
        
        # alpha calculation with high intensity core
        alpha = int(255 * (val ** 2.2) * v_fade)
        if alpha > 0:
            # Color: White core transitioning to golden-yellow edge
            core_factor = val ** 3.0
            r = 255
            g = int(210 + 45 * core_factor)
            b = int(70 + 185 * core_factor)
            img_flame.putpixel((x, y), (r, g, b, alpha))

img_flame.save(os.path.join(out_dir, "aura_flame.png"))
print("Generated aura_flame.png")

# 2. aura_ki_stream.png (256x256) - Pearlescent ethereal waves for meditation/cultivation
img_ki = Image.new("RGBA", (w, h), (0, 0, 0, 0))
for y in range(h):
    v_norm = y / float(h)
    v_fade = math.sin(v_norm * math.pi) ** 0.6
    for x in range(w):
        u_norm = x / float(w)
        # harmonic gentle flow
        wave1 = math.sin(u_norm * 6.0 * math.pi + y * 0.08)
        wave2 = math.cos(u_norm * 10.0 * math.pi - y * 0.05)
        wave3 = math.sin(u_norm * 2.0 * math.pi + y * 0.03)
        val = 0.5 + 0.3 * wave1 + 0.15 * wave2 + 0.15 * wave3
        val = max(0.0, min(1.0, val))
        
        alpha = int(220 * (val ** 1.8) * v_fade)
        if alpha > 0:
            # Pearlescent white with cyan and soft gold tint
            core = val ** 2.0
            r = int(220 + 35 * core)
            g = int(240 + 15 * core)
            b = 255
            img_ki.putpixel((x, y), (r, g, b, alpha))

img_ki.save(os.path.join(out_dir, "aura_ki_stream.png"))
print("Generated aura_ki_stream.png")

# 3. aura_lightning.png (256x256) - Electrical sparks and jagged arcs
img_light = Image.new("RGBA", (w, h), (0, 0, 0, 0))
draw_light = ImageDraw.Draw(img_light)
import random
random.seed(42)
for strand in range(12):
    curr_x = random.randint(20, w - 20)
    curr_y = 0
    points = [(curr_x, curr_y)]
    while curr_y < h:
        curr_y += random.randint(12, 28)
        curr_x += random.randint(-22, 22)
        curr_x = max(10, min(w - 10, curr_x))
        points.append((curr_x, min(h, curr_y)))
    # draw glow
    for i in range(len(points) - 1):
        draw_light.line([points[i], points[i+1]], fill=(140, 220, 255, 120), width=5)
        draw_light.line([points[i], points[i+1]], fill=(255, 255, 255, 255), width=2)

img_light.save(os.path.join(out_dir, "aura_lightning.png"))
print("Generated aura_lightning.png")

# 4. aura_ground_ring.png (256x256) - Concentric expanding shockwave energy disc
img_ring = Image.new("RGBA", (w, h), (0, 0, 0, 0))
cx, cy = w / 2.0, h / 2.0
max_r = w / 2.0 - 4
for y in range(h):
    for x in range(w):
        dx = x - cx
        dy = y - cy
        dist = math.sqrt(dx*dx + dy*dy)
        if dist <= max_r and dist > 4:
            norm_dist = dist / max_r
            # concentric pulse rings
            ring1 = math.sin(norm_dist * 8.0 * math.pi) ** 2
            ring2 = math.cos(norm_dist * 4.0 * math.pi) ** 2
            edge_fade = math.sin(norm_dist * math.pi) ** 1.2
            intensity = (0.6 * ring1 + 0.4 * ring2) * edge_fade
            alpha = int(240 * (intensity ** 1.5))
            if alpha > 0:
                img_ring.putpixel((x, y), (255, 240, 180, alpha))

img_ring.save(os.path.join(out_dir, "aura_ground_ring.png"))
print("Generated aura_ground_ring.png")
