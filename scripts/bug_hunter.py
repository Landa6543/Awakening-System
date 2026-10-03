import os
import re

def hunt_bugs():
    print("=== HUNTING POTENTIAL BUGS IN AWAKENING SYSTEM ===")
    
    server_crash_patterns = [
        ("Minecraft.getInstance()", re.compile(r'Minecraft\.getInstance\(\)')),
        ("DistExecutor or Dist.CLIENT import in common", re.compile(r'import net\.minecraft\.client\.')),
    ]

    concurrency_patterns = [
        re.compile(r'for\s*\([^)]+\)\s*\{[^}]*\.remove\('),
    ]

    div_zero_patterns = [
        re.compile(r'/\s*([a-zA-Z0-9_\.]+)\s*[\);,]'),
    ]

    issues = []

    for root, dirs, files in os.walk(os.path.join('src', 'main', 'java', 'com', 'soybe', 'awakeningsystem')):
        for file in files:
            if not file.endswith('.java'): continue
            path = os.path.join(root, file)
            # Check if this file is client-only
            is_client_file = any(c in file for c in ['Screen', 'Overlay', 'Client', 'Model', 'Renderer', 'Layer'])
            
            with open(path, 'r', encoding='utf-8', errors='ignore') as fp:
                lines = fp.readlines()

            for line_no, line in enumerate(lines, 1):
                # 1. Dedicated server crash check: Minecraft.getInstance() in non-client files!
                if not is_client_file and 'Minecraft.getInstance()' in line:
                    # check if guarded by Dist.CLIENT or FMLEnvironment
                    issues.append((file, line_no, "SERVER CRASH RISK", "Minecraft.getInstance() called in non-client file: " + line.strip()))

                # 2. Null check on getAttribute
                if '.getAttribute(' in line and ('.getValue()' in line or '.setBaseValue(' in line or '.addPermanentModifier(' in line):
                    issues.append((file, line_no, "POTENTIAL NPE", "getAttribute(...).method called directly without null check: " + line.strip()))

                # 3. getBlockEntity cast without check
                if 'level.getBlockEntity(' in line and '(' in line and ')' in line and 'BlockEntity)' in line:
                    if 'instanceof' not in line:
                        issues.append((file, line_no, "POTENTIAL CCE/NPE", "Casting getBlockEntity directly: " + line.strip()))

                # 4. Packet handling on wrong thread
                if 'handle(' in line and 'IPayloadContext' in line:
                    pass

    print(f"Total potential issues flagged: {len(issues)}")
    for file, line_no, category, desc in issues:
        print(f"[{category}] {file}:{line_no} -> {desc}")

if __name__ == '__main__':
    hunt_bugs()
