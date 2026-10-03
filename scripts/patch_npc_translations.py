import sys
import os
import json
import re

sys.stdout.reconfigure(encoding='utf-8')

print("Starting NPC and remaining screen translation script...")

# 1. Update CelestialDragonEntity.java
cd_path = "src/main/java/com/soybe/awakeningsystem/entity/CelestialDragonEntity.java"
with open(cd_path, "r", encoding="utf-8") as f:
    cd_code = f.read()

cd_code = cd_code.replace(
    'player.displayClientMessage(Component.literal("§6§l✦ ¡EL DRAGÓN CELESTIAL RECONOCE TU ALMA SUPREMA! ✦"), false);',
    'player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.recognized"), false);'
)

old_dragon_bond = """                if (player instanceof ServerPlayer sp) {
                    AwakeningSystemMod.notify(sp,
                            "literal:\\u00A76\\u00A7l✦ VÍNCULO DRACÓNICO CELESTIAL ✦",
                            "literal:\\u00A7fHas sometido al Dragón Celestial. Ahora es tu montura sagrada.",
                            0xFFFFD700);
                }"""
new_dragon_bond = """                if (player instanceof ServerPlayer sp) {
                    AwakeningSystemMod.notify(sp,
                            "awakening_system.entity.celestial_dragon.bond_title",
                            "awakening_system.entity.celestial_dragon.bond_desc",
                            0xFFFFD700);
                }"""
cd_code = cd_code.replace(old_dragon_bond, new_dragon_bond)

cd_code = cd_code.replace(
    'player.displayClientMessage(Component.literal("§6§l✦ ¡EL DRAGÓN CELESTIAL FUE DOMESTICADO (MODO CREATIVO)! ✦"), false);',
    'player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.creative_tamed"), false);'
)

with open(cd_path, "w", encoding="utf-8") as f:
    f.write(cd_code)
print("Updated CelestialDragonEntity.java")

# 2. Update SystemEvents.java line 1996
se_path = "src/main/java/com/soybe/awakeningsystem/SystemEvents.java"
with open(se_path, "r", encoding="utf-8") as f:
    se_code = f.read()

se_code = se_code.replace(
    'player.displayClientMessage(Component.literal("§d§l[Cúspide de Yggdrasil] §fHas ascendido sobre la copa de hojas del Gran Árbol. Explora el dosel para hallar la entrada oculta."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.action.yggdrasil_summit"), true);'
)

with open(se_path, "w", encoding="utf-8") as f:
    f.write(se_code)
print("Updated SystemEvents.java")

# 3. NPC translations dictionary
NPC_TRANSLATIONS = {
    "awakening_system.entity.celestial_dragon.bond_title": {
        "es": "§6§l✦ VÍNCULO DRACÓNICO CELESTIAL ✦",
        "en": "§6§l✦ CELESTIAL DRACONIC BOND ✦",
        "ru": "§6§l✦ НЕБЕСНАЯ ДРАКОНЬЯ СВЯЗЬ ✦"
    },
    "awakening_system.entity.celestial_dragon.bond_desc": {
        "es": "§fHas sometido al Dragón Celestial. Ahora es tu montura sagrada.",
        "en": "§fYou tamed the Celestial Dragon. It is now your sacred mount.",
        "ru": "§fВы подчинили Небесного Дракона. Теперь он ваш священный скакун."
    },

    # Sung Jinwoo
    "awakening_system.npc.sung_jinwoo.dialogue_title": {"es": "§5§l✦ Sung Jin-Woo ✦", "en": "§5§l✦ Sung Jin-Woo ✦", "ru": "§5§l✦ Сон Джин-Ву ✦"},
    "awakening_system.npc.sung_jinwoo.title": {"es": "Monarca de las Sombras", "en": "Shadow Monarch", "ru": "Монарх Теней"},
    "awakening_system.npc.sung_jinwoo.blessing_title": {"es": "§5§l✦ BENDICIÓN DE SUNG JIN-WOO ✦", "en": "§5§l✦ SUNG JIN-WOO'S BLESSING ✦", "ru": "§5§l✦ БЛАГОСЛОВЕНИЕ СОН ДЖИН-ВУ ✦"},
    "awakening_system.npc.sung_jinwoo.blessing_desc": {
        "es": "§eHas recibido la §dPresencia de las Sombras§e.\n§7(Fuerza II, Velocidad II, Visión Nocturna por 5 min)",
        "en": "§eYou received §dShadow Presence§e.\n§7(Strength II, Speed II, Night Vision for 5 min)",
        "ru": "§eВы получили §dПрисутствие Теней§e.\n§7(Сила II, Скорость II, Ночное зрение на 5 мин)"
    },
    "awakening_system.npc.sung_jinwoo.dialogue.0": {"es": "Surge... Aquellos que caigan bajo mi mando servirán por la eternidad a las sombras.", "en": "Arise... Those who fall under my command will serve the shadows for eternity.", "ru": "Восстань... Павшие под моим началом будут вечно служить теням."},
    "awakening_system.npc.sung_jinwoo.dialogue.1": {"es": "¿Te estás esforzando en tus mazmorras? El Sistema no recompensa la debilidad.", "en": "Are you pushing yourself in your dungeons? The System does not reward weakness.", "ru": "Ты выкладываешься в подземельях? Система не вознаграждает слабость."},
    "awakening_system.npc.sung_jinwoo.dialogue.2": {"es": "Mi ejército nunca duerme. Mantén la calma y sigue subiendo de nivel.", "en": "My army never sleeps. Stay calm and keep leveling up.", "ru": "Моя армия никогда не спит. Сохраняй хладнокровие и повышай уровень."},
    "awakening_system.npc.sung_jinwoo.dialogue.3": {"es": "Solo me arrodillo ante aquellos que protegen a los que no pueden luchar.", "en": "I only kneel before those who protect the ones who cannot fight.", "ru": "Я преклоняю колено лишь перед теми, кто защищает бессильных."},

    # Cha Hae-In
    "awakening_system.npc.cha_haein.dialogue_title": {"es": "§e§l✦ Cha Hae-In ✦", "en": "§e§l✦ Cha Hae-In ✦", "ru": "§e§l✦ Ча Ха-Ин ✦"},
    "awakening_system.npc.cha_haein.title": {"es": "Cazadora Rango S", "en": "S-Rank Hunter", "ru": "Охотница S-Ранга"},
    "awakening_system.npc.cha_haein.blessing_title": {"es": "§e§l✦ BENDICIÓN DE CHA HAE-IN ✦", "en": "§e§l✦ CHA HAE-IN'S BLESSING ✦", "ru": "§e§l✦ БЛАГОСЛОВЕНИЕ ЧА ХА-ИН ✦"},
    "awakening_system.npc.cha_haein.blessing_desc": {
        "es": "§eHas recibido la §6Danza de la Espada de Luz§e.\n§7(Prisa II, Velocidad II, Salto Mejorado II por 5 min)",
        "en": "§eYou received §6Dance of the Light Sword§e.\n§7(Haste II, Speed II, Jump Boost II for 5 min)",
        "ru": "§eВы получили §6Танец Меча Света§e.\n§7(Спешка II, Скорость II, Прыгучесть II на 5 мин)"
    },
    "awakening_system.npc.cha_haein.dialogue.0": {"es": "Tu aroma no huele como el de los demás cazadores... ¿Acaso estás conectado al Sistema?", "en": "Your scent isn't like the other hunters'... Are you connected to the System?", "ru": "Твой запах не похож на остальных охотников... Ты связан с Системой?"},
    "awakening_system.npc.cha_haein.dialogue.1": {"es": "La postura de la espada requiere concentración absoluta. No vaciles en el momento decisivo.", "en": "The sword stance requires absolute focus. Never hesitate at the decisive moment.", "ru": "Стойка меча требует абсолютной концентрации. Не колеблись в решающий момент."},
    "awakening_system.npc.cha_haein.dialogue.2": {"es": "No descuides tu entrenamiento físico; la agilidad lo es todo cuando se abre una Puerta Roja.", "en": "Don't neglect your physical training; agility is everything when a Red Gate opens.", "ru": "Не пренебрегай тренировками; ловкость решает всё при открытии Красных врат."},
    "awakening_system.npc.cha_haein.dialogue.3": {"es": "Siempre que desenvaines tu arma, recuerda por quién estás luchando.", "en": "Whenever you draw your blade, remember who you are fighting for.", "ru": "Обнажая клинок, всегда помни, ради кого сражаешься."},

    # Kim Dokja
    "awakening_system.npc.kim_dokja.dialogue_title": {"es": "§f§l✦ Kim Dokja ✦", "en": "§f§l✦ Kim Dokja ✦", "ru": "§f§l✦ Ким Докча ✦"},
    "awakening_system.npc.kim_dokja.title": {"es": "Rey Demonio de la Salvación", "en": "Demon King of Salvation", "ru": "Демонический Король Спасения"},
    "awakening_system.npc.kim_dokja.blessing_title": {"es": "§f§l✦ BENDICIÓN DE KIM DOKJA ✦", "en": "§f§l✦ KIM DOKJA'S BLESSING ✦", "ru": "§f§l✦ БЛАГОСЛОВЕНИЕ КИМ ДОКЧА ✦"},
    "awakening_system.npc.kim_dokja.blessing_desc": {
        "es": "§eHas recibido el §bOjo del Lector Omnisciente§e.\n§7(+250 Monedas, Suerte III y Visión Nocturna)",
        "en": "§eYou received §bOmniscient Reader's Eye§e.\n§7(+250 Coins, Luck III and Night Vision)",
        "ru": "§eВы получили §bОко Всевидящего Читателя§e.\n§7(+250 Монет, Удача III и Ночное зрение)"
    },
    "awakening_system.npc.kim_dokja.dialogue.0": {"es": "¿Buscando una guía para el próximo escenario? Todo está escrito si sabes leer entre líneas.", "en": "Looking for a guide to the next scenario? It's all written if you read between the lines.", "ru": "Ищешь руководство к следующему сценарию? Всё уже написано, если читать между строк."},
    "awakening_system.npc.kim_dokja.dialogue.1": {"es": "No confíes ciegamente en las Constelaciones del Star Stream; cobran muy caro por cada milagro.", "en": "Don't blindly trust Star Stream Constellations; they charge heavily for every miracle.", "ru": "Не доверяй слепо Созвездиям Star Stream; они дорого берут за каждое чудо."},
    "awakening_system.npc.kim_dokja.dialogue.2": {"es": "Este mundo no es una tragedia si luchamos hasta el último capítulo.", "en": "This world is not a tragedy if we fight to the very last chapter.", "ru": "Этот мир не трагедия, если мы боремся до самой последней главы."},
    "awakening_system.npc.kim_dokja.dialogue.3": {"es": "Toma algunas monedas del Sistema. En este escenario, la información y los recursos lo son todo.", "en": "Take some System coins. In this scenario, information and resources are everything.", "ru": "Возьми немного монет Системы. В этом сценарии информация и ресурсы решают всё."},

    # Yoo Joonghyuk
    "awakening_system.npc.yoo_joonghyuk.dialogue_title": {"es": "§8§l✦ Yoo Joonghyuk ✦", "en": "§8§l✦ Yoo Joonghyuk ✦", "ru": "§8§l✦ Ю Чжунхёк ✦"},
    "awakening_system.npc.yoo_joonghyuk.title": {"es": "El Regresor Supremo", "en": "The Supreme Regressor", "ru": "Высший Регрессор"},
    "awakening_system.npc.yoo_joonghyuk.blessing_title": {"es": "§8§l✦ BENDICIÓN DE YOO JOONGHYUK ✦", "en": "§8§l✦ YOO JOONGHYUK'S BLESSING ✦", "ru": "§8§l✦ БЛАГОСЛОВЕНИЕ Ю ЧЖУНХЁКА ✦"},
    "awakening_system.npc.yoo_joonghyuk.blessing_desc": {
        "es": "§eHas recibido la §cVoluntad del Regresor§e.\n§7(Resistencia II, Fuerza II, Resistencia al Fuego por 5 min)",
        "en": "§eYou received §cRegressor's Resolve§e.\n§7(Resistance II, Strength II, Fire Resistance for 5 min)",
        "ru": "§eВы получили §cВолю Регрессора§e.\n§7(Сопротивление II, Сила II, Огнестойкость на 5 мин)"
    },
    "awakening_system.npc.yoo_joonghyuk.dialogue.0": {"es": "Si eres débil, morirás antes del escenario 10. No te interpongas en mi camino.", "en": "If you are weak, you will die before scenario 10. Stay out of my path.", "ru": "Если ты слаб, погибнешь до 10-го сценария. Не стой у меня на пути."},
    "awakening_system.npc.yoo_joonghyuk.dialogue.1": {"es": "He visto este mundo perecer cientos de veces. No repetiré los mismos errores en esta vida.", "en": "I have seen this world perish hundreds of times. I will not repeat past mistakes.", "ru": "Я видел гибель этого мира сотни раз. Я не повторю прежних ошибок в этой жизни."},
    "awakening_system.npc.yoo_joonghyuk.dialogue.2": {"es": "¿Crees que tu determinación es suficiente? Demuéstramelo con tu filo en el campo de batalla.", "en": "Think your resolve is enough? Prove it with your blade on the battlefield.", "ru": "Считаешь свою решимость достаточной? Докажи это клинком на поле боя."},
    "awakening_system.npc.yoo_joonghyuk.dialogue.3": {"es": "No mueras en una mazmorra cualquiera. Una muerte patética no merece ser recordada.", "en": "Do not die in some random dungeon. A pathetic death deserves no remembrance.", "ru": "Не смей умирать в случайном подземелье. Жалкая смерть не заслуживает памяти."},

    # Lloyd Frontera
    "awakening_system.npc.lloyd_frontera.dialogue_title": {"es": "§6§l✦ Lloyd Frontera ✦", "en": "§6§l✦ Lloyd Frontera ✦", "ru": "§6§l✦ Ллойд Фронтера ✦"},
    "awakening_system.npc.lloyd_frontera.title": {"es": "El Ingeniero Supremo", "en": "The Supreme Engineer", "ru": "Величайший Инженер"},
    "awakening_system.npc.lloyd_frontera.blessing_title": {"es": "§6§l✦ BENDICIÓN DE LLOYD FRONTERA ✦", "en": "§6§l✦ LLOYD FRONTERA'S BLESSING ✦", "ru": "§6§l✦ БЛАГОСЛОВЕНИЕ ЛЛОЙДА ФРОНТЕРЫ ✦"},
    "awakening_system.npc.lloyd_frontera.blessing_desc": {
        "es": "§eHas recibido la §6Ingeniería de Frontera§e.\n§7(Prisa Minera III por 10 min, Héroe de la Aldea, Nutrición)",
        "en": "§eYou received §6Frontera Engineering§e.\n§7(Mining Haste III for 10 min, Hero of the Village, Saturation)",
        "ru": "§eВы получили §6Инженерию Фронтеры§e.\n§7(Спешка добычи III на 10 мин, Герой деревни, Насыщение)"
    },
    "awakening_system.npc.lloyd_frontera.dialogue.0": {"es": "¡El dinero mueve el mundo, pero el cemento, el agua y la pala lo construyen!", "en": "Money moves the world, but cement, water, and shovels build it!", "ru": "Деньги движут миром, но цемент, вода и лопата строят его!"},
    "awakening_system.npc.lloyd_frontera.dialogue.1": {"es": "¡Ondol caliente en invierno y acueductos limpios en verano! ¡Eso sí es verdadera calidad de vida!", "en": "Warm ondol in winter, clean aqueducts in summer! That's real quality of life!", "ru": "Тёплый ондоль зимой, чистые акведуки летом! Вот это истинное качество жизни!"},
    "awakening_system.npc.lloyd_frontera.dialogue.2": {"es": "¿Tienes monedas del Sistema? ¡Tengo planos, contratos y materiales de primera calidad!", "en": "Got System coins? I've got blueprints, contracts, and top-tier materials!", "ru": "Есть монеты Системы? У меня есть чертежи, контракты и первоклассные материалы!"},
    "awakening_system.npc.lloyd_frontera.dialogue.3": {"es": "¡Javier, anota la factura de este aventurero! ¡Cada metro de tierra tiene un precio!", "en": "Javier, invoice this adventurer! Every square meter of land has its price!", "ru": "Хавьер, выстави счёт этому авантюристу! Каждый метр земли имеет свою цену!"},

    # Javier Asrahan
    "awakening_system.npc.javier_asrahan.dialogue_title": {"es": "§b§l✦ Javier Asrahan ✦", "en": "§b§l✦ Javier Asrahan ✦", "ru": "§b§l✦ Хавьер Асрахан ✦"},
    "awakening_system.npc.javier_asrahan.title": {"es": "El Caballero Santo", "en": "The Holy Knight", "ru": "Святой Рыцарь"},
    "awakening_system.npc.javier_asrahan.blessing_title": {"es": "§b§l✦ BENDICIÓN DE JAVIER ASRAHAN ✦", "en": "§b§l✦ JAVIER ASRAHAN'S BLESSING ✦", "ru": "§b§l✦ БЛАГОСЛОВЕНИЕ ХАВЬЕРА АСРАХАНА ✦"},
    "awakening_system.npc.javier_asrahan.blessing_desc": {
        "es": "§eHas recibido el §bEscudo Sagrado de Asrahan§e.\n§7(Absorción IV y Resistencia II por 5 min)",
        "en": "§eYou received §bAsrahan's Holy Shield§e.\n§7(Absorption IV and Resistance II for 5 min)",
        "ru": "§eВы получили §bСвятой Щит Асрахана§e.\n§7(Поглощение IV и Сопротивление II на 5 мин)"
    },
    "awakening_system.npc.javier_asrahan.dialogue.0": {"es": "El joven maestro Lloyd parece codicioso, pero sus obras siempre terminan salvando a la gente.", "en": "Young Master Lloyd looks greedy, but his projects always save people in the end.", "ru": "Молодой господин Ллойд кажется жадным, но его проекты всегда спасают людей."},
    "awakening_system.npc.javier_asrahan.dialogue.1": {"es": "La Espada de Asrahan protege a los inocentes y corta toda maldad.", "en": "The Sword of Asrahan protects the innocent and strikes down all evil.", "ru": "Меч Асрахана защищает невинных и разит всякое зло."},
    "awakening_system.npc.javier_asrahan.dialogue.2": {"es": "Mantén la guardia alta. Un verdadero guerrero nunca baja los brazos en tierra desconocida.", "en": "Keep your guard up. A true warrior never lets down their vigilance in unknown lands.", "ru": "Держи оборону. Истинный воин никогда не теряет бдительность на чужой земле."},
    "awakening_system.npc.javier_asrahan.dialogue.3": {"es": "Si necesitas ayuda con monstruos que amenazan tus tierras, cuenta con mi espada.", "en": "If you need help with monsters threatening your lands, call upon my sword.", "ru": "Если монстры угрожают твоим землям, можешь рассчитывать на мой меч."},

    # Cheon Yeowoon
    "awakening_system.npc.cheon_yeowoon.dialogue_title": {"es": "§4§l✦ Cheon Yeo-woon ✦", "en": "§4§l✦ Cheon Yeo-woon ✦", "ru": "§4§l✦ Чхон Ё-Ун ✦"},
    "awakening_system.npc.cheon_yeowoon.title": {"es": "Señor del Culto Demoníaco", "en": "Lord of the Demonic Cult", "ru": "Владыка Демонического Культа"},
    "awakening_system.npc.cheon_yeowoon.blessing_title": {"es": "§4§l✦ BENDICIÓN DE CHEON YEO-WOON ✦", "en": "§4§l✦ CHEON YEO-WOON'S BLESSING ✦", "ru": "§4§l✦ БЛАГОСЛОВЕНИЕ ЧХОН Ё-УНА ✦"},
    "awakening_system.npc.cheon_yeowoon.blessing_desc": {
        "es": "§eHas recibido la §cCirculación de Nano-Máquinas§e.\n§7(Regeneración II por 3 min, Fuerza II y Velocidad II por 5 min)",
        "en": "§eYou received §cNano-Machine Circulation§e.\n§7(Regeneration II for 3 min, Strength II and Speed II for 5 min)",
        "ru": "§eВы получили §cЦиркуляцию Наномашин§e.\n§7(Регенерация II на 3 мин, Сила II и Скорость II на 5 мин)"
    },
    "awakening_system.npc.cheon_yeowoon.dialogue.0": {"es": "Nano, escanea los meridianos de este cazador... Estructura biológica y energética aceptable.", "en": "Nano, scan this hunter's meridians... Biological and energetic structure acceptable.", "ru": "Нано, просканируй меридианы этого охотника... Биологическая и энергетическая структура приемлемы."},
    "awakening_system.npc.cheon_yeowoon.dialogue.1": {"es": "El Culto Demoníaco Celestial no doblega su cabeza ante ningún dios ni emperador.", "en": "The Heavenly Demonic Cult bows its head to neither god nor emperor.", "ru": "Небесный Демонический Культ не склоняет голову ни перед богами, ни перед императором."},
    "awakening_system.npc.cheon_yeowoon.dialogue.2": {"es": "El poder absoluto no necesita excusas; se forja con precisión milimétrica y acero despiadado.", "en": "Absolute power needs no excuses; it is forged with millimeter precision and ruthless steel.", "ru": "Абсолютная сила не нуждается в оправданиях; она куётся с миллиметровой точностью и беспощадной сталью."},
    "awakening_system.npc.cheon_yeowoon.dialogue.3": {"es": "Corta a tu enemigo antes de que formule su primer pensamiento de traición.", "en": "Cut down your enemy before their first thought of betrayal can form.", "ru": "Порази врага прежде, чем у него возникнет первая мысль о предательстве."},

    # Arthur Leywin
    "awakening_system.npc.arthur_leywin.dialogue_title": {"es": "§9§l✦ Arthur Leywin ✦", "en": "§9§l✦ Arthur Leywin ✦", "ru": "§9§l✦ Артур Лейвин ✦"},
    "awakening_system.npc.arthur_leywin.title": {"es": "Gobernante del Éter", "en": "Ruler of Aether", "ru": "Владыка Эфира"},
    "awakening_system.npc.arthur_leywin.blessing_title": {"es": "§9§l✦ BENDICIÓN DE ARTHUR LEYWIN ✦", "en": "§9§l✦ ARTHUR LEYWIN'S BLESSING ✦", "ru": "§9§l✦ БЛАГОСЛОВЕНИЕ АРТУРА ЛЕЙВИНА ✦"},
    "awakening_system.npc.arthur_leywin.blessing_desc": {
        "es": "§eHas recibido el §9Núcleo de Éter Primordial§e.\n§7(Salto II, Velocidad II y Caída Lenta por 5 min)",
        "en": "§eYou received §9Primordial Aether Core§e.\n§7(Jump II, Speed II, and Slow Falling for 5 min)",
        "ru": "§eВы получили §9Изначальное Эфирное Ядро§e.\n§7(Прыгучесть II, Скорость II и Плавное падение на 5 мин)"
    },
    "awakening_system.npc.arthur_leywin.dialogue.0": {"es": "El maná llena el cuerpo, pero el éter rige las dimensiones mismas del espacio y la realidad.", "en": "Mana fills the body, but aether governs the very dimensions of space and reality.", "ru": "Мана наполняет тело, но эфир управляет самими измерениями пространства и реальности."},
    "awakening_system.npc.arthur_leywin.dialogue.1": {"es": "No lucho por títulos ni por vanagloria; peleo para asegurar que mis seres queridos tengan un mañana.", "en": "I fight not for titles or vain glory; I fight to ensure my loved ones have a tomorrow.", "ru": "Я сражаюсь не ради титулов и славы; я сражаюсь, чтобы у моих близких было завтра."},
    "awakening_system.npc.arthur_leywin.dialogue.2": {"es": "Purifica tu núcleo. La claridad de tu intención determina la fuerza de tu despertar.", "en": "Purify your core. The clarity of your intent defines the strength of your awakening.", "ru": "Очисти своё ядро. Ясность намерений определяет силу твоего пробуждения."},
    "awakening_system.npc.arthur_leywin.dialogue.3": {"es": "Aprende a fluir con el entorno; la fuerza bruta sin control solo engendra destrucción.", "en": "Learn to flow with your surroundings; brute force without control only breeds ruin.", "ru": "Учись течь в гармонии с окружением; грубая сила без контроля порождает лишь разрушение."},

    # Cha Yeon-woo
    "awakening_system.npc.cha_yeonwoo.dialogue_title": {"es": "§c§l✦ Cha Yeon-woo ✦", "en": "§c§l✦ Cha Yeon-woo ✦", "ru": "§c§l✦ Чха Ён-У ✦"},
    "awakening_system.npc.cha_yeonwoo.title": {"es": "El Cazador Caín", "en": "The Hunter Cain", "ru": "Охотник Каин"},
    "awakening_system.npc.cha_yeonwoo.blessing_title": {"es": "§c§l✦ BENDICIÓN DE CHA YEON-WOO ✦", "en": "§c§l✦ CHA YEON-WOO'S BLESSING ✦", "ru": "§c§l✦ БЛАГОСЛОВЕНИЕ ЧХА ЁН-У ✦"},
    "awakening_system.npc.cha_yeonwoo.blessing_desc": {
        "es": "§eHas recibido las §4Alas del Dragón Negro§e.\n§7(Inmunidad al Fuego por 10 min, Fuerza II y Caída Lenta)",
        "en": "§eYou received §4Black Dragon Wings§e.\n§7(Fire Immunity for 10 min, Strength II and Slow Falling)",
        "ru": "§eВы получили §4Крылья Чёрного Дракона§e.\n§7(Иммунитет к огню на 10 мин, Сила II и Плавное падение)"
    },
    "awakening_system.npc.cha_yeonwoo.dialogue.0": {"es": "Mi hermano me dejó un diario. Cada escalón de esta Torre será conquistado y purgado.", "en": "My brother left me a pocket watch and a diary. Every floor of this Tower will be conquered and purged.", "ru": "Мой брат оставил мне дневник. Каждый этаж этой Башни будет покорён и очищен."},
    "awakening_system.npc.cha_yeonwoo.dialogue.1": {"es": "Las alas del Dragón Negro devoran a los falsos dioses del firmamento.", "en": "The Black Dragon's wings devour false gods of the heavens.", "ru": "Крылья Чёрного Дракона пожирают ложных богов небосвода."},
    "awakening_system.npc.cha_yeonwoo.dialogue.2": {"es": "Si vas a escalar la Torre de los Deseos, prepárate para no mirar hacia atrás jamás.", "en": "If you scale the Tower of Desires, prepare never to look back.", "ru": "Если поднимаешься по Башне Желаний, будь готов никогда не оглядываться назад."},
    "awakening_system.npc.cha_yeonwoo.dialogue.3": {"es": "La debilidad es un pecado en un mundo regido por las Constelaciones.", "en": "Weakness is a sin in a realm governed by Constellations.", "ru": "Слабость — это грех в мире, управляемом Созвездиями."},

    # Chung Myung
    "awakening_system.npc.chung_myung.dialogue_title": {"es": "§a§l✦ Chung Myung ✦", "en": "§a§l✦ Chung Myung ✦", "ru": "§a§l✦ Чхон Мён ✦"},
    "awakening_system.npc.chung_myung.title": {"es": "Dragón Divino del Monte Hua", "en": "Divine Dragon of Mount Hua", "ru": "Божественный Дракон Горы Хуа"},
    "awakening_system.npc.chung_myung.blessing_title": {"es": "§a§l✦ BENDICIÓN DE CHUNG MYUNG ✦", "en": "§a§l✦ CHUNG MYUNG'S BLESSING ✦", "ru": "§a§l✦ БЛАГОСЛОВЕНИЕ ЧХОН МЁНА ✦"},
    "awakening_system.npc.chung_myung.blessing_desc": {
        "es": "§eHas recibido el §aFlorecer del Monte Hua§e.\n§7(Prisa Minera III, Fuerza II, Velocidad II por 5 min)",
        "en": "§eYou received §aMount Hua Bloom§e.\n§7(Mining Haste III, Strength II, Speed II for 5 min)",
        "ru": "§eВы получили §aЦветение Горы Хуа§e.\n§7(Спешка добычи III, Сила II, Скорость II на 5 мин)"
    },
    "awakening_system.npc.chung_myung.dialogue.0": {"es": "¿Dónde está el licor de ciruelo? ¡Y más importante, dónde está el dinero de los ancianos de Shaolin!", "en": "Where's the plum blossom wine? And more importantly, where's Shaolin's money?!", "ru": "Где сливовое вино? И что важнее, где деньги старейшин Шаолиня?!"},
    "awakening_system.npc.chung_myung.dialogue.1": {"es": "En el Monte Hua no nos rendimos, ¡golpeamos el doble de fuerte hasta que el enemigo entienda!", "en": "At Mount Hua we don't surrender; we strike twice as hard until the enemy gets it!", "ru": "На горе Хуа мы не сдаёмся: мы бьём вдвое сильнее, пока враг не поймёт!"},
    "awakening_system.npc.chung_myung.dialogue.2": {"es": "¿Crees que entrenar 10,000 estocadas al día es duro? ¡En mis tiempos hacíamos 100,000 antes del desayuno!", "en": "Think 10,000 thrusts a day is hard? Back in my day we did 100,000 before breakfast!", "ru": "Думаешь, 10 000 уколов в день — тяжело? В моё время мы делали 100 000 до завтрака!"},
    "awakening_system.npc.chung_myung.dialogue.3": {"es": "Desenvaina tu espada. Si vas a blandir un filo, que corte a través de las dudas como el florecer de un ciruelo en invierno.", "en": "Draw your sword. If you wield a blade, cut through doubts like plum blossoms in deep winter.", "ru": "Обнажи свой меч. Если берёшься за клинок, пусть он рассекает сомнения, как цветы сливы в лютую зиму."},

    # Ryu Hanbin
    "awakening_system.npc.ryu_hanbin.dialogue_title": {"es": "§c§l✦ Ryu Hanbin ✦", "en": "§c§l✦ Ryu Hanbin ✦", "ru": "§c§l✦ Рю Ханбин ✦"},
    "awakening_system.npc.ryu_hanbin.title": {"es": "El Rey de la Espada Valter", "en": "The Valter Sword King", "ru": "Король Мечей Вальтера"},
    "awakening_system.npc.ryu_hanbin.blessing_title": {"es": "§c§l✦ BENDICIÓN DE RYU HANBIN ✦", "en": "§c§l✦ RYU HANBIN'S BLESSING ✦", "ru": "§c§l✦ БЛАГОСЛОВЕНИЕ РЮ ХАНБИНА ✦"},
    "awakening_system.npc.ryu_hanbin.blessing_desc": {
        "es": "§eHas recibido el §cÍmpetu Colosal de Valter§e.\n§7(Fuerza III, Resistencia II, Absorción IV por 5 min)",
        "en": "§eYou received §cValter's Colossal Impetus§e.\n§7(Strength III, Resistance II, Absorption IV for 5 min)",
        "ru": "§eВы получили §cКолоссальный Порыв Вальтера§e.\n§7(Сила III, Сопротивление II, Поглощение IV на 5 мин)"
    },
    "awakening_system.npc.ryu_hanbin.dialogue.0": {"es": "Pasé más de veinte años atrapado en la Dimensión Rocosa por culpa de una guía con error... Ya nada me asusta.", "en": "I spent over twenty years trapped in the Rocky Mountain because of a glitched guideline... Nothing scares me anymore.", "ru": "Я провёл более двадцати лет в Скалистых горах из-за бага руководства... Меня уже ничто не пугает."},
    "awakening_system.npc.ryu_hanbin.dialogue.1": {"es": "¿Mi nivel? Sigue diciendo 'Nivel 5', pero mi fuerza puede partir una montaña en dos.", "en": "My level? Still says 'Level 5', but my strength can split a mountain in two.", "ru": "Мой уровень? Всё ещё пишет «Уровень 5», но моей силы хватит, чтобы рассечь гору надвое."},
    "awakening_system.npc.ryu_hanbin.dialogue.2": {"es": "No compliques las cosas. Si una bestia o un monstruo te ataca, usa una espada más grande y aplástalo.", "en": "Don't overcomplicate things. If a beast attacks you, grab a bigger sword and smash it.", "ru": "Не усложняй. Если нападает зверь или монстр, возьми меч побольше и сокруши его."},
    "awakening_system.npc.ryu_hanbin.dialogue.3": {"es": "A veces extraño el pollo frito y el ramen de la Tierra... Este mundo de fantasía es un caos total.", "en": "Sometimes I miss fried chicken and ramen from Earth... This fantasy world is pure chaos.", "ru": "Иногда я скучаю по жареной курочке и рамэну с Земли... Этот фэнтезийный мир — сущий хаос."},

    # Twenty Fifth Baam
    "awakening_system.npc.twenty_fifth_baam.dialogue_title": {"es": "§b§l✦ Vigesimoquinto Baam ✦", "en": "§b§l✦ Twenty-Fifth Baam ✦", "ru": "§b§l✦ Двадцать Пятый Баам ✦"},
    "awakening_system.npc.twenty_fifth_baam.title": {"es": "El Irregular de la Torre", "en": "The Tower's Irregular", "ru": "Незаконный Башни"},
    "awakening_system.npc.twenty_fifth_baam.blessing_title": {"es": "§b§l✦ BENDICIÓN DE BAAM ✦", "en": "§b§l✦ BAAM'S BLESSING ✦", "ru": "§b§l✦ БЛАГОСЛОВЕНИЕ БААМА ✦"},
    "awakening_system.npc.twenty_fifth_baam.blessing_desc": {
        "es": "§eHas recibido el §bDominio del Shinsoo Primordial§e.\n§7(Regeneración II, Inmunidad Fuego 10m, Caída Lenta, Prisa II)",
        "en": "§eYou received §bPrimordial Shinsoo Mastery§e.\n§7(Regen II, Fire Immunity 10m, Slow Falling, Haste II)",
        "ru": "§eВы получили §bВласть Изначального Шинсу§e.\n§7(Регенерация II, Огнестойкость 10м, Плавное падение, Спешка II)"
    },
    "awakening_system.npc.twenty_fifth_baam.dialogue.0": {"es": "¿Qué es lo que buscas en la cima de la Torre? ¿Riquezas, gloria, o a las personas que te importan?", "en": "What are you seeking at the top of the Tower? Wealth, glory, or the ones you cherish?", "ru": "Чего ты ищешь на вершине Башни? Богатства, славы или тех, кто тебе дорог?"},
    "awakening_system.npc.twenty_fifth_baam.dialogue.1": {"es": "El Shinsoo no es un arma que debamos poseer; es el aliento del mundo que nos permite respirar juntos.", "en": "Shinsoo isn't a weapon to possess; it is the world's breath that allows us to live together.", "ru": "Шинсу — это не оружие для обладания; это дыхание мира, позволяющее нам дышать вместе."},
    "awakening_system.npc.twenty_fifth_baam.dialogue.2": {"es": "No permitiré que nadie vuelva a lastimar a mis compañeros. Si debo romper las reglas de la Torre, lo haré.", "en": "I won't let anyone hurt my friends again. If I must break the Tower's rules, I will.", "ru": "Я никому не позволю снова ранить моих товарищей. Если придётся нарушить правила Башни, я нарушу их."},
    "awakening_system.npc.twenty_fifth_baam.dialogue.3": {"es": "La Espina late en mi interior... Un poder que desafía el destino mismo de las familias de la Torre.", "en": "The Thorn beats within me... A power that defies the very fate of the Great Families.", "ru": "Шип пульсирует внутри меня... Сила, бросающая вызов самой судьбе Великих Семей."},

    # Elena Receptionist
    "awakening_system.npc.elena_receptionist.dialogue_title": {"es": "§6§l✦ Elena ✦", "en": "§6§l✦ Elena ✦", "ru": "§6§l✦ Елена ✦"},
    "awakening_system.npc.elena_receptionist.title": {"es": "Recepcionista del Gremio", "en": "Guild Receptionist", "ru": "Администратор Гильдии"},
    "awakening_system.npc.elena_receptionist.blessing_title": {"es": "§6§l✦ BENDICIÓN DE LA RECEPCIONISTA ✦", "en": "§6§l✦ RECEPTIONIST'S BLESSING ✦", "ru": "§6§l✦ БЛАГОСЛОВЕНИЕ АДМИНИСТРАТОРА ✦"},
    "awakening_system.npc.elena_receptionist.blessing_desc": {
        "es": "§eHas recibido el §6Favor del Gremio de Aventureros§e.\n§7(Fuerza I, Velocidad I y Héroe del Pueblo por 5 min)",
        "en": "§eYou received §6Adventurer Guild's Favor§e.\n§7(Strength I, Speed I, and Hero of the Village for 5 min)",
        "ru": "§eВы получили §6Благосклонность Гильдии Авантюристов§e.\n§7(Сила I, Скорость I и Герой деревни на 5 мин)"
    },
    "awakening_system.npc.elena_receptionist.dialogue.0": {"es": "¡Bienvenido al Gremio de Aventureros! Consulta el Tablón de la pared para aceptar contratos de caza.", "en": "Welcome to the Adventurers Guild! Check the Bounty Board on the wall for hunting contracts.", "ru": "Добро пожаловать в Гильдию Авантюристов! Посмотрите на доску объявлений для контрактов на охоту."},
    "awakening_system.npc.elena_receptionist.dialogue.1": {"es": "¿Buscas gloria y recompensas legítimas? Completa contratos para subir de Rango de Aventurero hasta el mítico Rango S.", "en": "Seeking glory and rightful rewards? Complete contracts to rank up all the way to mythical Rank S.", "ru": "Ищете славы и достойных наград? Выполняйте контракты, чтобы повысить ранг до легендарного S-Ранга."},
    "awakening_system.npc.elena_receptionist.dialogue.2": {"es": "No olvides mantener tu equipo en las mesas de herrería de la planta alta antes de adentrarte en el bosque.", "en": "Don't forget to upkeep your gear at the upstairs smithing tables before heading into the wild.", "ru": "Не забудьте починить снаряжение на кузнечных столах наверху перед походом в чащу."},
    "awakening_system.npc.elena_receptionist.dialogue.3": {"es": "Cada bestia que derrotas en nombre del gremio te otorga Medallas de Aventurero y prestigio en todo el reino.", "en": "Every beast slain in the guild's name earns you Adventurer Medals and realm-wide prestige.", "ru": "Каждый зверь, поверженный во имя гильдии, приносит вам Медали Авантюриста и славу в королевстве."}
}

# 4. Update all 11 lang files with NPC translations
lang_dir = "src/main/resources/assets/awakening_system/lang"
lang_files = [f for f in os.listdir(lang_dir) if f.endswith(".json")]

for fname in lang_files:
    fpath = os.path.join(lang_dir, fname)
    with open(fpath, "r", encoding="utf-8") as f:
        data = json.load(f)
    
    lang_type = "en" if fname == "en_us.json" else ("ru" if fname == "ru_ru.json" else "es")
    
    added = 0
    for k, v in NPC_TRANSLATIONS.items():
        val = v.get(lang_type, v["es"])
        if k not in data or data[k] != val:
            data[k] = val
            added += 1
            
    with open(fpath, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"Updated {fname}: added/updated {added} keys. Total keys now: {len(data)}")

# 5. Patch SeriesNpcEntity.java
sne_path = "src/main/java/com/soybe/awakeningsystem/entity/SeriesNpcEntity.java"
with open(sne_path, "r", encoding="utf-8") as f:
    sne_code = f.read()

# Update custom name
old_custom_name = 'this.setCustomName(Component.literal(p.titleColor() + p.displayName() + " §7[" + p.title() + "]"));'
new_custom_name = 'this.setCustomName(Component.literal(p.titleColor() + p.displayName() + " §7[").append(Component.translatable("awakening_system.npc." + p.id() + ".title")).append(Component.literal("]")));'
sne_code = sne_code.replace(old_custom_name, new_custom_name)

# Update dialogue interaction notification
old_dialogue_notify = """            // Mensaje en pantalla estilo Sistema
            AwakeningSystemMod.notify(serverPlayer,
                    "literal:" + profile.titleColor() + "✦ " + profile.displayName() + " ✦",
                    "literal:§f\\"" + quote + "\\"",
                    0xFFFFD700);"""
new_dialogue_notify = """            // Mensaje en pantalla estilo Sistema
            int diagIdx = (int) (Math.random() * 4);
            AwakeningSystemMod.notify(serverPlayer,
                    "awakening_system.npc." + profile.id() + ".dialogue_title",
                    "awakening_system.npc." + profile.id() + ".dialogue." + diagIdx,
                    0xFFFFD700);"""
sne_code = sne_code.replace(old_dialogue_notify, new_dialogue_notify)

with open(sne_path, "w", encoding="utf-8") as f:
    f.write(sne_code)
print("Updated SeriesNpcEntity.java")

# 6. Patch NpcProfile.java blessings
np_path = "src/main/java/com/soybe/awakeningsystem/entity/NpcProfile.java"
with open(np_path, "r", encoding="utf-8") as f:
    np_code = f.read()

npc_ids = [
    "sung_jinwoo", "cha_haein", "kim_dokja", "yoo_joonghyuk",
    "lloyd_frontera", "javier_asrahan", "cheon_yeowoon", "arthur_leywin",
    "cha_yeonwoo", "chung_myung", "ryu_hanbin", "twenty_fifth_baam",
    "elena_receptionist"
]

for nid in npc_ids:
    # regex find AwakeningSystemMod.notify(player, "literal:.*?", "literal:.*?", (0x[0-9A-Fa-f]+));
    pattern = re.compile(
        r'AwakeningSystemMod\.notify\(player,\s*"literal:[^"]+",\s*"literal:[^"]+",\s*(0x[0-9A-Fa-f]+)\);',
        re.DOTALL
    )
    m = pattern.search(np_code)
    if m:
        color = m.group(1)
        replacement = f'AwakeningSystemMod.notify(player, "awakening_system.npc.{nid}.blessing_title", "awakening_system.npc.{nid}.blessing_desc", {color});'
        np_code = np_code[:m.start()] + replacement + np_code[m.end():]
        print(f"Patched blessing for {nid}")

with open(np_path, "w", encoding="utf-8") as f:
    f.write(np_code)
print("Updated NpcProfile.java")

print("All NPC translations script finished successfully!")
