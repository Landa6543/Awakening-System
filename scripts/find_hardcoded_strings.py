# -*- coding: utf-8 -*-
import re, os, sys
sys.stdout.reconfigure(encoding='utf-8')

pattern = re.compile(r'Component\.literal\("([^"]+)"\)')
spanish_chars = re.compile(r'[áéíóúÁÉÍÓÚñÑ¡¿]')
spanish_keywords = [
    'cultivo', 'maestro', 'fábula', 'fabula', 'inmortal', 'sombras', 'cazador',
    'nivel', 'puntos', 'reino', 'habilidad', 'misión', 'mision', 'tienda',
    'comprar', 'vender', 'monedas', 'desbloque', 'bloqueado', 'ver fábulas',
    'fábulas (orv)', 'cerrar', 'salir', 'daño', 'fuerza', 'agilidad', 'vitalidad',
    'inteligencia', 'percepción', 'voluntad', 'activo', 'pasivo', 'rango',
    'ascender', 'reclamar', 'completado', 'diaria', 'semanal', 'mazmorra'
]

found = []
for root, dirs, files in os.walk('src/main/java'):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8', errors='ignore') as fp:
                for idx, line in enumerate(fp):
                    matches = pattern.findall(line)
                    for m in matches:
                        # Skip simple symbols, numbers, format codes
                        cleaned = re.sub(r'§[0-9a-fk-or]', '', m).strip()
                        if not cleaned or len(cleaned) <= 2:
                            continue
                        if spanish_chars.search(m) or any(w in cleaned.lower() for w in spanish_keywords):
                            found.append((f, idx + 1, m, path))

print(f"Total hardcoded Spanish Component.literal strings: {len(found)}")
for f, line_no, text, p in found:
    print(f"{f}:{line_no} -> {text}")
