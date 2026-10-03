import os, sys, zipfile, io, struct, zlib, time
import nbtlib

zip_path = r'C:/Users/soybe/Downloads/estructuras/Genshin+Impact+Sumeru+in+Minecraft+UNZIP+ME.zip'

CREDIT_KEYWORDS = [
    'skyblock', 'tyler', 'juli', 'tobyas', 'loftcreator', 'timongcraft',
    'jongliermeister', 'pietbauer', 'nohaxjustfly', 'resourcepack', 'builder'
]

def clean_block_entities(be_list):
    cleaned = []
    for be in be_list:
        be_id = str(be.get('id', '')).lower()
        x, y, z = int(be.get('x', 0)), int(be.get('y', 0)), int(be.get('z', 0))
        
        # Booth area at 3380..3395, 6285..6300
        if 3375 <= x <= 3395 and 6285 <= z <= 6300:
            continue
        # Far-away credits at 9325..9340
        if 9325 <= x <= 9345 and 1700 <= z <= 1730:
            continue
            
        # Check text in signs
        is_credit = False
        for k in ['front_text', 'back_text', 'Text1', 'Text2', 'Text3', 'Text4']:
            if k in be:
                t_str = str(be[k]).lower()
                if any(kw in t_str for kw in CREDIT_KEYWORDS):
                    is_credit = True
                    break
        if is_credit:
            continue
            
        # Check skull owner
        for k in ['SkullOwner', 'ExtraType']:
            if k in be:
                s_str = str(be[k]).lower()
                if any(kw in s_str for kw in CREDIT_KEYWORDS):
                    is_credit = True
                    break
        if is_credit:
            continue
            
        cleaned.append(be)
    return cleaned

def clean_booth_palette(b):
    if not b or 'palette' not in b:
        return
    dirty_names = [
        'player_head', 'player_wall_head', 'oak_sign', 'oak_wall_sign',
        'dark_oak_planks', 'dark_oak_stairs', 'dark_oak_slab', 'dark_oak_fence',
        'gold_block', 'quartz_bricks'
    ]
    new_pal = []
    for p in b['palette']:
        name = str(p.get('Name', ''))
        if any(d in name for d in dirty_names):
            new_pal.append(nbtlib.Compound({'Name': nbtlib.String('minecraft:air')}))
        else:
            new_pal.append(p)
    b['palette'] = nbtlib.List[nbtlib.Compound](new_pal)

def process_region_data(mca_bytes, is_r612=False):
    header = mca_bytes[:4096]
    timestamps = mca_bytes[4096:8192]
    chunks = {}
    
    for cz in range(32):
        for cx in range(32):
            idx = 4 * (cx + cz * 32)
            offset_bytes = header[idx:idx+4]
            offset = struct.unpack('>I', b'\x00' + offset_bytes[:3])[0] * 4096
            sectors = offset_bytes[3]
            if offset > 0 and sectors > 0:
                length = struct.unpack('>I', mca_bytes[offset:offset+4])[0]
                comp = mca_bytes[offset+4]
                raw = mca_bytes[offset+5:offset+4+length]
                if comp == 2:
                    decomp = zlib.decompress(raw)
                    chunk_file = nbtlib.File.from_fileobj(io.BytesIO(decomp))
                    root = chunk_file.get('', chunk_file)
                    
                    # 1. Fix leaf persistence in all sections
                    for s in root.get('sections', []):
                        b = s.get('block_states')
                        if b and 'palette' in b:
                            for p in b['palette']:
                                name = str(p.get('Name', ''))
                                if 'leaves' in name:
                                    props = p.get('Properties', nbtlib.Compound())
                                    props['persistent'] = nbtlib.String('true')
                                    p['Properties'] = props
                                    
                        # If r.6.12 booth chunk (cx=19, cz=9) and Y=1 (blocks 16..31)
                        if is_r612 and cx == 19 and cz == 9 and int(s.get('Y', -99)) == 1:
                            clean_booth_palette(b)
                            
                    # 2. Clean credit block entities
                    if 'block_entities' in root:
                        root['block_entities'] = nbtlib.List[nbtlib.Compound](clean_block_entities(root['block_entities']))
                        
                    chunks[(cx, cz)] = chunk_file
                    
    # Re-encode MCA
    chunk_headers = bytearray(4096)
    out_timestamps = bytearray(4096)
    current_sector = 2
    body = bytearray()
    cur_time = int(time.time())
    
    for cz in range(32):
        for cx in range(32):
            idx = 4 * (cx + cz * 32)
            if (cx, cz) in chunks:
                chunk_file = chunks[(cx, cz)]
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
                
    return bytes(chunk_headers) + bytes(out_timestamps) + bytes(body)

def deploy_all_sumeru():
    dest_dirs = [
        r'run/saves/Mundo nuevo (9)/dimensions/awakening_system/teyvat_world/region',
        r'C:/Users/soybe/curseforge/minecraft/Instances/Awakening Monarchs Ascension/saves/Mundo nuevo (1)/dimensions/awakening_system/teyvat_world/region',
        r'C:/Users/soybe/curseforge/minecraft/Instances/Awakening Monarchs Ascension/saves/Mundo nuevo/dimensions/awakening_system/teyvat_world/region'
    ]
    
    start = time.time()
    print("Opening Sumeru zip...")
    with zipfile.ZipFile(zip_path, 'r') as z:
        entries = [n for n in z.namelist() if 'Genshin Impact Sumeru - 1.20.6-1.21.1/region/' in n and n.endswith('.mca')]
        print(f"Found {len(entries)} regions to process.")
        
        for entry in entries:
            fname = os.path.basename(entry)
            # Protect Mondstadt regions
            if fname in ['r.0.0.mca', 'r.0.1.mca', 'r.1.0.mca', 'r.1.1.mca']:
                continue
                
            is_r612 = (fname == 'r.6.12.mca')
            print(f"Processing {fname} (leaf persistence + credit removal)...")
            raw_mca = z.read(entry)
            if len(raw_mca) < 8192:
                print(f"Skipping empty {fname} ({len(raw_mca)} bytes)...")
                continue
            print(f"Processing {fname} (leaf persistence + credit removal)...")
            processed_mca = process_region_data(raw_mca, is_r612=is_r612)
            
            for d in dest_dirs:
                if os.path.exists(os.path.dirname(d)): # if save exists
                    os.makedirs(d, exist_ok=True)
                    out_path = os.path.join(d, fname)
                    with open(out_path, 'wb') as out_f:
                        out_f.write(processed_mca)
                        
    print(f"All Sumeru regions processed and deployed in {time.time() - start:.2f}s!")

if __name__ == '__main__':
    deploy_all_sumeru()
