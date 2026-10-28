# -*- coding: utf-8 -*-
"""
Generador de estructuras NBT oficiales (StructureTemplate) para Minecraft 1.21.1:
- tracen_starting_gates.nbt (Cajones de salida oficiales con StartingGateBlock)
- tracen_grandstand.nbt (Grada oficial con RacetrackBleachersBlock y Taquilla)
"""
import io
import os
import gzip
import struct

OUTPUT_DIR = r"C:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\data\awakening_system\structure"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Tags NBT
TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12

class NBTWriter:
    def __init__(self):
        self.buf = io.BytesIO()

    def write_byte(self, val):
        self.buf.write(struct.pack('>b', val))

    def write_short(self, val):
        self.buf.write(struct.pack('>h', val))

    def write_int(self, val):
        self.buf.write(struct.pack('>i', val))

    def write_long(self, val):
        self.buf.write(struct.pack('>q', val))

    def write_string(self, val):
        b = val.encode('utf-8')
        self.write_short(len(b))
        self.buf.write(b)

    def write_tag(self, tag_type, name, payload_fn):
        self.write_byte(tag_type)
        self.write_string(name)
        payload_fn()

    def write_compound(self, name, items):
        def payload():
            for it in items:
                it(self)
            self.write_byte(TAG_END)
        if name is not None:
            self.write_tag(TAG_COMPOUND, name, payload)
        else:
            payload()

def nbt_int(name, val):
    return lambda w: w.write_tag(TAG_INT, name, lambda: w.write_int(val))

def nbt_string(name, val):
    return lambda w: w.write_tag(TAG_STRING, name, lambda: w.write_string(val))

def nbt_compound(name, items):
    return lambda w: w.write_compound(name, items)

def nbt_list(name, item_tag_type, items):
    def fn(w):
        def payload():
            w.write_byte(item_tag_type)
            w.write_int(len(items))
            for it in items:
                it(w)
        w.write_tag(TAG_LIST, name, payload)
    return fn

def list_int_item(val):
    return lambda w: w.write_int(val)

def list_compound_item(items):
    return lambda w: w.write_compound(None, items)

def build_structure_template(size, palette, blocks):
    """
    size: (sx, sy, sz)
    palette: list of (block_name, properties_dict)
    blocks: list of (x, y, z, state_idx)
    """
    palette_compounds = []
    for b_name, props in palette:
        prop_items = []
        if props:
            for pk, pv in props.items():
                prop_items.append(nbt_string(pk, pv))
            palette_compounds.append(list_compound_item([
                nbt_string("Name", b_name),
                nbt_compound("Properties", prop_items)
            ]))
        else:
            palette_compounds.append(list_compound_item([
                nbt_string("Name", b_name)
            ]))

    block_compounds = []
    for x, y, z, s_idx in blocks:
        block_compounds.append(list_compound_item([
            nbt_int("state", s_idx),
            nbt_list("pos", TAG_INT, [list_int_item(x), list_int_item(y), list_int_item(z)])
        ]))

    root_items = [
        nbt_int("DataVersion", 3839),
        nbt_list("size", TAG_INT, [list_int_item(size[0]), list_int_item(size[1]), list_int_item(size[2])]),
        nbt_list("entities", TAG_COMPOUND, []),
        nbt_list("palette", TAG_COMPOUND, palette_compounds),
        nbt_list("blocks", TAG_COMPOUND, block_compounds)
    ]

    writer = NBTWriter()
    writer.write_compound("", root_items)
    raw = writer.buf.getvalue()
    return gzip.compress(raw)

def generate_starting_gates():
    # 9 carriles con StartingGateBlock y visera de jockey
    # Size: X=5, Y=6, Z=11
    palette = [
        ("minecraft:smooth_stone", {}),                                                                # 0
        ("minecraft:iron_bars", {"east": "false", "north": "true", "south": "true", "west": "false"}),# 1
        ("minecraft:white_wool", {}),                                                                 # 2
        ("minecraft:green_wool", {}),                                                                 # 3
        ("awakening_system:starting_gate", {"facing": "east", "open": "false", "powered": "false"})  # 4
    ]

    blocks = []
    # Base de piedra lisa (Y=0)
    for x in range(5):
        for z in range(11):
            blocks.append((x, 0, z, 0))

    # Pared trasera de soporte en X=0
    for z in range(1, 10):
        for y in range(1, 5):
            blocks.append((0, y, z, 0))

    # Flancos de barrotes en Z=0 y Z=10
    for x in range(1, 4):
        for y in range(1, 4):
            blocks.append((x, y, 0, 1))
            blocks.append((x, y, 10, 1))

    # Puertas articuladas StartingGateBlock en X=3 (frente de cada cabina)
    for lane in range(9):
        z = lane + 1
        blocks.append((3, 1, z, 4))
        # Techo con toldo alternado verde/blanco en Y=4
        wool_state = 2 if (lane % 2 == 0) else 3
        blocks.append((1, 4, z, wool_state))
        blocks.append((2, 4, z, wool_state))
        blocks.append((3, 4, z, wool_state))

    nbt_data = build_structure_template((5, 6, 11), palette, blocks)
    out_file = os.path.join(OUTPUT_DIR, "tracen_starting_gates.nbt")
    with open(out_file, "wb") as f:
        f.write(nbt_data)
    print(f"Estructura guardada: {out_file} ({len(blocks)} bloques)")

def generate_grandstand():
    # Grada oficial de hipódromo con RacetrackBleachersBlock y Taquilla de Apuestas
    # Size: X=9, Y=7, Z=7
    palette = [
        ("minecraft:stone_bricks", {}),                                                               # 0
        ("minecraft:stone_brick_stairs", {"facing": "north", "half": "bottom", "shape": "straight"}), # 1
        ("minecraft:stripped_dark_oak_log", {"axis": "y"}),                                          # 2
        ("minecraft:red_wool", {}),                                                                   # 3
        ("minecraft:white_wool", {}),                                                                 # 4
        ("minecraft:lantern", {"hanging": "true"}),                                                   # 5
        ("awakening_system:racetrack_bleachers", {"facing": "north"}),                                # 6
        ("awakening_system:uma_bookmaker", {"facing": "south"})                                       # 7
    ]

    blocks = []
    # Terraza de ladrillos de piedra (Y=0)
    for x in range(9):
        for z in range(7):
            blocks.append((x, 0, z, 0))

    # Columnas de madera oscura (Y=1 a 4)
    for y in range(1, 5):
        blocks.append((0, y, 0, 2))
        blocks.append((8, y, 0, 2))
        blocks.append((0, y, 6, 2))
        blocks.append((8, y, 6, 2))

    # Gradas RacetrackBleachersBlock en Z=4 y Z=5 (para sentarse)
    for x in range(2, 7):
        blocks.append((x, 1, 4, 6))
        blocks.append((x, 1, 5, 6))

    # Taquilla de Apuestas UmaBookmaker en el centro delantero Z=2
    blocks.append((4, 1, 2, 7))

    # Escalones de acceso en Z=0
    for x in range(3, 6):
        blocks.append((x, 1, 0, 1))

    # Techo con toldo festivo rojo y blanco en Y=5
    for x in range(9):
        for z in range(7):
            wool_state = 3 if ((x + z) % 2 == 0) else 4
            blocks.append((x, 5, z, wool_state))

    # Linternas colgantes en Y=4
    blocks.append((2, 4, 2, 5))
    blocks.append((6, 4, 2, 5))
    blocks.append((4, 4, 4, 5))

    nbt_data = build_structure_template((9, 7, 7), palette, blocks)
    out_file = os.path.join(OUTPUT_DIR, "tracen_grandstand.nbt")
    with open(out_file, "wb") as f:
        f.write(nbt_data)
    print(f"Estructura guardada: {out_file} ({len(blocks)} bloques)")

if __name__ == "__main__":
    generate_starting_gates()
    generate_grandstand()
