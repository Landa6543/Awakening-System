import os
from PIL import Image

def fix_item_doors():
    item_dir = "src/main/resources/assets/awakening_system/textures/item/"
    
    fixes = {
        "bamboo_door.png": (244, 238, 220, 255),
        "bamboo_door_item.png": (244, 238, 220, 255),
        "sandbearer_door.png": (248, 242, 228, 255),
        "sandbearer_door_item.png": (248, 242, 228, 255),
        "cuihua_door.png": (230, 225, 215, 255),
        "cuihua_door_item.png": (230, 225, 215, 255),
        "adhigama_door.png": (150, 115, 75, 255)
    }
    
    for fname, fill_col in fixes.items():
        path = os.path.join(item_dir, fname)
        if os.path.exists(path):
            im = Image.open(path).convert("RGBA")
            w, h = im.size
            for y in range(2, h - 2):
                for x in range(2, w - 2):
                    if im.getpixel((x, y))[3] < 200:
                        im.putpixel((x, y), fill_col)
            im.save(path)
            print(f"Fixed item door {fname}")

if __name__ == "__main__":
    fix_item_doors()
