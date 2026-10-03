import glob
import os
import re

screen_files = glob.glob('src/main/java/**/*Screen*.java', recursive=True)
print(f"Total screen files: {len(screen_files)}")

for sf in sorted(screen_files):
    with open(sf, 'r', encoding='utf-8', errors='ignore') as f:
        code = f.read()
    
    # Check if mouseClicked is overridden
    has_mouse_click = 'boolean mouseClicked(' in code
    # Check if mouseScrolled is overridden
    has_mouse_scroll = 'boolean mouseScrolled(' in code
    # Check if render has pose scaling or matrix transformations
    has_scaling = bool(re.search(r'pose\(\)\.scale|poseStack\.scale', code))
    
    # Check coordinate transformations
    has_mX = 'mX' in code or 'transformed' in code.lower()
    
    print(f"Screen: {os.path.basename(sf)}")
    print(f"  mouseClicked: {has_mouse_click}, mouseScrolled: {has_mouse_scroll}, scaling: {has_scaling}, transformedCoords: {has_mX}")
    
    # Check for potential bugs:
    # 1. Scaling without transformed mouse coordinates in mouseClicked!
    if has_scaling and has_mouse_click:
        # Check if mouseClicked divides mouseX/mouseY by the scale!
        click_match = re.search(r'public boolean mouseClicked\([^)]+\)\s*\{([\s\S]*?)\n\s*\}', code)
        if click_match:
            click_body = click_match.group(1)
            if 'scale' not in click_body.lower() and 'mx' not in click_body.lower():
                print(f"  WARNING: {os.path.basename(sf)} has scaling in render, but mouseClicked might not scale mouseX/mouseY!")
