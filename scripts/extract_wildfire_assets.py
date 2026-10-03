import zipfile
import os
import shutil

zip_path = r"C:\Users\soybe\Downloads\friends-and-foes-master.zip"
target_dir = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema"

with zipfile.ZipFile(zip_path, 'r') as z:
    # 1. Textures
    # wildfire entity texture -> assets/awakening_system/textures/entity/wildfire/wildfire.png
    dest_entity_tex = os.path.join(target_dir, r"src\main\resources\assets\awakening_system\textures\entity\wildfire\wildfire.png")
    os.makedirs(os.path.dirname(dest_entity_tex), exist_ok=True)
    with z.open("friends-and-foes-master/common/src/main/resources/assets/friendsandfoes/textures/entity/wildfire/wildfire.png") as src, open(dest_entity_tex, 'wb') as dst:
        dst.write(src.read())

    # items
    for item_name in ["wildfire_crown.png", "wildfire_crown_fragment.png", "wildfire_spawn_egg.png"]:
        src_entry = f"friends-and-foes-master/common/src/main/resources/assets/friendsandfoes/textures/item/{item_name}"
        dest_path = os.path.join(target_dir, rf"src\main\resources\assets\awakening_system\textures\item\{item_name}")
        with z.open(src_entry) as src, open(dest_path, 'wb') as dst:
            dst.write(src.read())

    # armor texture
    armor_tex_dest = os.path.join(target_dir, r"src\main\resources\assets\awakening_system\textures\models\armor\wildfire_crown_layer_1.png")
    os.makedirs(os.path.dirname(armor_tex_dest), exist_ok=True)
    with z.open("friends-and-foes-master/common/src/main/resources/assets/friendsandfoes/textures/models/armor/wildfire_crown_layer_1.png") as src, open(armor_tex_dest, 'wb') as dst:
        dst.write(src.read())

    # 2. Sounds
    sound_entries = [f for f in z.namelist() if "assets/friendsandfoes/sounds/entity/wildfire/" in f and f.endswith(".ogg")]
    sound_dest_base = os.path.join(target_dir, r"src\main\resources\assets\awakening_system\sounds\entity\wildfire")
    os.makedirs(sound_dest_base, exist_ok=True)
    for s in sound_entries:
        filename = os.path.basename(s)
        dest_file = os.path.join(sound_dest_base, filename)
        with z.open(s) as src, open(dest_file, 'wb') as dst:
            dst.write(src.read())

    # Crown equip sounds
    crown_sound_entries = [f for f in z.namelist() if "assets/friendsandfoes/sounds/item/wildfire_crown/" in f and f.endswith(".ogg")]
    crown_sound_dest = os.path.join(target_dir, r"src\main\resources\assets\awakening_system\sounds\item\wildfire_crown")
    os.makedirs(crown_sound_dest, exist_ok=True)
    for s in crown_sound_entries:
        filename = os.path.basename(s)
        dest_file = os.path.join(crown_sound_dest, filename)
        with z.open(s) as src, open(dest_file, 'wb') as dst:
            dst.write(src.read())

    # 3. Read Java classes to inspect
    java_files = [
        "friends-and-foes-master/common/src/main/java/com/faboslav/friendsandfoes/common/client/render/entity/model/WildfireEntityModel.java",
        "friends-and-foes-master/common/src/main/java/com/faboslav/friendsandfoes/common/entity/animation/WildfireAnimations.java",
        "friends-and-foes-master/common/src/main/java/com/faboslav/friendsandfoes/common/client/render/entity/renderer/WildfireEntityRenderer.java",
        "friends-and-foes-master/common/src/main/java/com/faboslav/friendsandfoes/common/entity/WildfireEntity.java"
    ]
    dump_dir = os.path.join(target_dir, "scratch", "wildfire_src")
    os.makedirs(dump_dir, exist_ok=True)
    for jf in java_files:
        filename = os.path.basename(jf)
        with z.open(jf) as src, open(os.path.join(dump_dir, filename), 'wb') as dst:
            dst.write(src.read())

print("Extraction completed successfully!")
