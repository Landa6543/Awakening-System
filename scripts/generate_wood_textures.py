import os
from PIL import Image

def generate_plank_texture(base_col, hi_col, shadow_col, seam_col):
    """Generates standard 16x16 Minecraft vanilla plank texture (4 horizontal boards)"""
    img = Image.new("RGBA", (16, 16), base_col)
    
    # 4 horizontal boards: each 4 pixels high (y=0..3, 4..7, 8..11, 12..15)
    for board in range(4):
        y_start = board * 4
        # Seam at y_start
        for x in range(16):
            img.putpixel((x, y_start), seam_col)
            # Board top highlight at y_start + 1
            img.putpixel((x, y_start + 1), hi_col if (x * 3 + board) % 4 != 0 else base_col)
            # Board mid at y_start + 2
            img.putpixel((x, y_start + 2), base_col if (x * 7 + board) % 3 != 0 else hi_col)
            # Board bottom shadow at y_start + 3
            img.putpixel((x, y_start + 3), shadow_col if (x * 5 + board) % 4 != 0 else base_col)
            
        # Vertical board seams
        if board % 2 == 0:
            split_x = 7
        else:
            split_x = 12
        for dy in range(1, 4):
            img.putpixel((split_x, y_start + dy), seam_col)
            img.putpixel(((split_x + 1) % 16, y_start + dy), hi_col)
            
    return img

def generate_door_textures(plank_img, trim_col, dark_trim, is_plum=True):
    """Generates door top and bottom textures (16x16 each)"""
    top = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    bottom = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    
    # Copy plank base
    for y in range(16):
        for x in range(16):
            top.putpixel((x, y), plank_img.getpixel((x, y)))
            bottom.putpixel((x, y), plank_img.getpixel((x, y)))
            
    # Draw stiles and rails (door frame)
    for y in range(16):
        # Left & right stiles
        for x in [0, 1, 14, 15]:
            c = trim_col if x in [1, 14] else dark_trim
            top.putpixel((x, y), c)
            bottom.putpixel((x, y), c)
            
    # Door top arch / window
    # Shoji paper panel with lattice for plum, or carved arched window for pine
    shoji = (248, 240, 230, 255) if is_plum else (210, 185, 145, 255)
    for y in range(3, 13):
        for x in range(4, 12):
            if x in [4, 11] or y in [3, 12]:
                top.putpixel((x, y), dark_trim)
            elif x == 7 or y == 7:
                top.putpixel((x, y), trim_col)
            else:
                top.putpixel((x, y), shoji)
                
    # Door handle at bottom (x=13, y=3..5)
    handle_col = (235, 195, 60, 255) # brass/gold
    handle_dark = (110, 80, 20, 255)
    bottom.putpixel((13, 3), handle_dark)
    bottom.putpixel((13, 4), handle_col)
    bottom.putpixel((13, 5), handle_col)
    bottom.putpixel((12, 4), handle_col)
    
    # Lower panel border on bottom
    for y in range(4, 13):
        for x in range(4, 12):
            if x in [4, 11] or y in [4, 12]:
                bottom.putpixel((x, y), dark_trim)
                
    return top, bottom

def generate_trapdoor_texture(plank_img, trim_col, dark_trim, is_plum=True):
    """Generates 16x16 trapdoor with frame and 4 panels"""
    trap = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            # Outer frame
            if x in [0, 1, 14, 15] or y in [0, 1, 14, 15]:
                trap.putpixel((x, y), trim_col if (x in [1, 14] or y in [1, 14]) else dark_trim)
            # Center cross bars
            elif x in [7, 8] or y in [7, 8]:
                trap.putpixel((x, y), trim_col if x == 7 or y == 7 else dark_trim)
            # 4 inner recessed panels
            else:
                trap.putpixel((x, y), plank_img.getpixel((x, y)))
    return trap

def generate_door_item(top_img, bottom_img):
    """Generates 16x16 handheld door item icon from top and bottom"""
    item = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    t_scaled = top_img.resize((10, 8), Image.Resampling.NEAREST)
    b_scaled = bottom_img.resize((10, 8), Image.Resampling.NEAREST)
    item.paste(t_scaled, (3, 0), t_scaled)
    item.paste(b_scaled, (3, 8), b_scaled)
    return item

def main():
    block_dir = "src/main/resources/assets/awakening_system/textures/block/"
    item_dir = "src/main/resources/assets/awakening_system/textures/item/"
    
    # 1. SPIRIT PLUM PLANKS
    plum_base = (196, 146, 158, 255)
    plum_hi = (218, 172, 184, 255)
    plum_shadow = (170, 122, 134, 255)
    plum_seam = (118, 76, 88, 255)
    
    plum_planks = generate_plank_texture(plum_base, plum_hi, plum_shadow, plum_seam)
    plum_planks.save(os.path.join(block_dir, "spirit_plum_planks.png"))
    
    plum_top, plum_bot = generate_door_textures(plum_planks, plum_hi, plum_seam, is_plum=True)
    plum_top.save(os.path.join(block_dir, "spirit_plum_door_top.png"))
    plum_bot.save(os.path.join(block_dir, "spirit_plum_door_bottom.png"))
    
    plum_trap = generate_trapdoor_texture(plum_planks, plum_hi, plum_seam, is_plum=True)
    plum_trap.save(os.path.join(block_dir, "spirit_plum_trapdoor.png"))
    
    plum_door_item = generate_door_item(plum_top, plum_bot)
    plum_door_item.save(os.path.join(item_dir, "spirit_plum_door.png"))

    # 2. CELESTIAL PINE PLANKS
    pine_base = (182, 138, 92, 255)
    pine_hi = (208, 166, 118, 255)
    pine_shadow = (156, 112, 68, 255)
    pine_seam = (104, 72, 40, 255)
    
    pine_planks = generate_plank_texture(pine_base, pine_hi, pine_shadow, pine_seam)
    pine_planks.save(os.path.join(block_dir, "celestial_pine_planks.png"))
    
    pine_top, pine_bot = generate_door_textures(pine_planks, pine_hi, pine_seam, is_plum=False)
    pine_top.save(os.path.join(block_dir, "celestial_pine_door_top.png"))
    pine_bot.save(os.path.join(block_dir, "celestial_pine_door_bottom.png"))
    
    pine_trap = generate_trapdoor_texture(pine_planks, pine_hi, pine_seam, is_plum=False)
    pine_trap.save(os.path.join(block_dir, "celestial_pine_trapdoor.png"))
    
    pine_door_item = generate_door_item(pine_top, pine_bot)
    pine_door_item.save(os.path.join(item_dir, "celestial_pine_door.png"))

    print("Successfully generated all Plum and Pine wood textures!")

if __name__ == "__main__":
    main()
