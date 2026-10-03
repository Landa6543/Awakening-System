import glob
import os
import re

models = glob.glob('src/main/java/**/client/**/*Model*.java', recursive=True)
models += glob.glob('src/main/java/**/renderer/**/*Model*.java', recursive=True)

print(f"Total model files found: {len(models)}")

for m in sorted(models):
    with open(m, 'r', encoding='utf-8', errors='ignore') as f:
        code = f.read()
    
    # Check setupAnim
    if 'setupAnim' in code:
        # extract setupAnim method body
        match = re.search(r'public void setupAnim\s*\([^)]+\)\s*\{([\s\S]*?)\n\s*\}', code)
        if match:
            body = match.group(1)
            # Check for suspicious patterns:
            # 1. division by zero (e.g. / 0 or / var without check)
            # 2. Math.sin with degree values without toRadians or Mth
            # 3. Missing reset of rotations (rotations accumulating every frame without being reset or set)
            # 4. Inconsistent angle ranges
            # 5. Nan risks
            issues = []
            if 'Math.asin' in body or 'Math.acos' in body:
                issues.append("Uses asin/acos without clamping check")
            if '/ 0' in body:
                issues.append("Divide by literal 0")
            
            # Print brief summary of model anim
            lines = [l.strip() for l in body.strip().split('\n') if l.strip() and not l.strip().startswith('//')]
            print(f"Model: {os.path.basename(m)} ({len(lines)} anim lines)")
            for iss in issues:
                print(f"  ISSUE: {iss}")
        else:
            print(f"Model: {os.path.basename(m)} (setupAnim found but regex mismatch)")
    else:
        print(f"Model: {os.path.basename(m)} (NO setupAnim)")
