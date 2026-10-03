# -*- coding: utf-8 -*-
"""
Extract and integrate Sumeru textures, models, font, map art, and Dawn Winery Timberframe resource pack.
"""
import os
import sys
import io
import zipfile
import shutil

sys.stdout.reconfigure(encoding='utf-8')

DOWNLOADS_DIR = r"C:\Users\soybe\Downloads\estructuras"
SUMERU_ZIP = os.path.join(DOWNLOADS_DIR, "Genshin+Impact+Sumeru+in+Minecraft+UNZIP+ME.zip")
WINERY_ZIP = os.path.join(DOWNLOADS_DIR, "DawnWineryUNPACK.zip")

MOD_ASSETS_DIR = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\assets"
CURSEFORGE_RP_DIR = r"C:\Users\soybe\curseforge\minecraft\Instances\Awakening Monarchs Ascension\resourcepacks"
os.makedirs(CURSEFORGE_RP_DIR, exist_ok=True)

WORLD_SAVES = [
    r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\run\saves\Mundo nuevo (9)",
    r"C:\Users\soybe\curseforge\minecraft\Instances\Awakening Monarchs Ascension\saves\Mundo nuevo (1)",
    r"C:\Users\soybe\curseforge\minecraft\Instances\Awakening Monarchs Ascension\saves\Mundo nuevo"
]

def integrate_sumeru_resources():
    print("=== Integrating Sumeru Textures & Resource Pack ===")
    if not os.path.exists(SUMERU_ZIP):
        print("Sumeru zip not found!")
        return

    with zipfile.ZipFile(SUMERU_ZIP, 'r') as z:
        res_bytes = z.read('Genshin Impact Sumeru - 1.20.6-1.21.1/resources.zip')

        # 1. Save Sumeru_Resources.zip in CurseForge resourcepacks
        rp_path = os.path.join(CURSEFORGE_RP_DIR, "Sumeru_Resources.zip")
        with open(rp_path, 'wb') as f:
            f.write(res_bytes)
        print(f"Saved {rp_path}")

        # 2. Extract into each active world save as resources.zip
        for ws in WORLD_SAVES:
            if os.path.exists(ws):
                save_res = os.path.join(ws, "resources.zip")
                with open(save_res, 'wb') as f:
                    f.write(res_bytes)
                print(f"Installed world resources.zip in {ws}")

        # 3. Extract into mod assets folder directly
        with zipfile.ZipFile(io.BytesIO(res_bytes)) as rz:
            for entry in rz.namelist():
                if entry.startswith('assets/'):
                    # relative path after 'assets/'
                    rel = entry[len('assets/'):]
                    target_file = os.path.join(MOD_ASSETS_DIR, rel)
                    os.makedirs(os.path.dirname(target_file), exist_ok=True)
                    if not entry.endswith('/'):
                        with open(target_file, 'wb') as f:
                            f.write(rz.read(entry))
                        print(f"  Extracted into mod assets: {rel}")

        # 4. Copy map paintings (data/map_*.dat) into world saves
        map_entries = [
            n for n in z.namelist() 
            if 'Genshin Impact Sumeru - 1.20.6-1.21.1/data/' in n and n.endswith('.dat')
        ]
        print(f"Found {len(map_entries)} data/map files in Sumeru world.")
        for ws in WORLD_SAVES:
            if os.path.exists(ws):
                data_dir = os.path.join(ws, "data")
                os.makedirs(data_dir, exist_ok=True)
                for me in map_entries:
                    fname = os.path.basename(me)
                    target = os.path.join(data_dir, fname)
                    with open(target, 'wb') as f:
                        f.write(z.read(me))
                print(f"Copied {len(map_entries)} map art files to {data_dir}")

def integrate_winery_resourcepack():
    print("\n=== Integrating Dawn Winery Timberframe Resource Pack ===")
    if not os.path.exists(WINERY_ZIP):
        print("Winery zip not found!")
        return

    with zipfile.ZipFile(WINERY_ZIP, 'r') as z:
        timber_entries = [n for n in z.namelist() if n.startswith('Timberframe Resourcepack/')]
        if not timber_entries:
            print("No Timberframe Resourcepack found in Winery zip.")
            return

        rp_path = os.path.join(CURSEFORGE_RP_DIR, "Dawn_Winery_Timberframe.zip")
        with zipfile.ZipFile(rp_path, 'w', compression=zipfile.ZIP_DEFLATED) as out_zip:
            prefix = 'Timberframe Resourcepack/'
            for entry in timber_entries:
                if entry.endswith('/'):
                    continue
                rel_name = entry[len(prefix):]
                data = z.read(entry)
                out_zip.writestr(rel_name, data)
        print(f"Saved {rp_path} ({os.path.getsize(rp_path) / 1024:.1f} KB)")

if __name__ == '__main__':
    integrate_sumeru_resources()
    integrate_winery_resourcepack()
    print("\nAll textures, models, maps, and resource packs successfully integrated!")
