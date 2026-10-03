import numpy as np
from PIL import Image

def generate_perfect_columbina():
    p = 'C:/Users/soybe/.gemini/antigravity/brain/930e4101-5310-48ee-9b53-1a9777f45603/.user_uploaded/media_1790385990980.jpg'
    im = Image.open(p)
    arr = np.array(im, dtype=np.uint8)
    h, w, _ = arr.shape

    # Columbina's center of symmetry is x = 544.
    # Let's inspect the bounding box of Columbina + chalice in 1024x992:
    # Top of wings: y = 200
    # Bottom of lily pendant: y = 936
    # Left chalice handle: x = 240
    # Right chalice handle: x = 848
    # Center = (240 + 848) / 2 = 544.0! Perfectly symmetrical!

    # Create RGBA array
    rgba = np.zeros((h, w, 4), dtype=np.uint8)
    rgba[:, :, :3] = arr

    # Start with all pixels inside Columbina bounding box as potential candidates
    # x in [230, 858], y in [180, 940]
    in_box = np.zeros((h, w), dtype=bool)
    in_box[180:940, 230:858] = True

    r = arr[:, :, 0].astype(int)
    g = arr[:, :, 1].astype(int)
    b = arr[:, :, 2].astype(int)

    # Background detection:
    # 1. Dark night sky (cold blue/indigo: R < G or B > R + 20, low brightness):
    # Notice: Columbina's hair has R > G + 15!
    # Dark sky has R <= G or B > R + 25.
    dark_sky = in_box & (
        ((r <= g) & (b > 40)) |
        ((b > r + 20) & (r < 110) & (g < 120))
    )

    # 2. Cyan crescent moon behind her:
    # Bright cyan has G > 190 and B > 220 and R < 190 (cold cyan/blue)
    crescent_moon = in_box & (b > 210) & (g > 180) & (r < 190)

    # 3. Pink sphere on the top-right background:
    # It is located at x > 680, y in [240, 480], colors: R > 220, G in [170, 225], B in [210, 255]
    pink_sphere = in_box & (x_coords := np.ogrid[:h, :w][1] > 680) & (y_coords := np.ogrid[:h, :w][0] < 500) & (r > 215) & (g > 165) & (g < 230) & (b > 205)

    # 4. Thin white concentric circle lines in sky:
    # Outside Columbina's body (x < 330 or x > 760 or (y < 235 and (x < 420 or x > 660))):
    white_rings = in_box & (r > 210) & (g > 210) & (b > 220) & (
        (x_coords < 330) | (x_coords > 760) |
        ((y_coords < 235) & ((x_coords < 425) | (x_coords > 660)))
    )

    # 5. Below the chalice:
    # The chalice bowl curves up towards the sides.
    # Below the chalice bowl (y > 750), pixels outside the chalice handles and pendant:
    # Chalice pendant is at x in [500, 588], y in [850, 936].
    # Chalice bowl bottom curves: at x=350, bottom is ~860; at x=544, bottom is ~915 (above pendant).
    # Anything below the chalice boundary is background!
    below_chalice = in_box & (y_coords > 840) & (
        ((x_coords < 505) & (y_coords > 855)) |
        ((x_coords > 583) & (y_coords > 855)) |
        (y_coords > 936)
    )

    # Combine all background detections
    bg = (~in_box) | dark_sky | crescent_moon | pink_sphere | white_rings | below_chalice

    # Also: the area between left and right head wings (x in [495, 595], y in [180, 238]):
    # In that gap, it's just sky above Columbina's head:
    wing_gap_sky = in_box & (x_coords >= 495) & (x_coords <= 595) & (y_coords < 238)
    bg |= wing_gap_sky

    # Apply alpha: 255 for Columbina, 0 for background
    rgba[bg, 3] = 0
    rgba[~bg, 3] = 255

    # Save full-res test cutout
    test_im = Image.fromarray(rgba)
    test_im.save('C:/Users/soybe/.gemini/antigravity/brain/930e4101-5310-48ee-9b53-1a9777f45603/columbina_fullres_cutout.png')
    print("Full-res cutout saved!")

if __name__ == '__main__':
    generate_perfect_columbina()
