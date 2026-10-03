import re

all_enums = [
    ("RAIDEN_SHOGUN", "COMPANION_RAIDEN_SHOGUN"),
    ("HU_TAO", "COMPANION_HU_TAO"),
    ("ZHONGLI", "COMPANION_ZHONGLI"),
    ("FURINA", "COMPANION_FURINA"),
    ("NAHIDA", "COMPANION_NAHIDA"),
    ("KAZUHA", "COMPANION_KAZUHA"),
    ("GOJO_SATORU", "COMPANION_GOJO_SATORU"),
    ("SUNG_JINWOO", "COMPANION_SUNG_JINWOO"),
    ("NOELLE", "COMPANION_NOELLE"),
    ("CHA_HAEIN", "COMPANION_CHA_HAEIN"),
    ("DILUC", "COMPANION_DILUC"),
    ("JEAN", "COMPANION_JEAN"),
    ("BENNETT", "COMPANION_BENNETT"),
    ("ACHERON", "COMPANION_ACHERON"),
    ("KAFKA", "COMPANION_KAFKA"),
    ("ELLEN_JOE", "COMPANION_ELLEN_JOE"),
    ("IGRIS", "COMPANION_IGRIS"),
    ("BERU", "COMPANION_BERU"),
    ("SUKUNA", "COMPANION_SUKUNA"),
    ("YUJI_ITADORI", "COMPANION_YUJI_ITADORI"),
    ("ARTORIA_PENDRAGON", "COMPANION_ARTORIA_PENDRAGON"),
    ("GILGAMESH", "COMPANION_GILGAMESH"),
    ("RENGOKU", "COMPANION_RENGOKU"),
    ("TANJIRO", "COMPANION_TANJIRO"),
    ("NEZUKO", "COMPANION_NEZUKO"),
    ("XIAO", "COMPANION_XIAO"),
    ("ARLECCHINO", "COMPANION_ARLECCHINO"),
    ("FIREFLY", "COMPANION_FIREFLY"),
    ("MEGUMI", "COMPANION_MEGUMI"),
    ("ARCHER", "COMPANION_ARCHER"),
]

# Build sound getters
def make_getter(method_name, sound_suffix):
    lines = [f"    public SoundEvent {method_name}() {{", "        return switch (this) {"]
    for en, snd in all_enums:
        lines.append(f"            case {en} -> ModSounds.{snd}_{sound_suffix}.get();")
    lines.append("        };")
    lines.append("    }")
    return "\n".join(lines)

summon_getter = make_getter("getSummonSound", "SUMMON")
skill_getter = make_getter("getSkillSound", "SKILL")
ambient_getter = make_getter("getAmbientSound", "AMBIENT")
hurt_getter = make_getter("getHurtSound", "HURT")

all_getters = f"{summon_getter}\n\n{skill_getter}\n\n{ambient_getter}\n\n{hurt_getter}"

new_entries = """    NEZUKO(
            "nezuko",
            "Nezuko Kamado",
            "Demonio de la Sangre Ardiente",
            4,
            "§d§l",
            "Fuego Demoníaco",
            "Piroquinesis Carmesí: Llamas rosas que abrasan al enemigo y regeneran salud al jugador.",
            "assets/awakening_system/textures/entity/companion/nezuko.png"
    ),
    XIAO(
            "xiao",
            "Xiao",
            "Guardián Yaksha (Alatus)",
            5,
            "§b§l",
            "Anemo",
            "Azote de Todo Mal: Saltos acrobáticos fulgurantes e impacto descendente que pulveriza a los enemigos.",
            "assets/awakening_system/textures/entity/companion/xiao.png"
    ),
    ARLECCHINO(
            "arlecchino",
            "Arlecchino",
            "La Sota (Padre de la Casa de la Hoguera)",
            5,
            "§4§l",
            "Pyro / Carmesí",
            "Vasto Asolamiento: Ráfagas lunares de fuego carmesí y corte de guadaña sanguinaria.",
            "assets/awakening_system/textures/entity/companion/arlecchino.png"
    ),
    FIREFLY(
            "firefly",
            "Firefly (SAM)",
            "Armadura de la Caballería de Hierro",
            5,
            "§6§l",
            "Fuego / Combustión",
            "Sobrecarga Estratégica: Embestida aérea ígnea con propulsores que calcina el campo de batalla.",
            "assets/awakening_system/textures/entity/companion/firefly.png"
    ),
    MEGUMI(
            "megumi",
            "Megumi Fushiguro",
            "Chamán de las Diez Sombras",
            4,
            "§3§l",
            "Sombra / Shikigami",
            "Diez Sombras: Despliega al Perro Divino y Nue eléctrica para acorralar objetivos.",
            "assets/awakening_system/textures/entity/companion/megumi.png"
    ),
    ARCHER(
            "archer",
            "Archer (EMIYA)",
            "Espíritu Heroico de la Forja",
            4,
            "§c§l",
            "Proyección / Espadas",
            "Unlimited Blade Works: Proyecta espadas Kanshou & Bakuya y una lluvia de flechas calibradas.",
            "assets/awakening_system/textures/entity/companion/archer.png"
    );"""

file_path = "src/main/java/com/soybe/awakeningsystem/entity/CompanionProfile.java"
with open(file_path, "r", encoding="utf-8") as f:
    code = f.read()

# Replace NEZUKO(...) ; with new_entries
code = re.sub(r'NEZUKO\(.*?\);', new_entries, code, flags=re.DOTALL)

# Replace isSlim()
old_is_slim = """    public boolean isSlim() {
        return switch (this) {
            case RAIDEN_SHOGUN, HU_TAO, NOELLE, NAHIDA, KAZUHA, CHA_HAEIN,
                 ACHERON, KAFKA, ELLEN_JOE, ARTORIA_PENDRAGON, NEZUKO -> true;
            default -> false;
        };
    }"""

new_is_slim = """    public boolean isSlim() {
        return switch (this) {
            case RAIDEN_SHOGUN, HU_TAO, NOELLE, NAHIDA, KAZUHA, CHA_HAEIN,
                 ACHERON, KAFKA, ELLEN_JOE, ARTORIA_PENDRAGON, NEZUKO,
                 ARLECCHINO, FIREFLY -> true;
            default -> false;
        };
    }"""

code = code.replace(old_is_slim, new_is_slim)

# Replace all 4 sound getters
# From public SoundEvent getSummonSound() { up to public static CompanionProfile getById
getter_pattern = r'public SoundEvent getSummonSound\(\) \{.*?public static CompanionProfile getById'
replacement = f"{all_getters}\n\n    public static CompanionProfile getById"

code = re.sub(getter_pattern, replacement, code, flags=re.DOTALL)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(code)

print("CompanionProfile.java successfully updated!")
