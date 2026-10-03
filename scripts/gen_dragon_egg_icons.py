import os
import math
from PIL import Image, ImageDraw

output_dir = "src/main/resources/assets/awakening_system/textures/item"
os.makedirs(output_dir, exist_ok=True)

def create_egg_icon(filename, base_color_fn, crack_color_fn, glow_color):
    size = 32
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()

    # Center is at (16, 17), egg shape: radius_x ~ 9, radius_y ~ 12, tapered towards top
    cx, cy = 15.5, 16.5
    rx = 8.5
    ry = 12.0

    # Draw egg body
    for y in range(size):
        for x in range(size):
            dx = x - cx
            dy = y - cy
            # Tapering factor: narrower at top (dy < 0), wider at bottom (dy > 0)
            taper = 1.0 - (dy / ry) * 0.22
            norm_x = dx / (rx * taper)
            norm_y = dy / ry

            dist_sq = norm_x * norm_x + norm_y * norm_y
            if dist_sq <= 1.0:
                dist = math.sqrt(dist_sq)
                # Shading from light source at top-left (-0.6, -0.6)
                light_dx = (x - (cx - 3.5)) / rx
                light_dy = (y - (cy - 4.5)) / ry
                light_dist = math.sqrt(light_dx * light_dx + light_dy * light_dy)
                shade = max(0.0, min(1.0, 1.15 - light_dist * 0.75))

                # Edge outline darkness
                edge_factor = 1.0 if dist < 0.88 else (1.0 - (dist - 0.88) * 6.0)

                # Base rock texture noise
                noise = (math.sin(x * 1.8 + y * 2.3) * 0.5 + 0.5) * 0.25

                r, g, b = base_color_fn(shade + noise)

                # Cracks / scales pattern
                crack_val = math.sin(x * 0.9 + math.cos(y * 1.1) * 2.2) + math.cos(y * 0.8 + math.sin(x * 1.2) * 1.8)
                if abs(crack_val) < 0.38:
                    intensity = 1.0 - abs(crack_val) / 0.38
                    cr, cg, cb = crack_color_fn(intensity)
                    r = int(r * (1 - intensity) + cr * intensity)
                    g = int(g * (1 - intensity) + cg * intensity)
                    b = int(b * (1 - intensity) + cb * intensity)

                # Outline border
                if dist >= 0.92:
                    r = int(r * 0.35)
                    g = int(g * 0.35)
                    b = int(b * 0.35)
                else:
                    r = int(r * edge_factor)
                    g = int(g * edge_factor)
                    b = int(b * edge_factor)

                pixels[x, y] = (max(0, min(255, r)), max(0, min(255, g)), max(0, min(255, b)), 255)

    img.save(os.path.join(output_dir, filename), "PNG")
    print(f"Generated {filename}")

# 1. Fire Egg
def fire_base(val):
    val = max(0.0, min(1.0, val))
    return int(45 + 90 * val), int(18 + 35 * val), int(12 + 20 * val)

def fire_cracks(val):
    return int(255), int(80 + 175 * val), int(10 + 60 * val)

create_egg_icon("ancient_dragon_egg_fire.png", fire_base, fire_cracks, (255, 140, 20))

# 2. Ice Egg
def ice_base(val):
    val = max(0.0, min(1.0, val))
    return int(15 + 40 * val), int(45 + 110 * val), int(80 + 150 * val)

def ice_cracks(val):
    return int(140 + 115 * val), int(220 + 35 * val), int(255)

create_egg_icon("ancient_dragon_egg_ice.png", ice_base, ice_cracks, (180, 240, 255))

# 3. Forest Egg
def forest_base(val):
    val = max(0.0, min(1.0, val))
    return int(18 + 35 * val), int(45 + 85 * val), int(22 + 45 * val)

def forest_cracks(val):
    return int(60 + 160 * val), int(210 + 45 * val), int(80 + 110 * val)

create_egg_icon("ancient_dragon_egg_forest.png", forest_base, forest_cracks, (100, 255, 130))

# 4. Shadow Egg
def shadow_base(val):
    val = max(0.0, min(1.0, val))
    return int(22 + 45 * val), int(12 + 25 * val), int(38 + 70 * val)

def shadow_cracks(val):
    return int(190 + 65 * val), int(60 + 120 * val), int(230 + 25 * val)

create_egg_icon("ancient_dragon_egg_shadow.png", shadow_base, shadow_cracks, (220, 100, 255))

# 5. Dragon Nest Icon
nest_img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
nest_pix = nest_img.load()
for y in range(32):
    for x in range(32):
        dx = x - 15.5
        dy = (y - 18.0) * 1.6  # Elliptical bowl
        dist = math.sqrt(dx * dx + dy * dy)
        if dist <= 13.5:
            # Rim vs center bed
            if dist >= 9.0:
                # Woven twigs / branch rim
                twig_pat = math.sin(x * 1.5 + y * 0.8) * 0.5 + 0.5
                r = int(50 + 40 * twig_pat)
                g = int(32 + 25 * twig_pat)
                b = int(20 + 15 * twig_pat)
                # Outer shadow
                if dist >= 12.5:
                    r = int(r * 0.5)
                    g = int(g * 0.5)
                    b = int(b * 0.5)
            else:
                # Straw bed inside
                straw = math.cos(x * 2.2 - y * 1.5) * 0.5 + 0.5
                r = int(120 + 45 * straw)
                g = int(90 + 35 * straw)
                b = int(45 + 20 * straw)
                # Small glowing ember
                if (x == 15 and y == 18) or (x == 17 and y == 19):
                    r, g, b = 255, 140, 30
            nest_pix[x, y] = (r, g, b, 255)

nest_img.save(os.path.join(output_dir, "dragon_nest.png"), "PNG")
print("Generated dragon_nest.png")
