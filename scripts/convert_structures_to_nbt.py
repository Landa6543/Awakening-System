# -*- coding: utf-8 -*-
"""
Convert Teyvat structures from C:/Users/soybe/Downloads/estructuras
into Minecraft 1.21.1 StructureTemplate NBT format and inject Sumeru regions.
"""
import os
import io
import sys
import math
import zlib
import gzip
import struct
import zipfile
import tempfile
import nbtlib
from nbtlib.tag import Compound, List, Int, Short, Byte, String, Long, ByteArray

sys.stdout.reconfigure(encoding='utf-8')

DOWNLOADS_DIR = r"C:\Users\soybe\Downloads\estructuras"
OUTPUT_DIR = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\data\awakening_system\structure"
os.makedirs(OUTPUT_DIR, exist_ok=True)

DATA_VERSION = 3839  # Minecraft 1.21.1

def parse_blockstate_str(s):
    if '[' in s and s.endswith(']'):
        name, props_str = s[:-1].split('[', 1)
        props = {}
        for p in props_str.split(','):
            if '=' in p:
                k, v = p.split('=', 1)
                props[k] = String(v)
        return name, props
    return s, None

# =========================================================================
# 1. Convert genftreeshouses.schem -> teyvat_treehouses.nbt
# =========================================================================
def convert_treehouses():
    print("--- Converting Treehouses (genftreeshouses.schem) ---")
    schem_path = os.path.join(DOWNLOADS_DIR, "genftreeshouses.schem")
    if not os.path.exists(schem_path):
        print("genftreeshouses.schem not found, skipping.")
        return

    nbt = nbtlib.load(schem_path)
    s = nbt['Schematic']
    width = int(s['Width'])
    height = int(s['Height'])
    length = int(s['Length'])
    print(f"Treehouses dimensions: {width} x {height} x {length}")

    raw_palette = s['Blocks']['Palette']
    # Build palette list
    id_to_state = {}
    palette_compounds = []
    
    # In Sponge v3, palette is a dict of blockstate -> int_id
    sorted_pal = sorted(raw_palette.items(), key=lambda x: int(x[1]))
    
    # We will map old palette index to new palette index
    old_to_new_pal = {}
    for block_str, old_idx in sorted_pal:
        name, props = parse_blockstate_str(str(block_str))
        comp = Compound({'Name': String(name)})
        if props:
            comp['Properties'] = Compound(props)
        new_idx = len(palette_compounds)
        palette_compounds.append(comp)
        old_to_new_pal[int(old_idx)] = (new_idx, name)

    # Read varints from Data
    bdata = bytes(s['Blocks']['Data'])
    expected = width * height * length
    block_ids = []
    i = 0
    data_len = len(bdata)
    while i < data_len and len(block_ids) < expected:
        val = 0
        shift = 0
        while True:
            b = bdata[i]
            i += 1
            val |= (b & 0x7F) << shift
            if (b & 0x80) == 0:
                break
            shift += 7
        block_ids.append(val)

    print(f"Decoded {len(block_ids)} blocks from Sponge schematic")

    # Map block entities
    bes_by_pos = {}
    for be in s['Blocks'].get('BlockEntities', []):
        pos = be.get('Pos', [0, 0, 0])
        bes_by_pos[(int(pos[0]), int(pos[1]), int(pos[2]))] = be

    # Create blocks list (skip air)
    blocks_list = []
    for idx, old_id in enumerate(block_ids):
        new_id, name = old_to_new_pal[old_id]
        if 'air' in name:
            continue
        # Order in Sponge v3: (y * length + z) * width + x
        x = idx % width
        rem = idx // width
        z = rem % length
        y = rem // length

        block_comp = Compound({
            'state': Int(new_id),
            'pos': List[Int]([Int(x), Int(y), Int(z)])
        })
        if (x, y, z) in bes_by_pos:
            be_data = Compound(bes_by_pos[(x, y, z)].get('Data', {}))
            if be_data:
                block_comp['nbt'] = be_data
        blocks_list.append(block_comp)

    print(f"Non-air blocks: {len(blocks_list)}")

    out_nbt = Compound({
        'DataVersion': Int(DATA_VERSION),
        'size': List[Int]([Int(width), Int(height), Int(length)]),
        'palette': List[Compound](palette_compounds),
        'blocks': List[Compound](blocks_list),
        'entities': List[Compound]()
    })

    out_file = os.path.join(OUTPUT_DIR, "teyvat_treehouses.nbt")
    nbt_file = nbtlib.File(out_nbt)
    nbt_file.save(out_file, gzipped=True)
    print(f"Saved teyvat_treehouses.nbt ({os.path.getsize(out_file) / 1024:.1f} KB)")


# =========================================================================
# 2. Convert genshin-impact-majo-no-island.zip -> witch_island.nbt
# =========================================================================
def convert_witch_island():
    print("\n--- Converting Hexenzirkel Witch Island ---")
    zip_path = os.path.join(DOWNLOADS_DIR, "genshin-impact-majo-no-island.zip")
    if not os.path.exists(zip_path):
        print("genshin-impact-majo-no-island.zip not found, skipping.")
        return

    with zipfile.ZipFile(zip_path, 'r') as z:
        for name in z.namelist():
            tmp = os.path.join(tempfile.gettempdir(), 'temp_witch.litematic')
            with open(tmp, 'wb') as f:
                f.write(z.read(name))
            nbt = nbtlib.load(tmp)
            os.remove(tmp)

            reg_name = list(nbt['Regions'].keys())[0]
            reg = nbt['Regions'][reg_name]
            size_x = abs(int(reg['Size']['x']))
            size_y = abs(int(reg['Size']['y']))
            size_z = abs(int(reg['Size']['z']))
            print(f"Witch island dimensions: {size_x} x {size_y} x {size_z}")

            pal = reg['BlockStatePalette']
            pal_compounds = []
            air_indices = set()
            for idx, p in enumerate(pal):
                name = str(p['Name'])
                if 'air' in name:
                    air_indices.add(idx)
                comp = Compound({'Name': String(name)})
                if 'Properties' in p:
                    comp['Properties'] = Compound({k: String(str(v)) for k, v in p['Properties'].items()})
                pal_compounds.append(comp)

            # Unpack bit-stream
            raw_states = reg['BlockStates']
            total_blocks = size_x * size_y * size_z
            blocks_list = []

            block_idx = 0
            for l in raw_states:
                val = int(l) & 0xFFFFFFFFFFFFFFFF
                for b in range(8):
                    p_idx = (val >> (b * 8)) & 0xFF
                    if p_idx not in air_indices and p_idx < len(pal_compounds):
                        # Coordinates: (y * size_z + z) * size_x + x
                        x = block_idx % size_x
                        rem = block_idx // size_x
                        z = rem % size_z
                        y = rem // size_z
                        blocks_list.append(Compound({
                            'state': Int(p_idx),
                            'pos': List[Int]([Int(x), Int(y), Int(z)])
                        }))
                    block_idx += 1
                    if block_idx >= total_blocks:
                        break
                if block_idx >= total_blocks:
                    break

            print(f"Decoded non-air blocks: {len(blocks_list)}")

            out_nbt = Compound({
                'DataVersion': Int(DATA_VERSION),
                'size': List[Int]([Int(size_x), Int(size_y), Int(size_z)]),
                'palette': List[Compound](pal_compounds),
                'blocks': List[Compound](blocks_list),
                'entities': List[Compound]()
            })

            out_file = os.path.join(OUTPUT_DIR, "witch_island.nbt")
            nbt_file = nbtlib.File(out_nbt)
            nbt_file.save(out_file, gzipped=True)
            print(f"Saved witch_island.nbt ({os.path.getsize(out_file) / 1024:.1f} KB)")
            break


# =========================================================================
# 3. Extract Dawn Winery mansion -> dawn_winery.nbt
# =========================================================================
def extract_dawn_winery():
    print("\n--- Extracting Dawn Winery Mansion ---")
    zip_path = os.path.join(DOWNLOADS_DIR, "DawnWineryUNPACK.zip")
    if not os.path.exists(zip_path):
        print("DawnWineryUNPACK.zip not found, skipping.")
        return

    with zipfile.ZipFile(zip_path, 'r') as z:
        data = z.read('Dawn Winery/region/r.-1.-2.mca')

    # Bounding box for Dawn Winery Mansion and entrance courtyard:
    # X: [-114..-44] (width: 71)
    # Y: [96..121]   (height: 26)
    # Z: [-735..-668] (length: 68)
    min_x, max_x = -114, -44
    min_y, max_y = 96, 121
    min_z, max_z = -735, -668
    width = max_x - min_x + 1
    height = max_y - min_y + 1
    length = max_z - min_z + 1
    print(f"Dawn Winery estate bounds: {width} x {height} x {length}")

    # Load chunks covering this bounding box:
    # cx from -114 // 16 = -8 to -44 // 16 = -3
    # cz from -735 // 16 = -46 to -668 // 16 = -42
    # Collect all blocks in this 3D box
    world_blocks = {}
    block_entities = {}

    for cx in range(-8, -2):
        for cz in range(-47, -41):
            chunk_idx = (cx + 32) + (cz + 64) * 32
            offset = struct.unpack('>I', b'\x00' + data[chunk_idx*4 : chunk_idx*4+3])[0]
            if offset == 0:
                continue
            sector_start = offset * 4096
            sec_len = struct.unpack('>I', data[sector_start:sector_start+4])[0]
            raw_chunk = zlib.decompress(data[sector_start+5 : sector_start+4+sec_len])
            tmp = os.path.join(tempfile.gettempdir(), 'chk_dw_ext.nbt')
            with open(tmp, 'wb') as f:
                f.write(raw_chunk)
            cnbt = nbtlib.load(tmp)

            # Block entities
            for be in cnbt.get('block_entities', []):
                bx = int(be.get('x', 0))
                by = int(be.get('y', 0))
                bz = int(be.get('z', 0))
                if min_x <= bx <= max_x and min_y <= by <= max_y and min_z <= bz <= max_z:
                    block_entities[(bx, by, bz)] = be

            for s in cnbt.get('sections', []):
                y_sec = int(s.get('Y', 0))
                sec_base_y = y_sec * 16
                if sec_base_y + 15 < min_y or sec_base_y > max_y:
                    continue
                bs = s.get('block_states', {})
                pal = bs.get('palette', [])
                if not pal or 'data' not in bs:
                    continue

                raw_data = bs['data']
                bits = max(4, math.ceil(math.log2(len(pal))))
                mask = (1 << bits) - 1
                b_per_long = 64 // bits

                for idx in range(4096):
                    long_idx = idx // b_per_long
                    bit_idx = (idx % b_per_long) * bits
                    val = (int(raw_data[long_idx]) >> bit_idx) & mask
                    if val < len(pal):
                        p_entry = pal[val]
                        name = str(p_entry.get('Name', 'minecraft:air'))
                        if 'air' not in name:
                            bx = (idx % 16) + cx * 16
                            by = ((idx // 256) % 16) + sec_base_y
                            bz = ((idx // 16) % 16) + cz * 16
                            if min_x <= bx <= max_x and min_y <= by <= max_y and min_z <= bz <= max_z:
                                props = p_entry.get('Properties')
                                prop_dict = {k: str(v) for k, v in props.items()} if props else None
                                world_blocks[(bx, by, bz)] = (name, prop_dict)
            os.remove(tmp)

    print(f"Extracted {len(world_blocks)} non-air blocks from Dawn Winery estate")

    # Build unique palette
    palette_map = {}
    palette_compounds = []
    for (name, props) in world_blocks.values():
        key = (name, tuple(sorted(props.items())) if props else None)
        if key not in palette_map:
            palette_map[key] = len(palette_compounds)
            comp = Compound({'Name': String(name)})
            if props:
                comp['Properties'] = Compound({k: String(v) for k, v in props.items()})
            palette_compounds.append(comp)

    print(f"Dawn Winery palette size: {len(palette_compounds)}")

    # Build block list in relative coordinates
    blocks_list = []
    for (bx, by, bz), (name, props) in world_blocks.items():
        rx = bx - min_x
        ry = by - min_y
        rz = bz - min_z
        key = (name, tuple(sorted(props.items())) if props else None)
        pal_id = palette_map[key]
        b_comp = Compound({
            'state': Int(pal_id),
            'pos': List[Int]([Int(rx), Int(ry), Int(rz)])
        })

        if (bx, by, bz) in block_entities:
            orig_be = block_entities[(bx, by, bz)]
            # Clean up or customize sign text
            be_copy = Compound({k: orig_be[k] for k in orig_be})
            be_copy['x'] = Int(rx)
            be_copy['y'] = Int(ry)
            be_copy['z'] = Int(rz)
            if 'sign' in str(orig_be.get('id', '')):
                # Custom winery sign
                be_copy['front_text'] = Compound({
                    'messages': List[String]([
                        String('{"text":"[VIÑEDO DEL AMANECER]","color":"gold","bold":true}'),
                        String('{"text":"Bodega de Diluc","color":"dark_red"}'),
                        String('{"text":"Mosto de Diente de León","color":"yellow"}'),
                        String('{"text":"Mondstadt","color":"aqua"}')
                    ]),
                    'has_glowing_text': Byte(1),
                    'color': String('gold')
                })
            b_comp['nbt'] = be_copy

        blocks_list.append(b_comp)

    out_nbt = Compound({
        'DataVersion': Int(DATA_VERSION),
        'size': List[Int]([Int(width), Int(height), Int(length)]),
        'palette': List[Compound](palette_compounds),
        'blocks': List[Compound](blocks_list),
        'entities': List[Compound]()
    })

    out_file = os.path.join(OUTPUT_DIR, "dawn_winery.nbt")
    nbt_file = nbtlib.File(out_nbt)
    nbt_file.save(out_file, gzipped=True)
    print(f"Saved dawn_winery.nbt ({os.path.getsize(out_file) / 1024:.1f} KB)")


# =========================================================================
# 4. Extract Sumeru 1.21.1 regions into world saves
# =========================================================================
def extract_sumeru_regions():
    print("\n--- Extracting Sumeru 1.21.1 Regions ---")
    zip_path = os.path.join(DOWNLOADS_DIR, "Genshin+Impact+Sumeru+in+Minecraft+UNZIP+ME.zip")
    if not os.path.exists(zip_path):
        print("Sumeru zip not found, skipping.")
        return

    # Destinations:
    target_dirs = [
        r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\run\saves\Mundo nuevo (9)\dimensions\awakening_system\teyvat_world",
        r"C:\Users\soybe\curseforge\minecraft\Instances\Awakening Monarchs Ascension\saves\Mundo nuevo (1)\dimensions\awakening_system\teyvat_world",
        r"C:\Users\soybe\curseforge\minecraft\Instances\Awakening Monarchs Ascension\saves\Mundo nuevo\dimensions\awakening_system\teyvat_world"
    ]

    # Filter out 0,0 and 1,0 so Mondstadt is NEVER touched!
    excluded_regions = {'r.0.0.mca', 'r.0.1.mca', 'r.1.0.mca', 'r.1.1.mca'}

    with zipfile.ZipFile(zip_path, 'r') as z:
        sumeru_entries = [
            n for n in z.namelist() 
            if 'Genshin Impact Sumeru - 1.20.6-1.21.1/' in n and n.endswith('.mca')
        ]
        print(f"Total Sumeru 1.21.1 MCA entries: {len(sumeru_entries)}")

        for tdir in target_dirs:
            if not os.path.exists(tdir):
                continue
            r_dir = os.path.join(tdir, "region")
            e_dir = os.path.join(tdir, "entities")
            os.makedirs(r_dir, exist_ok=True)
            os.makedirs(e_dir, exist_ok=True)

            copied = 0
            for entry in sumeru_entries:
                fname = os.path.basename(entry)
                if fname in excluded_regions:
                    continue
                dest_dir = e_dir if 'entities/' in entry else r_dir
                dest_file = os.path.join(dest_dir, fname)
                with open(dest_file, 'wb') as f:
                    f.write(z.read(entry))
                copied += 1
            print(f"Extracted {copied} Sumeru region & entity files to {tdir}")


if __name__ == '__main__':
    convert_treehouses()
    convert_witch_island()
    extract_dawn_winery()
    extract_sumeru_regions()
    print("\nAll structures successfully converted and integrated!")
