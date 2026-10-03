import json
import os

LANG_DIR = "src/main/resources/assets/awakening_system/lang"

# 1. New Teyvat Wood Blocks (ES, EN, RU)
TEYVAT_WOOD_KEYS_ES = {
    "block.awakening_system.cuihua_log": "Tronco de Cuihua",
    "item.awakening_system.cuihua_log": "Tronco de Cuihua",
    "block.awakening_system.cuihua_leaves": "Hojas de Cuihua",
    "item.awakening_system.cuihua_leaves": "Hojas de Cuihua",
    "block.awakening_system.cuihua_sapling": "Brote de Cuihua",
    "item.awakening_system.cuihua_sapling": "Brote de Cuihua",
    "block.awakening_system.cuihua_planks": "Madera de Cuihua",
    "item.awakening_system.cuihua_planks": "Madera de Cuihua",

    "block.awakening_system.adhigama_log": "Tronco de Adhigama",
    "item.awakening_system.adhigama_log": "Tronco de Adhigama",
    "block.awakening_system.adhigama_leaves": "Hojas de Adhigama",
    "item.awakening_system.adhigama_leaves": "Hojas de Adhigama",
    "block.awakening_system.adhigama_sapling": "Brote de Adhigama",
    "item.awakening_system.adhigama_sapling": "Brote de Adhigama",
    "block.awakening_system.adhigama_planks": "Madera de Adhigama",
    "item.awakening_system.adhigama_planks": "Madera de Adhigama",

    "block.awakening_system.sandbearer_log": "Tronco de Madera Arenosa",
    "item.awakening_system.sandbearer_log": "Tronco de Madera Arenosa",
    "block.awakening_system.sandbearer_leaves": "Hojas de Madera Arenosa",
    "item.awakening_system.sandbearer_leaves": "Hojas de Madera Arenosa",
    "block.awakening_system.sandbearer_sapling": "Brote de Madera Arenosa",
    "item.awakening_system.sandbearer_sapling": "Brote de Madera Arenosa",
    "block.awakening_system.sandbearer_planks": "Madera Arenosa Pulida",
    "item.awakening_system.sandbearer_planks": "Madera Arenosa Pulida",
}

TEYVAT_WOOD_KEYS_EN = {
    "block.awakening_system.cuihua_log": "Cuihua Log",
    "item.awakening_system.cuihua_log": "Cuihua Log",
    "block.awakening_system.cuihua_leaves": "Cuihua Leaves",
    "item.awakening_system.cuihua_leaves": "Cuihua Leaves",
    "block.awakening_system.cuihua_sapling": "Cuihua Sapling",
    "item.awakening_system.cuihua_sapling": "Cuihua Sapling",
    "block.awakening_system.cuihua_planks": "Cuihua Planks",
    "item.awakening_system.cuihua_planks": "Cuihua Planks",

    "block.awakening_system.adhigama_log": "Adhigama Log",
    "item.awakening_system.adhigama_log": "Adhigama Log",
    "block.awakening_system.adhigama_leaves": "Adhigama Leaves",
    "item.awakening_system.adhigama_leaves": "Adhigama Leaves",
    "block.awakening_system.adhigama_sapling": "Adhigama Sapling",
    "item.awakening_system.adhigama_sapling": "Adhigama Sapling",
    "block.awakening_system.adhigama_planks": "Adhigama Planks",
    "item.awakening_system.adhigama_planks": "Adhigama Planks",

    "block.awakening_system.sandbearer_log": "Sandbearer Log",
    "item.awakening_system.sandbearer_log": "Sandbearer Log",
    "block.awakening_system.sandbearer_leaves": "Sandbearer Leaves",
    "item.awakening_system.sandbearer_leaves": "Sandbearer Leaves",
    "block.awakening_system.sandbearer_sapling": "Sandbearer Sapling",
    "item.awakening_system.sandbearer_sapling": "Sandbearer Sapling",
    "block.awakening_system.sandbearer_planks": "Sandbearer Planks",
    "item.awakening_system.sandbearer_planks": "Sandbearer Planks",
}

TEYVAT_WOOD_KEYS_RU = {
    "block.awakening_system.cuihua_log": "Бревно цуйхуа",
    "item.awakening_system.cuihua_log": "Бревно цуйхуа",
    "block.awakening_system.cuihua_leaves": "Листья цуйхуа",
    "item.awakening_system.cuihua_leaves": "Листья цуйхуа",
    "block.awakening_system.cuihua_sapling": "Саженец цуйхуа",
    "item.awakening_system.cuihua_sapling": "Саженец цуйхуа",
    "block.awakening_system.cuihua_planks": "Доски цуйхуа",
    "item.awakening_system.cuihua_planks": "Доски цуйхуа",

    "block.awakening_system.adhigama_log": "Бревно адхигамы",
    "item.awakening_system.adhigama_log": "Бревно адхигамы",
    "block.awakening_system.adhigama_leaves": "Листья адхигамы",
    "item.awakening_system.adhigama_leaves": "Листья адхигамы",
    "block.awakening_system.adhigama_sapling": "Саженец адхигамы",
    "item.awakening_system.adhigama_sapling": "Саженец адхигамы",
    "block.awakening_system.adhigama_planks": "Доски адхигамы",
    "item.awakening_system.adhigama_planks": "Доски адхигамы",

    "block.awakening_system.sandbearer_log": "Бревно песчаного дерева",
    "item.awakening_system.sandbearer_log": "Бревно песчаного дерева",
    "block.awakening_system.sandbearer_leaves": "Листья песчаного дерева",
    "item.awakening_system.sandbearer_leaves": "Листья песчаного дерева",
    "block.awakening_system.sandbearer_sapling": "Саженец песчаного дерева",
    "item.awakening_system.sandbearer_sapling": "Саженец песчаного дерева",
    "block.awakening_system.sandbearer_planks": "Доски песчаного дерева",
    "item.awakening_system.sandbearer_planks": "Доски песчаного дерева",
}

# 2. Missing English translations (Columbina welkin & quest)
MISSING_EN = {
    "awakening_system.welkin.columbina_title": "Columbina's Celestial Blessing",
    "awakening_system.welkin.columbina_header": "§d§l✦ C O L U M B I N A ' S   C E L E S T I A L   B L E S S I N G ✦",
    "awakening_system.welkin.columbina_subtitle": "§b✦ Song of the Serene Moon • +50% Primogems (135/day) ✦",
    "awakening_system.welkin.columbina_days_remaining": "DAYS REMAINING: §e§l%s §fDAYS · §d[ENHANCED +50%]",
    "awakening_system.welkin.columbina_claim_button": "§f§l✦ CLICK HERE OR PRESS SPACE (+%s PRIMOGEMS) ✦",
    "awakening_system.gacha.topup.welkin_enhanced_name": "🌙 Columbina's Blessing (135/d)",
    "awakening_system.gacha.topup.welkin_enhanced_activated": "§d§l✦ COLUMBINA'S BLESSING ACTIVATED ✦ §e+450 Genesis Crystals and 30 days of enhanced blessing (135 daily Primogems).",
    "awakening_system.gacha.topup.welkin_enhanced_daily_claimed": "§d§l✦ COLUMBINA'S CELESTIAL BLESSING ✦ §eYou claimed your %s daily Primogems (+50% bonus)! (Days remaining: %s)",
    "awakening_system.welkin.quest.already_enhanced": "§d✦ You already have Columbina's Celestial Blessing active (+50% Primogems).",
    "block.awakening_system.trilunar_altar": "Trilunar Goddess Altar",
    "item.awakening_system.trilunar_altar": "Trilunar Goddess Altar",
    "item.awakening_system.lunar_authority_aria": "Lunar Authority of Aria (Full Moon)",
    "item.awakening_system.lunar_authority_aria.desc": "Pure essence of the full moon governing eternal celestial light and purifying Abyss shadows.",
    "item.awakening_system.lunar_authority_sonnet": "Lunar Authority of Sonnet (Crescent Moon)",
    "item.awakening_system.lunar_authority_sonnet.desc": "Melodious essence of the crescent moon guiding travelers through gloom with celestial harmony.",
    "item.awakening_system.lunar_authority_canon": "Lunar Authority of Canon (New Moon)",
    "item.awakening_system.lunar_authority_canon.desc": "Immutable essence of the new moon governing mysteries of fate and cosmic tides.",
    "item.awakening_system.trilunar_guide": "Trilunar Goddess Guide",
    "awakening_system.welkin.quest.started_title": "QUEST: TRILUNAR AWAKENING",
    "awakening_system.welkin.quest.step1_desc": "Travel with Columbina to the Ancient Shrine of the Three Moon Goddesses and reclaim her powers.",
    "awakening_system.welkin.quest.started_chat": "§6[SYSTEM] §d✦ Quest started: Help Columbina recover her Three Lunar Authorities (Aria, Sonnet, Canon) at the Ancient Shrine.",
    "awakening_system.welkin.quest.step1_status": "§d✦ Columbina's Quest: Travel to the Trilunar Shrine and overcome the trials of the Moon Goddesses.",
    "awakening_system.welkin.quest.step2_status": "§d✦ Columbina's Quest: Gather the Three Lunar Authorities (Aria, Sonnet, and Canon).",
    "awakening_system.welkin.quest.step3_status": "§d✦ Columbina's Quest: Consecrate the Three Authorities at the Trilunar Altar.",
    "awakening_system.welkin.quest.progress_title": "ASCENSION QUEST",
    "awakening_system.welkin.quest.step1_progress_msg": "Lunar Powers Recovered: %s / %s.",
    "awakening_system.welkin.quest.step1_complete_title": "TRIAL OVERCOME",
    "awakening_system.welkin.quest.step2_desc": "Overcome the trials of Aria, Sonnet, and Canon at the Shrine.",
    "awakening_system.welkin.quest.step2_started_chat": "§6[SYSTEM] §d✦ You recovered a Lunar Authority! Gather the remaining ones for the Final Rite.",
    "awakening_system.welkin.quest.step2_complete_title": "AUTHORITIES GATHERED",
    "awakening_system.welkin.quest.step3_desc": "Consecration at the Trilunar Altar: Awaken the true power of the Trilunar Goddess.",
    "awakening_system.welkin.quest.step3_started_chat": "§6[SYSTEM] §d✦ All Three Authorities are gathered! Touch the Trilunar Altar to complete Columbina's awakening.",
    "awakening_system.welkin.quest.completed_title": "TRILUNAR GODDESS AWAKENED!",
    "awakening_system.welkin.quest.completed_desc": "Columbina has reclaimed her power! Permanent blessing of +135 Primogems/day (+50%), Genesis Crystals, and 5★ Contract.",
    "awakening_system.welkin.quest.completed_chat": "§6§l[SYSTEM] §d§l✦ Columbina has been reborn as the Trilunar Goddess! Your Lunar Blessing now grants §e135 Primogems daily§d (+50% bonus), +450 Genesis Crystals, +300 Primogems, and her 5★ Summoning Contract.",
    "awakening_system.screen.missions.welkin_enhanced_active": "§d✦ Columbina: Trilunar Goddess Awakened (+50% Daily Primogems)",
    "awakening_system.screen.missions.welkin_quest_ready": "§e✦ Lunar Ascension: Start Columbina's Quest in Lunar Blessing",
    "awakening_system.screen.missions.welkin_quest_step1": "§d✦ Columbina: Journey to the Shrine of the Three Moon Goddesses",
    "awakening_system.screen.missions.welkin_quest_step2": "§d✦ Columbina: Recover the Three Lunar Authorities (Aria, Sonnet, Canon)",
    "awakening_system.screen.missions.welkin_quest_step3": "§d✦ Columbina: Consecrate the Authorities at the Trilunar Altar",
}

# 3. Comprehensive Russian translations for missing keys in ru_ru.json
MISSING_RU = {
    # Alchemy & Cauldron
    "awakening_system.alchemy.recipe.4.title": "Небесный щит",
    "awakening_system.alchemy.recipe.4.tier": "§b[Ранг V]",
    "awakening_system.alchemy.recipe.4.desc1": "§7Иммунитет к мгновенной гибели от молнии Дао.",
    "awakening_system.alchemy.recipe.4.desc2": "§b-70% урона от Небесного бедствия (3 мин).",
    "awakening_system.alchemy.recipe.5.title": "Трансмутация души",
    "awakening_system.alchemy.recipe.5.tier": "§6[Ранг VI]",
    "awakening_system.alchemy.recipe.5.desc1": "§7Концентрирует изначальную эссенцию мира.",
    "awakening_system.alchemy.recipe.5.desc2": "§e+600 Ци и 100 000 Монет.",
    "awakening_system.cauldron.missing_ingredients_5": "§e[Не хватает ингредиентов: Снежный лотос или Духовный нефрит]",
    "awakening_system.cauldron.missing_ingredients_6": "§e[Не хватает ингредиентов: Дикий женьшень или Духовный нефрит]",

    # Columbina Welkin
    "awakening_system.welkin.columbina_title": "Небесное благословение Коломбины",
    "awakening_system.welkin.columbina_header": "§d§l✦ Н Е Б Е С Н О Е   Б Л А Г О С Л О В Е Н И Е   К О Л О М Б И Н Ы ✦",
    "awakening_system.welkin.columbina_subtitle": "§b✦ Песнь безмятежной луны • +50% Камней Истока (135/день) ✦",
    "awakening_system.welkin.columbina_days_remaining": "ДНЕЙ ОСТАЛОСЬ: §e§l%s §fДН. · §d[УЛУЧШЕНО +50%]",
    "awakening_system.welkin.columbina_claim_button": "§f§l✦ НАЖМИТЕ ЗДЕСЬ ИЛИ ПРОБЕЛ (+%s КАМНЕЙ ИСТОКА) ✦",
    "awakening_system.gacha.topup.welkin_enhanced_name": "🌙 Благословение Коломбины (135/д)",
    "awakening_system.gacha.topup.welkin_enhanced_activated": "§d§l✦ БЛАГОСЛОВЕНИЕ КОЛОМБИНЫ АКТИВИРОВАНО ✦ §e+450 Кристаллов Сотворения и 30 дней улучшенного благословения (135 Камней Истока ежедневно).",
    "awakening_system.gacha.topup.welkin_enhanced_daily_claimed": "§d§l✦ НЕБЕСНОЕ БЛАГОСЛОВЕНИЕ КОЛОМБИНЫ ✦ §eВы забрали свои %s Камней Истока (+50% бонус)! (Осталось дней: %s)",
    "awakening_system.welkin.quest.already_enhanced": "§d✦ У вас уже активно Небесное благословение Коломбины (+50% Камней Истока).",

    # Trilunar Altar & Lunar Authorities
    "block.awakening_system.trilunar_altar": "Алтарь Трехлунной Богини",
    "item.awakening_system.trilunar_altar": "Алтарь Трехлунной Богини",
    "item.awakening_system.lunar_authority_aria": "Лунная власть Арии (Полнолуние)",
    "item.awakening_system.lunar_authority_aria.desc": "Чистая эссенция полной луны, правящая вечным небесным светом и очищающая тьму Бездны.",
    "item.awakening_system.lunar_authority_sonnet": "Лунная власть Сонета (Растущая луна)",
    "item.awakening_system.lunar_authority_sonnet.desc": "Мелодичная эссенция растущей луны, направляющая странников во мраке своей гармонией.",
    "item.awakening_system.lunar_authority_canon": "Лунная власть Канона (Новолуние)",
    "item.awakening_system.lunar_authority_canon.desc": "Нерушимая эссенция новолуния, управляющая тайнами судьбы и космическими приливами.",
    "item.awakening_system.trilunar_guide": "Руководство Трехлунной Богини",

    # Welkin Quest
    "awakening_system.welkin.quest.started_title": "ЗАДАНИЕ: ПРОБУЖДЕНИЕ ТРЕХЛУНИЯ",
    "awakening_system.welkin.quest.step1_desc": "Отправьтесь с Коломбиной в древнее Святилище Трех Богинь Луны и верните её силы.",
    "awakening_system.welkin.quest.started_chat": "§6[СИСТЕМА] §d✦ Задание начато: Помогите Коломбине вернуть Три Лунные Власти (Арию, Сонет, Канон) в древнем Святилище.",
    "awakening_system.welkin.quest.step1_status": "§d✦ Задание Коломбины: Отправляйтесь в Святилище Трехлуния и пройдите испытания Богинь Луны.",
    "awakening_system.welkin.quest.step2_status": "§d✦ Задание Коломбины: Соберите Три Лунные Власти (Арию, Сонет и Канон).",
    "awakening_system.welkin.quest.step3_status": "§d✦ Задание Коломбины: Освятите Три Власти на Алтаре Трехлуния.",
    "awakening_system.welkin.quest.progress_title": "ИСПЫТАНИЕ ВОЗВЫШЕНИЯ",
    "awakening_system.welkin.quest.step1_progress_msg": "Лунных сил возвращено: %s / %s.",
    "awakening_system.welkin.quest.step1_complete_title": "ИСПЫТАНИЕ ПРОЙДЕНО",
    "awakening_system.welkin.quest.step2_desc": "Пройдите испытания Арии, Сонета и Канона в Святилище.",
    "awakening_system.welkin.quest.step2_started_chat": "§6[СИСТЕМА] §d✦ Вы вернули Лунную Власть! Соберите оставшиеся для Финального Ритуала.",
    "awakening_system.welkin.quest.step2_complete_title": "ВСЕ ВЛАСТИ СОБРАНЫ",
    "awakening_system.welkin.quest.step3_desc": "Освящение на Алтаре Трехлуния: Пробудите истинную мощь Трехлунной Богини.",
    "awakening_system.welkin.quest.step3_started_chat": "§6[СИСТЕМА] §d✦ Все Три Власти готовы! Коснитесь Алтаря Трехлуния, чтобы завершить пробуждение Коломбины.",
    "awakening_system.welkin.quest.completed_title": "ТРЕХЛУННАЯ БОГИНЯ ПРОБУЖДЕНА!",
    "awakening_system.welkin.quest.completed_desc": "Коломбина вернула свои силы! Постоянное благословение +135 Камней Истока/день (+50%), Кристаллы Сотворения и контракт 5★.",
    "awakening_system.welkin.quest.completed_chat": "§6§l[СИСТЕМА] §d§l✦ Коломбина переродилась как Трехлунная Богиня! Ваше Лунное благословение теперь дает §e135 Камней Истока ежедневно§d (+50% бонус), +450 Кристаллов Сотворения, +300 Камней Истока и её 5★ Контракт призыва.",
    "awakening_system.screen.missions.welkin_enhanced_active": "§d✦ Коломбина: Трехлунная Богиня пробуждена (+50% ежедневных Камней Истока)",
    "awakening_system.screen.missions.welkin_quest_ready": "§e✦ Лунное возвышение: Начните задание Коломбины в Лунном благословении",
    "awakening_system.screen.missions.welkin_quest_step1": "§d✦ Коломбина: Отправляйтесь в Святилище Трех Богинь Луны",
    "awakening_system.screen.missions.welkin_quest_step2": "§d✦ Коломбина: Верните Три Лунные Власти (Арию, Сонет, Канон)",
    "awakening_system.screen.missions.welkin_quest_step3": "§d✦ Коломбина: Освятите Власти на Алтаре Трехлуния",

    # Weapons (Bows)
    "item.awakening_system.skyward_harp": "Небесное крыло",
    "item.awakening_system.polar_star": "Полярная звезда",
    "item.awakening_system.hunter_path": "Охотничья тропа",
    "item.awakening_system.hamayumi": "Хамаюми",
    "item.awakening_system.end_of_the_line": "Приближение конца",

    # Artifacts Tab & Artifacts
    "awakening_system.screen.tab.artifacts": "Артефакты",
    "item.awakening_system.artifact_crimson_witch_flower": "Цветок жизни (Горящая алая ведьма)",
    "item.awakening_system.artifact_crimson_witch_plume": "Перо смерти (Горящая алая ведьма)",
    "item.awakening_system.artifact_crimson_witch_sands": "Пески времени (Горящая алая ведьма)",
    "item.awakening_system.artifact_crimson_witch_goblet": "Кубок пространства (Горящая алая ведьма)",
    "item.awakening_system.artifact_crimson_witch_circlet": "Корона разума (Горящая алая ведьма)",

    "item.awakening_system.artifact_gladiator_flower": "Цветок жизни (Конец гладиатора)",
    "item.awakening_system.artifact_gladiator_plume": "Перо смерти (Конец гладиатора)",
    "item.awakening_system.artifact_gladiator_sands": "Пески времени (Конец гладиатора)",
    "item.awakening_system.artifact_gladiator_goblet": "Кубок пространства (Конец гладиатора)",
    "item.awakening_system.artifact_gladiator_circlet": "Корона разума (Конец гладиатора)",

    "item.awakening_system.artifact_viridescent_flower": "Цветок жизни (Изумрудная тень)",
    "item.awakening_system.artifact_viridescent_plume": "Перо смерти (Изумрудная тень)",
    "item.awakening_system.artifact_viridescent_sands": "Пески времени (Изумрудная тень)",
    "item.awakening_system.artifact_viridescent_goblet": "Кубок пространства (Изумрудная тень)",
    "item.awakening_system.artifact_viridescent_circlet": "Корона разума (Изумрудная тень)",

    "item.awakening_system.artifact_maiden_flower": "Цветок жизни (Возлюбленная юная дева)",
    "item.awakening_system.artifact_maiden_plume": "Перо смерти (Возлюбленная юная дева)",
    "item.awakening_system.artifact_maiden_sands": "Пески времени (Возлюбленная юная дева)",
    "item.awakening_system.artifact_maiden_goblet": "Кубок пространства (Возлюбленная юная дева)",
    "item.awakening_system.artifact_maiden_circlet": "Корона разума (Возлюбленная юная дева)",

    "item.awakening_system.artifact_bloodstained_flower": "Цветок жизни (Рыцарь крови)",
    "item.awakening_system.artifact_bloodstained_plume": "Перо смерти (Рыцарь крови)",
    "item.awakening_system.artifact_bloodstained_sands": "Пески времени (Рыцарь крови)",
    "item.awakening_system.artifact_bloodstained_goblet": "Кубок пространства (Рыцарь крови)",
    "item.awakening_system.artifact_bloodstained_circlet": "Корона разума (Рыцарь крови)",

    "item.awakening_system.artifact_berserker_flower": "Цветок жизни (Берсерк)",
    "item.awakening_system.artifact_berserker_plume": "Перо смерти (Берсерк)",
    "item.awakening_system.artifact_berserker_sands": "Пески времени (Берсерк)",
    "item.awakening_system.artifact_berserker_goblet": "Кубок пространства (Берсерк)",
    "item.awakening_system.artifact_berserker_circlet": "Корона разума (Берсерк)",

    "item.awakening_system.artifact_exile_flower": "Цветок жизни (Изгнанник)",
    "item.awakening_system.artifact_exile_plume": "Перо смерти (Изгнанник)",
    "item.awakening_system.artifact_exile_sands": "Пески времени (Изгнанник)",
    "item.awakening_system.artifact_exile_goblet": "Кубок пространства (Изгнанник)",
    "item.awakening_system.artifact_exile_circlet": "Корона разума (Изгнанник)",

    "item.awakening_system.artifact_brave_heart_flower": "Цветок жизни (Душа храбреца)",
    "item.awakening_system.artifact_brave_heart_plume": "Перо смерти (Душа храбреца)",
    "item.awakening_system.artifact_brave_heart_sands": "Пески времени (Душа храбреца)",
    "item.awakening_system.artifact_brave_heart_goblet": "Кубок пространства (Душа храбреца)",
    "item.awakening_system.artifact_brave_heart_circlet": "Корона разума (Душа храбреца)",

    # Ascension Gemstones
    "item.awakening_system.agnidus_agate_fragment": "Осколок агата Агнидус",
    "item.awakening_system.agnidus_agate_chunk": "Кусок агата Агнидус",
    "item.awakening_system.agnidus_agate_gemstone": "Драгоценный агат Агнидус",

    "item.awakening_system.shivada_jade_fragment": "Осколок нефрита Шивада",
    "item.awakening_system.shivada_jade_chunk": "Кусок нефрита Шивада",
    "item.awakening_system.shivada_jade_gemstone": "Драгоценный нефрит Шивада",

    "item.awakening_system.vayuda_turquoise_fragment": "Осколок бирюзы Вайюда",
    "item.awakening_system.vayuda_turquoise_chunk": "Кусок бирюзы Вайюда",
    "item.awakening_system.vayuda_turquoise_gemstone": "Драгоценная бирюза Вайюда",

    "item.awakening_system.varunada_lazurite_fragment": "Осколок лазурита Варунада",
    "item.awakening_system.varunada_lazurite_chunk": "Кусок лазурита Варунада",
    "item.awakening_system.varunada_lazurite_gemstone": "Драгоценный лазурит Варунада",

    "item.awakening_system.prithiva_topaz_fragment": "Осколок топаза Притхива",
    "item.awakening_system.prithiva_topaz_chunk": "Кусок топаза Притхива",
    "item.awakening_system.prithiva_topaz_gemstone": "Драгоценный топаз Притхива",

    "item.awakening_system.nagadus_emerald_fragment": "Осколок изумруда Нагадус",
    "item.awakening_system.nagadus_emerald_chunk": "Кусок изумруда Нагадус",
    "item.awakening_system.nagadus_emerald_gemstone": "Драгоценный изумруд Нагадус",

    "item.awakening_system.vajrada_amethyst_fragment": "Осколок аметиста Ваджрада",
    "item.awakening_system.vajrada_amethyst_chunk": "Кусок аметиста Ваджрада",
    "item.awakening_system.vajrada_amethyst_gemstone": "Драгоценный аметист Ваджрада",

    # Enhancement Ores, Crown, Foods
    "item.awakening_system.mystic_enhancement_ore": "Волшебная руда усиления",
    "item.awakening_system.fine_enhancement_ore": "Превосходная руда усиления",
    "item.awakening_system.enhancement_ore": "Обычная руда усиления",
    "item.awakening_system.crown_of_insight": "Корона прозрения",
    "item.awakening_system.mora_pouch": "Кошель моры искателя приключений",
    "item.awakening_system.mora_bundle": "Золотой мешок моры",
    "item.awakening_system.adventurers_experience": "Опыт искателя приключений",
    "item.awakening_system.crystal_core": "Кристальное ядро",
    "item.awakening_system.adeptus_temptation": "Искушение Адепта",
    "item.awakening_system.sweet_madame": "Цыплёнок в медовом соусе",
    "item.awakening_system.almond_tofu": "Миндальный тофу",

    # Cultivation & Dao Techniques
    "awakening_system.screen.cultivation.tab_meridians": "§b☯ Меридианы (12)",
    "awakening_system.screen.cultivation.tab_dao": "§e⚡ Техники Дао (5)",
    "awakening_system.screen.cultivation.tip": "§e☯ Совет: §7Медитируйте сидя или присев в местах с высокой концентрацией Ци для её накопления.",
    "awakening_system.screen.cultivation.tribulation_active": "§c⚡ Небесное бедствие: %s/%s",
    "awakening_system.screen.cultivation.dao_shield": "§a🛡 Щит Дао: %sс",
    "awakening_system.screen.cultivation.dao_equipped_prefix": "§6[★ НАЗНАЧЕНО НА J] ",
    "awakening_system.screen.cultivation.dao_req_realm": "§cТребуется Сфера %s (%s).",
    "awakening_system.screen.cultivation.dao_assigned": "§6★ Назначено",
    "awakening_system.screen.cultivation.dao_assign": "§fНазначить [J]",
    "awakening_system.screen.cultivation.dao_use": "§fИспользовать",
    "awakening_system.screen.cultivation.dao_locked": "§cЗаблокировано",
    "awakening_system.screen.cultivation.dao_help": "§e⚡ Техники Дао: §7Нажмите §a[J] §7в бою для применения назначенной техники.",
    "awakening_system.dao_skill.0.name": "Летающий духовный меч",
    "awakening_system.dao_skill.0.desc": "Призывает 3 клинка сияющей Ци, которые преследуют врагов на сверхзвуковой скорости, пробивая защиту и взрываясь небесным светом.",
    "awakening_system.dao_skill.1.name": "Ладонь небесной пустоты",
    "awakening_system.dao_skill.1.desc": "Сжимает ткань пространства в ладони и высвобождает ударную волну в конусе на 14 блоков, отбрасывая и дезориентируя врагов.",
    "awakening_system.dao_skill.2.name": "Владение золотым ядром",
    "awakening_system.dao_skill.2.desc": "Создает священный барьер радиусом 12м. Быстро восстанавливает здоровье и Ци, замедляя врагов и сжигая их духовным пламенем.",
    "awakening_system.dao_skill.3.name": "Проекция зарождающейся души",
    "awakening_system.dao_skill.3.desc": "Направляет аватар духовной души. Дарует мистическое ускорение, щиты поглощения и спасает культиватора от смертельного удара.",
    "awakening_system.dao_skill.4.name": "Небесный шаг духовной вспышки",
    "awakening_system.dao_skill.4.desc": "Разрывает пустоту и мгновенно телепортирует на 16 блоков вперед, оставляя иллюзорную копию из Ци, детонирующую небесным громом.",

    # Biomes
    "biome.awakening_system.teyvat_dragonspine": "Драконий хребет (Dragonspine)",
    "biome.awakening_system.teyvat_dawn_winery": "Винокурня «Рассвет»",
    "biome.awakening_system.teyvat_cider_lake": "Сидровое озеро (Cider Lake)",
    "biome.awakening_system.teyvat_stormterror_lair": "Логово Ужаса бури",
    "biome.awakening_system.teyvat_dihua_marsh": "Тростниковые острова Ли Юэ",
    "biome.awakening_system.teyvat_guyun_stone_forest": "Каменный лес Гуюнь",
    "biome.awakening_system.teyvat_the_chasm": "Разлом Ли Юэ (The Chasm)",
    "biome.awakening_system.teyvat_narukami_island": "Остров Наруками (Инадзума)",
    "biome.awakening_system.teyvat_watatsumi_island": "Остров Ватацуми (Инадзума)",
    "biome.awakening_system.teyvat_yashiori_island": "Остров Ясиори (Инадзума)",
    "biome.awakening_system.teyvat_dharma_forest": "Лес Дхармы (Сумеру)",
    "biome.awakening_system.teyvat_hypostyle_desert": "Пустыня Колоннад (Сумеру)",
    "biome.awakening_system.teyvat_fontaine_waters": "Воды и плато Фонтейна",

    # Monster Drops & Materials
    "item.awakening_system.condensed_resin": "Густая смола (4★)",
    "item.awakening_system.slime_condensate": "Слизь слайма (1★)",
    "item.awakening_system.slime_secretions": "Выделения слайма (2★)",
    "item.awakening_system.slime_concentrate": "Концентрат слайма (3★)",
    "item.awakening_system.damaged_mask": "Разбитая маска (1★)",
    "item.awakening_system.stained_mask": "Грязная маска (2★)",
    "item.awakening_system.ominous_mask": "Зловещая маска (3★)",
    "item.awakening_system.heavy_horn": "Тяжёлый рог (2★)",
    "block.awakening_system.teyvat_jar": "Сосуд с сокровищами Тейвата",

    # Pouches
    "item.awakening_system.wanderer_pouch": "Мешочек странника",
    "item.awakening_system.adventurer_pouch": "Мешочек искателя приключений",
    "item.awakening_system.outlander_pouch": "Мешочек чужеземца",
    "item.awakening_system.emergency_pouch": "Аварийный мешочек для хранения",
    "item.awakening_system.pouch.capacity": "§6Вместимость: §e%s ячеек",
    "item.awakening_system.pouch.lore": "§7Расширяемое портативное пространственное хранилище.",

    # Primogem tools, Mora block & armor, Curios
    "item.awakening_system.primogem_pickaxe": "Кирка Камня Истока (5★)",
    "item.awakening_system.primogem_sword": "Меч живого Камня Истока (5★)",
    "block.awakening_system.mora_block": "Блок моры",
    "item.awakening_system.mora_helmet": "Шлем из золотой моры",
    "item.awakening_system.mora_chestplate": "Нагрудник из золотой моры",
    "item.awakening_system.mora_leggings": "Поножи из золотой моры",
    "item.awakening_system.mora_boots": "Сапоги из золотой моры",
    "awakening_system.armor_set.mora.header": "§e§l[КОМПЛЕКТ: ЗОЛОТОЙ БАРЬЕР МОРЫ]",
    "item.awakening_system.omniscience_pouch": "Всеведущий мешочек (Диковина 5★)",
    "item.awakening_system.doctors_robe": "Халат доктора (Диковина 5★)",
    "item.awakening_system.galaxy_lottery": "Большая галактическая лотерея (Диковина 5★)"
}

def update_json_file(filename, new_entries):
    filepath = os.path.join(LANG_DIR, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    count = 0
    for k, v in new_entries.items():
        if k not in data or data[k] != v:
            data[k] = v
            count += 1
            
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"Updated {filename}: added/modified {count} keys. Total keys: {len(data)}")

def main():
    # 1. Update es_es.json and all Spanish variants
    spanish_files = [f for f in os.listdir(LANG_DIR) if f.startswith("es_") and f.endswith(".json")]
    for sf in spanish_files:
        update_json_file(sf, TEYVAT_WOOD_KEYS_ES)

    # 2. Update en_us.json
    en_entries = {}
    en_entries.update(TEYVAT_WOOD_KEYS_EN)
    en_entries.update(MISSING_EN)
    update_json_file("en_us.json", en_entries)

    # 3. Update ru_ru.json
    ru_entries = {}
    ru_entries.update(TEYVAT_WOOD_KEYS_RU)
    ru_entries.update(MISSING_RU)
    update_json_file("ru_ru.json", ru_entries)

if __name__ == "__main__":
    main()
