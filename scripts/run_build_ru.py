# -*- coding: utf-8 -*-
import json, re, sys, os
sys.stdout.reconfigure(encoding='utf-8')

from ru_part1_ui_items import PART1
from ru_part2_skills_cultivation import PART2
from ru_part3_constellations import CONSTELLATION_NAMES, translate_reaction

en_path = 'src/main/resources/assets/awakening_system/lang/en_us.json'
ru_path = 'src/main/resources/assets/awakening_system/lang/ru_ru.json'

with open(en_path, 'r', encoding='utf-8') as f:
    en_data = json.load(f)

ru_data = {}

# Word and phrase lexicon for translating skills, titles, descriptions, and lore
LEXICON = [
    # Stats and Mechanics
    ('Attack Damage', 'Урона от атак'),
    ('Attack Speed', 'Скорости атаки'),
    ('Movement Speed', 'Скорости передвижения'),
    ('Max Health', 'Макс. здоровья'),
    ('Knockback Resistance', 'Сопротивления отбрасыванию'),
    ('Armor Toughness', 'Твёрдости брони'),
    ('Armor', 'Брони'),
    ('Damage Reduction', 'Снижения урона'),
    ('Cooldown', 'Перезарядка'),
    ('Energy', 'Энергия'),
    ('Level', 'Уровень'),
    ('Experience', 'Опыт'),
    ('Critical Chance', 'Шанс крит. удара'),
    ('Critical Damage', 'Критический урон'),
    ('Mana', 'Мана'),
    ('Health', 'Здоровье'),
    ('Seconds', 'сек'),
    ('Second', 'сек'),
    ('Minutes', 'мин'),
    ('Minute', 'мин'),
    ('Requires', 'Требует'),
    ('Unlocked', 'Разблокировано'),
    ('Locked', 'Заблокировано'),
    ('Active', 'Активный'),
    ('Passive', 'Пассивный'),
    ('Cost', 'Стоимость'),
    ('Coins', 'Монет'),
    ('Coin', 'Монета'),

    # Hunter / Solo Leveling terms
    ('Shadow Monarch', 'Теневой Владыка'),
    ('Shadow Soldier', 'Теневой Солдат'),
    ('Shadow Soldiers', 'Теневые Солдаты'),
    ('Shadow Extraction', 'Извлечение Теней'),
    ('Shadow Step', 'Теневой Шаг'),
    ('Shadow Domain', 'Владения Теней'),
    ('Ruler\'s Authority', 'Власть Правителя'),
    ('Dragon Eyes', 'Глаза Дракона'),
    ('Bloodthirst', 'Жажда Крови'),
    ('Fourth Wall', 'Четвёртая Стена'),
    ('Iron Body', 'Железное Тело'),

    # Murim terms
    ('Mount Hua', 'Секта Хуашань'),
    ('Plum Blossom', 'Цветение Сливы'),
    ('Heavenly Demon', 'Небесный Демон'),
    ('Demonic Cult', 'Демонический Культ'),
    ('Dantian', 'Дантянь'),
    ('Qi Flow', 'Поток Ци'),
    ('Qi Gathering', 'Сбор Ци'),
    ('Qi', 'Ци'),
    ('Heavenly Tribulation', 'Небесное Бедствие'),
    ('Marrow Cleansing', 'Очищение Костного Мозга'),
    ('Rising Dragon', 'Взлетающий Дракон'),
    ('Realm Breakthrough', 'Прорыв Царства'),
    ('Black Tortoise', 'Чёрная Черепаха'),
    ('Soul Transformation', 'Трансформация Души'),
    ('Jiangshi', 'Цзянши'),
    ('Taoist Talisman', 'Талисман Даоса'),

    # Common prefixes / suffixes
    ('The Constellation', 'Созвездие'),
    ('The ', ''),
    ('Sword', 'Меч'),
    ('Blade', 'Клинок'),
    ('Dagger', 'Кинжал'),
    ('Daggers', 'Кинжалы'),
    ('Scythe', 'Коса'),
    ('Hammer', 'Молот'),
    ('Spear', 'Копьё'),
    ('Bow', 'Лук'),
    ('Crossbow', 'Арбалет'),
    ('Shield', 'Щит'),
    ('Helmet', 'Шлем'),
    ('Chestplate', 'Нагрудник'),
    ('Leggings', 'Поножи'),
    ('Boots', 'Ботинки'),
    ('Ring', 'Кольцо'),
    ('Amulet', 'Амулет'),
    ('Necklace', 'Ожерелье'),
    ('Crown', 'Корона'),
    ('Scroll', 'Свиток'),
    ('Book', 'Книга'),
    ('Key', 'Ключ'),
    ('Ticket', 'Билет'),
    ('Token', 'Жетон'),
    ('Pill', 'Пилюля'),
    ('Altar', 'Алтарь'),
    ('Gate', 'Врата'),
    ('Dungeon', 'Подземелье'),
    ('Tower', 'Башня'),
    ('Pagoda', 'Пагода')
]

def translate_phrase(text):
    for eng, ru in LEXICON:
        text = re.sub(r'\b' + re.escape(eng) + r'\b', ru, text, flags=re.IGNORECASE)
    return text

print('Loaded lexicon with', len(LEXICON), 'terms.')

# Processing all keys
for k, v in en_data.items():
    if k in PART1:
        ru_data[k] = PART1[k]
    elif k in PART2:
        ru_data[k] = PART2[k]
    elif '.react.' in k:
        ru_data[k] = translate_reaction(v)
    elif k.startswith('awakening_system.constellation.'):
        parts = k.split('.')
        c_id = parts[2]
        c_type = parts[3] if len(parts) > 3 else ''
        if c_type == 'name':
            ru_data[k] = CONSTELLATION_NAMES.get(c_id, translate_phrase(v))
        elif c_type == 'stigma_effect':
            ru_data[k] = translate_phrase(v)
        elif c_type in ['title', 'stigma']:
            ru_data[k] = translate_phrase(v)
        else:
            ru_data[k] = translate_phrase(v)
    else:
        ru_data[k] = translate_phrase(v)

# Verify key count
missing = [k for k in en_data if k not in ru_data]
print(f'Total keys in EN: {len(en_data)}, Total keys in RU: {len(ru_data)}, Missing: {len(missing)}')

# Ensure roulette token is strictly physical in Russian
ru_data['item.awakening_system.roulette_token'] = 'Жетон физической рулетки'
ru_data['item.awakening_system.roulette_token.desc'] = '§7Мистический жетон, наполненный космической вероятностью для физического алтаря рулетки.'
ru_data['item.awakening_system.roulette_token.use_altar'] = '§6✦ Вставьте в Алтарь Рулетки в мире, чтобы вращать 3D-рулетку.'
ru_data['item.awakening_system.roulette_token.must_use_on_altar'] = '§c[ФИЗИЧЕСКАЯ РУЛЕТКА] §7Этот жетон необходимо вставить непосредственно в Алтарь Рулетки в мире.'

# Save ru_ru.json
with open(ru_path, 'w', encoding='utf-8') as f:
    json.dump(ru_data, f, indent=2, ensure_ascii=False)

print('ru_ru.json successfully saved!')
