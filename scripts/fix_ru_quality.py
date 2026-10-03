# -*- coding: utf-8 -*-
import json, re, sys

sys.stdout.reconfigure(encoding='utf-8')

ru_path = 'src/main/resources/assets/awakening_system/lang/ru_ru.json'
en_path = 'src/main/resources/assets/awakening_system/lang/en_us.json'

with open(ru_path, 'r', encoding='utf-8') as f:
    ru = json.load(f)

with open(en_path, 'r', encoding='utf-8') as f:
    en = json.load(f)

FIXES = {
    'awakening_system.screen.shop.price': '%s монет | x%s',
    'awakening_system.class.sword_saint.short': '§aСвятой Меча',
    'awakening_system.rune_stone.dragon_eyes.desc': '§7ПКМ: §fПробуждает драконье зрение. §cОтображает точное здоровье, уровень и броню §fлюбой цели, на которую вы смотрите в вашем HUD.',
    'awakening_system.rune_stone.iron_body.desc': '§7ПКМ: §fЗакаливает ваше тело, как сталь. Дарует §e+4 постоянной пассивной брони §fи сопротивление отбрасыванию.',
    'awakening_system.screen.class_select.selected': '§a● ВЫБРАНО',
    'awakening_system.hud.trance_title': '⚔ РЕЖИМ ТРАНСА! (%s с)',
    'awakening_system.notification.constellation.sponsor_broadcast_msg': '§d%s §eпожертвовал §6%s монет §f%s§e.\n%s',
    'awakening_system.screen.roulette.cost': '§7Стоимость вращения: §e%s ⛃ Монет  §8|  §7Ваш баланс: %s ⛃',
    'awakening_system.screen.roulette.spin_btn': '⚡ ВРАЩАТЬ РУЛЕТКУ СИСТЕМЫ (%s ⛃) ⚡',
    'awakening_system.screen.dungeon_key.danger.low': '§a§lНИЗКИЙ',
    'awakening_system.action.imminent_collapse': '§c§lНЕИЗБЕЖНЫЙ ОБВАЛ! §e%s с...',
    'awakening_system.item.thousand_year_ginseng.consumed_desc': '§fВы поглотили Тысячелетний Женьшень: §b+500 Ци§f, и ваши духовные каналы озарились небесной силой.',
    'awakening_system.item.snow_lotus.consumed_desc': '§fСнежный Лотос очистил ваш Дантянь: §b+300 Ци§f.',
    'awakening_system.goddess_altar.blessing_fortune_toast': '§aБлагодать Фортуны дарована! (+%s Монет)',
    'awakening_system.goddess_altar.blessing_aegis_toast': '§eСвященная Эгида активирована!',

    # Colorized UI elements
    'awakening_system.screen.bank.virtual': '§7Виртуальный счёт: §e%s §6⛃',
    'awakening_system.screen.bank.physical': '§7В инвентаре: §b%s §3Монет',
    'awakening_system.screen.bank.deposit_header': '§a§l[ 📥 ВНЕСТИ МОНЕТЫ ]',
    'awakening_system.screen.bank.deposit_hint': '§8Внести на виртуальный баланс',
    'awakening_system.screen.bank.withdraw_header': '§e§l[ 📤 ВЫВЕСТИ МОНЕТЫ ]',
    'awakening_system.screen.bank.withdraw_hint': '§8Снять в виде физических предметов',
    'awakening_system.screen.roulette.desc1': '§7Вращайте легендарную §6Рулетку Системы§7, чтобы получить мистические награды.',
    'awakening_system.screen.roulette.desc2': '§8Выигранная награда будет помещена прямо в ваш инвентарь.',
    'awakening_system.screen.roulette.potential': '§bПОТЕНЦИАЛЬНЫЕ НАГРАДЫ РУЛЕТКИ:',
    'awakening_system.screen.skills.no_actives': '§cУ вас пока нет активных навыков.',
    'awakening_system.screen.skills.no_actives_hint': '§7Побеждайте враждебных монстров (Скелетов, Криперов, Утопленников...), чтобы усваивать навыки.',
    'awakening_system.screen.skills.no_passives': '§cВы ещё не пробудили пассивные навыки.',
    'awakening_system.screen.skills.no_passives_hint': '§7Добывайте руды, рубите деревья или исследуйте мир, чтобы пробудить пассивные инстинкты.',
    'awakening_system.screen.status.cultivation_btn': '§a§l[ ☯ КУЛЬТИВАЦИЯ МУРИМА ☯ ]',
    'awakening_system.screen.skills.in_slot': '§a[В слоте %s] §7✕',

    # Physical roulette token and altar
    'item.awakening_system.roulette_token': 'Жетон физической рулетки',
    'item.awakening_system.roulette_token.desc': '§7Мистический жетон, наполненный космической вероятностью для физического алтаря рулетки.',
    'item.awakening_system.roulette_token.use_altar': '§6✦ Вставьте в Алтарь Рулетки в мире, чтобы вращать 3D-рулетку.',
    'item.awakening_system.roulette_token.must_use_on_altar': '§c[ФИЗИЧЕСКАЯ РУЛЕТКА] §7Этот жетон необходимо вставить непосредственно в Алтарь Рулетки в мире.',
    'block.awakening_system.roulette_altar': 'Алтарь Рулетки Системы',
    'block.awakening_system.roulette_altar.desc': '§7Древний алтарь вероятностей, материализующий награды Системы в мире.',
    'block.awakening_system.roulette_altar.use': '§e[ПКМ с Жетоном] §fЗапускает физическое вращение рулетки.',

    # Remaining exact Russian localizations
    'awakening_system.screen.ranking.col.level': 'УР.',
    'awakening_system.screen.constellation.coins': '§eМонеты: §6%s ⛃',
    'awakening_system.screen.constellation.live': '§fВ ЭФИРЕ §7[#%s]',
    'awakening_system.screen.constellation.dim': '§7Измерение: §f%s',
    'awakening_system.screen.cultivation.qi_progress': '§bЦи: %s/%s',
    'awakening_system.screen.constellation.level_tag': '§8Ур.§b%s §7[%s]',
    'item.awakening_system.soul_transformation_pill.used': '§aВы поглотили Пилюлю Трансформации Души (+600 Ци).',
    'awakening_system.grotto.puzzle_solved_desc': '§fКруг У-Син в гармонии. Внутреннее святилище открылось.',
    'block.awakening_system.elemental_barrier': 'Стихийный барьер',
    'item.awakening_system.elemental_demon_crown.desc': '§7Мифическая корона, выкованная в узле Изначального Хаоса. Дарует невосприимчивость к отбрасыванию и усиливает стихийную магию на 40%.',
    'item.awakening_system.elemental_castle_key': 'Ключ от Парящих Замков',
    'container.awakening_system.spirit_jade_furnace': 'Печь Духовного Нефрита (Скорость 25x)',
    'item.awakening_system.upgrade_spirit_jade_to_astral': 'Улучшение: От Духовного Нефрита к Астральной Печи',
    'subtitles.awakening_system.goblin_ambient': 'Гоблин хихикает',
    'subtitles.awakening_system.goblin_chant': 'Племенное песнопение шамана'
}

for k, v in FIXES.items():
    ru[k] = v

# Ensure exact key ordering
ordered_ru = {k: ru[k] for k in en}

with open(ru_path, 'w', encoding='utf-8') as f:
    json.dump(ordered_ru, f, ensure_ascii=False, indent=2)

print("Quality fixes applied to ru_ru.json successfully!")
