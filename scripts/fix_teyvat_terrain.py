import os, sys, math, time, struct, zlib, io, random, zipfile
import nbtlib

def pack_subchunk(blocks_4096, unique_names):
    name_to_idx = {name: i for i, name in enumerate(unique_names)}
    palette_comp = [nbtlib.Compound({'Name': nbtlib.String(name)}) for name in unique_names]
    
    if len(unique_names) == 1:
        return nbtlib.Compound({
            'palette': nbtlib.List[nbtlib.Compound](palette_comp)
        })
    
    bits = max(4, (len(unique_names) - 1).bit_length())
    entries_per_long = 64 // bits
    total_longs = (4096 + entries_per_long - 1) // entries_per_long
    
    longs = [0] * total_longs
    for i, b_name in enumerate(blocks_4096):
        b_idx = name_to_idx[b_name]
        long_idx = i // entries_per_long
        bit_offset = (i % entries_per_long) * bits
        longs[long_idx] |= (b_idx & ((1 << bits) - 1)) << bit_offset
        
    signed_longs = [val if val < (1 << 63) else val - (1 << 64) for val in longs]
    
    return nbtlib.Compound({
        'palette': nbtlib.List[nbtlib.Compound](palette_comp),
        'data': nbtlib.LongArray(signed_longs)
    })

def read_mca(mca_path):
    with open(mca_path, 'rb') as f:
        data = f.read()
    header = data[:4096]
    timestamps = data[4096:8192]
    chunks = {}
    for cz in range(32):
        for cx in range(32):
            idx = 4 * (cx + cz * 32)
            offset_bytes = header[idx:idx+4]
            offset = struct.unpack('>I', b'\x00' + offset_bytes[:3])[0] * 4096
            sectors = offset_bytes[3]
            if offset > 0 and sectors > 0:
                length = struct.unpack('>I', data[offset:offset+4])[0]
                comp = data[offset+4]
                raw = data[offset+5:offset+4+length]
                if comp == 2:
                    decomp = zlib.decompress(raw)
                    chunks[(cx, cz)] = nbtlib.File.from_fileobj(io.BytesIO(decomp))
    return chunks

def write_mca(chunks_dict, out_path):
    chunk_headers = bytearray(4096)
    timestamps = bytearray(4096)
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
                timestamps[idx:idx+4] = struct.pack('>I', cur_time)
                
                body.extend(chunk_bytes)
                current_sector += sectors
            else:
                chunk_headers[idx:idx+4] = b'\x00\x00\x00\x00'
                timestamps[idx:idx+4] = b'\x00\x00\x00\x00'
                
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, 'wb') as f:
        f.write(chunk_headers)
        f.write(timestamps)
        f.write(body)

def generate_levantaviento_sections(cx, cz):
    # 3D block array for Y=48..95 (sections 3, 4, 5) -> 48 slices of 16x16
    blocks_345 = [[['minecraft:air' for _ in range(16)] for _ in range(16)] for _ in range(48)]
    
    # Store surface Y for tree placement
    surface_map = [[63 for _ in range(16)] for _ in range(16)]
    is_land_map = [[False for _ in range(16)] for _ in range(16)]
    
    for lz in range(16):
        z = cz * 16 + lz
        dz = z - 379
        # Natural winding coast of Cider Lake
        shore_x = int(684.0 + 0.003 * (dz * dz * 0.5) + 14.0 * math.sin(dz * 0.045) + 7.0 * math.cos(dz * 0.085) - 7.0)
        
        for lx in range(16):
            x = 512 + cx * 16 + lx
            
            if x < shore_x:
                # Cider Lake Water
                d_lake = shore_x - x
                if d_lake == 1:
                    bed_y = 61; bed_block = 'minecraft:sand'
                elif d_lake == 2:
                    bed_y = 59; bed_block = 'minecraft:sand'
                elif d_lake == 3:
                    bed_y = 57; bed_block = 'minecraft:gravel'
                else:
                    bed_y = 54; bed_block = 'minecraft:dirt'
                    
                for y in range(48, bed_y):
                    blocks_345[y - 48][lz][lx] = 'minecraft:stone'
                blocks_345[bed_y - 48][lz][lx] = bed_block
                for y in range(bed_y + 1, 64):
                    blocks_345[y - 48][lz][lx] = 'minecraft:water'
                for y in range(64, 96):
                    blocks_345[y - 48][lz][lx] = 'minecraft:air'
            else:
                # Levantaviento Mainland
                is_land_map[lz][lx] = True
                d = x - shore_x
                h = 72.0 + 5.0 * math.sin(x * 0.025 + z * 0.03) + 3.5 * math.cos(x * 0.04 - z * 0.02) + 2.0 * math.sin((x + z) * 0.05)
                
                # Near road
                if abs(dz) <= 3:
                    target_y = 77 if x < 725 else int(h)
                else:
                    slope = min(1.0, d / 6.0)
                    target_y = int(63.0 + (h - 63.0) * slope)
                    target_y = max(63, target_y)
                    
                target_y = min(90, target_y)
                surface_map[lz][lx] = target_y
                
                # Stone under target_y - 3
                for y in range(48, target_y - 3):
                    blocks_345[y - 48][lz][lx] = 'minecraft:stone'
                # Dirt
                for y in range(max(48, target_y - 3), target_y):
                    blocks_345[y - 48][lz][lx] = 'minecraft:dirt'
                    
                # Top block
                if d <= 2 and target_y <= 65:
                    top_block = 'minecraft:sand'
                elif abs(dz) <= 2 and d > 2:
                    top_block = 'minecraft:dirt_path'
                else:
                    top_block = 'minecraft:grass_block'
                blocks_345[target_y - 48][lz][lx] = top_block
                
                # Wildflowers on grass
                if top_block == 'minecraft:grass_block' and target_y + 1 < 95:
                    hseed = (x * 37 + z * 53) & 0x7FFFFFFF
                    if hseed % 23 == 0:
                        blocks_345[target_y + 1 - 48][lz][lx] = 'minecraft:poppy'
                    elif hseed % 29 == 0:
                        blocks_345[target_y + 1 - 48][lz][lx] = 'minecraft:dandelion'
                    elif hseed % 31 == 0:
                        blocks_345[target_y + 1 - 48][lz][lx] = 'minecraft:cornflower'
                    elif hseed % 37 == 0:
                        blocks_345[target_y + 1 - 48][lz][lx] = 'minecraft:azure_bluet'
                    elif hseed % 7 == 0:
                        blocks_345[target_y + 1 - 48][lz][lx] = 'minecraft:short_grass'

    # Oak trees placement within chunk interior (lx: 3..12, lz: 3..12)
    for lz in range(3, 13):
        z = cz * 16 + lz
        dz = z - 379
        if abs(dz) <= 6:
            continue # keep road clear
        for lx in range(3, 13):
            x = 512 + cx * 16 + lx
            if not is_land_map[lz][lx]:
                continue
            sy = surface_map[lz][lx]
            # check distance from shore
            shore_x = int(684.0 + 0.003 * (dz * dz * 0.5) + 14.0 * math.sin(dz * 0.045) + 7.0 * math.cos(dz * 0.085) - 7.0)
            if x < shore_x + 8:
                continue
            # tree seed
            if ((x * 19 + z * 41) & 0x7FFFFFFF) % 113 == 0:
                # Plant oak tree
                trunk_h = 5
                for ty in range(1, trunk_h + 1):
                    if sy + ty - 48 < 48:
                        blocks_345[sy + ty - 48][lz][lx] = 'minecraft:oak_log'
                # Leaves canopy
                for ox in range(-2, 3):
                    for oz in range(-2, 3):
                        if abs(ox) == 2 and abs(oz) == 2:
                            continue
                        for oy in range(trunk_h - 2, trunk_h):
                            py = sy + oy - 48
                            if 0 <= py < 48 and blocks_345[py][lz + oz][lx + ox] == 'minecraft:air':
                                blocks_345[py][lz + oz][lx + ox] = 'minecraft:oak_leaves'
                for ox in range(-1, 2):
                    for oz in range(-1, 2):
                        py = sy + trunk_h - 48
                        if 0 <= py < 48 and blocks_345[py][lz + oz][lx + ox] == 'minecraft:air':
                            blocks_345[py][lz + oz][lx + ox] = 'minecraft:oak_leaves'
                if sy + trunk_h + 1 - 48 < 48:
                    blocks_345[sy + trunk_h + 1 - 48][lz][lx] = 'minecraft:oak_leaves'

    # Build 24 sections
    deepslate_comp = pack_subchunk(['minecraft:deepslate'], ['minecraft:deepslate'])
    stone_comp = pack_subchunk(['minecraft:stone'], ['minecraft:stone'])
    air_comp = pack_subchunk(['minecraft:air'], ['minecraft:air'])
    windrise_biome = nbtlib.Compound({
        'palette': nbtlib.List[nbtlib.String]([nbtlib.String('awakening_system:teyvat_windrise_plains')])
    })
    
    sections_list = []
    for sec_y in range(-4, 20):
        sec_tag = nbtlib.Compound({'Y': nbtlib.Byte(sec_y)})
        sec_tag['biomes'] = windrise_biome
        
        if sec_y < 0:
            sec_tag['block_states'] = deepslate_comp
        elif sec_y < 3:
            sec_tag['block_states'] = stone_comp
        elif sec_y in [3, 4, 5]:
            base_y = (sec_y - 3) * 16
            sec_blocks = []
            for ly in range(16):
                for lz in range(16):
                    for lx in range(16):
                        sec_blocks.append(blocks_345[base_y + ly][lz][lx])
            unique_names = list(dict.fromkeys(sec_blocks))
            sec_tag['block_states'] = pack_subchunk(sec_blocks, unique_names)
        else:
            sec_tag['block_states'] = air_comp
            
        sections_list.append(sec_tag)
        
    return sections_list

def fix_r10_file(in_mca_path, out_mca_path):
    print(f"Reading {in_mca_path}...")
    chunks = read_mca(in_mca_path)
    print(f"Loaded {len(chunks)} chunks from r.1.0.mca.")
    
    modified = 0
    # Process all chunks where cx >= 10 (X >= 672)
    for cz in range(32):
        for cx in range(10, 32):
            if (cx, cz) in chunks:
                chunk_file = chunks[(cx, cz)]
                root = chunk_file.get('', chunk_file)
            else:
                # Create brand new chunk
                root = nbtlib.Compound({
                    'DataVersion': nbtlib.Int(3955),
                    'Status': nbtlib.String('minecraft:full'),
                    'xPos': nbtlib.Int(32 + cx),
                    'zPos': nbtlib.Int(cz),
                    'yPos': nbtlib.Int(-4),
                    'isLightOn': nbtlib.Byte(1)
                })
                chunk_file = nbtlib.File({'': root})
                chunks[(cx, cz)] = chunk_file
                
            root['DataVersion'] = nbtlib.Int(3955)
            root['Status'] = nbtlib.String('minecraft:full')
            root['yPos'] = nbtlib.Int(-4)
            root['xPos'] = nbtlib.Int(32 + cx)
            root['zPos'] = nbtlib.Int(cz)
            root['sections'] = nbtlib.List[nbtlib.Compound](generate_levantaviento_sections(cx, cz))
            modified += 1
            
    print(f"Sculpted {modified} chunks in Levantaviento continuous continent.")
    print(f"Writing updated region to {out_mca_path}...")
    write_mca(chunks, out_mca_path)
    print("Done r.1.0.mca!")

def clean_sumeru_booth(in_mca_path, out_mca_path):
    print(f"Checking Sumeru booth in {in_mca_path}...")
    chunks = read_mca(in_mca_path)
    # Booth at 3385, 20, 6294
    # cx = (3385 - 6*512)//16 = 19, cz = (6294 - 12*512)//16 = 9
    # check surrounding chunks cx: 18..20, cz: 8..10
    cleared = 0
    for cx in range(18, 21):
        for cz in range(8, 11):
            if (cx, cz) not in chunks:
                continue
            chunk_file = chunks[(cx, cz)]
            root = chunk_file.get('', chunk_file)
            sections = root.get('sections', [])
            for s in sections:
                if int(s.get('Y', -99)) == 1: # Y = 16..31
                    b = s.get('block_states')
                    if not b or 'palette' not in b:
                        continue
                    pal = [p.get('Name') for p in b['palette']]
                    dirty_names = ['player_head', 'player_wall_head', 'oak_sign', 'oak_wall_sign', 'dark_oak_planks', 'dark_oak_stairs', 'dark_oak_slab', 'gold_block', 'quartz_bricks', 'chest']
                    has_dirty = any(any(d in p for d in dirty_names) for p in pal)
                    if has_dirty:
                        # Clear dirty blocks from palette and replace with air or moss_block
                        new_palette = []
                        for p in b['palette']:
                            name = str(p.get('Name', ''))
                            if any(d in name for d in dirty_names):
                                new_palette.append(nbtlib.Compound({'Name': nbtlib.String('minecraft:air')}))
                            else:
                                new_palette.append(p)
                        b['palette'] = nbtlib.List[nbtlib.Compound](new_palette)
                        cleared += 1
                        
    print(f"Cleaned {cleared} sections with creator booth credits in Sumeru.")
    write_mca(chunks, out_mca_path)
    print("Done cleaning Sumeru booth!")

if __name__ == '__main__':
    base_r10 = r'run/saves/Mundo nuevo (9)/dimensions/awakening_system/teyvat_world/region/r.1.0.mca'
    base_r612 = r'run/saves/Mundo nuevo (9)/dimensions/awakening_system/teyvat_world/region/r.6.12.mca'
    
    # 1. Fix save 9
    fix_r10_file(base_r10, base_r10)
    clean_sumeru_booth(base_r612, base_r612)
    
    # 2. Fix CurseForge saves
    cf_saves = [
        r'C:/Users/soybe/curseforge/minecraft/Instances/Awakening Monarchs Ascension/saves/Mundo nuevo (1)/dimensions/awakening_system/teyvat_world/region',
        r'C:/Users/soybe/curseforge/minecraft/Instances/Awakening Monarchs Ascension/saves/Mundo nuevo/dimensions/awakening_system/teyvat_world/region'
    ]
    for cfs in cf_saves:
        if os.path.exists(cfs):
            fix_r10_file(base_r10, os.path.join(cfs, 'r.1.0.mca'))
            clean_sumeru_booth(base_r612, os.path.join(cfs, 'r.6.12.mca'))
            
    # 3. Update mondstadt_regions.zip
    zip_path = r'src/main/resources/data/awakening_system/mondstadt_regions.zip'
    print(f"Updating {zip_path} with perfected r.1.0.mca...")
    with open(base_r10, 'rb') as f:
        r10_data = f.read()
        
    temp_zip = zip_path + '.tmp'
    with zipfile.ZipFile(zip_path, 'r') as zin, zipfile.ZipFile(temp_zip, 'w', compression=zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            if item.filename == 'region/r.1.0.mca':
                zout.writestr('region/r.1.0.mca', r10_data)
            else:
                zout.writestr(item, zin.read(item.filename))
                
    os.replace(temp_zip, zip_path)
    print("All regions and zip successfully updated!")
