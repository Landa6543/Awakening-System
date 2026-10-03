import os, sys, io, zlib, struct, zipfile, time
import nbtlib

def clean_r10_file(in_path, out_path):
    print(f"Reading {in_path}...")
    with open(in_path, 'rb') as f:
        data = f.read()
    header = data[:4096]
    timestamps = data[4096:8192]

    ship_materials = {
        'minecraft:gray_wool', 'minecraft:red_wool', 'minecraft:yellow_wool', 'minecraft:white_wool',
        'minecraft:dark_oak_planks', 'minecraft:spruce_planks', 'minecraft:oak_planks',
        'minecraft:stripped_dark_oak_wood', 'minecraft:stripped_dark_oak_log',
        'minecraft:stripped_spruce_wood', 'minecraft:stripped_spruce_log',
        'minecraft:dark_oak_fence', 'minecraft:spruce_fence', 'minecraft:oak_fence',
        'minecraft:dark_oak_fence_gate', 'minecraft:spruce_fence_gate',
        'minecraft:dark_oak_stairs', 'minecraft:spruce_stairs', 'minecraft:oak_stairs',
        'minecraft:dark_oak_slab', 'minecraft:spruce_slab', 'minecraft:oak_slab',
        'minecraft:andesite_slab', 'minecraft:smooth_stone_slab', 'minecraft:birch_slab',
        'minecraft:dark_oak_trapdoor', 'minecraft:spruce_trapdoor', 'minecraft:lantern'
    }

    road_materials = {
        'minecraft:light_blue_wool', 'minecraft:white_wool', 'minecraft:campfire',
        'minecraft:oak_fence', 'minecraft:oak_stairs'
    }

    chunks_dict = {}
    barriers_cleared = 0
    ship_cleared = 0

    for cz in range(32):
        for cx in range(32):
            idx = 4 * (cx + cz * 32)
            off = struct.unpack('>I', b'\x00' + header[idx:idx+3])[0] * 4096
            if off == 0:
                continue
            length = struct.unpack('>I', data[off:off+4])[0]
            comp = data[off+4]
            if comp != 2:
                continue
            decomp = zlib.decompress(data[off+5:off+4+length])
            chunk = nbtlib.File.from_fileobj(io.BytesIO(decomp))
            root = chunk.get('', chunk)

            # 1. Clean ALL barriers everywhere in r.1.0
            for s in root.get('sections', []):
                b = s.get('block_states', {})
                pal = b.get('palette', [])
                for p in pal:
                    if str(p.get('Name')) == 'minecraft:barrier':
                        p['Name'] = nbtlib.String('minecraft:air')
                        if 'Properties' in p:
                            del p['Properties']
                        barriers_cleared += 1

            # 2. Clean misplaced Beidou ship in cx=10..14, cz=26..31, Y >= 63
            is_ship_area = (10 <= cx <= 14) and (26 <= cz <= 31)
            if is_ship_area:
                for s in root.get('sections', []):
                    y_sec = int(s.get('Y', -99))
                    if y_sec >= 4: # Y >= 64
                        b = s.get('block_states', {})
                        pal = b.get('palette', [])
                        for p in pal:
                            name = str(p.get('Name', ''))
                            if name in ship_materials:
                                p['Name'] = nbtlib.String('minecraft:air')
                                if 'Properties' in p:
                                    del p['Properties']
                                ship_cleared += 1

            # 3. Clean elevated road camp and fences in cx=10..15, cz=22..24
            is_road_area = (10 <= cx <= 15) and (22 <= cz <= 24)
            if is_road_area:
                for s in root.get('sections', []):
                    y_sec = int(s.get('Y', -99))
                    if y_sec >= 4:
                        b = s.get('block_states', {})
                        pal = b.get('palette', [])
                        for p in pal:
                            name = str(p.get('Name', ''))
                            if name in road_materials:
                                p['Name'] = nbtlib.String('minecraft:air')
                                if 'Properties' in p:
                                    del p['Properties']

            # Remove block_entities in ship/camp area
            if is_ship_area or is_road_area:
                be_list = root.get('block_entities', [])
                new_be = []
                for be in be_list:
                    bx = int(be.get('x', 0))
                    bz = int(be.get('z', 0))
                    by = int(be.get('y', 0))
                    # keep only if not in ship or camp
                    if is_ship_area and (675 <= bx <= 715 and 430 <= bz <= 560 and by >= 63):
                        continue
                    if is_road_area and (686 <= bx <= 750 and 365 <= bz <= 385 and by >= 76):
                        continue
                    new_be.append(be)
                root['block_entities'] = nbtlib.List[nbtlib.Compound](new_be)

            chunks_dict[(cx, cz)] = chunk

    print(f"Barriers replaced: {barriers_cleared}, Ship palettes cleared: {ship_cleared}")

    # Re-encode MCA
    chunk_headers = bytearray(4096)
    out_timestamps = bytearray(4096)
    current_sector = 2
    body = bytearray()
    cur_time = int(time.time())

    for cz in range(32):
        for cx in range(32):
            idx = 4 * (cx + cz * 32)
            if (cx, cz) in chunks_dict:
                chunk_file = chunks_dict[(cx, cz)]
                bio = io.BytesIO()
                chunk_file.write(bio)
                raw_nbt = bio.getvalue()
                comp_data = zlib.compress(raw_nbt)

                length = 1 + len(comp_data)
                chunk_bytes = struct.pack('>I', length) + b'\x02' + comp_data

                sectors = (len(chunk_bytes) + 4095) // 4096
                padded_len = sectors * 4096
                chunk_bytes = chunk_bytes.ljust(padded_len, b'\x00')

                chunk_headers[idx:idx+3] = struct.pack('>I', current_sector)[1:]
                chunk_headers[idx+3] = sectors
                out_timestamps[idx:idx+4] = struct.pack('>I', cur_time)

                body.extend(chunk_bytes)
                current_sector += sectors
            else:
                chunk_headers[idx:idx+4] = b'\x00\x00\x00\x00'
                out_timestamps[idx:idx+4] = b'\x00\x00\x00\x00'

    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, 'wb') as f:
        f.write(chunk_headers)
        f.write(out_timestamps)
        f.write(body)
    print(f"Successfully saved clean MCA to {out_path} ({os.path.getsize(out_path)} bytes)")

def clean_save_directory(dim_dir, cleaned_r10_data):
    if not os.path.exists(dim_dir):
        return
    r_dir = os.path.join(dim_dir, 'region')
    e_dir = os.path.join(dim_dir, 'entities')
    if not os.path.exists(r_dir):
        return

    synthetic_prefixes = [
        'r.-1.', 'r.-2.', 'r.0.-1', 'r.0.-2', 'r.0.2', 'r.1.-1', 'r.1.-2',
        'r.1.1', 'r.1.2', 'r.2.', 'r.3.', 'r.4.-1', 'r.4.0', 'r.4.1'
    ]

    deleted = 0
    for d in [r_dir, e_dir]:
        if os.path.exists(d):
            for f in os.listdir(d):
                if any(f.startswith(p) for p in synthetic_prefixes):
                    try:
                        os.remove(os.path.join(d, f))
                        deleted += 1
                    except Exception:
                        pass

    print(f"Deleted {deleted} synthetic ocean MCA files from {dim_dir}")

    # Write cleaned r.1.0.mca
    r10_target = os.path.join(r_dir, 'r.1.0.mca')
    with open(r10_target, 'wb') as f:
        f.write(cleaned_r10_data)
    print(f"Updated r.1.0.mca in {r_dir}")

    # Remove structure marker so TeyvatWorldManager re-places cleanly
    marker = os.path.join(dim_dir, '.structures_placed_v2')
    if os.path.exists(marker):
        os.remove(marker)
        print(f"Removed {marker} to trigger clean re-placement")

def main():
    base_r10 = r'run/saves/Teyvat/dimensions/awakening_system/teyvat_world/region/r.1.0.mca'
    clean_r10_tmp = r'scratch/cleaned_r.1.0.mca'
    clean_r10_file(base_r10, clean_r10_tmp)

    with open(clean_r10_tmp, 'rb') as f:
        cleaned_r10_bytes = f.read()

    # 1. Clean run/saves/Teyvat
    clean_save_directory(r'run/saves/Teyvat/dimensions/awakening_system/teyvat_world', cleaned_r10_bytes)

    # 2. Clean run/saves/Mundo nuevo (9)
    clean_save_directory(r'run/saves/Mundo nuevo (9)/dimensions/awakening_system/teyvat_world', cleaned_r10_bytes)

    # 3. Clean CurseForge saves
    clean_save_directory(r'C:/Users/soybe/curseforge/minecraft/Instances/Awakening Monarchs Ascension/saves/Mundo nuevo (1)/dimensions/awakening_system/teyvat_world', cleaned_r10_bytes)
    clean_save_directory(r'C:/Users/soybe/curseforge/minecraft/Instances/Awakening Monarchs Ascension/saves/Mundo nuevo/dimensions/awakening_system/teyvat_world', cleaned_r10_bytes)

    # 4. Clean mondstadt_regions.zip (KEEP ONLY Mondstadt real regions r.0.0, r.0.1, r.1.0!)
    zip_path = r'src/main/resources/data/awakening_system/mondstadt_regions.zip'
    temp_zip = zip_path + '.temp'
    print(f"\nPurging synthetic ocean regions from {zip_path}...")
    allowed_regions = {'region/r.0.0.mca', 'region/r.0.1.mca', 'region/r.1.0.mca',
                       'entities/r.0.0.mca', 'entities/r.0.1.mca', 'entities/r.1.0.mca'}

    with zipfile.ZipFile(zip_path, 'r') as zin, zipfile.ZipFile(temp_zip, 'w', compression=zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            if item.filename in allowed_regions:
                if item.filename == 'region/r.1.0.mca':
                    zout.writestr('region/r.1.0.mca', cleaned_r10_bytes)
                    print("Injected cleaned r.1.0.mca into zip.")
                else:
                    zout.writestr(item, zin.read(item.filename))
            else:
                # Omit synthetic ocean files!
                pass

    os.replace(temp_zip, zip_path)
    print(f"Purged {zip_path} successfully! Only authentic Mondstadt regions remain.")

if __name__ == '__main__':
    main()
