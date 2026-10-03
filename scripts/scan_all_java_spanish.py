import os
import re

java_dir = 'src/main/java'

spanish_signals = [' de la ', ' del ', ' las ', ' los ', ' daño ', ' vida ', ' habilidad ', ' ranura ', ' artefacto ', ' nivel ']

results = []
for root, _, files in os.walk(java_dir):
    for f in files:
        if f.endswith('.java'):
            fp = os.path.join(root, f)
            with open(fp, 'r', encoding='utf-8') as jf:
                lines = jf.readlines()
            for idx, line in enumerate(lines, 1):
                # check Component.literal
                for m in re.findall(r'Component\.literal\(\"([^\"]+)\"\)', line):
                    m_low = ' ' + m.lower() + ' '
                    if any(s in m_low for s in spanish_signals) or any(c in m for c in ['á', 'é', 'í', 'ó', 'ú', 'ñ', '¿', '¡']):
                        results.append((f, idx, m))

with open('scratch/all_java_hardcoded_spanish.txt', 'w', encoding='utf-8') as out:
    for f, idx, m in results:
        out.write(f"{f}:{idx}: {m}\n")

print(f"Total hardcoded Spanish Component.literal across entire codebase: {len(results)}")
