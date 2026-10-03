import os
import urllib.request
import re
from PIL import Image

def fetch_and_restore_genshin_foods():
    # 1. Restore Adeptus Temptation directly from PrimogemCraft
    pgc_adeptus = "c:/Users/soybe/Desktop/Mods NeoForge/PrimogemCraft-main/src/main/resources/assets/primogemcraft/textures/item/xian_tiao_qiang_.png"
    target_adeptus = "src/main/resources/assets/awakening_system/textures/item/adeptus_temptation.png"
    if os.path.exists(pgc_adeptus):
        im = Image.open(pgc_adeptus)
        im.save(target_adeptus)
        print(f"Restored adeptus_temptation from PrimogemCraft: {im.size} {im.mode}")
    elif os.path.exists("scratch/primogem_textures/adeptus_temptation.png"):
        im = Image.open("scratch/primogem_textures/adeptus_temptation.png")
        im.save(target_adeptus)
        print(f"Restored adeptus_temptation from scratch/primogem_textures: {im.size}")

    # 2. Restore Sweet Madame from authentic Genshin asset in scratch/genshin_food
    src_sweet = "scratch/genshin_food/Item_Sweet_Madame.png"
    target_sweet = "src/main/resources/assets/awakening_system/textures/item/sweet_madame.png"
    if os.path.exists(src_sweet):
        im_sw = Image.open(src_sweet).convert("RGBA")
        im_sw = im_sw.resize((64, 64), Image.Resampling.LANCZOS)
        im_sw.save(target_sweet)
        print(f"Restored sweet_madame from authentic Genshin asset: {im_sw.size}")

    # 3. Restore Almond Tofu from Genshin Wiki
    try:
        url = 'https://genshin-impact.fandom.com/wiki/File:Item_Almond_Tofu.png'
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        html = urllib.request.urlopen(req, timeout=10).read().decode('utf-8', errors='ignore')
        m = re.search(r'href="(https://static\.wikia\.nocookie\.net/gensin-impact/images/[^"]+Item_Almond_Tofu\.png[^"]*)"', html)
        if m:
            img_url = m.group(1).split('/revision/')[0]
            print(f"Downloading Almond Tofu from: {img_url}")
            img_data = urllib.request.urlopen(urllib.request.Request(img_url, headers={'User-Agent': 'Mozilla/5.0'}), timeout=10).read()
            with open("scratch/genshin_food/Item_Almond_Tofu.png", "wb") as f:
                f.write(img_data)
            im_tofu = Image.open("scratch/genshin_food/Item_Almond_Tofu.png").convert("RGBA")
            im_tofu = im_tofu.resize((64, 64), Image.Resampling.LANCZOS)
            im_tofu.save("src/main/resources/assets/awakening_system/textures/item/almond_tofu.png")
            print("Restored almond_tofu from authentic Genshin asset!")
    except Exception as e:
        print(f"Could not download Almond Tofu online ({e})")

    # 4. Restore Sunsettia from Genshin Wiki if needed
    try:
        url_s = 'https://genshin-impact.fandom.com/wiki/File:Item_Sunsettia.png'
        req = urllib.request.Request(url_s, headers={'User-Agent': 'Mozilla/5.0'})
        html_s = urllib.request.urlopen(req, timeout=10).read().decode('utf-8', errors='ignore')
        m_s = re.search(r'href="(https://static\.wikia\.nocookie\.net/gensin-impact/images/[^"]+Item_Sunsettia\.png[^"]*)"', html_s)
        if m_s:
            img_url_s = m_s.group(1).split('/revision/')[0]
            print(f"Downloading Sunsettia from: {img_url_s}")
            img_data_s = urllib.request.urlopen(urllib.request.Request(img_url_s, headers={'User-Agent': 'Mozilla/5.0'}), timeout=10).read()
            with open("scratch/genshin_food/Item_Sunsettia.png", "wb") as f:
                f.write(img_data_s)
            im_sun = Image.open("scratch/genshin_food/Item_Sunsettia.png").convert("RGBA")
            im_sun = im_sun.resize((64, 64), Image.Resampling.LANCZOS)
            im_sun.save("src/main/resources/assets/awakening_system/textures/item/sunsettia.png")
            print("Restored sunsettia from authentic Genshin asset!")
    except Exception as e:
        print(f"Could not download Sunsettia online ({e})")

if __name__ == "__main__":
    fetch_and_restore_genshin_foods()
