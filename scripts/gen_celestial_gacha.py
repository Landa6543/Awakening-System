import os
import json
from PIL import Image, ImageDraw

TEX_BLOCK_DIR = "src/main/resources/assets/awakening_system/textures/block"
MODELS_BLOCK_DIR = "src/main/resources/assets/awakening_system/models/block"

os.makedirs(TEX_BLOCK_DIR, exist_ok=True)
os.makedirs(MODELS_BLOCK_DIR, exist_ok=True)

def create_astrolabe_gold():
    # 32x32 radiant Liyue/Celestia gold texture with filigree, rivets and runes
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    # Base metallic golden gradient
    for y in range(32):
        for x in range(32):
            val = (x * 3 + y * 5) % 8
            if val < 2:
                c = (255, 225, 110, 255) # Highlight gold
            elif val < 5:
                c = (235, 190, 60, 255)  # Pure gold
            elif val < 7:
                c = (195, 145, 30, 255)  # Deep antique gold
            else:
                c = (150, 105, 18, 255)  # Shadow gold bronze
            img.putpixel((x, y), c)
            
    # Ornate border engraving
    d.rectangle([1, 1, 30, 30], outline=(255, 240, 160, 255))
    d.rectangle([2, 2, 29, 29], outline=(160, 110, 20, 255))
    
    # Corner rivets / gems
    for cx, cy in [(3, 3), (28, 3), (3, 28), (28, 28)]:
        img.putpixel((cx, cy), (255, 255, 240, 255))
        d.rectangle([cx-1, cy-1, cx+1, cy+1], outline=(140, 95, 15, 255))
        
    # Central celestial sun/star rune
    d.line([(16, 8), (16, 23)], fill=(255, 250, 200, 255), width=1)
    d.line([(8, 16), (23, 16)], fill=(255, 250, 200, 255), width=1)
    d.line([(11, 11), (21, 21)], fill=(255, 230, 130, 255), width=1)
    d.line([(11, 21), (21, 11)], fill=(255, 230, 130, 255), width=1)
    img.putpixel((16, 16), (255, 255, 255, 255))
    
    path = os.path.join(TEX_BLOCK_DIR, "celestial_astrolabe_gold.png")
    img.save(path)
    print("Saved", path)

def create_primogem_star_texture():
    # 32x32 iconic Primogem cosmic gradient: deep indigo -> cyan -> bright star white
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    for y in range(32):
        for x in range(32):
            dx = abs(x - 15.5) / 16.0
            dy = abs(y - 15.5) / 16.0
            dist = (dx**2 + dy**2)**0.5
            
            # 4-pointed star envelope formula: |x| + |y| <= 1
            star_dist = dx*0.7 + dy*1.3
            star_dist_h = dx*1.3 + dy*0.7
            star_metric = min(star_dist, star_dist_h)
            
            if star_metric < 0.2:
                c = (255, 255, 255, 255) # Pure core brilliance
            elif star_metric < 0.4:
                c = (190, 245, 255, 255) # Cyan-white
            elif star_metric < 0.6:
                c = (95, 195, 245, 255)  # Sky blue
            elif star_metric < 0.8:
                c = (145, 110, 235, 255) # Twilight violet
            elif star_metric < 1.0:
                c = (60, 45, 140, 255)   # Deep indigo space
            else:
                c = (35, 25, 80, 255)    # Cosmic border
            img.putpixel((x, y), c)
            
    # Diamond facet lines
    d.line([(16, 2), (30, 16)], fill=(220, 255, 255, 220))
    d.line([(30, 16), (16, 30)], fill=(120, 80, 210, 220))
    d.line([(16, 30), (2, 16)], fill=(100, 70, 190, 220))
    d.line([(2, 16), (16, 2)], fill=(220, 255, 255, 220))
    
    # Internal cross facet
    d.line([(16, 4), (16, 28)], fill=(255, 255, 255, 240))
    d.line([(4, 16), (28, 16)], fill=(255, 255, 255, 240))
    
    path = os.path.join(TEX_BLOCK_DIR, "celestial_primogem_star.png")
    img.save(path)
    print("Saved", path)

def create_fate_intertwined_texture():
    # 16x16 Intertwined Fate (pink and sky-blue celestial ribbon swirl)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            dx = abs(x - 7.5) / 8.0
            dy = abs(y - 7.5) / 8.0
            d = (dx*dx + dy*dy)**0.5
            if d <= 1.0:
                if (x + y) % 2 == 0:
                    c = (255, 170, 205, 255) # Radiant Pink
                else:
                    c = (130, 225, 255, 255) # Radiant Sky Blue
                if d > 0.8:
                    c = (255, 215, 80, 255)  # Gold trim rim
                elif d < 0.3:
                    c = (255, 255, 255, 255) # White starlight
                img.putpixel((x, y), c)
    path = os.path.join(TEX_BLOCK_DIR, "wishing_orb_intertwined.png")
    img.save(path)
    print("Saved", path)

def create_fate_acquaint_texture():
    # 16x16 Acquaint Fate (deep cosmic blue and violet swirl with gold rim)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            dx = abs(x - 7.5) / 8.0
            dy = abs(y - 7.5) / 8.0
            d = (dx*dx + dy*dy)**0.5
            if d <= 1.0:
                if (x - y) % 2 == 0:
                    c = (70, 110, 245, 255)  # Royal Deep Blue
                else:
                    c = (175, 110, 245, 255) # Twilight Violet
                if d > 0.8:
                    c = (255, 215, 80, 255)  # Gold trim rim
                elif d < 0.3:
                    c = (230, 245, 255, 255) # White starlight
                img.putpixel((x, y), c)
    path = os.path.join(TEX_BLOCK_DIR, "wishing_orb_acquaint.png")
    img.save(path)
    print("Saved", path)

create_astrolabe_gold()
create_primogem_star_texture()
create_fate_intertwined_texture()
create_fate_acquaint_texture()
