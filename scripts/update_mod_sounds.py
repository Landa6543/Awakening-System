path = "src/main/java/com/soybe/awakeningsystem/ModSounds.java"

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

new_companions = [
    ("XIAO", "xiao"),
    ("ARLECCHINO", "arlecchino"),
    ("ACHERON", "acheron"),
    ("KAFKA", "kafka"),
    ("FIREFLY", "firefly"),
    ("ELLEN_JOE", "ellen_joe"),
    ("IGRIS", "igris"),
    ("BERU", "beru"),
    ("SUKUNA", "sukuna"),
    ("YUJI_ITADORI", "yuji_itadori"),
    ("MEGUMI", "megumi"),
    ("ARTORIA_PENDRAGON", "artoria_pendragon"),
    ("GILGAMESH", "gilgamesh"),
    ("ARCHER", "archer"),
    ("RENGOKU", "rengoku"),
    ("TANJIRO", "tanjiro"),
    ("NEZUKO", "nezuko"),
]

code_lines = []
for uc, lc in new_companions:
    code_lines.append(f"    // Compañero: {lc}")
    for act in ["SUMMON", "SKILL", "AMBIENT", "HURT"]:
        act_lower = act.lower()
        code_lines.append(f"    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_{uc}_{act} =")
        code_lines.append(f"            SOUND_EVENTS.register(\"companion_{lc}_{act_lower}\",")
        code_lines.append(f"                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, \"companion_{lc}_{act_lower}\")));")

insert_block = "\n".join(code_lines) + "\n"

anchor = '    // Casino y Gachapon'
if anchor in content:
    new_content = content.replace(anchor, insert_block + "\n" + anchor)
    with open(path, "w", encoding="utf-8") as f:
        f.write(new_content)
    print("Successfully added all 17 companion sound definitions to ModSounds.java!")
else:
    print("Anchor not found!")
