import numpy as np
from PIL import Image
from collections import deque

def isolate_columbina():
    im = Image.open('src/main/resources/assets/awakening_system/textures/gui/welkin/welkin_columbina.png')
    arr = np.array(im)
    h, w, c = arr.shape

    # Columbina + chalice is located roughly within:
    # x in [110, 415], y in [105, 470]
    # Everything outside this box is definitely background!
    
    # Let's inspect the boundary between Columbina and the background
    # Let's create an initial background mask
    bg_mask = np.zeros((h, w), dtype=bool)

    # 1. Pixels outside the bounding box [100..425, 95..475] are definitely background
    bg_mask[:95, :] = True
    bg_mask[475:, :] = True
    bg_mask[:, :100] = True
    bg_mask[:, 425:] = True

    # 2. Existing transparent pixels (alpha < 10) are background
    bg_mask[arr[:, :, 3] < 10] = True

    # 3. Outer circular border pixels:
    # Any pixel at distance > 240 from center (255, 255) is background
    y_coords, x_coords = np.ogrid[:h, :w]
    dist_from_center = np.sqrt((x_coords - 255)**2 + (y_coords - 255)**2)
    bg_mask[dist_from_center > 240] = True

    # 4. In the regions around Columbina:
    # - Above the chalice wings:
    #   Left of chalice: x < 115
    #   Right of chalice: x > 410
    #   Under chalice: y > 470
    #   Above head wings: y < 110
    
    # Save intermediate background mask for inspection
    mask_im = Image.fromarray((~bg_mask * 255).astype(np.uint8))
    mask_im.save('C:/Users/soybe/.gemini/antigravity/brain/930e4101-5310-48ee-9b53-1a9777f45603/mask_test1.png')
    print("Initial mask saved")

if __name__ == '__main__':
    isolate_columbina()
