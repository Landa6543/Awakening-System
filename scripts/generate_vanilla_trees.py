import zipfile
import io
import os
import numpy as np
from PIL import Image

def generate_vanilla_trees():
    jar_path = r'C:\Users\soybe\.gradle\caches\forge_gradle\minecraft_repo\versions\1.20.1\client-extra.jar'
    out_dir = r'src/main/resources/assets/awakening_system/textures/block'
    os.makedirs(out_dir, exist_ok=True)

    with zipfile.ZipFile(jar_path, 'r') as z:
        def load_img(name):
            data = z.read(name)
            return Image.open(io.BytesIO(data)).convert("RGBA")

        oak_leaves = load_img('assets/minecraft/textures/block/oak_leaves.png')
        jungle_leaves = load_img('assets/minecraft/textures/block/jungle_leaves.png')
        cherry_leaves = load_img('assets/minecraft/textures/block/cherry_leaves.png')
        
        oak_log = load_img('assets/minecraft/textures/block/oak_log.png')
        oak_log_top = load_img('assets/minecraft/textures/block/oak_log_top.png')
        jungle_log = load_img('assets/minecraft/textures/block/jungle_log.png')
        jungle_log_top = load_img('assets/minecraft/textures/block/jungle_log_top.png')
        dark_oak_log = load_img('assets/minecraft/textures/block/dark_oak_log.png')
        dark_oak_log_top = load_img('assets/minecraft/textures/block/dark_oak_log_top.png')
        birch_log = load_img('assets/minecraft/textures/block/birch_log.png')
        birch_log_top = load_img('assets/minecraft/textures/block/birch_log_top.png')
        spruce_log = load_img('assets/minecraft/textures/block/spruce_log.png')
        spruce_log_top = load_img('assets/minecraft/textures/block/spruce_log_top.png')

    # Color grading helper: maps normalized grayscale lightness to a color ramp
    def apply_color_ramp(base_img, ramp, preserve_holes=True):
        """
        ramp is a list of tuples: (position 0.0..1.0, (r, g, b)) sorted by position.
        """
        w, h = base_img.size
        out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        base_arr = np.array(base_img)
        
        for y in range(h):
            for x in range(w):
                r, g, b, a = base_arr[y, x]
                if preserve_holes and a == 0:
                    out.putpixel((x, y), (0, 0, 0, 0))
                    continue
                
                # Perceived brightness
                luma = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                
                # Interpolate in ramp
                if luma <= ramp[0][0]:
                    cr, cg, cb = ramp[0][1]
                elif luma >= ramp[-1][0]:
                    cr, cg, cb = ramp[-1][1]
                else:
                    for i in range(len(ramp) - 1):
                        p0, c0 = ramp[i]
                        p1, c1 = ramp[i + 1]
                        if p0 <= luma <= p1:
                            t = (luma - p0) / (p1 - p0)
                            cr = int(round(c0[0] + t * (c1[0] - c0[0])))
                            cg = int(round(c0[1] + t * (c1[1] - c0[1])))
                            cb = int(round(c0[2] + t * (c1[2] - c0[2])))
                            break
                
                out.putpixel((x, y), (cr, cg, cb, a if not preserve_holes or a > 0 else 255))
        return out

    # =========================================================================
    # 1. CUIHUA (Mondstadt Apple/Emerald Green + Sunsettia fruit + Cedar Bark)
    # =========================================================================
    cuihua_ramp = [
        (0.00, (22, 54, 14)),
        (0.25, (38, 86, 22)),
        (0.50, (58, 126, 32)),
        (0.75, (86, 172, 44)),
        (1.00, (124, 214, 68))
    ]
    cuihua_leaves_img = apply_color_ramp(oak_leaves, cuihua_ramp, preserve_holes=True)
    cuihua_leaves_img.save(os.path.join(out_dir, "cuihua_leaves.png"))

    # Cuihua Fruited: 3 cute hand-crafted pixel art Sunsettia fruits nestled in leaves
    cuihua_fruited_img = cuihua_leaves_img.copy()
    fruits = [
        # (x, y) anchor of fruit
        (4, 5),
        (11, 3),
        (8, 11)
    ]
    for fx, fy in fruits:
        # Sunsettia shape:
        #  . s .
        #  H O H
        #  O R O
        #  . R .
        fruit_pixels = [
            (fx, fy - 1, (42, 85, 24)),    # stem/calyx
            (fx - 1, fy, (255, 215, 60)),  # top highlight
            (fx, fy, (255, 170, 30)),      # bright orange
            (fx + 1, fy, (245, 140, 20)),  # orange
            (fx - 1, fy + 1, (240, 110, 25)),
            (fx, fy + 1, (220, 50, 45)),   # rosy red bottom
            (fx + 1, fy + 1, (200, 35, 40)),
            (fx, fy + 2, (180, 25, 35))    # bottom tip
        ]
        for px, py, col in fruit_pixels:
            if 0 <= px < 16 and 0 <= py < 16:
                cuihua_fruited_img.putpixel((px, py), (*col, 255))
    cuihua_fruited_img.save(os.path.join(out_dir, "cuihua_leaves_fruited.png"))

    # Cuihua Log: Natural warm cedar/oak bark
    cuihua_log_ramp = [
        (0.00, (46, 32, 20)),
        (0.30, (68, 48, 30)),
        (0.60, (96, 72, 48)),
        (0.85, (122, 94, 64)),
        (1.00, (142, 112, 78))
    ]
    cuihua_log_img = apply_color_ramp(oak_log, cuihua_log_ramp, preserve_holes=False)
    cuihua_log_img.save(os.path.join(out_dir, "cuihua_log.png"))

    cuihua_log_top_ramp = [
        (0.00, (50, 34, 22)),
        (0.35, (95, 70, 46)),
        (0.65, (148, 116, 78)),
        (1.00, (178, 144, 102))
    ]
    cuihua_log_top_img = apply_color_ramp(oak_log_top, cuihua_log_top_ramp, preserve_holes=False)
    cuihua_log_top_img.save(os.path.join(out_dir, "cuihua_log_top.png"))

    # =========================================================================
    # 2. ADHIGAMA (Sumeru Tropical Rainforest Emerald + Jungle Bark)
    # =========================================================================
    adhigama_ramp = [
        (0.00, (12, 42, 22)),
        (0.25, (20, 64, 34)),
        (0.50, (32, 98, 52)),
        (0.75, (48, 138, 74)),
        (1.00, (72, 174, 96))
    ]
    adhigama_leaves_img = apply_color_ramp(jungle_leaves, adhigama_ramp, preserve_holes=True)
    adhigama_leaves_img.save(os.path.join(out_dir, "adhigama_leaves.png"))

    adhigama_log_ramp = [
        (0.00, (36, 28, 20)),
        (0.30, (56, 44, 32)),
        (0.60, (82, 66, 48)),
        (0.85, (104, 86, 62)),
        (1.00, (122, 102, 76))
    ]
    adhigama_log_img = apply_color_ramp(jungle_log, adhigama_log_ramp, preserve_holes=False)
    # Add subtle tropical moss nuances along some bark grooves
    adh_arr = np.array(adhigama_log_img)
    for y in range(16):
        for x in range(16):
            if (x * 7 + y * 13) % 19 == 0:
                r, g, b, a = adh_arr[y, x]
                adhigama_log_img.putpixel((x, y), (max(0, r - 15), min(255, g + 18), max(0, b - 10), 255))
    adhigama_log_img.save(os.path.join(out_dir, "adhigama_log.png"))

    adhigama_log_top_ramp = [
        (0.00, (42, 32, 24)),
        (0.35, (86, 68, 48)),
        (0.65, (134, 108, 76)),
        (1.00, (162, 134, 98))
    ]
    adhigama_log_top_img = apply_color_ramp(jungle_log_top, adhigama_log_top_ramp, preserve_holes=False)
    adhigama_log_top_img.save(os.path.join(out_dir, "adhigama_log_top.png"))

    # =========================================================================
    # 3. SANDBEARER (Liyue Autumn Golden Ginkgo + Dark Amber Bark)
    # =========================================================================
    sandbearer_ramp = [
        (0.00, (135, 62, 12)),
        (0.25, (185, 100, 16)),
        (0.50, (228, 150, 22)),
        (0.75, (248, 196, 38)),
        (1.00, (255, 230, 95))
    ]
    sandbearer_leaves_img = apply_color_ramp(oak_leaves, sandbearer_ramp, preserve_holes=True)
    sandbearer_leaves_img.save(os.path.join(out_dir, "sandbearer_leaves.png"))

    sandbearer_log_ramp = [
        (0.00, (38, 24, 14)),
        (0.30, (64, 42, 24)),
        (0.60, (92, 62, 36)),
        (0.85, (118, 82, 48)),
        (1.00, (138, 98, 60))
    ]
    sandbearer_log_img = apply_color_ramp(dark_oak_log, sandbearer_log_ramp, preserve_holes=False)
    sandbearer_log_img.save(os.path.join(out_dir, "sandbearer_log.png"))

    sandbearer_log_top_ramp = [
        (0.00, (44, 28, 16)),
        (0.35, (100, 68, 38)),
        (0.65, (158, 114, 66)),
        (1.00, (188, 142, 88))
    ]
    sandbearer_log_top_img = apply_color_ramp(dark_oak_log_top, sandbearer_log_top_ramp, preserve_holes=False)
    sandbearer_log_top_img.save(os.path.join(out_dir, "sandbearer_log_top.png"))

    # =========================================================================
    # 4. MANA WOOD (Mystic Celestial Cyan-Indigo + Glowing Mana Fruit + Twilight Bark)
    # =========================================================================
    mana_ramp = [
        (0.00, (14, 14, 42)),
        (0.25, (22, 30, 72)),
        (0.50, (32, 68, 132)),
        (0.75, (42, 148, 212)),
        (1.00, (160, 235, 255))
    ]
    mana_leaves_img = apply_color_ramp(oak_leaves, mana_ramp, preserve_holes=True)
    mana_leaves_img.save(os.path.join(out_dir, "mana_wood_leaves.png"))

    # Mana Fruited: Glowing celestial blue mana fruits
    mana_fruited_img = mana_leaves_img.copy()
    mfruits = [(5, 4), (12, 10), (3, 11)]
    for fx, fy in mfruits:
        mfruit_pixels = [
            (fx, fy - 1, (30, 18, 60)),     # violet stem
            (fx - 1, fy, (0, 180, 245)),    # outer cyan
            (fx, fy, (200, 250, 255)),      # brilliant glowing core
            (fx + 1, fy, (0, 180, 245)),
            (fx, fy + 1, (0, 110, 210))     # deep blue bottom
        ]
        for px, py, col in mfruit_pixels:
            if 0 <= px < 16 and 0 <= py < 16:
                mana_fruited_img.putpixel((px, py), (*col, 255))
    mana_fruited_img.save(os.path.join(out_dir, "mana_wood_leaves_fruited.png"))

    # Mana Log: Twilight mystic purple-blue bark
    mana_log_ramp = [
        (0.00, (20, 18, 38)),
        (0.30, (36, 32, 62)),
        (0.60, (54, 48, 88)),
        (0.85, (74, 68, 116)),
        (1.00, (96, 90, 142))
    ]
    mana_log_img = apply_color_ramp(dark_oak_log, mana_log_ramp, preserve_holes=False)
    mana_log_img.save(os.path.join(out_dir, "mana_wood_log.png"))

    mana_log_top_ramp = [
        (0.00, (28, 24, 48)),
        (0.35, (62, 58, 98)),
        (0.65, (108, 102, 154)),
        (1.00, (142, 136, 192))
    ]
    mana_log_top_img = apply_color_ramp(dark_oak_log_top, mana_log_top_ramp, preserve_holes=False)
    mana_log_top_img.save(os.path.join(out_dir, "mana_wood_log_top.png"))

    # =========================================================================
    # 5. ELDORIA (Astral/Elven Ethereal Lilac + Sacred Silver Bark)
    # =========================================================================
    eldoria_ramp = [
        (0.00, (58, 46, 78)),
        (0.25, (95, 78, 122)),
        (0.50, (138, 116, 168)),
        (0.75, (186, 166, 214)),
        (1.00, (235, 222, 248))
    ]
    eldoria_leaves_img = apply_color_ramp(cherry_leaves, eldoria_ramp, preserve_holes=True)
    eldoria_leaves_img.save(os.path.join(out_dir, "eldoria_leaves.png"))

    # Eldoria Log: Sacred ancient silver-birch bark
    eldoria_log_ramp = [
        (0.00, (75, 70, 85)),
        (0.30, (125, 120, 135)),
        (0.60, (175, 172, 184)),
        (0.85, (215, 212, 222)),
        (1.00, (244, 242, 248))
    ]
    eldoria_log_img = apply_color_ramp(birch_log, eldoria_log_ramp, preserve_holes=False)
    eldoria_log_img.save(os.path.join(out_dir, "eldoria_log.png"))

    eldoria_log_top_ramp = [
        (0.00, (82, 76, 92)),
        (0.35, (140, 132, 152)),
        (0.65, (190, 182, 202)),
        (1.00, (226, 220, 236))
    ]
    eldoria_log_top_img = apply_color_ramp(birch_log_top, eldoria_log_top_ramp, preserve_holes=False)
    eldoria_log_top_img.save(os.path.join(out_dir, "eldoria_log_top.png"))

    print("Successfully generated all 17 authentic vanilla-style tree textures!")

if __name__ == "__main__":
    generate_vanilla_trees()
