import os, sys, io, zlib, struct, zipfile
import nbtlib

def clean_sky_and_bridge_in_region(mca_path):
    if not os.path.exists(mca_path):
        return
    print(f"Processing region file: {mca_path}")
    with open(mca_path, 'rb') as f:
        data = f.read()
    if len(data) < 8192:
        return
    header = bytearray(data[:4096])
    timestamps = bytearray(data[4096:8192])

    modified_chunks = {}
    chunks_modified_count = 0

    # Materials to clear from Beidou ship remnants on the hill
    ship_materials = {
        'minecraft:dark_oak_planks', 'minecraft:spruce_planks', 'minecraft:oak_planks',
        'minecraft:stripped_dark_oak_wood', 'minecraft:stripped_dark_oak_log',
        'minecraft:stripped_spruce_wood', 'minecraft:stripped_spruce_log',
        'minecraft:dark_oak_fence', 'minecraft:spruce_fence', 'minecraft:oak_fence',
        'minecraft:dark_oak_fence_gate', 'minecraft:spruce_fence_gate',
        'minecraft:dark_oak_stairs', 'minecraft:spruce_stairs', 'minecraft:oak_stairs',
        'minecraft:dark_oak_slab', 'minecraft:spruce_slab', 'minecraft:oak_slab',
        'minecraft:dark_oak_trapdoor', 'minecraft:spruce_trapdoor', 'minecraft:oak_trapdoor',
        'minecraft:yellow_wool', 'minecraft:red_wool', 'minecraft:white_wool', 'minecraft:gray_wool',
        'minecraft:yellow_terracotta', 'minecraft:red_terracotta', 'minecraft:yellow_concrete', 'minecraft:red_concrete',
        'minecraft:iron_bars', 'minecraft:chain', 'minecraft:end_rod', 'minecraft:lantern'
    }

    # Materials from artificial portal / shrine / camp on the bridge
    bridge_artificial = {
        'minecraft:quartz_bricks', 'minecraft:chiseled_quartz_block', 'minecraft:sea_lantern',
        'minecraft:gold_block', 'awakening_system:tower_portal', 'awakening_system:ascension_portal',
        'minecraft:blue_banner', 'minecraft:campfire', 'minecraft:barrel', 'minecraft:light_blue_wool',
        'minecraft:command_block', 'minecraft:chain_command_block', 'minecraft:repeating_command_block'
    }

    for cz_rel in range(32):
        for cx_rel in range(32):
            idx = 4 * (cx_rel + cz_rel * 32)
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
            cx = int(root.get('xPos', 0))
            cz = int(root.get('zPos', 0))

            chunk_changed = False

            # 1. Clear ALL sky blocks above Y=120 over Mondstadt (wipes BOTH floating Jade Chambers)
            # Mondstadt sky bounding box in chunks: cx in [32..45], cz in [0..30]
            if 32 <= cx <= 45 and 0 <= cz <= 30:
                for s in root.get('sections', []):
                    y_sec = int(s.get('Y', -99))
                    if y_sec >= 8: # Y >= 128 (Sky level)
                        b = s.get('block_states', {})
                        pal = b.get('palette', [])
                        if pal:
                            # Replace palette with single air block and empty data
                            b['palette'] = nbtlib.List([nbtlib.Compound({'Name': nbtlib.String('minecraft:air')})])
                            if 'data' in b:
                                del b['data']
                            chunk_changed = True

            # 2. Clear misplaced Beidou ship remnants on the hill (cx=41..45, cz=26..31, Y >= 64)
            if 41 <= cx <= 45 and 26 <= cz <= 31:
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
                                chunk_changed = True

            # 3. Clear artificial shrine, quartz portal, banners, and sea lanterns on the bridge (cx=40..42, cz=22..24)
            if 40 <= cx <= 42 and 22 <= cz <= 24:
                for s in root.get('sections', []):
                    y_sec = int(s.get('Y', -99))
                    if y_sec in [4, 5]: # Y=64..95
                        b = s.get('block_states', {})
                        pal = b.get('palette', [])
                        for p in pal:
                            name = str(p.get('Name', ''))
                            if name in bridge_artificial:
                                # Replace floor level quartz/gold with clean stone bricks, portal/lanterns with air
                                p['Name'] = nbtlib.String('minecraft:stone_bricks' if 'quartz' in name or 'gold' in name else 'minecraft:air')
                                if 'Properties' in p:
                                    del p['Properties']
                                chunk_changed = True
                            elif name == 'minecraft:barrier':
                                p['Name'] = nbtlib.String('minecraft:air')
                                if 'Properties' in p:
                                    del p['Properties']
                                chunk_changed = True

                # Remove block entities on the bridge (banners, signs)
                be_list = root.get('block_entities', [])
                orig_be_len = len(be_list)
                filtered_be = []
                for be in be_list:
                    bx = int(be.get('x', 0))
                    bz = int(be.get('z', 0))
                    by = int(be.get('y', 0))
                    if 645 <= bx <= 680 and 365 <= bz <= 385:
                        continue # discard bridge banner/shrine BEs
                    filtered_be.append(be)
                if len(filtered_be) != orig_be_len:
                    root['block_entities'] = nbtlib.List(filtered_be)
                    chunk_changed = True

            if chunk_changed:
                chunks_modified_count += 1
                bio = io.BytesIO()
                chunk.write(bio)
                compressed = zlib.compress(bio.getvalue())
                length = len(compressed) + 1
                c_bytes = struct.pack('>IB', length, 2) + compressed
                pad = (4096 - (len(c_bytes) % 4096)) % 4096
                c_bytes += b'\x00' * pad
                modified_chunks[(cx_rel, cz_rel)] = c_bytes
            else:
                raw_chunk = data[off:off+4+length]
                pad = (4096 - (len(raw_chunk) % 4096)) % 4096
                modified_chunks[(cx_rel, cz_rel)] = raw_chunk + (b'\x00' * pad)

    if chunks_modified_count > 0:
        print(f"Writing {chunks_modified_count} modified chunks to {mca_path}...")
        cur_sec = 2
        new_data = bytearray(8192)
        for cz_rel in range(32):
            for cx_rel in range(32):
                idx = 4 * (cx_rel + cz_rel * 32)
                c_bytes = modified_chunks.get((cx_rel, cz_rel))
                if c_bytes is None:
                    continue
                sec_count = len(c_bytes) // 4096
                header[idx:idx+3] = struct.pack('>I', cur_sec)[1:]
                header[idx+3] = sec_count
                new_data += c_bytes
                cur_sec += sec_count
        new_data[:4096] = header
        new_data[4096:8192] = timestamps
        with open(mca_path, 'wb') as f:
            f.write(new_data)
        print(f"Successfully cleaned and updated {mca_path}!")

def clean_entities_file(mca_path):
    if not os.path.exists(mca_path):
        return
    print(f"Cleaning entities in: {mca_path}")
    with open(mca_path, 'rb') as f:
        data = f.read()
    if len(data) < 8192:
        return
    header = bytearray(data[:4096])
    timestamps = bytearray(data[4096:8192])

    modified_chunks = {}
    chunks_modified = 0

    for cz_rel in range(32):
        for cx_rel in range(32):
            idx = 4 * (cx_rel + cz_rel * 32)
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
            ents = root.get('Entities', [])
            orig_len = len(ents)

            # Filter out all item_frames, paintings, and markers around the bridge (X: 640..700, Z: 360..400)
            filtered = []
            for e in ents:
                eid = str(e.get('id', ''))
                pos = e.get('Pos', [])
                if len(pos) >= 3:
                    ex = float(pos[0])
                    ey = float(pos[1])
                    ez = float(pos[2])
                    # If in bridge / entrance area, remove item_frame, painting, marker, dropped items
                    if 640 <= ex <= 700 and 360 <= ez <= 400 and 60 <= ey <= 95:
                        if eid in ['minecraft:item_frame', 'minecraft:glow_item_frame', 'minecraft:painting', 'minecraft:marker', 'minecraft:item']:
                            continue
                filtered.append(e)

            if len(filtered) != orig_len:
                chunks_modified += 1
                root['Entities'] = nbtlib.List(filtered)
                bio = io.BytesIO()
                chunk.write(bio)
                compressed = zlib.compress(bio.getvalue())
                c_bytes = struct.pack('>IB', len(compressed) + 1, 2) + compressed
                pad = (4096 - (len(c_bytes) % 4096)) % 4096
                modified_chunks[(cx_rel, cz_rel)] = c_bytes + (b'\x00' * pad)
            else:
                raw_chunk = data[off:off+4+length]
                pad = (4096 - (len(raw_chunk) % 4096)) % 4096
                modified_chunks[(cx_rel, cz_rel)] = raw_chunk + (b'\x00' * pad)

    if chunks_modified > 0:
        cur_sec = 2
        new_data = bytearray(8192)
        for cz_rel in range(32):
            for cx_rel in range(32):
                idx = 4 * (cx_rel + cz_rel * 32)
                c_bytes = modified_chunks.get((cx_rel, cz_rel))
                if c_bytes is None:
                    continue
                sec_count = len(c_bytes) // 4096
                header[idx:idx+3] = struct.pack('>I', cur_sec)[1:]
                header[idx+3] = sec_count
                new_data += c_bytes
                cur_sec += sec_count
        new_data[:4096] = header
        new_data[4096:8192] = timestamps
        with open(mca_path, 'wb') as f:
            f.write(new_data)
        print(f"Successfully cleaned {chunks_modified} entity chunks in {mca_path}!")

def process_all_targets():
    dirs = [
        "run/saves/Teyvat/dimensions/awakening_system/teyvat_world",
        "run/saves/Mundo nuevo (9)/dimensions/awakening_system/teyvat_world",
        "C:/Users/soybe/curseforge/minecraft/Instances/Awakening Monarchs Ascension/saves/Mundo nuevo/dimensions/awakening_system/teyvat_world",
        "C:/Users/soybe/curseforge/minecraft/Instances/Awakening Monarchs Ascension/saves/Mundo nuevo (1)/dimensions/awakening_system/teyvat_world"
    ]

    for d in dirs:
        if os.path.exists(d):
            r10 = os.path.join(d, "region/r.1.0.mca")
            clean_sky_and_bridge_in_region(r10)
            e10 = os.path.join(d, "entities/r.1.0.mca")
            clean_entities_file(e10)
            # Also remove structure placement marker so structures regenerate cleanly
            marker = os.path.join(d, ".structures_placed_v2")
            if os.path.exists(marker):
                os.remove(marker)
                print(f"Removed marker: {marker}")

    # Also update the mod resource zip
    zip_path = "src/main/resources/data/awakening_system/mondstadt_regions.zip"
    if os.path.exists(zip_path):
        print(f"Updating mod zip: {zip_path}")
        tmp_dir = "scripts/tmp_zip_clean"
        os.makedirs(tmp_dir, exist_ok=True)
        with zipfile.ZipFile(zip_path, 'r') as z:
            z.extractall(tmp_dir)
        
        clean_sky_and_bridge_in_region(os.path.join(tmp_dir, "region/r.1.0.mca"))
        clean_entities_file(os.path.join(tmp_dir, "entities/r.1.0.mca"))

        # Re-pack zip
        with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as z:
            for root_dir, _, files in os.walk(tmp_dir):
                for file in files:
                    full_p = os.path.join(root_dir, file)
                    rel_p = os.path.relpath(full_p, tmp_dir).replace('\\', '/')
                    z.write(full_p, rel_p)
        print("Mod resource zip successfully updated!")

if __name__ == '__main__':
    process_all_targets()
