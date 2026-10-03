import json
import os
import sys

sys.stdout.reconfigure(encoding='utf-8')

LANG_DIR = os.path.join('src', 'main', 'resources', 'assets', 'awakening_system', 'lang')

# Translations to add:
# 1. Bestiary, Furnace, HUD, and Notifications
NEW_KEYS_EN = {
    # Bestiary
    "awakening_system.screen.bestiary.title": "Hunter's Bestiary",
    "awakening_system.screen.bestiary.header": "§b§l✦ HUNTER'S BESTIARY §7// §eSYSTEM ARCHIVES ✦",
    "awakening_system.screen.bestiary.section.abilities": "§e§l[SKILLS & PATTERNS]",
    "awakening_system.screen.bestiary.section.strategy": "§a§l[RECOMMENDED STRATEGY]",
    "awakening_system.screen.bestiary.section.drops": "§6§l[NOTABLE LOOT]",

    # Furnace
    "awakening_system.furnace.view_smelting": "§6✦ View Furnace (Smelting)",
    "awakening_system.furnace.view_smelting_desc": "§7Click to return to the smelting interface.",
    "awakening_system.furnace.config_augments": "§6✦ Configure Augments",
    "awakening_system.furnace.config_augments_desc": "§7Click to view, install or remove mode and speed augments.",
    "awakening_system.furnace.base_speed": "§eBase speed: §f%s",
    "awakening_system.furnace.active_mode": "§fActive mode: %s",
    "awakening_system.furnace.mode.blast": "§6Blast Furnace §e[2x Ores]",
    "awakening_system.furnace.mode.smoker": "§aSmoker §e[2x Food]",
    "awakening_system.furnace.mode.standard": "§7Standard",
    "awakening_system.furnace.alchemy_bonus": "§a✦ Alchemy: +%s%% double output",
    "awakening_system.furnace.flame": "§6Smelting Flame",
    "awakening_system.furnace.lit_active": "§aActive fuel burning",
    "awakening_system.furnace.slot_mode_title": "§c✦ Mode Augment Slot (Red)",
    "awakening_system.furnace.slot_mode_desc1": "§7Insert a Blast Furnace or Smoker augment",
    "awakening_system.furnace.slot_mode_desc2": "§7to double smelting speed for ores or food.",
    "awakening_system.furnace.slot_perf_title": "§a✦ Performance Slot (Green)",
    "awakening_system.furnace.slot_perf_desc": "§7Accepts speed and efficiency augments.",
    "awakening_system.furnace.slot_util_title": "§b✦ Utility Slot (Blue)",
    "awakening_system.furnace.slot_util_desc": "§7Accepts utility and energy augments.",

    # HUD Commands
    "awakening_system.hud.pos.top_left": "§b✦ HUD positioned at: §fTop-Left (TOP_LEFT)",
    "awakening_system.hud.pos.bottom_left": "§b✦ HUD positioned at: §fBottom-Left (BOTTOM_LEFT)",
    "awakening_system.hud.pos.above_hotbar": "§b✦ HUD positioned at: §fAbove Hotbar (ABOVE_HOTBAR)",
    "awakening_system.hud.skills.changed": "§b✦ Skill bar position changed to: §f%s",
    "awakening_system.hud.skills.bottom_left": "§b✦ Skill bar positioned at: §fBottom-Left (BOTTOM_LEFT)",
    "awakening_system.hud.skills.above_hotbar": "§b✦ Skill bar positioned at: §fAbove Hotbar (ABOVE_HOTBAR)",
    "awakening_system.hud.scale.auto": "§a✦ HUD Scale: §fAUTOMATIC (adapts to display resolution)",
    "awakening_system.hud.scale.small": "§a✦ HUD Scale: §fSmall (0.75x)",
    "awakening_system.hud.scale.normal": "§a✦ HUD Scale: §fNormal (1.0x)",
    "awakening_system.hud.scale.large": "§a✦ HUD Scale: §fLarge (1.25x)",
    "awakening_system.hud.scale.custom": "§a✦ HUD Scale set to: §f%sx",
    "awakening_system.hud.hearts.vanilla": "§b✦ Vanilla hearts: §aVisible (Vanilla Style)",
    "awakening_system.hud.hearts.hidden": "§b✦ Vanilla hearts: §cHidden (Custom HUD only)",

    # Red Gate Notification
    "awakening_system.notification.red_gate.title": "§4§l✦ ANOMALY DETECTED: RED GATE! ✦",
    "awakening_system.notification.red_gate.message": "§cDimensional exit sealed! Defeat the Boss to escape."
}

NEW_KEYS_ES = {
    # Bestiary
    "awakening_system.screen.bestiary.title": "Bestiario del Cazador",
    "awakening_system.screen.bestiary.header": "§b§l✦ BESTIARIO DEL CAZADOR §7// §eARCHIVOS DEL SISTEMA ✦",
    "awakening_system.screen.bestiary.section.abilities": "§e§l[HABILIDADES Y PATRONES]",
    "awakening_system.screen.bestiary.section.strategy": "§a§l[ESTRATEGIA RECOMENDADA]",
    "awakening_system.screen.bestiary.section.drops": "§6§l[BOTÍN NOTABLE]",

    # Furnace
    "awakening_system.furnace.view_smelting": "§6✦ Ver Horno (Fundición)",
    "awakening_system.furnace.view_smelting_desc": "§7Haz clic para volver a la interfaz de fundición.",
    "awakening_system.furnace.config_augments": "§6✦ Configurar Mejoras (Augments)",
    "awakening_system.furnace.config_augments_desc": "§7Haz clic para ver, instalar o retirar mejoras de modo y velocidad.",
    "awakening_system.furnace.base_speed": "§eVelocidad base: §f%s",
    "awakening_system.furnace.active_mode": "§fModo activo: %s",
    "awakening_system.furnace.mode.blast": "§6Alto Horno §e[2x Minerales]",
    "awakening_system.furnace.mode.smoker": "§aAhumador §e[2x Alimentos]",
    "awakening_system.furnace.mode.standard": "§7Estándar",
    "awakening_system.furnace.alchemy_bonus": "§a✦ Alquimia: +%s%% doble resultado",
    "awakening_system.furnace.flame": "§6Llama de Fundición",
    "awakening_system.furnace.lit_active": "§aCombustible en combustión activa",
    "awakening_system.furnace.slot_mode_title": "§c✦ Ranura de Modo (Roja)",
    "awakening_system.furnace.slot_mode_desc1": "§7Coloca una Mejora de Alto Horno o de Ahumador",
    "awakening_system.furnace.slot_mode_desc2": "§7para duplicar la velocidad en minerales o alimentos.",
    "awakening_system.furnace.slot_perf_title": "§a✦ Ranura de Rendimiento (Verde)",
    "awakening_system.furnace.slot_perf_desc": "§7Acepta mejoras de velocidad y eficiencia de Iron Furnaces.",
    "awakening_system.furnace.slot_util_title": "§b✦ Ranura de Utilidad (Azul)",
    "awakening_system.furnace.slot_util_desc": "§7Acepta mejoras utilitarias y de energía de Iron Furnaces.",

    # HUD Commands
    "awakening_system.hud.pos.top_left": "§b✦ HUD ubicado en: §fArriba a la Izquierda (TOP_LEFT)",
    "awakening_system.hud.pos.bottom_left": "§b✦ HUD ubicado en: §fAbajo a la Izquierda (BOTTOM_LEFT)",
    "awakening_system.hud.pos.above_hotbar": "§b✦ HUD ubicado en: §fSobre la Hotbar (ABOVE_HOTBAR)",
    "awakening_system.hud.skills.changed": "§b✦ Posición de habilidades cambiada a: §f%s",
    "awakening_system.hud.skills.bottom_left": "§b✦ Habilidades ubicadas en: §fAbajo a la Izquierda (BOTTOM_LEFT)",
    "awakening_system.hud.skills.above_hotbar": "§b✦ Habilidades ubicadas en: §fSobre la Hotbar (ABOVE_HOTBAR)",
    "awakening_system.hud.scale.auto": "§a✦ Escala del HUD: §fAUTOMÁTICA (se adapta al tamaño de la pantalla)",
    "awakening_system.hud.scale.small": "§a✦ Escala del HUD: §fPequeña (0.75x)",
    "awakening_system.hud.scale.normal": "§a✦ Escala del HUD: §fNormal (1.0x)",
    "awakening_system.hud.scale.large": "§a✦ Escala del HUD: §fGrande (1.25x)",
    "awakening_system.hud.scale.custom": "§a✦ Escala del HUD fijada en: §f%sx",
    "awakening_system.hud.hearts.vanilla": "§b✦ Corazones normales: §aVisibles (Estilo Vanilla)",
    "awakening_system.hud.hearts.hidden": "§b✦ Corazones normales: §cOcultos (Solo tarjeta de estado)",

    # Red Gate Notification
    "awakening_system.notification.red_gate.title": "§4§l✦ ¡ANOMALÍA DETECTADA: PUERTA ROJA! ✦",
    "awakening_system.notification.red_gate.message": "§c¡La salida dimensional ha sido sellada! Derrota al Jefe para escapar."
}

NEW_KEYS_RU = {
    # Bestiary
    "awakening_system.screen.bestiary.title": "Бестиарий Охотника",
    "awakening_system.screen.bestiary.header": "§b§l✦ БЕСТИАРИЙ ОХОТНИКА §7// §eАРХИВЫ СИСТЕМЫ ✦",
    "awakening_system.screen.bestiary.section.abilities": "§e§l[НАВЫКИ И ПАТТЕРНЫ]",
    "awakening_system.screen.bestiary.section.strategy": "§a§l[РЕКОМЕНДУЕМАЯ ТАКТИКА]",
    "awakening_system.screen.bestiary.section.drops": "§6§l[ПРИМЕЧАТЕЛЬНАЯ ДОБЫЧА]",

    # Furnace
    "awakening_system.furnace.view_smelting": "§6✦ Печь (Плавка)",
    "awakening_system.furnace.view_smelting_desc": "§7Нажмите, чтобы вернуться к плавке.",
    "awakening_system.furnace.config_augments": "§6✦ Улучшения (Аугменты)",
    "awakening_system.furnace.config_augments_desc": "§7Нажмите для установки или снятия улучшений режима и скорости.",
    "awakening_system.furnace.base_speed": "§eБазовая скорость: §f%s",
    "awakening_system.furnace.active_mode": "§fАктивный режим: %s",
    "awakening_system.furnace.mode.blast": "§6Плавильня §e[2x Руды]",
    "awakening_system.furnace.mode.smoker": "§aКоптильня §e[2x Еда]",
    "awakening_system.furnace.mode.standard": "§7Стандартный",
    "awakening_system.furnace.alchemy_bonus": "§a✦ Алхимия: +%s%% двойной выход",
    "awakening_system.furnace.flame": "§6Пламя плавки",
    "awakening_system.furnace.lit_active": "§aАктивное горение топлива",
    "awakening_system.furnace.slot_mode_title": "§c✦ Слот режима (Красный)",
    "awakening_system.furnace.slot_mode_desc1": "§7Установите улучшение плавильни или коптильни",
    "awakening_system.furnace.slot_mode_desc2": "§7для удвоения скорости плавки руд или еды.",
    "awakening_system.furnace.slot_perf_title": "§a✦ Слот эффективности (Зеленый)",
    "awakening_system.furnace.slot_perf_desc": "§7Принимает улучшения скорости и эффективности.",
    "awakening_system.furnace.slot_util_title": "§b✦ Слот утилит (Синий)",
    "awakening_system.furnace.slot_util_desc": "§7Принимает утилитарные и энергетические улучшения.",

    # HUD Commands
    "awakening_system.hud.pos.top_left": "§b✦ Позиция HUD: §fСверху слева (TOP_LEFT)",
    "awakening_system.hud.pos.bottom_left": "§b✦ Позиция HUD: §fСнизу слева (BOTTOM_LEFT)",
    "awakening_system.hud.pos.above_hotbar": "§b✦ Позиция HUD: §fНад панелью (ABOVE_HOTBAR)",
    "awakening_system.hud.skills.changed": "§b✦ Позиция панели навыков изменена на: §f%s",
    "awakening_system.hud.skills.bottom_left": "§b✦ Панель навыков: §fСнизу слева (BOTTOM_LEFT)",
    "awakening_system.hud.skills.above_hotbar": "§b✦ Панель навыков: §fНад панелью (ABOVE_HOTBAR)",
    "awakening_system.hud.scale.auto": "§a✦ Масштаб HUD: §fАВТОМАТИЧЕСКИЙ (под размер экрана)",
    "awakening_system.hud.scale.small": "§a✦ Масштаб HUD: §fМаленький (0.75x)",
    "awakening_system.hud.scale.normal": "§a✦ Масштаб HUD: §fОбычный (1.0x)",
    "awakening_system.hud.scale.large": "§a✦ Масштаб HUD: §fБольшой (1.25x)",
    "awakening_system.hud.scale.custom": "§a✦ Масштаб HUD установлен на: §f%sx",
    "awakening_system.hud.hearts.vanilla": "§b✦ Обычные сердца: §aВидимы (Ванильный стиль)",
    "awakening_system.hud.hearts.hidden": "§b✦ Обычные сердца: §cСкрыты (Только карточка)",

    # Red Gate Notification
    "awakening_system.notification.red_gate.title": "§4§l✦ ОБНАРУЖЕНА АНОМАЛИЯ: КРАСНЫЕ ВРАТА! ✦",
    "awakening_system.notification.red_gate.message": "§cПространственный выход запечатан! Победите Босса для побега."
}

# 2. Missing Dragon / Biome keys for RU and ES variants
MISSING_DRAGON_KEYS_ES = {
    "biome.awakening_system.isekai_dragon_roost": "Nido del Dragón Ancestral",
    "item.awakening_system.worn_lore_book": "Tomo de Crónicas Desgastado",
    "subtitles.awakening_system.ancient_dragon_ambient": "Dragón Ancestral ruge en la lejanía",
    "subtitles.awakening_system.ancient_dragon_breath": "Aliento titánico del Dragón Ancestral",
    "subtitles.awakening_system.ancient_dragon_death": "El Dragón Ancestral colapsa",
    "subtitles.awakening_system.ancient_dragon_flap": "Alas colosales baten el aire",
    "subtitles.awakening_system.ancient_dragon_hurt": "Dragón Ancestral herido",
    "subtitles.awakening_system.ancient_dragon_roar": "Rugido ensordecedor del Dragón Ancestral"
}

MISSING_DRAGON_KEYS_RU = {
    "biome.awakening_system.isekai_dragon_roost": "Гнездо Древнего Дракона",
    "subtitles.awakening_system.ancient_dragon_ambient": "Древний Дракон рычит вдали",
    "subtitles.awakening_system.ancient_dragon_breath": "Титаническое дыхание Древнего Дракона",
    "subtitles.awakening_system.ancient_dragon_death": "Древний Дракон повержен",
    "subtitles.awakening_system.ancient_dragon_flap": "Взмах колоссальных крыльев",
    "subtitles.awakening_system.ancient_dragon_hurt": "Древний Дракон ранен",
    "subtitles.awakening_system.ancient_dragon_roar": "Оглушительный рев Древнего Дракона"
}

def sync_translations():
    # 1. Update en_us.json
    en_path = os.path.join(LANG_DIR, 'en_us.json')
    with open(en_path, 'r', encoding='utf-8') as f:
        en_data = json.load(f)
    en_data.update(NEW_KEYS_EN)
    with open(en_path, 'w', encoding='utf-8') as f:
        json.dump(en_data, f, ensure_ascii=False, indent=2)
    print("Updated en_us.json")

    # 2. Update ru_ru.json
    ru_path = os.path.join(LANG_DIR, 'ru_ru.json')
    with open(ru_path, 'r', encoding='utf-8') as f:
        ru_data = json.load(f)
    ru_data.update(MISSING_DRAGON_KEYS_RU)
    ru_data.update(NEW_KEYS_RU)
    with open(ru_path, 'w', encoding='utf-8') as f:
        json.dump(ru_data, f, ensure_ascii=False, indent=2)
    print("Updated ru_ru.json")

    # 3. Update all Spanish dialects
    es_files = [f for f in os.listdir(LANG_DIR) if f.startswith('es_') and f.endswith('.json')]
    for ef in es_files:
        epath = os.path.join(LANG_DIR, ef)
        with open(epath, 'r', encoding='utf-8') as f:
            es_data = json.load(f)
        es_data.update(MISSING_DRAGON_KEYS_ES)
        es_data.update(NEW_KEYS_ES)
        with open(epath, 'w', encoding='utf-8') as f:
            json.dump(es_data, f, ensure_ascii=False, indent=2)
        print(f"Updated {ef}")

if __name__ == '__main__':
    sync_translations()
