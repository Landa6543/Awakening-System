from PIL import Image, ImageDraw
import os

target_dir = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\assets\awakening_system\textures\item"
os.makedirs(target_dir, exist_ok=True)

def create_pill(center_color, inner_color, ring_color, glow_color, aura_sparkles=None):
    size = 32
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    cx, cy = 16, 16
    
    # Outer aura / glow
    for r in range(12, 6, -1):
        alpha = int(80 * (1.0 - (r - 7) / 6.0))
        glow = (glow_color[0], glow_color[1], glow_color[2], alpha)
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=glow)
        
    # Dark border
    draw.ellipse([cx - 7, cy - 7, cx + 7, cy + 7], fill=(20, 20, 25, 255))
    
    # Outer ring
    draw.ellipse([cx - 6, cy - 6, cx + 6, cy + 6], fill=ring_color)
    
    # Inner body
    draw.ellipse([cx - 5, cy - 5, cx + 5, cy + 5], fill=inner_color)
    
    # Center core
    draw.ellipse([cx - 3, cy - 3, cx + 3, cy + 3], fill=center_color)
    
    # Highlight
    draw.ellipse([cx - 4, cy - 4, cx - 2, cy - 2], fill=(255, 255, 255, 230))
    draw.point((cx - 3, cy - 3), fill=(255, 255, 255, 255))
    
    # Sparkles / runes
    if aura_sparkles:
        for (sx, sy, col) in aura_sparkles:
            draw.point((sx, sy), fill=col)
            
    return img

pills = {
    # 1. Nine Revolutions Pill (Golden / Divine amber)
    "nine_revolutions_pill.png": {
        "center": (255, 240, 150, 255),
        "inner": (245, 180, 40, 255),
        "ring": (190, 120, 15, 255),
        "glow": (255, 215, 0),
        "sparkles": [(8, 8, (255, 255, 200, 255)), (24, 7, (255, 230, 100, 255)), 
                     (7, 23, (255, 200, 50, 255)), (25, 24, (255, 255, 255, 255))]
    },
    # 2. Crimson Phoenix Pill (Blazing ruby flame)
    "crimson_phoenix_pill.png": {
        "center": (255, 140, 80, 255),
        "inner": (230, 40, 40, 255),
        "ring": (140, 15, 25, 255),
        "glow": (255, 50, 30),
        "sparkles": [(9, 9, (255, 200, 50, 255)), (23, 8, (255, 80, 20, 255)), 
                     (6, 22, (255, 120, 40, 255)), (24, 23, (255, 220, 100, 255))]
    },
    # 3. White Tiger Pill (Silver with thunder azure sparks)
    "white_tiger_pill.png": {
        "center": (255, 255, 255, 255),
        "inner": (210, 225, 245, 255),
        "ring": (70, 120, 190, 255),
        "glow": (100, 180, 255),
        "sparkles": [(8, 10, (140, 220, 255, 255)), (23, 9, (80, 160, 255, 255)), 
                     (7, 24, (180, 240, 255, 255)), (25, 22, (255, 255, 255, 255))]
    },
    # 4. Heavenly Enlightenment Pill (Purple-Sapphire Dao mist)
    "heavenly_enlightenment_pill.png": {
        "center": (220, 180, 255, 255),
        "inner": (140, 80, 230, 255),
        "ring": (70, 30, 140, 255),
        "glow": (170, 100, 255),
        "sparkles": [(9, 7, (230, 180, 255, 255)), (24, 8, (200, 140, 255, 255)), 
                     (8, 23, (240, 200, 255, 255)), (24, 25, (255, 255, 255, 255))]
    },
    # 5. Foundation Building Pill (Earthy emerald jade)
    "foundation_building_pill.png": {
        "center": (170, 255, 180, 255),
        "inner": (50, 190, 90, 255),
        "ring": (20, 110, 50, 255),
        "glow": (40, 220, 100),
        "sparkles": [(8, 8, (180, 255, 200, 255)), (24, 9, (120, 240, 150, 255)), 
                     (7, 23, (160, 255, 170, 255)), (23, 24, (220, 255, 230, 255))]
    },
    # 6. Primordial Rebirth Pill (Prismatic rainbow cosmic crystal)
    "primordial_rebirth_pill.png": {
        "center": (255, 255, 255, 255),
        "inner": (255, 160, 240, 255),
        "ring": (120, 60, 220, 255),
        "glow": (240, 140, 255),
        "sparkles": [(7, 7, (255, 255, 150, 255)), (25, 7, (120, 240, 255, 255)), 
                     (6, 24, (255, 140, 220, 255)), (25, 24, (180, 255, 180, 255))]
    }
}

for filename, cfg in pills.items():
    img = create_pill(cfg["center"], cfg["inner"], cfg["ring"], cfg["glow"], cfg["sparkles"])
    path = os.path.join(target_dir, filename)
    img.save(path)
    print("Generated pill texture:", path)
