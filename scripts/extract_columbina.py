import numpy as np
from PIL import Image
from collections import deque
from scipy import ndimage

def extract():
    im = Image.open('src/main/resources/assets/awakening_system/textures/gui/welkin/welkin_columbina.png')
    arr = np.array(im, dtype=np.uint8)
    h, w, _ = arr.shape

    # 1. Background mask: True = background (to remove)
    bg = np.zeros((h, w), dtype=bool)

    # Everything outside bounding box
    bg[:90, :] = True
    bg[475:, :] = True
    bg[:, :110] = True
    bg[:, 418:] = True

    # Existing transparent
    bg[arr[:, :, 3] < 10] = True

    # Circular border (radius > 238 from 255, 255)
    y_coords, x_coords = np.ogrid[:h, :w]
    dist = np.sqrt((x_coords - 255)**2 + (y_coords - 255)**2)
    bg[dist > 238] = True

    # 2. Dark sky in the corners and sides
    # Dark sky: R in [20, 85], G in [20, 95], B in [50, 155]
    r, g, b = arr[:, :, 0], arr[:, :, 1], arr[:, :, 2]
    dark_sky = (r < 85) & (g < 95) & (b >= 45) & (b < 155)

    # Cyan crescent moon: B > 220, G > 185, R < 195
    crescent = (b > 215) & (g > 180) & (r < 195)

    # Pink sphere on top-right: R > 225, G in [170, 225], B in [215, 255], x > 330, y < 250
    pink_sphere = (r > 225) & (g >= 170) & (g <= 225) & (b >= 215) & (x_coords > 330) & (y_coords < 250)

    # White circular lines & stars outside Columbina's central silhouette
    stars_and_rings = (r > 190) & (g > 190) & (b > 200)

    # Start flood fill from outer edges through dark_sky, crescent, and pink_sphere
    # Only allow flood fill to travel through background candidates
    bg_candidate = bg | dark_sky | crescent | pink_sphere

    # Seed flood-fill from all image edges
    q = deque()
    visited = np.zeros((h, w), dtype=bool)

    # Seed top, bottom, left, right borders
    for x in range(w):
        for y in [0, 1, 2, h-3, h-2, h-1]:
            q.append((y, x))
            visited[y, x] = True
            bg[y, x] = True
    for y in range(h):
        for x in [0, 1, 2, w-3, w-2, w-1]:
            q.append((y, x))
            visited[y, x] = True
            bg[y, x] = True

    # Run BFS
    while q:
        cy, cx = q.popleft()
        for dy, dx in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            ny, nx = cy + dy, cx + dx
            if 0 <= ny < h and 0 <= nx < w and not visited[ny, nx]:
                # Can we expand into (ny, nx)?
                # Expand if it's already bg, or if it matches dark_sky or crescent or pink_sphere or border
                is_bg_pixel = False
                if bg[ny, nx]:
                    is_bg_pixel = True
                elif dark_sky[ny, nx]:
                    is_bg_pixel = True
                elif crescent[ny, nx]:
                    # Check that it's not a feather (feathers are inside x in 160..370 and have high R)
                    if r[ny, nx] < 195:
                        is_bg_pixel = True
                elif pink_sphere[ny, nx]:
                    is_bg_pixel = True
                elif dist[ny, nx] > 230:
                    is_bg_pixel = True
                # Star dots in the sky: small bright spots surrounded by sky
                elif stars_and_rings[ny, nx]:
                    # If it's outside Columbina's body (x < 165 or x > 365 or y < 115 or y > 465)
                    if nx < 165 or nx > 365 or ny < 115 or ny > 465:
                        is_bg_pixel = True
                    # Or between left/right wings at top (x in 235..295, y in 115..185)
                    elif 235 <= nx <= 295 and 115 <= ny <= 185 and (r[ny, nx] < 120 or (g[ny, nx] < 120 and b[ny, nx] < 160)):
                        is_bg_pixel = True

                if is_bg_pixel:
                    visited[ny, nx] = True
                    bg[ny, nx] = True
                    q.append((ny, nx))

    # Apply mask
    result = arr.copy()
    result[bg, 3] = 0

    # Let's save the result to brain to inspect with view_file!
    res_im = Image.fromarray(result)
    res_im.save('C:/Users/soybe/.gemini/antigravity/brain/930e4101-5310-48ee-9b53-1a9777f45603/columbina_isolated.png')
    print("Columbina isolated saved successfully!")

if __name__ == '__main__':
    extract()
