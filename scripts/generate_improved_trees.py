import math
import random
from PIL import Image, ImageDraw

def generate_improved_tree_textures():
    size = 32
    target_dir = "src/main/resources/assets/awakening_system/textures/block"
    
    # Use deterministic seeded perlin/cellular-like organic texture generation
    rng = random.Random(1337)

    def generate_bark(base_col, groove_col, highlight_col, moss_col=None, knot_freq=0.03):
        im = Image.new("RGBA", (size, size))
        # Generate vertical wavy bark columns
        cols = []
        cx = 0
        while cx < size:
            w = rng.randint(3, 5)
            cols.append((cx, w))
            cx += w
            
        for y in range(size):
            for x in range(size):
                # find which column
                col_idx = 0
                for i, (start_x, w) in enumerate(cols):
                    if start_x <= x < start_x + w:
                        col_idx = i
                        break
                
                # Column edge groove
                is_groove = False
                for start_x, w in cols:
                    if x == start_x or x == start_x + w - 1:
                        # wavy wobble
                        if (y + col_idx * 3) % 4 != 0:
                            is_groove = True

                # Bark grain noise
                grain = rng.uniform(-0.15, 0.15)
                # vertical light gradient
                v_var = math.sin((y + x * 0.5) * 0.3) * 0.1

                if is_groove:
                    r, g, b = [int(c * (0.65 + grain * 0.5)) for c in groove_col]
                else:
                    # check if highlight ridge (center of column)
                    for start_x, w in cols:
                        if start_x < x < start_x + w - 1:
                            if abs(x - (start_x + w / 2.0)) < 0.8:
                                factor = 1.0 + v_var + grain
                                r, g, b = [min(255, int(c * factor)) for c in highlight_col]
                                break
                    else:
                        factor = 1.0 + v_var * 0.5 + grain
                        r, g, b = [min(255, int(c * factor)) for c in base_col]

                # Knots / imperfections
                if rng.random() < knot_freq:
                    r, g, b = [int(c * 0.55) for c in groove_col]

                # Moss / lichen
                if moss_col and (math.sin(x * 0.25) * math.cos(y * 0.2) + rng.uniform(-0.2, 0.2)) > 0.45:
                    r, g, b = [int(r * 0.3 + c * 0.7) for c in moss_col]

                im.putpixel((x, y), (max(0, min(255, r)), max(0, min(255, g)), max(0, min(255, b)), 255))
        return im

    def generate_log_top(bark_col, wood_base, wood_ring, core_glow=None):
        im = Image.new("RGBA", (size, size))
        for y in range(size):
            for x in range(size):
                dx = x - 15.5
                dy = y - 15.5
                dist = math.hypot(dx, dy)
                ang = math.atan2(dy, dx)
                
                # Outer rough bark border
                wobble = math.sin(ang * 8.0) * 0.8 + rng.uniform(-0.4, 0.4)
                if dist + wobble > 14.2:
                    r, g, b = [int(c * rng.uniform(0.85, 1.15)) for c in bark_col]
                elif dist + wobble > 13.0: # cambium
                    r, g, b = [int(c * 1.1) for c in wood_base]
                else:
                    # Natural growth rings
                    ring_phase = dist * 1.25 + math.sin(ang * 4.0) * 0.4
                    is_ring = (math.sin(ring_phase * math.pi) > 0.3)
                    col = wood_ring if is_ring else wood_base
                    grain = rng.uniform(-0.08, 0.08)
                    r, g, b = [int(c * (1.0 + grain)) for c in col]
                    
                    if core_glow and dist < 4.0:
                        glow = 1.0 - (dist / 4.0)
                        r = int(r * (1.0 - glow) + core_glow[0] * glow)
                        g = int(g * (1.0 - glow) + core_glow[1] * glow)
                        b = int(b * (1.0 - glow) + core_glow[2] * glow)

                im.putpixel((x, y), (max(0, min(255, r)), max(0, min(255, g)), max(0, min(255, b)), 255))
        return im

    def generate_leaves(palette, fruit_type=None):
        # palette: [shadow, dark, mid, light, highlight]
        im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        
        # Grid of leaf clumps (2x2 to 3x3 organic leaf shapes)
        pixels = [[None for _ in range(size)] for _ in range(size)]
        
        # 1. Spawn leaf clusters
        num_clusters = 70
        for _ in range(num_clusters):
            cx = rng.randint(0, size - 1)
            cy = rng.randint(0, size - 1)
            radius = rng.randint(2, 4)
            cluster_shade = rng.randint(1, 4)
            for dy in range(-radius, radius + 1):
                for dx in range(-radius, radius + 1):
                    if dx*dx + dy*dy <= radius * radius:
                        px = (cx + dx) % size
                        py = (cy + dy) % size
                        # Leaves have internal highlight / shadow
                        shade = cluster_shade
                        if dy > 0:
                            shade = max(0, shade - 1) # bottom in shadow
                        elif dy < 0 and dx <= 0:
                            shade = min(4, shade + 1) # top catch light
                        pixels[py][px] = shade

        # 2. Render leaves with natural cutouts
        for y in range(size):
            for x in range(size):
                shade = pixels[y][x]
                if shade is None:
                    # 30% chance of small stray leaf, 70% air
                    if rng.random() < 0.28:
                        shade = rng.randint(0, 2)
                    else:
                        continue
                
                col = palette[shade]
                grain = rng.randint(-6, 6)
                r = max(0, min(255, col[0] + grain))
                g = max(0, min(255, col[1] + grain))
                b = max(0, min(255, col[2] + grain))
                im.putpixel((x, y), (r, g, b, 255))

        # 3. Add Fruits if requested
        if fruit_type == "sunsettia":
            # 3 distinct ripe Sunsettias hanging from the leaves
            for fx, fy in [(8, 14), (22, 11), (15, 23)]:
                for dy in range(-3, 4):
                    w = int(math.sqrt(max(0, 1.0 - (dy / 3.5) ** 2)) * 3.2)
                    for dx in range(-w, w + 1):
                        px = (fx + dx) % size
                        py = (fy + dy) % size
                        norm_y = (dy + 3) / 6.0
                        if norm_y < 0.35:
                            fr, fg, fb = 255, 225, 65
                        elif norm_y < 0.7:
                            fr, fg, fb = 255, 135, 30
                        else:
                            fr, fg, fb = 220, 45, 55
                        if abs(dx) + abs(dy) < 2:
                            fr, fg, fb = min(255, fr + 35), min(255, fg + 30), fb
                        im.putpixel((px, py), (fr, fg, fb, 255))
                # stem
                im.putpixel((fx, (fy - 4) % size), (70, 160, 45, 255))
        elif fruit_type == "mana":
            # 3 glowing azure/cyan Mana Fruits
            for fx, fy in [(9, 13), (23, 12), (16, 22)]:
                for dy in range(-3, 4):
                    w = int(math.sqrt(max(0, 1.0 - (dy / 3.5) ** 2)) * 3.2)
                    for dx in range(-w, w + 1):
                        px = (fx + dx) % size
                        py = (fy + dy) % size
                        dist = math.hypot(dx, dy)
                        fr = int(50 + 60 * (1.0 - dist / 3.5))
                        fg = int(180 + 75 * (1.0 - dist / 3.5))
                        fb = 255
                        if dist < 1.5:
                            fr, fg, fb = 220, 255, 255
                        im.putpixel((px, py), (fr, fg, fb, 255))
                im.putpixel((fx, (fy - 4) % size), (140, 240, 255, 255))

        return im

    # 1. CUIHUA (Orchard Tree)
    cui_bark = generate_bark(
        base_col=(105, 95, 80),
        groove_col=(65, 55, 45),
        highlight_col=(135, 125, 105),
        moss_col=None
    )
    cui_bark.save(f"{target_dir}/cuihua_log.png")

    cui_top = generate_log_top(
        bark_col=(75, 65, 55),
        wood_base=(165, 145, 115),
        wood_ring=(135, 115, 88)
    )
    cui_top.save(f"{target_dir}/cuihua_log_top.png")

    # Apple-green foliage palette
    cui_palette = [
        (28, 75, 20),   # shadow
        (42, 108, 30),  # dark
        (65, 145, 40),  # mid
        (92, 185, 55),  # light
        (125, 215, 75)  # highlight
    ]
    generate_leaves(cui_palette, None).save(f"{target_dir}/cuihua_leaves.png")
    generate_leaves(cui_palette, "sunsettia").save(f"{target_dir}/cuihua_leaves_fruited.png")

    # 2. ADHIGAMA (Sumeru Rainforest)
    adhi_bark = generate_bark(
        base_col=(78, 62, 48),
        groove_col=(45, 34, 25),
        highlight_col=(105, 86, 68),
        moss_col=(50, 145, 55),
        knot_freq=0.04
    )
    adhi_bark.save(f"{target_dir}/adhigama_log.png")

    adhi_top = generate_log_top(
        bark_col=(55, 42, 32),
        wood_base=(135, 105, 75),
        wood_ring=(105, 80, 55)
    )
    adhi_top.save(f"{target_dir}/adhigama_log_top.png")

    # Deep tropical jungle palette
    adhi_palette = [
        (18, 55, 28),
        (26, 82, 40),
        (38, 118, 52),
        (58, 155, 72),
        (85, 192, 98)
    ]
    generate_leaves(adhi_palette, None).save(f"{target_dir}/adhigama_leaves.png")

    # 3. SANDBEARER (Liyue Golden Pagoda)
    sand_bark = generate_bark(
        base_col=(130, 85, 48),
        groove_col=(80, 50, 28),
        highlight_col=(165, 115, 68),
        moss_col=None
    )
    sand_bark.save(f"{target_dir}/sandbearer_log.png")

    sand_top = generate_log_top(
        bark_col=(90, 58, 35),
        wood_base=(185, 135, 85),
        wood_ring=(150, 105, 65)
    )
    sand_top.save(f"{target_dir}/sandbearer_log_top.png")

    # Glowing golden autumn foliage palette
    sand_palette = [
        (120, 65, 12),
        (175, 100, 18),
        (215, 145, 25),
        (245, 185, 45),
        (255, 218, 80)
    ]
    generate_leaves(sand_palette, None).save(f"{target_dir}/sandbearer_leaves.png")

    # 4. MANA WOOD (Celestial Mystical)
    mana_bark = generate_bark(
        base_col=(55, 45, 85),
        groove_col=(32, 25, 55),
        highlight_col=(80, 70, 125),
        moss_col=(40, 200, 230) # glowing cyan mana moss
    )
    mana_bark.save(f"{target_dir}/mana_wood_log.png")

    mana_top = generate_log_top(
        bark_col=(40, 30, 65),
        wood_base=(95, 80, 140),
        wood_ring=(70, 58, 110),
        core_glow=(80, 240, 255)
    )
    mana_top.save(f"{target_dir}/mana_wood_log_top.png")

    # Ethereal cyan-violet celestial foliage palette
    mana_palette = [
        (20, 45, 95),
        (35, 78, 145),
        (55, 125, 200),
        (85, 175, 240),
        (140, 225, 255)
    ]
    generate_leaves(mana_palette, None).save(f"{target_dir}/mana_wood_leaves.png")
    generate_leaves(mana_palette, "mana").save(f"{target_dir}/mana_wood_leaves_fruited.png")

    # 5. ELDORIA (Ancient Fantasy)
    eld_bark = generate_bark(
        base_col=(160, 155, 170),
        groove_col=(105, 100, 115),
        highlight_col=(205, 200, 220),
        moss_col=None
    )
    eld_bark.save(f"{target_dir}/eldoria_log.png")

    eld_top = generate_log_top(
        bark_col=(120, 115, 130),
        wood_base=(195, 190, 205),
        wood_ring=(165, 160, 175)
    )
    eld_top.save(f"{target_dir}/eldoria_log_top.png")

    # Whispering pastel lilac foliage palette
    eld_palette = [
        (85, 75, 115),
        (120, 105, 150),
        (160, 145, 195),
        (195, 180, 225),
        (230, 218, 250)
    ]
    generate_leaves(eld_palette, None).save(f"{target_dir}/eldoria_leaves.png")

    print("All trees regenerated with authentic organic Minecraft pixel art!")

    # Update showcase image
    tree_samples = [
        ("cuihua_log", "cuihua_leaves_fruited"),
        ("sandbearer_log", "sandbearer_leaves"),
        ("adhigama_log", "adhigama_leaves"),
        ("mana_wood_log", "mana_wood_leaves_fruited"),
        ("eldoria_log", "eldoria_leaves")
    ]
    show_w = len(tree_samples) * (32 * 2 + 10) + 10
    show_h = 32 + 20
    comp = Image.new("RGBA", (show_w, show_h), (20, 20, 24, 255))
    cur_x = 10
    for log_n, leaf_n in tree_samples:
        l_im = Image.open(f"{target_dir}/{log_n}.png")
        lf_im = Image.open(f"{target_dir}/{leaf_n}.png")
        comp.paste(l_im, (cur_x, 10), l_im)
        comp.paste(lf_im, (cur_x + 32, 10), lf_im)
        cur_x += 74
    comp_scaled = comp.resize((comp.width * 2, comp.height * 2), Image.NEAREST)
    comp_scaled.save("C:/Users/soybe/.gemini/antigravity/brain/930e4101-5310-48ee-9b53-1a9777f45603/trees_showcase.png")
    print("Updated tree showcase saved!")

if __name__ == "__main__":
    generate_improved_tree_textures()
