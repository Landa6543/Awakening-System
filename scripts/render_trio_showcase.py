from PIL import Image, ImageDraw, ImageFont
import os
import shutil

# Paths
base_dir = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema"
item_dir = os.path.join(base_dir, r"src\main\resources\assets\awakening_system\textures\item")
entity_dir = os.path.join(base_dir, r"src\main\resources\assets\awakening_system\textures\entity")

# Textures
tex_ruin_guard = os.path.join(entity_dir, r"ruin_guard\ruin_guard.png")
tex_lawachurl = os.path.join(entity_dir, r"lawachurl\stonehide_lawachurl.png")
tex_regisvine = os.path.join(entity_dir, r"regisvine\cryo_regisvine.png")

# Items
chaos_core = os.path.join(item_dir, "chaos_core.png")
black_crystal_horn = os.path.join(item_dir, "black_crystal_horn.png")
hoarfrost_core = os.path.join(item_dir, "hoarfrost_core.png")

# Create Canvas
width = 1100
height = 550
img = Image.new("RGBA", (width, height), (18, 20, 28, 255))
draw = ImageDraw.Draw(img)

# Title & subtitle
draw.rectangle([(20, 20), (width - 20, 90)], fill=(28, 32, 48, 255), outline=(70, 80, 110, 255), width=2)
# Draw mock header text
draw.text((40, 30), "NUEVOS JEFES DE CAMPO Y AUTOMATAS DE TEYVAT (GENSHIN IMPACT)", fill=(240, 210, 100, 255))
draw.text((40, 58), "Awakening System v0.3.0 - NeoForge 1.21.1 | Modelos 3D Blockbench, IA Autonoma & Drops", fill=(180, 190, 220, 255))

# 3 Columns
cols = [
    {
        "title": "GUARDIAN DE LAS RUINAS",
        "sub": "Automata de Guerra de Khaenri'ah",
        "type": "Maquinaria Hostil",
        "hp": "300 HP | Torbellino & Misiles",
        "tex": tex_ruin_guard,
        "item": chaos_core,
        "item_name": "Nucleo del Caos",
        "color": (210, 150, 70, 255),
        "bg": (35, 30, 24, 255)
    },
    {
        "title": "STONEHIDE LAWACHURL",
        "sub": "Cacique Supremo Hilichurl",
        "type": "Elite Salvaje Geo",
        "hp": "280 HP | Escudo & Salto Sismico",
        "tex": tex_lawachurl,
        "item": black_crystal_horn,
        "item_name": "Cuerno de Cristal Negro",
        "color": (220, 180, 80, 255),
        "bg": (38, 34, 26, 255)
    },
    {
        "title": "REGISVID CRIOGENICA",
        "sub": "Flor Criopurpurea Titanica",
        "type": "Jefe de Campo Cryo",
        "hp": "350 HP | Corola, Giro & Carambanos",
        "tex": tex_regisvine,
        "item": hoarfrost_core,
        "item_name": "Nucleo de Escarcha",
        "color": (90, 190, 240, 255),
        "bg": (22, 34, 46, 255)
    }
]

col_w = 330
start_x = 40
start_y = 110

for i, col in enumerate(cols):
    x = start_x + i * (col_w + 25)
    y = start_y
    draw.rectangle([(x, y), (x + col_w, y + 400)], fill=col["bg"], outline=col["color"], width=2)
    
    # Title
    draw.text((x + 15, y + 15), col["title"], fill=col["color"])
    draw.text((x + 15, y + 35), col["sub"], fill=(200, 200, 200, 255))
    draw.text((x + 15, y + 55), col["type"], fill=(150, 150, 150, 255))
    draw.text((x + 15, y + 75), col["hp"], fill=(255, 230, 140, 255))

    # Entity Texture Preview Box
    if os.path.exists(col["tex"]):
        t_img = Image.open(col["tex"]).convert("RGBA")
        t_img = t_img.resize((180, 180), Image.Resampling.NEAREST)
        img.paste(t_img, (x + (col_w - 180) // 2, y + 105), t_img)

    # Item Drop
    draw.rectangle([(x + 20, y + 310), (x + col_w - 20, y + 380)], fill=(20, 22, 30, 200), outline=(80, 90, 110, 255))
    if os.path.exists(col["item"]):
        it_img = Image.open(col["item"]).convert("RGBA")
        it_img = it_img.resize((50, 50), Image.Resampling.NEAREST)
        img.paste(it_img, (x + 30, y + 320), it_img)
    draw.text((x + 90, y + 325), "Drop Caracteristico:", fill=(160, 170, 190, 255))
    draw.text((x + 90, y + 345), col["item_name"], fill=col["color"])

# Save
out_local = os.path.join(base_dir, "trio_bosses_showcase.png")
out_artifact = r"C:\Users\soybe\.gemini\antigravity\brain\930e4101-5310-48ee-9b53-1a9777f45603\trio_bosses_showcase.png"

img.save(out_local)
img.save(out_artifact)
print(f"Showcase saved to {out_local} and {out_artifact}")
