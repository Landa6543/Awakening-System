from PIL import Image, ImageDraw, ImageFont
import os

dest_dir = "src/main/resources/assets/awakening_system/textures/item"
os.makedirs(dest_dir, exist_ok=True)

# Denominations and colors
cards = [
    (5, (40, 180, 90), (20, 80, 40), (220, 255, 220)),     # Green ($5)
    (15, (60, 140, 240), (20, 50, 120), (220, 240, 255)),  # Blue ($15)
    (30, (170, 70, 240), (70, 20, 110), (245, 220, 255)),  # Purple ($30)
    (50, (240, 150, 40), (120, 60, 10), (255, 240, 210)),  # Gold/Amber ($50)
    (100, (240, 50, 80), (120, 15, 30), (255, 220, 230))    # Crimson / Diamond ($100)
]

for val, bg_bright, bg_dark, fg_color in cards:
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    
    # Outer card shape (rotated credit/gift card aspect)
    # Card rectangle from (2, 6) to (29, 25)
    # Outer border
    draw.rounded_rectangle([(2, 6), (29, 25)], radius=3, fill=(15, 15, 20, 240), outline=(220, 200, 140, 255))
    # Inner body gradient
    draw.rounded_rectangle([(4, 8), (27, 23)], radius=2, fill=bg_dark)
    for y in range(8, 24):
        alpha = int(255 * (y - 8) / 16)
        r = int(bg_bright[0] * (16 - (y-8))/16 + bg_dark[0] * (y-8)/16)
        g = int(bg_bright[1] * (16 - (y-8))/16 + bg_dark[1] * (y-8)/16)
        b = int(bg_bright[2] * (16 - (y-8))/16 + bg_dark[2] * (y-8)/16)
        draw.line([(5, y), (26, y)], fill=(r, g, b, 255))
    
    # Holographic gold chip on left (7, 12) to (11, 16)
    draw.rectangle([(6, 12), (10, 15)], fill=(255, 215, 0, 255), outline=(180, 140, 0, 255))
    draw.line([(8, 12), (8, 15)], fill=(180, 140, 0, 255))
    
    # Star / Crest on top right
    draw.point((23, 10), fill=(255, 255, 255, 255))
    draw.point((22, 11), fill=(255, 240, 180, 255))
    draw.point((24, 11), fill=(255, 240, 180, 255))
    draw.point((23, 12), fill=(255, 255, 255, 255))
    
    # Dollar symbol & number
    # Simple pixel rendering of '$' and value
    # Draw '$' at (14, 12)
    draw.line([(14, 11), (14, 16)], fill=fg_color)
    draw.line([(13, 12), (15, 12)], fill=fg_color)
    draw.line([(13, 14), (15, 14)], fill=fg_color)
    
    # Value text
    val_str = str(val)
    if val == 5:
        # '5' at x=17, y=11
        draw.line([(17, 11), (20, 11)], fill=fg_color)
        draw.point((17, 12), fill=fg_color)
        draw.line([(17, 13), (20, 13)], fill=fg_color)
        draw.point((20, 14), fill=fg_color)
        draw.line([(17, 15), (20, 15)], fill=fg_color)
    elif val == 15:
        # '1'
        draw.line([(16, 11), (16, 15)], fill=fg_color)
        # '5'
        draw.line([(18, 11), (21, 11)], fill=fg_color)
        draw.point((18, 12), fill=fg_color)
        draw.line([(18, 13), (21, 13)], fill=fg_color)
        draw.point((21, 14), fill=fg_color)
        draw.line([(18, 15), (21, 15)], fill=fg_color)
    elif val == 30:
        # '3'
        draw.line([(16, 11), (19, 11)], fill=fg_color)
        draw.line([(16, 13), (19, 13)], fill=fg_color)
        draw.line([(16, 15), (19, 15)], fill=fg_color)
        draw.point((19, 12), fill=fg_color)
        draw.point((19, 14), fill=fg_color)
        # '0'
        draw.rectangle([(21, 11), (24, 15)], outline=fg_color)
    elif val == 50:
        # '5'
        draw.line([(16, 11), (19, 11)], fill=fg_color)
        draw.point((16, 12), fill=fg_color)
        draw.line([(16, 13), (19, 13)], fill=fg_color)
        draw.point((19, 14), fill=fg_color)
        draw.line([(16, 15), (19, 15)], fill=fg_color)
        # '0'
        draw.rectangle([(21, 11), (24, 15)], outline=fg_color)
    elif val == 100:
        # '1'
        draw.line([(15, 11), (15, 15)], fill=fg_color)
        # '0'
        draw.rectangle([(17, 11), (19, 15)], outline=fg_color)
        # '0'
        draw.rectangle([(21, 11), (23, 15)], outline=fg_color)
    
    # Magnetic stripe on back / bottom glow
    draw.line([(4, 21), (27, 21)], fill=(255, 255, 255, 60))
    
    out_file = os.path.join(dest_dir, f"gift_card_{val}.png")
    im.save(out_file)
    print(f"Generated {out_file}")

# Also generate item models in models/item/
models_dir = "src/main/resources/assets/awakening_system/models/item"
os.makedirs(models_dir, exist_ok=True)
for val, _, _, _ in cards:
    model_json = f"""{{
  "parent": "item/generated",
  "textures": {{
    "layer0": "awakening_system:item/gift_card_{val}"
  }}
}}"""
    with open(os.path.join(models_dir, f"gift_card_{val}.json"), "w") as f:
        f.write(model_json)
print("All gift card textures and models generated successfully!")
