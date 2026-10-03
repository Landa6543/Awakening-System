import glob
import os
import re

screen_files = glob.glob('src/main/java/**/*Screen*.java', recursive=True)

for sf in sorted(screen_files):
    with open(sf, 'r', encoding='utf-8', errors='ignore') as f:
        code = f.read()
    
    has_get_gui_scale = 'getGuiScale' in code
    has_transform = 'transformMouseX' in code
    
    if has_get_gui_scale or 'pose().scale' in code or 'poseStack.scale' in code:
        # Check mouseClicked
        click_match = re.search(r'public boolean mouseClicked\s*\([^)]+\)\s*\{([\s\S]*?)\n\s*\}', code)
        click_transforms = False
        if click_match:
            click_body = click_match.group(1)
            click_transforms = 'transformMouseX' in click_body or 'mX' in click_body or 'scale' in click_body
        
        # Check mouseScrolled
        scroll_match = re.search(r'public boolean mouseScrolled\s*\([^)]+\)\s*\{([\s\S]*?)\n\s*\}', code)
        scroll_transforms = False
        if scroll_match:
            scroll_body = scroll_match.group(1)
            scroll_transforms = 'transformMouseX' in scroll_body or 'mX' in scroll_body or 'scale' in scroll_body
        
        print(f"Screen: {os.path.basename(sf)}")
        print(f"  getGuiScale/scale present: True")
        print(f"  mouseClicked transforms: {click_transforms}")
        if scroll_match:
            print(f"  mouseScrolled transforms: {scroll_transforms}")
