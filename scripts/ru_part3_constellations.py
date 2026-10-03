# -*- coding: utf-8 -*-
import re

CONSTELLATION_NAMES = {
    'abyssal_black_dragon': 'Чёрный Дракон Бездны',
    'abyssal_eye_stalker': 'Око-Преследователь Бездны',
    'aether_core_monarch': 'Монарх Эфирного Ядра',
    'architect_of_the_system': 'Архитектор Системы',
    'black_dragon_avenger': 'Мститель Чёрного Дракона',
    'blood_red_commander': 'Кроваво-красный Командир',
    'blue_dragon_of_retribution': 'Синий Дракон Возмездия',
    'caregiver_of_the_abyss': 'Опекун Бездны',
    'death_defying_regressor': 'Бросающий Вызов Смерти Регрессор',
    'death_king_hunter': 'Охотник Король Смерти',
    'demon_king_of_salvation': 'Демонический Король Спасения',
    'demon_like_judge_of_fire': 'Демоноподобный Огненный Судья',
    'dragon_emperor': 'Император Драконов',
    'dragon_emperor_of_destruction': 'Император Драконов Разрушения',
    'eternal_rebirth_phoenix': 'Феникс Вечного Возрождения',
    'father_of_the_rich_night': 'Отец Богатой Ночи',
    'first_sword_of_goryeo': 'Первый Меч Корё',
    'flame_emperor': 'Пламенный Император',
    'grand_marshal_of_shadows': 'Великий Маршал Теней',
    'great_sage_heaven_equal': 'Великий Мудрец, Равный Небу',
    'heavenly_demon_ascendant': 'Вознесённый Небесный Демон',
    'hell_dimension_survivor': 'Выживший в Адском Измерении',
    'iron_blooded_sword_saint': 'Железнокровный Святой Меча',
    'judge_of_the_underworld_scales': 'Судья Весов Загробного Мира',
    'lightning_titan_olympus': 'Громовой Титан Олимпа',
    'mad_dog_of_the_spear': 'Бешеный Пёс Копья',
    'maritime_war_god': 'Морской Бог Войны',
    'martial_king_nayuta': 'Боевой Король Наюта',
    'master_of_mobius_gacha': 'Мастер Гачи Мёбиуса',
    'master_of_steel': 'Повелитель Стали',
    'master_of_the_infinite_labyrinth': 'Хозяин Бесконечного Лабиринта',
    'master_of_the_underworld': 'Владыка Подземного Царства',
    'monarch_of_eternal_calamity': 'Монарх Вечного Бедствия',
    'murim_heavenly_demon': 'Небесный Демон Мурима',
    'nine_heavens_golden_dragon': 'Золотой Дракон Девяти Небес',
    'plum_blossom_sword_saint': 'Святой Меча Цветения Сливы',
    'primordial_shadow_lord': 'Изначальный Повелитель Теней',
    'prisoner_of_the_golden_headband': 'Узник Золотого Обруча',
    'queen_of_the_darkest_night': 'Королева Самой Тёмной Ночи',
    'radiant_sword_princess': 'Принцесса Сияющего Меча',
    'reaper_of_drifting_moon': 'Жнец Плывущей Луны',
    'righteous_sword_of_mount_hua': 'Праведный Меч Хуашань',
    'ruler_of_the_howling_gales': 'Владыка Воющих Ветров',
    'sacred_water_dragon': 'Священный Водный Дракон',
    'salvation_reader': 'Читатель Спасения',
    'scribe_of_cosmic_destiny': 'Летописец Космической Судьбы',
    'secretive_plotter': 'Скрытный Интриган',
    'shadow_monarch': 'Теневой Владыка',
    'shadow_monarch_ashborn': 'Теневой Владыка Эшборн',
    'silver_moon_huntress': 'Охотница Серебряной Луны',
    'strategist_of_the_blue_flame': 'Стратег Синего Пламени',
    'supreme_estate_engineer': 'Величайший Инженер Поместий',
    'supreme_regressor_of_steel': 'Верховный Стальной Регрессор',
    'tower_gate_opener': 'Открывающий Врата Башни',
    'weaver_of_forgotten_fate': 'Ткач Забытой Судьбы'
}

def translate_reaction(text):
    m = re.match(r"§6\[The constellation '§e%s§6' (.*?)[.!] (?:Has sponsored|Sponsored) §e%s coins§6\.\]", text)
    if not m:
        return text
    action = m.group(1)
    
    # Simple common replacements for constellation actions
    reps = [
        ('adjusts his cold gaze and nods quietly at your calculated progression', 'устремляет холодный взор и спокойно кивает вашему расчетливому прогрессу'),
        ('analyzes your vital metrics and advises calculating an escape route', 'анализирует ваши показатели и советует подготовить путь к отступлению'),
        ('smirks faintly as the projected variable completes successfully', 'едва заметно усмехается, видя успешное завершение расчетной переменной'),
        ('examines the rift variables with sharp, analytical scrutiny', 'внимательно изучает переменные разлома с аналитическим расчётом'),
        ('observes the ruthless synchronization of your combo with tactical interest', 'с тактическим интересом наблюдает за безупречной точностью ваших комбо'),
        ('silently notes the altered causality variables following your rise', 'молча отмечает изменение переменных причинности после вашего роста'),
        ('recalibrates the scenario odds with an enigmatic faint smirk', 'пересчитывает шансы сценария с загадочной лёгкой усмешкой'),
        ('reminds you that dying prematurely ruins every grand design', 'напоминает, что преждевременная гибель рушит любой великий замысел'),
        ('calculates your optimal escape trajectory and dispatches aid', 'рассчитывает вашу оптимальную траекторию отхода и направляет помощь'),
        ('crosses another inevitable hurdle off the chessboard with cold satisfaction', 'с холодным удовлетворением вычёркивает очередное препятствие с шахматной доски'),
        ('ponders how this divergence deviates from previous timelines', 'размышляет, насколько это событие отклоняется от предыдущих временных линий'),
        ('advises you to anticipate hidden scenario mechanics ahead', 'советует заранее просчитывать скрытые механики предстоящего сценария'),
        ('watches from the gloom waiting for you to decipher the core puzzle', 'наблюдает из темноты, ожидая, пока вы разгадаете суть головоломки'),
        ('commends the ruthless efficiency of your chained maneuvers', 'одобряет беспощадную эффективность вашей цепочки маневров'),
        ('acknowledges that your offensive execution leaves zero openings', 'признает, что ваша атакующая техника не оставляет противнику ни единой бреши')
    ]
    for eng, ru in reps:
        if eng in action:
            action = action.replace(eng, ru)
            break
            
    return f"§6[Созвездие '§e%s§6' {action}. Пожертвовало §e%s монет§6.]"
