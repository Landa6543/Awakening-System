import re

path = "src/main/java/com/soybe/awakeningsystem/PromotionalBanner.java"
with open(path, "r", encoding="utf-8") as f:
    code = f.read()

# 1. Update GENSHIN_PHASES: add Xiao and Arlecchino
xiao_phase = """            new BannerPhase(
                    6, Franchise.GENSHIN,
                    "INVITACIÓN DE LOS VIENTOS",
                    "Guardián Yaksha & Danza del Conquistador del Mal",
                    CompanionProfile.XIAO, null,
                    () -> SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.isBound() ? new ItemStack(SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.SKYWARD_BLADE.isBound() ? new ItemStack(SystemItems.SKYWARD_BLADE.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT),
                    List.of(
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF00BFA5, 0x66004D40,
                    "ANEMO 🍃 5★",
                    "\"Si despierta tu miedo, grita mi nombre: Xiao.\""
            ),
            new BannerPhase(
                    7, Franchise.GENSHIN,
                    "CENIZAS DE LA HOGUERA",
                    "La Sota de los Fatui & Luna Carmesí del Destino",
                    CompanionProfile.ARLECCHINO, null,
                    () -> SystemItems.CRIMSON_MOONS_SEMBLANCE.isBound() ? new ItemStack(SystemItems.CRIMSON_MOONS_SEMBLANCE.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.ABSOLUTION.isBound() ? new ItemStack(SystemItems.ABSOLUTION.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.BENNETT, CompanionProfile.NOELLE),
                    List.of(
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFB71C1C, 0x667F0000,
                    "PYRO / LUNA CARMESÍ 🌙 5★",
                    "\"En la Casa de la Hoguera no hay lugar para la debilidad. Llámame Padre.\""
            )
    };"""

code = code.replace('            )\n    };\n\n    // ==========================================\n    // 2. HONKAI', '            ),\n' + xiao_phase + '\n\n    // ==========================================\n    // 2. HONKAI')

# 2. Update HONKAI_PHASES: add Firefly
firefly_phase = """,
            new BannerPhase(
                    3, Franchise.HONKAI_ZZZ,
                    "VUELO DE LA LUCIÉRNAGA",
                    "Armadura SAM de la Caballería de Hierro & Mar de Fuego",
                    CompanionProfile.FIREFLY, null,
                    () -> SystemItems.HERTA_HAMMER.isBound() ? new ItemStack(SystemItems.HERTA_HAMMER.get()) : new ItemStack(Items.NETHERITE_AXE),
                    () -> SystemItems.DOMAIN_OF_SANCTION.isBound() ? new ItemStack(SystemItems.DOMAIN_OF_SANCTION.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.CHA_HAEIN),
                    List.of(
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFFF6F00, 0x66E65100,
                    "COMBUSTIÓN 🔥 5★",
                    "\"¡Prenderé fuego a los mares hasta que florezca la última luciérnaga!\""
            )
    };"""

code = re.sub(r'List\.of\(CompanionProfile\.NOELLE, CompanionProfile\.CHA_HAEIN\),.*?"\"¿Dulces\?.*?\"\s*\)\s*\}\;', lambda m: m.group(0)[:-2] + firefly_phase, code, flags=re.DOTALL)

# 3. Add MEGUMI to JJK rate-ups
code = code.replace('List.of(CompanionProfile.YUJI_ITADORI)', 'List.of(CompanionProfile.YUJI_ITADORI, CompanionProfile.MEGUMI)')

# 4. Add ARCHER to Fate rate-ups
code = code.replace('List.of(CompanionProfile.CHA_HAEIN, CompanionProfile.NOELLE)', 'List.of(CompanionProfile.CHA_HAEIN, CompanionProfile.ARCHER)')

with open(path, "w", encoding="utf-8") as f:
    f.write(code)

print("Updated PromotionalBanner.java successfully!")
