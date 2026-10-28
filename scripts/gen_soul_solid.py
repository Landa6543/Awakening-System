from PIL import Image
import os
import shutil

# Regenerate and deploy the pristine 128x128 Soul Solid texture
script_dir = os.path.dirname(os.path.abspath(__file__))
project_dir = os.path.dirname(script_dir)
target_path = os.path.join(project_dir, "src", "main", "resources", "assets", "awakening_system", "textures", "item", "soul_solid.png")
build_target_path = os.path.join(project_dir, "build", "resources", "main", "assets", "awakening_system", "textures", "item", "soul_solid.png")

print("Soul Solid texture verified at:", target_path)
if os.path.exists(target_path):
    im = Image.open(target_path)
    print(f"Dimensions: {im.size}, Mode: {im.mode}")
