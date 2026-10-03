import re

with open('src/main/java/com/soybe/awakeningsystem/SystemScreen.java', 'r', encoding='utf-8') as f:
    lines = f.readlines()

spanish_signals = [' de ', ' del ', ' la ', ' las ', ' los ', ' para ', ' con ', ' por ', ' una ', ' uno ', ' este ', ' esta ', ' daño ', ' vida ', ' nivel ', ' mejora ', ' objeto ', ' enemigo ', ' enemigos ', ' habilidad ', ' ranura ', ' rango ', ' puntos ', ' estado ', ' misiones ', ' titulos ', ' tienda ']

hardcoded = []
for idx, line in enumerate(lines, 1):
    # look for Component.literal(...) or g.drawString(...) with spanish
    match_lit = re.findall(r'Component\.literal\(\"([^\"]+)\"\)', line)
    match_str = re.findall(r'drawString\([^,]+,\s*\"([^\"]+)\"', line)
    for s in match_lit + match_str:
        s_low = ' ' + s.lower() + ' '
        if any(w in s_low for w in spanish_signals) or any(c in s for c in ['á', 'é', 'í', 'ó', 'ú', 'ñ', '¿', '¡']):
            hardcoded.append((idx, s))

print(f"Total hardcoded Spanish strings in SystemScreen: {len(hardcoded)}")
with open('scratch/systemscreen_hardcoded.txt', 'w', encoding='utf-8') as f:
    for line_no, text in hardcoded:
        f.write(f"L{line_no}: {text}\n")

for line_no, text in hardcoded[:25]:
    print(f"L{line_no}: {text}")
