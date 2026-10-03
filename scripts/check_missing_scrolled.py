import glob
import os
import re

screen_files = glob.glob('src/main/java/**/*Screen*.java', recursive=True)

for sf in sorted(screen_files):
    with open(sf, 'r', encoding='utf-8', errors='ignore') as f:
        code = f.read()
    
    # Find any scroll field (e.g. int ...scroll... = 0;)
    scroll_fields = re.findall(r'(?:int|float)\s+([a-zA-Z0-9_]*scroll[a-zA-Z0-9_]*)\s*=', code, re.IGNORECASE)
    has_mouse_scrolled = 'mouseScrolled' in code
    
    if scroll_fields and not has_mouse_scrolled:
        print(f"BUG in {os.path.basename(sf)}: Has scroll fields {scroll_fields} but MISSING mouseScrolled()!")
    elif scroll_fields:
        print(f"OK {os.path.basename(sf)}: Has {scroll_fields} and implements mouseScrolled()")
