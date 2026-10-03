import glob
import re

models = glob.glob('src/main/java/**/client/**/*Model*.java', recursive=True)

for m in sorted(models):
    with open(m, 'r', encoding='utf-8', errors='ignore') as f:
        code = f.read()
    
    if 'setupAnim' in code:
        has_reset = 'resetPose' in code
        has_plus_equals = bool(re.search(r'\w+\.(?:xRot|yRot|zRot|x|y|z)\s*[\+\-]=', code))
        
        # Check if plus_equals is used WITHOUT resetPose
        if has_plus_equals and not has_reset:
            print(f"CRITICAL: {m} uses +=/-= on part rotations without resetPose()!")
        elif not has_reset:
            print(f"NOTE: {m} does not call resetPose()")
